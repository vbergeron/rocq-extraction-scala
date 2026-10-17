# Plan: sbt and Mill plugins for Scala extraction

Status: the core and the Mill plugin are started in `integrations/`
(declarative extractions, main modules, `RocqModule` dependencies); the
sbt plugin is not.

## Goal

Let a Scala 3 project keep Rocq sources next to its Scala sources and have
its build tool turn them into Scala code on every compile, with no
hand-run `rocq c` step and no extracted code committed to the repository:

```scala
// build.sbt
enablePlugins(RocqExtractionPlugin)
rocqScalaPackage := Some("com.example.verified")
rocqExtractions  += RocqExtraction("Arith", "MyProject.Arith", Seq("addNat", "mulNat"))
```

```scala
// build.mill
object app extends ScalaModule with RocqExtractionModule {
  def scalaVersion     = "3.7.3"
  def rocqScalaPackage = Some("com.example.verified")
  def rocqExtractions  = Seq(RocqExtraction("Arith", "MyProject.Arith", Seq("addNat", "mulNat")))
}
```

`compile` then compiles the `.v` files, runs the extraction, and compiles
the resulting `.scala` files with the rest of the module. Editing a `.v`
file and recompiling (or `~compile` / `mill -w`) picks up the change.

## What the plugins build on

Facts about this repository that shape the design:

- The extractor is a Rocq plugin, installed with opam. The build plugins do
  **not** ship it: they drive a `rocq` binary that must already have
  `rocq-extraction-scala` installed, and check that it does (see
  "Toolchain").
- `Extraction "Name" ...` writes `Name.scala` holding one `object Name`
  (`src/scala.ml`, `file_naming`/`preamble`); `Name` must be a valid Scala
  identifier. `Set Extraction Output Directory` decides where the file goes
  (`tests/filename/filename.v`).
- `Set Extraction Scala Package "a.b"` and
  `Set Extraction Scala Pure Functions` are ordinary options, so a
  generated `.v` file can set them per extraction.
- Modular extraction (`Separate Extraction`, `Extraction Library`) is not
  supported, so the unit of output is one `Extraction "Name" ...` command,
  one file, one object.
- The extracted code needs **no runtime library**: `RocqLazy` and the
  dummy value `__` are emitted inside each object. The plugins therefore
  add no library dependency, only generated sources.
- The output is Scala 3 only. Pure functions mode needs a recent Scala 3
  (checked with 3.9).

## Shared design

Both plugins are thin adapters over one build-tool-agnostic library,
`rocq-extraction-build-core`, so that the behaviour (and its bugs) is the
same in both tools.

### Inputs

1. **Rocq source roots**, each with a logical name:
   `Seq(RocqRoot(dir, "MyProject"))`, turned into `-Q dir MyProject`.
   Default: `src/main/rocq` (sbt) / `rocq/` (Mill), logical name derived
   from the project/module name.
2. **Extra load paths** for already-built Rocq libraries
   (`-Q`/`-R` pairs), e.g. an opam-installed library or another module's
   output (see "Module dependencies").
3. **Extractions**, two ways of declaring them:
   - *Declarative* (the default): a list of
     ```scala
     final case class RocqExtraction(
       name: String,                       // file and object name
       module: String,                     // logical path to Require
       definitions: Seq[String],           // what to extract
       scalaPackage: Option[String] = None,// overrides the global one
       pureFunctions: Option[Boolean] = None,
     )
     ```
     plus global settings: package, pure functions, and *preludes*, the
     `ExtrScala*` files to import (`Basic`, `NatBigInt`, `ZBigInt`, as an
     enum, default `Basic`).
   - *Script*: `.v` files the user writes themselves, containing their
     own `Extraction "..."` commands (listed in a setting, e.g.
     `rocqExtractionScripts`). For projects that need `Extract Constant`,
     `Extract Inductive` or anything else the declarative form doesn't
     cover.

   The declarative form is implemented on top of the script one: it
   generates a script.

### Pipeline

```
.v sources ──rocq dep──> order ──rocq c (each file)──> .vo
                                                        │
generated driver .v  ───────────rocq c─────────────────┘──> tmp/*.scala
                                                                │
                                         sync (copy changed, delete stale)
                                                                ▼
                                          managed/generated source dir
```

1. **Dependency order.** Run `rocq dep` on the source roots and parse its
   make-style output (`A.vo ...: A.v B.vo`) into a graph, then a
   topological order. A cycle or a missing logical path is a build error
   naming the file.
2. **Compile.** Each source root is mirrored into the build output
   directory (its `.v` files copied to `<out>/roots/MyProject`, the files
   of deleted `.v` files removed), and each copy compiled in place with
   `rocq c -q -Q <out>/roots/MyProject MyProject ... A.v`, in order. So
   `.vo` files never go into the source tree and each logical name has one
   directory; file names in Rocq's messages are mapped back to the
   sources. A file is recompiled when its content, a file it requires, a
   compiled library of the load paths (by size and date), the flags or the
   Rocq version changed (hashes kept in `<out>/state`). Compiling
   independent files in parallel is left for later.
3. **Driver.** For declarative extractions, generate one driver file:
   ```coq
   (* Generated by the Rocq extraction build plugin. Do not edit. *)
   From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.
   Require MyProject.Arith.
   Extraction Language Scala.
   Set Extraction Output Directory "<tmp>".
   Set Extraction Scala Package "com.example.verified".
   Extraction "Arith" MyProject.Arith.addNat MyProject.Arith.mulNat.
   ```
   One file for all extractions, with `Set`/`Unset` between them when
   their options differ. Script mode runs the user's scripts the same
   way, after injecting the output directory (see "Open questions").
4. **Collect.** Extraction does not report the files it writes, so each
   run extracts into an empty temporary directory, and the result is
   synced into the managed source directory: changed files copied, files
   no longer produced deleted, unchanged files left alone (their
   timestamps unchanged so the Scala compiler's incremental analysis isn't
   invalidated). Files are placed under their package's directories
   (`com/example/verified/Arith.scala`), as IDEs expect.
5. **Validate.** Two extractions with the same name, a name that is not a
   valid identifier, or a package that `Set Extraction Scala Package`
   would reject, are reported before running Rocq.

### Toolchain

- The `rocq` binary comes from a setting, default `rocq` on the `PATH`.
  Optional extra environment (`OCAMLPATH`, `ROCQPATH`, ...) and an optional
  wrapper prefix, for `opam exec --switch=<s> --` or `nix develop -c`.
- Before the first compile, a check (cached per binary and version) runs
  `rocq --version` and compiles a one-line file requiring
  `ScalaExtraction`. If it fails, the error says what is missing and how to
  install it (`opam install rocq-extraction-scala`, plus the
  pin-to-master instructions from the README while those are needed).
- The Rocq version is part of every cache key.

### Errors

Rocq errors (`File "...", line N, characters a-b:` followed by the
message) are parsed and reported through each tool's problem reporter
(sbt `xsbti.Problem`, Mill's `CompileProblemReporter`), so they show up
with positions in the terminal and through BSP in Metals/IntelliJ.
Anything unparsed is shown verbatim. A failing `Fail` command or a
warning is passed through as a warning.

### Scala version checks

A warning when the module's Scala version is 2.x (the output is Scala 3),
and when pure functions mode is on with a Scala 3 older than the version
it was checked with.

## sbt plugin: `sbt-rocq-extraction`

- An `AutoPlugin` (`requires = JvmPlugin`, `trigger = noTrigger`, enabled
  with `enablePlugins(RocqExtractionPlugin)`), built for sbt 1.x
  (Scala 2.12) first, sbt 2.x (Scala 3) once the core cross-builds.
- Keys, all scoped per configuration (`Compile`, `Test`) with `inConfig`:

  | Key | Type | Default |
  |---|---|---|
  | `rocqSourceRoots` | `Seq[RocqRoot]` | `src/<config>/rocq` with logical name `name.value` capitalized |
  | `rocqLoadPaths` | `Seq[RocqLoadPath]` | empty |
  | `rocqBinary` | `String` | `"rocq"` |
  | `rocqCommandPrefix` / `rocqEnv` | `Seq[String]` / `Map[String, String]` | empty |
  | `rocqFlags` | `Seq[String]` | empty |
  | `rocqPreludes` | `Seq[RocqPrelude]` | `Seq(RocqPrelude.Basic)` |
  | `rocqScalaPackage` | `Option[String]` | `None` |
  | `rocqPureFunctions` | `Boolean` | `false` |
  | `rocqExtractions` | `Seq[RocqExtraction]` | empty |
  | `rocqExtractionScripts` | `Seq[File]` | empty |
  | `rocqCompile` | task, `File` (the `.vo` directory) | |
  | `rocqExtract` | task, `Seq[File]` | |

- Wiring:
  ```scala
  Compile / sourceGenerators += (Compile / rocqExtract).taskValue
  ```
  with output in `(Compile / sourceManaged).value / "rocq"`, and the
  source roots registered as `fileInputs` (sbt 1.4+), so `~compile`
  watches the `.v` files and `rocqExtract` uses `changedInputFiles` for
  incrementality.
- `Test` gets the same settings with its own roots, so tests can extract
  test-only Rocq code; it sees `Compile`'s `.vo` output on its load path.
- Logs go to `streams.value.log`; Rocq output is shown at `info` when
  `rocqExtract` is run directly and at `debug` from `compile`.
- Tests: sbt `scripted` tests under `src/sbt-test/`: a minimal project, a
  multi-file project with dependencies, script mode, a package, a Rocq
  error with its position, incremental rebuild (touching an unrelated
  `.v` file doesn't rewrite the `.scala` files), a missing toolchain.

## Mill plugin: `mill-rocq-extraction`

- Built against Mill 1.x (Scala 3), the version new projects use; Mill
  0.12 support only if asked for. Loaded from `build.mill` with a
  `//| mvnDeps: ["io.github.vbergeron::mill-rocq-extraction::<ver>"]`
  header.
- `trait RocqExtractionModule extends ScalaModule`:
  ```scala
  def rocqSources: T[Seq[PathRef]]        = Task.Sources("rocq")
  def rocqLogicalName: T[String]          = Task { moduleSegments.parts.last.capitalize }
  def rocqLoadPaths: T[Seq[RocqLoadPath]] = Task { Seq.empty }
  def rocqBinary: T[String]               = Task { "rocq" }
  def rocqCommandPrefix: T[Seq[String]]   = Task { Seq.empty }
  def rocqFlags: T[Seq[String]]           = Task { Seq.empty }
  def rocqPreludes: T[Seq[RocqPrelude]]   = Task { Seq(RocqPrelude.Basic) }
  def rocqScalaPackage: T[Option[String]] = Task { None }
  def rocqPureFunctions: T[Boolean]       = Task { false }
  def rocqExtractions: T[Seq[RocqExtraction]] = Task { Seq.empty }
  def rocqExtractionScripts: T[Seq[PathRef]]  = Task { Seq.empty }

  def rocqCompile: T[PathRef]      // .vo files in Task.dest
  def rocqExtract: T[Seq[PathRef]] // .scala files in Task.dest

  override def generatedSources = Task { super.generatedSources() ++ rocqExtract() }
  ```
  The core's case classes get upickle `ReadWriter`s (in the Mill module,
  so the core stays dependency-free). Mill's task cache gives
  incrementality for free at the task level; the core's per-file hashes
  still avoid recompiling unchanged `.v` files inside `rocqCompile`
  (`Task(persistent = true)`).
- A `RocqModule` trait for modules that only hold Rocq code: its
  `rocqCompile` output is added to the load path of every module that
  lists it in `moduleDeps`, so a Rocq library can be shared by several
  Scala modules. `RocqExtractionModule extends RocqModule`.
- A `TestModule` inside a `RocqExtractionModule` mixes in the trait with
  `rocq` sources under `test/rocq`.
- Tests: `mill-testkit` integration tests with the same scenarios as the
  sbt scripted tests, plus `moduleDeps` between Rocq modules.

## Repository layout and build

In this repository, so that the build plugins are versioned and tested
with the extractor they drive. Each plugin is built by its own tool: a
Mill build for the core and the Mill plugin, an sbt build for the sbt
plugin.

```
integrations/
  build.mill              # Mill build: core + Mill plugin
  core/                   # rocq-extraction-build-core
    src/                  #   shared sources, valid Scala 2.12 and 3
    test/
  mill-plugin/            # mill-rocq-extraction, Scala 3, Mill libs compileMvnDeps
  sbt-plugin/             # sbt build: sbt-rocq-extraction, Scala 2.12
    build.sbt
    project/
    src/main/scala/
    src/sbt-test/         #   scripted tests
  examples/{sbt,mill}/    # small projects, also used by the docs
```

- `core` has no dependencies beyond the JDK (`ProcessBuilder`,
  `java.nio.file`), so it causes no conflicts inside either tool's
  classloader, and its sources compile unchanged with Scala 2.12 and 3.
  Unit tests (munit) for the `rocq dep` parser, toposort, driver
  generation, output sync and error parsing, with fake `rocq` executables
  where a process is needed.
- **Mill build** (`integrations/build.mill`): `core` as a cross module
  over Scala 3 (published, used by the Mill plugin) and 2.12 (compiled and
  tested only, never published: it guards the sources against Scala 3
  only syntax, which would otherwise break only the sbt build).
  `millPlugin` depends on `core[3]`, has the Mill libraries as compile-only
  dependencies, publishes with Mill's plugin suffix (`_mill1_3`), and runs
  `mill-testkit` integration tests over `examples/mill` and smaller test
  projects. Both artifacts are published to Maven Central with Mill's
  `SonatypeCentralPublishModule`.
- **sbt build** (`integrations/sbt-plugin`): one project with
  `sbtPlugin := true`. It compiles the core's sources directly
  (`Compile / unmanagedSourceDirectories += baseDirectory.value / ".." / "core" / "src"`)
  instead of depending on a published core: no Scala 2.12 core artifact,
  no ordering between the two builds when developing (`sbt scripted`
  works on a fresh checkout) or releasing, and the plugin jar is
  self-contained. Tested with `scripted` over `src/sbt-test/` and
  `examples/sbt`; published with `sbt-ci-release`.
- Both builds read the version from one place (a `VERSION` file in
  `integrations/`, or the git tag `integrations-vX.Y.Z`), so a release
  publishes the core, the Mill plugin and the sbt plugin with the same
  version, independently of the opam package's version. The README
  documents which plugin versions go with which extractor versions (the
  plugins only rely on commands and options, so this should rarely
  matter).
- CI: a new `integrations` job in `.github/workflows/ci.yml`, after the
  existing one: it reuses the cached Rocq install, `dune install`s the
  extractor into the same prefix, installs a JDK, Mill and sbt, then runs
  `./mill __.test` (core unit tests for both Scala versions, Mill
  integration tests) and `sbt scripted` in `sbt-plugin/`. The core unit
  tests need no Rocq and also run on their own. A release job on
  `integrations-v*` tags runs `./mill mill.scalalib.SonatypeCentralPublishModule/`
  and `sbt ci-release`.

## Milestones

1. *(partly done: `rocq dep`'s output and the out-of-tree compile are
   settled, the script mode output directory is not)* **Prototype the Rocq side** with plain shell commands, to settle the
   open questions below: `-o` plus a second `-Q` for out-of-tree `.vo`
   files, injecting the output directory into script mode, `rocq dep`
   output on a multi-root project.
2. *(done)* **Mill build and core library**: `build.mill` with the cross-built
   core; toolchain check, `rocq dep` parsing, incremental compile, driver
   generation, output sync, error parsing; unit tests on Scala 2.12 and 3.
3. *(done, except `examples/mill`: the integration test project stands in
   for it)* **Mill plugin**, declarative mode, main module only;
   integration tests; example project.
4. **sbt build and plugin**, same scope, compiling the core's sources;
   scripted tests; example project.
5. Script mode, `Test` configuration / test modules. (Mill `RocqModule`
   dependencies are done, through `rocqModuleDeps`.)
6. Error positions through the problem reporters (BSP); Scala version
   warnings.
7. *(CI done for the core and the Mill plugin)* CI job, publishing, README section ("Using from sbt / Mill").
8. Later, if wanted: sbt 2.x, Mill 0.12, a `rocqToolchain` setting that
   provisions Rocq itself (opam switch or Nix), and a `rocqDoc` task.

## Open questions

- **Output directory in script mode.** The declarative driver sets
  `Extraction Output Directory` itself, but a user script may not. Options
  to verify: `rocq c -set "Extraction Output Directory=<tmp>"` (the option
  only exists once the extraction plugin is loaded, so this may be
  rejected at startup), or running `rocq c` with `<tmp>` as working
  directory, or prepending a generated file with `-l`. Settle in milestone
  1; if none works, require scripts to leave the option unset and run them
  from `<tmp>`.
- ~~**Out-of-tree `.vo` files.**~~ Settled: sources are mirrored into the
  output directory by copying (see "Pipeline"), which needs nothing
  particular from Rocq.
- **Duplicated types across extractions.** Two extractions that both use
  `nat` each get their own `Nat` inside their own object, and the two are
  different Scala types. The docs should recommend one extraction per
  set of definitions that exchange values; whether the plugin should warn
  when it sees this is open.
- **Plugin names and organization** (`sbt-rocq-extraction`,
  `mill-rocq-extraction`, `io.github.vbergeron`) are placeholders.

# Build tool plugins

Plugins running the Scala extraction from Scala builds, so that Rocq
files sit next to Scala sources and are extracted on every compile. See
[`docs/build-tool-plugins.md`](../docs/build-tool-plugins.md) for the
design.

- `core/`: `rocq-extraction-build-core`, everything that is not specific
  to a build tool: running `rocq`, ordering and incrementally compiling
  `.v` files out of the source tree, generating the extraction driver,
  syncing the extracted files, parsing Rocq's errors. Depends only on the
  JDK; its sources compile with Scala 3 and 2.12 (for the sbt plugin, to
  come).
- `mill-plugin/`: `mill-rocq-extraction`, the Mill 1.x glue over the core.

Both are built with Mill (`build.mill`).

## Using the Mill plugin

Rocq with `rocq-extraction-scala` must be installed (see the
[installation](../README.md#installation)).

```scala
//| mvnDeps:
//| - io.github.vbergeron::mill-rocq-extraction::0.1.0-SNAPSHOT
package build

import mill.*, scalalib.*
import rocqextraction.*

/** Rocq files only, in lib/rocq/, under the logical name `Lib`. */
object lib extends RocqModule

/** Rocq files in app/rocq/ (logical name `App`), extracted into the
  * module's generated sources. */
object app extends ScalaModule, RocqExtractionModule {
  def scalaVersion = "3.7.4"
  override def rocqModuleDeps = Seq(lib)
  def rocqPreludes = Seq(RocqPrelude.NatBigInt)
  def rocqScalaPackage = Some("com.example.verified")
  def rocqExtractions = Seq(RocqExtraction("Arith", "App.Arith", Seq("addNat")))
}
```

`./mill app.compile` compiles the `.v` files, extracts `addNat` to
`com/example/verified/Arith.scala` (`object Arith`), and compiles it with
`app`'s sources. Only the `.v` files that changed, or that require one
that changed, are recompiled.

| Task | Default | |
|---|---|---|
| `rocqSources` | `rocq/` | The module's `.v` files |
| `rocqLogicalName` | the module's name, capitalized | `rocq/A/B.v` is `<name>.A.B` |
| `rocqModuleDeps` | the `RocqModule`s among `moduleDeps` | Rocq modules whose files are required |
| `rocqLoadPaths` | none | Other compiled Rocq libraries (`RocqLoadPath`) |
| `rocqBinary`, `rocqCommandPrefix`, `rocqEnv` | `rocq`, none, none | How to run Rocq, e.g. `Seq("opam", "exec", "--")` as prefix |
| `rocqFlags` | none | Extra arguments of `rocq c` |
| `rocqPreludes` | `Basic` | The `ExtrScala*` files to import |
| `rocqScalaPackage` | none | `Set Extraction Scala Package` |
| `rocqPureFunctions` | `false` | `Set Extraction Scala Pure Functions` |
| `rocqExtractions` | none | The `RocqExtraction(name, module, definitions)` to run |
| `rocqCompile`, `rocqExtract` | | The compiled files, the extracted files |

## Developing

```sh
./mill __.compile
./mill __.test                  # core (Scala 3 and 2.12) and plugin unit tests
./mill mill-plugin.integration  # runs Mill on a project using the plugin
```

The tests that run Rocq are skipped when `rocq` with the extractor is not
found, unless `ROCQ_INTEGRATION=required` (as in CI).

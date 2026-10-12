# rocq-extraction-scala

A [Rocq](https://rocq-prover.org) plugin that adds **Scala 3** as a target
language for extraction.

```coq
From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Fixpoint addNat (n m : nat) : nat :=
  match n with
  | 0 => m
  | S p => S (addNat p m)
  end.

Recursive Extraction addNat.
```

```scala
object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def addNat: Nat => Nat => Nat =
    (n: Nat) => (m: Nat) => 
    n match {
    case O() => m
    case S(p) => S(addNat(p)(m))
    }

}
```

## Installation

```sh
opam install rocq-extraction-scala
```

It requires a Rocq release that includes the extraction API for external
languages ([rocq-prover/rocq#22541](https://github.com/rocq-prover/rocq/pull/22541)).

### From source

Until that API is in a Rocq release, pin Rocq to `master`, then build the
plugin:

```sh
opam pin add rocq-runtime git+https://github.com/rocq-prover/rocq#master
opam pin add rocq-core git+https://github.com/rocq-prover/rocq#master
opam pin add rocq-extraction-scala git+https://github.com/vbergeron/rocq-extraction-scala
```

## Usage

Load the plugin with `From ScalaExtraction Require Import ScalaExtraction.`
(or `Declare ML Module "rocq-extraction-scala.plugin".` after loading
the extraction plugin), then select Scala with `Extraction Language Scala.`
The usual extraction commands then produce Scala 3 source.

- Each extraction is wrapped in an `object`, named `Main` for extraction
  to the standard output, or after the file for `Extraction "file" ...`
  (so the file name must be a valid identifier).
- `Set Extraction Scala Package "com.foo.bar".` adds a `package` clause at
  the top of every extracted file. `Unset Extraction Scala Package.`
  removes it.
- `From ScalaExtraction Require Import ExtrScalaBasic.` extracts `bool`,
  `option`, `unit`, `list`, `prod` and `sum` to Scala's `Boolean`, `Option`,
  `Unit`, `List`, tuples and `Either` (and `sumbool`/`sumor` to `Boolean`/`Option`),
  like `ExtrOcamlBasic` does for OCaml.
  It also replaces their basic operations (`andb`, `orb`, `implb`, `xorb`,
  `negb`, `fst`, `snd`, `option_map`, `app`, `ListDef.map`) with Scala's own.
- `Extract Inductive` and `Extract Constant` work as for the other
  languages. Literals are extracted natively when `ascii`/`string` are
  extracted to `Char`/`String`.
- Modular extraction (`Separate Extraction`, `Extraction Library`) is not
  supported: use `Recursive Extraction` or `Extraction "file"`.

## Translation

Extraction first turns Rocq terms into an intermediate ML language (proofs and
types erased, as for OCaml), which this plugin prints as Scala 3.

| Rocq | Scala |
|---|---|
| Inductive type | `sealed trait` and one `final case class` per constructor, fields `_1`, `_2`, … |
| Type parameters | Scala type parameters: `Tree[a]` |
| Record | One `final case class` with named fields, plus a `type` alias when the record and constructor names differ |
| Single-constructor, single-field inductive | `type` alias of the field's type |
| Coinductive type | Case classes under `__Name`, and `type Name = RocqLazy[__Name[...]]`; constructors are wrapped in `RocqLazy(...)` and matches use `.force` |
| `Definition`, `Fixpoint` | `val` or `def` holding a curried function, with its full type: `def treeMap[t1, t2]: (t1 => t2) => Tree[t1] => Tree[t2]` |
| Polymorphic definition | `def` with type parameters `[t1, t2, ...]`; every use passes them explicitly: `treeMap[t1, t2](f)(l)` |
| Constructor of a polymorphic type | Explicit type arguments: `Node[t2](...)`, `MyNil[t1]()` |
| Application | Curried calls: `f(a)(b)` |
| `match` | `match` with constructor patterns |
| `let x := a in b` | `{ val x: T = a; b }`, or `{ def x[t3]: T = a; b }` when `a` is a function used at several types |
| Local `fix` | Block with a local `def` and its type, then the call |
| Lambda | Parameters with their types: `(x: Nat) => ...` |
| Proofs and type arguments | Erased; an erased value that is still needed is `__` (`val __ : Unit = ()`) |
| Axiom | `throw new RuntimeException("AXIOM TO BE REALIZED (...)")`, to replace with `Extract Constant` |
| Identifiers | `'` becomes `_`; type names are capitalized; Scala keywords are renamed |

Everything is printed inside one `object`, so two extracted files can be
compiled together without name clashes.

### Types

Extraction keeps the types of top-level definitions and constructor fields
only. Scala needs more: a type for every lambda parameter and local
definition, and type arguments wherever it cannot infer them. Before
printing, a type reconstruction pass (`src/typing/mltyping.ml`) therefore
computes the type of every node of each definition, in terms of the
intermediate language's own types: it is independent of Scala, and usable
by other extraction backends. It is Hindley-Milner type inference with
polymorphic `let`, extended in two ways:

- types flow from the context into lambdas and `match` branches, so that
  the parameters of a lambda passed to a function get the function's
  parameter types;
- it never fails: where the term has no ML type, it records a coercion
  between the type the term has and the type it is used at.

The printer then writes every binder with its type and every polymorphic
use with its type arguments, so that the Scala type of each expression is
exactly the one computed by the pass.

### Casts

A cast `.asInstanceOf[...]` is printed at a coercion only, when Scala does
not already accept the value at its use (Scala's functions are
contravariant, and every value is an `Any`). Casts state types the Rocq
program already guarantees; a cast failing at runtime would be a bug in
this plugin. They come from:

1. **Types extraction cannot express.** Rocq accepts programs that ML type
   systems reject, such as a result type computed from a value
   (`if b then nat else bool`), or an argument used at several types.
   Extraction types these parts as unknown, printed `Any`, and a cast
   brings the value back to its precise type where it is used:

   ```scala
   val dep: Bool => Any = ...
   val useDep: Nat = dep(True()).asInstanceOf[Nat]
   ```

2. **Match functions given by `Extract Inductive`.** Their Scala type is the
   user's: each branch is passed as an `Any => Any` function, and the result
   is cast back to the type of the `match`.

In the test suite, 3 of the 65 extracted definitions contain a cast. On the
JVM, casts to generic types are not checked at runtime, and casts to class
types are a single type check.

## Tests

```sh
dune test
```

Each test is a directory `tests/<t>/` holding `<t>.v`, which is run with
`rocq c`, and its expected output: `<t>.scala` when the output is a single
Scala file, `<t>.out` otherwise (error messages, several extractions). A test
writing a file with `Extraction "name"` has both: `<t>.out` for the output of
`rocq c` and `<t>.scala` for `name.scala`. After adding a test (with an empty
`<t>.out` if it needs one), run `dune build @gen --auto-promote` to
regenerate `tests/dune.inc`, then `dune test --auto-promote` to record its
output.

```sh
dune build @scalac
```

compiles the Scala code extracted by each test with `scalac` (Scala 3). It does
nothing when `scalac` is not on the `PATH`.

CI (`.github/workflows/ci.yml`) builds Rocq from `master`, runs the
tests and compiles their Scala output.

## License

LGPL 2.1, like Rocq: the backend is adapted from Rocq's extraction plugin.

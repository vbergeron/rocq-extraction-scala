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

## Requirements

The plugin uses the extraction plugin's API for external languages
([rocq-prover/rocq#22541](https://github.com/rocq-prover/rocq/pull/22541)).
Until it is merged, build Rocq from that branch:

```sh
opam pin add rocq-runtime git+https://github.com/vbergeron/rocq#extraction-external-languages
opam pin add rocq-core git+https://github.com/vbergeron/rocq#extraction-external-languages
```

## Installation

```sh
opam pin add rocq-extraction-scala git+https://github.com/vbergeron/rocq-extraction-scala
```

or, from a clone:

```sh
dune build
dune install
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
- `Extract Inductive` and `Extract Constant` work as for the other
  languages. Literals are extracted natively when `ascii`/`string` are
  extracted to `Char`/`String`.
- Modular extraction (`Separate Extraction`, `Extraction Library`) is not
  supported: use `Recursive Extraction` or `Extraction "file"`.

## Tests

```sh
dune test
```

Each `tests/*.v` file is run with `rocq c` and its output is compared with
the matching `.out` file.

```sh
dune build @scalac
```

compiles the Scala code printed by each test with `scalac` (Scala 3). It
does nothing when `scalac` is not on the `PATH`. After adding a test, run
`dune build @gen --auto-promote` to regenerate `tests/dune.inc`, then
`dune test --auto-promote` to record its output.

## License

LGPL 2.1, like Rocq: the backend is adapted from Rocq's extraction plugin.

# Example: a verified sort, from Rocq to Scala with Mill

A small project using the Mill plugin end to end:

- `sorting/rocq/Insertion.v`: insertion sort on natural numbers, with
  proofs that its result is sorted (`sort_sorted`) and a permutation of its
  input (`sort_count`). A Rocq-only module (`RocqModule`), logical name
  `Sorting`.
- `app/rocq/Stats.v`: `minimum` and `distinct`, written with that sort.
  Logical name `App`.
- `app/src/Main.scala`: a command line application using the extracted
  `Sorting.sort`, `Stats.minimum` and `Stats.distinct`.
- `app/test/src/VerifiedTests.scala`: checks the extracted code against
  Scala's own `sorted`, `minOption` and `distinct` on random lists.

`build.mill` extracts with the `NatBigInt` prelude, so `nat` becomes
`BigInt`, `list` becomes `List` and `option` becomes `Option`, into the
package `com.example.verified`.

## Running it

It needs Rocq with `rocq-extraction-scala` installed (see the
[installation](../../../README.md#installation)) and, until the plugin is
published, the plugin in your local Ivy repository:

```sh
(cd ../.. && ./mill __.publishLocal)
./mill app.run 5 3 8 1 3
./mill app.test
```

```
input:    5 3 8 1 3
sorted:   1 3 3 5 8
distinct: 1 3 5 8
minimum:  1
```

The extracted files are in `out/app/rocqExtract.dest/src/`. Editing a `.v`
file and running again recompiles that file and the files requiring it;
a broken proof fails the build with Rocq's message, on the source file.

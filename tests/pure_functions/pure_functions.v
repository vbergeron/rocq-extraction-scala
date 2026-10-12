(* Scala 3 extraction backend: [Set Extraction Scala Pure Functions]
   prints function types with the pure arrow [->] (and [RocqLazy]'s
   by-name parameters as [-> T]), and imports
   [language.experimental.pureFunctions] at the top of every extracted
   file, after the [package] clause if any. Lambdas keep [=>]: only
   types change. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Definition compose (A B C : Type) (g : B -> C) (f : A -> B) (x : A) : C := g (f x).

Definition twice (A : Type) (f : A -> A) : A -> A := fun x => f (f x).

CoInductive stream (A : Type) : Type :=
  | Scons : A -> stream A -> stream A.

Arguments Scons {A} _ _.

CoFixpoint const_stream (n : nat) : stream nat := Scons n (const_stream n).

Set Extraction Scala Pure Functions.

Recursive Extraction compose twice const_stream.

(* The import comes after the [package] clause. *)
Set Extraction Scala Package "com.foo.bar".

Recursive Extraction twice.

Unset Extraction Scala Package.
Unset Extraction Scala Pure Functions.

(* Back to [=>], with no import. *)
Recursive Extraction twice.

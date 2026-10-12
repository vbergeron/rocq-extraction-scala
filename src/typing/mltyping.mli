(************************************************************************)
(*         *      The Rocq Prover / The Rocq Development Team           *)
(*  v      *         Copyright INRIA, CNRS and contributors             *)
(* <O___,, * (see version control and CREDITS file for authors & dates) *)
(*   \VV/  **************************************************************)
(*    //   *    This file is distributed under the terms of the         *)
(*         *     GNU Lesser General Public License Version 2.1          *)
(*         *     (see LICENSE file for the text of the license)         *)
(************************************************************************)

(** Type reconstruction for MiniML, independent of any target language.

    Extraction types the terms it produces, but keeps only the result
    of that typing as [MLmagic] markers: a backend for a statically
    typed language is left without the types of most of the nodes it
    prints. This pass rebuilds them, as [ml_type]s.

    The algorithm is Hindley-Milner with two extensions:
    - it is bidirectional: an expected type is pushed into lambdas and
      match branches, which gives a lambda's parameters their type
      from its context;
    - it never fails: wherever the term is ill-typed (extraction's own
      optimizations and type erasure can produce such terms), it
      records a {!TCoerce} node and carries on. [Tunknown] and [Taxiom]
      are compatible with every type, but their uses also give
      {!TCoerce} nodes, as a backend may need a conversion there.

    In the result, every meta variable is resolved. One left
    unconstrained becomes [Tunknown]. The type parameters of the
    definition being typed are the rigid [Tvar' i] (as in extraction's
    own convention, see [Mlutil.var2var']), and those of a
    let-generalized local definition are [Tvar' k] for [k] above them.
    [Tvar] does not appear. *)

open Names
open Extraction_plugin
open Miniml

(** {6 Typed terms} *)

type texpr = { node : tnode; ty : ml_type }

and tnode =
  | TRel of int * ml_type list
  (** A variable, and the instantiation of its type parameters when it
      is a let-generalized one (see {!TLetin}). *)
  | TGlob of global * ml_type list
  (** A global, and the instantiation of the [Tvar 1 ... Tvar n] of its
      type. *)
  | TApp of texpr * texpr list
  | TLam of (ml_ident * ml_type) list * texpr
  (** Parameters, outermost first. *)
  | TLetin of ml_ident * int list * texpr * texpr
  (** The [Tvar' k] generalized in the type of the bound term. *)
  | TCons of global * ml_type list * texpr list
  (** A constructor, the type arguments of its inductive, its fields. *)
  | TTuple of texpr list
  | TCase of global option * ml_type list * texpr * tbranch array
  (** The matched inductive (when known) and its type arguments, the
      scrutinee and the branches. The scrutinee has type
      [Tglob (ind, args)] whenever the inductive is known. *)
  | TFix of int * (Id.t * ml_type) array * texpr array
  | TCoerce of texpr
  (** The inner term, of type [inner.ty], used at type [ty]. *)
  | TExn of string
  | TDummy of kill_reason
  | TAxiom of string
  | TPrim of ml_ast
  (** A primitive literal or array, left untyped ([Tunknown]). *)

and tbranch = {
  ids : (ml_ident * ml_type) list;
  (** Same order as in [ml_branch]: the last one is [Prel 1]. *)
  pat : ml_pattern;
  body : texpr;
}

(** {6 Typing} *)

(** The types of the globals, constructors and type abbreviations of
    a structure. *)
type env

val env_of_structure : ml_structure -> env

(** [type_definition env ty body] checks the body of a top-level
    definition against its declared type [ty], whose [Tvar]s are the
    definition's own type parameters.

    [generalize] decides which bound terms of a [MLletin] get a
    polymorphic type (default: all of them). A backend can only allow
    the ones it can print as polymorphic definitions. *)
val type_definition :
  ?generalize:(ml_ast -> bool) -> env -> ml_type -> ml_ast -> texpr

(** {6 Utilities} *)

(** Back to MiniML, coercions dropped. *)
val erase : texpr -> ml_ast

(** Same as [Mlutil.eq_ml_type], up to the expansion of type
    abbreviations and singleton inductives. *)
val eq_type : env -> ml_type -> ml_type -> bool

(** For debugging. *)
val pr_type : ml_type -> Pp.t
val pr_texpr : texpr -> Pp.t

(************************************************************************)
(*         *      The Rocq Prover / The Rocq Development Team           *)
(*  v      *         Copyright INRIA, CNRS and contributors             *)
(* <O___,, * (see version control and CREDITS file for authors & dates) *)
(*   \VV/  **************************************************************)
(*    //   *    This file is distributed under the terms of the         *)
(*         *     GNU Lesser General Public License Version 2.1          *)
(*         *     (see LICENSE file for the text of the license)         *)
(************************************************************************)

(*s Type reconstruction for MiniML. See the interface for an overview. *)

open Pp
open Util
open Names
open Extraction_plugin
open Miniml
open Mlutil
open Table

type texpr = { node : tnode; ty : ml_type }

and tnode =
  | TRel of int * ml_type list
  | TGlob of global * ml_type list
  | TApp of texpr * texpr list
  | TLam of (ml_ident * ml_type) list * texpr
  | TLetin of ml_ident * int list * texpr * texpr
  | TCons of global * ml_type list * texpr list
  | TTuple of texpr list
  | TCase of global option * ml_type list * texpr * tbranch array
  | TFix of int * (Id.t * ml_type) array * texpr array
  | TCoerce of texpr
  | TExn of string
  | TDummy of kill_reason
  | TAxiom of string
  | TPrim of ml_ast

and tbranch = {
  ids : (ml_ident * ml_type) list;
  pat : ml_pattern;
  body : texpr;
}

let mk node ty = { node; ty }

(*S The global environment. *)

type cons_info = {
  ind : global;       (* its inductive *)
  nparams : int;      (* the number of type parameters of [ind] *)
  fields : ml_type list;
}

type env = {
  globs : ml_type Refmap'.t;
  (* The declared type of each [Dterm]/[Dfix]. *)
  conss : cons_info Refmap'.t;
  inds : int Refmap'.t;
  (* The number of type parameters of each inductive. *)
  abbrevs : (int * ml_type) Refmap'.t;
  (* The types to expand during unification: the [Dtype] abbreviations
     and the informative singleton inductives, both erased to their
     body. Their parameters are [Tvar 1 ... Tvar n]. *)
}

let add_packet kind env p =
  if p.ip_logical then env
  else
    let r = p.ip_typename_ref in
    let nparams = List.length p.ip_vars in
    let conss = ref env.conss in
    Array.iteri
      (fun j c -> conss := Refmap'.add c { ind = r; nparams; fields = p.ip_types.(j) } !conss)
      p.ip_consnames_ref;
    let abbrevs = match kind, p.ip_types with
      | Singleton, [| [t] |] when not (is_custom r) -> Refmap'.add r (nparams, t) env.abbrevs
      | _ -> env.abbrevs
    in
    { env with conss = !conss; inds = Refmap'.add r nparams env.inds; abbrevs }

let add_decl env = function
  | Dind ind -> Array.fold_left (add_packet ind.ind_kind) env ind.ind_packets
  | Dtype (r, ids, t) when not (is_custom r) && t != Taxiom ->
    { env with abbrevs = Refmap'.add r (List.length ids, t) env.abbrevs }
  | Dtype _ -> env
  | Dterm (r, _, t) -> { env with globs = Refmap'.add r t env.globs }
  | Dfix (rv, _, tv) ->
    { env with globs = Array.fold_left2 (fun g r t -> Refmap'.add r t g) env.globs rv tv }

let rec add_elem env (_, se) = match se with
  | SEdecl d -> add_decl env d
  | SEmodule { ml_mod_expr = MEstruct (_, sel) } -> List.fold_left add_elem env sel
  | SEmodule _ | SEmodtype _ -> env

let env_of_structure s =
  let empty =
    { globs = Refmap'.empty; conss = Refmap'.empty; inds = Refmap'.empty;
      abbrevs = Refmap'.empty }
  in
  List.fold_left (fun env (_, sel) -> List.fold_left add_elem env sel) empty s

let expand env = function
  | Tglob (r, args) ->
    (match Refmap'.find_opt r env.abbrevs with
     | Some (n, t) when Int.equal n (List.length args) -> Some (type_subst_list args t)
     | _ -> None)
  | _ -> None

(*S Meta variables and unification. *)

(* Meta variables carry a level, the number of [MLletin]s around the
   point where they were created: a meta still free in the type of a
   bound term can be generalized if its level is above the one of the
   [MLletin]. *)
let level = ref 0
let meta_count = ref 0
let meta_level : (int, int) Hashtbl.t = Hashtbl.create 97

let new_meta () =
  incr meta_count;
  Hashtbl.replace meta_level !meta_count !level;
  Tmeta { id = !meta_count; contents = None }

let rec repr = function
  | Tmeta { contents = Some t } -> repr t
  | t -> t

(* What to do to undo the unifications since the start of [attempt]. *)
let trail : (unit -> unit) list ref = ref []

let set_level id l =
  let old = Hashtbl.find meta_level id in
  if l < old then begin
    Hashtbl.replace meta_level id l;
    trail := (fun () -> Hashtbl.replace meta_level id old) :: !trail
  end

exception Mismatch

let bind m t =
  let l = Hashtbl.find meta_level m.id in
  let rec check t = match repr t with
    | Tmeta m' when Int.equal m.id m'.id -> raise Mismatch
    | Tmeta m' -> set_level m'.id l
    | Tarr (a, b) -> check a; check b
    | Tglob (_, args) -> List.iter check args
    | _ -> ()
  in
  check t;
  m.contents <- Some t;
  trail := (fun () -> m.contents <- None) :: !trail

let eq_global r1 r2 = GlobRef.UserOrd.equal r1.glob r2.glob

(* With [gradual], [Tunknown] and [Taxiom] are compatible with every
   type, without constraining it. *)
let rec unify env ~gradual t1 t2 =
  match repr t1, repr t2 with
  | Tmeta m1, Tmeta m2 when Int.equal m1.id m2.id -> ()
  | Tmeta m, t | t, Tmeta m -> bind m t
  | (Tunknown | Taxiom), _ | _, (Tunknown | Taxiom) when gradual -> ()
  | Tarr (a1, b1), Tarr (a2, b2) -> unify env ~gradual a1 a2; unify env ~gradual b1 b2
  | Tglob (r1, l1), Tglob (r2, l2)
    when eq_global r1 r2 && Int.equal (List.length l1) (List.length l2) ->
    List.iter2 (unify env ~gradual) l1 l2
  | t1, t2 ->
    match expand env t1, expand env t2 with
    | Some t1, _ -> unify env ~gradual t1 t2
    | None, Some t2 -> unify env ~gradual t1 t2
    | None, None ->
      match t1, t2 with
      | Tvar i, Tvar j | Tvar' i, Tvar' j when Int.equal i j -> ()
      | Tdummy _, Tdummy _ | Tunknown, Tunknown | Taxiom, Taxiom -> ()
      | _ -> raise Mismatch

(* Runs [f], undoing its unifications if it fails. *)
let attempt f =
  let saved = !trail in
  trail := [];
  let ok = try f (); true with Mismatch -> List.iter (fun undo -> undo ()) !trail; false in
  trail := saved;
  ok

let eq_type env t1 t2 = attempt (fun () -> unify env ~gradual:false t1 t2)

(* [e] used at type [expected]. *)
let coerce env e expected =
  if attempt (fun () -> unify env ~gradual:false e.ty expected) then e
  else begin
    ignore (attempt (fun () -> unify env ~gradual:true e.ty expected));
    mk (TCoerce e) expected
  end

let rec as_arrow env t = match repr t with
  | Tarr (a, b) -> Some (a, b)
  | Tmeta m ->
    let a = new_meta () and b = new_meta () in
    bind m (Tarr (a, b));
    Some (a, b)
  | t -> Option.bind (expand env t) (as_arrow env)

(*S Generalization. *)

(* The last [Tvar' k] in use: the definition's own type parameters,
   then the let-generalized ones. *)
let last_rigid = ref 0

(* Turns the metas of [t] created inside the current [MLletin]'s bound
   term, and still free, into fresh [Tvar' k]. *)
let generalize t =
  let gens = ref [] in
  let rec walk t = match repr t with
    | Tmeta m when Hashtbl.find meta_level m.id > !level ->
      incr last_rigid;
      m.contents <- Some (Tvar' !last_rigid);
      gens := !last_rigid :: !gens
    | Tarr (a, b) -> walk a; walk b
    | Tglob (_, l) -> List.iter walk l
    | _ -> ()
  in
  walk t;
  List.rev !gens

let rec subst_rigid s t = match repr t with
  | Tvar' k as t -> (try List.assoc k s with Not_found -> t)
  | Tarr (a, b) -> Tarr (subst_rigid s a, subst_rigid s b)
  | Tglob (r, l) -> Tglob (r, List.map (subst_rigid s) l)
  | t -> t

(*S Typing. *)

type ctx = {
  env : env;
  vars : (int list * ml_type) list;
  (* The type of each de Bruijn variable, and its let-generalized
     [Tvar' k]. *)
  generalizable : ml_ast -> bool;
}

let push ctx tys = { ctx with vars = List.map (fun t -> [], t) tys @ ctx.vars }

(* The inductive matched by a [MLcase], and its number of parameters. *)
let case_ind env typ pv =
  let from_pat = Array.find_map (fun (_, p, _) -> match p with
      | Pusual r | Pcons (r, _) -> Option.map (fun c -> c.ind) (Refmap'.find_opt r env.conss)
      | Prel _ | Pwild | Ptuple _ -> None) pv
  in
  let ind = match from_pat, typ with
    | Some r, _ -> Some r
    | None, Tglob (r, _) -> Some r
    | None, _ -> None
  in
  Option.bind ind (fun r -> Option.map (fun n -> (r, n)) (Refmap'.find_opt r env.inds))

(* The types of the [n] variables bound by pattern [pat] against a
   value of type [ty], as a de Bruijn list ([Prel 1] first). *)
let pattern_types env pat ty n =
  let tys = Array.init n (fun _ -> new_meta ()) in
  let fields r ty = match Refmap'.find_opt r env.conss with
    | None -> None
    | Some c ->
      let args = List.init c.nparams (fun _ -> new_meta ()) in
      ignore (attempt (fun () -> unify env ~gradual:true ty (Tglob (c.ind, args))));
      Some (List.map (type_subst_list args) c.fields)
  in
  let rec walk pat ty = match pat with
    | Prel i -> if 1 <= i && i <= n then tys.(i-1) <- ty
    | Pwild -> ()
    | Pusual r ->
      (match fields r ty with
       | Some fts when Int.equal (List.length fts) n ->
         List.iteri (fun j ft -> tys.(n-1-j) <- ft) fts
       | _ -> ())
    | Pcons (r, pats) ->
      (match fields r ty with
       | Some fts when Int.equal (List.length fts) (List.length pats) -> List.iter2 walk pats fts
       | _ -> List.iter (fun p -> walk p (new_meta ())) pats)
    | Ptuple pats -> List.iter (fun p -> walk p (new_meta ())) pats
  in
  walk pat ty;
  Array.to_list tys

let rec infer ctx = function
  | MLrel n ->
    let gens, t = List.nth ctx.vars (n-1) in
    let inst = List.map (fun _ -> new_meta ()) gens in
    mk (TRel (n, inst)) (subst_rigid (List.combine gens inst) t)
  | MLglob r ->
    (match Refmap'.find_opt r ctx.env.globs with
     | Some t ->
       let inst = List.init (type_maxvar t) (fun _ -> new_meta ()) in
       mk (TGlob (r, inst)) (type_subst_list inst t)
     | None -> mk (TGlob (r, [])) Tunknown)
  | MLapp (f, args) -> infer_app ctx (infer ctx f) args
  | MLlam _ as a ->
    let ids, body = collect_lams a in
    infer_lams ctx (List.rev ids) body
  | MLletin (id, a, b) ->
    let ta, gens, ctx' = infer_bound ctx a in
    let tb = infer ctx' b in
    mk (TLetin (id, gens, ta, tb)) tb.ty
  | MLcons (_, r, args) -> infer_cons ctx r args None
  | MLtuple l -> mk (TTuple (List.map (infer ctx) l)) Tunknown
  | MLcase (typ, a, pv) -> infer_case ctx typ a pv (new_meta ())
  | MLfix (i, ids, defs) ->
    let tys = Array.map (fun _ -> new_meta ()) ids in
    (* The last function is [MLrel 1]. *)
    let ctx' = push ctx (List.rev (Array.to_list tys)) in
    let tdefs = Array.map2 (check ctx') defs tys in
    mk (TFix (i, Array.map2 (fun id t -> (id, t)) ids tys, tdefs)) tys.(i)
  | MLexn s -> mk (TExn s) (new_meta ())
  | MLdummy k -> mk (TDummy k) (Tdummy k)
  | MLaxiom s -> mk (TAxiom s) (new_meta ())
  | MLmagic a -> infer ctx a
  | (MLuint _ | MLfloat _ | MLstring _ | MLparray _) as a -> mk (TPrim a) Tunknown

and check ctx a expected = match a with
  | MLmagic a -> check ctx a expected
  | MLlam _ ->
    let ids, body = collect_lams a in
    check_lams ctx (List.rev ids) body expected
  | MLletin (id, a, b) ->
    let ta, gens, ctx' = infer_bound ctx a in
    let tb = check ctx' b expected in
    mk (TLetin (id, gens, ta, tb)) tb.ty
  | MLcons (_, r, args) -> infer_cons ctx r args (Some expected)
  | MLcase (typ, a, pv) -> infer_case ctx typ a pv expected
  | MLexn s -> mk (TExn s) expected
  | MLaxiom s -> mk (TAxiom s) expected
  | a -> coerce ctx.env (infer ctx a) expected

and infer_app ctx tf args =
  let rec apply fty acc = function
    | a :: rest as args ->
      (match as_arrow ctx.env fty with
       | Some (dom, cod) -> apply cod (check ctx a dom :: acc) rest
       | None -> fty, List.rev acc, args)
    | [] -> fty, List.rev acc, []
  in
  let ty, targs, rest = apply tf.ty [] args in
  let head = if List.is_empty targs then tf else mk (TApp (tf, targs)) ty in
  match rest with
  | [] -> head
  | _ ->
    (* [head] is not a function: used as one. *)
    infer_app ctx (coerce ctx.env head (Tarr (new_meta (), new_meta ()))) rest

and infer_lams ctx ids body =
  let tys = List.map (fun _ -> new_meta ()) ids in
  let tb = infer (push ctx (List.rev tys)) body in
  mk (TLam (List.combine ids tys, tb)) (type_recomp (tys, tb.ty))

(* [ids] outermost first. Their types come from [expected], as long as
   it is a function type. *)
and check_lams ctx ids body expected =
  let finish params tb =
    if List.is_empty params then tb
    else mk (TLam (params, tb)) (type_recomp (List.map snd params, tb.ty))
  in
  let rec go ctx params ids expected = match ids with
    | [] -> finish (List.rev params) (check ctx body expected)
    | id :: rest ->
      match as_arrow ctx.env expected with
      | Some (a, b) -> go (push ctx [a]) ((id, a) :: params) rest b
      | None -> finish (List.rev params) (coerce ctx.env (infer_lams ctx ids body) expected)
  in
  go ctx [] ids expected

and infer_bound ctx a =
  incr level;
  let ta = infer ctx a in
  decr level;
  let gens = if ctx.generalizable a then generalize ta.ty else [] in
  ta, gens, { ctx with vars = (gens, ta.ty) :: ctx.vars }

and infer_cons ctx r args expected =
  match Refmap'.find_opt r ctx.env.conss with
  | Some c when Int.equal (List.length c.fields) (List.length args) ->
    let targs = List.init c.nparams (fun _ -> new_meta ()) in
    let ty = Tglob (c.ind, targs) in
    (* The expected type first, for the fields. *)
    Option.iter (fun t -> ignore (attempt (fun () -> unify ctx.env ~gradual:false ty t))) expected;
    let fields = List.map2 (fun a ft -> check ctx a (type_subst_list targs ft)) args c.fields in
    let e = mk (TCons (r, targs, fields)) ty in
    (match expected with Some t -> coerce ctx.env e t | None -> e)
  | _ ->
    let e = mk (TCons (r, [], List.map (infer ctx) args)) Tunknown in
    (match expected with Some t -> coerce ctx.env e t | None -> e)

(* All branches are checked against [res]. *)
and infer_case ctx typ a pv res =
  let ind = case_ind ctx.env typ pv in
  let targs, ta = match ind with
    | Some (r, n) ->
      let targs = List.init n (fun _ -> new_meta ()) in
      targs, check ctx a (Tglob (r, targs))
    | None -> [], infer ctx a
  in
  let branch (ids, pat, body) =
    let tys = pattern_types ctx.env pat ta.ty (List.length ids) in
    let tb = check (push ctx tys) body res in
    { ids = List.combine ids (List.rev tys); pat; body = tb }
  in
  mk (TCase (Option.map fst ind, targs, ta, Array.map branch pv)) res

(*S Resolution of the remaining metas. *)

let rec zonk_type t = match repr t with
  | Tmeta _ -> Tunknown
  | Tarr (a, b) -> Tarr (zonk_type a, zonk_type b)
  | Tglob (r, l) -> Tglob (r, List.map zonk_type l)
  | t -> t

let rec zonk e = { node = zonk_node e.node; ty = zonk_type e.ty }

and zonk_node = function
  | TRel (n, l) -> TRel (n, List.map zonk_type l)
  | TGlob (r, l) -> TGlob (r, List.map zonk_type l)
  | TApp (f, args) -> TApp (zonk f, List.map zonk args)
  | TLam (params, b) -> TLam (List.map (fun (id, t) -> (id, zonk_type t)) params, zonk b)
  | TLetin (id, gens, a, b) -> TLetin (id, gens, zonk a, zonk b)
  | TCons (r, l, args) -> TCons (r, List.map zonk_type l, List.map zonk args)
  | TTuple l -> TTuple (List.map zonk l)
  | TCase (ind, l, a, br) ->
    let zonk_branch b =
      { b with ids = List.map (fun (id, t) -> (id, zonk_type t)) b.ids; body = zonk b.body }
    in
    TCase (ind, List.map zonk_type l, zonk a, Array.map zonk_branch br)
  | TFix (i, fns, defs) ->
    TFix (i, Array.map (fun (id, t) -> (id, zonk_type t)) fns, Array.map zonk defs)
  | TCoerce a -> TCoerce (zonk a)
  | (TExn _ | TDummy _ | TAxiom _ | TPrim _) as n -> n

let type_definition ?(generalize = fun _ -> true) env ty body =
  Hashtbl.reset meta_level;
  meta_count := 0;
  level := 0;
  trail := [];
  last_rigid := type_maxvar ty;
  let ctx = { env; vars = []; generalizable = generalize } in
  zonk (check ctx body (var2var' ty))

(*S Utilities. *)

let rec erase e = match e.node with
  | TRel (n, _) -> MLrel n
  | TGlob (r, _) -> MLglob r
  | TApp (f, args) -> MLapp (erase f, List.map erase args)
  | TLam (params, b) -> List.fold_right (fun (id, _) b -> MLlam (id, b)) params (erase b)
  | TLetin (id, _, a, b) -> MLletin (id, erase a, erase b)
  | TCons (r, _, args) -> MLcons (e.ty, r, List.map erase args)
  | TTuple l -> MLtuple (List.map erase l)
  | TCase (_, _, a, br) ->
    MLcase (a.ty, erase a, Array.map (fun b -> (List.map fst b.ids, b.pat, erase b.body)) br)
  | TFix (i, fns, defs) -> MLfix (i, Array.map fst fns, Array.map erase defs)
  | TCoerce a -> MLmagic (erase a)
  | TExn s -> MLexn s
  | TDummy k -> MLdummy k
  | TAxiom s -> MLaxiom s
  | TPrim a -> a

let rec pr_type t = match repr t with
  | Tarr (a, b) -> str "(" ++ pr_type a ++ str " ->" ++ spc () ++ pr_type b ++ str ")"
  | Tglob (r, []) -> Printer.pr_global r.glob
  | Tglob (r, l) ->
    str "(" ++ Printer.pr_global r.glob ++ spc () ++ prlist_with_sep spc pr_type l ++ str ")"
  | Tvar i -> str "'a" ++ int i
  | Tvar' i -> str "'b" ++ int i
  | Tmeta m -> str "?" ++ int m.id
  | Tdummy _ -> str "__"
  | Tunknown -> str "Unknown"
  | Taxiom -> str "Axiom"

let pr_id id = Id.print (id_of_mlid id)

let pr_inst = function
  | [] -> mt ()
  | l -> str "[" ++ prlist_with_sep pr_comma pr_type l ++ str "]"

let rec pr_texpr e =
  let node = match e.node with
    | TRel (n, l) -> str "#" ++ int n ++ pr_inst l
    | TGlob (r, l) -> Printer.pr_global r.glob ++ pr_inst l
    | TApp (f, args) -> str "app" ++ spc () ++ prlist_with_sep spc pr_texpr (f :: args)
    | TLam (params, b) ->
      str "fun" ++ spc () ++
      prlist_with_sep spc (fun (id, t) -> str "(" ++ pr_id id ++ str " : " ++ pr_type t ++ str ")")
        params ++ spc () ++ pr_texpr b
    | TLetin (id, gens, a, b) ->
      str "let " ++ pr_id id ++
      prlist (fun k -> str " 'b" ++ int k) gens ++ spc () ++ pr_texpr a ++ spc () ++ pr_texpr b
    | TCons (r, l, args) ->
      Printer.pr_global r.glob ++ pr_inst l ++ spc () ++ prlist_with_sep spc pr_texpr args
    | TTuple l -> str "tuple" ++ spc () ++ prlist_with_sep spc pr_texpr l
    | TCase (_, _, a, br) ->
      let pr_branch b =
        str "(" ++ prlist_with_sep spc (fun (id, t) -> pr_id id ++ str " : " ++ pr_type t) b.ids ++
        str " =>" ++ spc () ++ pr_texpr b.body ++ str ")"
      in
      str "match" ++ spc () ++ pr_texpr a ++ spc () ++ prvect_with_sep spc pr_branch br
    | TFix (i, fns, defs) ->
      str "fix " ++ int i ++ spc () ++
      prvect_with_sep spc (fun ((id, t), d) ->
          str "(" ++ Id.print id ++ str " : " ++ pr_type t ++ str " :=" ++ spc () ++ pr_texpr d ++ str ")")
        (Array.map2 (fun f d -> (f, d)) fns defs)
    | TCoerce a -> str "coerce" ++ spc () ++ pr_texpr a
    | TExn s -> str "exn " ++ qstring s
    | TDummy _ -> str "__"
    | TAxiom s -> str "axiom " ++ qstring s
    | TPrim _ -> str "prim"
  in
  hov 1 (str "(" ++ node ++ str " :" ++ spc () ++ pr_type e.ty ++ str ")")

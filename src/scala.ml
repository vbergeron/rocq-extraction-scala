(************************************************************************)
(*         *      The Rocq Prover / The Rocq Development Team           *)
(*  v      *         Copyright INRIA, CNRS and contributors             *)
(* <O___,, * (see version control and CREDITS file for authors & dates) *)
(*   \VV/  **************************************************************)
(*    //   *    This file is distributed under the terms of the         *)
(*         *     GNU Lesser General Public License Version 2.1          *)
(*         *     (see LICENSE file for the text of the license)         *)
(************************************************************************)

(*s Production of Scala (Scala 3) syntax. *)

(* Each definition is first typed by [Mltyping], then printed from its
   typed form with every type Scala needs written out: see
   [pp_expr]. *)

open Pp
open CErrors
open Util
open Names
open Extraction_plugin
open Table
open Miniml
open Mlutil
open Common
open Mltyping

(*s Options. *)

(* The package clause printed at the top of every extracted file (empty -
   the default - means no [package] clause at all). *)
let { Goptions.get = scala_package } =
  Goptions.declare_interpreted_string_option_and_ref
    (fun s ->
       let valid_segment seg =
         not (String.is_empty seg) && Unicode.is_basic_ascii seg && Id.is_valid seg
       in
       if String.is_empty s || List.for_all valid_segment (String.split_on_char '.' s)
       then s
       else user_err Pp.(str "Extraction Scala Package must be a dotted sequence of \
                              valid ASCII identifiers, e.g. \"com.foo.bar\"."))
    Fun.id
    ~key:["Extraction"; "Scala"; "Package"]
    ~value:""
    ()

(* [Set Extraction Scala Pure Functions.]: extracted terms are pure, so
   print function types with Scala 3's pure arrow [A -> B] (and by-name
   parameters as [-> T]) instead of [A => B], and import
   [language.experimental.pureFunctions] in every extracted file so
   that syntax is accepted. Off by default, as it needs a Scala
   compiler supporting it. *)
let { Goptions.get = scala_pure_functions } =
  Goptions.declare_bool_option_and_ref
    ~key:["Extraction"; "Scala"; "Pure"; "Functions"]
    ~value:false
    ()

(* The arrow of a function type: [->] (pure) or [=>] (impure). *)
let pp_fun_arrow () = str (if scala_pure_functions () then "->" else "=>")

(* [import language.experimental.pureFunctions], when pure arrows are
   on. Right after the package clause, before the wrapping [object]
   (see [preamble]): Scala only allows this language import at the top
   level of a file. *)
let pp_pure_functions_import () =
  if scala_pure_functions () then
    str "import language.experimental.pureFunctions" ++ fnl2 ()
  else mt ()

(*s Scala renaming issues. *)

let keywords =
  List.fold_right (fun s -> Id.Set.add (Id.of_string s))
    [ (* Scala 3 hard keywords *)
      "abstract"; "case"; "catch"; "class"; "def"; "do"; "else"; "enum";
      "export"; "extends"; "false"; "final"; "finally"; "for"; "given";
      "if"; "implicit"; "import"; "lazy"; "match"; "new"; "null"; "object";
      "override"; "package"; "private"; "protected"; "return"; "sealed";
      "super"; "then"; "this"; "throw"; "trait"; "true"; "try"; "type";
      "val"; "var"; "while"; "with"; "yield";
      (* Scala 3 soft keywords / contextual identifiers, avoided for safety.
         Note: "macro" was a Scala 2-only soft keyword (`def foo = macro
         impl`), dropped along with the old macro system; it is a plain
         identifier in Scala 3 and deliberately not listed here. *)
      "derives"; "end"; "extension"; "infix"; "inline"; "opaque"; "open";
      "transparent"; "using";
      (* Members inherited from Any / AnyRef that are best not shadowed
         at top level *)
      "equals"; "hashCode"; "toString"; "getClass"; "wait"; "notify";
      "notifyAll"; "synchronized"; "clone"; "asInstanceOf"; "isInstanceOf";
      (* Our own helpers and the usual extraction dummy identifiers *)
      "unsafeCoerce"; "unit"; "Unit"; "Any"; "Nothing"; "Null";
      "_"; "__" ]
    Id.Set.empty

(* Note: do not shorten [str "foo" ++ fnl ()] into [str "foo\n"],
   the '\n' character interacts badly with the Format boxing mechanism *)

let pp_comment s = str "// " ++ hov 0 s ++ fnl ()
let pp_bracket_comment s = str "/* " ++ hov 0 s ++ str " */"

(* A Rocq identifier as a Scala one. Scala identifiers cannot contain
   an apostrophe, which is nonetheless very common in Rocq-chosen names
   ([n'], [IHn']), and must not end with an underscore, since [x_: T]
   reads as the identifier [x_:]. Trailing apostrophes and underscores
   are dropped ([n'] and [n_] become [n]), other apostrophes become
   underscores. Two names becoming the same are then told apart by a
   number, as any other clash: see [ctx_push_vars] for local names,
   [register_global_names] for global ones. An identifier made of
   underscores only ([_], [__]) is left as it is. *)
let scala_ident s =
  let s = String.map (fun c -> if c == '\'' then '_' else c) s in
  let n = String.length s in
  let rec trailing k = if k < n && s.[n-1-k] == '_' then trailing (k+1) else k in
  let k = trailing 0 in
  if Int.equal k 0 || Int.equal k n then s else String.sub s 0 (n-k)

let scala_id id = Id.of_string (scala_ident (Id.to_string id))

let pr_id id = str (scala_ident (Id.to_string id))

(* Every extracted file's contents are wrapped in a single [object]
   named after the module (see [preamble]): [mod_name] cleaned up the
   same way [pr_id] does, capitalized to follow Scala's own naming
   convention for an [object] - purely cosmetic, since Scala does not
   actually require it. *)
let pr_object_name mod_name = str (String.capitalize_ascii (scala_ident (Id.to_string mod_name)))

let pp_header_comment = function
  | None -> mt ()
  | Some com -> pp_bracket_comment com ++ fnl2 ()

(* [package foo.bar], from [Set Extraction Scala Package "foo.bar".]
   (empty - the default - prints nothing). Must come before the
   wrapping [object] (see [preamble]): a Scala [package] clause is a
   file-level statement, not something that can itself live inside an
   object. *)
let pp_package_header () =
  let pkg = scala_package () in
  if String.is_empty pkg then mt ()
  else str "package " ++ str pkg ++ fnl2 ()

(*s No modular extraction. *)

(* Two whole-structure analyses computed once by [pp_struct]
   ([register_structure]) and read from everywhere else while that same
   structure is being printed: [tenv], the types of its globals and
   constructors, for [Mltyping]; [needs_lazy], whether it has any
   coinductive type, which decides whether [RocqLazy] gets emitted (see
   [preamble]).

   Held in one [ref] rather than passed as an argument because
   [pp_decl] can be called on its own, once [pp_struct] has already
   populated it (isolated single-declaration printing in
   [extract_env.ml]) - [pp_decl]'s signature is shared by every backend,
   so it cannot carry this backend-specific extra state. Reset on every
   [pp_struct] call, so it never leaks across unrelated extraction
   commands. *)
type struct_info = {
  tenv : Mltyping.env;
  needs_lazy : bool;
}

let struct_info = ref { tenv = Mltyping.env_of_structure []; needs_lazy = false }

let rec has_coinductive (_,se) = match se with
  | SEdecl (Dind ind) -> ind.ind_kind == Coinductive
  | SEmodule { ml_mod_expr = MEstruct (_,sel) } -> List.exists has_coinductive sel
  | SEdecl _ | SEmodule _ | SEmodtype _ -> false

let register_structure (s : ml_structure) =
  { tenv = Mltyping.env_of_structure s;
    needs_lazy = List.exists (fun (_,sel) -> List.exists has_coinductive sel) s }

(* [Lazy.t]/[lazy]/[Lazy.force] reimplemented on Scala's native
   laziness: a by-name parameter [=> T] is a thunk, a [lazy val] caches
   it on first access. Not a one-liner [case class] because Scala
   forbids by-name parameters there. Covariant, so that a wrapped
   constructor [RocqLazy(Scons(...))] has the type of its coinductive. *)
let pp_lazy_class () =
  str "final class RocqLazy[+T](thunk: " ++ pp_fun_arrow () ++ str " T) {" ++ fnl () ++
  str "  lazy val force: T = thunk" ++ fnl () ++
  str "}" ++ fnl () ++
  str "object RocqLazy {" ++ fnl () ++
  str "  def apply[T](thunk: " ++ pp_fun_arrow () ++
  str " T): RocqLazy[T] = new RocqLazy(thunk)" ++ fnl () ++
  str "}"

(* Every extracted file is a package declaration (optional, see
   [pp_package_header]) followed by one [object mod_name { ... }]
   wrapping everything else - the dummy value, [RocqLazy] (both below)
   and the whole structure printed by [pp_struct], which closes the
   brace this opens. Doing this unconditionally, not just when a
   package is set, keeps two separately-extracted files from each
   defining their own top-level [RocqLazy]/[Nat]/... and colliding as
   soon as both are compiled together, package or not. *)
let preamble _table mod_name comment _used_modules usf =
  pp_header_comment comment ++
  pp_package_header () ++
  pp_pure_functions_import () ++
  str "object " ++ pr_object_name mod_name ++ str " {" ++ fnl2 () ++
  (if not usf.mldummy then mt ()
   else str "val __ : Unit = ()" ++ fnl2 ())
  ++
  (if not (!struct_info).needs_lazy then mt ()
   else pp_lazy_class () ++ fnl2 ())

(*s The pretty-printer for Scala syntax *)

(* Global names as Scala identifiers. Common makes the names of globals
   unique among themselves, before [unquote] turns them into Scala
   identifiers ([scala_ident]): two of them may then become the same
   ([plus] and [plus']). Once the names of a whole structure are known
   (by its printing phase, see [pp_struct]), [register_global_names]
   gives each name that [scala_ident] changes a Scala name clashing with
   no other: [plus'] becomes [plus0] when there is a [plus].
   [global_scala_ids] holds all the Scala names of globals, which local
   names must not shadow. *)
let global_names = ref String.Map.empty
let global_scala_ids = ref Id.Set.empty

let register_global_names table =
  let plain s = String.map (fun c -> if c == '\'' then '_' else c) s in
  let changed id = let s = Id.to_string id in not (String.equal (scala_ident s) (plain s)) in
  let all = Id.Set.elements (snd (empty_env table ())) in
  (* Names [scala_ident] keeps, apostrophes aside. *)
  let taken =
    List.fold_left (fun taken id ->
        if changed id then taken
        else if String.contains (Id.to_string id) '\'' then Id.Set.add (scala_id id) taken
        else Id.Set.add id taken)
      Id.Set.empty all in
  let names, taken =
    List.fold_left (fun (names, taken) id ->
        if not (changed id) then names, taken
        else
          let id' = rename_id (scala_id id) taken in
          String.Map.add (Id.to_string id) (Id.to_string id') names, Id.Set.add id' taken)
      (String.Map.empty, taken) all in
  global_names := names;
  global_scala_ids := taken

let unquote_global s =
  match String.Map.find_opt s !global_names with
  | Some s' -> s'
  | None -> scala_ident s

let pp_global table k r =
  if is_inline_custom r then str (find_custom r) else str (Common.pp_global table k r)

let pp_global_with_key table k key r =
  if is_inline_custom r then str (find_custom r)
  else str (Common.pp_global_with_key table k key r)

(*s Record field names, computed the same way OCaml's backend does
    ([get_record_fields]/[get_ind]/[kn_of_ind]): an explicitly-given
    field is named after its projection constant, an anonymous one
    falls back to a synthetic [Type__i]. *)

let get_ind r = let open GlobRef in match r.glob with
  | IndRef _ -> r
  | ConstructRef (ind,_) -> { glob = IndRef ind; inst = r.inst }
  | _ -> assert false

let kn_of_ind r = let open GlobRef in match r.glob with
  | IndRef (kn,_) -> MutInd.user kn
  | _ -> assert false

let pp_one_field table r i = function
  | Some r' -> pp_global_with_key table Term (kn_of_ind (get_ind r)) r'
  | None -> pp_global table Type (get_ind r) ++ str "__" ++ int i

let pp_fields table r fields = List.mapi (pp_one_field table r) fields

let pp_field table r fields i = pp_one_field table r i (List.nth fields i)

(* The type parameters of an inductive or a type definition. *)
let scala_tvars l = rename_tvars keywords (List.map scala_id l)

(*s Pretty-printing of types. [par] is a boolean indicating whether
    parentheses are needed or not. [var i] prints [Tvar i] and
    [Tvar' i]. *)

let pp_type_params = function
  | [] -> mt ()
  | l -> str "[" ++ prlist_with_sep (fun () -> str ", ") pr_id l ++ str "]"

let rec pp_type_with table par var t =
  let rec pp_rec par = function
    | Tvar i | Tvar' i -> var i
    | Tmeta { contents = Some t } -> pp_rec par t
    | Tmeta { contents = None } | Tunknown -> str "Any"
    | Tglob (r,[]) -> pp_global table Type r
    | Tglob (gr,l)
        when not (keep_singleton ()) && Rocqlib.check_ref sig_type_name gr.glob ->
          pp_type_with table false var (List.hd l)
    | Tglob (r,l) ->
        pp_global table Type r ++ str "[" ++
        prlist_with_sep (fun () -> str ", ") (pp_rec false) l ++ str "]"
    | Tarr (t1,t2) ->
        pp_par par (pp_rec true t1 ++ str " " ++ pp_fun_arrow () ++ str " " ++ pp_rec false t2)
    | Tdummy _ -> str "Unit"
    | Taxiom -> str "Any" ++ spc () ++ pp_bracket_comment (str "AXIOM TO BE REALIZED")
  in
  hov 0 (pp_rec par t)

(* With the type variables [vl] of a declaration. *)
let pp_type table par vl t =
  pp_type_with table par (fun i ->
      try pr_id (List.nth vl (pred i)) with Failure _ -> str "T" ++ int i) t

(* The fresh names of a declaration's [n] type parameters. *)
let type_params n =
  rename_tvars keywords (List.init n (fun i -> Id.of_string ("T" ^ string_of_int (i+1))))

(*s Pretty-printing of expressions, from their [Mltyping] typed form.

    Every binder is printed with its type, every polymorphic definition
    or constructor applied to its type arguments: the Scala type of
    each printed expression is then exactly its [Mltyping] type (or a
    subtype of it), and a cast is only needed where [Mltyping] found a
    coercion ([TCoerce]). Function application is always printed as a
    chain of curried calls [f(arg1)(arg2)...], matching the way every
    function value (whether top-level or local) is compiled as a chain
    of one-argument Scala functions. *)

(* The printing context: [table] (global naming state), [env] (de
   Bruijn naming) and [tvars] (the names of the type variables in
   scope - [Mltyping]'s [Tvar' i]). *)
type ctx = {
  table : State.t;
  env : Common.env;
  tvars : Id.t Int.Map.t;
}

(* Local names are turned into Scala identifiers ([scala_ident]) before
   being made unique, so that a name becoming the same as another in
   scope ([n'] next to [n]) gets a number instead ([n0]). *)
let ctx_push_vars ids ctx =
  let scala id =
    if id == dummy_name then id
    else Id.of_string (scala_ident (String.uncapitalize_ascii (Id.to_string id))) in
  let ids', env = push_vars (List.map scala ids) ctx.env in
  ids', { ctx with env }

let ctx_add_tvars l ctx =
  { ctx with tvars = List.fold_left (fun m (i, id) -> Int.Map.add i id m) ctx.tvars l }

let pp_ty ctx par t =
  pp_type_with ctx.table par (fun i ->
      match Int.Map.find_opt i ctx.tvars with
      | Some id -> pr_id id
      | None -> str "Any") t

let pp_type_args ctx = function
  | [] -> mt ()
  | l -> str "[" ++ prlist_with_sep (fun () -> str ", ") (pp_ty ctx false) l ++ str "]"

(* Whether a Scala value of type [t1] can be used at type [t2] without a
   cast: Scala functions are contravariant in their domain, and every
   value is an [Any]. *)
let rec subtype t1 t2 = match t1, t2 with
  | _, (Tunknown | Taxiom) -> true
  | Tarr (a1,b1), Tarr (a2,b2) -> subtype a2 a1 && subtype b1 b2
  | _ -> Mltyping.eq_type (!struct_info).tenv t1 t2

(* Let-bound terms printed as a polymorphic local [def]. *)
let rec is_lambda = function
  | MLlam _ -> true
  | MLmagic a -> is_lambda a
  | _ -> false

let type_definition typ body =
  Mltyping.type_definition ~generalize:is_lambda (!struct_info).tenv typ body

let tparam_names = List.map (fun k -> (k, Id.of_string ("t" ^ string_of_int k)))

(* Parameters [(x: T) => ...], with their names already pushed. *)
let pp_params ctx names params =
  prlist (fun (id, (_, t)) -> str "(" ++ pr_id id ++ str ": " ++ pp_ty ctx false t ++ str ") => ")
    (List.combine names params)

let push_params ctx params =
  let names, ctx = ctx_push_vars (List.rev_map (fun (id, _) -> id_of_mlid id) params) ctx in
  List.rev names, ctx

(* An expression that needs no parentheses as the head of an
   application or the receiver of a method. *)
let rec is_atomic e = match e.node with
  | TRel _ | TGlob _ | TApp _ | TCons _ | TTuple _ | TDummy _ -> true
  | TCoerce a -> subtype a.ty e.ty && is_atomic a
  | TLam _ | TLetin _ | TCase _ | TFix _ | TExn _ | TAxiom _ | TPrim _ -> false

(* [par]: whether [e] is printed where only an atomic expression can be
   (see [is_atomic]). *)
let rec pp_expr ctx par e =
  if par && not (is_atomic e) then str "(" ++ pp_expr ctx false e ++ str ")"
  else match e.node with
  | TRel (n, inst) ->
    let id = get_db_name n ctx.env in
    (* Try to survive to the occurrence of a Dummy rel.
       TODO: we should get rid of this hack (cf. BZ#592) *)
    let id = if Id.equal id dummy_name then Id.of_string "__" else id in
    pr_id id ++ pp_type_args ctx inst
  | TGlob (r, inst) ->
    pp_global ctx.table Term r ++
    (if is_inline_custom r then mt () else pp_type_args ctx inst)
  | TApp ({ node = TExn _ | TAxiom _ } as f, _) ->
    (* An exception may be applied, but I don't really care. *)
    pp_expr ctx par f
  | TApp ({ node = TFix (i, fns, defs) }, args) -> pp_fix ctx i fns defs args
  | TApp (f, args) ->
    pp_expr ctx true f ++
    prlist (fun a -> str "(" ++ pp_expr ctx false a ++ str ")") args
  | TLam (params, body) ->
    let names, ctx' = push_params ctx params in
    pp_params ctx' names params ++ pp_expr ctx' false body
  | TLetin (id, gens, a, b) ->
    let i, ctx' = ctx_push_vars [id_of_mlid id] ctx in
    let gens = tparam_names gens in
    let kw =
      if List.is_empty gens then str "val "
      else str "def " in
    let ctx_a = ctx_add_tvars gens ctx in
    hv 0 (str "{ " ++ kw ++
          hov 2 (pr_id (List.hd i) ++ pp_type_params (List.map snd gens) ++ str ": " ++
                 pp_ty ctx_a false a.ty ++ str " =" ++ spc () ++ pp_expr ctx_a false a) ++
          str ";" ++ spc () ++ pp_expr ctx' false b ++ str " }")
  | TCons (r, targs, args) -> pp_cons ctx e r targs args
  | TTuple l ->
    str "(" ++ prlist_with_sep (fun () -> str ", ") (pp_expr ctx false) l ++ str ")"
  | TCase (ind, _, a, br) -> pp_case ctx ind a br e.ty
  | TFix (i, fns, defs) -> pp_fix ctx i fns defs []
  | TCoerce a ->
    if subtype a.ty e.ty then pp_expr ctx par a
    else pp_expr ctx true a ++ str ".asInstanceOf[" ++ pp_ty ctx false e.ty ++ str "]"
  | TExn s -> str "throw new RuntimeException(" ++ qs s ++ str ")"
  | TDummy k ->
    (match msg_of_implicit k with
     | "" -> str "__"
     | s -> str "__" ++ spc () ++ pp_bracket_comment (str s))
  | TAxiom s -> str "throw new RuntimeException(\"AXIOM TO BE REALIZED (" ++ str s ++ str ")\")"
  | TPrim (MLuint _) -> str "throw new RuntimeException(\"EXTRACTION OF UINT NOT IMPLEMENTED\")"
  | TPrim (MLfloat _) -> str "throw new RuntimeException(\"EXTRACTION OF FLOAT NOT IMPLEMENTED\")"
  | TPrim (MLstring _) -> str "throw new RuntimeException(\"EXTRACTION OF STRING NOT IMPLEMENTED\")"
  | TPrim _ -> str "throw new RuntimeException(\"EXTRACTION OF ARRAY NOT IMPLEMENTED\")"

(* A custom constructor (e.g. via [Extract Inductive]) is printed as
   given, without type arguments. A coinductive value is delayed via
   [RocqLazy]; forced back where a [match] reads it (see [pp_case]). *)
and pp_cons ctx e r targs args =
  let c = Mltyping.erase e in
  if is_native_char c then pp_native_char c
  else if is_native_string c then pp_native_string c
  else if List.is_empty args && is_inline_custom r then pp_global ctx.table Cons r
  else
    let targs = if is_inline_custom r then mt () else pp_type_args ctx targs in
    let cons =
      pp_global ctx.table Cons r ++ targs ++ str "(" ++
      prlist_with_sep (fun () -> str ", ") (pp_expr ctx false) args ++ str ")"
    in
    if is_coinductive (State.get_table ctx.table) r then str "RocqLazy(" ++ cons ++ str ")"
    else cons

and pp_case ctx ind a br ty =
  let pv = Array.map (fun b -> (List.map fst b.ids, b.pat, MLexn "")) br in
  if is_custom_match pv then pp_custom_match ctx a br pv ty
  else match pp_record_proj ctx a br with
  | Some pp -> pp
  | None ->
    let coind = match ind with
      | Some r -> is_coinductive (State.get_table ctx.table) r
      | None -> false
    in
    let head = pp_expr ctx true a ++ (if coind then str ".force" else mt ()) in
    v 0 (head ++ str " match {" ++ fnl () ++
         prvect_with_sep fnl (pp_branch ctx) br ++ fnl () ++ str "}")

and pp_branch ctx b =
  let ids, ctx' = ctx_push_vars (List.rev_map (fun (id, _) -> id_of_mlid id) b.ids) ctx in
  let par = match b.body.node with TLam _ -> true | _ -> false in
  hov 2 (str "case " ++ pp_pat ctx' (List.rev ids) b.pat ++ str " =>" ++ spc () ++
         pp_expr ctx' par b.body)

(* A user-given match function, applied to one function per branch,
   then to the scrutinee. Its Scala type is unknown: each branch is
   given to it as an [Any => ... => Any] function, and its result is
   cast back to the type of the match.

   Scala has no bare juxtaposition application: every argument to the
   user-supplied match function - each branch, then the scrutinee -
   needs its own [(...)]. The match-function string itself stays
   unparenthesized: like any other `Extract` replacement, it is on the
   user to give it as an already-parenthesized curried lambda.

   [inner] additionally gets one unconditional pair of parens on top of
   the per-piece ones: Scala inserts a statement-terminating semicolon
   after a closing paren followed by a newline unless still inside an
   open bracket. Printed unwrapped, this chain would silently split
   into unrelated statements - no error, just the last one's value
   (verified against a real `scalac`). *)
and pp_custom_match ctx a br pv ty =
  if not (is_regular_match pv) then
    user_err Pp.(str "Cannot mix yet user-given match and general patterns.");
  let pp_branch b =
    let fn = match b.ids with
      | [] -> str "(_: Any) => " ++ pp_expr ctx false b.body
      | ids ->
        (* An unused parameter can take anything. *)
        let ids = List.map (fun (id, t) -> (id, if id == Dummy then Tunknown else t)) ids in
        let names, ctx' = push_params ctx ids in
        let fn = pp_params ctx' names ids ++ pp_expr ctx' false b.body in
        if List.for_all (fun (_, t) -> t == Tunknown) ids then fn
        else
          let arrow = " " ^ Pp.string_of_ppcmds (pp_fun_arrow ()) ^ " " in
          let any = String.concat arrow (List.map (fun _ -> "Any") ids @ ["Any"]) in
          str "(" ++ fn ++ str ").asInstanceOf[" ++ str any ++ str "]"
    in
    str "(" ++ fn ++ str ")" ++ fnl ()
  in
  let inner =
    str "(" ++
    hov 2 (str (find_custom_match pv) ++ fnl () ++
           prvect pp_branch br ++
           str "(" ++ pp_expr ctx false a ++ str ")") ++
    str ")"
  in
  if subtype Tunknown ty then inner
  else inner ++ str ".asInstanceOf[" ++ pp_ty ctx false ty ++ str "]"

(* Mirrors OCaml's [pp_record_proj]: a [match] that does nothing but
   read out a bound field (possibly applied to further arguments) is
   printed as a plain [t.field] instead - a readability nicety, never a
   correctness fix. [None] wherever the match does not have this
   shape. *)
and pp_record_proj ctx a br =
  let fields = record_fields_of_type (State.get_table ctx.table) a.ty in
  match br with
  | [| { ids; pat; body } |] when not (List.is_empty fields) ->
    let n = List.length ids in
    let no_patvar a = not (List.exists (fun a -> ast_occurs_itvl 1 n (Mltyping.erase a)) a) in
    let rel_args = match body.node with
      | TRel (i, []) when i <= n -> Some (i, [])
      | TApp ({ node = TRel (i, []) }, args) when i <= n && no_patvar args -> Some (i, args)
      | _ -> None
    in
    let rec lookup_rel i idx = function
      | Prel j :: l -> if Int.equal i j then Some idx else lookup_rel i (idx+1) l
      | Pwild :: l -> lookup_rel i (idx+1) l
      | _ -> None
    in
    let field = match rel_args, pat with
      | Some (i, args), Pusual r -> Some (r, n - i, args)
      | Some (i, args), Pcons (r, l) -> Option.map (fun idx -> (r, idx, args)) (lookup_rel i 0 l)
      | _ -> None
    in
    (match field with
     (* A custom-extracted constructor isn't printed as a case class. *)
     | Some (r, idx, args) when not (is_inline_custom r) ->
       let _, ctx' = ctx_push_vars (List.rev_map (fun (id, _) -> id_of_mlid id) ids) ctx in
       Some (pp_expr ctx true a ++ str "." ++ pp_field ctx.table r fields idx ++
             prlist (fun a -> str "(" ++ pp_expr ctx' false a ++ str ")") args)
     | _ -> None)
  | _ -> None

(* Unlike Haskell, a Scala constructor pattern is always printed as a
   call [Cons(args)], never bare juxtaposition, so it never needs
   parentheses of its own. *)
and pp_cons_pat ctx r ppl =
  match ppl with
  | [] when is_inline_custom r ->
    (* As for a constructor term: e.g. a literal like ["true"]. *)
    pp_global ctx.table Cons r
  | _ ->
    pp_global ctx.table Cons r ++ str "(" ++ prlist_with_sep (fun () -> str ", ") identity ppl ++ str ")"

and pp_pat ctx ids = function
  | Pcons (r,l) -> pp_cons_pat ctx r (List.map (pp_pat ctx ids) l)
  | Pusual r -> pp_cons_pat ctx r (List.map pr_id ids)
  | Ptuple l -> str "(" ++ prlist_with_sep (fun () -> str ", ") (pp_pat ctx ids) l ++ str ")"
  | Pwild -> str "_"
  | Prel n -> pr_id (get_db_name n ctx.env)

(* A local [fix]/[cofix]: a block of mutually recursive inner [def]s,
   each with its explicit type (Scala can't infer a recursive [def]'s
   type), then the one the [fix] stands for, applied to [args]. As at
   top level (see [pp_definition]), a [def] has one parameter list per
   parameter, so that a full application is a plain method call and a
   self-recursive call in tail position becomes a loop; with no [args],
   Scala eta-expands the bare [def] into a function value. *)
and pp_fix ctx i fns defs args =
  let ids, ctx' = ctx_push_vars (List.rev_map fst (Array.to_list fns)) ctx in
  let ids = Array.of_list (List.rev ids) in
  let pp_fn j def =
    let head = str "def " ++ pr_id ids.(j) in
    match def.node with
    | TLam (params, body) -> pp_method ctx' head params body
    | _ ->
      v 0 (head ++ str ": " ++ pp_ty ctx' false (snd fns.(j)) ++ str " =" ++ fnl () ++
           str "  " ++ pp_expr ctx' false def)
  in
  v 0 (v 2 (str "{" ++ fnl () ++
            prvect_with_sep fnl identity (Array.mapi pp_fn defs) ++ fnl () ++
            pr_id ids.(i) ++
            prlist (fun a -> str "(" ++ pp_expr ctx false a ++ str ")") args) ++
       fnl () ++ str "}")

(* [head(x: A)(y: B): R =] then [body] on its own line: a method with
   one parameter list per parameter. An anonymous parameter ([_]) gets
   a fresh name: unlike a lambda's, a method's parameter cannot be [_],
   and nothing refers to it (a dummy [MLrel] prints as [__], see
   [pp_expr]). *)
and pp_method ctx head params body =
  let names, ctx' = push_params ctx params in
  let pp_param i id (_, t) =
    let id = if Id.equal id dummy_name then Id.of_string ("__x" ^ string_of_int (i+1)) else id in
    str "(" ++ pr_id id ++ str ": " ++ pp_ty ctx' false t ++ str ")"
  in
  v 0 (hov 2 (head ++
              prlist identity (List.mapi (fun i (id, p) -> pp_param i id p) (List.combine names params)) ++
              str ": " ++ pp_ty ctx' false body.ty ++ str " =") ++
       fnl () ++ str "  " ++ pp_expr ctx' false body)

(*s Top-level definitions: [Dterm]s, and the members of a [Dfix]. *)

(* A definition with parameters is a method with one parameter list per
   parameter, [def name[vl](x: A)(y: B): rest =] then its body on its
   own line: a full application [name(a)(b)] is then a plain method call
   (no closure built per argument) and a self-recursive call in tail
   position becomes a loop. Scala 3 eta-expands a partially applied or
   bare reference [name(a)] into the function value every other position
   expects (see [pp_method]).

   Without parameters: [val], or [def] when [recursive] or polymorphic -
   [val] can't carry type parameters. *)
let pp_definition table ~recursive name typ body =
  let vl = type_params (type_maxvar typ) in
  let ctx =
    ctx_add_tvars (List.mapi (fun i id -> (i+1, id)) vl)
      { table; env = (let db, ids = empty_env table () in
                      db, Id.Set.union ids !global_scala_ids);
        tvars = Int.Map.empty }
  in
  let def = type_definition typ body in
  match def.node with
  | TLam (params, body) ->
    pp_method ctx (str "def " ++ name ++ pp_type_params vl) params body
  | _ ->
    let kw = if recursive || not (List.is_empty vl) then str "def " else str "val " in
    hov 2 (kw ++ name ++ pp_type_params vl ++ str ": " ++ pp_type table false vl typ ++
           str " =" ++ (if recursive then fnl () ++ str "  " else spc ()) ++
           pp_expr ctx false def)

(*s Pretty-printing of inductive types declaration. *)

let pp_logical_ind packet =
  pp_bracket_comment
    (Id.print packet.ip_typename ++ str " : logical inductive" ++ fnl () ++
     str "with constructors : " ++ prvect_with_sep spc Id.print packet.ip_consnames)
  ++ fnl ()

let pp_singleton table packet =
  let name = pp_global table Type packet.ip_typename_ref in
  let l = scala_tvars packet.ip_vars in
  hov 2 (str "type " ++ name ++ pp_type_params l ++ str " =" ++ spc () ++
         pp_type table false l (List.hd packet.ip_types.(0))) ++ fnl () ++
  pp_comment (str "singleton inductive, whose constructor was " ++
              Id.print packet.ip_consnames.(0))

(* [co]: whether the inductive is coinductive. When it is, the [sealed
   trait]/[case class] hierarchy is named [__Name] and [Name] itself
   becomes a [RocqLazy[__Name]] alias, so a coinductive value is always
   wrapped, never a bare case class (see [pp_expr_cons]'s [RocqLazy]
   wrap and [pp_expr_case]'s [.force]). *)
let pp_one_ind table co p pl cv =
  let pl = scala_tvars pl in
  let targs = pp_type_params pl in
  let typename = pp_global table Type p.ip_typename_ref in
  let rawname = if co then str "__" ++ typename else typename in
  let pp_constructor i typs =
    let cname = pp_global table Cons p.ip_consnames_ref.(i) in
    (* Fields get their real, precise Rocq type, directly from [typs]. *)
    let fields = List.mapi (fun j t -> str "_" ++ int (j+1) ++ str ": " ++ pp_type table false pl t) typs in
    hov 2 (str "final case class " ++ cname ++ targs ++ str "(" ++
           prlist_with_sep (fun () -> str ", ") identity fields ++
           str ") extends " ++ rawname ++ targs)
  in
  if Array.is_empty cv then
    hov 2 (str "type " ++ typename ++ targs ++ str " = Unit") ++
    spc () ++ pp_comment (str "empty inductive")
  else
    str "sealed trait " ++ rawname ++ targs ++ fnl () ++
    v 0 (prvecti (fun i typs -> pp_constructor i typs ++ fnl ()) cv) ++
    (if not co then mt ()
     else fnl () ++
          hov 2 (str "type " ++ typename ++ targs ++ str " = RocqLazy[" ++
                 rawname ++ targs ++ str "]"))

(* A record prints directly as a single, named-field case class rather
   than the generic [sealed trait]-plus-[case class] encoding, plus a
   thin type alias when the record's type name and constructor name
   differ (the usual case). *)
let pp_record table fields packet =
  let ind = packet.ip_typename_ref in
  let typename = pp_global table Type ind in
  let cname = pp_global table Cons packet.ip_consnames_ref.(0) in
  let pl = scala_tvars packet.ip_vars in
  let targs = pp_type_params pl in
  let fieldnames = pp_fields table ind fields in
  let l = List.combine fieldnames packet.ip_types.(0) in
  let decl =
    hov 2 (str "final case class " ++ cname ++ targs ++ str "(" ++
           prlist_with_sep (fun () -> str ", ")
             (fun (fname,t) -> fname ++ str ": " ++ pp_type table false pl t) l ++
           str ")")
  in
  if String.equal (Pp.string_of_ppcmds cname) (Pp.string_of_ppcmds typename) then decl
  else
    decl ++ fnl () ++
    hov 2 (str "type " ++ typename ++ targs ++ str " = " ++ cname ++ targs)

let rec pp_ind table co first i ind =
  if i >= Array.length ind.ind_packets then
    if first then mt () else fnl ()
  else
    let p = ind.ind_packets.(i) in
    let ip = p.ip_typename_ref in
    if is_custom ip then pp_ind table co first (i+1) ind
    else
      if p.ip_logical then
        pp_logical_ind p ++ pp_ind table co first (i+1) ind
      else
        pp_one_ind table co p p.ip_vars p.ip_types ++ fnl () ++
        pp_ind table co false (i+1) ind

(*s Pretty-printing of a declaration. *)

let pp_mind table i = match i.ind_kind with
  | Singleton -> pp_singleton table i.ind_packets.(0) ++ fnl ()
  | Record fields -> pp_record table fields i.ind_packets.(0) ++ fnl2 ()
  | Coinductive -> hov 0 (pp_ind table true true 0 i)
  | Standard -> hov 0 (pp_ind table false true 0 i)

(* An [Extract Constant]-replaced ([is_custom], not [is_inline_custom])
   [Dfix]/[Dterm]'s own declaration: ascribed with its real, precise
   Miniml type, even though the printed *body* is just the user's raw
   replacement text - its callers use it at that type.
   [str " "], not the breakable [spc ()]: a replacement string is
   opaque user text that Format could wrap onto a deeper-indented
   continuation line, and under Scala 3's significant indentation that
   opens an *implicit indented block* swallowing every following
   sibling declaration into this one's value (verified against a real
   `scalac`: it silently drops them). Keeping it on one physical line,
   however long, avoids the ambiguity. *)
let pp_custom_binding table name typ replacement =
  match typ with
  | Taxiom | Tunknown -> str "def " ++ name ++ str ": Any =" ++ str " " ++ str replacement
  | _ ->
  let vl = type_params (type_maxvar typ) in
  let kw = if List.is_empty vl then str "val " else str "def " in
  kw ++ name ++ pp_type_params vl ++ str ": " ++ pp_type table false vl typ ++
  str " =" ++ str " " ++ str replacement

let pp_decl table = function
  | Dind i -> pp_mind table i
  | Dtype (r, l, t) ->
      if is_inline_custom r then mt ()
      else
        let l = scala_tvars l in
        let st =
          try
            let ids,s = find_type_custom r in
            (match ids with
             | [] -> mt ()
             | _ -> str "[" ++ prlist_with_sep (fun () -> str ", ") str ids ++ str "]") ++
            str " =" ++ spc () ++ str s
          with Not_found ->
            pp_type_params l ++
            (if t == Taxiom then
               str " =" ++ spc () ++ str "Any" ++ spc () ++
               pp_bracket_comment (str "AXIOM TO BE REALIZED")
             else str " =" ++ spc () ++ pp_type table false l t)
        in
        hov 2 (str "type " ++ pp_global table Type r ++ st) ++ fnl2 ()
  | Dfix (rv, defs, typs) ->
      let names = Array.map
        (fun r -> if is_inline_custom r then mt () else pp_global table Term r) rv
      in
      prvecti
        (fun i r ->
          let void = is_inline_custom r ||
            (not (is_custom r) &&
             match defs.(i) with MLexn "UNUSED" -> true | _ -> false)
          in
          if void then mt ()
          else
            (if is_custom r then
               pp_custom_binding table names.(i) typs.(i) (find_custom r)
             else
               pp_definition table ~recursive:true names.(i) typs.(i) defs.(i))
            ++ fnl2 ())
        rv
  | Dterm (r, a, t) ->
      if is_inline_custom r then mt ()
      else
        let e = pp_global table Term r in
        if is_custom r then
          pp_custom_binding table e t (find_custom r) ++ fnl2 ()
        else
          hov 0 (pp_definition table ~recursive:false e t a ++ fnl2 ())

let rec pp_structure_elem table = function
  | (_,SEdecl d) -> pp_decl table d
  | (_,SEmodule m) -> pp_module_expr table m.ml_mod_expr
  | (_,SEmodtype _) -> mt ()
      (* for the moment we simply discard module types *)

and pp_module_expr table = function
  | MEstruct (_,sel) -> prlist_strict (fun e -> pp_structure_elem table e) sel
  | MEfunctor _ -> mt ()
      (* for the moment we simply discard unapplied functors *)
  | MEident _ | MEapply _ -> assert false
      (* should be expanded in extract_env *)

(* Closes the [object] opened by [preamble]. *)
let pp_struct table s =
  struct_info := register_structure s;
  register_global_names table;
  let pp_sel (mp,sel) = State.with_visibility table mp [] begin fun table ->
    prlist_strict (fun e -> pp_structure_elem table e) sel
  end in
  prlist_strict pp_sel s ++ str "}" ++ fnl ()

let file_naming state mp = file_of_modfile (State.get_table state) mp

(* Every file is wrapped in an [object] named after the file (see
   [preamble]), so the basename has to be a valid identifier. *)
let id_of_filename f =
  try Id.of_string f
  with UserError _ ->
    user_err Pp.(str "Extraction: provided filename is not a valid identifier")

let scala_descr = {
  keywords = keywords;
  file_suffix = ".scala";
  file_naming = file_naming;
  preamble = preamble;
  pp_struct = pp_struct;
  sig_suffix = None;
  sig_preamble = (fun _ _ _ _ _ -> mt ());
  pp_sig = (fun _ _ -> mt ());
  pp_decl = pp_decl;
  unquote = unquote_global;
  upper_types = true;
  char_type = Some "Char";
  string_type = Some "String";
  modular = false;
  id_of_filename = id_of_filename;
}

(subdir
 adt
 (rule
  (deps adt.v (package rocq-extraction-scala))
  (action
   (with-outputs-to adt.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction adt.v))))

 (rule
  (alias runtest)
  (action (diff adt.scala adt.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py adt.log)
  (action (run python3 ../check_scala.py adt.log))))

(subdir
 basic
 (rule
  (deps basic.v (package rocq-extraction-scala))
  (action
   (with-outputs-to basic.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction basic.v))))

 (rule
  (alias runtest)
  (action (diff basic.scala basic.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py basic.log)
  (action (run python3 ../check_scala.py basic.log))))

(subdir
 basic_ops
 (rule
  (deps basic_ops.v (package rocq-extraction-scala))
  (action
   (with-outputs-to basic_ops.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction basic_ops.v))))

 (rule
  (alias runtest)
  (action (diff basic_ops.scala basic_ops.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py basic_ops.log)
  (action (run python3 ../check_scala.py basic_ops.log))))

(subdir
 basic_types
 (rule
  (deps basic_types.v (package rocq-extraction-scala))
  (action
   (with-outputs-to basic_types.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction basic_types.v))))

 (rule
  (alias runtest)
  (action (diff basic_types.scala basic_types.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py basic_types.log)
  (action (run python3 ../check_scala.py basic_types.log))))

(subdir
 coinductive
 (rule
  (deps coinductive.v (package rocq-extraction-scala))
  (action
   (with-outputs-to coinductive.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction coinductive.v))))

 (rule
  (alias runtest)
  (action (diff coinductive.scala coinductive.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py coinductive.log)
  (action (run python3 ../check_scala.py coinductive.log))))

(subdir
 corelib
 (rule
  (deps corelib.v (package rocq-extraction-scala))
  (action
   (with-outputs-to corelib.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction corelib.v))))

 (rule
  (alias runtest)
  (action (diff corelib.scala corelib.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py corelib.log)
  (action (run python3 ../check_scala.py corelib.log))))

(subdir
 custom_nat
 (rule
  (deps custom_nat.v (package rocq-extraction-scala))
  (action
   (with-outputs-to custom_nat.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction custom_nat.v))))

 (rule
  (alias runtest)
  (action (diff custom_nat.scala custom_nat.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py custom_nat.log)
  (action (run python3 ../check_scala.py custom_nat.log))))

(subdir
 dummy_params
 (rule
  (deps dummy_params.v (package rocq-extraction-scala))
  (action
   (with-outputs-to dummy_params.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction dummy_params.v))))

 (rule
  (alias runtest)
  (action (diff dummy_params.scala dummy_params.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py dummy_params.log)
  (action (run python3 ../check_scala.py dummy_params.log))))

(subdir
 filename
 (rule
  (targets filename.log answer_file.scala)
  (deps filename.v (package rocq-extraction-scala))
  (action
   (with-outputs-to filename.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction filename.v))))

 (rule
  (alias runtest)
  (action (diff filename.out filename.log)))

 (rule
  (alias runtest)
  (action (diff filename.scala answer_file.scala)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py answer_file.scala)
  (action (run python3 ../check_scala.py answer_file.scala))))

(subdir
 generic_calls
 (rule
  (deps generic_calls.v (package rocq-extraction-scala))
  (action
   (with-outputs-to generic_calls.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction generic_calls.v))))

 (rule
  (alias runtest)
  (action (diff generic_calls.scala generic_calls.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py generic_calls.log)
  (action (run python3 ../check_scala.py generic_calls.log))))

(subdir
 header_comment
 (rule
  (deps header_comment.v (package rocq-extraction-scala))
  (action
   (with-outputs-to header_comment.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction header_comment.v))))

 (rule
  (alias runtest)
  (action (diff header_comment.scala header_comment.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py header_comment.log)
  (action (run python3 ../check_scala.py header_comment.log))))

(subdir
 identifiers
 (rule
  (deps identifiers.v (package rocq-extraction-scala))
  (action
   (with-outputs-to identifiers.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction identifiers.v))))

 (rule
  (alias runtest)
  (action (diff identifiers.scala identifiers.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py identifiers.log)
  (action (run python3 ../check_scala.py identifiers.log))))

(subdir
 inlined
 (rule
  (deps inlined.v (package rocq-extraction-scala))
  (action
   (with-outputs-to inlined.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction inlined.v))))

 (rule
  (alias runtest)
  (action (diff inlined.scala inlined.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py inlined.log)
  (action (run python3 ../check_scala.py inlined.log))))

(subdir
 keywords
 (rule
  (deps keywords.v (package rocq-extraction-scala))
  (action
   (with-outputs-to keywords.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction keywords.v))))

 (rule
  (alias runtest)
  (action (diff keywords.scala keywords.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py keywords.log)
  (action (run python3 ../check_scala.py keywords.log))))

(subdir
 local_fix
 (rule
  (deps local_fix.v (package rocq-extraction-scala))
  (action
   (with-outputs-to local_fix.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction local_fix.v))))

 (rule
  (alias runtest)
  (action (diff local_fix.scala local_fix.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py local_fix.log)
  (action (run python3 ../check_scala.py local_fix.log))))

(subdir
 local_mutual_fix
 (rule
  (deps local_mutual_fix.v (package rocq-extraction-scala))
  (action
   (with-outputs-to local_mutual_fix.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction local_mutual_fix.v))))

 (rule
  (alias runtest)
  (action (diff local_mutual_fix.scala local_mutual_fix.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py local_mutual_fix.log)
  (action (run python3 ../check_scala.py local_mutual_fix.log))))

(subdir
 logical_empty
 (rule
  (deps logical_empty.v (package rocq-extraction-scala))
  (action
   (with-outputs-to logical_empty.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction logical_empty.v))))

 (rule
  (alias runtest)
  (action (diff logical_empty.scala logical_empty.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py logical_empty.log)
  (action (run python3 ../check_scala.py logical_empty.log))))

(subdir
 modules
 (rule
  (deps modules.v (package rocq-extraction-scala))
  (action
   (with-outputs-to modules.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction modules.v))))

 (rule
  (alias runtest)
  (action (diff modules.scala modules.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py modules.log)
  (action (run python3 ../check_scala.py modules.log))))

(subdir
 mutual
 (rule
  (deps mutual.v (package rocq-extraction-scala))
  (action
   (with-outputs-to mutual.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction mutual.v))))

 (rule
  (alias runtest)
  (action (diff mutual.scala mutual.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py mutual.log)
  (action (run python3 ../check_scala.py mutual.log))))

(subdir
 mutual_ind
 (rule
  (deps mutual_ind.v (package rocq-extraction-scala))
  (action
   (with-outputs-to mutual_ind.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction mutual_ind.v))))

 (rule
  (alias runtest)
  (action (diff mutual_ind.scala mutual_ind.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py mutual_ind.log)
  (action (run python3 ../check_scala.py mutual_ind.log))))

(subdir
 nat_bigint
 (rule
  (deps nat_bigint.v (package rocq-extraction-scala))
  (action
   (with-outputs-to nat_bigint.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction nat_bigint.v))))

 (rule
  (alias runtest)
  (action (diff nat_bigint.scala nat_bigint.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py nat_bigint.log)
  (action (run python3 ../check_scala.py nat_bigint.log))))

(subdir
 package
 (rule
  (deps package.v (package rocq-extraction-scala))
  (action
   (with-outputs-to package.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction package.v))))

 (rule
  (alias runtest)
  (action (diff package.out package.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py package.log)
  (action (run python3 ../check_scala.py package.log))))

(subdir
 patterns
 (rule
  (deps patterns.v (package rocq-extraction-scala))
  (action
   (with-outputs-to patterns.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction patterns.v))))

 (rule
  (alias runtest)
  (action (diff patterns.scala patterns.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py patterns.log)
  (action (run python3 ../check_scala.py patterns.log))))

(subdir
 poly
 (rule
  (deps poly.v (package rocq-extraction-scala))
  (action
   (with-outputs-to poly.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction poly.v))))

 (rule
  (alias runtest)
  (action (diff poly.scala poly.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py poly.log)
  (action (run python3 ../check_scala.py poly.log))))

(subdir
 pure_custom_match
 (rule
  (deps pure_custom_match.v (package rocq-extraction-scala))
  (action
   (with-outputs-to pure_custom_match.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction pure_custom_match.v))))

 (rule
  (alias runtest)
  (action (diff pure_custom_match.scala pure_custom_match.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py pure_custom_match.log)
  (action (run python3 ../check_scala.py pure_custom_match.log))))

(subdir
 pure_functions
 (rule
  (deps pure_functions.v (package rocq-extraction-scala))
  (action
   (with-outputs-to pure_functions.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction pure_functions.v))))

 (rule
  (alias runtest)
  (action (diff pure_functions.out pure_functions.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py pure_functions.log)
  (action (run python3 ../check_scala.py pure_functions.log))))

(subdir
 records
 (rule
  (deps records.v (package rocq-extraction-scala))
  (action
   (with-outputs-to records.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction records.v))))

 (rule
  (alias runtest)
  (action (diff records.scala records.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py records.log)
  (action (run python3 ../check_scala.py records.log))))

(subdir
 singleton_keep
 (rule
  (deps singleton_keep.v (package rocq-extraction-scala))
  (action
   (with-outputs-to singleton_keep.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction singleton_keep.v))))

 (rule
  (alias runtest)
  (action (diff singleton_keep.scala singleton_keep.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py singleton_keep.log)
  (action (run python3 ../check_scala.py singleton_keep.log))))

(subdir
 strings
 (rule
  (deps strings.v (package rocq-extraction-scala))
  (action
   (with-outputs-to strings.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction strings.v))))

 (rule
  (alias runtest)
  (action (diff strings.scala strings.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py strings.log)
  (action (run python3 ../check_scala.py strings.log))))

(subdir
 type_defs
 (rule
  (deps type_defs.v (package rocq-extraction-scala))
  (action
   (with-outputs-to type_defs.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction type_defs.v))))

 (rule
  (alias runtest)
  (action (diff type_defs.scala type_defs.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py type_defs.log)
  (action (run python3 ../check_scala.py type_defs.log))))

(subdir
 typing
 (rule
  (deps typing.v (package rocq-extraction-scala))
  (action
   (with-outputs-to typing.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction typing.v))))

 (rule
  (alias runtest)
  (action (diff typing.scala typing.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py typing.log)
  (action (run python3 ../check_scala.py typing.log))))

(subdir
 z_bigint
 (rule
  (deps z_bigint.v (package rocq-extraction-scala))
  (action
   (with-outputs-to z_bigint.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction z_bigint.v))))

 (rule
  (alias runtest)
  (action (diff z_bigint.scala z_bigint.log)))

 (rule
  (alias scalac)
  (enabled_if %{bin-available:scalac})
  (deps ../check_scala.py z_bigint.log)
  (action (run python3 ../check_scala.py z_bigint.log))))


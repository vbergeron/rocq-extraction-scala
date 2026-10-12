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


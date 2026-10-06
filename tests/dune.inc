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


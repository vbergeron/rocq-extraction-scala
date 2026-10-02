(rule
 (deps adt.v (package rocq-extraction-scala))
 (action
  (with-outputs-to adt.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction adt.v)))))

(rule
 (alias runtest)
 (action (diff adt.out adt.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py adt.log)
 (action (run python3 check_scala.py adt.log)))

(rule
 (deps basic.v (package rocq-extraction-scala))
 (action
  (with-outputs-to basic.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction basic.v)))))

(rule
 (alias runtest)
 (action (diff basic.out basic.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py basic.log)
 (action (run python3 check_scala.py basic.log)))

(rule
 (deps coinductive.v (package rocq-extraction-scala))
 (action
  (with-outputs-to coinductive.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction coinductive.v)))))

(rule
 (alias runtest)
 (action (diff coinductive.out coinductive.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py coinductive.log)
 (action (run python3 check_scala.py coinductive.log)))

(rule
 (deps custom_nat.v (package rocq-extraction-scala))
 (action
  (with-outputs-to custom_nat.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction custom_nat.v)))))

(rule
 (alias runtest)
 (action (diff custom_nat.out custom_nat.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py custom_nat.log)
 (action (run python3 check_scala.py custom_nat.log)))

(rule
 (deps filename.v (package rocq-extraction-scala))
 (action
  (with-outputs-to filename.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction filename.v)
    (run cat answer_file.scala)))))

(rule
 (alias runtest)
 (action (diff filename.out filename.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py filename.log)
 (action (run python3 check_scala.py filename.log)))

(rule
 (deps mutual.v (package rocq-extraction-scala))
 (action
  (with-outputs-to mutual.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction mutual.v)))))

(rule
 (alias runtest)
 (action (diff mutual.out mutual.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py mutual.log)
 (action (run python3 check_scala.py mutual.log)))

(rule
 (deps package.v (package rocq-extraction-scala))
 (action
  (with-outputs-to package.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction package.v)))))

(rule
 (alias runtest)
 (action (diff package.out package.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py package.log)
 (action (run python3 check_scala.py package.log)))

(rule
 (deps poly.v (package rocq-extraction-scala))
 (action
  (with-outputs-to poly.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction poly.v)))))

(rule
 (alias runtest)
 (action (diff poly.out poly.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py poly.log)
 (action (run python3 check_scala.py poly.log)))

(rule
 (deps records.v (package rocq-extraction-scala))
 (action
  (with-outputs-to records.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction records.v)))))

(rule
 (alias runtest)
 (action (diff records.out records.log)))

(rule
 (alias scalac)
 (enabled_if %{bin-available:scalac})
 (deps check_scala.py records.log)
 (action (run python3 check_scala.py records.log)))


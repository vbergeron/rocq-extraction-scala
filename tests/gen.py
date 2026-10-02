# Regenerates dune.inc: one rule per test, diffing the output of
# `rocq c` against the .out file. Run with `dune build @gen --auto-promote`.
import glob, os

tests = sorted(os.path.splitext(f)[0] for f in glob.glob("*.v"))
for t in tests:
    # filename.v writes answer_file.scala, which is appended to its output
    extra = "\n    (run cat answer_file.scala)" if t == "filename" else ""
    print(f"""(rule
 (deps {t}.v (package rocq-extraction-scala))
 (action
  (with-outputs-to {t}.log
   (progn
    (run rocq c -q -test-mode -Q ../theories ScalaExtraction {t}.v){extra}))))

(rule
 (alias runtest)
 (action (diff {t}.out {t}.log)))
""")

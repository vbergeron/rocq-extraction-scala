# Regenerates dune.inc. Each test lives in its own directory <t>/, with
# <t>.v and its expected output:
#
# - <t>.scala alone: the output of `rocq c` on <t>.v is a single Scala
#   file, and is compared with it.
# - <t>.out alone: the output of `rocq c` is something else (error
#   messages, several extractions), and is compared with it.
# - <t>.out and <t>.scala: <t>.v also writes a file with
#   [Extraction "name" ...]; the output of `rocq c` is compared with
#   <t>.out and name.scala with <t>.scala.
#
# The Scala code is also compiled with scalac (alias scalac, only when
# scalac is installed). Run with `dune build @gen --auto-promote`.
import glob, os, re

tests = sorted(os.path.dirname(f) for f in glob.glob("*/*.v")
               if os.path.basename(f) == os.path.dirname(f) + ".v")
for t in tests:
    has_out = os.path.exists(f"{t}/{t}.out")
    src = open(f"{t}/{t}.v").read()
    written = re.search(r'^Extraction "(\w+)"', src, re.M)
    if has_out and written:
        file = written.group(1) + ".scala"
        targets = f"\n  (targets {t}.log {file})"
        diffs = [(f"{t}.out", f"{t}.log"), (f"{t}.scala", file)]
        scala = file
    else:
        targets = ""
        diffs = [(f"{t}.out" if has_out else f"{t}.scala", f"{t}.log")]
        scala = f"{t}.log"
    print(f"""(subdir
 {t}
 (rule{targets}
  (deps {t}.v (package rocq-extraction-scala))
  (action
   (with-outputs-to {t}.log
    (run rocq c -q -test-mode -Q ../../theories ScalaExtraction {t}.v))))""")
    for expected, actual in diffs:
        print(f"""
 (rule
  (alias runtest)
  (action (diff {expected} {actual})))""")
    print(f"""
 (rule
  (alias scalac)
  (enabled_if %{{bin-available:scalac}})
  (deps ../check_scala.py {scala})
  (action (run python3 ../check_scala.py {scala}))))
""")

# Compiles the Scala code in a test output with scalac. Each extracted
# file (an optional [package] clause, then an [object] up to its closing
# brace) is compiled on its own, as an output may contain several of them.
import os, re, subprocess, sys, tempfile

# String and char literals, whose braces are not counted
LITERALS = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])\'')

log = sys.argv[1]
chunks, cur, depth = [], None, 0
for line in open(log):
    if cur is None and (line.startswith("package ") or line.startswith("object ")):
        cur, depth = [], 0
    if cur is not None:
        cur.append(line)
        code = LITERALS.sub("", line)
        depth += code.count("{") - code.count("}")
        if depth == 0 and "}" in code:
            chunks.append("".join(cur))
            cur = None

if not chunks:
    sys.exit(f"{log}: no Scala code found")

failed = False
for i, chunk in enumerate(chunks):
    with tempfile.TemporaryDirectory() as d:
        src = os.path.join(d, f"chunk{i}.scala")
        with open(src, "w") as f:
            f.write(chunk)
        r = subprocess.run(["scalac", "-d", d, src],
                           stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        if r.returncode != 0:
            failed = True
            print(f"{log}: Scala code #{i + 1} does not compile:\n{chunk}\n{r.stdout}")
sys.exit(1 if failed else 0)

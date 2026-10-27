import os
import re

src_dir = r"F:\AppMaking\XimiLib\sources\src"
for root, dirs, files in os.walk(src_dir):
    for f in files:
        if f.endswith(".java"):
            p = os.path.join(root, f)
            with open(p, "r", encoding="utf-8", errors="ignore") as fp:
                txt = fp.read()
                m = re.findall(r'"(BK[0-9]+|S[0-9]+|MS[0-9]+)"', txt)
                if m:
                    print(f, m)

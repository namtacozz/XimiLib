import os
import sqlite3
import unicodedata
import re

conn = sqlite3.connect(r'F:\AppMaking\ViThay\libman.db')
c = conn.cursor()

c.execute("SELECT id, title, cover_image FROM books")
rows = c.fetchall()
print(f"Total rows: {len(rows)}")

src_img_dir = r"F:\AppMaking\ViThay\src\main\resources\com\vithay\libman\images"
missing = []
formats = set()

for bid, title, cov in rows:
    if not cov:
        missing.append((bid, title, "NULL or empty cover_image"))
        continue
    # Extract filename
    fname = os.path.basename(cov)
    full_path = os.path.join(src_img_dir, fname)
    if not os.path.exists(full_path):
        missing.append((bid, title, f"File not found: {fname}"))
    formats.add(os.path.splitext(fname)[1].lower())

print(f"Missing count: {len(missing)}")
print(f"File extensions: {formats}")
if missing:
    for m in missing[:10]:
        print(" ", m)

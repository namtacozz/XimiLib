import sys
import sqlite3
import unicodedata
import re

sys.stdout.reconfigure(encoding='utf-8')

conn = sqlite3.connect(r'F:\AppMaking\ViThay\libman.db')
c = conn.cursor()

def norm(s):
    if not s:
        return ''
    s = unicodedata.normalize('NFC', s.strip())
    return re.sub(r'\s+', ' ', s)

c.execute("SELECT DISTINCT category_id, category FROM books ORDER BY category_id")
cats = c.fetchall()
print("Categories in ViThay:")
for cid, name in cats:
    print(f"  {cid}: {name}")

c.execute("SELECT DISTINCT publisher FROM books WHERE publisher IS NOT NULL AND publisher != '' ORDER BY publisher")
pubs = c.fetchall()
print(f"\nPublishers ({len(pubs)}):")
for p in pubs:
    print(f"  {p[0]}")

c.execute("SELECT DISTINCT author FROM books WHERE author IS NOT NULL AND author != '' ORDER BY author")
authors = c.fetchall()
print(f"\nAuthors ({len(authors)}):")
for a in authors:
    print(f"  {a[0]}")

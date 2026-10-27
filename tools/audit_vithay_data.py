import sqlite3
import unicodedata
import re

conn = sqlite3.connect(r'F:\AppMaking\ViThay\libman.db')
c = conn.cursor()

def norm(s):
    if not s:
        return ''
    s = unicodedata.normalize('NFC', s.strip())
    return re.sub(r'\s+', ' ', s)

c.execute("SELECT id, title, author, category, category_id, shelf_location, isbn, price, publish_year, publisher, status, cover_image, is_deleted FROM books ORDER BY id")
rows = c.fetchall()

titles = {}
isbns = {}
missing_author = []
missing_cat = []
missing_pub = []
missing_isbn = []
duplicates = []

for r in rows:
    bid, title, author, category, category_id, shelf, isbn, price, pub_year, publisher, status, cover, is_del = r
    t_norm = norm(title)
    a_norm = norm(author)
    
    if not t_norm:
        missing_cat.append((bid, "Missing title"))
    if not a_norm:
        missing_author.append((bid, t_norm))
    if not publisher or not norm(publisher):
        missing_pub.append((bid, t_norm))
    if not isbn or not norm(isbn):
        missing_isbn.append((bid, t_norm))

    pair = (t_norm.lower(), a_norm.lower())
    if pair in titles:
        duplicates.append((bid, titles[pair], pair))
    else:
        titles[pair] = bid

    if isbn:
        isbn_clean = norm(isbn).replace("-", "")
        if isbn_clean in isbns:
            print(f"Duplicate ISBN: {isbn} for books {bid} and {isbns[isbn_clean]}")
        else:
            isbns[isbn_clean] = bid

print(f"Total books: {len(rows)}")
print(f"Missing authors: {len(missing_author)}")
print(f"Missing publishers: {len(missing_pub)}")
print(f"Missing ISBNs: {len(missing_isbn)}")
print(f"Duplicate (title + author): {len(duplicates)}")
print(f"Soft deleted books in ViThay: {sum(1 for r in rows if r[12] == 1)}")

import os
import sys
import sqlite3
import csv
import unicodedata
import re
import shutil

sys.stdout.reconfigure(encoding='utf-8')

src_db = r'F:\AppMaking\ViThay\libman.db'
src_img_dir = r'F:\AppMaking\ViThay\src\main\resources\com\vithay\libman\images'
dest_img_dir = r'F:\AppMaking\XimiLib\sources\src\main\resources\assets\covers'
dry_run_dir = r'F:\AppMaking\XimiLib\import-report\dry-run'

os.makedirs(dest_img_dir, exist_ok=True)
os.makedirs(dry_run_dir, exist_ok=True)

conn = sqlite3.connect(src_db)
c = conn.cursor()

def norm(s):
    if not s:
        return ''
    s = unicodedata.normalize('NFC', str(s).strip())
    return re.sub(r'\s+', ' ', s)

# 1. Categories
c.execute("SELECT DISTINCT category_id, category FROM books ORDER BY category_id")
cat_rows = c.fetchall()
categories_data = []
for cid, cname in cat_rows:
    categories_data.append({
        'Matheloai': int(cid),
        'Tentheloai': norm(cname),
        'Mota': f'Thể loại {norm(cname)}'
    })

cat_csv = os.path.join(dry_run_dir, 'categories_to_insert.csv')
with open(cat_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['Matheloai', 'Tentheloai', 'Mota'])
    writer.writeheader()
    writer.writerows(categories_data)
print(f"Generated {cat_csv} ({len(categories_data)} rows)")

# 2. Authors
c.execute("SELECT DISTINCT author FROM books WHERE author IS NOT NULL AND author != '' ORDER BY author")
author_rows = c.fetchall()
authors_data = []
author_map = {} # name -> Matacgia
for idx, (aname,) in enumerate(author_rows, start=1):
    matacgia = f"TG{idx:03d}"
    aname_norm = norm(aname)
    author_map[aname_norm] = matacgia
    authors_data.append({
        'Matacgia': matacgia,
        'Tentacgia': aname_norm,
        'Mota': ''
    })

author_csv = os.path.join(dry_run_dir, 'authors_to_insert.csv')
with open(author_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['Matacgia', 'Tentacgia', 'Mota'])
    writer.writeheader()
    writer.writerows(authors_data)
print(f"Generated {author_csv} ({len(authors_data)} rows)")

# 3. Publishers
c.execute("SELECT DISTINCT publisher FROM books WHERE publisher IS NOT NULL AND publisher != '' ORDER BY publisher")
pub_rows = c.fetchall()
publishers_data = []
pub_map = {} # name -> manxb
for idx, (pname,) in enumerate(pub_rows, start=1):
    manxb = f"NXB{idx:02d}"
    pname_norm = norm(pname)
    pub_map[pname_norm] = manxb
    publishers_data.append({
        'manxb': manxb,
        'tennxb': pname_norm,
        'diachinxb': '',
        'sdt': '',
        'mota': ''
    })

pub_csv = os.path.join(dry_run_dir, 'publishers_to_insert.csv')
with open(pub_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['manxb', 'tennxb', 'diachinxb', 'sdt', 'mota'])
    writer.writeheader()
    writer.writerows(publishers_data)
print(f"Generated {pub_csv} ({len(publishers_data)} rows)")

# 4. Books
c.execute("SELECT id, title, author, category, category_id, shelf_location, isbn, price, publish_year, publisher, status, cover_image, is_deleted FROM books ORDER BY id")
books_raw = c.fetchall()

books_data = []
rejected_data = []
needs_review_data = []
missing_cover_data = []
copied_covers = 0

for idx, row in enumerate(books_raw, start=1):
    src_id, title, author, category, category_id, shelf, isbn, price, pub_year, publisher, status, cover, is_del = row
    
    t_norm = norm(title)
    a_norm = norm(author)
    p_norm = norm(publisher)
    c_norm = norm(category)
    isbn_norm = norm(isbn)
    
    # Check rejection criteria
    if not t_norm:
        rejected_data.append({'id_nguon': src_id, 'ly_do': 'Tên sách rỗng'})
        continue
    
    # Check cover
    img_filename = ''
    rel_img_path = ''
    if cover:
        img_filename = os.path.basename(cover)
        src_img_path = os.path.join(src_img_dir, img_filename)
        dest_img_path = os.path.join(dest_img_dir, img_filename)
        if os.path.exists(src_img_path):
            shutil.copy2(src_img_path, dest_img_path)
            copied_covers += 1
            rel_img_path = f"/assets/covers/{img_filename}"
        else:
            missing_cover_data.append({'id_nguon': src_id, 'tensach': t_norm, 'file_anh': img_filename})
    else:
        missing_cover_data.append({'id_nguon': src_id, 'tensach': t_norm, 'file_anh': 'NULL'})

    # Target ID
    masach = f"MS{idx:04d}"
    matheloai = int(category_id)
    manxb = pub_map.get(p_norm, '')
    matacgia = author_map.get(a_norm, '')

    books_data.append({
        'id_nguon': src_id,
        'Masach': masach,
        'Tensach': t_norm,
        'Matheloai': matheloai,
        'Tentheloai': c_norm,
        'Manxb': manxb,
        'Tennxb': p_norm,
        'Isbn': isbn_norm,
        'Namxb': pub_year if pub_year else '',
        'Giabia': f"{float(price):.2f}" if price else '0.00',
        'Hinhanh': rel_img_path,
        'Matacgia': matacgia,
        'Tentacgia': a_norm,
        'IsDeleted': 0
    })

books_csv = os.path.join(dry_run_dir, 'books_to_insert.csv')
with open(books_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=[
        'id_nguon', 'Masach', 'Tensach', 'Matheloai', 'Tentheloai',
        'Manxb', 'Tennxb', 'Isbn', 'Namxb', 'Giabia',
        'Hinhanh', 'Matacgia', 'Tentacgia', 'IsDeleted'
    ])
    writer.writeheader()
    writer.writerows(books_data)
print(f"Generated {books_csv} ({len(books_data)} rows)")

# 5. Rejected
rej_csv = os.path.join(dry_run_dir, 'rejected.csv')
with open(rej_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['id_nguon', 'ly_do'])
    writer.writeheader()
    writer.writerows(rejected_data)
print(f"Generated {rej_csv} ({len(rejected_data)} rows)")

# 6. Needs review
nr_csv = os.path.join(dry_run_dir, 'needs_review.csv')
with open(nr_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['id_nguon', 'tensach', 'ly_do'])
    writer.writeheader()
    writer.writerows(needs_review_data)
print(f"Generated {nr_csv} ({len(needs_review_data)} rows)")

# 7. Missing cover
mc_csv = os.path.join(dry_run_dir, 'missing_cover.csv')
with open(mc_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['id_nguon', 'tensach', 'file_anh'])
    writer.writeheader()
    writer.writerows(missing_cover_data)
print(f"Generated {mc_csv} ({len(missing_cover_data)} rows)")

print(f"\n--- Summary ---")
print(f"Source count: {len(books_raw)}")
print(f"Books to insert: {len(books_data)}")
print(f"Rejected: {len(rejected_data)}")
print(f"Needs review: {len(needs_review_data)}")
print(f"Missing cover: {len(missing_cover_data)}")
print(f"Covers copied: {copied_covers}")
print(f"Reconcile equation: {len(books_raw)} = {len(books_data)} + 0 + {len(rejected_data)} + {len(needs_review_data)}")

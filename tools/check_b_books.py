import sqlite3
import sys
sys.stdout.reconfigure(encoding='utf-8')

conn = sqlite3.connect(r'F:\AppMaking\ViThay\libman.db')
c = conn.cursor()
c.execute("SELECT id, title, author, category, category_id, isbn FROM books WHERE id LIKE 'B0%'")
for r in c.fetchall():
    print(r)

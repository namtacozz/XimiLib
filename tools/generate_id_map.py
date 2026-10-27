import os
import csv
import sys
sys.stdout.reconfigure(encoding='utf-8')

dry_books_csv = r'F:\AppMaking\XimiLib\import-report\dry-run\books_to_insert.csv'
id_map_csv = r'F:\AppMaking\XimiLib\import-report\id-map.csv'

with open(dry_books_csv, 'r', encoding='utf-8-sig') as f:
    reader = csv.DictReader(f)
    rows = list(reader)

with open(id_map_csv, 'w', encoding='utf-8-sig', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['id_nguon', 'Masach', 'Tensach', 'Matacgia', 'Tentacgia', 'Matheloai', 'Manxb'])
    writer.writeheader()
    for r in rows:
        writer.writerow({
            'id_nguon': r['id_nguon'],
            'Masach': r['Masach'],
            'Tensach': r['Tensach'],
            'Matacgia': r['Matacgia'],
            'Tentacgia': r['Tentacgia'],
            'Matheloai': r['Matheloai'],
            'Manxb': r['Manxb']
        })

print(f"Generated {id_map_csv} with {len(rows)} mappings.")

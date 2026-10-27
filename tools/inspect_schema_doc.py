import sys
sys.stdout.reconfigure(encoding='utf-8')
with open(r'F:\AppMaking\XimiLib\docs\DATABASE_SCHEMA.md', 'r', encoding='utf-8') as f:
    for i, line in enumerate(f):
        if line.startswith('#'):
            print(f'{i+1}: {line.strip()}')

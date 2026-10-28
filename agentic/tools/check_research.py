"""Lists research pages whose recipes are missing from data/neothaum/recipe, and research icons that name no registered item.

Usage: python check_research.py [items.txt]   (items.txt: registered NeoThaum item ids, one per line, e.g. from the icons scene's log)
"""
import glob
import json
import os
import sys

ROOT = os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'main', 'resources', 'data', 'neothaum')
d = json.load(open(os.path.join(ROOT, 'neothaum', 'research.json'), encoding='utf-8'))
base = os.path.join(ROOT, 'recipe')
recipes = {os.path.splitext(os.path.relpath(f, base))[0].replace(os.sep, '/') for f in glob.glob(os.path.join(base, '**', '*.json'), recursive=True)}
items = set(open(sys.argv[1]).read().split()) if len(sys.argv) > 1 else None

missing = 0
for r in d['research']:
    for p in r.get('pages', []):
        rs = p.get('recipes')
        for rid in rs if isinstance(rs, list) else []:
            ns, path = rid.split(':')
            if ns == 'neothaum' and path not in recipes:
                print('recipe', r['category'], r['key'], path)
                missing += 1
    ic = r.get('icon') or {}
    it = ic.get('item')
    if items is not None and it and it.startswith('neothaum:') and it.split(':')[1] not in items:
        print('icon', r['category'], r['key'], it)
        missing += 1
print(missing, 'problems')

"""Removes textures in textures/item that are byte-identical copies of textures/items, repointing models to the items/ copy."""
import filecmp
import glob
import os

os.chdir(os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'main', 'resources', 'assets', 'neothaum'))
dups = []
for f in glob.glob('textures/item/*.png'):
    name = os.path.basename(f)[:-4]
    other = 'textures/items/' + name + '.png'
    if os.path.exists(other) and filecmp.cmp(f, other, shallow=False):
        dups.append(name)
changed = 0
for m in glob.glob('models/**/*.json', recursive=True):
    s = open(m, encoding='utf-8').read()
    t = s
    for d in dups:
        t = t.replace('"neothaum:item/%s"' % d, '"neothaum:items/%s"' % d)
    if t != s:
        open(m, 'w', encoding='utf-8').write(t)
        changed += 1
for d in dups:
    os.remove('textures/item/%s.png' % d)
print(len(dups), 'duplicates removed,', changed, 'models changed')

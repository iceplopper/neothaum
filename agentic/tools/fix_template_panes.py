"""Bake glass-pane and iron-bar connections into a structure template: templates keep block states as saved, so a pane saved unconnected stays a post."""
import sys
import nbt_min as n

SOLIDISH_SKIP = ('air', 'ladder', 'torch', 'door', 'trapdoor', 'carpet', 'button', 'lever', 'sign', 'flower', 'rail', 'bed', 'banner')


def connects(name):
    short = name.split(':')[-1]
    return not any(k in short for k in SOLIDISH_SKIP)


def fix(path):
    name, r = n.load(path)
    pal = r['palette'][1][1]
    blocks = r['blocks'][1][1]
    names = [p['id'][1] for p in pal]
    at = {tuple(b['pos'][1][1]): names[b['state'][1]] for b in blocks}
    keyed = {}
    changed = 0
    for b in blocks:
        if not (names[b['state'][1]].endswith('glass_pane') or names[b['state'][1]].endswith('iron_bars')):
            continue
        x, y, z = b['pos'][1][1]
        props = {'waterlogged': 'false'}
        for d, (dx, dz) in {'north': (0, -1), 'south': (0, 1), 'west': (-1, 0), 'east': (1, 0)}.items():
            props[d] = 'true' if connects(at.get((x + dx, y, z + dz), 'minecraft:air')) else 'false'
        key = (names[b['state'][1]], tuple(sorted(props.items())))
        if key not in keyed:
            pal.append({'id': (n.STRING, key[0]), 'properties': (n.COMPOUND, {k: (n.STRING, v) for k, v in props.items()})})
            names.append(key[0])
            keyed[key] = len(pal) - 1
        b['state'] = (n.INT, keyed[key])
        changed += 1
    n.save(path, name, r)
    print(path, 'panes fixed:', changed)


if __name__ == '__main__':
    for p in sys.argv[1:]:
        fix(p)

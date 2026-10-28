"""Turns the straight-line block placements of a TC4 world generator into a NeoThaum structure file.

Usage: python port_structure.py <WorldGenX.java> <out.txt> [method ...]

Reads setBlock(i + dx, j + dy, k + dz, Block[, meta, flags]) and setBlockToAir calls, in order, from the named methods (all of the class if
none are named) and writes one "dx dy dz blockstate" line per placement. Placements that depend on a random roll or a variable are left out
for the Java side to do; they are listed on stderr.
"""
import csv
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
FIELDS = os.path.join(HERE, '..', '..', 'original', 'tools', 'mcp', 'fields.csv')
srg = {r[0]: r[1] for r in csv.reader(open(FIELDS, encoding='utf-8')) if r and r[0].startswith('field_')}

STAIR = {0: 'east', 1: 'west', 2: 'south', 3: 'north'}
LADDER = {2: 'north', 3: 'south', 4: 'west', 5: 'east'}


def state(block, meta):
    """The 1.7 block and metadata as a modern block state string."""
    if block.startswith('ConfigBlocks.'):
        raise KeyError(block)
    name = srg[block.split('.', 1)[1]]
    if name == 'stone_stairs':
        return 'minecraft:cobblestone_stairs[facing=%s,half=%s]' % (STAIR[meta & 3], 'top' if meta & 4 else 'bottom')
    if name == 'ladder':
        return 'minecraft:ladder[facing=%s]' % LADDER[meta]
    if name == 'tallgrass':
        return {0: 'minecraft:dead_bush', 1: 'minecraft:short_grass', 2: 'minecraft:fern'}[meta]
    if name == 'stonebrick':
        return ['minecraft:stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:chiseled_stone_bricks'][meta]
    simple = {'air': 'air', 'dirt': 'dirt', 'cobblestone': 'cobblestone', 'grass': 'grass_block', 'mossy_cobblestone': 'mossy_cobblestone',
              'mob_spawner': 'spawner', 'iron_bars': 'iron_bars', 'chest': 'chest', 'trapped_chest': 'trapped_chest', 'tnt': 'tnt', 'stone': 'stone',
              'snow_layer': 'snow', 'gravel': 'gravel', 'sand': 'sand', 'torch': 'torch', 'web': 'cobweb', 'obsidian': 'obsidian'}
    if name in simple and meta == 0:
        return 'minecraft:' + simple[name]
    raise KeyError('%s:%d' % (name, meta))


def offset(expr, var):
    expr = expr.strip()
    m = re.fullmatch(var + r'(?:\s*([+-])\s*(\d+))?', expr)
    if not m:
        raise ValueError(expr)
    return 0 if not m.group(1) else int(m.group(2)) * (1 if m.group(1) == '+' else -1)


src = open(sys.argv[1], encoding='utf-8').read()
methods = sys.argv[3:]
if methods:
    body = ''
    for meth in methods:
        start = re.search(r'(public|private|protected) \w+ ' + meth + r'\(', src).start()
        body += src[start:src.index('\n   }\n', start)]
else:
    body = src
out, skipped = [], []
for line in body.split('\n'):
    s = line.strip()
    m = re.match(r'world\.func_147449_b\(([^,]+), ([^,]+), ([^,]+), ([\w.]+)\);$', s)  # setBlock(x, y, z, block)
    meta = 0
    if not m:
        m = re.match(r'world\.func_147465_d\(([^,]+), ([^,]+), ([^,]+), ([\w.]+), (\d+), \d+\);$', s)  # setBlock(x, y, z, block, meta, flags)
        if m:
            meta = int(m.group(5))
    if not m:
        m2 = re.match(r'world\.func_147468_f\(([^,]+), ([^,]+), ([^,]+)\);$', s)  # setBlockToAir
        if m2:
            m, block = m2, 'Blocks.field_150350_a'
    else:
        block = m.group(4)
    if not m:
        if 'func_147465_d' in s or 'func_147449_b' in s or 'func_147468_f' in s:
            skipped.append(s)
        continue
    try:
        out.append('%d %d %d %s' % (offset(m.group(1), 'i'), offset(m.group(2), 'j'), offset(m.group(3), 'k'), state(block, meta)))
    except (KeyError, ValueError) as e:
        skipped.append('%s  (%s)' % (s, e))
os.makedirs(os.path.dirname(os.path.abspath(sys.argv[2])), exist_ok=True)
open(sys.argv[2], 'w', encoding='utf-8').write('\n'.join(out) + '\n')
print('%d placements written, %d left for the Java side' % (len(out), len(skipped)))
for s in skipped:
    print('  ' + s, file=sys.stderr)

"""Prints a gzipped NBT file (e.g. a structure template) as JSON-ish text. Usage: python nbt_dump.py <file.nbt> [jigsaw]

With "jigsaw", prints only the template's size, palette entries that are jigsaws, and the blocks using them."""
import gzip
import json
import struct
import sys


def read(f, t):
    if t == 1:
        return struct.unpack('>b', f.read(1))[0]
    if t == 2:
        return struct.unpack('>h', f.read(2))[0]
    if t == 3:
        return struct.unpack('>i', f.read(4))[0]
    if t == 4:
        return struct.unpack('>q', f.read(8))[0]
    if t == 5:
        return struct.unpack('>f', f.read(4))[0]
    if t == 6:
        return struct.unpack('>d', f.read(8))[0]
    if t == 7:
        n = read(f, 3)
        return list(f.read(n))
    if t == 8:
        n = struct.unpack('>H', f.read(2))[0]
        return f.read(n).decode('utf-8')
    if t == 9:
        et = read(f, 1)
        n = read(f, 3)
        return [read(f, et) for _ in range(n)]
    if t == 10:
        d = {}
        while True:
            ct = read(f, 1)
            if ct == 0:
                return d
            name = read(f, 8)
            d[name] = read(f, ct)
    if t == 11:
        n = read(f, 3)
        return [read(f, 3) for _ in range(n)]
    if t == 12:
        n = read(f, 3)
        return [read(f, 4) for _ in range(n)]
    raise ValueError(t)


with gzip.open(sys.argv[1], 'rb') as f:
    assert read(f, 1) == 10
    read(f, 8)
    root = read(f, 10)
if len(sys.argv) > 2 and sys.argv[2] == 'jigsaw':
    print('size', root.get('size'))
    jig = [i for i, p in enumerate(root['palette']) if p.get('id', p.get('Name')) == 'minecraft:jigsaw']
    for i in jig:
        print('palette', i, root['palette'][i])
    for b in root['blocks']:
        if b['state'] in jig:
            print('block', b)
else:
    print(json.dumps(root, indent=1)[:20000])

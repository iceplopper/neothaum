"""Generates spawn egg textures in the modern style: a shaded egg in the mob's TC4 egg colours (ItemSpawnerEgg.addMapping) with a small
pixel-art emblem of the mob on the front. Run from anywhere; writes textures/item/<name>_spawn_egg.png."""
import os
from PIL import Image

OUT = os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'main', 'resources', 'assets', 'neothaum', 'textures', 'item')

SHAPE = [
    '................',
    '......####......',
    '.....######.....',
    '....########....',
    '...##########...',
    '...##########...',
    '..############..',
    '..############..',
    '..############..',
    '..############..',
    '..############..',
    '..############..',
    '...##########...',
    '....########....',
    '.....######.....',
    '................',
]

# name: (primary, secondary, emblem palette, 8x8 emblem drawn at x 4..11, y 5..12; '.' lets the shell show through)
EGGS = {
    'mind_spider': (0xAAAAAA, 0x404040, {'k': 0x3A3A3A, 'e': 0xE040E0}, [
        'k.k..k.k',
        '.kkkkkk.',
        'kkekkekk',
        '.kkkkkk.',
        'kkkkkkkk',
        '.kkkkkk.',
        'k.kkkk.k',
        '...kk...']),
    'eldritch_guardian': (0x444450, 0x404040, {'h': 0x2A2830, 'v': 0x050505}, [
        '..hhhh..',
        '.hhhhhh.',
        'hhvvvvhh',
        'hvvvvvvh',
        'hvvvvvvh',
        'hhvvvvhh',
        'hhhvvhhh',
        '.hhhhhh.']),
    'eldritch_warden': (0x552222, 0x404040, {'h': 0x34302E, 'v': 0x050505, 'r': 0xD03030, 'g': 0x9A8A50}, [
        'g.g..g.g',
        '.hhhhhh.',
        'hhvvvvhh',
        'hvrvvrvh',
        'hvvvvvvh',
        'hhvvvvhh',
        'hhhvvhhh',
        '.hhhhhh.']),
    'cultist_knight': (0xFF5555, 0x000080, {'s': 0x8A8A96, 'd': 0x5A5A66, 'v': 0x101010, 'n': 0x1C1C80}, [
        '..ssss..',
        '.ssssss.',
        '.svvvvs.',
        '.dssssd.',
        '.svsvss.',
        '..dssd..',
        '...nn...',
        '..nnnn..']),
    'cultist_cleric': (0xFF5555, 0x800000, {'m': 0x8A1010, 'k': 0x1A0A0A, 'e': 0xE8C090}, [
        '..mmmm..',
        '.mmmmmm.',
        'mmkkkkmm',
        'mkkkkkkm',
        'mkekkekm',
        'mmkkkkmm',
        '.mmmmmm.',
        '..m..m..']),
    'cultist_leader': (0xFF5555, 0x505050, {'s': 0x5A5A60, 'v': 0x101010, 'g': 0xE0B030}, [
        'g.g..g.g',
        'gggggggg',
        '.ssssss.',
        '.svvvvs.',
        '.ssssss.',
        '.ssvvss.',
        '..ssss..',
        '...gg...']),
    'cultist_portal': (0xFF5555, 0xFF55FF, {'p': 0xFF55FF, 'k': 0x300818, 'w': 0xFFE0FF}, [
        '..pppp..',
        '.pkkkkp.',
        'pkkppkkp',
        'pkpwwpkp',
        'pkpwwpkp',
        'pkkppkkp',
        '.pkkkkp.',
        '..pppp..']),
    'eldritch_golem': (0x555555, 0x404040, {'s': 0x7A7A7A, 'g': 0xC09030, 'c': 0x20A090, 'w': 0xC0FFF0}, [
        '.ssssss.',
        'ssssssss',
        'ssggggss',
        'sgccccgs',
        'sgcwccgs',
        'ssggggss',
        'ssssssss',
        '.s.ss.s.']),
    'eldritch_crab': (0x555555, 0x550000, {'k': 0x2A2A2E, 'r': 0xB02020, 'c': 0x30C0B0}, [
        'kk....kk',
        'kkk..kkk',
        '.kk..kk.',
        '..kkkk..',
        '.krckrk.',
        'kkkkkkkk',
        'k.kkkk.k',
        'k......k']),
    'inhabited_zombie': (0x557755, 0x550000, {'k': 0x2A2A2E, 'r': 0xB02020, 'z': 0x6A9A5A, 'e': 0x101010, 'd': 0x3A5A32}, [
        'kkkkkkkk',
        'krkkkkrk',
        'zzzzzzzz',
        'zeezzeez',
        'zzzzzzzz',
        'zzddddzz',
        'zzzzzzzz',
        '.zzzzzz.']),
}


def rgb(c):
    return (c >> 16 & 255, c >> 8 & 255, c & 255)


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def inside(x, y):
    return 0 <= x < 16 and 0 <= y < 16 and SHAPE[y][x] == '#'


def make(name, c1, c2, pal, emblem):
    p, s = rgb(c1), rgb(c2)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if not inside(x, y):
                continue
            if not all(inside(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                c = shade(mix(p, s, 0.4), 0.45)
            else:
                # lit from the top left, with a bright spot and the secondary colour in speckles
                c = shade(p, 1.2 - (x + y) / 32.0)
                if (x, y) in ((5, 3), (4, 4), (6, 2)):
                    c = mix(c, (255, 255, 255), 0.35)
                elif (x * 5 + y * 3) % 7 == 0:
                    c = mix(c, s, 0.6)
            img.putpixel((x, y), c + (255,))
    for ey, row in enumerate(emblem):
        for ex, ch in enumerate(row):
            if ch != '.':
                img.putpixel((4 + ex, 5 + ey), rgb(pal[ch]) + (255,))
    img.save(os.path.join(OUT, name + '_spawn_egg.png'))


for n, args in EGGS.items():
    make(n, *args)
print('made', len(EGGS))

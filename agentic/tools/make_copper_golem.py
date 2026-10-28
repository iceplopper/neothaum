"""Makes the copper golem's skins in TC4's golem layout: golem_iron.png gradient-mapped onto the colours of vanilla's copper block at each
stage of oxidation (taken from the Minecraft jar). Usage: python make_copper_golem.py <minecraft jar>"""
import io
import os
import sys
import zipfile
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
GOLEM = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'assets', 'neothaum', 'textures', 'entity', 'golem')
STAGES = {'': 'copper_block', '_exposed': 'exposed_copper', '_weathered': 'weathered_copper', '_oxidized': 'oxidized_copper'}


def lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


jar = zipfile.ZipFile(sys.argv[1])
iron = Image.open(os.path.join(GOLEM, 'golem_iron.png')).convert('RGBA')
ipx = [p for p in iron.getdata() if p[3] > 0]
ilo, ihi = min(lum(p) for p in ipx), max(lum(p) for p in ipx)
for suffix, block in STAGES.items():
    tex = Image.open(io.BytesIO(jar.read('assets/minecraft/textures/block/%s.png' % block))).convert('RGB')
    palette = sorted(set(tex.getdata()), key=lum)
    out = Image.new('RGBA', iron.size)
    for i, p in enumerate(iron.getdata()):
        if p[3] == 0:
            out.putpixel((i % iron.size[0], i // iron.size[0]), (0, 0, 0, 0))
            continue
        t = (lum(p) - ilo) / max(1.0, ihi - ilo)
        c = palette[min(len(palette) - 1, int(t * (len(palette) - 1) + 0.5))]
        out.putpixel((i % iron.size[0], i // iron.size[0]), c + (p[3],))
    out.save(os.path.join(GOLEM, 'golem_copper%s.png' % suffix))
    print('golem_copper%s.png from %s (%d colours)' % (suffix, block, len(palette)))

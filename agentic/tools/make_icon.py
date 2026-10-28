"""Draws the ModMenu icon (a violet vis crystal in a gold ring, 32x32 pixel art scaled to 128x128) and the Xaero world map node button.
Usage: python make_icon.py"""
import os
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'assets', 'neothaum', 'icon.png')
S = 32

img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# rounded dark tile with a soft violet glow toward the middle
for y in range(S):
    for x in range(S):
        r = ((x - 15.5) ** 2 + (y - 15.5) ** 2) ** 0.5
        t = max(0.0, 1.0 - r / 22.0)
        img.putpixel((x, y), (int(22 + 40 * t), int(14 + 12 * t), int(38 + 60 * t), 255))
for cx, cy in ((0, 0), (S - 1, 0), (0, S - 1), (S - 1, S - 1)):
    for dx in range(2):
        for dy in range(2):
            if dx + dy < 2:
                img.putpixel((abs(cx - dx), abs(cy - dy)), (0, 0, 0, 0))
d.rectangle((1, 1, S - 2, S - 2), outline=(58, 36, 84, 255))

# gold ring
d.ellipse((5, 5, 26, 26), outline=(196, 152, 58, 255))
d.ellipse((6, 6, 25, 25), outline=(122, 88, 32, 255))
for x, y in ((15, 4), (16, 4), (15, 27), (16, 27), (4, 15), (4, 16), (27, 15), (27, 16)):
    img.putpixel((x, y), (240, 206, 110, 255))

# faceted crystal: light left face, dark right face
top, mid_l, mid_r, bot = (15.5, 7), (10, 13), (21, 13), (15.5, 25)
d.polygon([top, mid_l, (15.5, 14), bot], fill=(196, 128, 255, 255))
d.polygon([top, mid_r, bot, (15.5, 14)], fill=(118, 56, 196, 255))
d.polygon([mid_l, (15.5, 14), mid_r, top], fill=(226, 180, 255, 255))
d.line([top, mid_l, bot, mid_r, top], fill=(60, 20, 104, 255))
for x, y in ((14, 10), (13, 11), (12, 12)):
    img.putpixel((x, y), (255, 244, 255, 255))

# sparkles
for x, y in ((24, 7), (8, 23), (25, 22)):
    img.putpixel((x, y), (255, 230, 150, 255))

img.resize((128, 128), Image.NEAREST).save(OUT)
print('wrote', os.path.normpath(OUT))

# Xaero world map button: 32x16, aura node "on" at u=0 and greyed "off" at u=16
btn = Image.new('RGBA', (32, 16), (0, 0, 0, 0))
STAR = [(7, 1), (8, 1), (7, 2), (8, 2), (7, 3), (8, 3), (1, 7), (2, 7), (3, 7), (1, 8), (2, 8), (3, 8), (12, 7), (13, 7), (14, 7), (12, 8),
        (13, 8), (14, 8), (7, 12), (8, 12), (7, 13), (8, 13), (7, 14), (8, 14)]
for ox, glow, core, ray in ((0, (150, 70, 230, 255), (255, 255, 255, 255), (220, 170, 255, 255)),
                            (16, (70, 70, 80, 255), (150, 150, 160, 255), (110, 110, 120, 255))):
    bd = ImageDraw.Draw(btn)
    bd.ellipse((ox + 3, 3, ox + 12, 12), fill=glow)
    bd.ellipse((ox + 5, 5, ox + 10, 10), fill=ray)
    bd.rectangle((ox + 6, 6, ox + 9, 9), fill=core)
    for x, y in STAR:
        btn.putpixel((ox + x, y), ray)
BTN = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'assets', 'neothaum', 'textures', 'gui', 'worldmap_nodes.png')
btn.save(BTN)
print('wrote', os.path.normpath(BTN))

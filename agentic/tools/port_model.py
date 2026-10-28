"""Translates a decompiled TC4 (1.7.10) ModelBase constructor into modern ModelPart code.

Usage: python port_model.py <ModelX.java> [f]

Prints Java: one `ModelPart` field per ModelRenderer, a constructor body that looks each one up, and a static createLayer() that builds the same
boxes, pivots, rotations, mirroring and parent/child links. 1.7 details kept: a box is mirrored only if the renderer's mirror flag was set
before addBox, and a renderer's texture size at the time of addBox is the model's (setTextureSize afterwards has no effect on the box).

For an armour model (a ModelBiped subclass), pass the constructor's scale f (0.5 or 1.0): `if (f < 1.0F)` branches are resolved with it, and
parts added to the biped's head, body, arms and legs become children of empty parts with those vanilla names and pivots, so the wearer's pose
can be copied onto them. Parts attached to nothing are dropped.
"""
import re
import sys

src = open(sys.argv[1], encoding='utf-8').read()
cls = re.search(r'class (\w+)', src).group(1)
ctor = src[src.index('public ' + cls + '('):]
ctor = ctor[:ctor.index('\n   }\n')]
F = float(sys.argv[2]) if len(sys.argv) > 2 else None


def unroll(m):
    """for (int a = 0; a < N; a++) { body } with this.X[a] becomes N copies naming the parts X0, X1, ..."""
    var, n, body = m.group(1), int(m.group(2)), m.group(3)
    out = []
    for i in range(n):
        b = re.sub(r'this\.(\w+)\[' + var + r'\]', lambda mm: 'this.%s%d' % (mm.group(1), i), body)
        b = re.sub(r'\b' + var + r'\b', str(i), b)
        b = re.sub(r'new ModelRenderer\(this, ([^)]*)\)', lambda mm: 'new ModelRenderer(this, %s)' % ', '.join(str(int(eval(x))) for x in mm.group(1).split(',')), b)
        out.append(b)
    return '\n'.join(out)


ctor = re.sub(r'for \(int (\w) = 0; \1 < (\d+); \1\+\+\) \{\n(.*?)\n\s*\}\n', unroll, ctor, flags=re.S)
ctor = re.sub(r'this\.(\w+)\[(\d+)\]', r'this.\1\2', ctor)

BIPED = {'field_78116_c': 'head', 'field_78114_d': 'hat', 'field_78115_e': 'body', 'field_78112_f': 'right_arm', 'field_78113_g': 'left_arm',
         'field_78123_h': 'right_leg', 'field_78124_i': 'left_leg'}
BIPED_PIVOT = {'head': (0, 0, 0), 'hat': (0, 0, 0), 'body': (0, 0, 0), 'right_arm': (-5, 2, 0), 'left_arm': (5, 2, 0), 'right_leg': (-1.9, 12, 0),
               'left_leg': (1.9, 12, 0)}

if F is not None:
    kept, stack = [], []
    for line in ctor.split('\n'):
        st = line.strip()
        m = re.match(r'if \((!\()?f < 1\.0F\)?\) \{$', st)
        if m:
            cond = F < 1.0
            if m.group(1):
                cond = not cond
            stack.append(cond)
            continue
        if st == '} else {' and stack:
            stack[-1] = not stack[-1]
            continue
        if st == '}' and stack:
            stack.pop()
            continue
        if all(stack):
            kept.append(line)
    ctor = '\n'.join(kept)


def num(s):
    s = s.strip()
    m = re.match(r'\(float\)\s*\((.*)\)$', s)
    if m:
        s = m.group(1)
    s = s.replace('Math.PI', '3.141592653589793').replace('F', '')
    return float(eval(s))


tex_w, tex_h = 64, 32
m = re.search(r'field_78090_t = (\d+)', ctor)
if m:
    tex_w = int(m.group(1))
m = re.search(r'field_78089_u = (\d+)', ctor)
if m:
    tex_h = int(m.group(1))

parts = {}
order = []
children = {}
parent = {}

for line in ctor.split('\n'):
    line = line.strip()
    m = re.match(r'this\.(\w+) = new ModelRenderer\(this(?:, (\d+), (\d+))?\);', line)
    if m:
        name = m.group(1)
        parts[name] = {'u': int(m.group(2) or 0), 'v': int(m.group(3) or 0), 'mirror': False, 'boxes': [], 'pivot': (0, 0, 0), 'rot': [0, 0, 0]}
        order.append(name)
        continue
    m = re.match(r'this\.(\w+)\.func_78784_a\((\d+), (\d+)\);', line)  # setTextureOffset
    if m:
        parts[m.group(1)]['u'], parts[m.group(1)]['v'] = int(m.group(2)), int(m.group(3))
        continue
    m = re.match(r'this\.(\w+)\.func_78790_a\((.*)\);', line)  # addBox with inflation
    if m:
        a = [x.strip() for x in m.group(2).split(',')]
        p = parts[m.group(1)]
        p['boxes'].append((p['u'], p['v'], [num(x) for x in a[:3]], [int(num(x)) for x in a[3:6]], num(a[6]), p['mirror']))
        continue
    m = re.match(r'this\.(\w+)\.func_78789_a\((.*)\);', line)  # addBox
    if m:
        a = [x.strip() for x in m.group(2).split(',')]
        p = parts[m.group(1)]
        p['boxes'].append((p['u'], p['v'], [num(x) for x in a[:3]], [int(num(x)) for x in a[3:6]], 0.0, p['mirror']))
        continue
    m = re.match(r'this\.(\w+)\.func_78793_a\((.*)\);', line)  # setRotationPoint
    if m:
        parts[m.group(1)]['pivot'] = tuple(num(x) for x in m.group(2).split(','))
        continue
    m = re.match(r'this\.(\w+)\.field_78809_i = (true|false);', line)  # mirror
    if m:
        parts[m.group(1)]['mirror'] = m.group(2) == 'true'
        continue
    m = re.match(r'this\.setRotation\(this\.(\w+), (.*)\);', line)
    if m:
        parts[m.group(1)]['rot'] = [num(x) for x in m.group(2).split(',')]
        continue
    m = re.match(r'this\.(\w+)\.field_7879(5|6)_[fg] = (.*);', line) or re.match(r'this\.(\w+)\.field_78808_(h) = (.*);', line)
    if m and m.group(1) in parts:
        idx = {'5': 0, '6': 1, 'h': 2}[m.group(2)]
        try:
            parts[m.group(1)]['rot'][idx] = num(m.group(3))
        except Exception:
            pass
        continue
    m = re.match(r'this\.(\w+)\.func_78792_a\(this\.(\w+)\);', line)  # addChild
    if m:
        p = BIPED.get(m.group(1), m.group(1))
        children.setdefault(p, []).append(m.group(2))
        parent[m.group(2)] = p

used = set()
if F is not None:
    def mark(n):
        used.add(n)
        for c in children.get(n, []):
            mark(c)
    for b in BIPED_PIVOT:
        for c in children.get(b, []):
            mark(c)
    order = [n for n in order if n in used]


def f(x):
    return repr(float(x)) + 'F'


def snake(n):
    return re.sub(r'(?<!^)(?=[A-Z])', '_', n).lower()


def field(n):
    return n[0].lower() + n[1:]


def emit(name, parent_var, out, indent):
    p = parts[name]
    cubes = 'CubeListBuilder.create()'
    for (u, v, o, s, inf, mir) in p['boxes']:
        cubes += '.texOffs(%d, %d)' % (u, v)
        if mir:
            cubes += '.mirror()'
        if inf:
            cubes += '.addBox(%s, %s, %s, %d, %d, %d, new CubeDeformation(%s))' % (f(o[0]), f(o[1]), f(o[2]), s[0], s[1], s[2], f(inf))
        else:
            cubes += '.addBox(%s, %s, %s, %d, %d, %d)' % (f(o[0]), f(o[1]), f(o[2]), s[0], s[1], s[2])
        if mir:
            cubes += '.mirror(false)'
    px, py, pz = p['pivot']
    rx, ry, rz = p['rot']
    pose = 'PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s)' % (f(px), f(py), f(pz), f(rx), f(ry), f(rz))
    var = 'p_' + snake(name)
    decl = 'PartDefinition %s = ' % var if name in children else ''
    out.append('%s%s%s.addOrReplaceChild("%s", %s, %s);' % (indent, decl, parent_var, snake(name), cubes, pose))
    for c in children.get(name, []):
        emit(c, var, out, indent)


def path_of(n):
    path = [n]
    while path[0] in parent:
        path.insert(0, parent[path[0]])
    return path


out = ['\t// generated by agentic/tools/port_model.py from %s%s' % (sys.argv[1].replace('\\', '/').split('/')[-1],
                                                                    '' if F is None else ' with f = %s' % F)]
for n in order:
    out.append('\tfinal ModelPart %s;' % field(n))
out.append('')
out.append('\t// constructor body')
for n in order:
    out.append('\t\tthis.%s = root%s;' % (field(n), ''.join('.getChild("%s")' % (x if x in BIPED_PIVOT else snake(x)) for x in path_of(n))))
out.append('')
out.append('\tpublic static LayerDefinition createLayer() {')
out.append('\t\tMeshDefinition mesh = new MeshDefinition();')
out.append('\t\tPartDefinition root = mesh.getRoot();')
if F is not None:
    for b, (px, py, pz) in BIPED_PIVOT.items():
        var = 'p_' + b
        out.append('\t\tPartDefinition %s = root.addOrReplaceChild("%s", CubeListBuilder.create(), PartPose.offset(%s, %s, %s));' % (var, b, f(px), f(py), f(pz)))
        for c in children.get(b, []):
            emit(c, var, out, '\t\t')
else:
    for n in order:
        if n not in parent:
            emit(n, 'root', out, '\t\t')
out.append('\t\treturn LayerDefinition.create(mesh, %d, %d);' % (tex_w, tex_h))
out.append('\t}')
print('\n'.join(out))

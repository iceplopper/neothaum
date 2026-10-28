"""Splices port_model.py output into a Java template.

Usage: python splice_model.py <generated.txt> <template.java> <out.java>
The template marks where the field declarations, the constructor lookups and createLayer() go with //@FIELDS, //@CTOR and //@LAYER on their
own lines; each block is indented to match its marker.
"""
import sys

gen = open(sys.argv[1], encoding='utf-8').read()
fields = gen[gen.index('\tfinal ModelPart'):gen.index('\n\n\t// constructor body')]
ctor = gen[gen.index('// constructor body') + len('// constructor body\n'):gen.index('\n\n\tpublic static LayerDefinition')]
layer = gen[gen.index('\tpublic static LayerDefinition'):].rstrip('\n')


def block(text, marker_line):
    indent = marker_line[:len(marker_line) - len(marker_line.lstrip())]
    lines = [l[1:] if l.startswith('\t') else l for l in text.split('\n')]
    if text is ctor:
        lines = [l[1:] if l.startswith('\t') else l for l in lines]
    return '\n'.join((indent + l) if l.strip() else '' for l in lines)


out = []
for line in open(sys.argv[2], encoding='utf-8').read().split('\n'):
    s = line.strip()
    if s == '//@FIELDS':
        out.append(block(fields, line))
    elif s == '//@CTOR':
        out.append(block(ctor, line))
    elif s == '//@LAYER':
        out.append(block(layer, line))
    else:
        out.append(line)
open(sys.argv[3], 'w', encoding='utf-8').write('\n'.join(out))

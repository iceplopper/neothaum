"""Minimal NBT (gzip) reader/writer for structure templates: tags become (type, value) pairs so they round-trip exactly."""
import gzip
import struct

END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTES, STRING, LIST, COMPOUND, INTS, LONGS = range(13)


def _read(buf, pos, t):
    if t == BYTE:
        return struct.unpack_from('>b', buf, pos)[0], pos + 1
    if t == SHORT:
        return struct.unpack_from('>h', buf, pos)[0], pos + 2
    if t == INT:
        return struct.unpack_from('>i', buf, pos)[0], pos + 4
    if t == LONG:
        return struct.unpack_from('>q', buf, pos)[0], pos + 8
    if t == FLOAT:
        return struct.unpack_from('>f', buf, pos)[0], pos + 4
    if t == DOUBLE:
        return struct.unpack_from('>d', buf, pos)[0], pos + 8
    if t == BYTES:
        n = struct.unpack_from('>i', buf, pos)[0]
        return bytes(buf[pos + 4:pos + 4 + n]), pos + 4 + n
    if t == STRING:
        n = struct.unpack_from('>H', buf, pos)[0]
        return buf[pos + 2:pos + 2 + n].decode('utf-8'), pos + 2 + n
    if t == LIST:
        et = buf[pos]
        n = struct.unpack_from('>i', buf, pos + 1)[0]
        pos += 5
        items = []
        for _ in range(n):
            v, pos = _read(buf, pos, et)
            items.append(v)
        return (et, items), pos
    if t == COMPOUND:
        d = {}
        while True:
            tt = buf[pos]
            pos += 1
            if tt == END:
                return d, pos
            name, pos = _read(buf, pos, STRING)
            v, pos = _read(buf, pos, tt)
            d[name] = (tt, v)
    if t == INTS:
        n = struct.unpack_from('>i', buf, pos)[0]
        return list(struct.unpack_from('>%di' % n, buf, pos + 4)), pos + 4 + 4 * n
    if t == LONGS:
        n = struct.unpack_from('>i', buf, pos)[0]
        return list(struct.unpack_from('>%dq' % n, buf, pos + 4)), pos + 4 + 8 * n
    raise ValueError(t)


def _write(out, t, v):
    if t == BYTE:
        out += struct.pack('>b', v)
    elif t == SHORT:
        out += struct.pack('>h', v)
    elif t == INT:
        out += struct.pack('>i', v)
    elif t == LONG:
        out += struct.pack('>q', v)
    elif t == FLOAT:
        out += struct.pack('>f', v)
    elif t == DOUBLE:
        out += struct.pack('>d', v)
    elif t == BYTES:
        out += struct.pack('>i', len(v)) + v
    elif t == STRING:
        b = v.encode('utf-8')
        out += struct.pack('>H', len(b)) + b
    elif t == LIST:
        et, items = v
        out += struct.pack('>bi', et if items else (et or END), len(items))
        for i in items:
            _write(out, et, i)
    elif t == COMPOUND:
        for name, (tt, vv) in v.items():
            out += struct.pack('>b', tt)
            _write(out, STRING, name)
            _write(out, tt, vv)
        out += b'\x00'
    elif t == INTS:
        out += struct.pack('>i%di' % len(v), len(v), *v)
    elif t == LONGS:
        out += struct.pack('>i%dq' % len(v), len(v), *v)


def load(path):
    buf = gzip.decompress(open(path, 'rb').read())
    t = buf[0]
    name, pos = _read(buf, 1, STRING)
    root, _ = _read(buf, pos, t)
    return name, root


def save(path, name, root):
    out = bytearray(b'\x0a')
    _write(out, STRING, name)
    _write(out, COMPOUND, root)
    open(path, 'wb').write(gzip.compress(bytes(out)))

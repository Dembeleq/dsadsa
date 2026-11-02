#!/usr/bin/env python3
"""Model onizleme (painter's algorithm). Kullanim: python3 preview.py car_clio [car_supra ...]  -> /tmp/preview.png"""
import json, math, sys, os
from PIL import Image, ImageDraw
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
MD = f'{ROOT}/ChicagoRP-ResourcePack/assets/chicagorp/models/item'
PAL = [(22,22,26),(62,64,70),(135,139,146),(246,246,246),(205,30,36),(30,85,205),(250,205,30),(240,130,20),
       (40,165,62),(135,52,195),(38,58,92),(255,240,170),(205,210,220),(120,15,22),(112,72,42),(150,232,42),
       (215,195,160),(215,170,40),(190,194,200),(170,175,182),(30,90,50),(20,35,90),(30,140,140),(230,120,170),
       (90,20,30),(90,100,50),(8,8,10),(40,42,48),(220,170,30),(120,190,240),(70,45,30),(190,70,255)]
FACES = {  # normal, corner idx
    'north': ((0,0,-1), [(0,0,0),(1,0,0),(1,1,0),(0,1,0)]), 'south': ((0,0,1), [(0,0,1),(1,0,1),(1,1,1),(0,1,1)]),
    'west': ((-1,0,0), [(0,0,0),(0,0,1),(0,1,1),(0,1,0)]), 'east': ((1,0,0), [(1,0,0),(1,0,1),(1,1,1),(1,1,0)]),
    'down': ((0,-1,0), [(0,0,0),(1,0,0),(1,0,1),(0,0,1)]), 'up': ((0,1,0), [(0,1,0),(1,1,0),(1,1,1),(0,1,1)])}

def render(name, yaw, pitch, size=(560, 360), tint=(205, 30, 36), scale=None):
    m = json.load(open(f'{MD}/{name}.json'))
    quads = []
    cy, sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
    cp, sp = math.cos(math.radians(pitch)), math.sin(math.radians(pitch))
    def tr(p):
        x, y, z = p[0] - 8, p[1] - 8, p[2] - 8
        x, z = x * cy + z * sy, -x * sy + z * cy
        y, z = y * cp + z * sp, -y * sp + z * cp
        return x, y, z
    for e in m['elements']:
        f, t = e['from'], e['to']
        for fn, fd in e['faces'].items():
            n, cs = FACES[fn]
            pts = [tr((f[0] + c[0] * (t[0] - f[0]), f[1] + c[1] * (t[1] - f[1]), f[2] + c[2] * (t[2] - f[2]))) for c in cs]
            nn = tr((n[0] + 8, n[1] + 8, n[2] + 8)); nn = (nn[0], nn[1], nn[2])
            if nn[2] > 0: continue          # kameraya bakmayan yuz (kamera -z'den bakar)
            ci = int(fd['uv'][0] / 0.5)
            col = tint if 'tintindex' in fd else PAL[min(ci, 31)]
            if 'tintindex' in fd: col = tuple(int(a * b / 255) for a, b in zip(PAL[3], tint))
            light = 0.55 + 0.45 * max(0, (-nn[2]) * 0.5 + nn[1] * 0.7 + nn[0] * 0.3)
            col = tuple(min(255, int(c * light)) for c in col)
            depth = sum(p[2] for p in pts) / 4
            quads.append((depth, pts, col))
    quads.sort(key=lambda q: -q[0])
    s = scale or 10.5
    img = Image.new('RGB', size, (200, 215, 225)); d = ImageDraw.Draw(img)
    d.rectangle([0, int(size[1] * 0.66), size[0], size[1]], fill=(110, 120, 110))
    for _, pts, col in quads:
        d.polygon([(size[0] / 2 + p[0] * s, size[1] * 0.60 - p[1] * s) for p in pts], fill=col, outline=tuple(int(c * 0.6) for c in col))
    return img

if __name__ == '__main__':
    names = sys.argv[1:]
    views = [(35, 18), (90, 8), (200, 18)]
    W, H = 560, 330
    out = Image.new('RGB', (W * len(views), H * len(names)))
    for i, n in enumerate(names):
        for j, (yw, pt) in enumerate(views):
            out.paste(render(n, yw, pt, (W, H)), (j * W, i * H))
    out.save('/tmp/preview.png'); print('ok', out.size)

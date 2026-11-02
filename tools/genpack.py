#!/usr/bin/env python3
"""ChicagoRP kaynak paketi uretici: 3D arac modelleri, telefon, uygulama ikonlari, 3D tabela yazilari.
Calistir:  python3 genpack.py   ->  ../../ChicagoRP-ResourcePack/ + ChicagoRP-ResourcePack.zip + sha1 + signs-def.yml
Hicbir mod gerekmez: 1.21.4 'item_model' bileseni + duz kaynak paketi (ItemsAdder'a da aktarilabilir)."""
import json, os, struct, zlib, hashlib, zipfile, shutil, sys
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
OUT = os.path.join(ROOT, 'ChicagoRP-ResourcePack')
NS = 'chicagorp'
shutil.rmtree(OUT, ignore_errors=True)
for d in ('items', 'models/item', 'textures/item'):
    os.makedirs(f'{OUT}/assets/{NS}/{d}', exist_ok=True)

PAL = [(22,22,26),(62,64,70),(135,139,146),(246,246,246),(205,30,36),(30,85,205),(250,205,30),(240,130,20),
       (40,165,62),(135,52,195),(38,58,92),(255,240,170),(205,210,220),(120,15,22),(112,72,42),(150,232,42),
       (215,195,160),(215,170,40),(190,194,200),(170,175,182),(30,90,50),(20,35,90),(30,140,140),(230,120,170),
       (90,20,30),(90,100,50),(8,8,10),(40,42,48),(220,170,30),(120,190,240),(70,45,30),(190,70,255)]
BLACK, DGRAY, GRAY, WHITE, RED, BLUE, YELLOW, ORANGE, GREEN, PURPLE, GLASS, LIGHT, CHROME, DRED, BROWN, LIME, \
    BEIGE, GOLD, SILVER2, SILVER, DGREEN, NAVY, TEAL, PINK, MAROON, OLIVE, DBLACK, COAL, MUSTARD, SKY, COFFEE, NEON = range(32)

def write_png(path, w, h, rows):
    def chunk(t, d):
        c = struct.pack('>I', len(d)) + t + d
        return c + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    raw = b''.join(b'\x00' + bytes(v for px in row for v in px) for row in rows)
    data = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) \
        + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    open(path, 'wb').write(data)

def wj(rel, obj):
    p = f'{OUT}/{rel}'
    os.makedirs(os.path.dirname(p), exist_ok=True)
    json.dump(obj, open(p, 'w'), separators=(',', ':'))

json.dump({"pack": {"pack_format": 46, "description": "ChicagoRP 3D modelleri"}}, open(f'{OUT}/pack.mcmeta', 'w'))

# ---- palet dokusu (64x64, her renk 4px'lik dikey seritler)
rows = [[PAL[x // 4] + (255,) for x in range(128)] for _ in range(64)]
write_png(f'{OUT}/assets/{NS}/textures/item/palette.png', 128, 64, rows)

def r3(v): return round(v, 3)
def el(x1, y1, z1, x2, y2, z2, c, tint=False):
    f = {"uv": [c * 0.5 + 0.1, 2, c * 0.5 + 0.4, 6], "texture": "#0"}
    if tint: f["tintindex"] = 0
    return {"from": [r3(x1), r3(y1), r3(z1)], "to": [r3(x2), r3(y2), r3(z2)],
            "faces": {k: dict(f) for k in ("north", "east", "south", "west", "up", "down")}}

def save_model(name, elements, display=None, tinted=False):
    m = {"textures": {"0": f"{NS}:item/palette", "particle": f"{NS}:item/palette"}, "elements": elements}
    if display: m["display"] = display
    wj(f'assets/{NS}/models/item/{name}.json', m)
    mod = {"type": "minecraft:model", "model": f"{NS}:item/{name}"}
    if tinted: mod["tints"] = [{"type": "minecraft:custom_model_data", "index": 0, "default": -1}]
    wj(f'assets/{NS}/items/{name}.json', {"model": mod})

exec(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'cars_v3.py'), encoding='utf-8').read())

# ================= SILAHLAR / KELEPCE / COP / ATM / OK / MERMI =================
HELD = {"gui": {"rotation": [0, 0, 0], "scale": [1, 1, 1]},
        "firstperson_righthand": {"rotation": [0, -90, 0], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
        "thirdperson_righthand": {"rotation": [0, -90, 0], "translation": [0, 3, 1], "scale": [0.7, 0.7, 0.7]},
        "ground": {"scale": [0.6, 0.6, 0.6]}, "fixed": {"rotation": [0, 180, 0], "scale": [1, 1, 1]}}
def E1(*a): return el(*a)

glock = [E1(2.5,8.0,7.2,12.5,10.4,8.8,DGRAY), E1(12.5,8.6,7.5,13.4,9.8,8.5,BLACK), E1(3.0,7.0,7.3,10.0,8.0,8.7,BLACK),
         E1(3.0,3.2,7.3,5.8,7.0,8.7,BLACK), E1(6.0,6.2,7.4,8.4,7.0,8.6,BLACK), E1(6.8,6.6,7.6,7.2,8.0,8.4,DGRAY),
         E1(2.8,10.4,7.7,3.4,10.9,8.3,CHROME), E1(11.8,10.4,7.7,12.4,10.9,8.3,CHROME), E1(3.0,2.6,7.4,5.8,3.2,8.6,DGRAY)]
deagle = [E1(1.5,8.0,7.0,13.5,10.8,9.0,CHROME), E1(13.5,8.6,7.6,15.4,10.2,8.4,DGRAY), E1(2.5,6.8,7.1,11.0,8.0,8.9,DGRAY),
          E1(2.8,2.6,7.2,6.2,6.8,8.8,BLACK), E1(6.4,5.8,7.3,9.0,6.8,8.7,DGRAY), E1(7.2,6.2,7.5,7.7,7.8,8.5,CHROME),
          E1(2.0,10.8,7.6,2.8,11.4,8.4,CHROME), E1(12.8,10.8,7.6,13.4,11.4,8.4,CHROME), E1(2.8,2.0,7.3,6.2,2.6,8.7,DGRAY)]
ak = [E1(0.5,5.5,7.2,4.0,9.0,8.8,COFFEE), E1(4.0,6.5,7.1,10.0,9.5,8.9,DGRAY), E1(4.0,9.5,7.4,10.0,10.2,8.6,DGRAY),
      E1(10.0,6.8,7.2,13.5,8.8,8.8,COFFEE), E1(13.5,7.6,7.6,15.9,8.4,8.4,BLACK), E1(10.0,8.8,7.5,13.5,9.5,8.5,DGRAY),
      E1(6.5,2.6,7.4,8.4,6.5,8.6,DBLACK), E1(7.4,1.2,7.4,9.4,2.8,8.6,DBLACK), E1(4.2,3.8,7.3,5.8,6.5,8.7,BLACK),
      E1(15.3,8.4,7.8,15.9,9.4,8.2,CHROME), E1(5.5,6.0,7.3,7.5,6.5,8.7,BLACK)]
shotgun = [E1(0.5,5.0,7.3,4.0,8.5,8.7,COFFEE), E1(4.0,6.5,7.2,8.0,9.0,8.8,DGRAY), E1(8.0,8.0,7.5,15.9,9.0,8.5,BLACK),
           E1(8.0,6.9,7.5,14.0,7.9,8.5,DGRAY), E1(9.0,6.0,7.3,12.0,7.0,8.7,COFFEE), E1(4.2,3.8,7.3,5.6,6.5,8.7,COFFEE), E1(15.2,9.0,7.8,15.8,9.6,8.2,CHROME)]
sniper = [E1(0.2,5.0,7.3,4.0,8.2,8.7,DGRAY), E1(4.0,6.5,7.2,9.0,9.0,8.8,BLACK), E1(9.0,7.6,7.6,15.9,8.6,8.4,BLACK),
          E1(5.0,9.0,7.4,10.5,10.8,8.6,DBLACK), E1(10.5,9.0,7.5,11.2,10.8,8.5,CHROME), E1(4.6,9.0,7.5,5.0,10.8,8.5,CHROME),
          E1(4.2,3.8,7.3,5.6,6.5,8.7,BLACK), E1(10.5,5.8,7.4,11.1,7.6,7.7,DGRAY), E1(10.5,5.8,8.3,11.1,7.6,8.6,DGRAY)]
bat = [E1(0.8,7.4,7.5,5.0,8.6,8.5,DBLACK), E1(5.0,7.0,7.2,10.0,9.0,8.8,COFFEE), E1(10.0,6.6,7.0,15.5,9.4,9.0,BROWN), E1(0.2,7.2,7.3,0.8,8.8,8.7,COFFEE)]
for n, e in (('wpn_glock', glock), ('wpn_deagle', deagle), ('wpn_ak47', ak), ('wpn_pompali', shotgun), ('wpn_keskin', sniper), ('wpn_bat', bat)):
    save_model(n, e, HELD)
cuffs = []
for ox in (0.0, 8.0):
    cuffs += [E1(2+ox,9.5,7.5,6.5+ox,10.5,8.5,CHROME), E1(2+ox,5.5,7.5,6.5+ox,6.5,8.5,CHROME), E1(2+ox,6.5,7.5,3+ox,9.5,8.5,CHROME), E1(5.5+ox,6.5,7.5,6.5+ox,9.5,8.5,CHROME)]
cuffs += [E1(6.5,7.6,7.6,9.5,8.4,8.4,CHROME), E1(1.5,7.0,7.4,2.0,9.0,8.6,DGRAY)]
save_model('kelepce', cuffs, {"gui": {"rotation": [0, 0, 0], "scale": [1.1, 1.1, 1.1]}, "firstperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 3, 3], "scale": [0.7, 0.7, 0.7]}})
save_model('cop', [E1(1,7.5,7.5,15,8.5,8.5,DBLACK), E1(1,7.3,7.3,4.5,8.7,8.7,DGRAY), E1(14.5,7.4,7.4,15.2,8.6,8.6,CHROME)], HELD)
for n, col in (('ammo_pistol', YELLOW), ('ammo_rifle', GREEN), ('ammo_shell', RED), ('ammo_sniper', BLUE)):
    save_model(n, [E1(3,3,4,13,9,12,col), E1(3,9,4,13,10.2,12,DGRAY), E1(5,5,3.8,11,7,4,WHITE)])
# ATM (2 blok boyunda, on +z): govde, ekran, tus takimi, nakit yuvasi, ust isik
atm = [E1(2,0,3,14,26,13,DGRAY), E1(2.5,6,13,13.5,24,13.6,COAL), E1(3.5,14,13.6,12.5,23,14.0,SKY), E1(3.5,14,14.0,12.5,23,14.1,GLASS),
       E1(3.5,8,13.6,12.5,13,14.4,BLACK), E1(4.2,9,14.4,6,12,14.7,GRAY), E1(7,9,14.4,9,12,14.7,GRAY), E1(10,9,14.4,11.8,12,14.7,GRAY),
       E1(4,5,13.6,12,6.4,14.2,DBLACK), E1(2,26,3,14,28,13,BLUE), E1(4,27.5,13,12,29,13.8,YELLOW), E1(3.5,2,13.6,12.5,4,14.0,CHROME)]
save_model('atm', atm)
# yol tarifi oku (mor, +z yonune bakar)
arrow = [E1(6.8,7.2,1,9.2,8.8,10,PURPLE), E1(7.2,8.8,1,8.8,9.2,10,NEON)]
for i, (z1, z2, hw_) in enumerate([(10, 11.2, 4.2), (11.2, 12.4, 3.2), (12.4, 13.6, 2.2), (13.6, 14.8, 1.2), (14.8, 15.8, 0.5)]):
    arrow.append(E1(8 - hw_, 7.0, z1, 8 + hw_, 9.0, z2, PURPLE)); arrow.append(E1(8 - hw_ + 0.4, 9.0, z1, 8 + hw_ - 0.4, 9.3, z2, NEON))
save_model('nav_arrow', arrow)
# asagi bakan mor ok (hedef isareti) - yone bagimli degil
down = [el(6.8, 17, 6.8, 9.2, 31, 9.2, PURPLE), el(7.3, 17, 7.3, 8.7, 31, 8.7, NEON)]
for i, (y1, y2, hw_) in enumerate([(15.8, 17, 4.2), (14.6, 15.8, 3.2), (13.4, 14.6, 2.2), (12.2, 13.4, 1.2), (11.0, 12.2, 0.5)]):
    down.append(el(8 - hw_, y1, 8 - hw_, 8 + hw_, y2, 8 + hw_, PURPLE)); down.append(el(8 - hw_ + 0.3, y1, 8 - hw_ + 0.3, 8 + hw_ - 0.3, y2, 8 + hw_ - 0.3, NEON))
save_model('nav_arrow_down', down)


# ================= TELEFON (3D) =================
ph = [el(4.5, 1, 7.3, 11.5, 15, 8.7, BLACK),
      el(5.0, 1.6, 7.2, 11.0, 14.4, 8.8, GLASS),
      el(5.0, 11.0, 7.15, 11.0, 14.4, 8.85, PURPLE),
      el(5.0, 7.5, 7.15, 11.0, 11.0, 8.85, BLUE),
      el(5.0, 1.6, 7.15, 11.0, 7.5, 8.85, GLASS),
      el(7.3, 0.6, 7.2, 8.7, 1.0, 8.8, CHROME)]
for i, (cx, cy, col) in enumerate([(5.7, 5.2, RED), (7.7, 5.2, YELLOW), (9.7, 5.2, GREEN), (5.7, 3.0, ORANGE), (7.7, 3.0, LIGHT), (9.7, 3.0, LIME)]):
    ph.append(el(cx, cy, 7.1, cx + 1.3, cy + 1.3, 8.9, col))
ph.append(el(9.8, 12.3, 7.0, 10.6, 13.1, 9.0, BLACK))
save_model('phone', ph, {"gui": {"rotation": [0, 0, 0], "scale": [1.15, 1.15, 1.15]},
                         "firstperson_righthand": {"rotation": [0, -90, 20], "translation": [1.0, 2, 0], "scale": [0.6, 0.6, 0.6]},
                         "thirdperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 3, 1], "scale": [0.5, 0.5, 0.5]}})

# ================= UYGULAMA IKONLARI (2D, 16x16) =================
GLYPHS = {
 'sehir':  ["00111100","01111110","01100110","01100110","01111110","00111100","00011000","00001000"],
 'isler':  ["00111100","00100100","11111111","11111111","11111111","11111111","11111111","00000000"],
 'arac':   ["00000000","00111100","01111110","11111111","11111111","11111111","01100110","00000000"],
 'cete':   ["11111111","11111111","11111111","11111111","01111110","00111100","00011000","00000000"],
 'sosyal': ["00011000","00111100","00011000","00111100","01111110","11111111","11111111","00000000"],
 'oduller':["00011000","00011000","11111111","01111110","00111100","01100110","11000011","00000000"],
 'kilit':  ["00111100","01000010","01000010","11111111","11111111","11111111","11111111","00000000"],
 'harita': ["11111111","10101011","11011101","10110101","11101011","10111101","11010111","11111111"],
 'kapat':  ["10000001","01000010","00100100","00011000","00011000","00100100","01000010","10000001"],
 'saat':   ["00111100","01000010","10001001","10001001","10000101","10000001","01000010","00111100"],
 'para':   ["00111100","01111110","11011011","11011111","11111011","11011011","01111110","00111100"],
}
COLORS = {'sehir': (40,150,230), 'isler': (40,170,90), 'arac': (230,130,30), 'cete': (200,45,60), 'sosyal': (150,70,210),
          'oduller': (240,190,30), 'kilit': (90,90,110), 'harita': (30,120,120), 'kapat': (170,40,40), 'saat': (60,60,150), 'para': (30,140,60)}
# banka ikonu
GLYPHS['banka'] = ["00011000","00111100","11111111","01111110","01011010","01011010","01011010","11111111"]
COLORS['banka'] = (30, 130, 70)

for name, g in GLYPHS.items():
    col = COLORS[name]
    img = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            cx, cy = min(x, 15 - x), min(y, 15 - y)
            if (cx == 0 and cy == 0) or (cx + cy == 1 and (cx == 0 or cy == 0) and False): continue
            if cx == 0 and cy <= 1 or cy == 0 and cx <= 1: continue
            shade = int(30 * (y / 15.0))
            img[y][x] = (max(col[0] - shade, 0), max(col[1] - shade, 0), max(col[2] - shade, 0), 255)
    for gy in range(8):
        for gx in range(8):
            if g[gy][gx] == '1':
                for dy in (0, 1):
                    for dx in (0, 1):
                        px, py = 0 + gx * 1 + 4 - 0, gy * 1 + 4
                        if 0 <= py < 16 and 0 <= px < 16: img[py][px] = (255, 255, 255, 255)
    write_png(f'{OUT}/assets/{NS}/textures/item/app_{name}.png', 16, 16, img)
    wj(f'assets/{NS}/models/item/app_{name}.json', {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/app_{name}"}})
    wj(f'assets/{NS}/items/app_{name}.json', {"model": {"type": "minecraft:model", "model": f"{NS}:item/app_{name}"}})

# ================= 3D TABELA YAZILARI =================
F = {
 'A':["01110","10001","10001","11111","10001","10001","10001"], 'B':["11110","10001","10001","11110","10001","10001","11110"],
 'C':["01110","10001","10000","10000","10000","10001","01110"], 'D':["11110","10001","10001","10001","10001","10001","11110"],
 'E':["11111","10000","10000","11110","10000","10000","11111"], 'G':["01110","10001","10000","10111","10001","10001","01111"],
 'H':["10001","10001","10001","11111","10001","10001","10001"], 'I':["01110","00100","00100","00100","00100","00100","01110"],
 'J':["00111","00010","00010","00010","00010","10010","01100"], 'K':["10001","10010","10100","11000","10100","10010","10001"],
 'L':["10000","10000","10000","10000","10000","10000","11111"], 'M':["10001","11011","10101","10101","10001","10001","10001"],
 'N':["10001","11001","10101","10011","10001","10001","10001"], 'O':["01110","10001","10001","10001","10001","10001","01110"],
 'P':["11110","10001","10001","11110","10000","10000","10000"], 'R':["11110","10001","10001","11110","10100","10010","10001"],
 'S':["01111","10000","10000","01110","00001","00001","11110"], 'T':["11111","00100","00100","00100","00100","00100","00100"],
 'U':["10001","10001","10001","10001","10001","10001","01110"], 'V':["10001","10001","10001","10001","10001","01010","00100"],
 'Y':["10001","10001","01010","00100","00100","00100","00100"], 'Z':["11111","00001","00010","00100","01000","10000","11111"],
 ' ':["00000"]*7,
}
EXTRA = {  # (ustte satirlar, altta satirlar)
 'İ': ('I', ["00100"], []), 'Ö': ('O', ["01010"], []), 'Ü': ('U', ["01010"], []), 'Ç': ('C', [], ["00100"]), 'Ş': ('S', [], ["00100"]), 'Ğ': ('G', ["01110"], []),
}
PX, PH, DEPTH = 0.5, 0.55, 1.2

def glyph(ch):
    if ch in EXTRA:
        base, above, below = EXTRA[ch]
        return F[base], above, below
    return F[ch], [], []

def text_elements(lines, color):
    """lines: satir listesi. Doner: (elements, genislik_birim)"""
    els = []
    maxw = max(len(l) for l in lines) * 6 * PX - PX
    line_h = 11 * PH
    total_h = len(lines) * line_h
    top = 8 + total_h / 2
    for li, line in enumerate(lines):
        w = len(line) * 6 * PX - PX
        x0 = 8 - w / 2
        ybase = top - (li + 1) * line_h + 2 * PH
        for ci, ch in enumerate(line):
            rows, above, below = glyph(ch)
            allrows = []
            for k, r in enumerate(above): allrows.append((-len(above) + k, r))
            for k, r in enumerate(rows): allrows.append((k, r))
            for k, r in enumerate(below): allrows.append((7 + k, r))
            for ri, r in allrows:
                y1 = ybase + (6 - ri) * PH
                x = 0
                while x < 5:
                    if r[x] == '1':
                        s = x
                        while x < 5 and r[x] == '1': x += 1
                        els.append(el(x0 + ci * 6 * PX + s * PX, y1, 8 - DEPTH / 2, x0 + ci * 6 * PX + x * PX, y1 + PH, 8 + DEPTH / 2, color))
                    else:
                        x += 1
    return els, maxw

SIGNS = {  # id: (satirlar, renk, genislik_blok)
 'karakol': (["POLİS", "KARAKOLU"], BLUE, 6.5), 'durumcu': (["DÜRÜMCÜ"], RED, 10), 'market': (["MARKET"], GREEN, 9),
 'giyim': (["GİYİM"], BLACK, 8), 'kahve': (["KAHVEHANE"], BROWN, 11), 'silah': (["SİLAHÇI"], DGRAY, 10),
 'demirci': (["DEMİRCİ"], ORANGE, 10), 'kargo': (["KARGO"], ORANGE, 8), 'mekanik': (["MEKANİK"], BLUE, 10),
 'galeri': (["GALERİ"], BLACK, 14), 'jenerator': (["JENERATÖR"], YELLOW, 12), 'pavyon': (["PAVYON"], PURPLE, 10),
 'belediye': (["BELEDİYE"], BLUE, 11), 'hastane': (["HASTANE"], RED, 14), 'banka': (["BANKA"], YELLOW, 8),
}
cfg = []
for sid, (lines, col, wblocks) in SIGNS.items():
    els, units = text_elements(lines, col)
    save_model(f'sign_{sid}', els)
    cfg.append(f"  {sid}: {{units: {units}, width: {wblocks}}}")
open(os.path.join(ROOT, 'signs-def.yml'), 'w').write("signs-def:\n" + "\n".join(cfg) + "\n")

# ---- zip + sha1
zpath = os.path.join(ROOT, 'ChicagoRP-ResourcePack.zip')
if os.path.exists(zpath): os.remove(zpath)
with zipfile.ZipFile(zpath, 'w', zipfile.ZIP_DEFLATED) as z:
    for d, _, fs in os.walk(OUT):
        for f in fs:
            fp = os.path.join(d, f); z.write(fp, os.path.relpath(fp, OUT))
sha = hashlib.sha1(open(zpath, 'rb').read()).hexdigest()
open(os.path.join(ROOT, 'ChicagoRP-ResourcePack.sha1'), 'w').write(sha)
print('OK', len(os.listdir(f'{OUT}/assets/{NS}/models/item')), 'model', os.path.getsize(zpath) // 1024, 'KB', sha)

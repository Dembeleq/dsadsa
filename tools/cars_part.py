# ================= ARACLAR v2 (on = +z, icleri gorunur, koltuklu) =================
SC = 1.5  # gercek boyut carpani
G = 0.8   # modeli -16..32 sinirina sigdirmak icin kucultme; oyunda scale = SC/G
def build_car(s):
    L, W = s['L'], s['W']; belt, roof, hh, th = s['belt'], s['roof'], s['hood'], s['trunk']
    zf, zr = s['zf'], s['zr']
    body = s['body']; hw = W / 2.0
    wheel = s.get('wheel', 6.4); wcol = s.get('wcol', CHROME)
    E = []
    def B(x1, y1, z1, x2, y2, z2, c): E.append(el(8 + x1 * G, 8 + y1 * G, 8 + z1 * G, 8 + x2 * G, 8 + y2 * G, 8 + z2 * G, c))
    def sym(x1, y1, z1, x2, y2, z2, c):   # iki yan icin
        B(x1, y1, z1, x2, y2, z2, c); B(-x2, y1, z1, -x1, y2, z2, c)
    # sasi + yan paneller (kapilar), kaput, bagaj
    B(-hw, 1.0, -L/2 + 1, hw, 2.2, L/2 - 1, COAL)
    sym(hw - 1.1, 1.8, -L/2 + 2.5, hw, belt, L/2 - 2.5, body)
    B(-hw, 1.8, zf, hw, hh, L/2, body)
    B(-hw + 0.4, hh, zf + 1.5, hw - 0.4, hh + 0.5, L/2 - 3.0, body)
    B(-hw, 1.8, -L/2, hw, th, zr, body)
    B(-hw + 0.4, th, -L/2 + 1.0, hw - 0.4, th + 0.5, zr - 0.5, body)
    # ic mekan: torpido, direksiyon, koltuklar, arka raf
    B(-hw + 1.1, belt - 2.4, zf - 3.4, hw - 1.1, belt - 0.2, zf, COAL)
    B(-hw + 1.1, belt - 0.2, zf - 3.4, hw - 1.1, belt + 0.4, zf - 2.2, DBLACK)
    B(-hw + 1.1, belt - 1.6, zr, hw - 1.1, belt - 0.4, zr + 2.0, COAL)
    def seat(x, z):
        B(x - 2.0, 2.2, z - 2.5, x + 2.0, 4.0, z + 1.5, BEIGE if s.get('beige') else COAL)
        B(x - 2.0, 4.0, z - 3.3, x + 2.0, 8.4, z - 2.3, BEIGE if s.get('beige') else COAL)
        B(x - 1.1, 8.4, z - 3.1, x + 1.1, 10.0, z - 2.4, DBLACK)
    zfs = zf - 5.6; zrs = zr + 4.2; xs = hw * 0.5
    seats = [(xs, zfs), (-xs, zfs)]
    if s['seats'] >= 4: seats += [(xs, zrs), (-xs, zrs)]
    if s['seats'] >= 5: seats += [(0.0, zrs)]
    for (x, z) in seats: seat(x, z)
    xd, zd = seats[0]
    wz = zd + 4.2
    B(xd - 1.5, belt + 0.5, wz, xd + 1.5, belt + 0.9, wz + 0.5, DBLACK); B(xd - 1.5, belt - 1.1, wz, xd + 1.5, belt - 0.7, wz + 0.5, DBLACK)
    B(xd - 1.5, belt - 1.1, wz, xd - 1.1, belt + 0.9, wz + 0.5, DBLACK); B(xd + 1.1, belt - 1.1, wz, xd + 1.5, belt + 0.9, wz + 0.5, DBLACK)
    B(xd - 0.3, belt - 2.0, wz - 1.2, xd + 0.3, belt - 0.6, wz + 0.3, DBLACK)
    # direkler (A egimli, C egimli), cati
    for i in range(4):
        y1 = belt + (roof - belt) * i / 4.0; y2 = belt + (roof - belt) * (i + 1) / 4.0
        za = zf - 0.6 - 1.15 * i
        sym(hw - 1.2, y1, za - 1.0, hw - 0.3, y2, za, body)
        zc = zr + 0.4 + 0.9 * i
        sym(hw - 1.2, y1, zc, hw - 0.3, y2, zc + 1.0, body)
    z_front_edge = zf - 0.6 - 1.15 * 4 - 0.2
    z_rear_edge = zr + 0.4 + 0.9 * 4 + 1.0
    B(-hw + 0.3, roof - 0.8, z_rear_edge - 1.2, hw - 0.3, roof, z_front_edge + 1.2, body)
    if s['seats'] >= 4 and s.get('doors', 4) == 4:
        mid = (zf + zr) / 2.0
        sym(hw - 1.2, belt, mid - 0.5, hw - 0.3, roof - 0.8, mid + 0.5, body)
    # tekerlekler (dis cikintili) + jantlar
    wb = L * 0.2
    for zc in (L/2 - wb, -L/2 + wb):
        for sgn in (1, -1):
            xa, xb = (hw - 0.4, hw + 1.4) if sgn > 0 else (-hw - 1.4, -hw + 0.4)
            B(xa, 0, zc - wheel/2, xb, wheel, zc + wheel/2, BLACK)
            hx1, hx2 = (hw + 1.4, hw + 1.65) if sgn > 0 else (-hw - 1.65, -hw - 1.4)
            B(hx1, wheel/2 - 1.4, zc - 1.4, hx2, wheel/2 + 1.4, zc + 1.4, wcol)
    # kapi izleri + kollar
    for z in (zf - 1.0, (zf + zr) / 2.0 if s['seats'] >= 4 else zr + 1.0):
        sym(hw, 2.4, z, hw + 0.15, belt, z + 0.25, COAL)
        sym(hw, belt - 2.0, z + 1.2, hw + 0.3, belt - 1.4, z + 2.6, CHROME)
    # farlar, stoplar, izgara, tamponlar, plaka, aynalar
    for x in (hw - 3.6, -hw + 1.1):
        B(x, hh - 2.4, L/2 - 0.05, x + 2.5, hh - 0.8, L/2 + 0.4, LIGHT)
        B(x, th - 2.4, -L/2 - 0.4, x + 2.5, th - 0.8, -L/2 + 0.05, RED)
    B(-3.2, 2.8, L/2 - 0.05, 3.2, hh - 1.6, L/2 + 0.3, COAL)
    B(-hw + 0.1, 1.2, L/2 - 0.3, hw - 0.1, 2.8, L/2 + 0.9, s.get('bump', COAL)); B(-hw + 0.1, 1.2, -L/2 - 0.9, hw - 0.1, 2.8, -L/2 + 0.3, s.get('bump', COAL))
    B(-2.4, 3.0, L/2 + 0.4, 2.4, 4.4, L/2 + 0.6, WHITE); B(-2.4, 3.0, -L/2 - 0.6, 2.4, 4.4, -L/2 - 0.4, WHITE)
    sym(hw + 0.1, belt + 0.2, zf - 1.8, hw + 1.4, belt + 1.6, zf - 0.6, body)
    # modele ozel
    for ex in s.get('extras', []):
        if ex == 'scoop':   B(-2.5, hh + 0.5, zf + 3, 2.5, hh + 1.9, zf + 8, COAL)
        if ex == 'stripes':
            B(-2.2, hh + 0.5, zf + 1.5, -0.8, hh + 0.7, L/2 - 3.0, s.get('stripe', WHITE)); B(0.8, hh + 0.5, zf + 1.5, 2.2, hh + 0.7, L/2 - 3.0, s.get('stripe', WHITE))
            B(-2.2, th + 0.5, -L/2 + 1.0, -0.8, th + 0.7, zr - 0.5, s.get('stripe', WHITE)); B(0.8, th + 0.5, -L/2 + 1.0, 2.2, th + 0.7, zr - 0.5, s.get('stripe', WHITE))
        if ex == 'wing':
            sym(hw - 3.5, th, -L/2 + 1.0, hw - 2.5, th + 3.2, -L/2 + 2.0, COAL)
            B(-hw + 0.3, th + 3.2, -L/2 - 0.4, hw - 0.3, th + 3.9, -L/2 + 3.6, s.get('wing', COAL))
        if ex == 'ducktail': B(-hw + 0.6, th + 0.5, -L/2 - 0.6, hw - 0.6, th + 1.6, -L/2 + 3.0, s.get('wing', COAL))
        if ex == 'bullbar':
            B(-hw + 0.8, 2.6, L/2 + 0.9, hw - 0.8, 3.4, L/2 + 1.8, COAL); B(-hw + 0.8, 2.6, L/2 + 0.9, -hw + 1.6, hh - 0.5, L/2 + 1.8, COAL); B(hw - 1.6, 2.6, L/2 + 0.9, hw - 0.8, hh - 0.5, L/2 + 1.8, COAL)
        if ex == 'spare':   B(-3.4, 2.8, -L/2 - 2.4, 3.4, 10.0, -L/2 - 0.9, BLACK); B(-1.3, 5.2, -L/2 - 2.6, 1.3, 7.6, -L/2 - 2.4, CHROME)
        if ex == 'rails':
            sym(hw - 2.4, roof, z_rear_edge, hw - 1.7, roof + 0.7, z_front_edge, CHROME)
            B(-hw + 2.4, roof + 0.7, z_rear_edge + 2, hw - 2.4, roof + 1.1, z_rear_edge + 3, CHROME)
        if ex == 'steps':   sym(hw - 0.2, 1.4, zr + 3, hw + 1.0, 2.0, zf - 1, COAL)
        if ex == 'exhaust': sym(hw - 3.5, 1.4, -L/2 - 1.2, hw - 2.2, 2.8, -L/2 + 0.2, CHROME)
        if ex == 'splitter': B(-hw + 0.3, 0.8, L/2 - 0.5, hw - 0.3, 1.4, L/2 + 2.0, COAL)
    k = SC / 16.0
    def blk(v): return round(v * k, 3)
    offsets = [[blk(x - xd), blk(z - zd)] for (x, z) in seats[1:]]
    return E, [blk(xd), blk(zd)], offsets

# id: (ad, seviye, fiyat, hiz, parametreler)
CARSPEC = {
 'clio':    ("Renault Clio", 1, 40000, 0.22, dict(L=40, W=19, belt=8.4, roof=14.4, hood=7.0, trunk=8.0, zf=6, zr=-19, seats=2, doors=2, body=RED, beige=False, extras=[])),
 'corolla': ("Toyota Corolla", 2, 85000, 0.26, dict(L=46, W=19.5, belt=8.5, roof=14.8, hood=7.2, trunk=8.3, zf=7, zr=-12, seats=2, doors=2, body=WHITE, extras=[])),
 'impreza': ("Subaru Impreza 22B STI", 3, 220000, 0.40, dict(L=44, W=21, belt=8.2, roof=14.2, hood=7.0, trunk=8.6, zf=6, zr=-14, seats=4, doors=2, body=BLUE, wcol=GOLD, wheel=6.8, extras=['scoop', 'wing', 'exhaust'])),
 'egea':    ("Fiat Egea", 4, 150000, 0.30, dict(L=46, W=19.5, belt=8.8, roof=15.2, hood=7.4, trunk=8.6, zf=7, zr=-12, seats=4, doors=4, body=SILVER, extras=[])),
 'jimny':   ("Suzuki Jimny", 5, 260000, 0.28, dict(L=36, W=18.5, belt=10.0, roof=17.5, hood=8.8, trunk=10.0, zf=10, zr=-17, seats=4, doors=2, body=DGREEN, wheel=7.6, extras=['bullbar', 'spare'])),
 'mustang': ("Ford Mustang GT", 6, 600000, 0.46, dict(L=48, W=20.5, belt=8.2, roof=13.5, hood=7.2, trunk=8.4, zf=1, zr=-14, seats=4, doors=2, body=YELLOW, extras=['stripes', 'splitter', 'exhaust'], stripe=BLACK, wcol=CHROME, wheel=6.8)),
 'landcruiser': ("Toyota Land Cruiser 300", 7, 900000, 0.40, dict(L=50, W=21, belt=11.0, roof=19.0, hood=10.0, trunk=11.0, zf=8, zr=-24, seats=5, doors=4, body=DBLACK, wheel=8.0, extras=['rails', 'steps', 'bullbar'])),
 'supra':   ("Toyota GR Supra A90", 8, 1500000, 0.55, dict(L=45, W=21, belt=7.4, roof=12.8, hood=6.4, trunk=7.6, zf=3, zr=-12, seats=2, doors=2, body=ORANGE, extras=['ducktail', 'splitter', 'exhaust'], wing=DBLACK, wheel=6.8)),
}
MATS = {'clio': 'RED_CONCRETE', 'corolla': 'WHITE_CONCRETE', 'impreza': 'BLUE_CONCRETE', 'egea': 'LIGHT_GRAY_CONCRETE', 'jimny': 'GREEN_CONCRETE', 'mustang': 'YELLOW_CONCRETE', 'landcruiser': 'BLACK_CONCRETE', 'supra': 'ORANGE_CONCRETE'}
veh_cfg = ["vehicles:"]
for cid, (nm, lv, price, spd, p) in CARSPEC.items():
    els, drv, offs = build_car(p)
    save_model(f'car_{cid}', els)
    veh_cfg.append(f'  {cid}: {{name: "{nm}", level: {lv}, price: {price}, speed: {spd}, seats: {p["seats"]}, scale: {round(SC / G, 4)}, model: car_{cid}, material: {MATS[cid]}, ia: "", driver: {drv}, seat-offsets: {offs}}}')
open(os.path.join(ROOT, 'vehicles.yml'), 'w').write("\n".join(veh_cfg) + "\n")

# ================= ARACLAR v3: gercek oranli, tekerlek yuvali, icinden gorunur, boyanabilir (tint) =================
DISP = 2.0           # oyunda ItemDisplay olcegi  ->  model birimi = blok * 16/DISP = blok * 8
K = 16.0 / DISP

def build_car(p):
    E = []
    kind = p.get('kind', 'car')
    fixed = p.get('fixed')                      # sabit govde rengi (is araclari); None = boyanabilir
    def B(x1, y1, z1, x2, y2, z2, c, tint=False):
        E.append(el(8 + min(x1, x2) * K, 8 + min(y1, y2) * K, 8 + min(z1, z2) * K, 8 + max(x1, x2) * K, 8 + max(y1, y2) * K, 8 + max(z1, z2) * K, WHITE if tint else c, tint))
    def BODY(x1, y1, z1, x2, y2, z2):
        if fixed is None: B(x1, y1, z1, x2, y2, z2, WHITE, True)
        else: B(x1, y1, z1, x2, y2, z2, fixed)
    def sym(x1, y1, z1, x2, y2, z2, c, body=False):
        for a, b in ((x1, x2), (-x2, -x1)):
            if body: BODY(a, y1, z1, b, y2, z2)
            else: B(a, y1, z1, b, y2, z2, c)
    L, W, roof, belt, hood, trunk = p['L'], p['W'], p['roof'], p['belt'], p['hood'], p['trunk']
    hw = W / 2.0; zF, zR = L / 2.0, -L / 2.0
    zc = p['cowl']; rake = p['rake']; zrf = zc - rake; zrr = p['roofr']; zb = p['rbase']    # on cam / tavan arka / arka cam tabani
    wr = p.get('wheel', 0.33); ra = wr + 0.06
    zw1, zw2 = zF - p.get('ov_f', 0.80), zR + p.get('ov_r', 0.80)
    bump = p.get('bump', COAL)
    # ---- sasi + zemin
    B(-hw + 0.1, 0.2, zR + 0.12, hw - 0.1, 0.3, zF - 0.12, DBLACK)
    # ---- yan paneller (tekerlek yuvalari acik)
    for xa, xb in ((hw - 0.12, hw), (-hw, -hw + 0.12)):
        def P(z1, z2, y1, y2): BODY(xa, y1, z1, xb, y2, z2)
        P(zw1 + ra, zF - 0.05, 0.24, hood - 0.04)                        # on camurluk
        P(zw2 + ra, zw1 - ra, 0.24, belt)                                 # kapilar
        P(zR + 0.05, zw2 - ra, 0.24, trunk - 0.04)                        # arka camurluk
        for zw in (zw1, zw2):
            top = hood - 0.04 if zw == zw1 else trunk - 0.04
            P(zw - ra, zw + ra, wr * 2 + 0.05, top if zw == zw1 else top)
    sym(hw - 0.1, 0.15, zw2 + ra, hw + 0.03, 0.3, zw1 - ra, DBLACK)       # esik
    # ---- on: kaput, panel, izgara, farlar, tampon
    BODY(-hw + 0.1, hood - 0.07, zc, hw - 0.1, hood, zF - 0.04)
    BODY(-hw + 0.1, 0.36, zF - 0.12, hw - 0.1, hood - 0.07, zF - 0.03)
    B(-hw + 0.12, 0.26, zc, hw - 0.12, hood - 0.08, zF - 0.12, DBLACK)
    B(-0.46, 0.5, zF - 0.05, 0.46, hood - 0.18, zF + 0.02, COAL)
    B(-0.36, 0.5, zF - 0.06, 0.36, 0.56, zF + 0.03, CHROME)
    for sx in (1, -1):
        x1, x2 = (0.5, 0.84) if sx > 0 else (-0.84, -0.5)
        B(x1 - 0.03, hood - 0.3, zF - 0.06, x2 + 0.03, hood - 0.08, zF + 0.02, DBLACK)
        B(x1, hood - 0.27, zF - 0.04, x2, hood - 0.11, zF + 0.04, LIGHT)
        B(x1 + 0.03, 0.34, zF - 0.06, x2 - 0.03, 0.43, zF + 0.04, YELLOW)
    B(-hw + 0.02, 0.22, zF - 0.14, hw - 0.02, 0.4, zF + 0.07, bump)
    B(-0.27, 0.25, zF + 0.06, 0.27, 0.36, zF + 0.08, WHITE)
    # ---- arka: bagaj/hatch, stop, tampon
    if p.get('tail', 'sedan') == 'sedan':
        BODY(-hw + 0.1, trunk - 0.07, zR + 0.04, hw - 0.1, trunk, zb)
        BODY(-hw + 0.1, 0.36, zR + 0.03, hw - 0.1, trunk - 0.07, zR + 0.1)
        B(-hw + 0.12, 0.26, zR + 0.1, hw - 0.12, trunk - 0.08, zb, DBLACK)
    elif p.get('tail') == 'hatch':
        BODY(-hw + 0.1, belt - 0.07, zR + 0.04, hw - 0.1, belt, zb)       # kisa bagaj kapagi
        BODY(-hw + 0.1, 0.36, zR + 0.03, hw - 0.1, belt - 0.07, zR + 0.1)
        B(-hw + 0.12, 0.26, zR + 0.1, hw - 0.12, belt - 0.08, zb, DBLACK)
    for sx in (1, -1):
        x1, x2 = (0.46, 0.88) if sx > 0 else (-0.88, -0.46)
        B(x1, trunk - 0.32 if p.get('tail', 'sedan') == 'sedan' else belt - 0.34, zR - 0.03, x2, trunk - 0.1 if p.get('tail', 'sedan') == 'sedan' else belt - 0.12, zR + 0.05, RED)
    B(-hw + 0.02, 0.22, zR - 0.07, hw - 0.02, 0.4, zR + 0.14, bump)
    B(-0.27, 0.25, zR - 0.08, 0.27, 0.36, zR - 0.06, WHITE)
    # ---- ic mekan
    sym(hw - 0.22, 0.32, zrr + 0.15, hw - 0.12, belt - 0.02, zc - 0.1, COAL)             # kapi kartlari
    B(-hw + 0.14, belt - 0.46, zc - 0.55, hw - 0.14, belt + 0.04, zc - 0.04, COAL)         # torpido
    B(-hw + 0.14, belt + 0.04, zc - 0.5, hw - 0.14, belt + 0.07, zc - 0.1, DBLACK)
    B(-0.15, 0.3, zc - 1.1, 0.15, 0.66, zc - 0.5, COAL); B(-0.05, 0.66, zc - 0.85, 0.05, 0.78, zc - 0.75, CHROME)   # konsol + vites
    seats = p['seats']
    scol = p.get('seatcol', BEIGE)
    for (x, z) in seats:
        B(x - 0.22, 0.28, z - 0.3, x + 0.22, 0.46, z + 0.2, scol)
        B(x - 0.22, 0.46, z - 0.36, x + 0.22, 1.0, z - 0.24, scol)
        B(x - 0.12, 1.0, z - 0.35, x + 0.12, 1.16, z - 0.27, DBLACK)
        B(x - 0.25, 0.3, z - 0.28, x - 0.22, 0.6, z + 0.15, DBLACK); B(x + 0.22, 0.3, z - 0.28, x + 0.25, 0.6, z + 0.15, DBLACK)
    xd, zd = seats[0]
    sz = zc - 0.62
    B(xd - 0.19, belt - 0.02, sz, xd + 0.19, belt + 0.03, sz + 0.05, DBLACK); B(xd - 0.19, belt - 0.22, sz, xd + 0.19, belt - 0.17, sz + 0.05, DBLACK)
    B(xd - 0.19, belt - 0.22, sz, xd - 0.14, belt + 0.03, sz + 0.05, DBLACK); B(xd + 0.14, belt - 0.22, sz, xd + 0.19, belt + 0.03, sz + 0.05, DBLACK)
    B(xd - 0.03, belt - 0.4, sz - 0.12, xd + 0.03, belt - 0.1, sz + 0.03, DBLACK)
    # ---- sutunlar, tavan
    n = 6
    for i in range(n):
        t0, t1 = i / n, (i + 1) / n
        y1, y2 = belt + (roof - belt) * t0, belt + (roof - belt) * t1 + 0.01
        za = zc - rake * t0; zb2 = zc - rake * t1
        sym(hw - 0.12, y1, min(za, zb2) - 0.06, hw - 0.03, y2, max(za, zb2) + 0.02, 0, True)
    rb = p.get('rrake', 0.5)
    for i in range(n):
        t0, t1 = i / n, (i + 1) / n
        y1, y2 = roof - (roof - belt) * t0, roof - (roof - belt) * t1
        za = zrr - rb * t0; zb2 = zrr - rb * t1
        sym(hw - 0.12, min(y1, y2) - 0.01, min(za, zb2) - 0.02, hw - 0.03, max(y1, y2), max(za, zb2) + 0.07, 0, True)
    BODY(-hw + 0.1, roof - 0.07, zrr - 0.02, hw - 0.1, roof, zrf + 0.02)
    B(-hw + 0.12, roof - 0.11, zrr, hw - 0.12, roof - 0.07, zrf, BEIGE)
    BODY(-hw + 0.1, belt - 0.0, zrr - rb - 0.04, hw - 0.1, belt + 0.06, zrr - rb + 0.05) if p.get('tail') == 'hatch' else None
    BODY(-hw + 0.12, belt, zc - 0.05, hw - 0.12, belt + 0.07, zc + 0.1)       # cam alti panel
    if p.get('doors', 2) == 4:
        zB = (zrf + zrr) / 2.0
        sym(hw - 0.13, belt, zB - 0.06, hw - 0.02, roof - 0.07, zB + 0.06, DBLACK)
    sym(hw - 0.12, belt, zrr - rb, hw + 0.0, belt + 0.035, zc, DBLACK)       # kemer ustu siyah cita
    # ---- kapi detaylari, aynalar
    sx_ = [zc - 0.12, zrr + 0.1] if p.get('doors', 2) == 4 else [zc - 0.12]
    for zz in sx_ + [(zc + zrr) / 2.0]:
        sym(hw, 0.3, zz, hw + 0.012, belt - 0.03, zz + 0.02, DBLACK)
    sym(hw, belt - 0.2, zc - 0.5, hw + 0.035, belt - 0.14, zc - 0.34, CHROME)
    sym(hw + 0.0, belt + 0.12, zc - 0.02, hw + 0.2, belt + 0.28, zc + 0.12, 0, True); sym(hw - 0.05, belt + 0.06, zc, hw + 0.02, belt + 0.12, zc + 0.08, DBLACK)
    # ---- tekerlekler (sekizgen), jant
    for zw in (zw1, zw2):
        for sx in (1, -1):
            xa, xb = ((hw - 0.34, hw - 0.1) if sx > 0 else (-hw + 0.1, -hw + 0.34))
            B(xa, 0.0, zw - 0.2, xb, wr * 2, zw + 0.2, BLACK); B(xa, 0.12, zw - wr, xb, wr * 2 - 0.12, zw + wr, BLACK)
            ox = (hw - 0.11, hw - 0.075) if sx > 0 else (-hw + 0.075, -hw + 0.11)
            B(ox[0], 0.15, zw - 0.13, ox[1], wr * 2 - 0.15, zw + 0.13, p.get('rim', CHROME)); B(ox[0], 0.11, zw - 0.07, ox[1], wr * 2 - 0.11, zw + 0.07, p.get('rim', CHROME))
            B(ox[0] + 0.0, 0.27, zw - 0.04, ox[1] + 0.01, 0.4, zw + 0.04, DGRAY)
    # ---- modele ozel
    for ex in p.get('extras', []):
        if ex == 'scoop':   B(-0.3, hood, zc + 0.35, 0.3, hood + 0.1, zc + 0.9, COAL)
        if ex == 'stripes':
            for a in (-0.28, 0.1): B(a, hood + 0.0, zc, a + 0.18, hood + 0.01, zF - 0.04, p.get('stripe', WHITE)); B(a, trunk, zR + 0.04, a + 0.18, trunk + 0.01, zb, p.get('stripe', WHITE)); B(a, roof, zrr, a + 0.18, roof + 0.01, zrf, p.get('stripe', WHITE))
        if ex == 'wing':
            sym(hw - 0.5, trunk, zR + 0.12, hw - 0.4, trunk + 0.3, zR + 0.22, COAL); B(-hw + 0.1, trunk + 0.3, zR + 0.05, hw - 0.1, trunk + 0.36, zR + 0.4, p.get('wing', COAL))
        if ex == 'ducktail': B(-hw + 0.15, trunk, zR - 0.02, hw - 0.15, trunk + 0.1, zR + 0.3, p.get('wing', COAL))
        if ex == 'bullbar':
            B(-hw + 0.2, 0.3, zF + 0.04, hw - 0.2, 0.4, zF + 0.16, COAL); sym(hw - 0.3, 0.3, zF + 0.04, hw - 0.2, hood - 0.1, zF + 0.16, COAL)
        if ex == 'spare':   B(-0.33, 0.3, zR - 0.28, 0.33, 0.95, zR - 0.06, BLACK); B(-0.15, 0.5, zR - 0.3, 0.15, 0.75, zR - 0.27, CHROME)
        if ex == 'rails':   sym(hw - 0.3, roof, zrr + 0.05, hw - 0.22, roof + 0.07, zrf - 0.05, CHROME); B(-hw + 0.3, roof + 0.07, zrr + 0.3, hw - 0.3, roof + 0.1, zrr + 0.42, CHROME)
        if ex == 'steps':   sym(hw - 0.0, 0.2, zw2 + ra, hw + 0.14, 0.26, zw1 - ra, COAL)
        if ex == 'exhaust': sym(hw - 0.55, 0.2, zR - 0.1, hw - 0.4, 0.34, zR + 0.05, CHROME)
        if ex == 'splitter': B(-hw + 0.1, 0.12, zF - 0.05, hw - 0.1, 0.2, zF + 0.2, COAL)
        if ex == 'cargobox':
            sym(hw - 0.12, 0.24, zR + 0.05, hw, roof - 0.07, zrf - 0.95, 0, True)
            BODY(-hw + 0.1, 0.36, zR + 0.03, hw - 0.1, roof - 0.07, zR + 0.1); BODY(-hw + 0.1, 0.36, zrf - 1.0, hw - 0.1, roof - 0.07, zrf - 0.93)
            B(-0.04, 0.4, zR - 0.02, 0.04, roof - 0.15, zR + 0.04, DBLACK)
            sym(hw + 0.0, 0.5, zR + 0.4, hw + 0.012, roof - 0.5, zR + 0.42, DBLACK)
        if ex == 'bed':
            zbc = zrr - rb - 0.1
            B(-hw + 0.1, 0.6, zR + 0.05, hw - 0.1, 0.68, zbc, DGRAY)
            sym(hw - 0.1, 0.6, zR + 0.05, hw, belt + 0.05, zbc, 0, True)
            BODY(-hw + 0.1, 0.6, zbc - 0.1, hw - 0.1, belt + 0.05, zbc)
            BODY(-hw + 0.1, 0.6, zR + 0.05, hw - 0.1, belt + 0.02, zR + 0.12)
        if ex == 'lightbar':
            B(-0.55, roof, zc - 0.6, 0.55, roof + 0.1, zc - 0.3, DBLACK); B(-0.5, roof + 0.1, zc - 0.58, -0.02, roof + 0.2, zc - 0.32, RED); B(0.02, roof + 0.1, zc - 0.58, 0.5, roof + 0.2, zc - 0.32, BLUE)
        if ex == 'taxisign': B(-0.3, roof, zc - 0.55, 0.3, roof + 0.18, zc - 0.3, YELLOW); B(-0.22, roof + 0.07, zc - 0.56, 0.22, roof + 0.12, zc - 0.29, BLACK)
        if ex == 'stripe_side': sym(hw, 0.42, zw2 + ra, hw + 0.02, 0.52, zw1 - ra, p.get('stripe', WHITE))
        if ex == 'badge': sym(hw, belt - 0.4, zc - 0.9, hw + 0.02, belt - 0.2, zc - 0.5, p.get('stripe', WHITE))
    k = 1.0
    return E, [round(xd, 3), round(zd, 3)], [[round(x - xd, 3), round(z - zd, 3)] for (x, z) in seats[1:]]

def spec(cid, name, level, price, speed, tint, mat, trunk_slots, p):
    return cid, name, level, price, speed, tint, mat, trunk_slots, p

hw_ = lambda W: W / 2.0
CARSPEC = [
 spec('clio', "Renault Clio", 1, 40000, 0.22, 0xC8201E, 'RED_CONCRETE', 9,
      dict(L=3.6, W=1.64, roof=1.46, belt=0.92, hood=0.98, trunk=1.0, cowl=0.62, rake=0.58, roofr=-1.1, rbase=-1.4, rrake=0.5, tail='hatch', doors=2,
           seats=[(0.4, -0.2), (-0.4, -0.2)], wheel=0.31, ov_f=0.7, ov_r=0.62, extras=['badge'])),
 spec('corolla', "Toyota Corolla", 2, 85000, 0.26, 0xF2F2F2, 'WHITE_CONCRETE', 18,
      dict(L=4.0, W=1.68, roof=1.45, belt=0.93, hood=0.96, trunk=1.0, cowl=0.58, rake=0.62, roofr=-0.85, rbase=-1.25, rrake=0.55, doors=2,
           seats=[(0.4, -0.12), (-0.4, -0.12)], wheel=0.32, ov_f=0.78, ov_r=0.8, extras=['badge'])),
 spec('impreza', "Subaru Impreza 22B STI", 3, 220000, 0.40, 0x1E4FC8, 'BLUE_CONCRETE', 18,
      dict(L=4.1, W=1.78, roof=1.4, belt=0.9, hood=0.98, trunk=1.0, cowl=0.6, rake=0.6, roofr=-0.95, rbase=-1.3, rrake=0.55, doors=2,
           seats=[(0.42, -0.1), (-0.42, -0.1), (0.42, -0.85), (-0.42, -0.85)], wheel=0.34, rim=GOLD, extras=['scoop', 'wing', 'exhaust', 'stripe_side'], stripe=GOLD)),
 spec('egea', "Fiat Egea", 4, 150000, 0.30, 0xB8BCC4, 'LIGHT_GRAY_CONCRETE', 27,
      dict(L=4.4, W=1.78, roof=1.5, belt=0.96, hood=1.0, trunk=1.03, cowl=0.7, rake=0.65, roofr=-1.05, rbase=-1.5, rrake=0.55, doors=4,
           seats=[(0.42, -0.12), (-0.42, -0.12), (0.42, -0.85), (-0.42, -0.85)], wheel=0.33, ov_f=0.85, ov_r=0.85, extras=['badge'])),
 spec('jimny', "Suzuki Jimny", 5, 260000, 0.28, 0x2C6B3A, 'GREEN_CONCRETE', 27,
      dict(L=3.5, W=1.66, roof=1.76, belt=1.14, hood=1.12, trunk=1.14, cowl=0.52, rake=0.1, roofr=-1.5, rbase=-1.62, rrake=0.08, tail='hatch', doors=2,
           seats=[(0.4, -0.12), (-0.4, -0.12), (0.4, -0.95), (-0.4, -0.95)], wheel=0.38, ov_f=0.55, ov_r=0.5, extras=['bullbar', 'spare'], bump=COAL, seatcol=DGRAY)),
 spec('mustang', "Ford Mustang GT", 6, 600000, 0.46, 0xF2C21E, 'YELLOW_CONCRETE', 18,
      dict(L=4.7, W=1.88, roof=1.36, belt=0.9, hood=0.96, trunk=1.0, cowl=0.4, rake=0.62, roofr=-1.2, rbase=-1.68, rrake=0.48, doors=2,
           seats=[(0.45, -0.5), (-0.45, -0.5), (0.45, -1.05), (-0.45, -1.05)], wheel=0.35, ov_f=0.95, ov_r=0.9, extras=['stripes', 'splitter', 'exhaust'], stripe=BLACK, seatcol=DBLACK)),
 spec('landcruiser', "Toyota Land Cruiser 300", 7, 900000, 0.40, 0x181A1E, 'BLACK_CONCRETE', 54,
      dict(L=4.9, W=1.98, roof=1.95, belt=1.22, hood=1.26, trunk=1.22, cowl=0.95, rake=0.5, roofr=-2.0, rbase=-2.2, rrake=0.12, tail='hatch', doors=4,
           seats=[(0.5, 0.1), (-0.5, 0.1), (0.5, -0.8), (-0.5, -0.8), (0.0, -0.8)], wheel=0.42, ov_f=0.9, ov_r=0.85, extras=['rails', 'steps', 'bullbar'], seatcol=DGRAY)),
 spec('supra', "Toyota GR Supra A90", 8, 1500000, 0.55, 0xF07A14, 'ORANGE_CONCRETE', 9,
      dict(L=4.2, W=1.88, roof=1.27, belt=0.82, hood=0.9, trunk=0.95, cowl=0.62, rake=0.72, roofr=-0.55, rbase=-1.05, rrake=0.6, doors=2,
           seats=[(0.45, -0.15), (-0.45, -0.15)], wheel=0.35, ov_f=0.85, ov_r=0.8, extras=['ducktail', 'splitter', 'exhaust'], wing=DBLACK, seatcol=DBLACK, bump=DBLACK)),
]
JOBCARS = [
 spec('polis_araci', "Polis Aracı", 0, 0, 0.42, 0, 'BLUE_CONCRETE', 18,
      dict(L=4.4, W=1.78, roof=1.5, belt=0.96, hood=1.0, trunk=1.03, cowl=0.7, rake=0.65, roofr=-1.05, rbase=-1.5, rrake=0.55, doors=4, fixed=WHITE,
           seats=[(0.42, -0.12), (-0.42, -0.12), (0.42, -0.85), (-0.42, -0.85)], wheel=0.33, ov_f=0.85, ov_r=0.85, extras=['lightbar', 'stripe_side'], stripe=NAVY, seatcol=DBLACK)),
 spec('taksi', "Taksi", 0, 0, 0.30, 0, 'YELLOW_CONCRETE', 18,
      dict(L=4.2, W=1.74, roof=1.47, belt=0.94, hood=0.98, trunk=1.0, cowl=0.62, rake=0.62, roofr=-0.95, rbase=-1.35, rrake=0.55, doors=4, fixed=YELLOW,
           seats=[(0.42, -0.1), (-0.42, -0.1), (0.42, -0.85), (-0.42, -0.85)], wheel=0.33, extras=['taxisign', 'stripe_side'], stripe=BLACK)),
]
JOBCARS += [
 spec('kargo_van', "Kargo Van", 0, 0, 0.30, 0, 'ORANGE_CONCRETE', 54,
      dict(L=4.8, W=1.92, roof=2.1, belt=1.12, hood=1.0, trunk=1.0, cowl=1.25, rake=0.35, roofr=-2.3, rbase=-2.35, rrake=0.05, tail='none', doors=2, fixed=WHITE, extras=['cargobox', 'stripe_side', 'badge'], stripe=ORANGE,
           seats=[(0.45, 0.45), (-0.45, 0.45)], wheel=0.36, ov_f=0.8, ov_r=0.8, seatcol=DGRAY)),
 spec('mekanik_pickup', "Mekanik Pickup", 0, 0, 0.32, 0, 'BLUE_CONCRETE', 54,
      dict(L=5.0, W=1.9, roof=1.8, belt=1.1, hood=1.05, trunk=1.1, cowl=0.9, rake=0.5, roofr=-0.45, rbase=-0.5, rrake=0.1, tail='none', doors=2, fixed=BLUE, extras=['bed', 'stripe_side', 'rails'], stripe=WHITE,
           seats=[(0.45, 0.15), (-0.45, 0.15)], wheel=0.4, ov_f=0.85, ov_r=0.8, seatcol=DGRAY)),
]
# durumcu motoru (scooter) - tek kisilik
def build_scooter():
    E = []
    def B(x1, y1, z1, x2, y2, z2, c): E.append(el(8 + min(x1, x2) * K, 8 + min(y1, y2) * K, 8 + min(z1, z2) * K, 8 + max(x1, x2) * K, 8 + max(y1, y2) * K, 8 + max(z1, z2) * K, c))
    for zc in (0.65, -0.65):
        B(-0.06, 0.0, zc - 0.15, 0.06, 0.38, zc + 0.15, BLACK); B(-0.06, 0.07, zc - 0.22, 0.06, 0.31, zc + 0.22, BLACK); B(-0.07, 0.15, zc - 0.07, 0.07, 0.23, zc + 0.07, CHROME)
    B(-0.22, 0.3, -0.85, 0.22, 0.6, 0.05, RED)                 # govde
    B(-0.2, 0.6, -0.9, 0.2, 0.72, -0.1, DBLACK)                # koltuk
    B(-0.14, 0.3, 0.05, 0.14, 0.95, 0.3, RED)                  # on panel
    B(-0.04, 0.38, 0.55, 0.04, 1.0, 0.7, DGRAY)                # catal
    B(-0.38, 1.0, 0.5, 0.38, 1.06, 0.62, BLACK)                # gidon
    B(-0.12, 0.92, 0.5, 0.12, 1.05, 0.7, LIGHT)                # far
    B(-0.2, 0.42, -1.05, 0.2, 0.62, -0.85, RED); B(-0.18, 0.62, -1.2, 0.18, 0.7, -0.88, CHROME)   # arka kutu
    B(-0.16, 0.3, -0.5, 0.16, 0.36, 0.0, DGRAY)                # ayaklik
    return E, [0.0, -0.45], []
els, drv, offs = build_scooter()
save_model('car_durum_motor', els)
veh_extra = (f'  durum_motor: {{name: "Dürüm Motoru", level: 0, price: 0, speed: 0.34, seats: 1, scale: {DISP}, model: car_durum_motor, material: RED_CONCRETE, ia: "", color: 0, paint: false, trunk: 9, job-car: true, driver: {drv}, seat-offsets: []}}')
veh = ["vehicles:"]
FIX_MAP = {}
for cid, name, lv, price, spd, tint, mat, trunk, p in CARSPEC + JOBCARS:
    els, drv, offs = build_car(p)
    save_model(f'car_{cid}', els, tinted=(p.get('fixed') is None))
    job = lv == 0
    veh.append(f'  {cid}: {{name: "{name}", level: {lv}, price: {price}, speed: {spd}, seats: {len(p["seats"])}, scale: {DISP}, model: car_{cid}, material: {mat}, ia: "", color: {tint}, paint: {"false" if job else "true"}, trunk: {trunk}, job-car: {"true" if job else "false"}, driver: {drv}, seat-offsets: {offs}}}')
veh.append(veh_extra)
open(os.path.join(ROOT, 'vehicles.yml'), 'w').write("\n".join(veh) + "\n")

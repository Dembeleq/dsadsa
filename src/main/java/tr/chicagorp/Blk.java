package tr.chicagorp;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.sign.Side;

/** Yerel koordinatli insaat seti. Yerel on cephe lz=0'dadir (kuzey). r: 0-3 saat yonunde 90 derece donus. */
public class Blk {
    public final World w;
    final int ox, oy, oz, sx, sz, r;
    private static final BlockFace[] ORDER = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public Blk(World w, int ox, int oy, int oz, int sx, int sz, int r) {
        this.w = w; this.ox = ox; this.oy = oy; this.oz = oz; this.sx = sx; this.sz = sz; this.r = r;
    }

    public Block at(int lx, int ly, int lz) {
        int wx, wz;
        switch (r) {
            case 0 -> { wx = ox + lx; wz = oz + lz; }
            case 1 -> { wx = ox + (sz - 1 - lz); wz = oz + lx; }
            case 2 -> { wx = ox + sx - 1 - lx; wz = oz + sz - 1 - lz; }
            default -> { wx = ox + lz; wz = oz + (sx - 1 - lx); }
        }
        return w.getBlockAt(wx, oy + ly, wz);
    }

    /** Kose koordinatli (kesirli) yerel nokta -> dunya konumu. */
    public Location locf(double fx, double fy, double fz) {
        double wx, wz;
        switch (r) {
            case 0 -> { wx = ox + fx; wz = oz + fz; }
            case 1 -> { wx = ox + (sz - fz); wz = oz + fx; }
            case 2 -> { wx = ox + sx - fx; wz = oz + sz - fz; }
            default -> { wx = ox + fz; wz = oz + (sx - fx); }
        }
        return new Location(w, wx, oy + fy, wz);
    }

    /** Binanin on cephesinin baktigi yonun entity yaw'i. */
    public float yaw() { return (180f + r * 90f) % 360f; }

    public Location loc(int lx, int ly, int lz) { return at(lx, ly, lz).getLocation().add(0.5, 0, 0.5); }

    public BlockFace rot(BlockFace f) {
        for (int i = 0; i < 4; i++) if (ORDER[i] == f) return ORDER[(i + r) % 4];
        return f;
    }

    static boolean phys(Material m) {
        String n = m.name();
        return n.endsWith("_PANE") || n.endsWith("FENCE") || n.endsWith("_WALL") || m == Material.IRON_BARS;
    }

    public void set(int x, int y, int z, Material m) { at(x, y, z).setType(m, phys(m)); }

    public void fill(int x1, int y1, int z1, int x2, int y2, int z2, Material m) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) set(x, y, z, m);
    }

    /** Dis kabuk wall, ic bosluk hava (taban ve tavan katmani dahil kabuk). */
    public void box(int x1, int y1, int z1, int x2, int y2, int z2, Material wall) {
        fill(x1, y1, z1, x2, y2, z2, wall);
        fill(x1 + 1, y1 + 1, z1 + 1, x2 - 1, y2 - 1, z2 - 1, Material.AIR);
    }

    public void ring(int y, int x1, int z1, int x2, int z2, Material m) {
        fill(x1, y, z1, x2, y, z1, m); fill(x1, y, z2, x2, y, z2, m);
        fill(x1, y, z1, x1, y, z2, m); fill(x2, y, z1, x2, y, z2, m);
    }

    public void checker(int y, int x1, int z1, int x2, int z2, Material a, Material b2, int cell) {
        for (int x = x1; x <= x2; x++) for (int z = z1; z <= z2; z++)
            set(x, y, z, (((x - x1) / cell + (z - z1) / cell) % 2 == 0) ? a : b2);
    }

    public void data(int x, int y, int z, String bd) { at(x, y, z).setBlockData(Bukkit.createBlockData(bd), false); }

    public void stair(int x, int y, int z, Material m, BlockFace f, boolean top) {
        Block b = at(x, y, z);
        b.setType(m, false);
        org.bukkit.block.data.type.Stairs d = (org.bukkit.block.data.type.Stairs) b.getBlockData();
        d.setFacing(rot(f));
        d.setHalf(top ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
        b.setBlockData(d, false);
    }

    public void slab(int x, int y, int z, Material m, boolean top) {
        Block b = at(x, y, z);
        b.setType(m, false);
        org.bukkit.block.data.type.Slab d = (org.bukkit.block.data.type.Slab) b.getBlockData();
        d.setType(top ? org.bukkit.block.data.type.Slab.Type.TOP : org.bukkit.block.data.type.Slab.Type.BOTTOM);
        b.setBlockData(d, false);
    }

    public void door(int x, int y, int z, Material m, BlockFace f) {
        Block b = at(x, y, z), t = at(x, y + 1, z);
        b.setType(m, false); t.setType(m, false);
        org.bukkit.block.data.type.Door d = (org.bukkit.block.data.type.Door) b.getBlockData();
        d.setFacing(rot(f)); d.setHalf(Bisected.Half.BOTTOM); b.setBlockData(d, false);
        org.bukkit.block.data.type.Door u = (org.bukkit.block.data.type.Door) t.getBlockData();
        u.setFacing(rot(f)); u.setHalf(Bisected.Half.TOP); t.setBlockData(u, false);
    }

    public void wallSign(int x, int y, int z, BlockFace f, String... lines) {
        Block b = at(x, y, z);
        b.setType(Material.OAK_WALL_SIGN, false);
        org.bukkit.block.data.type.WallSign d = (org.bukkit.block.data.type.WallSign) b.getBlockData();
        d.setFacing(rot(f));
        b.setBlockData(d, false);
        Sign s = (Sign) b.getState();
        for (int i = 0; i < lines.length && i < 4; i++) s.getSide(Side.FRONT).line(i, Component.text(lines[i]));
        s.setWaxed(true);
        s.update();
    }

    public void chest(int x, int y, int z, BlockFace f) {
        Block b = at(x, y, z);
        b.setType(Material.CHEST, false);
        org.bukkit.block.data.type.Chest d = (org.bukkit.block.data.type.Chest) b.getBlockData();
        d.setFacing(rot(f));
        b.setBlockData(d, false);
    }

    /** Yatak: ayak (x,z), bas f yonunde bir blok ileride. */
    public void bed(int x, int y, int z, BlockFace f, Material bed) {
        Block foot = at(x, y, z), head = at(x + f.getModX(), y, z + f.getModZ());
        foot.setType(bed, false); head.setType(bed, false);
        org.bukkit.block.data.type.Bed a = (org.bukkit.block.data.type.Bed) foot.getBlockData();
        a.setPart(org.bukkit.block.data.type.Bed.Part.FOOT); a.setFacing(rot(f)); foot.setBlockData(a, false);
        org.bukkit.block.data.type.Bed h = (org.bukkit.block.data.type.Bed) head.getBlockData();
        h.setPart(org.bukkit.block.data.type.Bed.Part.HEAD); h.setFacing(rot(f)); head.setBlockData(h, false);
    }

    /** Masa: cit + basinc plakasi. */
    public void table(int x, int y, int z) { set(x, y, z, Material.OAK_FENCE); set(x, y + 1, z, Material.OAK_PRESSURE_PLATE); }

    /** Sandalye: merdiven, sirt f'nin tersine bakar (f: oturanin baktigi yon). */
    public void chair(int x, int y, int z, BlockFace lookDir) {
        BlockFace back = lookDir.getOppositeFace();
        stair(x, y, z, Material.OAK_STAIRS, back, false);
    }

    /** Ucgen beşik çatı; sirt z ekseni boyunca. y: ilk catı katmani. */
    public void gable(int x1, int z1, int x2, int z2, int y, Material stair, Material full, Material wall) {
        for (int k = 0; x1 - 1 + k <= x2 + 1 - k; k++) {
            int l = x1 - 1 + k, rr = x2 + 1 - k;
            for (int z = z1 - 1; z <= z2 + 1; z++) {
                if (l == rr) set(l, y + k, z, full);
                else { stair(l, y + k, z, stair, BlockFace.EAST, false); stair(rr, y + k, z, stair, BlockFace.WEST, false); }
            }
            if (k >= 1) { fill(l, y + k - 1, z1, rr, y + k - 1, z1, wall); fill(l, y + k - 1, z2, rr, y + k - 1, z2, wall); }
        }
    }
}

package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import static org.bukkit.Material.*;

/** Spawn meydani, park, liman, tarla, otopark ve ortak dekor. */
public final class Places {
    private Places() {}
    private static BlockData leaves;

    static BlockData leaves() {
        if (leaves == null) leaves = Bukkit.createBlockData("minecraft:oak_leaves[persistent=true]");
        return leaves;
    }

    public static void tree(Blk b, int x, int z, int h) {
        for (int i = 0; i < h; i++) b.set(x, i, z, OAK_LOG);
        for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 1; dy++) for (int dz = -2; dz <= 2; dz++) {
            if (dx * dx + dy * dy + dz * dz > 5) continue;
            if (dx == 0 && dz == 0 && dy <= 0) continue;
            var bl = b.at(x + dx, h + dy, z + dz);
            if (bl.getType() == AIR) bl.setBlockData(leaves(), false);
        }
    }

    public static void lamp(Blk b, int x, int z) {
        b.fill(x, 0, z, x, 3, z, STONE_BRICK_WALL);
        b.set(x, 4, z, LANTERN);
    }

    public static void bench(Blk b, int x, int z, BlockFace face) { b.stair(x, 0, z, OAK_STAIRS, face, false); }

    // ------------------------------------------------------------ SPAWN MEYDANI (58x58)
    public static Location spawn(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        b.checker(-1, 0, 0, 57, 57, SMOOTH_QUARTZ, POLISHED_ANDESITE, 4);
        b.ring(-1, 0, 0, 57, 57, STONE_BRICKS); b.ring(-1, 1, 1, 56, 56, STONE_BRICKS);
        // buyuk cesme
        for (int dx = -10; dx <= 10; dx++) for (int dz = -10; dz <= 10; dz++) {
            int d2 = dx * dx + dz * dz, x = 28 + dx, z = 28 + dz;
            if (d2 >= 56 && d2 <= 90) b.set(x, 0, z, STONE_BRICKS);
            else if (d2 < 56) { b.set(x, -1, z, LIGHT_BLUE_CONCRETE); b.set(x, 0, z, WATER); }
            if (d2 <= 12) b.set(x, 0, z, QUARTZ_BLOCK);
            if (d2 <= 4) b.set(x, 1, z, QUARTZ_BLOCK);
        }
        b.fill(28, 2, 28, 28, 7, 28, QUARTZ_PILLAR); b.set(28, 8, 28, SEA_LANTERN);
        for (int[] o : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) { b.set(28 + o[0], 2, 28 + o[1], WATER); b.set(28 + 2 * o[0], 1, 28 + 2 * o[1], WATER); }
        for (int[] p : new int[][]{{11, 0, 1}, {-11, 0, -1}, {0, 11, 2}, {0, -11, 3}, {8, 8, 2}, {-8, 8, 3}, {8, -8, 1}, {-8, -8, 0}}) {
            BlockFace f = p[0] > 0 ? E : p[0] < 0 ? W : p[1] > 0 ? S : N;
            for (int i = -1; i <= 1; i++) {
                if (p[0] == 0) bench(b, 28 + p[0] + i, 28 + p[1], f); else bench(b, 28 + p[0], 28 + p[1] + i, f);
            }
        }
        for (int[] l : new int[][]{{14, 14}, {42, 14}, {14, 42}, {42, 42}, {28, 14}, {28, 42}, {14, 28}, {42, 28}}) lamp(b, l[0], l[1]);
        // cicek yataklari + agaclar
        int idx = 0;
        for (int[] c : new int[][]{{4, 4}, {47, 4}, {4, 47}, {47, 47}}) {
            b.fill(c[0], -1, c[1], c[0] + 5, -1, c[1] + 5, GRASS_BLOCK);
            b.ring(0, c[0], c[1], c[0] + 5, c[1] + 5, STONE_BRICK_SLAB);
            Material[] fl = {POPPY, DANDELION, ALLIUM, AZURE_BLUET, CORNFLOWER, OXEYE_DAISY};
            for (int x = c[0] + 1; x <= c[0] + 4; x++) for (int z = c[1] + 1; z <= c[1] + 4; z++) if ((x * 7 + z * 3 + idx) % 3 != 0) b.set(x, 0, z, fl[(x + z + idx) % 6]);
            tree(b, c[0] + 2, c[1] + 2, 5);
            idx++;
        }
        // saat kulesi (kuzey)
        b.box(26, -1, 3, 30, 17, 7, STONE_BRICKS);
        b.fill(28, 0, 7, 28, 1, 7, AIR); b.fill(28, 0, 6, 28, 0, 6, RED_CARPET);
        b.set(28, 4, 6, LANTERN);
        for (int y : new int[]{4, 9}) b.ring(y, 26, 3, 30, 7, DEEPSLATE_BRICKS);
        for (int[] f : new int[][]{{27, 3, 29, 3}, {27, 7, 29, 7}, {26, 4, 26, 6}, {30, 4, 30, 6}}) {
            boolean xs = f[1] == f[3];
            if (xs) b.fill(f[0], 12, f[1], f[2], 14, f[3], WHITE_CONCRETE); else b.fill(f[0], 12, f[1], f[2], 14, f[3], WHITE_CONCRETE);
        }
        b.set(28, 13, 3, BLACK_CONCRETE); b.set(28, 14, 3, BLACK_CONCRETE); b.set(29, 13, 3, BLACK_CONCRETE);
        b.set(28, 13, 7, BLACK_CONCRETE); b.set(28, 14, 7, BLACK_CONCRETE); b.set(29, 13, 7, BLACK_CONCRETE);
        for (int k = 0; k < 3; k++) b.fill(26 + k, 18 + k, 3 + k, 30 - k, 18 + k, 7 - k, DEEPSLATE_TILES);
        b.fill(28, 21, 5, 28, 24, 5, LIGHTNING_ROD);
        // bilgi panosu (guney)
        b.fill(23, 0, 47, 34, 3, 47, DARK_OAK_PLANKS); b.ring(4, 23, 47, 34, 47, STONE_BRICK_SLAB);
        String[][] sg = {{"HOŞGELDİN", "Chicago RP"}, {"TELEFON", "/telefon"}, {"PVP", "/pvp kapat"}, {"MESLEK", "/meslek"}, {"ARAÇ", "/galeri"}};
        for (int i = 0; i < 5; i++) b.wallSign(24 + i * 2 + 1, 2, 48, S, sg[i][0], sg[i][1], "", "");
        // pergolalar
        for (int[] pg : new int[][]{{5, 24}, {46, 24}}) {
            for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) b.fill(pg[0] + c[0], 0, pg[1] + c[1], pg[0] + c[0], 4, pg[1] + c[1], QUARTZ_PILLAR);
            b.fill(pg[0] - 1, 5, pg[1] - 1, pg[0] + 7, 5, pg[1] + 7, OAK_SLAB);
            b.chair(pg[0] + 2, 0, pg[1] + 3, E); b.chair(pg[0] + 4, 0, pg[1] + 3, W); b.table(pg[0] + 3, 0, pg[1] + 3);
            b.data(pg[0] + 3, 4, pg[1] + 3, "minecraft:lantern[hanging=true]");
        }
        var pl = org.bukkit.plugin.java.JavaPlugin.getPlugin(ChicagoRP.class);
        Signs.atm(pl, b, 20, 0, 44, false); Signs.atm(pl, b, 37, 0, 44, false);
        return b.loc(28, 0, 40);
    }

    // ------------------------------------------------------------ PARK (38x38)
    public static void park(Blk b) {
        final BlockFace E = BlockFace.EAST, W = BlockFace.WEST;
        b.fill(18, -1, 0, 19, -1, 37, DIRT_PATH); b.fill(0, -1, 18, 37, -1, 19, DIRT_PATH);
        b.fill(14, -1, 14, 23, -1, 23, STONE_BRICKS);
        b.fill(17, -1, 17, 20, -1, 20, LIGHT_BLUE_CONCRETE); b.fill(17, 0, 17, 20, 0, 20, WATER);
        b.ring(0, 16, 16, 21, 21, STONE_BRICK_SLAB);
        b.fill(25, -4, 5, 33, -4, 12, CLAY); b.fill(25, -3, 5, 33, -1, 12, WATER);
        b.ring(-1, 24, 4, 34, 13, STONE_BRICKS);
        for (int[] l : new int[][]{{27, 7}, {30, 10}, {29, 6}}) b.set(l[0], 0, l[1], LILY_PAD);
        for (int[] t : new int[][]{{4, 4}, {10, 12}, {5, 25}, {12, 32}, {28, 26}, {33, 31}, {24, 22}, {8, 8}, {31, 20}, {3, 14}}) tree(b, t[0], t[1], 5);
        for (int z : new int[]{5, 11, 26, 32}) { bench(b, 17, z, E); bench(b, 20, z, W); lamp(b, 16, z + 2 > 36 ? z - 2 : z + 2); }
        b.fill(6, 0, 28, 6, 3, 28, OAK_FENCE); b.fill(10, 0, 28, 10, 3, 28, OAK_FENCE); b.fill(6, 3, 28, 10, 3, 28, OAK_FENCE);
        for (int x : new int[]{7, 9}) { b.set(x, 2, 28, CHAIN); b.set(x, 1, 28, CHAIN); b.slab(x, 0, 28, OAK_SLAB, false); }
        b.fill(24, 0, 30, 29, 0, 35, SMOOTH_SANDSTONE);
    }

    public static void miniPark(Blk b) {
        b.fill(8, -1, 0, 9, -1, 17, DIRT_PATH); b.fill(0, -1, 8, 17, -1, 9, DIRT_PATH);
        for (int[] t : new int[][]{{3, 3}, {14, 3}, {3, 14}, {14, 14}}) tree(b, t[0], t[1], 5);
        bench(b, 7, 5, BlockFace.EAST); bench(b, 10, 12, BlockFace.WEST);
        lamp(b, 7, 11); lamp(b, 10, 6);
    }

    // ------------------------------------------------------------ OTOPARK (18x18)
    public static void parking(Blk b) {
        b.fill(0, -1, 0, 17, -1, 17, GRAY_CONCRETE);
        for (int x = 1; x <= 16; x += 3) { b.fill(x, -1, 1, x, -1, 5, WHITE_CONCRETE); b.fill(x, -1, 12, x, -1, 16, WHITE_CONCRETE); }
        b.fill(0, -1, 8, 17, -1, 9, BLACK_CONCRETE);
        Material[] col = {RED_CONCRETE, BLUE_CONCRETE, WHITE_CONCRETE, BLACK_CONCRETE, YELLOW_CONCRETE};
        int i = 0;
        for (int x = 2; x <= 14; x += 3) { if (x % 2 == 0) Shops.car(b, x, 0, 1, col[i++ % 5]); else Shops.car(b, x, 0, 12, col[i++ % 5]); }
        lamp(b, 0, 7); lamp(b, 17, 10);
        b.fill(4, 0, 17, 6, 1, 17, STONE_BRICKS);
        b.wallSign(5, 1, 18, BlockFace.SOUTH, "OTOPARK", "", "", "");
    }

    // ------------------------------------------------------------ LIMAN (38x38)
    public static void harbor(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH;
        b.checker(-1, 0, 0, 37, 37, STONE_BRICKS, POLISHED_ANDESITE, 3);
        b.fill(4, -4, 4, 33, -4, 26, CLAY);
        b.fill(4, -3, 4, 33, -1, 26, WATER);
        b.ring(-1, 3, 3, 34, 27, SMOOTH_STONE); b.ring(0, 3, 3, 34, 27, ANDESITE_WALL);
        for (int x = 3; x <= 34; x += 6) { b.set(x, 1, 3, ANDESITE_WALL); b.set(x, 1, 27, ANDESITE_WALL); }
        // iskele
        b.fill(16, -1, 4, 18, -1, 27, SPRUCE_PLANKS); b.fill(8, -1, 14, 26, -1, 15, SPRUCE_PLANKS);
        for (int z = 6; z <= 26; z += 4) for (int x : new int[]{16, 18}) { b.fill(x, -3, z, x, -2, z, OAK_FENCE); b.set(x, 0, z, OAK_FENCE); b.set(x, 1, z, LANTERN); }
        for (int[] bt : new int[][]{{10, 8}, {24, 20}, {21, 8}}) {
            Location l = b.loc(bt[0], -1, bt[1]);
            try { b.w.spawnEntity(l, EntityType.OAK_BOAT); } catch (Throwable t) { /* surum farki */ }
        }
        // balikci kulubesi
        b.box(2, -1, 30, 11, 4, 36, SPRUCE_PLANKS); b.fill(3, -1, 31, 10, -1, 35, OAK_PLANKS);
        b.fill(2, 4, 30, 11, 4, 36, DARK_OAK_PLANKS);
        b.fill(5, 0, 30, 5, 1, 30, AIR); b.door(5, 0, 30, SPRUCE_DOOR, N);
        b.fill(8, 1, 30, 9, 2, 30, GLASS_PANE);
        b.wallSign(5, 3, 29, N, "BALIKÇI", "Olta atmak için", "/meslek balikci", "");
        for (int x = 3; x <= 9; x += 2) b.set(x, 0, 35, BARREL);
        b.chest(3, 0, 32, E()); b.set(9, 0, 33, CRAFTING_TABLE); b.set(9, 0, 32, COMPOSTER);
        b.data(6, 3, 33, "minecraft:lantern[hanging=true]");
        for (int i = 0; i < 4; i++) { b.set(13, 0, 30 + i, HAY_BLOCK); b.set(14, 0, 30 + i, BARREL); }
        for (int i = 0; i < 5; i++) b.fill(24 + i * 2, 0, 30, 24 + i * 2, 1, 30, OAK_FENCE);
        // fener
        b.box(31, -1, 31, 35, 13, 35, WHITE_CONCRETE);
        for (int y : new int[]{3, 4, 8, 9}) b.ring(y, 31, 31, 35, 35, RED_CONCRETE);
        b.fill(33, 0, 31, 33, 1, 31, AIR); b.fill(33, 14, 31, 33, 14, 31, AIR);
        b.fill(31, 14, 31, 35, 14, 35, QUARTZ_SLAB);
        b.fill(32, 15, 32, 34, 16, 34, GLASS); b.set(33, 15, 33, SEA_LANTERN); b.set(33, 16, 33, SEA_LANTERN);
        b.fill(32, 17, 32, 34, 17, 34, DEEPSLATE_TILE_SLAB);
    }

    private static BlockFace E() { return BlockFace.EAST; }

    // ------------------------------------------------------------ TARLA (38x38)
    public static void farm(Blk b) {
        BlockData wheat = Bukkit.createBlockData("minecraft:wheat[age=7]");
        for (int[] f : new int[][]{{2, 2}, {22, 2}, {2, 22}, {22, 22}}) {
            int x0 = f[0], z0 = f[1];
            b.fill(x0, -1, z0, x0 + 13, -1, z0 + 13, FARMLAND);
            b.fill(x0 + 6, -1, z0, x0 + 6, -1, z0 + 13, WATER);
            for (int x = x0; x <= x0 + 13; x++) for (int z = z0; z <= z0 + 13; z++) if (x != x0 + 6) b.at(x, 0, z).setBlockData(wheat, false);
            b.ring(0, x0 - 1, z0 - 1, x0 + 14, z0 + 14, OAK_FENCE);
            b.fill(x0 + 5, 0, z0 - 1, x0 + 7, 0, z0 - 1, AIR);
            b.fill(x0 + 5, 0, z0 + 14, x0 + 7, 0, z0 + 14, AIR);
        }
        b.fill(16, -1, 0, 21, -1, 37, DIRT_PATH); b.fill(0, -1, 16, 37, -1, 21, DIRT_PATH);
        b.box(16, -1, 16, 21, 3, 21, RED_TERRACOTTA);
        b.gable(16, 16, 21, 21, 4, DARK_OAK_STAIRS, DARK_OAK_PLANKS, RED_TERRACOTTA);
        b.fill(18, 0, 16, 19, 1, 16, AIR); b.door(18, 0, 16, SPRUCE_DOOR, BlockFace.NORTH);
        b.wallSign(19, 3, 15, BlockFace.NORTH, "TARLA", "KİRALA", "/tarla kirala", "");
        b.fill(17, 0, 18, 20, 1, 20, HAY_BLOCK);
        b.set(20, 0, 17, BARREL);
    }
}

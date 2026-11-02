package tr.chicagorp;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import static org.bukkit.Material.*;

/** 3 katli polis karakolu. Boyut: sx=36, sz=34. Hucreler arka kanatta (ayri bina), koridorla bagli. */
public final class Karakol {
    private Karakol() {}

    public static Location build(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        // ---------- ana kabuk ----------
        b.box(0, -1, 0, 35, 14, 21, WHITE_CONCRETE);
        b.checker(-1, 1, 1, 34, 20, POLISHED_ANDESITE, SMOOTH_QUARTZ, 2);
        b.fill(1, 4, 1, 34, 4, 20, DARK_OAK_PLANKS);
        b.fill(1, 9, 1, 34, 9, 20, DARK_OAK_PLANKS);
        b.ring(0, 0, 0, 35, 21, DEEPSLATE_BRICKS);
        for (int x : new int[]{0, 7, 14, 21, 28, 35}) {
            b.fill(x, 0, 0, x, 13, 0, BLUE_CONCRETE);
            b.fill(x, 0, 21, x, 13, 21, BLUE_CONCRETE);
        }
        for (int y : new int[]{4, 9, 14}) {
            b.fill(0, y, 0, 35, y, 0, BLUE_CONCRETE); b.fill(0, y, 21, 35, y, 21, BLUE_CONCRETE);
            b.fill(0, y, 0, 0, y, 21, BLUE_CONCRETE); b.fill(35, y, 0, 35, y, 21, BLUE_CONCRETE);
        }
        // ---------- pencereler ----------
        int[][] gaps = {{2, 5}, {9, 12}, {16, 19}, {23, 26}, {30, 33}};
        for (int[] g : gaps) {
            for (int yb : new int[]{6, 11}) {
                b.fill(g[0], yb, 0, g[1], yb + 1, 0, GLASS_PANE);
                b.fill(g[0], yb, 21, g[1], yb + 1, 21, GLASS_PANE);
            }
            if (g[0] != 16) b.fill(g[0], 1, 0, g[1], 2, 0, GLASS_PANE);
            b.fill(g[0], 1, 21, g[1], 2, 21, GLASS_PANE);
        }
        for (int yb : new int[]{1, 6, 11}) for (int[] zz : new int[][]{{3, 5}, {9, 11}, {15, 17}}) {
            b.fill(0, yb, zz[0], 0, yb + 1, zz[1], GLASS_PANE);
            b.fill(35, yb, zz[0], 35, yb + 1, zz[1], GLASS_PANE);
        }
        // ---------- giris ----------
        b.fill(16, 0, 0, 19, 3, 0, AIR);
        b.fill(16, 0, 0, 16, 2, 0, GLASS_PANE); b.fill(19, 0, 0, 19, 2, 0, GLASS_PANE);
        b.fill(16, 3, 0, 19, 3, 0, GLASS_PANE); b.fill(17, 2, 0, 18, 2, 0, GLASS_PANE);
        b.door(17, 0, 0, IRON_DOOR, N); b.door(18, 0, 0, IRON_DOOR, N);
        b.fill(14, 4, -2, 21, 4, -1, BLUE_CONCRETE);
        b.fill(14, 0, -2, 14, 3, -2, QUARTZ_PILLAR); b.fill(21, 0, -2, 21, 3, -2, QUARTZ_PILLAR);
        b.fill(12, -1, -2, 23, -1, -1, STONE_BRICKS);
        b.data(16, 3, -1, "minecraft:lantern[hanging=true]"); b.data(19, 3, -1, "minecraft:lantern[hanging=true]");
        b.wallSign(17, 5, -1, N, "POLİS", "", "", "");
        b.wallSign(18, 5, -1, N, "KARAKOLU", "", "", "");
        // ---------- cati ----------
        b.ring(15, 0, 0, 35, 21, STONE_BRICK_WALL);
        b.fill(12, 14, 5, 23, 14, 16, GRAY_CONCRETE);
        b.fill(14, 14, 7, 14, 14, 14, YELLOW_CONCRETE); b.fill(21, 14, 7, 21, 14, 14, YELLOW_CONCRETE);
        b.fill(15, 14, 10, 20, 14, 11, YELLOW_CONCRETE);
        for (int[] c : new int[][]{{12, 5}, {23, 5}, {12, 16}, {23, 16}}) b.set(c[0], 14, c[1], SEA_LANTERN);
        for (int x : new int[]{2, 3, 4}) { b.set(x, 15, 2, IRON_BLOCK); b.set(x, 16, 2, IRON_BARS); }
        for (int i = 0; i < 8; i++) b.set(31, 15 + i, 18, IRON_BARS);
        b.set(31, 23, 18, SEA_LANTERN);
        // ---------- merdiven bosluklari ----------
        b.fill(10, 4, 12, 11, 4, 16, AIR); b.fill(13, 9, 12, 14, 9, 16, AIR); b.fill(16, 14, 12, 17, 14, 16, AIR);
        for (int k = 0; k < 5; k++) for (int x : new int[]{10, 11}) b.stair(x, k, 12 + k, QUARTZ_STAIRS, S, false);
        for (int k = 0; k < 5; k++) for (int x : new int[]{13, 14}) b.stair(x, 5 + k, 12 + k, QUARTZ_STAIRS, S, false);
        for (int k = 0; k < 5; k++) for (int x : new int[]{16, 17}) b.stair(x, 10 + k, 12 + k, QUARTZ_STAIRS, S, false);

        // ================= ZEMIN KAT =================
        // lobi
        b.fill(12, 0, 1, 23, 0, 7, BLUE_CARPET);
        b.fill(13, 0, 8, 21, 0, 8, QUARTZ_BLOCK);
        for (int x = 13; x <= 21; x++) b.slab(x, 1, 8, QUARTZ_SLAB, false);
        b.fill(13, 0, 10, 22, 1, 10, BOOKSHELF);
        b.chair(15, 0, 9, N); b.chair(19, 0, 9, N);
        for (int x = 11; x <= 13; x++) b.chair(x, 0, 3, S);
        for (int x = 22; x <= 24; x++) b.chair(x, 0, 3, S);
        b.table(12, 0, 5); b.table(23, 0, 5);
        for (int[] l : new int[][]{{14, 4}, {21, 4}, {15, 11}, {20, 11}}) b.data(l[0], 3, l[1], "minecraft:lantern[hanging=true]");
        // bolmeler
        b.fill(9, 0, 1, 9, 3, 20, LIGHT_GRAY_CONCRETE); b.fill(1, 0, 10, 8, 3, 10, LIGHT_GRAY_CONCRETE);
        b.fill(26, 0, 1, 26, 3, 20, LIGHT_GRAY_CONCRETE); b.fill(27, 0, 10, 34, 3, 10, LIGHT_GRAY_CONCRETE);
        b.fill(12, 0, 12, 25, 3, 12, LIGHT_GRAY_CONCRETE);
        for (int z : new int[]{5, 15}) { b.fill(9, 0, z, 9, 1, z, AIR); b.door(9, 0, z, OAK_DOOR, E); b.fill(26, 0, z, 26, 1, z, AIR); b.door(26, 0, z, OAK_DOOR, W); }
        b.fill(4, 0, 10, 4, 1, 10, AIR); b.door(4, 0, 10, OAK_DOOR, S);
        b.fill(30, 0, 10, 30, 1, 10, AIR); b.door(30, 0, 10, OAK_DOOR, S);
        b.fill(20, 0, 12, 20, 1, 12, AIR); b.door(20, 0, 12, IRON_DOOR, S);
        // ifade odasi
        b.table(4, 0, 4); b.chair(3, 0, 4, E); b.chair(5, 0, 4, W);
        b.data(4, 3, 4, "minecraft:lantern[hanging=true]");
        // arsiv
        b.fill(1, 0, 12, 1, 2, 19, BOOKSHELF); b.fill(2, 0, 20, 8, 2, 20, BOOKSHELF);
        b.table(5, 0, 15); b.set(7, 0, 12, BARREL);
        // silah deposu
        b.fill(27, 0, 4, 34, 3, 4, IRON_BARS); b.fill(30, 0, 4, 30, 1, 4, AIR); b.door(30, 0, 4, IRON_DOOR, S);
        for (int x = 28; x <= 33; x++) { b.chest(x, 0, 2, S); b.set(x, 0, 1, BARREL); }
        b.set(34, 0, 3, ANVIL);
        // soyunma
        b.fill(34, 0, 12, 34, 1, 19, BARREL);
        for (int z = 12; z <= 19; z++) b.stair(31, 0, z, OAK_STAIRS, E, false);
        // operasyon merkezi
        b.fill(12, 1, 20, 25, 2, 20, BLACK_CONCRETE);
        for (int x : new int[]{14, 18, 22}) { b.fill(x, 1, 20, x + 1, 2, 20, LIGHT_BLUE_STAINED_GLASS); b.set(x, 0, 15, OAK_SLAB); b.set(x + 1, 0, 15, OAK_SLAB); b.set(x, 1, 15, OAK_PRESSURE_PLATE); b.chair(x, 0, 17, N); }
        b.set(19, 0, 18, CARTOGRAPHY_TABLE);

        // ================= 2. KAT (amir odalari) =================
        b.fill(1, 5, 9, 34, 8, 9, LIGHT_GRAY_CONCRETE);
        b.fill(1, 5, 12, 9, 8, 12, LIGHT_GRAY_CONCRETE); b.fill(16, 5, 12, 34, 8, 12, LIGHT_GRAY_CONCRETE);
        b.fill(10, 5, 12, 11, 5, 12, OAK_FENCE); b.fill(12, 5, 12, 12, 5, 16, OAK_FENCE);
        b.fill(11, 5, 1, 11, 8, 8, LIGHT_GRAY_CONCRETE); b.fill(23, 5, 1, 23, 8, 8, LIGHT_GRAY_CONCRETE);
        b.fill(9, 5, 12, 9, 8, 20, LIGHT_GRAY_CONCRETE); b.fill(16, 5, 12, 16, 8, 20, LIGHT_GRAY_CONCRETE);
        b.fill(1, 5, 10, 34, 5, 11, RED_CARPET);
        String[] titles = {"AMİR ODASI", "BAŞKOMİSER", "KOMİSER"};
        int[][] off = {{1, 10}, {12, 22}, {24, 34}};
        for (int i = 0; i < 3; i++) {
            int x1 = off[i][0], x2 = off[i][1], cx = (x1 + x2) / 2;
            b.fill(x1, 5, 1, x2, 5, 8, RED_CARPET);
            b.fill(cx - 1, 5, 5, cx + 1, 5, 5, DARK_OAK_PLANKS);
            b.set(cx, 6, 5, OAK_PRESSURE_PLATE);
            b.chair(cx, 5, 4, S); b.chair(cx - 1, 5, 7, N); b.chair(cx + 1, 5, 7, N);
            b.fill(x1, 5, 2, x1, 6, 7, BOOKSHELF);
            b.set(x2, 5, 2, BLUE_BANNER);
            b.data(cx, 8, 4, "minecraft:lantern[hanging=true]");
            b.fill(cx, 5, 9, cx, 6, 9, AIR); b.door(cx, 5, 9, OAK_DOOR, S);
            b.wallSign(cx, 7, 10, S, titles[i], "", "", "");
        }
        // toplanti salonu
        b.fill(3, 5, 14, 6, 5, 18, DARK_OAK_PLANKS);
        for (int z = 14; z <= 18; z++) { b.chair(2, 5, z, E); b.chair(7, 5, z, W); }
        b.fill(2, 6, 20, 7, 7, 20, BLACK_CONCRETE); b.fill(3, 6, 20, 6, 7, 20, LIGHT_BLUE_STAINED_GLASS);
        b.fill(5, 5, 12, 5, 6, 12, AIR); b.door(5, 5, 12, OAK_DOOR, S);
        b.wallSign(5, 7, 11, N, "TOPLANTI", "", "", "");
        // delil odasi
        for (int x = 18; x <= 33; x += 2) { b.set(x, 5, 19, BARREL); b.chest(x, 5, 17, S); }
        b.fill(18, 5, 14, 33, 8, 14, IRON_BARS);
        b.fill(20, 5, 12, 20, 6, 12, AIR); b.door(20, 5, 12, IRON_DOOR, S);
        b.wallSign(20, 7, 11, N, "DELİL ODASI", "", "", "");
        for (int[] l : new int[][]{{5, 12}, {20, 12}, {27, 5}}) b.data(l[0], 8, l[1], "minecraft:lantern[hanging=true]");

        // ================= 3. KAT (komuta ve dinlenme) =================
        b.fill(18, 10, 1, 18, 13, 20, LIGHT_GRAY_CONCRETE);
        b.fill(18, 10, 6, 18, 11, 6, AIR); b.door(18, 10, 6, OAK_DOOR, E);
        b.fill(1, 10, 3, 1, 12, 18, BLACK_CONCRETE); b.fill(1, 11, 5, 1, 12, 8, LIGHT_BLUE_STAINED_GLASS); b.fill(1, 11, 12, 1, 12, 16, LIGHT_BLUE_STAINED_GLASS);
        b.fill(5, 10, 8, 10, 10, 12, DARK_OAK_PLANKS); b.set(7, 11, 10, CARTOGRAPHY_TABLE);
        for (int z = 8; z <= 12; z += 2) { b.chair(4, 10, z, E); b.chair(11, 10, z, W); }
        b.fill(2, 10, 20, 15, 10, 20, BLUE_CARPET);
        for (int x : new int[]{4, 8, 12}) { b.set(x, 10, 17, OAK_SLAB); b.set(x, 11, 17, OAK_PRESSURE_PLATE); b.chair(x, 10, 18, N); }
        // dinlenme / mutfak
        b.set(33, 10, 1, SMOKER); b.set(32, 10, 1, FURNACE); b.set(31, 10, 1, CRAFTING_TABLE); b.set(30, 10, 1, BARREL);
        b.table(24, 10, 8); b.table(28, 10, 8); b.chair(23, 10, 8, E); b.chair(25, 10, 8, W); b.chair(27, 10, 8, E); b.chair(29, 10, 8, W);
        for (int x = 22; x <= 26; x++) b.stair(x, 10, 17, DARK_OAK_STAIRS, N, false);
        b.fill(21, 10, 20, 31, 12, 20, BLACK_CONCRETE);
        b.fill(23, 10, 19, 24, 10, 19, RED_CARPET);
        for (int[] l : new int[][]{{8, 6}, {8, 15}, {26, 6}, {26, 14}}) b.data(l[0], 13, l[1], "minecraft:lantern[hanging=true]");

        // ================= HUCRE KANADI =================
        b.fill(15, -1, 22, 20, 5, 23, STONE_BRICKS); b.fill(16, 0, 22, 19, 4, 23, AIR);
        b.fill(17, 0, 21, 18, 2, 21, AIR); b.door(17, 0, 21, IRON_DOOR, N); b.door(18, 0, 21, IRON_DOOR, N);
        b.box(8, -1, 24, 27, 5, 33, STONE_BRICKS);
        b.fill(9, -1, 25, 26, -1, 32, POLISHED_DEEPSLATE);
        b.fill(17, 0, 24, 18, 2, 24, AIR); b.door(17, 0, 24, IRON_DOOR, N); b.door(18, 0, 24, IRON_DOOR, N);
        b.fill(15, 0, 25, 15, 3, 32, IRON_BARS); b.fill(20, 0, 25, 20, 3, 32, IRON_BARS);
        for (int x1 : new int[]{9, 21}) {
            b.fill(x1, 0, 27, x1 + 5, 3, 27, STONE_BRICKS); b.fill(x1, 0, 30, x1 + 5, 3, 30, STONE_BRICKS);
        }
        for (int z : new int[]{26, 29, 32}) {
            b.fill(15, 0, z, 15, 1, z, AIR); b.door(15, 0, z, IRON_DOOR, E);
            b.fill(20, 0, z, 20, 1, z, AIR); b.door(20, 0, z, IRON_DOOR, W);
        }
        for (int z : new int[]{25, 28, 31}) {
            b.bed(9, 0, z, S, WHITE_BED); b.set(14, 0, z, CAULDRON);
            b.bed(26, 0, z, S, WHITE_BED); b.set(21, 0, z, CAULDRON);
            b.set(8, 2, z + 1, IRON_BARS); b.set(27, 2, z + 1, IRON_BARS);
        }
        b.set(17, 0, 31, OAK_SLAB); b.chair(18, 0, 31, W);
        b.data(17, 4, 28, "minecraft:lantern[hanging=true]"); b.data(18, 4, 31, "minecraft:lantern[hanging=true]");
        b.data(12, 3, 26, "minecraft:lantern[hanging=true]"); b.data(23, 3, 26, "minecraft:lantern[hanging=true]");
        return b.loc(11, 0, 26);
    }
}

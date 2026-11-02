package tr.chicagorp;

import org.bukkit.block.BlockFace;
import static org.bukkit.Material.*;

/** Buyuk hastane: 36x16, 3 kat. Acil, radyoloji, eczane, lab, 6 hasta odasi, ameliyathane, yogun bakim, doktor ve baskhekim odalari. */
public final class Hospital {
    private Hospital() {}

    private static void door(Blk b, int x, int y, int z, org.bukkit.Material m, BlockFace f) { b.fill(x, y, z, x, y + 1, z, AIR); b.door(x, y, z, m, f); }
    private static void lantern(Blk b, int x, int y, int z) { b.data(x, y, z, "minecraft:lantern[hanging=true]"); }

    public static void build(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        b.box(0, -1, 0, 35, 14, 15, WHITE_CONCRETE);
        b.checker(-1, 1, 1, 34, 14, SMOOTH_QUARTZ, LIGHT_GRAY_CONCRETE, 2);
        b.fill(1, 4, 1, 34, 4, 14, SMOOTH_QUARTZ); b.fill(1, 9, 1, 34, 9, 14, SMOOTH_QUARTZ);
        b.ring(0, 0, 0, 35, 15, LIGHT_GRAY_CONCRETE);
        for (int x : new int[]{0, 7, 14, 21, 28, 35}) { b.fill(x, 0, 0, x, 13, 0, LIGHT_GRAY_CONCRETE); b.fill(x, 0, 15, x, 13, 15, LIGHT_GRAY_CONCRETE); }
        for (int y : new int[]{4, 9, 14}) b.ring(y, 0, 0, 35, 15, RED_CONCRETE);
        int[][] gaps = {{2, 5}, {9, 12}, {16, 19}, {23, 26}, {30, 33}};
        for (int[] g : gaps) {
            if (g[0] != 16) b.fill(g[0], 1, 0, g[1], 2, 0, GLASS_PANE);
            for (int yb : new int[]{6, 11}) if (!(g[0] == 16 && yb == 6)) b.fill(g[0], yb, 0, g[1], yb + 1, 0, GLASS_PANE);
            for (int yb : new int[]{1, 6, 11}) b.fill(g[0], yb, 15, g[1], yb + 1, 15, GLASS_PANE);
        }
        for (int yb : new int[]{1, 6, 11}) for (int[] zz : new int[][]{{3, 5}, {10, 12}}) { b.fill(0, yb, zz[0], 0, yb + 1, zz[1], GLASS_PANE); b.fill(35, yb, zz[0], 35, yb + 1, zz[1], GLASS_PANE); }
        b.fill(17, 5, 0, 18, 8, 0, RED_CONCRETE); b.fill(16, 6, 0, 19, 7, 0, RED_CONCRETE);
        // giris + ambulans saçağı
        b.fill(16, 0, 0, 19, 3, 0, AIR); b.fill(16, 0, 0, 16, 2, 0, GLASS_PANE); b.fill(19, 0, 0, 19, 2, 0, GLASS_PANE); b.fill(16, 3, 0, 19, 3, 0, GLASS_PANE);
        b.door(17, 0, 0, IRON_DOOR, N); b.door(18, 0, 0, IRON_DOOR, N);
        b.fill(14, 4, -2, 21, 4, -1, RED_CONCRETE); b.fill(14, 0, -2, 14, 3, -2, QUARTZ_PILLAR); b.fill(21, 0, -2, 21, 3, -2, QUARTZ_PILLAR);
        b.fill(12, -1, -2, 23, -1, -1, SMOOTH_QUARTZ);
        b.wallSign(17, 2, -1, N, "ACİL", "", "", ""); b.wallSign(18, 2, -1, N, "SERVİS", "", "", "");
        // cati
        b.ring(15, 0, 0, 35, 15, STONE_BRICK_WALL);
        b.fill(12, 14, 4, 23, 14, 11, GRAY_CONCRETE); b.ring(14, 12, 4, 23, 11, WHITE_CONCRETE);
        b.fill(15, 14, 5, 15, 14, 10, RED_CONCRETE); b.fill(20, 14, 5, 20, 14, 10, RED_CONCRETE); b.fill(15, 14, 7, 20, 14, 8, RED_CONCRETE);
        for (int x : new int[]{3, 4, 30, 31}) { b.set(x, 15, 12, IRON_BLOCK); b.set(x, 16, 12, IRON_BARS); }
        // merdivenler
        b.fill(33, 4, 9, 34, 4, 13, AIR); b.fill(1, 9, 9, 2, 9, 13, AIR);
        for (int k = 0; k < 5; k++) for (int x : new int[]{33, 34}) b.stair(x, k, 9 + k, QUARTZ_STAIRS, S, false);
        for (int k = 0; k < 5; k++) for (int x : new int[]{1, 2}) b.stair(x, 5 + k, 9 + k, QUARTZ_STAIRS, S, false);

        // ===================== ZEMIN: lobi, acil, radyoloji, eczane, laboratuvar =====================
        b.fill(12, 0, 1, 12, 3, 14, LIGHT_GRAY_CONCRETE); b.fill(24, 0, 1, 24, 3, 14, LIGHT_GRAY_CONCRETE);
        b.fill(1, 0, 8, 11, 3, 8, LIGHT_GRAY_CONCRETE); b.fill(25, 0, 8, 34, 3, 8, LIGHT_GRAY_CONCRETE);
        door(b, 12, 0, 4, OAK_DOOR, E); door(b, 12, 0, 11, OAK_DOOR, E); door(b, 24, 0, 4, OAK_DOOR, W); door(b, 24, 0, 11, OAK_DOOR, W);
        door(b, 6, 0, 8, OAK_DOOR, S); door(b, 29, 0, 8, OAK_DOOR, S);
        b.fill(13, 0, 1, 23, 0, 4, BLUE_CARPET);
        b.fill(14, 0, 6, 21, 0, 6, QUARTZ_BLOCK); for (int x = 14; x <= 21; x++) b.slab(x, 1, 6, QUARTZ_SLAB, false);
        for (int x : new int[]{15, 19}) { b.set(x, 2, 6, OAK_PRESSURE_PLATE); b.chair(x, 0, 8, N); }
        b.fill(13, 0, 10, 22, 1, 10, BOOKSHELF);
        for (int x = 14; x <= 22; x += 2) b.chair(x, 0, 13, N);
        for (int x : new int[]{14, 17, 20}) b.chair(x, 0, 2, S);
        b.set(13, 0, 12, POTTED_FERN); b.set(23, 0, 12, POTTED_FERN); b.set(13, 0, 2, POTTED_FERN); b.set(23, 0, 2, POTTED_FERN);
        for (int[] l : new int[][]{{15, 3}, {20, 3}, {15, 11}, {20, 11}}) lantern(b, l[0], 3, l[1]);
        // acil servis
        for (int x : new int[]{2, 5, 8}) { b.bed(x, 0, 2, S, WHITE_BED); b.bed(x, 0, 5, S, WHITE_BED); }
        for (int x : new int[]{3, 6, 9}) { b.fill(x, 0, 1, x, 2, 3, WHITE_STAINED_GLASS_PANE); b.fill(x, 0, 5, x, 2, 7, WHITE_STAINED_GLASS_PANE); }
        b.set(10, 0, 2, CAULDRON); b.set(11, 0, 2, SMITHING_TABLE); b.fill(10, 0, 6, 11, 0, 6, QUARTZ_BLOCK); b.set(10, 1, 6, OAK_PRESSURE_PLATE);
        // radyoloji
        b.fill(5, 0, 11, 7, 0, 12, QUARTZ_BLOCK); b.fill(4, 3, 11, 8, 3, 11, IRON_BLOCK); b.set(6, 2, 11, SEA_LANTERN);
        b.fill(9, 0, 12, 11, 2, 12, GLASS); b.set(10, 0, 14, OAK_SLAB); b.set(10, 1, 14, OAK_PRESSURE_PLATE); b.chair(10, 0, 13, S);
        b.fill(1, 0, 13, 4, 2, 13, BLACK_CONCRETE);
        // eczane
        b.fill(25, 0, 5, 33, 0, 5, QUARTZ_BLOCK); for (int x = 25; x <= 33; x++) b.slab(x, 1, 5, QUARTZ_SLAB, false);
        b.set(27, 2, 5, BREWING_STAND); b.set(30, 2, 5, BREWING_STAND);
        b.fill(26, 0, 7, 33, 2, 7, BOOKSHELF); b.set(34, 0, 6, BARREL);
        // laboratuvar
        b.fill(26, 0, 10, 31, 0, 10, QUARTZ_BLOCK);
        for (int x : new int[]{27, 29, 31}) b.set(x, 1, 10, BREWING_STAND);
        b.set(26, 0, 13, CAULDRON); b.set(28, 0, 13, CRAFTING_TABLE); b.set(31, 0, 13, BLAST_FURNACE);
        b.set(28, 3, 12, SEA_LANTERN); b.set(31, 3, 12, SEA_LANTERN);

        // ===================== 2. KAT: hasta odalari + ameliyathane =====================
        int[][] rooms = {{1, 6}, {8, 12}, {14, 18}, {20, 24}, {26, 30}, {32, 34}};
        b.fill(1, 5, 7, 34, 8, 7, LIGHT_GRAY_CONCRETE);
        for (int x : new int[]{7, 13, 19, 25, 31}) b.fill(x, 5, 1, x, 8, 6, LIGHT_GRAY_CONCRETE);
        b.fill(1, 5, 8, 34, 5, 9, BLUE_CARPET);
        for (int i = 0; i < rooms.length; i++) {
            int x1 = rooms[i][0], x2 = rooms[i][1], dx = (x1 + x2) / 2;
            door(b, dx, 5, 7, OAK_DOOR, S);
            b.wallSign(dx, 7, 8, S, "ODA " + (i + 1), "", "", "");
            b.bed(x1 + 1, 5, 2, S, WHITE_BED);
            if (x2 > x1 + 1) b.set(x2, 5, 2, BARREL);
            b.set(x1 + 1, 5, 6, OAK_SLAB); b.set(x1 + 1, 6, 6, OAK_PRESSURE_PLATE); b.chair(x2, 5, 5, W);
            lantern(b, x1 + 1, 8, 4);
        }
        b.fill(3, 5, 10, 30, 8, 10, LIGHT_GRAY_CONCRETE); b.fill(3, 5, 11, 3, 8, 14, LIGHT_GRAY_CONCRETE); b.fill(21, 5, 11, 21, 8, 14, LIGHT_GRAY_CONCRETE); b.fill(30, 5, 11, 30, 8, 14, LIGHT_GRAY_CONCRETE);
        door(b, 12, 5, 10, IRON_DOOR, S); door(b, 25, 5, 10, IRON_DOOR, S); door(b, 30, 5, 12, OAK_DOOR, E);
        b.fill(10, 5, 12, 13, 5, 13, QUARTZ_BLOCK); b.fill(10, 6, 12, 13, 6, 13, QUARTZ_SLAB);
        for (int[] l : new int[][]{{9, 11}, {14, 11}, {9, 14}, {14, 14}}) b.set(l[0], 8, l[1], SEA_LANTERN);
        b.set(5, 5, 11, CAULDRON); b.set(5, 5, 13, SMITHING_TABLE); b.set(19, 5, 11, CAULDRON); b.fill(18, 5, 14, 20, 5, 14, QUARTZ_BLOCK);
        b.set(22, 5, 11, BLAST_FURNACE); b.set(23, 5, 11, BLAST_FURNACE); b.fill(22, 5, 14, 28, 5, 14, QUARTZ_BLOCK); b.set(27, 5, 11, CAULDRON);
        b.fill(33, 5, 8, 34, 5, 8, OAK_FENCE); b.fill(32, 5, 9, 32, 5, 13, OAK_FENCE);

        // ===================== 3. KAT: yogun bakim, doktorlar, baskhekim =====================
        b.fill(1, 10, 7, 34, 13, 7, LIGHT_GRAY_CONCRETE); b.fill(1, 10, 8, 34, 10, 9, WHITE_CARPET);
        for (int x : new int[]{2, 6, 10, 14, 18}) {
            b.bed(x, 10, 2, S, WHITE_BED); b.fill(x + 2, 10, 1, x + 2, 12, 5, GLASS_PANE);
            b.set(x + 1, 10, 2, BARREL); b.set(x + 1, 11, 2, LIGHT_BLUE_STAINED_GLASS);
        }
        b.fill(8, 10, 6, 13, 10, 6, QUARTZ_BLOCK); b.set(10, 11, 6, OAK_PRESSURE_PLATE);
        door(b, 5, 10, 7, OAK_DOOR, S); b.wallSign(5, 12, 8, S, "YOĞUN BAKIM", "", "", "");
        for (int x : new int[]{26, 30}) b.fill(x, 10, 1, x, 13, 6, LIGHT_GRAY_CONCRETE);
        String[] drs = {"DR. ODASI 1", "DR. ODASI 2", "DR. ODASI 3"};
        int[][] dr = {{22, 25}, {27, 29}, {31, 34}};
        for (int i = 0; i < 3; i++) {
            int x1 = dr[i][0], x2 = dr[i][1], dx = (x1 + x2) / 2;
            door(b, dx, 10, 7, OAK_DOOR, S); b.wallSign(dx, 12, 8, S, drs[i], "", "", "");
            b.set(dx, 10, 3, OAK_SLAB); b.set(dx, 11, 3, OAK_PRESSURE_PLATE); b.chair(dx, 10, 2, S); b.chair(dx, 10, 5, N);
            b.fill(x1, 10, 6, x2, 11, 6, BOOKSHELF); b.fill(x1, 10, 1, x2, 10, 5, RED_CARPET);
        }
        b.fill(3, 10, 10, 34, 13, 10, LIGHT_GRAY_CONCRETE); b.fill(3, 10, 11, 3, 13, 14, LIGHT_GRAY_CONCRETE); b.fill(15, 10, 11, 15, 13, 14, LIGHT_GRAY_CONCRETE); b.fill(27, 10, 11, 27, 13, 14, LIGHT_GRAY_CONCRETE);
        door(b, 9, 10, 10, OAK_DOOR, S); door(b, 21, 10, 10, OAK_DOOR, S); door(b, 31, 10, 10, OAK_DOOR, S);
        b.wallSign(9, 12, 9, N, "BAŞHEKİM", "", "", ""); b.wallSign(21, 12, 9, N, "TOPLANTI", "", "", ""); b.wallSign(31, 12, 9, N, "KAFETERYA", "", "", "");
        b.fill(4, 10, 11, 14, 10, 14, RED_CARPET); b.fill(7, 10, 12, 11, 10, 12, DARK_OAK_PLANKS); b.chair(9, 10, 13, N); b.fill(4, 10, 14, 14, 11, 14, BOOKSHELF);
        b.fill(18, 10, 12, 24, 10, 13, DARK_OAK_PLANKS);
        for (int x = 18; x <= 24; x += 2) { b.chair(x, 10, 11, S); b.chair(x, 10, 14, N); }
        b.fill(16, 11, 14, 26, 12, 14, BLACK_CONCRETE);
        b.fill(28, 10, 14, 34, 10, 14, QUARTZ_BLOCK); b.set(29, 11, 14, SMOKER); b.set(31, 11, 14, FURNACE);
        b.table(30, 10, 12); b.table(33, 10, 12); b.chair(29, 10, 12, E); b.chair(31, 10, 12, W); b.chair(32, 10, 12, E); b.chair(34, 10, 12, W);
        b.fill(3, 10, 9, 3, 10, 13, OAK_FENCE); b.fill(1, 10, 8, 2, 10, 8, OAK_FENCE);
        for (int[] l : new int[][]{{8, 5}, {16, 5}, {26, 4}, {9, 8}}) lantern(b, l[0], 13, l[1]);
    }
}

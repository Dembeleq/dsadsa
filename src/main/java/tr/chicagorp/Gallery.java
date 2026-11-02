package tr.chicagorp;

import org.bukkit.block.BlockFace;
import static org.bukkit.Material.*;

/** Buyuk arac galerisi: 36x16, 2 kat. Cam cephe, 4 podyum, resepsiyon, VIP salon. */
public final class Gallery {
    private Gallery() {}

    public static void build(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        b.box(0, -1, 0, 35, 9, 15, WHITE_CONCRETE);
        b.checker(-1, 1, 1, 34, 14, SMOOTH_QUARTZ, POLISHED_ANDESITE, 4);
        b.fill(1, 4, 1, 34, 4, 14, DARK_OAK_PLANKS);
        for (int x : new int[]{0, 6, 12, 23, 29, 35}) { b.fill(x, 0, 0, x, 8, 0, BLACK_CONCRETE); b.fill(x, 0, 15, x, 8, 15, BLACK_CONCRETE); }
        b.ring(4, 0, 0, 35, 15, BLACK_CONCRETE); b.ring(9, 0, 0, 35, 15, BLACK_CONCRETE);
        b.fill(1, 0, 0, 34, 3, 0, GLASS); b.fill(1, 5, 0, 34, 8, 0, GLASS);
        for (int x : new int[]{0, 6, 12, 23, 29, 35}) b.fill(x, 0, 0, x, 8, 0, BLACK_CONCRETE);
        b.fill(17, 0, 0, 18, 2, 0, AIR); b.door(17, 0, 0, IRON_DOOR, N); b.door(18, 0, 0, IRON_DOOR, N);
        b.fill(14, 4, -2, 21, 4, -1, BLACK_CONCRETE); b.fill(14, 0, -2, 14, 3, -2, QUARTZ_PILLAR); b.fill(21, 0, -2, 21, 3, -2, QUARTZ_PILLAR);
        b.fill(12, -1, -2, 23, -1, -1, SMOOTH_QUARTZ);
        b.wallSign(17, 2, -1, N, "ARAÇ", "", "", ""); b.wallSign(18, 2, -1, N, "GALERİSİ", "", "", "");
        for (int[] zz : new int[][]{{3, 5}, {10, 12}}) { b.fill(0, 6, zz[0], 0, 7, zz[1], GLASS_PANE); b.fill(35, 6, zz[0], 35, 7, zz[1], GLASS_PANE); }
        for (int x = 2; x <= 33; x += 6) b.fill(x, 6, 15, x + 3, 7, 15, GLASS_PANE);
        b.fill(7, 0, 15, 11, 2, 15, IRON_BARS); b.fill(24, 0, 15, 28, 2, 15, IRON_BARS);
        b.ring(10, 0, 0, 35, 15, STONE_BRICK_WALL);
        b.fill(14, 9, 3, 21, 9, 12, GRAY_CONCRETE); b.fill(14, 9, 3, 14, 9, 12, YELLOW_CONCRETE); b.fill(21, 9, 3, 21, 9, 12, YELLOW_CONCRETE);
        // merdiven
        b.fill(1, 4, 9, 2, 4, 13, AIR);
        for (int k = 0; k < 5; k++) for (int x : new int[]{1, 2}) b.stair(x, k, 9 + k, QUARTZ_STAIRS, S, false);
        b.fill(3, 5, 9, 3, 5, 13, OAK_FENCE); b.fill(1, 5, 8, 2, 5, 8, OAK_FENCE);

        // podyumlar + sergi araclari
        int[][] pods = {{4, 2}, {4, 9}, {26, 2}, {26, 9}};
        org.bukkit.Material[] cols = {BLUE_CONCRETE, RED_CONCRETE, LIME_CONCRETE, PURPLE_CONCRETE};
        for (int i = 0; i < 4; i++) {
            int px = pods[i][0], pz = pods[i][1];
            b.fill(px, 0, pz, px + 7, 0, pz + 5, QUARTZ_BLOCK); b.ring(0, px, pz, px + 7, pz + 5, SEA_LANTERN);
            b.fill(px + 1, 0, pz + 1, px + 6, 0, pz + 4, QUARTZ_BLOCK);
            Shops.car(b, px + 3, 1, pz + 1, cols[i]);
            b.set(px, 3, pz, SEA_LANTERN); b.set(px + 7, 3, pz, SEA_LANTERN); b.set(px, 3, pz + 5, SEA_LANTERN); b.set(px + 7, 3, pz + 5, SEA_LANTERN);
            b.set(i < 2 ? px + 8 : px - 1, 0, pz + 2, LECTERN);
        }
        // resepsiyon
        b.fill(14, 0, 11, 21, 0, 11, QUARTZ_BLOCK); for (int x = 14; x <= 21; x++) b.slab(x, 1, 11, QUARTZ_SLAB, false);
        b.set(17, 2, 11, LECTERN);
        b.chair(15, 0, 13, N); b.chair(20, 0, 13, N); b.fill(13, 0, 14, 22, 2, 14, BLACK_CONCRETE); b.fill(15, 1, 14, 20, 2, 14, LIGHT_BLUE_STAINED_GLASS);
        b.fill(15, 0, 1, 20, 0, 6, RED_CARPET);
        for (int[] l : new int[][]{{16, 3}, {19, 3}, {16, 9}, {19, 9}}) b.data(l[0], 3, l[1], "minecraft:lantern[hanging=true]");
        b.set(13, 0, 2, POTTED_FERN); b.set(22, 0, 2, POTTED_FERN);
        // ust kat: VIP salon + ofisler
        b.fill(4, 5, 1, 34, 5, 7, RED_CARPET);
        for (int x : new int[]{6, 10, 14}) { b.chair(x, 5, 3, S); b.chair(x + 1, 5, 3, S); b.table(x, 5, 5); }
        for (int x : new int[]{24, 28, 32}) { b.chair(x, 5, 3, S); b.chair(x + 1, 5, 3, S); b.table(x, 5, 5); }
        b.fill(18, 5, 5, 20, 5, 5, QUARTZ_BLOCK); b.set(19, 6, 5, BREWING_STAND);
        b.fill(4, 5, 10, 34, 8, 10, LIGHT_GRAY_CONCRETE);
        b.fill(12, 5, 10, 12, 6, 10, AIR); b.door(12, 5, 10, OAK_DOOR, S); b.fill(26, 5, 10, 26, 6, 10, AIR); b.door(26, 5, 10, OAK_DOOR, S);
        b.wallSign(12, 7, 9, N, "SATIŞ MÜDÜRÜ", "", "", ""); b.wallSign(26, 7, 9, N, "EVRAK ODASI", "", "", "");
        b.fill(8, 5, 12, 11, 5, 12, DARK_OAK_PLANKS); b.chair(9, 5, 13, N); b.fill(5, 5, 14, 11, 6, 14, BOOKSHELF);
        b.fill(22, 5, 12, 30, 5, 12, DARK_OAK_PLANKS); b.fill(22, 5, 14, 30, 6, 14, BOOKSHELF); b.chair(25, 5, 13, N);
        for (int[] l : new int[][]{{8, 4}, {20, 4}, {30, 4}, {9, 12}}) b.data(l[0], 8, l[1], "minecraft:lantern[hanging=true]");
    }
}

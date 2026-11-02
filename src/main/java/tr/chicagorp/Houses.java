package tr.chicagorp;

import org.bukkit.block.BlockFace;
import static org.bukkit.Material.*;

/** 5 farkli mustakil ev modeli. On cephe lz=0. */
public final class Houses {
    private Houses() {}

    public static final String[] NAMES = {"Gecekondu", "Müstakil", "Klasik İki Katlı", "Modern Ev", "Villa"};
    public static final int[] SX = {7, 9, 11, 12, 15};
    public static final int[] SZ = {8, 10, 12, 11, 11};
    public static final int[] FRONT_YARD = {4, 4, 3, 3, 2};
    public static final int[] PRICE = {10000, 25000, 45000, 70000, 120000};
    public static final int[] DOOR_X = {3, 4, 5, 8, 6};

    public static void build(int t, Blk b) {
        switch (t) {
            case 0 -> h0(b);
            case 1 -> h1(b);
            case 2 -> h2(b);
            case 3 -> h3(b);
            default -> h4(b);
        }
    }

    static void h0(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH;
        b.box(0, -1, 0, 6, 3, 7, SPRUCE_PLANKS);
        b.fill(1, -1, 1, 5, -1, 6, COARSE_DIRT);
        for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 7}, {6, 7}}) b.fill(c[0], 0, c[1], c[0], 2, c[1], STRIPPED_SPRUCE_LOG);
        b.fill(0, 3, 0, 6, 3, 7, LIGHT_GRAY_CONCRETE);
        b.fill(0, 0, 1, 0, 1, 6, COBBLESTONE);
        b.fill(3, 0, 0, 3, 1, 0, AIR); b.door(3, 0, 0, SPRUCE_DOOR, N);
        b.set(1, 1, 0, GLASS_PANE); b.set(5, 1, 0, GLASS_PANE); b.set(0, 1, 3, GLASS_PANE); b.set(6, 1, 4, GLASS_PANE);
        b.bed(1, 0, 5, S, RED_BED); b.chest(5, 0, 6, N); b.set(5, 0, 1, FURNACE); b.set(1, 0, 1, CRAFTING_TABLE);
        b.data(3, 2, 3, "minecraft:lantern[hanging=true]");
        b.set(5, 4, 6, BARREL);
        for (int i = 0; i < 4; i++) b.set(1, 4 + i, 1, IRON_BARS);
        b.set(2, 4, 4, CAULDRON);
    }

    static void h1(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH;
        b.box(0, -1, 0, 8, 4, 9, WHITE_CONCRETE);
        b.fill(1, -1, 1, 7, -1, 8, OAK_PLANKS);
        for (int[] c : new int[][]{{0, 0}, {8, 0}, {0, 9}, {8, 9}}) b.fill(c[0], 0, c[1], c[0], 3, c[1], STRIPPED_OAK_LOG);
        b.ring(0, 0, 0, 8, 9, STONE_BRICKS);
        b.gable(0, 0, 8, 9, 5, DEEPSLATE_BRICK_STAIRS, DEEPSLATE_BRICKS, WHITE_CONCRETE);
        b.fill(4, 0, 0, 4, 1, 0, AIR); b.door(4, 0, 0, OAK_DOOR, N);
        b.fill(1, 1, 0, 2, 2, 0, GLASS_PANE); b.fill(6, 1, 0, 7, 2, 0, GLASS_PANE);
        b.fill(0, 1, 3, 0, 2, 4, GLASS_PANE); b.fill(8, 1, 3, 8, 2, 4, GLASS_PANE);
        b.fill(2, 1, 9, 3, 2, 9, GLASS_PANE); b.fill(5, 1, 9, 6, 2, 9, GLASS_PANE);
        b.fill(3, 3, -1, 5, 3, -1, OAK_PLANKS);
        for (int x : new int[]{3, 5}) b.fill(x, 0, -1, x, 2, -1, OAK_FENCE);
        b.fill(3, -1, -1, 5, -1, -1, STONE_BRICKS);
        b.fill(1, 0, 5, 7, 3, 5, WHITE_CONCRETE);
        b.fill(4, 0, 5, 4, 1, 5, AIR); b.door(4, 0, 5, OAK_DOOR, S);
        b.fill(2, 0, 2, 6, 0, 4, RED_CARPET);
        b.chair(2, 0, 4, N); b.chair(3, 0, 4, N); b.table(5, 0, 3);
        b.fill(2, 1, 1, 3, 2, 1, BLACK_CONCRETE);
        b.set(7, 0, 1, SMOKER); b.set(7, 0, 2, FURNACE); b.set(7, 0, 3, CRAFTING_TABLE); b.set(7, 0, 4, BARREL);
        b.bed(1, 0, 7, S, BLUE_BED); b.bed(7, 0, 7, S, BLUE_BED);
        b.fill(3, 0, 8, 5, 1, 8, BOOKSHELF); b.set(4, 0, 6, OAK_PRESSURE_PLATE);
        b.data(4, 3, 3, "minecraft:lantern[hanging=true]"); b.data(4, 3, 7, "minecraft:lantern[hanging=true]");
        b.fill(6, 5, 6, 6, 9, 6, BRICKS); b.set(6, 10, 6, CAMPFIRE);
    }

    static void h2(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        b.box(0, -1, 0, 10, 9, 11, BRICKS);
        b.fill(1, -1, 1, 9, -1, 10, SPRUCE_PLANKS); b.fill(1, 4, 1, 9, 4, 10, SPRUCE_PLANKS);
        b.ring(4, 0, 0, 10, 11, QUARTZ_BLOCK); b.ring(0, 0, 0, 10, 11, STONE_BRICKS);
        for (int[] c : new int[][]{{0, 0}, {10, 0}, {0, 11}, {10, 11}}) b.fill(c[0], 1, c[1], c[0], 9, c[1], QUARTZ_PILLAR);
        b.gable(0, 0, 10, 11, 10, STONE_BRICK_STAIRS, STONE_BRICKS, BRICKS);
        b.fill(5, 0, 0, 5, 1, 0, AIR); b.door(5, 0, 0, DARK_OAK_DOOR, N);
        for (int yb : new int[]{1, 6}) {
            b.fill(2, yb, 0, 3, yb + 1, 0, GLASS_PANE); b.fill(7, yb, 0, 8, yb + 1, 0, GLASS_PANE);
            b.fill(0, yb, 3, 0, yb + 1, 4, GLASS_PANE); b.fill(0, yb, 7, 0, yb + 1, 8, GLASS_PANE);
            b.fill(10, yb, 3, 10, yb + 1, 4, GLASS_PANE); b.fill(10, yb, 7, 10, yb + 1, 8, GLASS_PANE);
            b.fill(2, yb, 11, 3, yb + 1, 11, GLASS_PANE); b.fill(7, yb, 11, 8, yb + 1, 11, GLASS_PANE);
        }
        b.fill(5, 6, 0, 5, 7, 0, GLASS_PANE);
        b.fill(1, 4, -2, 9, 4, -1, STONE_BRICK_SLAB); b.fill(1, -1, -2, 9, -1, -1, STONE_BRICKS);
        for (int x : new int[]{1, 9}) b.fill(x, 0, -2, x, 3, -2, QUARTZ_PILLAR);
        b.fill(9, 4, 6, 9, 4, 10, AIR);
        for (int k = 0; k < 5; k++) b.stair(9, k, 10 - k, OAK_STAIRS, N, false);
        b.fill(8, 5, 6, 8, 5, 10, OAK_FENCE);
        b.fill(2, 0, 2, 7, 0, 6, RED_CARPET);
        for (int x = 3; x <= 5; x++) b.chair(x, 0, 4, N);
        b.fill(4, 0, 1, 6, 2, 1, BRICKS); b.set(5, 0, 1, CAMPFIRE);
        b.table(3, 0, 8); b.chair(2, 0, 8, E); b.chair(4, 0, 8, W); b.chair(3, 0, 9, N);
        b.fill(1, 0, 7, 1, 0, 9, QUARTZ_BLOCK);
        b.set(1, 0, 10, SMOKER); b.set(2, 0, 10, FURNACE); b.set(3, 0, 10, CRAFTING_TABLE);
        b.fill(1, 5, 6, 8, 8, 6, WHITE_CONCRETE); b.fill(5, 5, 6, 5, 6, 6, AIR); b.door(5, 5, 6, OAK_DOOR, S);
        b.bed(2, 5, 2, S, RED_BED); b.bed(8, 5, 2, S, RED_BED);
        b.set(5, 5, 2, BARREL); b.fill(1, 5, 8, 4, 6, 8, BOOKSHELF); b.set(8, 5, 8, CAULDRON); b.set(6, 5, 9, CHEST);
        b.fill(2, 5, 3, 7, 5, 5, GRAY_CARPET);
        for (int[] l : new int[][]{{4, 3}, {6, 5}, {5, 8}}) b.data(l[0], 3, l[1], "minecraft:lantern[hanging=true]");
        b.data(4, 8, 4, "minecraft:lantern[hanging=true]"); b.data(7, 8, 9, "minecraft:lantern[hanging=true]");
        b.fill(1, 10, 5, 1, 14, 5, BRICKS);
    }

    static void h3(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        b.box(0, -1, 0, 11, 4, 10, WHITE_CONCRETE);
        b.fill(1, -1, 1, 10, -1, 9, SMOOTH_QUARTZ);
        b.box(2, 4, 1, 11, 8, 9, GRAY_CONCRETE);
        b.fill(3, 4, 2, 10, 4, 8, OAK_PLANKS);
        b.fill(10, 4, 3, 10, 4, 7, AIR);
        b.fill(1, 0, 0, 6, 3, 0, GLASS);
        b.fill(8, 0, 0, 8, 1, 0, AIR); b.door(8, 0, 0, ACACIA_DOOR, N);
        b.fill(9, 0, 0, 11, 3, 0, OAK_PLANKS);
        b.fill(3, 5, 1, 10, 7, 1, GLASS);
        b.fill(0, 5, 0, 11, 5, 0, GLASS_PANE); b.fill(0, 5, 0, 0, 5, 10, GLASS_PANE);
        b.fill(0, 1, 3, 0, 2, 5, GLASS); b.fill(11, 1, 3, 11, 2, 5, GLASS);
        b.fill(3, 1, 10, 8, 2, 10, GLASS); b.fill(11, 6, 3, 11, 7, 6, GLASS); b.fill(3, 6, 9, 9, 7, 9, GLASS);
        b.ring(9, 2, 1, 11, 9, WHITE_CONCRETE);
        b.fill(3, 9, 2, 5, 9, 4, GRAY_CONCRETE); b.set(4, 10, 3, IRON_BLOCK);
        b.fill(4, 0, 3, 7, 0, 3, QUARTZ_BLOCK);
        for (int x = 4; x <= 7; x++) { b.slab(x, 1, 3, QUARTZ_SLAB, false); b.chair(x, 0, 2, S); }
        b.set(2, 0, 6, GRAY_CARPET);
        b.fill(2, 0, 5, 4, 0, 7, GRAY_CARPET);
        for (int z = 5; z <= 7; z++) b.chair(5, 0, z, W);
        b.fill(1, 1, 5, 1, 2, 7, BLACK_CONCRETE);
        b.table(8, 0, 7); b.chair(7, 0, 7, E); b.chair(9, 0, 7, W);
        b.set(7, 0, 9, SMOKER); b.set(8, 0, 9, FURNACE); b.set(9, 0, 9, CRAFTING_TABLE);
        for (int k = 0; k < 5; k++) b.stair(10, k, 3 + k, QUARTZ_STAIRS, S, false);
        b.fill(9, 5, 3, 9, 5, 7, OAK_FENCE); b.fill(10, 5, 2, 10, 5, 2, OAK_FENCE);
        b.bed(4, 5, 3, S, WHITE_BED); b.fill(3, 5, 8, 6, 6, 8, BOOKSHELF);
        b.set(8, 5, 8, OAK_SLAB); b.set(8, 6, 8, OAK_PRESSURE_PLATE); b.chair(8, 5, 7, S);
        for (int[] l : new int[][]{{5, 3}, {9, 5}, {5, 8}}) b.data(l[0], 3, l[1], "minecraft:lantern[hanging=true]");
        b.data(6, 7, 5, "minecraft:lantern[hanging=true]");
        b.fill(8, -1, -3, 10, -1, -1, GRAY_CONCRETE);
    }

    static void h4(Blk b) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        b.box(0, -1, 0, 14, 5, 10, WHITE_CONCRETE);
        b.checker(-1, 1, 1, 13, 9, QUARTZ_BLOCK, POLISHED_DIORITE, 2);
        b.ring(0, 0, 0, 14, 10, STONE_BRICKS);
        b.box(1, 5, 1, 13, 9, 9, WHITE_CONCRETE);
        b.fill(2, 5, 2, 12, 5, 8, OAK_PLANKS);
        b.fill(12, 5, 2, 12, 5, 7, AIR);
        for (int k = 0; k < 6; k++) b.stair(12, k, 2 + k, QUARTZ_STAIRS, S, false);
        b.fill(11, 6, 2, 11, 6, 7, OAK_FENCE);
        b.gable(1, 1, 13, 9, 10, DEEPSLATE_TILE_STAIRS, DEEPSLATE_TILES, WHITE_CONCRETE);
        for (int x : new int[]{2, 5, 9, 12}) b.fill(x, 0, -2, x, 4, -2, QUARTZ_PILLAR);
        b.fill(2, 5, -2, 12, 5, -1, QUARTZ_SLAB); b.fill(2, -1, -2, 12, -1, -1, QUARTZ_BLOCK);
        b.fill(6, 0, 0, 7, 1, 0, AIR); b.door(6, 0, 0, DARK_OAK_DOOR, N); b.door(7, 0, 0, DARK_OAK_DOOR, N);
        b.fill(6, 2, 0, 7, 4, 0, GLASS_PANE);
        b.fill(2, 1, 0, 3, 3, 0, GLASS_PANE); b.fill(10, 1, 0, 11, 3, 0, GLASS_PANE);
        for (int[] zz : new int[][]{{2, 3}, {7, 8}}) { b.fill(0, 1, zz[0], 0, 3, zz[1], GLASS_PANE); b.fill(14, 1, zz[0], 14, 3, zz[1], GLASS_PANE); }
        for (int[] xx : new int[][]{{2, 4}, {6, 8}, {10, 12}}) b.fill(xx[0], 1, 10, xx[1], 3, 10, GLASS_PANE);
        b.fill(3, 6, 1, 5, 7, 1, GLASS_PANE); b.fill(9, 6, 1, 11, 7, 1, GLASS_PANE);
        b.fill(3, 6, 9, 5, 7, 9, GLASS_PANE); b.fill(9, 6, 9, 11, 7, 9, GLASS_PANE);
        b.ring(6, 0, 0, 14, 10, GLASS_PANE);
        b.fill(2, 0, 2, 11, 0, 8, RED_CARPET);
        for (int x = 3; x <= 5; x++) b.chair(x, 0, 4, N);
        for (int x = 8; x <= 10; x++) b.chair(x, 0, 4, N);
        b.fill(6, 0, 1, 7, 2, 1, BLACK_CONCRETE);
        b.table(7, 0, 6); b.chair(6, 0, 6, E); b.chair(8, 0, 6, W); b.chair(7, 0, 7, N);
        b.fill(10, 0, 8, 13, 0, 8, QUARTZ_BLOCK);
        b.set(13, 0, 9, SMOKER); b.set(12, 0, 9, FURNACE); b.set(11, 0, 9, CRAFTING_TABLE);
        b.fill(1, 0, 9, 4, 1, 9, BOOKSHELF);
        for (int[] l : new int[][]{{4, 4}, {10, 4}, {7, 7}, {4, 8}}) b.data(l[0], 4, l[1], "minecraft:lantern[hanging=true]");
        b.bed(6, 6, 3, S, RED_BED); b.bed(8, 6, 3, S, RED_BED);
        b.fill(3, 6, 8, 7, 7, 8, BOOKSHELF); b.set(10, 6, 8, CAULDRON); b.set(9, 6, 7, CHEST);
        b.data(7, 8, 6, "minecraft:lantern[hanging=true]");
        // havuz
        b.fill(3, -4, 12, 11, -4, 14, LIGHT_BLUE_CONCRETE);
        b.fill(3, -3, 12, 11, -1, 14, WATER);
        b.ring(-1, 2, 11, 12, 15, QUARTZ_BLOCK);
    }
}

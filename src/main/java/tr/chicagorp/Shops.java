package tr.chicagorp;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import static org.bukkit.Material.*;

/** 16x16 tabanli, cok katli, temali dukkanlar. On cephe lz=0. */
public final class Shops {
    private Shops() {}

    public record Shop(String l1, String l2, Material wall, Material trim, Material awA, Material awB, Material floor, int floors, String kind) {}

    public static Shop get(String id) {
        return switch (id) {
            case "durumcu" -> new Shop("DÜRÜMCÜ", "Lezzet Durağı", SMOOTH_SANDSTONE, RED_CONCRETE, RED_WOOL, WHITE_WOOL, TERRACOTTA, 2, "durum");
            case "market" -> new Shop("MARKET", "7/24 Açık", WHITE_CONCRETE, GREEN_CONCRETE, GREEN_WOOL, WHITE_WOOL, LIGHT_GRAY_CONCRETE, 2, "market");
            case "giyim" -> new Shop("GİYİM", "Moda Mağazası", WHITE_TERRACOTTA, BLACK_CONCRETE, BLACK_WOOL, WHITE_WOOL, BIRCH_PLANKS, 3, "giyim");
            case "kahve" -> new Shop("KAHVEHANE", "Okey • Çay", BRICKS, DARK_OAK_PLANKS, BROWN_WOOL, ORANGE_WOOL, DARK_OAK_PLANKS, 2, "kahve");
            case "silah" -> new Shop("SİLAHÇI", "Ruhsatlı Satış", GRAY_CONCRETE, BLACK_CONCRETE, BLACK_WOOL, GRAY_WOOL, POLISHED_ANDESITE, 2, "silah");
            case "demirci" -> new Shop("DEMİRCİ", "Zırh • Silah", STONE_BRICKS, DEEPSLATE_BRICKS, ORANGE_WOOL, BLACK_WOOL, COBBLED_DEEPSLATE, 2, "demirci");
            case "kargo" -> new Shop("KARGO", "Hızlı Teslimat", LIGHT_GRAY_CONCRETE, ORANGE_CONCRETE, ORANGE_WOOL, WHITE_WOOL, GRAY_CONCRETE, 2, "kargo");
            case "mekanik" -> new Shop("MEKANİK", "Oto Servis", WHITE_CONCRETE, BLUE_CONCRETE, BLUE_WOOL, WHITE_WOOL, GRAY_CONCRETE, 2, "mekanik");
            case "galeri" -> new Shop("GALERİ", "Araç Satış", WHITE_CONCRETE, BLACK_CONCRETE, BLACK_WOOL, WHITE_WOOL, QUARTZ_BLOCK, 2, "galeri");
            case "jenerator" -> new Shop("JENERATÖR", "Market", WAXED_CUT_COPPER, BLACK_CONCRETE, YELLOW_WOOL, BLACK_WOOL, GRAY_CONCRETE, 2, "jenerator");
            case "pavyon" -> new Shop("PAVYON", "VIP Gece", BLACK_CONCRETE, PURPLE_CONCRETE, MAGENTA_WOOL, BLACK_WOOL, BLACK_CONCRETE, 2, "pavyon");
            case "belediye" -> new Shop("BELEDİYE", "Arsa Ofisi", SMOOTH_QUARTZ, BLUE_CONCRETE, BLUE_WOOL, WHITE_WOOL, POLISHED_ANDESITE, 3, "belediye");
            case "hastane" -> new Shop("HASTANE", "Acil Servis", WHITE_CONCRETE, RED_CONCRETE, RED_WOOL, WHITE_WOOL, WHITE_CONCRETE, 3, "hastane");
            case "banka" -> new Shop("BANKA", "Güvenli Kasa", SMOOTH_STONE, GOLD_BLOCK, YELLOW_WOOL, WHITE_WOOL, QUARTZ_BLOCK, 2, "banka");
            default -> null;
        };
    }

    public static void build(Blk b, Shop s) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        int fl = s.floors(), roof = fl * 5 - 1;
        b.box(0, -1, 0, 15, roof, 15, s.wall());
        b.fill(1, -1, 1, 14, -1, 14, s.floor());
        for (int f = 1; f < fl; f++) b.fill(1, 5 * f - 1, 1, 14, 5 * f - 1, 14, BIRCH_PLANKS);
        for (int[] c : new int[][]{{0, 0}, {15, 0}, {0, 15}, {15, 15}}) b.fill(c[0], 0, c[1], c[0], roof, c[1], s.trim());
        b.fill(0, 3, 0, 15, 3, 0, s.trim());
        for (int f = 1; f < fl; f++) b.ring(5 * f - 1, 0, 0, 15, 15, s.trim());
        b.ring(roof, 0, 0, 15, 15, s.trim());
        b.ring(roof + 1, 0, 0, 15, 15, s.wall());
        // vitrin
        b.fill(1, 0, 0, 14, 2, 0, s.wall());
        b.fill(1, 1, 0, 14, 2, 0, GLASS);
        b.fill(4, 0, 0, 4, 2, 0, s.trim()); b.fill(11, 0, 0, 11, 2, 0, s.trim());
        b.fill(7, 0, 0, 8, 2, 0, AIR);
        b.door(7, 0, 0, OAK_DOOR, N); b.door(8, 0, 0, OAK_DOOR, N);
        b.fill(7, 2, 0, 8, 2, 0, GLASS);
        b.wallSign(7, 3, -1, N, s.l1(), "", "", "");
        b.wallSign(8, 3, -1, N, s.l2(), "", "", "");
        for (int x = 1; x <= 14; x++) { b.set(x, 4, -1, x % 2 == 0 ? s.awA() : s.awB()); b.set(x, 4, -2, x % 2 == 0 ? s.awA() : s.awB()); }
        for (int x : new int[]{2, 5, 10, 13}) b.data(x, 3, -2, "minecraft:lantern[hanging=true]");
        // yan pencereler, arka kapi
        for (int[] zz : new int[][]{{3, 5}, {10, 12}}) {
            b.fill(0, 1, zz[0], 0, 2, zz[1], GLASS_PANE); b.fill(15, 1, zz[0], 15, 2, zz[1], GLASS_PANE);
            for (int f = 1; f < fl; f++) { b.fill(0, 5 * f + 1, zz[0], 0, 5 * f + 2, zz[1], GLASS_PANE); b.fill(15, 5 * f + 1, zz[0], 15, 5 * f + 2, zz[1], GLASS_PANE); }
        }
        for (int f = 1; f < fl; f++) for (int[] xx : new int[][]{{2, 5}, {7, 8}, {10, 13}}) {
            b.fill(xx[0], 5 * f + 1, 0, xx[1], 5 * f + 2, 0, GLASS_PANE);
            b.fill(xx[0], 5 * f + 1, 15, xx[1], 5 * f + 2, 15, GLASS_PANE);
        }
        b.fill(7, 0, 15, 7, 1, 15, AIR); b.door(7, 0, 15, IRON_DOOR, S);
        // merdivenler
        for (int f = 0; f < fl - 1; f++) {
            b.fill(1, 5 * f + 4, 9, 2, 5 * f + 4, 13, AIR);
            for (int k = 0; k < 5; k++) for (int x = 1; x <= 2; x++) b.stair(x, 5 * f + k, 9 + k, OAK_STAIRS, S, false);
            b.fill(3, 5 * f + 5, 9, 3, 5 * f + 5, 13, OAK_FENCE); b.fill(1, 5 * f + 5, 8, 2, 5 * f + 5, 8, OAK_FENCE);
        }
        // cati detaylari
        b.set(3, roof + 2, 3, IRON_BLOCK); b.set(4, roof + 2, 3, IRON_BLOCK); b.set(3, roof + 3, 3, IRON_BARS);
        b.set(12, roof + 2, 12, CAULDRON); for (int i = 0; i < 4; i++) b.set(11, roof + 2 + i, 4, IRON_BARS);
        // ic mekan
        interior(b, s);
        for (int f = 1; f < fl; f++) office(b, 5 * f);
        b.data(5, 3, 7, "minecraft:lantern[hanging=true]"); b.data(10, 3, 7, "minecraft:lantern[hanging=true]");
    }

    static void office(Blk b, int y) {
        b.fill(2, y, 2, 13, y, 3, GRAY_CARPET);
        b.fill(5, y, 14, 13, y + 1, 14, BOOKSHELF);
        for (int x : new int[]{5, 9, 12}) { b.set(x, y, 8, OAK_SLAB); b.set(x + 0, y + 1, 8, OAK_PRESSURE_PLATE); b.chair(x, y, 10, BlockFace.NORTH); }
        b.data(5, y + 3, 6, "minecraft:lantern[hanging=true]"); b.data(11, y + 3, 6, "minecraft:lantern[hanging=true]");
    }

    static void car(Blk b, int x, int y, int z, Material body) {
        b.fill(x, y, z, x + 1, y, z + 3, body);
        b.fill(x, y + 1, z + 1, x + 1, y + 2, z + 2, BLACK_STAINED_GLASS);
        b.fill(x, y + 2, z + 1, x + 1, y + 2, z + 2, body);
        for (int[] w : new int[][]{{x, z}, {x + 1, z}, {x, z + 3}, {x + 1, z + 3}}) b.set(w[0], y, w[1], BLACK_CONCRETE);
        b.set(x, y + 1, z, body); b.set(x + 1, y + 1, z, body);
    }

    static void tag(Blk b, int x, int y, int z, String t) {
        Block bl = b.at(x, y, z);
        bl.setType(LECTERN, false);
        if (bl.getState() instanceof TileState ts) { ts.getPersistentDataContainer().set(U.SHOP, PersistentDataType.STRING, t); ts.update(); }
    }

    static void mannequin(Blk b, int x, int y, int z, Color c) {
        Location l = b.loc(x, y, z);
        b.w.spawn(l, ArmorStand.class, a -> {
            a.setGravity(false); a.setArms(true); a.setBasePlate(false);
            ItemStack ch = new ItemStack(LEATHER_CHESTPLATE), lg = new ItemStack(LEATHER_LEGGINGS);
            LeatherArmorMeta m1 = (LeatherArmorMeta) ch.getItemMeta(); m1.setColor(c); ch.setItemMeta(m1);
            LeatherArmorMeta m2 = (LeatherArmorMeta) lg.getItemMeta(); m2.setColor(c); lg.setItemMeta(m2);
            a.getEquipment().setChestplate(ch); a.getEquipment().setLeggings(lg);
        });
    }

    static void interior(Blk b, Shop s) {
        final BlockFace N = BlockFace.NORTH, S = BlockFace.SOUTH, E = BlockFace.EAST, W = BlockFace.WEST;
        switch (s.kind()) {
            case "durum" -> {
                b.fill(2, 0, 5, 12, 0, 5, QUARTZ_BLOCK);
                for (int x = 2; x <= 12; x++) b.slab(x, 1, 5, QUARTZ_SLAB, false);
                for (int x = 3; x <= 12; x += 3) { b.set(x, 0, 8, SMOKER); b.set(x + 1 > 12 ? 12 : x + 1, 0, 8, FURNACE); }
                b.fill(3, 0, 7, 12, 0, 7, SMOOTH_STONE);
                b.fill(3, 3, 8, 12, 3, 8, IRON_BLOCK);
                b.fill(5, 0, 14, 12, 0, 14, BARREL); b.chest(4, 0, 14, S);
                for (int[] t : new int[][]{{3, 3}, {12, 3}, {3, 11}, {12, 11}}) {
                    b.table(t[0], 0, t[1]);
                    b.chair(t[0] - 1, 0, t[1], E); b.chair(t[0] + 1, 0, t[1], W);
                }
                b.fill(2, 1, 14, 3, 2, 14, RED_TERRACOTTA);
            }
            case "market" -> {
                for (int z : new int[]{7, 10}) { b.fill(4, 0, z, 12, 1, z, BOOKSHELF); b.set(3, 0, z, BARREL); b.set(13, 0, z, BARREL); }
                b.fill(4, 0, 13, 13, 2, 13, GLASS); b.fill(4, 0, 14, 13, 2, 14, IRON_BLOCK);
                b.fill(10, 0, 3, 13, 0, 3, QUARTZ_BLOCK); for (int x = 10; x <= 13; x++) b.slab(x, 1, 3, QUARTZ_SLAB, false);
                b.set(11, 2, 3, OAK_PRESSURE_PLATE); b.chair(11, 0, 2, S);
                b.set(4, 0, 2, COMPOSTER); b.set(5, 0, 2, COMPOSTER);
            }
            case "giyim" -> {
                mannequin(b, 4, 0, 3, Color.RED); mannequin(b, 11, 0, 3, Color.BLUE);
                mannequin(b, 4, 0, 8, Color.BLACK); mannequin(b, 11, 0, 8, Color.WHITE);
                b.fill(4, 0, 2, 4, 0, 2, WHITE_CARPET); b.fill(3, -1, 7, 5, -1, 9, WHITE_CONCRETE); b.fill(10, -1, 7, 12, -1, 9, WHITE_CONCRETE);
                b.set(7, 0, 6, LOOM); b.set(8, 0, 6, LOOM);
                for (int x = 10; x <= 13; x += 3) { b.fill(x, 0, 11, x + 2, 2, 11, PINK_STAINED_GLASS_PANE); b.fill(x, 0, 12, x + 2, 0, 14, WHITE_CARPET); }
                b.fill(2, 0, 14, 8, 2, 14, GLASS);
                b.fill(2, 0, 12, 8, 0, 12, OAK_SLAB);
            }
            case "kahve" -> {
                b.fill(2, 0, 2, 13, 0, 9, RED_CARPET);
                for (int[] t : new int[][]{{3, 3}, {12, 3}, {3, 7}, {12, 7}, {6, 5}, {10, 5}}) {
                    b.table(t[0], 0, t[1]);
                    b.chair(t[0] - 1, 0, t[1], E); b.chair(t[0] + 1, 0, t[1], W);
                    b.chair(t[0], 0, t[1] - 1, S); b.chair(t[0], 0, t[1] + 1, N);
                }
                b.fill(4, 0, 11, 13, 0, 11, DARK_OAK_PLANKS);
                b.set(5, 1, 11, BREWING_STAND); b.set(7, 1, 11, BREWING_STAND); b.set(9, 1, 11, CAULDRON);
                b.fill(3, 0, 14, 13, 2, 14, BOOKSHELF);
            }
            case "silah" -> {
                for (int z : new int[]{3, 6}) { b.fill(3, 0, z, 12, 0, z, GRAY_CONCRETE); b.fill(3, 1, z, 12, 1, z, GLASS); }
                b.fill(2, 0, 9, 13, 3, 9, IRON_BARS); b.fill(4, 0, 9, 4, 1, 9, AIR); b.door(4, 0, 9, IRON_DOOR, S);
                for (int x = 3; x <= 12; x += 3) b.chest(x, 0, 12, N);
                b.set(13, 0, 12, ANVIL); b.fill(2, 0, 14, 13, 1, 14, GRAY_CONCRETE);
            }
            case "demirci" -> {
                b.fill(2, 0, 4, 13, 0, 4, DARK_OAK_PLANKS);
                for (int x : new int[]{4, 7, 10}) b.set(x, 0, 11, BLAST_FURNACE);
                b.set(13, 0, 11, LAVA_CAULDRON); b.set(4, 0, 7, ANVIL); b.set(10, 0, 7, ANVIL);
                b.set(13, 0, 7, SMITHING_TABLE); b.set(6, 0, 13, GRINDSTONE); b.set(8, 0, 13, GRINDSTONE);
                for (int x = 10; x <= 13; x++) b.set(x, 0, 14, COAL_BLOCK);
                b.fill(2, 1, 14, 8, 2, 14, IRON_BARS);
                for (int x : new int[]{4, 7, 10}) { b.fill(x, 4, 11, x, roofHeight(s) - 1, 11, STONE_BRICKS); }
            }
            case "kargo" -> {
                b.set(7, 0, 4, LECTERN); b.fill(2, 0, 4, 5, 0, 4, QUARTZ_BLOCK); b.fill(10, 0, 4, 13, 0, 4, QUARTZ_BLOCK);
                for (int x : new int[]{4, 6, 10, 12}) for (int z : new int[]{9, 12}) { b.set(x, 0, z, CHEST); b.set(x, 1, z, BARREL); b.set(x + 1, 0, z, HAY_BLOCK); }
                b.fill(3, 0, 7, 12, 0, 7, BLACK_CONCRETE);
                b.fill(2, 0, 14, 13, 2, 14, SCAFFOLDING);
            }
            case "mekanik" -> {
                car(b, 4, 0, 6, Material.RED_CONCRETE); car(b, 10, 0, 6, Material.YELLOW_CONCRETE);
                for (int x : new int[]{3, 6, 9, 12}) { b.set(x, 0, 5, IRON_BLOCK); b.set(x, 0, 11, IRON_BLOCK); }
                b.fill(2, 1, 14, 13, 2, 14, IRON_BARS);
                b.set(13, 0, 13, SMITHING_TABLE); b.set(2, 0, 13, ANVIL);
                for (int i = 0; i < 3; i++) { b.set(13, i, 2, COAL_BLOCK); }
            }
            case "galeri" -> {
                b.fill(3, 0, 6, 6, 0, 11, QUARTZ_BLOCK); b.fill(9, 0, 6, 12, 0, 11, QUARTZ_BLOCK);
                car(b, 4, 1, 7, Material.BLUE_CONCRETE); car(b, 10, 1, 7, Material.RED_CONCRETE);
                for (int[] l : new int[][]{{3, 6}, {6, 11}, {9, 6}, {12, 11}}) b.set(l[0], 1, l[1], SEA_LANTERN);
                b.fill(5, 0, 3, 10, 0, 3, QUARTZ_BLOCK); tag(b, 7, 1, 3, "galeri"); b.chair(7, 0, 2, S);
            }
            case "jenerator" -> {
                for (int x : new int[]{3, 6, 9, 12}) for (int z : new int[]{8, 11}) {
                    b.set(x, 0, z, WAXED_COPPER_BLOCK); b.set(x, 1, z, WAXED_COPPER_BLOCK); b.set(x, 2, z, LIGHTNING_ROD);
                    b.set(x, 1, z - 1, REDSTONE_LAMP);
                }
                b.fill(5, 0, 3, 10, 0, 3, QUARTZ_BLOCK); tag(b, 7, 1, 3, "jenerator"); b.chair(7, 0, 2, S);
                b.fill(2, 0, 14, 13, 1, 14, CHAIN);
            }
            case "pavyon" -> {
                b.checker(-1, 3, 2, 12, 9, MAGENTA_CONCRETE, PURPLE_CONCRETE, 1);
                for (int[] l : new int[][]{{5, 5}, {10, 5}, {7, 8}, {12, 8}}) b.set(l[0], 3, l[1], SEA_LANTERN);
                b.fill(3, 0, 12, 12, 0, 14, BLACK_CONCRETE); b.fill(4, 1, 13, 11, 1, 13, RED_CARPET);
                b.set(3, 1, 12, NOTE_BLOCK); b.set(12, 1, 12, NOTE_BLOCK); b.set(3, 2, 12, NOTE_BLOCK); b.set(12, 2, 12, NOTE_BLOCK);
                b.fill(2, 0, 3, 2, 0, 8, BLACK_CONCRETE); b.set(2, 1, 4, BREWING_STAND); b.set(2, 1, 6, BREWING_STAND);
                for (int z = 3; z <= 8; z += 1) b.stair(1, 0, z, DARK_OAK_STAIRS, E, false);
                for (int[] t : new int[][]{{14, 4}, {14, 8}}) { b.table(t[0], 0, t[1]); b.chair(t[0] - 1, 0, t[1], E); }
                b.fill(1, 2, 14, 14, 2, 14, PINK_STAINED_GLASS);
            }
            case "belediye" -> {
                b.fill(2, 0, 1, 13, 0, 14, BLUE_CARPET);
                b.fill(3, 0, 6, 12, 0, 6, QUARTZ_BLOCK); for (int x = 3; x <= 12; x++) b.slab(x, 1, 6, QUARTZ_SLAB, false);
                for (int x : new int[]{4, 8, 11}) { b.set(x, 0, 8, LECTERN); b.chair(x, 0, 9, N); }
                for (int x = 3; x <= 12; x += 3) for (int z : new int[]{2, 3}) b.chair(x, 0, z, S);
                b.set(2, 0, 13, RED_BANNER); b.set(13, 0, 13, RED_BANNER);
                b.fill(3, 0, 14, 12, 1, 14, BOOKSHELF);
            }
            case "hastane" -> {
                b.fill(3, 0, 3, 12, 0, 3, QUARTZ_BLOCK); for (int x = 3; x <= 12; x++) b.slab(x, 1, 3, QUARTZ_SLAB, false);
                for (int x : new int[]{4, 7, 10, 13}) { b.bed(x, 0, 8, S, WHITE_BED); b.bed(x, 0, 12, S, WHITE_BED); b.set(x, 0, 7, OAK_PRESSURE_PLATE); }
                for (int x : new int[]{5, 8, 11}) { b.fill(x, 0, 8, x, 2, 9, WHITE_STAINED_GLASS_PANE); b.fill(x, 0, 12, x, 2, 13, WHITE_STAINED_GLASS_PANE); }
                b.fill(4, 0, 5, 8, 0, 5, RED_CARPET); b.set(12, 0, 5, CAULDRON);
                b.fill(13, 0, 3, 13, 1, 3, RED_CONCRETE);
            }
            case "banka" -> {
                b.fill(2, 0, 6, 13, 0, 6, QUARTZ_BLOCK); b.fill(2, 1, 6, 13, 2, 6, GLASS);
                for (int x = 4; x <= 12; x += 4) b.fill(x, 1, 6, x, 1, 6, IRON_BARS);
                b.fill(9, 0, 10, 14, 3, 10, IRON_BARS); b.fill(11, 0, 10, 11, 1, 10, AIR); b.door(11, 0, 10, IRON_DOOR, S);
                for (int x = 10; x <= 13; x++) { b.set(x, 0, 13, GOLD_BLOCK); b.set(x, 1, 13, GOLD_BLOCK); b.chest(x, 0, 14, N); }
                for (int x : new int[]{3, 5, 10}) b.chair(x, 0, 3, N);
                b.set(5, 0, 3, LECTERN);
                var pl = org.bukkit.plugin.java.JavaPlugin.getPlugin(ChicagoRP.class);
                Signs.atm(pl, b, 2, 0, 1, true); Signs.atm(pl, b, 13, 0, 1, true);
            }
            default -> {}
        }
    }

    private static int roofHeight(Shop s) { return s.floors() * 5 + 2; }
}

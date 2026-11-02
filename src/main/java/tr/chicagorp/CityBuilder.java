package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import static org.bukkit.Material.*;

/** /sehir dunya tek komutla tum sehri kurar. Isler tick basina zaman butcesiyle yapilir (sunucu donmaz). */
public class CityBuilder {
    private final ChicagoRP p;
    private final Deque<Runnable> q = new ArrayDeque<>();
    private boolean running;
    private int total, done;
    private World w;
    private UUID starter;

    static final int X0 = -125, Z0 = -125, P = 48, R = 10, NB = 5, G = -59;
    static final String[] MAP = {"LHHHF", "HAKBH", "CPSPD", "HHHHH", "HHHHH"};
    static final String[][] SHOPS = {
            {"durumcu", "market", "giyim", "kahve"},
            {"silah", "demirci", "kargo", "mekanik"},
            {"galeri", "-", "jenerator", "pavyon"},
            {"hastane", "-", "belediye", "banka"}};

    public CityBuilder(ChicagoRP p) { this.p = p; }

    public void command(Player pl, String[] a) {
        if (!pl.hasPermission("rp.admin")) { U.msg(pl, "Yetkin yok."); return; }
        if (a.length == 0) { U.msg(pl, "/sehir dunya | durum | hapis | tabela | tabelacevir | araccevir | okcevir"); return; }
        switch (a[0]) {
            case "dunya" -> start(pl);
            case "durum" -> U.msg(pl, running ? "Insaat: " + done + "/" + total : "Insaat calismiyor.");
            case "hapis" -> { setLoc("jail", pl.getLocation()); U.msg(pl, "Hapis noktasi ayarlandi."); }
            case "tabela" -> U.msg(pl, Signs.respawnAll(p) + " tabela yeniden kuruldu.");
            case "tabelacevir" -> {
                double cur = p.getConfig().getDouble("models.sign-yaw-offset", 0);
                p.getConfig().set("models.sign-yaw-offset", cur == 0 ? 180.0 : 0.0);
                p.saveConfig();
                U.msg(pl, Signs.respawnAll(p) + " tabela ters çevrildi (yazı ters görünüyorsa tekrar yaz).");
            }
            case "okcevir" -> {
                double cur = p.getConfig().getDouble("models.arrow-yaw-offset", 0);
                p.getConfig().set("models.arrow-yaw-offset", cur == 0 ? 180.0 : 0.0);
                p.saveConfig();
                U.msg(pl, "Yol tarifi oku yönü: " + p.getConfig().getDouble("models.arrow-yaw-offset"));
            }
            case "araccevir" -> {
                double cur = p.getConfig().getDouble("models.vehicle-yaw-offset", 0);
                p.getConfig().set("models.vehicle-yaw-offset", cur == 0 ? 180.0 : 0.0);
                p.saveConfig();
                U.msg(pl, "Araç modeli yönü değişti: " + p.getConfig().getDouble("models.vehicle-yaw-offset"));
            }
        }
    }

    private void setLoc(String key, Location l) {
        p.getConfig().set(key + ".world", l.getWorld().getName());
        p.getConfig().set(key + ".x", l.getX()); p.getConfig().set(key + ".y", l.getY()); p.getConfig().set(key + ".z", l.getZ());
    }

    private void place(String id, String name, Location l) {
        p.getConfig().set("places." + id + ".name", name);
        p.getConfig().set("places." + id + ".world", l.getWorld().getName());
        p.getConfig().set("places." + id + ".x", l.getX()); p.getConfig().set("places." + id + ".y", l.getY()); p.getConfig().set("places." + id + ".z", l.getZ());
    }

    private void start(Player pl) {
        if (running) { U.msg(pl, "Insaat zaten suruyor: " + done + "/" + total); return; }
        w = Bukkit.getWorld("sehir");
        if (w == null) {
            WorldCreator wc = new WorldCreator("sehir").type(WorldType.FLAT).generateStructures(false)
                    .generatorSettings("{\"layers\":[{\"block\":\"minecraft:bedrock\",\"height\":1},{\"block\":\"minecraft:stone\",\"height\":3},{\"block\":\"minecraft:grass_block\",\"height\":1}],\"biome\":\"minecraft:plains\"}");
            w = Bukkit.createWorld(wc);
        }
        if (w == null) { U.msg(pl, "Dunya olusturulamadi."); return; }
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        w.setTime(6000);
        w.getWorldBorder().setCenter(0, 0); w.getWorldBorder().setSize(420);
        p.getConfig().set("spawn.world", "sehir");
        starter = pl.getUniqueId();
        q.clear(); done = 0;
        p.getConfig().set("signs", null);
        for (int x = X0; x < X0 + NB * P + R; x++) { final int xx = x; q.add(() -> roadColumn(xx)); }
        for (int j = 0; j < 5; j++) for (int i = 0; i < 5; i++) {
            final int bi = i, bj = j, bx = X0 + i * P + R, bz = Z0 + j * P + R;
            char c = MAP[j].charAt(i);
            switch (c) {
                case 'S' -> q.add(() -> {
                    Blk b = new Blk(w, X0 + 2 * P, G, Z0 + 2 * P, 58, 58, 0);
                    Location sp = Places.spawn(b);
                    setLoc("spawn", sp); p.getConfig().set("spawn.radius", 42);
                    w.setSpawnLocation(sp);
                    place("spawn", "Spawn Meydanı", sp);
                });
                case 'K' -> q.add(() -> {
                    pave(bx, bz, bx + 37, bz + 37, POLISHED_ANDESITE);
                    Blk b = new Blk(w, bx + 1, G, bz + 2, 36, 34, 0);
                    Location jail = Karakol.build(b);
                    setLoc("jail", jail);
                    place("karakol", "Polis Karakolu", b.loc(17, 0, -3));
                    Signs.add(p, b, "karakol", 18.0, 7.4, -0.6);
                });
                case 'A', 'B', 'C', 'D' -> shopBlock(bx, bz, SHOPS[c - 'A']);
                case 'P' -> q.add(() -> Places.park(new Blk(w, bx, G, bz, 38, 38, 0)));
                case 'L' -> q.add(() -> {
                    Places.harbor(new Blk(w, bx, G, bz, 38, 38, 0));
                    place("liman", "Liman (Balıkçı)", new Location(w, bx + 17.5, G, bz + 29.5));
                });
                case 'F' -> q.add(() -> {
                    Places.farm(new Blk(w, bx, G, bz, 38, 38, 0));
                    place("tarla", "Tarlalar (Çiftçi)", new Location(w, bx + 18.5, G, bz + 14.5));
                });
                default -> houseBlock(bi, bj, bx, bz);
            }
        }
        q.add(() -> {
            p.minimap.item();
            p.saveConfig(); p.plots.save();
            Player s = Bukkit.getPlayer(starter);
            if (s != null) { U.msg(s, "&aSehir tamamlandi! Spawn'a isinlaniyorsun."); s.teleport(spawnLoc()); }
            Bukkit.broadcast(U.L.deserialize("&6[RP] &fSehir insaati bitti."));
        });
        total = q.size();
        running = true;
        pl.teleport(new Location(w, 0.5, G, 0.5));
        U.msg(pl, "Sehir kuruluyor (" + total + " parca). Tamamlaninca haber verecegim. /sehir durum");
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                long end = System.nanoTime() + 35_000_000L;
                while (!q.isEmpty() && System.nanoTime() < end) {
                    Runnable r = q.poll(); done++;
                    try { r.run(); } catch (Throwable e) { p.getLogger().warning("Insaat hatasi: " + e); e.printStackTrace(); }
                }
                if (++t % 200 == 0) { Player s = Bukkit.getPlayer(starter); if (s != null) U.msg(s, "Insaat: " + done + "/" + total); }
                if (q.isEmpty()) { running = false; cancel(); }
            }
        }.runTaskTimer(p, 20L, 1L);
    }

    private Location spawnLoc() {
        var s = p.getConfig().getConfigurationSection("spawn");
        return new Location(w, s.getDouble("x"), s.getDouble("y"), s.getDouble("z"));
    }

    // ---------------------------------------------------------------- yollar
    private static int idx(int v, int origin) {
        int t = v - origin;
        if (t < 0 || t >= NB * P + R) return -1;
        int m = t % P;
        return m < R ? m : -1;
    }

    private Material roadMat(int x, int z, int a, int c) {
        boolean vx = a >= 0, hz = c >= 0;
        if (!vx && !hz) return null;
        if (vx && hz) {
            boolean sa = a <= 1 || a >= 8, sc = c <= 1 || c >= 8;
            return (sa && sc) ? LIGHT_GRAY_CONCRETE : BLACK_CONCRETE;
        }
        int o = vx ? a : c, along = vx ? z : x;
        if (o == 0 || o == 9) return LIGHT_GRAY_CONCRETE;
        if (o == 1 || o == 8) return SMOOTH_STONE;
        int pos = Math.floorMod(along - (vx ? Z0 : X0), P);
        if ((pos >= 10 && pos <= 11) || (pos >= 46 && pos <= 47)) return (o % 2 == 0) ? WHITE_CONCRETE : BLACK_CONCRETE;
        if ((o == 4 || o == 5) && Math.floorMod(along, 6) < 4) return YELLOW_CONCRETE;
        return BLACK_CONCRETE;
    }

    private void roadColumn(int x) {
        int a = idx(x, X0);
        for (int z = Z0; z < Z0 + NB * P + R; z++) {
            int c = idx(z, Z0);
            Material m = roadMat(x, z, a, c);
            if (m == null) continue;
            w.getBlockAt(x, G - 1, z).setType(m, false);
            boolean vx = a >= 0, hz = c >= 0;
            if (vx == hz) continue;
            int o = vx ? a : c, along = vx ? z : x;
            int pos = Math.floorMod(along - (vx ? Z0 : X0), P);
            if ((o == 0 || o == 9) && pos >= 14 && pos <= 44) {
                Blk one = new Blk(w, x, G, z, 1, 1, 0);
                if (pos % 12 == 4) Places.lamp(one, 0, 0);
                else if (pos % 12 == 10) Places.tree(one, 0, 0, 4);
            }
        }
    }

    private void pave(int x1, int z1, int x2, int z2, Material m) {
        for (int x = x1; x <= x2; x++) for (int z = z1; z <= z2; z++) w.getBlockAt(x, G - 1, z).setType(m, false);
    }

    // ---------------------------------------------------------------- dukkan bloklari
    private void shopBlock(int bx, int bz, String[] ids) {
        for (int k = 0; k < 4; k++) {
            final int lx = bx + (k % 2) * 20, lz = bz + (k / 2) * 20, r = (k / 2 == 0) ? 0 : 2;
            final String id = ids[k];
            if (id.equals("-")) continue;
            q.add(() -> {
                if (id.equals("galeri") || id.equals("hastane")) {
                    pave(lx, lz, lx + 37, lz + 17, POLISHED_ANDESITE);
                    Blk b = new Blk(w, lx + 1, G, lz + 1, 36, 16, 0);
                    if (id.equals("galeri")) { Gallery.build(b); place("galeri", "Araç Galerisi", b.loc(17, 0, -3)); Signs.add(p, b, "galeri", 18.0, 11.4, 0.5); }
                    else { Hospital.build(b); place("hastane", "Hastane", b.loc(17, 0, -3)); Signs.add(p, b, "hastane", 18.0, 16.6, 0.5); }
                    return;
                }
                pave(lx, lz, lx + 17, lz + 17, POLISHED_ANDESITE);
                Shops.Shop s = Shops.get(id);
                Blk b = new Blk(w, lx + 1, G, lz + 1, 16, 16, r);
                Shops.build(b, s);
                place(id, s.l1(), b.loc(7, 0, -3));
                Signs.add(p, b, id, 8.0, s.floors() * 5 - 1 + 2.4, 0.5);
            });
        }
    }

    // ---------------------------------------------------------------- evler
    private void houseBlock(int bi, int bj, int bx, int bz) {
        for (int k = 0; k < 4; k++) {
            final int lx = bx + (k % 2) * 20, lz = bz + (k / 2) * 20, r = (k / 2 == 0) ? 0 : 2, kk = k;
            int h = Math.floorMod(bi * 7 + bj * 13 + k * 5 + bi * bj, 10);
            final int t = h < 2 ? 0 : h < 5 ? 1 : h < 7 ? 2 : h < 9 ? 3 : 4;
            q.add(() -> {
                int sx = Houses.SX[t], sz = Houses.SZ[t], fy = Houses.FRONT_YARD[t];
                int ox = lx + (18 - sx) / 2, oz = (r == 0) ? lz + fy : lz + 18 - fy - sz;
                Blk b = new Blk(w, ox, G, oz, sx, sz, r);
                Houses.build(t, b);
                int dx = Houses.DOOR_X[t];
                b.fill(dx, -1, -fy, dx, -1, -1, STONE_BRICKS);
                hedge(lx, lz, r);
                if (t != 4) {
                    int tz = (r == 0) ? lz + 15 : lz + 2;
                    Places.tree(new Blk(w, lx + 2, G, tz, 1, 1, 0), 0, 0, 5);
                    Places.tree(new Blk(w, lx + 15, G, tz, 1, 1, 0), 0, 0, 5);
                }
                String id = "ev_" + bi + "_" + bj + "_" + kk;
                p.plots.create(id, w.getName(), lx, lz, lx + 17, lz + 17, Houses.PRICE[t]);
            });
        }
    }

    private void hedge(int lx, int lz, int r) {
        BlockData leaves = Places.leaves();
        int backZ = (r == 0) ? lz + 17 : lz;
        for (int x = lx; x <= lx + 17; x++) w.getBlockAt(x, G, backZ).setBlockData(leaves, false);
        for (int z = lz; z <= lz + 17; z++) { w.getBlockAt(lx, G, z).setBlockData(leaves, false); w.getBlockAt(lx + 17, G, z).setBlockData(leaves, false); }
    }
}

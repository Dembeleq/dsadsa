package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class PlotManager implements Listener {
    private final ChicagoRP p;
    private final File f;
    private final YamlConfiguration y;
    private static final double[] EFF = {1.0, 0.4, 0.2};

    public PlotManager(ChicagoRP p) {
        this.p = p;
        p.getDataFolder().mkdirs();
        f = new File(p.getDataFolder(), "plots.yml");
        y = YamlConfiguration.loadConfiguration(f);
    }
    public void save() { try { y.save(f); } catch (IOException e) { p.getLogger().warning(e.getMessage()); } }

    public void create(String id, String world, int x1, int z1, int x2, int z2, double price) {
        String b = "plots." + id + ".";
        y.set(b + "world", world);
        y.set(b + "x1", Math.min(x1, x2)); y.set(b + "x2", Math.max(x1, x2));
        y.set(b + "z1", Math.min(z1, z2)); y.set(b + "z2", Math.max(z1, z2));
        y.set(b + "price", price);
    }

    public String plotAt(Location l) {
        ConfigurationSection s = y.getConfigurationSection("plots");
        if (s == null) return null;
        for (String id : s.getKeys(false)) {
            String b = "plots." + id + ".";
            if (!l.getWorld().getName().equals(y.getString(b + "world"))) continue;
            int x = l.getBlockX(), z = l.getBlockZ();
            if (x >= y.getInt(b + "x1") && x <= y.getInt(b + "x2") && z >= y.getInt(b + "z1") && z <= y.getInt(b + "z2")) return id;
        }
        return null;
    }
    public int owned(UUID u) {
        int n = 0;
        ConfigurationSection s = y.getConfigurationSection("plots");
        if (s == null) return 0;
        for (String id : s.getKeys(false)) if (u.toString().equals(y.getString("plots." + id + ".owner"))) n++;
        return n;
    }
    public int limit(Player pl) { return pl.hasPermission("rp.rank.boss") ? 3 : pl.hasPermission("rp.rank.gangsta") ? 2 : 1; }

    public boolean mayUse(Player pl, String id) {
        if (id == null) return false;
        String b = "plots." + id + ".";
        return pl.getUniqueId().toString().equals(y.getString(b + "owner"))
                || y.getStringList(b + "trusted").contains(pl.getUniqueId().toString());
    }

    public void command(Player pl, String[] a) {
        if (a.length == 0) { U.msg(pl, "/arsa al|sat|ekle|cikar|at|bilgi  (admin: olustur <id> <fiyat> <yaricap>)"); return; }
        UUID u = pl.getUniqueId();
        String here = plotAt(pl.getLocation());
        switch (a[0]) {
            case "olustur" -> {
                if (!pl.hasPermission("rp.admin") || a.length < 4) return;
                int r = Integer.parseInt(a[3]); Location l = pl.getLocation();
                create(a[1], l.getWorld().getName(), l.getBlockX() - r, l.getBlockZ() - r, l.getBlockX() + r, l.getBlockZ() + r, Double.parseDouble(a[2]));
                U.msg(pl, "Arsa olusturuldu: " + a[1]);
            }
            case "al" -> {
                if (here == null) { U.msg(pl, "Bir arsanin icinde durmalisin."); return; }
                String b = "plots." + here + ".";
                if (y.getString(b + "owner") != null) { U.msg(pl, "Bu arsa satilik degil."); return; }
                if (owned(u) >= limit(pl)) { U.msg(pl, "Arsa hakkin dolu (" + limit(pl) + ")."); return; }
                double price = y.getDouble(b + "price");
                if (!p.data.take(u, price)) { U.msg(pl, "Yetersiz bakiye: " + (long) price); return; }
                y.set(b + "owner", u.toString());
                U.msg(pl, "Arsa senin!");
            }
            case "sat" -> {
                if (here == null || !u.toString().equals(y.getString("plots." + here + ".owner"))) { U.msg(pl, "Kendi arsanin icinde olmalisin."); return; }
                p.data.add(u, y.getDouble("plots." + here + ".price") * 0.5);
                y.set("plots." + here + ".owner", null); y.set("plots." + here + ".trusted", null);
                U.msg(pl, "Arsa satildi (%50 iade).");
            }
            case "ekle", "cikar" -> {
                if (here == null || a.length < 2 || !u.toString().equals(y.getString("plots." + here + ".owner"))) return;
                Player t = Bukkit.getPlayer(a[1]); if (t == null) return;
                List<String> l = y.getStringList("plots." + here + ".trusted");
                if (a[0].equals("ekle")) l.add(t.getUniqueId().toString()); else l.remove(t.getUniqueId().toString());
                y.set("plots." + here + ".trusted", l);
                U.msg(pl, "Guncellendi.");
            }
            case "at" -> {
                if (here == null || !u.toString().equals(y.getString("plots." + here + ".owner"))) return;
                if (p.getConfig().getBoolean("war-active", false)) { U.msg(pl, "Savas sirasinda kullanilamaz."); return; }
                for (Player o : pl.getWorld().getPlayers()) {
                    if (o == pl || !here.equals(plotAt(o.getLocation())) || mayUse(o, here) || p.police.isPolice(o)) continue;
                    var s = p.getConfig().getConfigurationSection("spawn");
                    o.teleport(new Location(Bukkit.getWorld(s.getString("world")), s.getDouble("x"), s.getDouble("y"), s.getDouble("z")));
                }
                U.msg(pl, "Yabancilar atildi.");
            }
            case "bilgi" -> U.msg(pl, here == null ? "Arsa yok." : "Arsa: " + here + " sahip: " + y.getString("plots." + here + ".owner", "satilik"));
        }
    }

    public void generator(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        String here = plotAt(pl.getLocation());
        if (!mayUse(pl, here) || !u.toString().equals(y.getString("plots." + here + ".owner"))) { U.msg(pl, "Kendi arsanda olmalisin."); return; }
        if (p.data.gens(u) >= limit(pl)) { U.msg(pl, "Jenerator limitin dolu."); return; }
        double price = p.getConfig().getDouble("generator.price") * (p.data.gens(u) + 1);
        if (!p.data.take(u, price)) { U.msg(pl, "Yetersiz bakiye: " + (long) price); return; }
        p.data.set(u, "gens", p.data.gens(u) + 1);
        U.msg(pl, "Jenerator alindi.");
    }

    /** Sadece sahibi cevrimiciyken uretir; 1. %100, 2. %40, 3. %20. */
    public void tickGenerators() {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            int g = Math.min(p.data.gens(pl.getUniqueId()), 3);
            if (g == 0 || owned(pl.getUniqueId()) == 0) continue;
            double mult = 0; for (int i = 0; i < g; i++) mult += EFF[i];
            p.data.add(pl.getUniqueId(), p.getConfig().getDouble("generator.base-income-per-minute") * mult);
        }
    }

    public void lock(Player pl) {
        Block b = pl.getTargetBlockExact(5);
        if (b == null || !(b.getState() instanceof TileState ts)) { U.msg(pl, "Bir sandiga bak."); return; }
        ts.getPersistentDataContainer().set(U.TAG, PersistentDataType.STRING, pl.getUniqueId().toString());
        ts.update();
        U.msg(pl, "Kilitlendi.");
    }

    @EventHandler public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof TileState ts)) return;
        String owner = ts.getPersistentDataContainer().get(U.TAG, PersistentDataType.STRING);
        if (owner != null && !owner.equals(e.getPlayer().getUniqueId().toString()) && !e.getPlayer().hasPermission("rp.admin")) {
            e.setCancelled(true); U.msg(e.getPlayer(), "Bu kilitli.");
        }
    }
    private boolean may(Player pl, Location l) { return pl.hasPermission("rp.admin") || mayUse(pl, plotAt(l)); }
    @EventHandler public void onBreak(BlockBreakEvent e) { if (!may(e.getPlayer(), e.getBlock().getLocation())) e.setCancelled(true); }
    @EventHandler public void onPlace(BlockPlaceEvent e) { if (!may(e.getPlayer(), e.getBlock().getLocation())) e.setCancelled(true); }
}

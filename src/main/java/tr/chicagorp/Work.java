package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Gercek meslek mekanikleri: taksi, kargo, durumcu, kacakci, soyguncu + mekanik kiti. */
public class Work implements Listener {
    private final ChicagoRP p;
    private final Random rnd = new Random();

    static class Ride { UUID passenger, driver; String dest; Location destLoc, pickup; int state; long t; }
    static class Task { Location dest; String name; long start; double dist; }
    static class Offer { UUID seller; double price; long exp; }
    static class Robbery { UUID robber; String place; Location loc; long end; double loot; }

    private final Map<UUID, Ride> rides = new HashMap<>();
    private final Map<UUID, Task> cargo = new HashMap<>(), smuggle = new HashMap<>();
    private final Map<UUID, Long> smuggleCd = new HashMap<>(), cooking = new HashMap<>();
    private final Map<UUID, Offer> offers = new HashMap<>();
    private final Map<UUID, Robbery> robberies = new HashMap<>();
    private final Map<String, Long> robCd = new HashMap<>();

    public Work(ChicagoRP p) {
        this.p = p;
        Bukkit.getScheduler().runTaskTimer(p, this::tick, 20L, 20L);
    }

    // ------------------------------------------------------------------ yardimcilar
    private Location place(String id) {
        var s = p.getConfig().getConfigurationSection("places." + id);
        if (s == null) return null;
        var w = Bukkit.getWorld(s.getString("world", "sehir"));
        return w == null ? null : new Location(w, s.getDouble("x"), s.getDouble("y"), s.getDouble("z"));
    }

    private String placeName(String id) { return p.getConfig().getString("places." + id + ".name", id); }

    private boolean near(Player pl, String id, double r) {
        Location l = place(id);
        return l != null && l.getWorld().equals(pl.getWorld()) && l.distance(pl.getLocation()) <= r;
    }

    private String randomPlace(Player pl, Set<String> exclude, double min, double max) {
        var s = p.getConfig().getConfigurationSection("places");
        List<String> ok = new ArrayList<>();
        if (s != null) for (String id : s.getKeys(false)) {
            if (exclude.contains(id)) continue;
            Location l = place(id);
            if (l == null || !l.getWorld().equals(pl.getWorld())) continue;
            double d = l.distance(pl.getLocation());
            if (d >= min && d <= max) ok.add(id);
        }
        if (ok.isEmpty() && s != null) for (String id : s.getKeys(false)) if (!exclude.contains(id)) ok.add(id);
        return ok.isEmpty() ? null : ok.get(rnd.nextInt(ok.size()));
    }

    private boolean job(Player pl, String j) {
        if (p.data.job(pl.getUniqueId()).equals(j)) return true;
        U.msg(pl, "&cBu komut için mesleğin &f" + j + " &colmalı.");
        return false;
    }

    private void alertPolice(String msg, Location l, String navName) {
        for (Player o : Bukkit.getOnlinePlayers()) if (p.police.isPolice(o)) {
            U.msg(o, msg);
            if (l != null) p.phone.setNav(o, l.clone(), navName);
        }
    }

    private int policeCount() { int n = 0; for (Player o : Bukkit.getOnlinePlayers()) if (p.police.isPolice(o)) n++; return n; }

    // ------------------------------------------------------------------ meslek kiti
    public void onJobChange(Player pl, String prev, String now) {
        if (prev.equals("mekanik") && !now.equals("mekanik")) {
            var inv = pl.getInventory();
            for (int i = 0; i < inv.getSize(); i++) if ("mek_cantasi".equals(U.tagOf(inv.getItem(i)))) inv.setItem(i, null);
        }
        if (now.equals("mekanik") && !prev.equals("mekanik")) {
            pl.getInventory().addItem(U.item(Material.TRIPWIRE_HOOK, "&bTamir Çantası &7(araca sağ tık)", "mek_cantasi"));
            U.msg(pl, "&bTamir çantası verildi. Kaza yapmış araca çantayla sağ tıkla.");
        }
    }

    // ------------------------------------------------------------------ komutlar
    public void command(Player pl, String cmd, String[] a) {
        switch (cmd) {
            case "taksi" -> taksi(pl, a);
            case "kargo" -> kargo(pl, a);
            case "durum" -> durum(pl, a);
            case "kacak" -> kacak(pl, a);
            case "soygun" -> soygun(pl, a);
        }
    }

    // ---------------- taksi
    private void taksi(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        String sub = a.length > 0 ? a[0] : "";
        switch (sub) {
            case "cagir" -> {
                if (a.length < 2 || p.getConfig().getConfigurationSection("places." + a[1]) == null) { U.msg(pl, "/taksi cagir <yer>  (yerler telefonda: Şehir)"); return; }
                if (rides.containsKey(u)) { U.msg(pl, "Zaten bir taksi talebin var. /taksi iptal"); return; }
                int n = 0;
                Ride r = new Ride(); r.passenger = u; r.dest = a[1]; r.destLoc = place(a[1]); r.state = 0; r.t = System.currentTimeMillis();
                rides.put(u, r);
                Location l = pl.getLocation();
                for (Player o : Bukkit.getOnlinePlayers()) if (p.data.job(o.getUniqueId()).equals("taksici")) {
                    n++;
                    U.msg(o, "&e[TAKSİ] &f" + pl.getName() + " taksi istiyor &7(hedef: " + placeName(a[1]) + ", " + (int) o.getLocation().distance(l) + " m uzakta) &e/taksi kabul " + pl.getName());
                }
                U.msg(pl, n == 0 ? "&cŞu an aktif taksici yok." : "&aTaksi çağrıldı (" + n + " taksici). Beklerken yakında dur.");
                if (n == 0) rides.remove(u);
            }
            case "kabul" -> {
                if (!job(pl, "taksici") || a.length < 2) return;
                Player pa = Bukkit.getPlayer(a[1]);
                Ride r = pa == null ? null : rides.get(pa.getUniqueId());
                if (r == null || r.state != 0) { U.msg(pl, "Bu talep yok ya da alınmış."); return; }
                r.driver = u; r.state = 1; r.pickup = pa.getLocation();
                p.phone.setNav(pl, pa.getLocation(), "Yolcu: " + pa.getName());
                U.msg(pl, "&aYolcuya git, aracına bindir (yolcu araca sağ tıklayıp binmeli).");
                U.msg(pa, "&aTaksi yolda: &f" + pl.getName() + " &7(araca binmek için sağ tıkla)");
            }
            case "iptal" -> { Ride r = rides.remove(u); if (r == null) for (var en : new ArrayList<>(rides.entrySet())) if (u.equals(en.getValue().driver)) rides.remove(en.getKey()); U.msg(pl, "Taksi talebi iptal edildi."); }
            default -> U.msg(pl, "Yolcu: /taksi cagir <yer>  •  Taksici: /taksi kabul <oyuncu>  •  /taksi iptal");
        }
    }

    // ---------------- kargo
    private void kargo(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        if (!job(pl, "kargocu")) return;
        String sub = a.length > 0 ? a[0] : "gorev";
        if (sub.equals("iptal")) { cargo.remove(u); removeItems(pl, "kargo"); U.msg(pl, "Görev iptal edildi."); return; }
        if (cargo.containsKey(u)) { Task t = cargo.get(u); U.msg(pl, "Aktif görevin: &e" + t.name + " &7(telefon yol tarifi açık). İptal: /kargo iptal"); return; }
        String id = randomPlace(pl, Set.of("spawn", "karakol", "hastane", "liman", "tarla"), 80, 450);
        if (id == null) { U.msg(pl, "Teslimat noktası bulunamadı (şehir kurulu mu?)."); return; }
        Task t = new Task(); t.dest = place(id); t.name = placeName(id); t.start = System.currentTimeMillis(); t.dist = t.dest.distance(pl.getLocation());
        cargo.put(u, t);
        pl.getInventory().addItem(U.item(Material.CHEST, "&6Kargo Paketi &7→ " + t.name, "kargo"));
        p.phone.setNav(pl, t.dest, "Kargo: " + t.name);
        U.msg(pl, "&aPaket alındı. Teslim yeri: &e" + t.name + " &7(" + (int) t.dist + " m). Oraya git, otomatik teslim edilir.");
    }

    private void removeItems(Player pl, String tag) {
        var inv = pl.getInventory();
        for (int i = 0; i < inv.getSize(); i++) if (tag.equals(U.tagOf(inv.getItem(i)))) inv.setItem(i, null);
    }

    // ---------------- durumcu
    private void durum(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        String sub = a.length > 0 ? a[0] : "";
        var c = p.getConfig().getConfigurationSection("durum");
        double radius = c.getDouble("near-radius", 45);
        switch (sub) {
            case "yap" -> {
                if (!job(pl, "durumcu")) return;
                if (!near(pl, "durumcu", radius)) { U.msg(pl, "Dürüm yapmak için Dürümcü dükkanında olmalısın (telefon > Şehir)."); return; }
                if (cooking.getOrDefault(u, 0L) > System.currentTimeMillis()) { U.msg(pl, "Zaten dürüm yapıyorsun."); return; }
                if (!p.bank.pay(u, c.getDouble("ingredient-cost", 60))) { U.msg(pl, "Malzeme parası yok (" + c.getInt("ingredient-cost") + " TL)."); return; }
                long secs = c.getLong("cook-seconds", 20);
                cooking.put(u, System.currentTimeMillis() + secs * 1000);
                U.msg(pl, "&eDürüm hazırlanıyor... (" + secs + " sn)");
                Bukkit.getScheduler().runTaskLater(p, () -> {
                    cooking.remove(u);
                    if (!pl.isOnline()) return;
                    pl.getInventory().addItem(U.item(Material.COOKED_BEEF, "&6Dürüm", "durum"));
                    pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1f);
                    U.msg(pl, "&aDürüm hazır! &7Müşteriye /durum sat <oyuncu> <fiyat> ya da tezgaha /durum tezgah");
                }, secs * 20L);
            }
            case "tezgah" -> {
                if (!job(pl, "durumcu")) return;
                if (!near(pl, "durumcu", radius)) { U.msg(pl, "Tezgah için Dürümcü dükkanında olmalısın."); return; }
                int n = U.count(pl, "durum");
                if (n == 0) { U.msg(pl, "Satacak dürümün yok."); return; }
                U.remove(pl, "durum", n);
                double pay = n * c.getDouble("city-price", 480);
                p.data.add(u, pay); p.data.addXp(u, n * 12L);
                U.msg(pl, "&a" + n + " dürüm tezgahta satıldı: +" + Placeholders.num(pay) + " TL");
            }
            case "sat" -> {
                if (!job(pl, "durumcu") || a.length < 3) { if (a.length < 3) U.msg(pl, "/durum sat <oyuncu> <fiyat>"); return; }
                Player t = Bukkit.getPlayer(a[1]);
                double price; try { price = Double.parseDouble(a[2]); } catch (NumberFormatException e) { U.msg(pl, "Geçersiz fiyat."); return; }
                if (t == null || price < 0 || price > 5000 || t.getLocation().distance(pl.getLocation()) > 6 || U.count(pl, "durum") == 0) { U.msg(pl, "Müşteri yakında değil, fiyat 0-5000 olmalı ve dürümün olmalı."); return; }
                Offer o = new Offer(); o.seller = u; o.price = price; o.exp = System.currentTimeMillis() + 30000;
                offers.put(t.getUniqueId(), o);
                U.msg(pl, "Teklif gönderildi."); U.msg(t, "&e" + pl.getName() + " sana dürüm satıyor: &6" + (long) price + " TL &7→ /durum kabul");
            }
            case "kabul" -> {
                Offer o = offers.remove(u);
                Player s = o == null ? null : Bukkit.getPlayer(o.seller);
                if (o == null || s == null || o.exp < System.currentTimeMillis() || U.count(s, "durum") == 0) { U.msg(pl, "Geçerli teklif yok."); return; }
                if (!p.bank.pay(u, o.price)) { U.msg(pl, "Yetersiz bakiye."); return; }
                U.remove(s, "durum", 1);
                p.data.add(o.seller, o.price); p.data.addXp(o.seller, 15);
                pl.getInventory().addItem(U.item(Material.COOKED_BEEF, "&6Dürüm", "durum"));
                U.msg(pl, "&aDürüm alındı."); U.msg(s, "&a+" + (long) o.price + " TL (dürüm satışı)");
            }
            default -> U.msg(pl, "Dürümcü: /durum yap (dükkanda) • /durum tezgah • /durum sat <oyuncu> <fiyat> • Müşteri: /durum kabul");
        }
    }

    // ---------------- kacakci
    private void kacak(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        if (!job(pl, "kacakci")) return;
        var c = p.getConfig().getConfigurationSection("smuggle");
        if (a.length > 0 && a[0].equals("iptal")) { smuggle.remove(u); removeItems(pl, "kacak"); U.msg(pl, "Kaçakçılık görevi iptal edildi."); return; }
        if (smuggle.containsKey(u)) { U.msg(pl, "Aktif görevin var: &e" + smuggle.get(u).name + "&f. İptal: /kacak iptal"); return; }
        if (smuggleCd.getOrDefault(u, 0L) > System.currentTimeMillis()) { U.msg(pl, "Biraz bekle, ortalık sıcak (" + (smuggleCd.get(u) - System.currentTimeMillis()) / 60000 + " dk)."); return; }
        if (!near(pl, "liman", c.getDouble("near-radius", 70))) { U.msg(pl, "Mal almak için limana git (telefon > Şehir > Liman)."); return; }
        String id = randomPlace(pl, Set.of("spawn", "karakol", "hastane", "liman", "tarla", "belediye"), 120, 500);
        if (id == null) { U.msg(pl, "Teslim noktası bulunamadı."); return; }
        Task t = new Task(); t.dest = place(id); t.name = placeName(id); t.start = System.currentTimeMillis(); t.dist = t.dest.distance(pl.getLocation());
        smuggle.put(u, t);
        pl.getInventory().addItem(U.item(Material.BARREL, "&c&lKaçak Mal", "kacak"));
        p.phone.setNav(pl, t.dest, "Gizli teslim: " + t.name);
        Location fuzzy = pl.getLocation().add(rnd.nextInt(81) - 40, 0, rnd.nextInt(81) - 40);
        alertPolice("&c[İSTİHBARAT] &fLiman çevresinde kaçakçılık şüphesi (yaklaşık konum işaretlendi).", fuzzy, "Kaçakçılık şüphesi");
        U.msg(pl, "&aMal alındı. Teslim: &e" + t.name + " &7(" + (int) t.dist + " m). Polis haberdar olabilir, dikkatli ol!");
    }

    // ---------------- soyguncu
    private void soygun(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        if (!job(pl, "soyguncu")) return;
        var c = p.getConfig().getConfigurationSection("robbery");
        var loot = c.getConfigurationSection("loot");
        if (a.length < 1 || loot == null || !loot.contains(a[0])) { U.msg(pl, "Soygun: /soygun <yer>  →  " + (loot == null ? "" : String.join(", ", loot.getKeys(false)))); return; }
        String id = a[0];
        if (robberies.containsKey(u)) { U.msg(pl, "Zaten bir soygundasın."); return; }
        if (policeCount() < c.getInt("min-police", 2)) { U.msg(pl, "&cSoygun için en az " + c.getInt("min-police", 2) + " polis çevrimiçi olmalı."); return; }
        if (robCd.getOrDefault(id, 0L) > System.currentTimeMillis()) { U.msg(pl, "Bu yer yakın zamanda soyuldu."); return; }
        Location l = place(id);
        if (l == null || !near(pl, id, c.getDouble("radius", 14))) { U.msg(pl, "Soyacağın yerin yanında olmalısın."); return; }
        Robbery r = new Robbery(); r.robber = u; r.place = id; r.loc = l; r.loot = loot.getDouble(id);
        r.end = System.currentTimeMillis() + c.getLong("seconds", 120) * 1000;
        robberies.put(u, r);
        alertPolice("&c&l[SOYGUN] &f" + placeName(id) + " soyuluyor! &7(yol tarifi açıldı)", l, "Soygun: " + placeName(id));
        U.msg(pl, "&cSoygun başladı! " + c.getLong("seconds", 120) + " sn boyunca bölgeden ayrılma ve hayatta kal.");
    }

    // ------------------------------------------------------------------ zamanlayici
    private void tick() {
        long now = System.currentTimeMillis();
        // taksi
        for (UUID k : new ArrayList<>(rides.keySet())) {
            Ride r = rides.get(k);
            Player pa = Bukkit.getPlayer(r.passenger), d = r.driver == null ? null : Bukkit.getPlayer(r.driver);
            if (pa == null || (r.state > 0 && d == null)) { rides.remove(k); continue; }
            if (r.state == 0 && now - r.t > 300000) { rides.remove(k); U.msg(pa, "Taksi talebi zaman aşımına uğradı."); continue; }
            if (r.state == 1 && p.vehicles.ridesWith(pa, d.getUniqueId())) {
                r.state = 2; r.pickup = pa.getLocation();
                p.phone.setNav(d, r.destLoc, "Taksi hedefi: " + placeName(r.dest));
                U.msg(d, "&aYolcu bindi. Hedef: &e" + placeName(r.dest)); U.msg(pa, "&aYolculuk başladı. Hedef: &e" + placeName(r.dest));
            } else if (r.state == 2) {
                if (!p.vehicles.ridesWith(pa, d.getUniqueId())) { rides.remove(k); U.msg(d, "Yolcu indi, yolculuk iptal."); U.msg(pa, "Yolculuk iptal."); continue; }
                if (d.getWorld().equals(r.destLoc.getWorld()) && d.getLocation().distance(r.destLoc) < 12) {
                    var c = p.getConfig().getConfigurationSection("taxi");
                    double fare = c.getDouble("fare-base", 400) + c.getDouble("fare-per-100", 150) * r.pickup.distance(r.destLoc) / 100.0;
                    if (!p.bank.pay(pa.getUniqueId(), fare)) fare = Math.max(0, p.data.money(pa.getUniqueId()));
                    p.data.add(d.getUniqueId(), fare); p.data.addXp(d.getUniqueId(), 50); p.data.addXp(pa.getUniqueId(), 10);
                    U.msg(d, "&aYolculuk bitti: &f+" + Placeholders.num(fare) + " TL"); U.msg(pa, "&aVardın. Ücret: &f" + Placeholders.num(fare) + " TL");
                    rides.remove(k);
                }
            }
        }
        // kargo / kacak
        for (var map : List.of(cargo, smuggle)) for (UUID k : new ArrayList<>(map.keySet())) {
            Player pl = Bukkit.getPlayer(k);
            Task t = map.get(k);
            boolean isCargo = map == cargo;
            String tag = isCargo ? "kargo" : "kacak";
            if (pl == null) { map.remove(k); continue; }
            if (U.count(pl, tag) == 0) { map.remove(k); U.msg(pl, "Paketi kaybettin, görev bitti."); continue; }
            if (pl.getWorld().equals(t.dest.getWorld()) && pl.getLocation().distance(t.dest) < 7) {
                removeItems(pl, tag);
                map.remove(k);
                double pay;
                if (isCargo) {
                    var c = p.getConfig().getConfigurationSection("cargo");
                    pay = c.getDouble("base-pay", 300) + c.getDouble("per-100", 100) * t.dist / 100.0;
                    if ((now - t.start) / 1000.0 < t.dist / 6.0 + 25) pay *= 1.25;
                } else {
                    pay = p.getConfig().getDouble("smuggle.pay", 6000);
                    smuggleCd.put(k, now + p.getConfig().getLong("smuggle.cooldown-minutes", 12) * 60000L);
                }
                p.data.add(k, pay); p.data.addXp(k, isCargo ? 60 : 100);
                U.msg(pl, "&aTeslim edildi: &f+" + Placeholders.num(pay) + " TL");
            }
        }
        // soygun
        for (UUID k : new ArrayList<>(robberies.keySet())) {
            Robbery r = robberies.get(k);
            Player pl = Bukkit.getPlayer(k);
            double radius = p.getConfig().getDouble("robbery.radius", 14) + 6;
            if (pl == null || pl.isDead() || p.police.isCuffed(pl) || !pl.getWorld().equals(r.loc.getWorld()) || pl.getLocation().distance(r.loc) > radius) {
                robberies.remove(k);
                robCd.put(r.place, now + 5 * 60000L);
                if (pl != null) U.msg(pl, "&cSoygun başarısız!");
                alertPolice("&a[SOYGUN] &f" + placeName(r.place) + " soygunu durduruldu.", null, "");
                continue;
            }
            long left = (r.end - now) / 1000;
            if (left > 0) { pl.sendActionBar(U.L.deserialize("&c&lSOYGUN &f" + left + " sn &7(bölgeden ayrılma)")); continue; }
            robberies.remove(k);
            robCd.put(r.place, now + p.getConfig().getLong("robbery.cooldown-minutes", 30) * 60000L);
            p.data.add(k, r.loot); p.data.addXp(k, 200);
            U.msg(pl, "&aSoygun başarılı: &f+" + Placeholders.num(r.loot) + " TL &7— kaç!");
            alertPolice("&c[SOYGUN] &f" + placeName(r.place) + " soyuldu! Şüpheliyi yakalayın.", pl.getLocation(), "Şüpheli");
        }
    }

    @EventHandler public void onDeath(PlayerDeathEvent e) {
        Robbery r = robberies.remove(e.getEntity().getUniqueId());
        if (r != null) { robCd.put(r.place, System.currentTimeMillis() + 5 * 60000L); }
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        rides.remove(u); robberies.remove(u);
    }

    @SuppressWarnings("unused") private static ItemStack unused() { return null; }
}

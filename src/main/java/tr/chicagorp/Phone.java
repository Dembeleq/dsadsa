package tr.chicagorp;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Telefon: renkli duvar kagidi, 3D uygulama ikonlari, yol tarifi (mor isik sutunu). Telefon esyasi son hotbar slotunda. */
public class Phone implements Listener {
    private final ChicagoRP p;
    private final Map<UUID, Location> nav = new HashMap<>();
    private final Map<UUID, String> navName = new HashMap<>();
    private final Map<UUID, Long> policeCd = new HashMap<>();
    private final Map<UUID, ItemDisplay[]> arrows = new HashMap<>();
    private int tickN = 0;

    private static final Material[] WALL_MAIN = {Material.PURPLE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE};
    private static final Material[] WALL_SUB = {Material.CYAN_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE};

    public Phone(ChicagoRP p) {
        this.p = p;
        Bukkit.getScheduler().runTaskTimer(p, this::navTick, 10L, 2L);
    }

    /** Telefonu son hotbar slotuna (9. slot) koyar. */
    public void give(Player pl) {
        int has = -1;
        var inv = pl.getInventory();
        for (int i = 0; i < inv.getSize(); i++) if (U.hasTag(inv.getItem(i), "telefon")) { has = i; break; }
        ItemStack phone = has >= 0 ? inv.getItem(has) : U.custom(p.getConfig().getString("phone-ia", ""),
                U.withModel(U.item(Material.IRON_NUGGET, "&b&lTelefon &7(sağ tık)", "telefon"), "phone"));
        if (has == 8) return;
        if (has >= 0) inv.setItem(has, null);
        ItemStack old = inv.getItem(8);
        inv.setItem(8, phone);
        if (old != null) inv.addItem(old);
    }

    @EventHandler public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (U.hasTag(e.getItem(), "telefon")) { e.setCancelled(true); open(e.getPlayer()); }
    }

    private void wall(Gui g, int size, Material[] rows) {
        for (int i = 0; i < size; i++) g.put(i, U.icon(rows[Math.min(i / 9, rows.length - 1)], " "), null);
    }

    public void open(Player pl) {
        UUID u = pl.getUniqueId();
        Gui g = new Gui("telefon", 36, "&8&l Telefon");
        wall(g, 36, WALL_MAIN);
        g.put(10, U.app("app_sehir", Material.COMPASS, "&bŞehir", "&7Yerler ve yol tarifi"), "page:sehir");
        g.put(11, U.app("app_isler", Material.EMERALD, "&aİşler & Para", "&7Meslek seç"), "page:isler");
        g.put(12, U.app("app_arac", Material.MINECART, "&6Araçlar", "&7Garajın"), "page:arac");
        g.put(14, U.app("app_cete", Material.SHIELD, "&cÇete & Güvenlik", "&7PvP, çete, polis çağır"), "page:cete");
        g.put(15, U.app("app_sosyal", Material.PLAYER_HEAD, "&dSosyal", "&7Oyuncular"), "page:sosyal");
        g.put(16, U.app("app_oduller", Material.NETHER_STAR, "&eÖdüller & Seviye", "&7Günlük ödül"), "page:oduller");
        g.put(20, U.app("app_saat", Material.CLOCK, "&f" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm  •  dd.MM.yyyy"))), null);
        g.put(21, U.app("app_para", Material.GOLD_INGOT, "&6Bakiye: &f" + (long) p.data.money(u) + " TL", "&7Seviye: &b" + p.data.level(u)), null);
        g.put(22, U.app("app_banka", Material.EMERALD, "&aBanka", "&7Nakit: &f" + (long) p.data.money(u) + " TL", "&7Banka: &f" + (long) p.data.bank(u) + " TL", "&eTıkla: aç"), "bank");
        g.put(23, U.app("app_harita", Material.FILLED_MAP, "&2Şehir Haritası", "&7Tıkla: haritayı al"), "map");
        g.put(24, U.app("app_kilit", Material.IRON_DOOR, "&eAraç Ayarları", "&7Kilit • yolcu • atma"), "carset");
        g.put(35, U.app("app_kapat", Material.BARRIER, "&cKapat"), "close");
        pl.openInventory(g.inv);
    }

    private Gui sub(String title, int size) {
        Gui g = new Gui("telefon", size, title);
        wall(g, size, WALL_SUB);
        g.put(size - 1, U.app("app_kapat", Material.ARROW, "&7Geri"), "back");
        return g;
    }

    private void page(Player pl, String pg) {
        UUID u = pl.getUniqueId();
        switch (pg) {
            case "sehir" -> {
                Gui g = sub("&8Şehir", 36);
                var s = p.getConfig().getConfigurationSection("places");
                int i = 9;
                g.put(4, U.app("app_sehir", Material.COMPASS, "&b&lYol tarifi", "&7Bir yer seç, mor ışık sütununu takip et"), null);
                if (s != null) for (String id : s.getKeys(false)) { if (i >= 27) break; g.put(i++, U.app("app_harita", Material.MAP, "&e" + s.getString(id + ".name", id), "&7Tıkla: yol tarifi"), "nav:" + id); }
                g.put(31, U.icon(Material.BARRIER, "&cYol tarifini kapat"), "navoff");
                pl.openInventory(g.inv);
            }
            case "isler" -> {
                Gui g = sub("&8İşler & Para", 54);
                int i = 0;
                for (String j : JobManager.ALL) {
                    int cap = p.jobs.capacity(j), cnt = p.data.countJob(j);
                    boolean mine = j.equals(p.data.job(u));
                    String desc = p.getConfig().getString("jobs." + j + ".desc", "");
                    g.put(i++, U.app("app_isler", mine ? Material.LIME_DYE : Material.PAPER, (mine ? "&a" : "&e") + j,
                            "&7" + desc, "&7Kontenjan: &f" + cnt + "/" + (cap < 0 ? "∞" : String.valueOf(cap)),
                            mine ? "&aŞu anki mesleğin" : "&eSol tık: seç", "&bSağ tık: nasıl yapılır?"), "job:" + j);
                }
                g.put(48, U.icon(Material.RED_DYE, "&cMesleği bırak"), "job:birak");
                g.put(49, U.app("app_para", Material.GOLD_INGOT, "&6Bakiye: &f" + (long) p.data.money(u) + " TL"), null);
                pl.openInventory(g.inv);
            }
            case "arac" -> p.vehicles.openGarage(pl);
            case "cete" -> {
                Gui g = sub("&8Çete & Güvenlik", 27);
                g.put(10, U.icon(Material.LIME_CONCRETE, "&aPvP aç"), "pvp:ac");
                g.put(11, U.icon(Material.RED_CONCRETE, "&cPvP kapat", "&7Sadece spawn'da"), "pvp:kapat");
                g.put(13, U.app("app_cete", Material.IRON_SWORD, "&eÇete bilgisi", "&7" + p.gang.info(u)), "gang");
                g.put(15, U.icon(Material.BELL, "&9Polisi çağır", "&7Konumun polislere gider"), "polis");
                g.put(16, U.icon(Material.NAME_TAG, "&eKimliğim"), "kimlik");
                pl.openInventory(g.inv);
            }
            case "sosyal" -> {
                Gui g = sub("&8Sosyal", 54);
                int i = 0;
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (i >= 45) break;
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta m = (SkullMeta) head.getItemMeta();
                    m.setOwningPlayer(o); m.displayName(U.L.deserialize("&e" + o.getName())); head.setItemMeta(m);
                    g.put(i++, head, "profil:" + o.getName());
                }
                pl.openInventory(g.inv);
            }
            case "oduller" -> {
                Gui g = sub("&8Ödüller & Seviye", 27);
                g.put(11, U.app("app_oduller", Material.EXPERIENCE_BOTTLE, "&bSeviye: &f" + p.data.level(u)), null);
                g.put(15, U.app("app_para", Material.CHEST, "&6Günlük ödül", "&7" + (long) p.getConfig().getDouble("daily-reward", 1500) + " TL + 100 XP", "&7Tıkla: al"), "daily");
                pl.openInventory(g.inv);
            }
        }
    }

    @EventHandler public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Gui g) || !g.page.equals("telefon")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player pl)) return;
        String a = g.act.get(e.getRawSlot());
        if (a == null) return;
        UUID u = pl.getUniqueId();
        if (a.startsWith("page:")) page(pl, a.substring(5));
        else if (a.equals("back")) open(pl);
        else if (a.equals("close")) pl.closeInventory();
        else if (a.equals("bank")) p.bank.open(pl);
        else if (a.equals("carset")) p.vehicles.openSettings(pl);
        else if (a.equals("map")) {
            pl.closeInventory();
            ItemStack m = p.minimap.item();
            if (m == null) U.msg(pl, "Şehir henüz kurulmamış (/sehir dunya).");
            else { pl.getInventory().addItem(m); U.msg(pl, "&aŞehir haritası envanterine eklendi."); }
        }
        else if (a.startsWith("job:")) {
            if (e.isRightClick()) { pl.closeInventory(); p.jobs.info(pl, a.substring(4)); }
            else { pl.performCommand("meslek " + a.substring(4)); page(pl, "isler"); }
        }
        else if (a.startsWith("nav:")) {
            var s = p.getConfig().getConfigurationSection("places." + a.substring(4));
            var w = s == null ? null : Bukkit.getWorld(s.getString("world", "sehir"));
            if (w == null) return;
            nav.put(u, new Location(w, s.getDouble("x"), s.getDouble("y"), s.getDouble("z")));
            navName.put(u, s.getString("name", a.substring(4)));
            pl.setCompassTarget(nav.get(u));
            pl.closeInventory();
            U.msg(pl, "Yol tarifi: &e" + navName.get(u) + " &f— mor ışık sütununu ve parçacık okunu takip et.");
        }
        else if (a.equals("navoff")) { nav.remove(u); clearArrows(u); pl.closeInventory(); U.msg(pl, "Yol tarifi kapatıldı."); }
        else if (a.equals("pvp:ac")) { pl.closeInventory(); p.pvp.toggle(pl, new String[]{"ac"}); }
        else if (a.equals("pvp:kapat")) { pl.closeInventory(); p.pvp.toggle(pl, new String[]{"kapat"}); }
        else if (a.equals("gang")) { pl.closeInventory(); U.msg(pl, "Çete: " + p.gang.info(u) + " | /cete kur, davet, katil, ayril, baris"); }
        else if (a.equals("kimlik")) { pl.closeInventory(); pl.performCommand("kimlik"); }
        else if (a.startsWith("profil:")) { pl.closeInventory(); pl.performCommand("profil " + a.substring(7)); }
        else if (a.equals("polis")) { pl.closeInventory(); callPolice(pl); }
        else if (a.equals("daily")) daily(pl);
    }

    private void callPolice(Player pl) {
        long now = System.currentTimeMillis();
        if (policeCd.getOrDefault(pl.getUniqueId(), 0L) + 60000L > now) { U.msg(pl, "Az önce çağırdın, bekle."); return; }
        policeCd.put(pl.getUniqueId(), now);
        int n = 0;
        Location l = pl.getLocation();
        for (Player o : Bukkit.getOnlinePlayers()) {
            if (!p.police.isPolice(o)) continue;
            n++;
            U.msg(o, "&c[ACİL] &f" + pl.getName() + " yardım istiyor: " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + " (yol tarifi açıldı)");
            nav.put(o.getUniqueId(), l.clone()); navName.put(o.getUniqueId(), "Acil çağrı");
        }
        U.msg(pl, n == 0 ? "Şu an aktif polis yok." : "Polis çağrıldı (" + n + " görevli).");
    }

    private void daily(Player pl) {
        UUID u = pl.getUniqueId();
        long last = ((Number) p.data.get(u, "daily", 0L)).longValue(), now = System.currentTimeMillis();
        if (last + 24L * 3600_000L > now) { U.msg(pl, "Günlük ödülü zaten aldın."); return; }
        double dr = p.getConfig().getDouble("daily-reward", 1500);
        p.data.set(u, "daily", now); p.data.add(u, dr); p.data.addXp(u, 100);
        U.msg(pl, "&a+" + (long) dr + " TL, +100 XP");
        pl.closeInventory();
    }

    public void setNav(Player pl, Location l, String name) {
        nav.put(pl.getUniqueId(), l);
        navName.put(pl.getUniqueId(), name);
    }

    private ItemDisplay mkArrow(Player pl, Location l, String model, float scale) {
        ItemStack it = U.withModel(new ItemStack(Material.ARROW), model);
        ItemDisplay d = l.getWorld().spawn(l, ItemDisplay.class, e -> {
            e.setItemStack(it);
            e.setTransformation(new Transformation(new Vector3f(0f, 0f, 0f), new AxisAngle4f(0f, 0f, 0f, 1f), new Vector3f(scale, scale, scale), new AxisAngle4f(0f, 0f, 0f, 1f)));
            e.setVisibleByDefault(false);
            e.setPersistent(false);
            e.setViewRange(6f);
            e.setTeleportDuration(2);
            e.setGlowing(true);
            e.setGlowColorOverride(Color.fromRGB(175, 45, 245));
        });
        pl.showEntity(p, d);
        return d;
    }

    private void clearArrows(UUID u) {
        ItemDisplay[] ar = arrows.remove(u);
        if (ar != null) for (ItemDisplay d : ar) if (d != null) d.remove();
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) { nav.remove(e.getPlayer().getUniqueId()); clearArrows(e.getPlayer().getUniqueId()); }

    /** Yol tarifi: oyuncunun onunde hedefe donuk mor ok + hedefin ustunde ziplayan asagi bakan mor ok (sadece o oyuncu gorur). */
    private void navTick() {
        tickN++;
        float yawOff = (float) p.getConfig().getDouble("models.arrow-yaw-offset", 0);
        for (UUID u : new ArrayList<>(nav.keySet())) {
            Player pl = Bukkit.getPlayer(u);
            Location t = nav.get(u);
            if (pl == null) { nav.remove(u); clearArrows(u); continue; }
            if (!pl.getWorld().equals(t.getWorld())) continue;
            double d = pl.getLocation().distance(t);
            if (d < 5) { nav.remove(u); clearArrows(u); U.msg(pl, "Hedefe vardın."); continue; }
            pl.setCompassTarget(t);
            if (tickN % 5 == 0) pl.sendActionBar(Component.text("Hedef: " + navName.getOrDefault(u, "?") + " — " + (int) d + " m"));
            ItemDisplay[] ar = arrows.computeIfAbsent(u, k -> new ItemDisplay[2]);
            Vector dir = t.toVector().subtract(pl.getLocation().toVector()).setY(0);
            if (dir.lengthSquared() > 1) {
                dir.normalize();
                Location gl = pl.getEyeLocation().add(dir.clone().multiply(2.8)).add(0, -0.9, 0);
                gl.setYaw((float) Math.toDegrees(Math.atan2(-dir.getX(), dir.getZ())) + yawOff);
                gl.setPitch(0f);
                if (ar[0] == null || !ar[0].isValid()) ar[0] = mkArrow(pl, gl, "nav_arrow", 2.2f); else ar[0].teleport(gl);
            }
            if (d < 110) {
                Location ml = t.clone().add(0.5, 1.5 + Math.sin(tickN * 0.25) * 0.6, 0.5);
                ml.setYaw(tickN * 6f);
                if (ar[1] == null || !ar[1].isValid()) ar[1] = mkArrow(pl, ml, "nav_arrow_down", 3.5f); else ar[1].teleport(ml);
            } else if (ar[1] != null) { ar[1].remove(); ar[1] = null; }
        }
    }
}

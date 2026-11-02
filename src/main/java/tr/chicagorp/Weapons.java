package tr.chicagorp;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.*;

/** Modsuz silah sistemi: hitscan, sarjor, mermi, yeniden doldurma, sopa. Silahci menusu + Silah Ustasi uretimi. */
public class Weapons implements Listener {
    static final NamespacedKey WID = NamespacedKey.fromString("chicagorp:wid");
    static final NamespacedKey MAG = NamespacedKey.fromString("chicagorp:mag");
    private final ChicagoRP p;
    private final Map<UUID, Long> nextShot = new HashMap<>(), reloading = new HashMap<>(), producing = new HashMap<>();
    private final Random rnd = new Random();

    public Weapons(ChicagoRP p) { this.p = p; }

    public ConfigurationSection def(String id) { return p.getConfig().getConfigurationSection("weapons." + id); }

    public String idOf(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return null;
        return it.getItemMeta().getPersistentDataContainer().get(WID, PersistentDataType.STRING);
    }

    private int magOf(ItemStack it) { return it.getItemMeta().getPersistentDataContainer().getOrDefault(MAG, PersistentDataType.INTEGER, 0); }

    private void setMag(ItemStack it, int m, ConfigurationSection c) {
        ItemMeta im = it.getItemMeta();
        im.getPersistentDataContainer().set(MAG, PersistentDataType.INTEGER, m);
        im.lore(List.of(U.L.deserialize("&7Şarjör: &f" + m + "&7/&f" + c.getInt("mag")).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                U.L.deserialize("&8Ateş: sağ tık  •  Doldur: F").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)));
        it.setItemMeta(im);
    }

    public ItemStack make(String id) {
        var c = def(id);
        if (c == null) return null;
        Material m = Material.matchMaterial(c.getString("material", "BLAZE_ROD"));
        ItemStack it = new ItemStack(m == null ? Material.BLAZE_ROD : m);
        ItemMeta im = it.getItemMeta();
        im.displayName(U.L.deserialize(c.getString("name", id)).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        im.getPersistentDataContainer().set(WID, PersistentDataType.STRING, id);
        it.setItemMeta(im);
        if (c.getString("type", "gun").equals("gun")) setMag(it, c.getInt("mag"), c);
        return U.withModel(it, c.getString("model", "wpn_" + id));
    }

    public ItemStack ammo(String type, int n) {
        var c = p.getConfig().getConfigurationSection("ammo." + type);
        if (c == null) return null;
        ItemStack it = U.item(Material.GOLD_NUGGET, c.getString("name", type), "ammo_" + type);
        it.setAmount(Math.max(1, Math.min(64, n)));
        return U.withModel(it, c.getString("model", "ammo_" + type));
    }

    // ------------------------------------------------------------------ ates / sarjor
    @EventHandler public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        String id = idOf(e.getItem());
        if (id == null) return;
        var c = def(id);
        if (c == null || !c.getString("type", "gun").equals("gun")) return;
        e.setCancelled(true);
        fire(e.getPlayer(), e.getItem(), c);
    }

    private void fire(Player pl, ItemStack item, ConfigurationSection c) {
        UUID u = pl.getUniqueId();
        long now = System.currentTimeMillis();
        if (!p.getConfig().getBoolean("guns-enabled", true)) { pl.sendActionBar(Component.text("Silahlar şu an devre dışı.")); return; }
        if (reloading.getOrDefault(u, 0L) > now || nextShot.getOrDefault(u, 0L) > now) return;
        int mag = magOf(item);
        if (mag <= 0) {
            pl.sendActionBar(U.L.deserialize("&cŞarjör boş! &7F ile doldur"));
            pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.6f);
            nextShot.put(u, now + 350);
            return;
        }
        setMag(item, mag - 1, c);
        nextShot.put(u, now + c.getInt("cooldown", 5) * 50L);
        pl.sendActionBar(U.L.deserialize("&7Şarjör: &f" + (mag - 1) + "&7/&f" + c.getInt("mag")));
        double dmg = c.getDouble("damage", 5), range = c.getDouble("range", 40), spread = c.getDouble("spread", 0.02);
        int pellets = Math.max(1, c.getInt("pellets", 1));
        Location eye = pl.getEyeLocation();
        for (int i = 0; i < pellets; i++) {
            Vector d = eye.getDirection().clone().add(new Vector((rnd.nextDouble() - .5) * 2 * spread, (rnd.nextDouble() - .5) * 2 * spread, (rnd.nextDouble() - .5) * 2 * spread)).normalize();
            RayTraceResult r = pl.getWorld().rayTrace(eye, d, range, FluidCollisionMode.NEVER, true, 0.15,
                    en -> en != pl && en instanceof LivingEntity && !(en instanceof ArmorStand));
            Vector end = r != null ? r.getHitPosition() : eye.toVector().add(d.clone().multiply(range));
            if (r != null && r.getHitEntity() instanceof LivingEntity t) {
                boolean head = r.getHitPosition().getY() > t.getEyeLocation().getY() - 0.3;
                t.damage(dmg * (head ? p.getConfig().getDouble("headshot-multiplier", 1.5) : 1.0), pl);
                pl.playSound(pl.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.7f, head ? 1.6f : 1.1f);
            }
            double len = end.distance(eye.toVector());
            for (double s = 1.5; s < len; s += 1.5) {
                Location pt = eye.clone().add(d.clone().multiply(s));
                new ParticleBuilder(Particle.DUST).location(pt).color(Color.fromRGB(255, 225, 120), 0.5f).count(1).spawn();
            }
        }
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.5f);
    }

    @EventHandler public void onSwap(PlayerSwapHandItemsEvent e) {
        ItemStack it = e.getPlayer().getInventory().getItemInMainHand();
        String id = idOf(it);
        if (id == null) return;
        var c = def(id);
        if (c == null || !c.getString("type", "gun").equals("gun")) return;
        e.setCancelled(true);
        reload(e.getPlayer(), c, id);
    }

    private void reload(Player pl, ConfigurationSection c, String id) {
        UUID u = pl.getUniqueId();
        ItemStack it = pl.getInventory().getItemInMainHand();
        int mag = magOf(it), max = c.getInt("mag");
        if (mag >= max || reloading.getOrDefault(u, 0L) > System.currentTimeMillis()) return;
        String tag = "ammo_" + c.getString("ammo", "pistol");
        if (U.count(pl, tag) <= 0) { pl.sendActionBar(U.L.deserialize("&cMermin yok!")); return; }
        long ticks = c.getInt("reload", 50);
        reloading.put(u, System.currentTimeMillis() + ticks * 50);
        pl.sendActionBar(U.L.deserialize("&eŞarjör dolduruluyor..."));
        pl.playSound(pl.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 1.4f);
        Bukkit.getScheduler().runTaskLater(p, () -> {
            ItemStack now = pl.getInventory().getItemInMainHand();
            if (!pl.isOnline() || !id.equals(idOf(now))) { reloading.remove(u); return; }
            int cur = magOf(now), need = max - cur, have = U.count(pl, tag), load = Math.min(need, have);
            if (load > 0) { U.remove(pl, tag, load); setMag(now, cur + load, c); }
            reloading.remove(u);
            pl.sendActionBar(U.L.deserialize("&aŞarjör hazır: &f" + (cur + load) + "&7/&f" + max));
            pl.playSound(pl.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1f, 1.4f);
        }, ticks);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMelee(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player pl)) return;
        String id = idOf(pl.getInventory().getItemInMainHand());
        if (id == null) return;
        var c = def(id);
        if (c != null && c.getString("type", "gun").equals("melee")) e.setDamage(c.getDouble("damage", 6));
    }

    // ------------------------------------------------------------------ silahci menusu
    public void openShop(Player pl) {
        Gui g = new Gui("silahci", 54, "&8&lSİLAHÇI");
        for (int i = 0; i < 54; i++) g.put(i, U.icon(i < 9 || i >= 45 ? Material.BLACK_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, " "), null);
        int lv = p.data.level(pl.getUniqueId());
        g.put(4, U.icon(Material.GOLD_INGOT, "&6Nakit: &f" + (long) p.data.money(pl.getUniqueId()) + " TL", "&7Banka: &f" + (long) p.data.bank(pl.getUniqueId()) + " TL", "&7Seviyen: &b" + lv), null);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        int s = 0;
        var ws = p.getConfig().getConfigurationSection("weapons");
        for (String id : ws.getKeys(false)) {
            var c = ws.getConfigurationSection(id);
            if (!c.getBoolean("shop", false) || s >= slots.length) continue;
            boolean ok = lv >= c.getInt("level", 1);
            ItemStack it = make(id);
            ItemMeta im = it.getItemMeta();
            im.lore(List.of(U.L.deserialize("&8━━━━━━━━━━━━━━").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                    U.L.deserialize("&7Hasar: &f" + c.getDouble("damage")).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                    U.L.deserialize("&7Fiyat: &6" + c.getInt("price") + " TL").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                    U.L.deserialize(ok ? "&eTıkla: satın al" : "&c✖ Seviye " + c.getInt("level") + " gerekli").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)));
            it.setItemMeta(im);
            g.put(slots[s++], it, ok ? "w:" + id : null);
        }
        int a = 28;
        var am = p.getConfig().getConfigurationSection("ammo");
        for (String t : am.getKeys(false)) {
            ItemStack it = ammo(t, 1);
            ItemMeta im = it.getItemMeta();
            im.lore(List.of(U.L.deserialize("&7Fiyat: &6" + am.getInt(t + ".price") + " TL / adet").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                    U.L.deserialize("&eSol tık: x10  •  Sağ tık: x30").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)));
            it.setItemMeta(im);
            g.put(a++, it, "a:" + t);
        }
        g.put(49, U.app("app_kapat", Material.BARRIER, "&cKapat"), "close");
        pl.openInventory(g.inv);
    }

    @EventHandler public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Gui g) || !g.page.equals("silahci")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player pl)) return;
        String a = g.act.get(e.getRawSlot());
        if (a == null) return;
        if (a.equals("close")) { pl.closeInventory(); return; }
        if (a.startsWith("w:")) {
            var c = def(a.substring(2));
            if (c == null) return;
            if (p.data.level(pl.getUniqueId()) < c.getInt("level", 1)) { U.msg(pl, "&cSeviyen yetersiz."); return; }
            if (!p.bank.pay(pl.getUniqueId(), c.getInt("price"))) { U.msg(pl, "&cYetersiz bakiye."); return; }
            pl.getInventory().addItem(make(a.substring(2)));
            U.msg(pl, "&a" + U.L.serialize(U.L.deserialize(c.getString("name"))) .replaceAll("&.", "") + " satın alındı.");
        } else if (a.startsWith("a:")) {
            String t = a.substring(2);
            int n = e.isRightClick() ? 30 : 10;
            int price = p.getConfig().getInt("ammo." + t + ".price") * n;
            if (!p.bank.pay(pl.getUniqueId(), price)) { U.msg(pl, "&cYetersiz bakiye (" + price + " TL)."); return; }
            pl.getInventory().addItem(ammo(t, n));
            U.msg(pl, "&a" + n + " mermi alındı (-" + price + " TL).");
        }
        openShop(pl);
    }

    // ------------------------------------------------------------------ silah ustasi uretimi + admin
    public void produce(Player pl, String id) {
        var c = def(id);
        UUID u = pl.getUniqueId();
        if (c == null || c.getBoolean("shop", false)) { U.msg(pl, "Bu silah üretilemez."); return; }
        if (!p.data.job(u).equals("silah_ustasi")) { U.msg(pl, "Sadece Silah Ustası üretebilir."); return; }
        if (producing.getOrDefault(u, 0L) > System.currentTimeMillis()) { U.msg(pl, "Zaten bir üretim sürüyor."); return; }
        if (!p.bank.pay(u, c.getInt("production-cost"))) { U.msg(pl, "Malzeme bedeli: " + c.getInt("production-cost") + " TL."); return; }
        long secs = c.getLong("production-seconds", 1800);
        producing.put(u, System.currentTimeMillis() + secs * 1000);
        Location l = pl.getLocation();
        for (Player o : Bukkit.getOnlinePlayers()) if (p.police.isPolice(o)) {
            U.msg(o, "&c[İHBAR] &fSilah üretimi tespit edildi: " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + " &7(yol tarifi açıldı)");
            p.phone.setNav(o, l.clone(), "Silah üretimi");
        }
        U.msg(pl, "Üretim başladı (" + secs / 60 + " dk). Polis haberdar edildi, üretimi koru!");
        Bukkit.getScheduler().runTaskLater(p, () -> {
            producing.remove(u);
            ItemStack w = make(id);
            if (pl.isOnline()) { pl.getInventory().addItem(w); U.msg(pl, "&aÜretim tamamlandı!"); }
            else { List<String> l2 = new ArrayList<>(); Object o = p.data.get(u, "pending_w", null); if (o instanceof List<?> li) for (Object x : li) l2.add(String.valueOf(x)); l2.add(id); p.data.set(u, "pending_w", l2); }
        }, secs * 20L);
    }

    public void deliverPending(Player pl) {
        Object o = p.data.get(pl.getUniqueId(), "pending_w", null);
        if (!(o instanceof List<?> li) || li.isEmpty()) return;
        for (Object x : li) { ItemStack w = make(String.valueOf(x)); if (w != null) pl.getInventory().addItem(w); }
        p.data.set(pl.getUniqueId(), "pending_w", null);
        U.msg(pl, "&aÜretilen silahların envanterine eklendi.");
    }

    public void adminGive(CommandSender s, String player, String id, int n) {
        Player t = Bukkit.getPlayer(player);
        if (t == null) { U.msg(s, "Oyuncu bulunamadı."); return; }
        ItemStack w = make(id);
        if (w == null) { U.msg(s, "Silah yok: " + String.join(", ", p.getConfig().getConfigurationSection("weapons").getKeys(false))); return; }
        for (int i = 0; i < Math.max(1, n); i++) t.getInventory().addItem(make(id));
        U.msg(s, "Verildi: " + id + " x" + Math.max(1, n));
    }

    public void adminAmmo(CommandSender s, String player, String type, int n) {
        Player t = Bukkit.getPlayer(player);
        ItemStack a = ammo(type, n);
        if (t == null || a == null) { U.msg(s, "Oyuncu/mermi tipi geçersiz (pistol, rifle, shell, sniper)."); return; }
        t.getInventory().addItem(a);
        U.msg(s, "Mermi verildi.");
    }
}

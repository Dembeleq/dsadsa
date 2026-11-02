package tr.chicagorp;

import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import java.util.*;

/** Arac sistemi: kademeli hizlanma, hiz gostergesi, hasar/tamir, bagaj, boyama/motor modifiyesi, is araclari, koltuklar, kilit. */
public class Vehicles implements Listener {
    public record Car(String id, String name, int level, double price, double speed, int seats, double scale, String model, Material mat, String ia,
                      int color, boolean paint, int trunk, boolean jobCar, double dx, double dz, List<double[]> seatOff) {}

    static class Active {
        Horse horse; ItemDisplay body; Interaction hit; List<ArmorStand> seats = new ArrayList<>();
        UUID owner; Car car; boolean modelUsed, locked = true, anyone = false; Set<UUID> allowed = new HashSet<>();
        int hp = 100, engine = 0, color; double cur, lx, lz, prevV, kmh; long repairing = 0;
    }

    static class TrunkHolder implements InventoryHolder {
        final UUID owner; final String car; Inventory inv;
        TrunkHolder(UUID o, String c) { owner = o; car = c; }
        @Override public Inventory getInventory() { return inv; }
    }

    private final ChicagoRP p;
    private final Map<UUID, Active> active = new HashMap<>();
    private final Map<UUID, UUID> clickOwner = new HashMap<>();
    private int tickN = 0;

    public Vehicles(ChicagoRP p) {
        this.p = p;
        Bukkit.getScheduler().runTaskTimer(p, this::tick, 2L, 1L);
    }

    private static double num(List<?> l, int i) { return l != null && l.size() > i && l.get(i) instanceof Number n ? n.doubleValue() : 0; }

    public Map<String, Car> cars() {
        Map<String, Car> m = new LinkedHashMap<>();
        var s = p.getConfig().getConfigurationSection("vehicles");
        if (s == null) return m;
        for (String id : s.getKeys(false)) {
            var c = s.getConfigurationSection(id);
            Material mat = Material.matchMaterial(c.getString("material", "GRAY_CONCRETE"));
            List<double[]> so = new ArrayList<>();
            List<?> sl = c.getList("seat-offsets");
            if (sl != null) for (Object o : sl) if (o instanceof List<?> l2) so.add(new double[]{num(l2, 0), num(l2, 1)});
            List<?> dr = c.getList("driver");
            m.put(id, new Car(id, c.getString("name", id), c.getInt("level", 1), c.getDouble("price"), c.getDouble("speed", 0.2), c.getInt("seats", 2),
                    c.getDouble("scale", 2.0), c.getString("model", "car_" + id), mat == null ? Material.GRAY_CONCRETE : mat, c.getString("ia", ""),
                    c.getInt("color", 0xFFFFFF), c.getBoolean("paint", true), c.getInt("trunk", 18), c.getBoolean("job-car", false), num(dr, 0), num(dr, 1), so));
        }
        return m;
    }

    public List<String> owned(UUID u) {
        List<String> l = new ArrayList<>();
        Object o = p.data.get(u, "cars", null);
        if (o instanceof List<?> li) for (Object x : li) l.add(String.valueOf(x));
        return l;
    }

    private static int kmhMax(Car c, int engine) { return (int) (c.speed() * (1 + 0.08 * engine) * 43 * 3.6); }
    private int colorOf(UUID u, Car c) { return ((Number) p.data.get(u, "car_color_" + c.id(), c.color())).intValue(); }
    private int engineOf(UUID u, Car c) { return ((Number) p.data.get(u, "car_eng_" + c.id(), 0)).intValue(); }
    private ItemStack icon(UUID u, Car c, String name, String... lore) {
        ItemStack it = U.withModel(U.icon(Material.PAPER, name, lore), c.model());
        return c.paint() ? U.paint(it, u == null ? c.color() : colorOf(u, c)) : it;
    }

    // ------------------------------------------------------------------ placeholder verileri
    public String carName(UUID u) { Active a = active.get(u); return a == null ? "-" : a.car.name(); }
    public int kmh(UUID u) { Active a = active.get(u); return a == null ? 0 : (int) a.kmh; }
    public int hp(UUID u) { Active a = active.get(u); return a == null ? 0 : a.hp; }
    public Location carLocation(UUID owner) { Active a = active.get(owner); return a == null ? null : a.horse.getLocation(); }
    public boolean ridesWith(Player pl, UUID owner) {
        Active a = active.get(owner);
        if (a == null) return false;
        if (a.horse.getPassengers().contains(pl)) return true;
        for (ArmorStand s : a.seats) if (s.getPassengers().contains(pl)) return true;
        return false;
    }

    // ------------------------------------------------------------------ satin alma / admin
    public void buy(Player pl, String id) {
        Car c = cars().get(id);
        UUID u = pl.getUniqueId();
        if (c == null || c.jobCar()) return;
        List<String> l = owned(u);
        if (l.contains(id)) { U.msg(pl, "Bu araca zaten sahipsin."); return; }
        if (p.data.level(u) < c.level()) { U.msg(pl, "&cBu araç için Seviye " + c.level() + " gerekli (senin seviyen: " + p.data.level(u) + ")."); return; }
        if (!p.bank.pay(u, c.price())) { U.msg(pl, "&cYetersiz bakiye: " + Placeholders.num(c.price()) + " TL (nakit + banka)"); return; }
        l.add(id); p.data.set(u, "cars", l);
        U.msg(pl, "&a" + c.name() + " satın alındı! &7Telefon > Araçlar'dan çıkarabilirsin.");
    }

    public void adminGive(CommandSender s, String name, String id, boolean give) {
        Player t = Bukkit.getPlayer(name);
        Car c = cars().get(id);
        if (t == null || c == null) { U.msg(s, "Oyuncu veya araç yok. Araçlar: " + String.join(", ", cars().keySet())); return; }
        List<String> l = owned(t.getUniqueId());
        if (give) { if (!l.contains(id)) l.add(id); } else { l.remove(id); remove(t.getUniqueId()); }
        p.data.set(t.getUniqueId(), "cars", l);
        U.msg(s, (give ? "Verildi: " : "Silindi: ") + c.name() + " -> " + t.getName());
        if (give) U.msg(t, "&aYeni aracın: &e" + c.name());
    }

    /** Mesleğe ozel arac (polis, taksi, kargo, mekanik, durumcu). */
    public void spawnJobCar(Player pl) {
        String id = p.getConfig().getString("job-vehicles." + p.data.job(pl.getUniqueId()), "");
        if (id.isEmpty() || !cars().containsKey(id)) { U.msg(pl, "Mesleğinin iş aracı yok (polis, taksici, kargocu, mekanik, dürümcü)."); return; }
        spawn(pl, id, true);
    }

    // ------------------------------------------------------------------ arac olusturma
    public void spawn(Player pl, String id, boolean free) {
        Car c = cars().get(id);
        UUID u = pl.getUniqueId();
        if (c == null) return;
        if (!(boolean) p.data.get(u, "ehliyet", false)) { U.msg(pl, "Ehliyetin yok. /ehliyet"); return; }
        if (!free && !owned(u).contains(id)) { U.msg(pl, "Bu araç senin değil."); return; }
        remove(u);
        Location l = pl.getLocation().add(pl.getLocation().getDirection().setY(0).normalize().multiply(3));
        World w = pl.getWorld();
        Active a = new Active();
        a.owner = u; a.car = c;
        a.locked = (boolean) p.data.get(u, "car_locked", true);
        a.anyone = (boolean) p.data.get(u, "car_anyone", false);
        a.hp = ((Number) p.data.get(u, "car_hp_" + id, 100)).intValue();
        a.engine = engineOf(u, c); a.color = colorOf(u, c);
        Object al = p.data.get(u, "car_allowed", null);
        if (al instanceof List<?> li) for (Object x : li) { try { a.allowed.add(UUID.fromString(String.valueOf(x))); } catch (Exception ignored) { } }
        double hs = p.getConfig().getDouble("vehicle.horse-scale", 0.25), idle = p.getConfig().getDouble("vehicle.idle-speed", 0.05);
        a.cur = idle;
        a.horse = w.spawn(l, Horse.class, h -> {
            h.setAdult(); h.setTamed(true);
            h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            h.setInvisible(true); h.setSilent(true); h.setInvulnerable(true);
            h.setRemoveWhenFarAway(false); h.setPersistent(false);
            h.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(idle);
            var sc = h.getAttribute(Attribute.SCALE);
            if (sc != null) sc.setBaseValue(hs);
        });
        a.lx = l.getX(); a.lz = l.getZ();
        try { Bukkit.getMobGoals().removeAllGoals(a.horse); } catch (Throwable ignored) { }
        ItemStack ia = U.custom(c.ia(), null);
        boolean models = p.getConfig().getBoolean("models.enabled", true);
        float sc = (float) c.scale();
        if (ia != null) a.body = display(w, l, ia, 1f, 1f, 1f);
        else if (models) {
            ItemStack it = U.withModel(new ItemStack(Material.PAPER), c.model());
            if (c.paint()) U.paint(it, a.color);
            a.body = display(w, l, it, sc, sc, sc); a.modelUsed = true;
        } else a.body = display(w, l, new ItemStack(c.mat()), 1.9f, 0.7f, 4.0f);
        a.hit = w.spawn(l, Interaction.class, i -> { i.setInteractionWidth(2.3f); i.setInteractionHeight(1.5f); i.setPersistent(false); });
        for (int k = 0; k < c.seatOff().size(); k++) {
            ArmorStand st = w.spawn(l, ArmorStand.class, s -> {
                s.setVisible(false); s.setGravity(false); s.setMarker(true); s.setSmall(true);
                s.setInvulnerable(true); s.setPersistent(false); s.setSilent(true);
            });
            a.seats.add(st);
            clickOwner.put(st.getUniqueId(), u);
        }
        active.put(u, a);
        clickOwner.put(a.horse.getUniqueId(), u);
        clickOwner.put(a.hit.getUniqueId(), u);
        a.horse.addPassenger(pl);
        U.msg(pl, "&e" + c.name() + " hazır. &7Ayar: /arac ayar  •  Bagaj: /arac bagaj  •  İnmek: shift");
        if (a.hp <= 0) U.msg(pl, "&cAraç bozuk! Bir mekanik çağır (tamir çantası ile araca sağ tık).");
    }

    private ItemDisplay display(World w, Location l, ItemStack it, float sx, float sy, float sz) {
        return w.spawn(l, ItemDisplay.class, d -> {
            d.setItemStack(it);
            d.setTransformation(new Transformation(new Vector3f(0f, 0f, 0f), new AxisAngle4f(0f, 0f, 0f, 1f), new Vector3f(sx, sy, sz), new AxisAngle4f(0f, 0f, 0f, 1f)));
            d.setPersistent(false);
            d.setTeleportDuration(1);
            d.setViewRange(2f);
        });
    }

    public void remove(UUID u) {
        Active a = active.remove(u);
        if (a == null) return;
        clickOwner.remove(a.horse.getUniqueId()); clickOwner.remove(a.hit.getUniqueId());
        for (ArmorStand s : a.seats) { clickOwner.remove(s.getUniqueId()); s.remove(); }
        a.horse.remove(); a.body.remove(); a.hit.remove();
    }

    public void removeAll() { for (UUID u : new ArrayList<>(active.keySet())) remove(u); }
    public boolean hasCar(UUID u) { return active.containsKey(u); }

    private boolean canRide(Player pl, Active a) {
        if (pl.getUniqueId().equals(a.owner)) return true;
        if (a.locked) return false;
        return a.anyone || a.allowed.contains(pl.getUniqueId());
    }

    // ------------------------------------------------------------------ ana dongu
    private void tick() {
        tickN++;
        float off = (float) p.getConfig().getDouble("models.vehicle-yaw-offset", 0);
        double seatY = p.getConfig().getDouble("vehicle.seat-y", 0.25), modelY = p.getConfig().getDouble("vehicle.model-y", 0);
        double hs = p.getConfig().getDouble("vehicle.horse-scale", 0.25), idle = p.getConfig().getDouble("vehicle.idle-speed", 0.05);
        double accel = p.getConfig().getDouble("vehicle.accel", 0.0035), brake = p.getConfig().getDouble("vehicle.brake", 0.012);
        boolean speedo = p.getConfig().getBoolean("vehicle.speedometer", true);
        for (UUID u : new ArrayList<>(active.keySet())) {
            Active a = active.get(u);
            if (!a.horse.isValid()) { remove(u); continue; }
            var sc = a.horse.getAttribute(Attribute.SCALE);
            if (sc != null && Math.abs(sc.getBaseValue() - hs) > 0.001) sc.setBaseValue(hs);
            Location h = a.horse.getLocation();
            Car c = a.car;
            // hiz olcumu, kademeli hizlanma, carpma
            double dx = h.getX() - a.lx, dz = h.getZ() - a.lz, v = Math.sqrt(dx * dx + dz * dz);
            a.lx = h.getX(); a.lz = h.getZ();
            Player drv = Bukkit.getPlayer(u);
            boolean driven = drv != null && a.horse.getPassengers().contains(drv);
            double target = c.speed() * (1 + 0.08 * a.engine) * (a.hp < 35 ? 0.6 : 1.0);
            double acc = accel * (1 + 0.15 * a.engine);
            if (driven && a.hp > 0 && a.prevV > 0.32 && v < a.prevV * 0.35) crash(a, a.prevV - v, drv);
            if (a.hp <= 0) a.cur = 0;
            else if (driven) a.cur = v > 0.012 ? Math.min(target, a.cur + acc) : Math.max(idle, a.cur - brake);
            else a.cur = idle;
            a.horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(a.cur);
            a.prevV = v; a.kmh = a.kmh * 0.6 + (v * 20 * 3.6) * 0.4;
            if (speedo && driven && tickN % 4 == 0) {
                double max = kmhMax(c, a.engine);
                int bars = (int) Math.max(0, Math.min(20, a.kmh / Math.max(1, max) * 20));
                String col = bars < 12 ? "&a" : bars < 17 ? "&e" : "&c";
                String bar = col + "▮".repeat(bars) + "&8" + "▮".repeat(20 - bars);
                drv.sendActionBar(U.L.deserialize("&f&l" + (int) a.kmh + " &7km/h  " + bar + "  &c❤ &f" + a.hp + "%  &8| &7" + c.name() + (a.hp <= 0 ? "  &4&lBOZUK" : "")));
            }
            // konumlandirma
            double r = Math.toRadians(h.getYaw()), cs = Math.cos(r), sn = Math.sin(r);
            Location center = h.clone().add(-(cs * c.dx() - sn * c.dz()), modelY, -(sn * c.dx() + cs * c.dz()));
            Location disp = center.clone();
            disp.setYaw(a.modelUsed ? h.getYaw() + off : h.getYaw());
            a.body.teleport(disp);
            a.hit.teleport(center);
            for (int k = 0; k < a.seats.size(); k++) {
                double[] so = c.seatOff().get(k);
                Location sl = h.clone().add(cs * so[0] - sn * so[1], seatY, sn * so[0] + cs * so[1]);
                sl.setYaw(h.getYaw());
                a.seats.get(k).teleport(sl, TeleportFlag.EntityState.RETAIN_PASSENGERS);
            }
            for (Entity e : new ArrayList<>(a.horse.getPassengers()))
                if (e instanceof Player pl && !pl.getUniqueId().equals(a.owner)) a.horse.removePassenger(pl);
            for (ArmorStand st : a.seats)
                for (Entity e : new ArrayList<>(st.getPassengers()))
                    if (e instanceof Player pl && !canRide(pl, a)) { st.removePassenger(pl); U.msg(pl, "&cAraçtan atıldın."); }
        }
    }

    private void crash(Active a, double drop, Player drv) {
        int dmg = (int) Math.max(3, drop * p.getConfig().getDouble("vehicle.crash-damage", 55));
        a.hp = Math.max(0, a.hp - dmg);
        a.cur = p.getConfig().getDouble("vehicle.idle-speed", 0.05);
        p.data.set(a.owner, "car_hp_" + a.car.id(), a.hp);
        a.horse.getWorld().playSound(a.horse.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.4f);
        U.msg(drv, a.hp <= 0 ? "&4Araç bozuldu! &7Mekanik çağır." : "&cKaza! Araç hasarı: -" + dmg + "% &7(kalan " + a.hp + "%)");
    }

    private void board(Player pl, Active a) {
        if (pl.getUniqueId().equals(a.owner)) {
            if (a.horse.getPassengers().contains(pl)) return;
            for (ArmorStand st : a.seats) st.removePassenger(pl);
            a.horse.addPassenger(pl);
            return;
        }
        if (!canRide(pl, a)) { U.msg(pl, "&cBu araç kilitli ya da izin verilmemiş."); return; }
        for (ArmorStand st : a.seats) if (st.getPassengers().isEmpty()) { st.addPassenger(pl); return; }
        U.msg(pl, "&cAraç dolu.");
    }

    // ------------------------------------------------------------------ mekanik: tamir
    private void repair(Player m, Active a) {
        UUID mu = m.getUniqueId();
        if (!p.data.job(mu).equals("mekanik")) { U.msg(m, "Sadece mekanikler tamir edebilir."); return; }
        if (a.hp >= 100) { U.msg(m, "Araç sağlam."); return; }
        long now = System.currentTimeMillis();
        if (a.repairing > now) return;
        a.repairing = now + 6000;
        m.sendActionBar(U.L.deserialize("&eTamir ediliyor..."));
        m.getWorld().playSound(m.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
        Bukkit.getScheduler().runTaskLater(p, () -> {
            if (!m.isOnline() || !a.horse.isValid() || m.getLocation().distance(a.horse.getLocation()) > 7) { a.repairing = 0; return; }
            int missing = 100 - a.hp;
            double cost = missing * p.getConfig().getDouble("vehicle.repair-cost-per-hp", 50);
            if (!p.bank.pay(a.owner, cost)) { U.msg(m, "&cAraç sahibinin parası yetmiyor (" + Placeholders.num(cost) + " TL)."); a.repairing = 0; return; }
            a.hp = 100;
            p.data.set(a.owner, "car_hp_" + a.car.id(), 100);
            double pay = missing * p.getConfig().getDouble("vehicle.repair-pay-per-hp", 45);
            p.data.add(mu, pay); p.data.addXp(mu, 40);
            U.msg(m, "&aTamir tamamlandı: &f+" + Placeholders.num(pay) + " TL");
            Player o = Bukkit.getPlayer(a.owner);
            if (o != null) U.msg(o, "&aAracın tamir edildi (-" + Placeholders.num(cost) + " TL).");
            m.getWorld().playSound(m.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.4f);
        }, 120L);
    }

    // ------------------------------------------------------------------ bagaj
    public void openTrunk(Player pl) {
        Active a = null;
        Active own = active.get(pl.getUniqueId());
        if (own != null && own.horse.getLocation().distance(pl.getLocation()) < 7) a = own;
        else for (Active x : active.values()) if (x.horse.getWorld().equals(pl.getWorld()) && x.horse.getLocation().distance(pl.getLocation()) < 5 && canRide(pl, x)) { a = x; break; }
        if (a == null) { U.msg(pl, "Yakınında erişebileceğin bir araç yok."); return; }
        Car c = a.car;
        int size = Math.max(9, Math.min(54, c.trunk() / 9 * 9));
        TrunkHolder th = new TrunkHolder(a.owner, c.id());
        th.inv = Bukkit.createInventory(th, size, U.L.deserialize("&8Bagaj: " + c.name()));
        Object b64 = p.data.get(a.owner, "trunk_" + c.id(), null);
        if (b64 instanceof String s && !s.isEmpty()) {
            try { ItemStack[] items = ItemStack.deserializeItemsFromBytes(Base64.getDecoder().decode(s)); th.inv.setContents(Arrays.copyOf(items, size)); } catch (Exception ignored) { }
        }
        pl.openInventory(th.inv);
        pl.playSound(pl.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1f);
    }

    @EventHandler public void onTrunkClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof TrunkHolder th)) return;
        p.data.set(th.owner, "trunk_" + th.car, Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(e.getInventory().getContents())));
    }

    // ------------------------------------------------------------------ sahip ayarlari
    public void toggleLock(Player pl) {
        Active a = active.get(pl.getUniqueId());
        boolean now = a == null ? !(boolean) p.data.get(pl.getUniqueId(), "car_locked", true) : !a.locked;
        if (a != null) a.locked = now;
        p.data.set(pl.getUniqueId(), "car_locked", now);
        U.msg(pl, now ? "&cAraç kilitli: sadece sen binebilirsin." : "&aAraç kilidi açıldı.");
    }

    public void toggleAnyone(Player pl) {
        Active a = active.get(pl.getUniqueId());
        boolean now = a == null ? !(boolean) p.data.get(pl.getUniqueId(), "car_anyone", false) : !a.anyone;
        if (a != null) a.anyone = now;
        p.data.set(pl.getUniqueId(), "car_anyone", now);
        U.msg(pl, now ? "&aKilit açıkken herkes yolcu olabilir." : "&eSadece izin verdiklerin yolcu olabilir.");
    }

    public void toggleAllow(Player pl, String name) {
        Player t = Bukkit.getPlayer(name);
        if (t == null) { U.msg(pl, "Oyuncu bulunamadı."); return; }
        Active a = active.get(pl.getUniqueId());
        Set<UUID> set = a != null ? a.allowed : new HashSet<>();
        if (a == null) { Object al = p.data.get(pl.getUniqueId(), "car_allowed", null); if (al instanceof List<?> li) for (Object x : li) set.add(UUID.fromString(String.valueOf(x))); }
        boolean added = set.add(t.getUniqueId());
        if (!added) set.remove(t.getUniqueId());
        List<String> out = new ArrayList<>(); for (UUID x : set) out.add(x.toString());
        p.data.set(pl.getUniqueId(), "car_allowed", out);
        U.msg(pl, added ? "&a" + t.getName() + " yolcu olabilir (araç kilitli değilse)." : "&e" + t.getName() + " izni kaldırıldı.");
    }

    private List<Player> passengers(Active a) {
        List<Player> l = new ArrayList<>();
        for (ArmorStand st : a.seats) for (Entity e : st.getPassengers()) if (e instanceof Player t) l.add(t);
        return l;
    }

    public void kick(Player pl, Player target) {
        Active a = active.get(pl.getUniqueId());
        if (a == null || target == null || target.equals(pl)) return;
        for (ArmorStand st : a.seats) if (st.getPassengers().contains(target)) {
            st.removePassenger(target); U.msg(target, "&cAraçtan atıldın."); U.msg(pl, target.getName() + " araçtan atıldı."); return;
        }
    }

    public void kickAll(Player pl) {
        Active a = active.get(pl.getUniqueId());
        if (a == null) return;
        for (Player t : passengers(a)) kick(pl, t);
    }

    public void openSettings(Player pl) {
        Active a = active.get(pl.getUniqueId());
        if (a == null) { U.msg(pl, "Önce bir araç çıkar: /telefon > Araçlar"); return; }
        Gui g = new Gui("aracayar", 36, "&8Araç Ayarları");
        for (int i = 0; i < 36; i++) g.put(i, U.icon(i < 9 ? Material.MAGENTA_STAINED_GLASS_PANE : i < 27 ? Material.PURPLE_STAINED_GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, " "), null);
        g.put(10, U.app("app_kilit", Material.IRON_DOOR, a.locked ? "&cKilit: KİLİTLİ" : "&aKilit: AÇIK", "&7Kilitliyken sadece sen binebilirsin", "&eTıkla: değiştir"), "lock");
        g.put(11, U.app("app_sosyal", Material.PLAYER_HEAD, a.anyone ? "&aHerkes yolcu olabilir" : "&eSadece izinliler yolcu olur", "&7Kilit açıkken geçerli", "&eTıkla: değiştir"), "anyone");
        g.put(12, U.icon(Material.CHEST, "&6Bagaj", "&7" + a.car.trunk() + " slot", "&eTıkla: aç"), "trunk");
        g.put(13, U.app("app_cete", Material.BARRIER, "&cTüm yolcuları at"), "kickall");
        List<String> names = new ArrayList<>();
        for (UUID x : a.allowed) { var op = Bukkit.getOfflinePlayer(x); names.add("&7- " + (op.getName() == null ? x.toString().substring(0, 6) : op.getName())); }
        g.put(15, U.icon(Material.BOOK, "&eİzinli yolcular", names.isEmpty() ? new String[]{"&7(boş)  /arac izin <oyuncu>"} : names.toArray(new String[0])), null);
        g.put(16, U.icon(Material.IRON_PICKAXE, "&bDurum", "&7Sağlık: &f" + a.hp + "%", "&7Motor seviyesi: &f" + a.engine, "&7Max hız: &f" + kmhMax(a.car, a.engine) + " km/h"), null);
        int s = 19;
        for (Player t : passengers(a)) {
            if (s > 26) break;
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta m = (SkullMeta) head.getItemMeta(); m.setOwningPlayer(t); m.displayName(U.L.deserialize("&e" + t.getName() + " &7(at)")); head.setItemMeta(m);
            g.put(s++, head, "kick:" + t.getName());
        }
        g.put(35, U.icon(Material.ARROW, "&7Geri"), "back");
        pl.openInventory(g.inv);
    }

    // ------------------------------------------------------------------ menuler: garaj, galeri, modifiye
    public void openGarage(Player pl) {
        UUID u = pl.getUniqueId();
        Gui g = new Gui("garaj", 36, "&8Garaj");
        for (int i = 0; i < 36; i++) g.put(i, U.icon(i < 18 ? Material.ORANGE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, " "), null);
        Map<String, Car> all = cars();
        int s = 0;
        for (String id : owned(u)) {
            Car c = all.get(id);
            if (c == null || s >= 18) continue;
            int hpv = ((Number) p.data.get(u, "car_hp_" + id, 100)).intValue();
            g.put(s++, icon(u, c, "&e" + c.name(), "&7Koltuk: &f" + c.seats() + " kişilik", "&7Hız: &f" + kmhMax(c, engineOf(u, c)) + " km/h", "&7Sağlık: &f" + hpv + "%", "&aTıkla: çıkar"), "car:" + id);
        }
        String jc = p.getConfig().getString("job-vehicles." + p.data.job(u), "");
        if (!jc.isEmpty() && all.containsKey(jc)) g.put(27, icon(null, all.get(jc), "&bİş aracı: " + all.get(jc).name(), "&7Mesleğin için ücretsiz", "&eTıkla: çıkar"), "car:" + jc);
        g.put(29, U.app("app_arac", Material.BARRIER, "&cAracı park et"), "carpark");
        g.put(30, U.icon(Material.CHEST, "&6Bagaj"), "trunk");
        g.put(31, U.app("app_kilit", Material.IRON_DOOR, "&eAraç ayarları", "&7Kilit, yolcu izni, atma"), "settings");
        g.put(35, U.icon(Material.ARROW, "&7Geri"), "back");
        pl.openInventory(g.inv);
    }

    /** Galeri NPC'si bu menuyu acar (konsol: galerimenu <oyuncu>). */
    public void openGallery(Player pl) {
        UUID u = pl.getUniqueId();
        int lv = p.data.level(u);
        Gui g = new Gui("galeri", 54, "&8&lARAÇ GALERİSİ");
        for (int i = 0; i < 54; i++) g.put(i, U.icon(i < 9 || i >= 45 ? Material.BLACK_STAINED_GLASS_PANE : (i / 9 % 2 == 1 ? Material.GRAY_STAINED_GLASS_PANE : Material.LIGHT_GRAY_STAINED_GLASS_PANE), " "), null);
        g.put(4, U.app("app_para", Material.GOLD_INGOT, "&6Nakit: &f" + Placeholders.num(p.data.money(u)) + " TL", "&7Banka: &f" + Placeholders.num(p.data.bank(u)) + " TL", "&7Seviyen: &b" + lv), null);
        List<Car> list = new ArrayList<>();
        for (Car c : cars().values()) if (!c.jobCar()) list.add(c);
        list.sort(Comparator.comparingInt(Car::level));
        int[] slots = {11, 12, 14, 15, 29, 30, 32, 33};
        List<String> mine = owned(u);
        for (int i = 0; i < list.size() && i < slots.length; i++) {
            Car c = list.get(i);
            boolean has = mine.contains(c.id()), ok = lv >= c.level();
            g.put(slots[i], icon(null, c, (has ? "&a" : ok ? "&e" : "&c") + c.name(),
                    "&8━━━━━━━━━━━━━━",
                    "&7Seviye: " + (ok ? "&a✔ Lv." : "&c✖ Lv.") + c.level(),
                    "&7Koltuk: &f" + c.seats() + " kişilik",
                    "&7Hız: &f" + kmhMax(c, 0) + " km/h",
                    "&7Bagaj: &f" + c.trunk() + " slot",
                    "&7Fiyat: &6" + Placeholders.num(c.price()) + " TL",
                    "&8━━━━━━━━━━━━━━",
                    has ? "&a✔ Sahipsin" : ok ? "&eTıkla: satın al" : "&cSeviye " + c.level() + " gerekli"),
                    (!has && ok) ? "pick:" + c.id() : null);
        }
        g.put(49, U.app("app_kapat", Material.BARRIER, "&cKapat"), "close");
        pl.openInventory(g.inv);
    }

    private void openConfirm(Player pl, String id) {
        Car c = cars().get(id);
        if (c == null) return;
        Gui g = new Gui("galeri-onay", 27, "&8Satın alma onayı");
        for (int i = 0; i < 27; i++) g.put(i, U.icon(Material.BLACK_STAINED_GLASS_PANE, " "), null);
        g.put(13, icon(null, c, "&e" + c.name(), "&7Koltuk: &f" + c.seats(), "&7Hız: &f" + kmhMax(c, 0) + " km/h", "&7Fiyat: &6" + Placeholders.num(c.price()) + " TL"), null);
        g.put(11, U.icon(Material.LIME_CONCRETE, "&a&lSATIN AL", "&7-" + Placeholders.num(c.price()) + " TL"), "yes:" + id);
        g.put(15, U.icon(Material.RED_CONCRETE, "&c&lİPTAL"), "no");
        pl.openInventory(g.inv);
    }

    /** Modifiyeci NPC'si bu menuyu acar (konsol: modifiyemenu <oyuncu>). Boya + motor. */
    public void openModify(Player pl) {
        UUID u = pl.getUniqueId();
        Gui g = new Gui("modifiye", 36, "&8&lMODİFİYE");
        for (int i = 0; i < 36; i++) g.put(i, U.icon(i < 9 || i >= 27 ? Material.BLACK_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, " "), null);
        g.put(4, U.app("app_para", Material.GOLD_INGOT, "&6Nakit: &f" + Placeholders.num(p.data.money(u)) + " TL", "&7Banka: &f" + Placeholders.num(p.data.bank(u)) + " TL"), null);
        int s = 10;
        for (String id : owned(u)) {
            Car c = cars().get(id);
            if (c == null || !c.paint() || s > 25) continue;
            g.put(s++, icon(u, c, "&e" + c.name(), "&7Motor seviyesi: &f" + engineOf(u, c), "&eTıkla: boya / motor"), "mc:" + id);
        }
        if (s == 10) g.put(13, U.icon(Material.BARRIER, "&cModifiye edilecek aracın yok"), null);
        g.put(31, U.app("app_kapat", Material.BARRIER, "&cKapat"), "close");
        pl.openInventory(g.inv);
    }

    private void openModifyCar(Player pl, String id) {
        UUID u = pl.getUniqueId();
        Car c = cars().get(id);
        if (c == null) return;
        Gui g = new Gui("modifiye-araba", 54, "&8Modifiye: " + c.name());
        for (int i = 0; i < 54; i++) g.put(i, U.icon(i < 9 || i >= 45 ? Material.BLACK_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, " "), null);
        g.put(4, icon(u, c, "&e" + c.name(), "&7Mevcut renk ve motor"), null);
        double price = p.getConfig().getDouble("tuning.paint-price", 5000);
        var cols = p.getConfig().getConfigurationSection("tuning.colors");
        int s = 9;
        if (cols != null) for (String nm : cols.getKeys(false)) {
            if (s >= 36) break;
            int rgb = parseHex(cols.getString(nm, "#FFFFFF"));
            ItemStack dye = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            LeatherArmorMeta lm = (LeatherArmorMeta) dye.getItemMeta();
            lm.setColor(Color.fromRGB(rgb)); lm.displayName(U.L.deserialize("&f" + nm).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            lm.lore(List.of(U.L.deserialize("&7Boya: &6" + Placeholders.num(price) + " TL").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)));
            lm.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES, org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            dye.setItemMeta(lm);
            g.put(s++, dye, "col:" + id + ":" + rgb);
        }
        int eng = engineOf(u, c);
        List<Integer> prices = p.getConfig().getIntegerList("tuning.engine-prices");
        if (eng < prices.size()) g.put(47, U.icon(Material.BLAZE_POWDER, "&c&lMotor paketi " + (eng + 1), "&7Hız +%8, ivmelenme +%15", "&7Fiyat: &6" + Placeholders.num(prices.get(eng)) + " TL", "&eTıkla: satın al"), "eng:" + id);
        else g.put(47, U.icon(Material.BLAZE_POWDER, "&aMotor tam güçte (seviye " + eng + ")"), null);
        g.put(49, U.icon(Material.ARROW, "&7Geri"), "mback");
        pl.openInventory(g.inv);
    }

    private static int parseHex(String h) { try { return Integer.parseInt(h.replace("#", ""), 16); } catch (Exception e) { return 0xFFFFFF; } }

    private void refreshActive(UUID u) {
        Active a = active.get(u);
        if (a == null) return;
        a.engine = engineOf(u, a.car); a.color = colorOf(u, a.car);
        if (a.modelUsed) { ItemStack it = U.withModel(new ItemStack(Material.PAPER), a.car.model()); if (a.car.paint()) U.paint(it, a.color); a.body.setItemStack(it); }
    }

    @EventHandler public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Gui g)) return;
        if (!g.page.equals("garaj") && !g.page.equals("galeri") && !g.page.equals("galeri-onay") && !g.page.equals("aracayar") && !g.page.equals("modifiye") && !g.page.equals("modifiye-araba")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player pl)) return;
        String a = g.act.get(e.getRawSlot());
        if (a == null) return;
        UUID u = pl.getUniqueId();
        if (a.startsWith("car:")) { pl.closeInventory(); spawn(pl, a.substring(4), a.substring(4).equals(p.getConfig().getString("job-vehicles." + p.data.job(u), "-"))); }
        else if (a.equals("carpark")) { remove(u); pl.closeInventory(); }
        else if (a.equals("trunk")) { pl.closeInventory(); openTrunk(pl); }
        else if (a.equals("settings")) openSettings(pl);
        else if (a.equals("lock")) { toggleLock(pl); openSettings(pl); }
        else if (a.equals("anyone")) { toggleAnyone(pl); openSettings(pl); }
        else if (a.equals("kickall")) { kickAll(pl); openSettings(pl); }
        else if (a.startsWith("kick:")) { kick(pl, Bukkit.getPlayer(a.substring(5))); openSettings(pl); }
        else if (a.equals("back")) p.phone.open(pl);
        else if (a.equals("close")) pl.closeInventory();
        else if (a.startsWith("pick:")) openConfirm(pl, a.substring(5));
        else if (a.equals("no")) openGallery(pl);
        else if (a.startsWith("yes:")) { buy(pl, a.substring(4)); openGallery(pl); }
        else if (a.startsWith("mc:")) openModifyCar(pl, a.substring(3));
        else if (a.equals("mback")) openModify(pl);
        else if (a.startsWith("col:")) {
            String[] sp = a.split(":");
            double price = p.getConfig().getDouble("tuning.paint-price", 5000);
            if (!p.bank.pay(u, price)) { U.msg(pl, "&cYetersiz bakiye."); return; }
            p.data.set(u, "car_color_" + sp[1], Integer.parseInt(sp[2]));
            refreshActive(u);
            U.msg(pl, "&aAraç boyandı! (-" + Placeholders.num(price) + " TL)");
            openModifyCar(pl, sp[1]);
        } else if (a.startsWith("eng:")) {
            String id = a.substring(4);
            Car c = cars().get(id);
            int eng = engineOf(u, c);
            List<Integer> prices = p.getConfig().getIntegerList("tuning.engine-prices");
            if (c == null || eng >= prices.size()) return;
            if (!p.bank.pay(u, prices.get(eng))) { U.msg(pl, "&cYetersiz bakiye."); return; }
            p.data.set(u, "car_eng_" + id, eng + 1);
            refreshActive(u);
            U.msg(pl, "&aMotor paketi " + (eng + 1) + " takıldı!");
            openModifyCar(pl, id);
        }
    }

    // ------------------------------------------------------------------ olaylar
    @EventHandler public void onMount(EntityMountEvent e) {
        UUID owner = clickOwner.get(e.getMount().getUniqueId());
        if (owner == null || !(e.getEntity() instanceof Player pl)) return;
        Active a = active.get(owner);
        if (a == null) return;
        boolean isHorse = e.getMount().getUniqueId().equals(a.horse.getUniqueId());
        if (isHorse ? !pl.getUniqueId().equals(owner) : !canRide(pl, a)) { e.setCancelled(true); U.msg(pl, "&cBu araca binemezsin (kilitli ya da izin yok)."); }
    }

    @EventHandler public void onInteractCar(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        UUID owner = clickOwner.get(e.getRightClicked().getUniqueId());
        if (owner == null) return;
        e.setCancelled(true);
        Active a = active.get(owner);
        if (a == null) return;
        Player pl = e.getPlayer();
        if ("mek_cantasi".equals(U.tagOf(pl.getInventory().getItemInMainHand()))) { repair(pl, a); return; }
        board(pl, a);
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) { remove(e.getPlayer().getUniqueId()); }

    @EventHandler public void onShopBlock(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof TileState ts)) return;
        String t = ts.getPersistentDataContainer().get(U.SHOP, PersistentDataType.STRING);
        if (t == null) return;
        e.setCancelled(true);
        if (t.equals("jenerator")) U.msg(e.getPlayer(), "Jeneratör almak için kendi arsanda &e/jenerator al &fyaz.");
    }

    @SuppressWarnings("unused") private static Component unused() { return Component.empty(); }
}

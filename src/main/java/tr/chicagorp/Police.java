package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;

/** Polis: meslek kiti (uniforma/zirh, cop, kelepce, Glock), kelepce mekanigi, tutuklama. */
public class Police implements Listener {
    private final ChicagoRP p;
    private final Map<UUID, Long> cuffed = new HashMap<>();

    public Police(ChicagoRP p) {
        this.p = p;
        Bukkit.getScheduler().runTaskTimer(p, () -> {
            long now = System.currentTimeMillis();
            for (UUID u : new ArrayList<>(cuffed.keySet())) {
                Player t = Bukkit.getPlayer(u);
                if (t == null || cuffed.get(u) < now) { cuffed.remove(u); if (t != null) U.msg(t, "Kelepçen çözüldü."); }
            }
        }, 20L, 20L);
    }

    public boolean isPolice(Player pl) { return p.data.job(pl.getUniqueId()).equals("polis"); }
    public boolean isCuffed(Player pl) { return cuffed.containsKey(pl.getUniqueId()); }

    private static boolean policeItem(ItemStack it) {
        String t = U.tagOf(it);
        return t != null && (t.startsWith("pol_") || t.equals("zirh_polis"));
    }

    /** Polis meslegine girince verilen kit. */
    public void giveKit(Player pl) {
        takeKit(pl);
        PlayerInventory inv = pl.getInventory();
        ItemStack[] arm = Armors.make(p, "polis");
        if (arm != null) {
            String[] part = {"POLİS Şapka", "POLİS Gömlek", "POLİS Pantolon", "POLİS Bot"};
            ItemStack[] cur = {inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()};
            for (int i = 0; i < 4; i++) {
                var im = arm[i].getItemMeta();
                im.displayName(U.L.deserialize("&9&l" + part[i]).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
                arm[i].setItemMeta(im);
            }
            if (cur[0] == null) inv.setHelmet(arm[0]); else inv.addItem(arm[0]);
            if (cur[1] == null) inv.setChestplate(arm[1]); else inv.addItem(arm[1]);
            if (cur[2] == null) inv.setLeggings(arm[2]); else inv.addItem(arm[2]);
            if (cur[3] == null) inv.setBoots(arm[3]); else inv.addItem(arm[3]);
        }
        inv.addItem(U.withModel(U.item(Material.STICK, "&9Cop &7(polis)", "pol_cop"), "cop"));
        inv.addItem(U.withModel(U.item(Material.IRON_NUGGET, "&bKelepçe &7(oyuncuya sağ tık)", "pol_kelepce"), "kelepce"));
        ItemStack gun = p.weapons.make("glock");
        if (gun != null) {
            var im = gun.getItemMeta();
            im.getPersistentDataContainer().set(U.TAG, PersistentDataType.STRING, "pol_gun");
            gun.setItemMeta(im);
            inv.addItem(gun);
            inv.addItem(p.weapons.ammo("pistol", 34));
        }
        U.msg(pl, "&9Polis teçhizatın verildi: zırh, cop, kelepçe, Glock.");
    }

    /** Meslekten ayrilinca polis esyalarini siler. */
    public void takeKit(Player pl) {
        PlayerInventory inv = pl.getInventory();
        for (int i = 0; i < inv.getSize(); i++) if (policeItem(inv.getItem(i))) inv.setItem(i, null);
        if (policeItem(inv.getHelmet())) inv.setHelmet(null);
        if (policeItem(inv.getChestplate())) inv.setChestplate(null);
        if (policeItem(inv.getLeggings())) inv.setLeggings(null);
        if (policeItem(inv.getBoots())) inv.setBoots(null);
        cuffed.remove(pl.getUniqueId());
    }

    public void kit(Player pl) {
        if (!isPolice(pl)) { U.msg(pl, "Polis değilsin."); return; }
        giveKit(pl);
    }

    public void arrest(Player pl, String[] a) {
        if (!isPolice(pl) || a.length == 0) { U.msg(pl, "/tutukla <oyuncu> (sadece polis)"); return; }
        Player t = Bukkit.getPlayer(a[0]);
        if (t == null || t.getLocation().distance(pl.getLocation()) > 4) { U.msg(pl, "Hedef yakında değil."); return; }
        var j = p.getConfig().getConfigurationSection("jail");
        var w = Bukkit.getWorld(j.getString("world"));
        if (w == null) return;
        cuffed.remove(t.getUniqueId());
        t.teleport(new Location(w, j.getDouble("x"), j.getDouble("y"), j.getDouble("z")));
        UUID tu = t.getUniqueId();
        p.data.set(tu, "sabika", (int) p.data.get(tu, "sabika", 0) + 1);
        p.data.add(pl.getUniqueId(), p.getConfig().getDouble("arrest-reward"));
        p.data.addXp(pl.getUniqueId(), 100);
        U.msg(pl, "Tutukladın: &a+" + p.getConfig().getInt("arrest-reward") + " TL");
        U.msg(t, "Tutuklandın.");
        long sec = p.getConfig().getLong("arrest-seconds");
        Bukkit.getScheduler().runTaskLater(p, () -> {
            if (!t.isOnline()) return;
            var s = p.getConfig().getConfigurationSection("spawn");
            t.teleport(new Location(Bukkit.getWorld(s.getString("world")), s.getDouble("x"), s.getDouble("y"), s.getDouble("z")));
            U.msg(t, "Serbest bırakıldın.");
        }, sec * 20L);
    }

    // ---------------------------------------------------------------- kelepce
    @EventHandler public void onCuff(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || !(e.getRightClicked() instanceof Player t)) return;
        Player pl = e.getPlayer();
        if (!"pol_kelepce".equals(U.tagOf(pl.getInventory().getItemInMainHand()))) return;
        e.setCancelled(true);
        if (!isPolice(pl)) return;
        if (pl.getLocation().distance(t.getLocation()) > 3.5) { U.msg(pl, "Çok uzaksın."); return; }
        if (cuffed.containsKey(t.getUniqueId())) {
            cuffed.remove(t.getUniqueId());
            U.msg(pl, "&e" + t.getName() + " kelepçesi çözüldü."); U.msg(t, "Kelepçen çözüldü.");
        } else {
            cuffed.put(t.getUniqueId(), System.currentTimeMillis() + p.getConfig().getLong("police.cuff-seconds", 120) * 1000);
            U.msg(pl, "&a" + t.getName() + " kelepçelendi."); U.msg(t, "&cKelepçelendin! Hareket edemezsin.");
            t.getWorld().playSound(t.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1f, 1f);
        }
    }

    @EventHandler public void onMove(PlayerMoveEvent e) {
        if (!cuffed.containsKey(e.getPlayer().getUniqueId())) return;
        Location f = e.getFrom(), t = e.getTo();
        if (t != null && (f.getX() != t.getX() || f.getZ() != t.getZ())) { t.setX(f.getX()); t.setZ(f.getZ()); e.setTo(t); }
    }

    @EventHandler public void onCuffedInteract(PlayerInteractEvent e) { if (isCuffed(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onCuffedDrop(PlayerDropItemEvent e) { if (isCuffed(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(priority = EventPriority.LOWEST) public void onCuffedCmd(PlayerCommandPreprocessEvent e) {
        if (isCuffed(e.getPlayer()) && !e.getPlayer().hasPermission("rp.admin")) { e.setCancelled(true); U.msg(e.getPlayer(), "&cKelepçeliyken komut kullanamazsın."); }
    }
    @EventHandler(priority = EventPriority.LOWEST) public void onCuffedAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player d && isCuffed(d)) e.setCancelled(true);
    }
    @EventHandler public void onQuit(PlayerQuitEvent e) { cuffed.remove(e.getPlayer().getUniqueId()); }
}

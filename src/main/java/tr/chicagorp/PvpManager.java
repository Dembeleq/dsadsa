package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.*;

public class PvpManager implements Listener {
    private final ChicagoRP p;
    private final Set<UUID> off = new HashSet<>();
    private final Map<UUID, Long> cd = new HashMap<>();
    private final Map<UUID, Long> combat = new HashMap<>();
    private static final Set<String> LOCKED = Set.of("torbaci", "kacakci", "soyguncu");

    public PvpManager(ChicagoRP p) { this.p = p; }

    public boolean isOff(UUID u) { return off.contains(u); }

    public boolean inSpawn(Location l) {
        var s = p.getConfig().getConfigurationSection("spawn");
        if (l.getWorld() == null || !l.getWorld().getName().equals(s.getString("world"))) return false;
        double dx = l.getX() - s.getDouble("x"), dz = l.getZ() - s.getDouble("z");
        return Math.sqrt(dx * dx + dz * dz) <= s.getDouble("radius");
    }

    public void toggle(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        if (a.length == 0) { U.msg(pl, "/pvp ac | kapat"); return; }
        long wait = p.getConfig().getLong("pvp-cooldown-minutes") * 60000L;
        if (cd.getOrDefault(u, 0L) + wait > System.currentTimeMillis()) { U.msg(pl, "Her degisiklikten sonra bekleme var."); return; }
        if (a[0].equals("kapat")) {
            if (!inSpawn(pl.getLocation())) { U.msg(pl, "Sadece spawn'da kapatabilirsin."); return; }
            if (LOCKED.contains(p.data.job(u))) { U.msg(pl, "Yasa disi meslekte PvP kapatilamaz."); return; }
            off.add(u);
        } else off.remove(u);
        cd.put(u, System.currentTimeMillis());
        U.msg(pl, "PvP " + (off.contains(u) ? "kapali" : "acik"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player v)) return;
        Player at = null;
        if (e.getDamager() instanceof Player x) at = x;
        else if (e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Player x) at = x;
        if (at == null || at == v) return;
        if (p.gang.same(at.getUniqueId(), v.getUniqueId())) { e.setCancelled(true); return; }
        if (off.contains(v.getUniqueId()) || off.contains(at.getUniqueId())) { e.setCancelled(true); return; }
        if (p.police.isPolice(at) && inSpawn(v.getLocation())) { e.setCancelled(true); return; }
        long now = System.currentTimeMillis();
        combat.put(v.getUniqueId(), now); combat.put(at.getUniqueId(), now);
    }

    @EventHandler public void onDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity(), k = dead.getKiller();
        combat.remove(dead.getUniqueId());
        if (k == null) return;
        combat.remove(k.getUniqueId());           // oldurunce combat etiketi biter
        if (p.police.isPolice(k)) e.getDrops().clear(); // polis oldurunce esya yok olur, para yok
    }

    public boolean inCombat(Player pl) { return combat.getOrDefault(pl.getUniqueId(), 0L) + 15000L > System.currentTimeMillis(); }
}

package tr.chicagorp;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.*;

/** Ekranin ustundeki boss bar. Metin config'ten ({level} {money} {gang} ... veya %PAPI%). */
public class BossBars implements Listener {
    private final ChicagoRP p;
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private final Set<UUID> off = new HashSet<>();

    public BossBars(ChicagoRP p) {
        this.p = p;
        Bukkit.getScheduler().runTaskTimer(p, this::tick, 20L, 20L);
    }

    public boolean toggle(Player pl) {
        UUID u = pl.getUniqueId();
        boolean nowOff = off.add(u);
        if (!nowOff) off.remove(u);
        else hide(pl);
        return !nowOff;
    }

    private void hide(Player pl) { BossBar b = bars.remove(pl.getUniqueId()); if (b != null) pl.hideBossBar(b); }
    public void reload() { for (Player pl : Bukkit.getOnlinePlayers()) hide(pl); }

    private <T extends Enum<T>> T en(Class<T> c, String v, T def) { try { return Enum.valueOf(c, v.toUpperCase()); } catch (Exception e) { return def; } }

    private void tick() {
        var c = p.getConfig().getConfigurationSection("bossbar");
        boolean on = c != null && c.getBoolean("enabled", true);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            UUID u = pl.getUniqueId();
            if (!on || off.contains(u)) { hide(pl); continue; }
            String text = Placeholders.apply(p, pl, c.getString("text", "&6{server}"));
            float prog = 1f;
            String mode = c.getString("progress", "xp");
            if (mode.equals("xp")) prog = (p.data.xp(u) % 1000) / 1000f;
            else if (mode.equals("health")) prog = (float) Math.max(0, Math.min(1, pl.getHealth() / pl.getMaxHealth()));
            BossBar b = bars.get(u);
            if (b == null) {
                b = BossBar.bossBar(U.L.deserialize(text), prog, en(BossBar.Color.class, c.getString("color", "PURPLE"), BossBar.Color.PURPLE),
                        en(BossBar.Overlay.class, c.getString("style", "PROGRESS"), BossBar.Overlay.PROGRESS));
                bars.put(u, b);
                pl.showBossBar(b);
            } else { b.name(U.L.deserialize(text)); b.progress(Math.max(0f, Math.min(1f, prog))); }
        }
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) { bars.remove(e.getPlayer().getUniqueId()); }
}

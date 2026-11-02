package tr.chicagorp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class JobManager implements Listener {
    private final ChicagoRP p;
    public static final List<String> ALL = List.of("ciftci", "kimyager", "torbaci", "demirci", "kargocu",
            "silah_ustasi", "balikci", "mekanik", "taksici", "durumcu", "polis", "kacakci", "soyguncu");

    public JobManager(ChicagoRP p) { this.p = p; }

    /** Meslegin ne yaptigini ve nasil yapildigini gosterir (config: jobs.<id>.desc / how). */
    public void info(Player pl, String j) {
        var c = p.getConfig().getConfigurationSection("jobs." + j);
        if (c == null) { U.msg(pl, "Böyle bir meslek yok."); return; }
        int cap = capacity(j);
        pl.sendMessage(U.L.deserialize("&8&m                    &r &6&l" + j.toUpperCase() + " &8&m                    "));
        pl.sendMessage(U.L.deserialize("&7" + c.getString("desc", "")));
        pl.sendMessage(U.L.deserialize("&7Kontenjan: &f" + p.data.countJob(j) + "/" + (cap < 0 ? "∞" : cap) + "  &7Kazanç: &a" + c.getString("income", "-")));
        pl.sendMessage(U.L.deserialize("&e&lNasıl yapılır?"));
        int i = 1;
        for (String line : c.getStringList("how")) pl.sendMessage(U.L.deserialize("&8" + (i++) + ". &f" + line));
    }

    /** config job-capacity.<meslek>; -1 = sinirsiz */
    public int capacity(String j) { return p.getConfig().getInt("job-capacity." + j, -1); }

    public void command(Player pl, String[] a) {
        if (a.length >= 1 && a[0].equalsIgnoreCase("bilgi")) { if (a.length > 1) info(pl, a[1].toLowerCase()); else U.msg(pl, "/meslek bilgi <meslek>  (" + String.join(", ", ALL) + ")"); return; }
        if (a.length == 0) { U.msg(pl, "Meslekler: " + String.join(", ", ALL) + " | Mevcut: " + p.data.job(pl.getUniqueId())); return; }
        String j = a[0].toLowerCase();
        if (j.equals("birak")) { if (p.data.job(pl.getUniqueId()).equals("polis")) p.police.takeKit(pl); p.data.set(pl.getUniqueId(), "job", "yok"); U.msg(pl, "Mesleği bıraktın."); return; }
        if (!ALL.contains(j) && !j.equals("yok")) { U.msg(pl, "Boyle bir meslek yok."); return; }
        if (j.equals("polis") && !pl.hasPermission("rp.job.polis")) { U.msg(pl, "Polis icin destek talebi acip mulakata girmelisin."); return; }
        int cap = capacity(j);
        if (!j.equals("yok") && cap >= 0 && !j.equals(p.data.job(pl.getUniqueId())) && p.data.countJob(j) >= cap) {
            U.msg(pl, "&c" + j + " mesleğinin kontenjanı dolu (" + p.data.countJob(j) + "/" + cap + ").");
            return;
        }
        String prev = p.data.job(pl.getUniqueId());
        p.data.set(pl.getUniqueId(), "job", j);
        U.msg(pl, "Mesleğin: &e" + j + " &7(detay: /meslek bilgi " + j + ")");
        p.work.onJobChange(pl, prev, j);
        if (j.equals("polis") && !prev.equals("polis")) p.police.giveKit(pl);
        else if (prev.equals("polis") && !j.equals("polis")) p.police.takeKit(pl);
    }

    @EventHandler public void onFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player pl = e.getPlayer();
        if (!p.data.job(pl.getUniqueId()).equals("balikci")) return;
        var c = p.getConfig().getConfigurationSection("fishing");
        int pay = ThreadLocalRandom.current().nextInt(c.getInt("min"), c.getInt("max") + 1);
        p.data.add(pl.getUniqueId(), pay); p.data.addXp(pl.getUniqueId(), 20);
        U.msg(pl, "Balik sattin: &a+" + pay + " TL");
    }
}

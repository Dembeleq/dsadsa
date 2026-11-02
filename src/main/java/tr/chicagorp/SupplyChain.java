package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.*;

/** Ciftci -> ham madde NPC (stok) -> kimyager -> el alti NPC (stok) -> torbaci -> bagimli. Oyun ici kurgusal esyalar. */
public class SupplyChain {
    private final ChicagoRP p;
    private int rawStock = 0, goodsStock = 0;
    private final Map<UUID, Long> rentUntil = new HashMap<>(), plantedAt = new HashMap<>(), refineAt = new HashMap<>();

    public SupplyChain(ChicagoRP p) { this.p = p; }

    public void command(String cmd, Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        var c = p.getConfig().getConfigurationSection("chain");
        long now = System.currentTimeMillis();
        String sub = a.length > 0 ? a[0] : "";
        switch (cmd) {
            case "tarla" -> {
                if (!p.data.job(u).equals("ciftci")) { U.msg(pl, "Ciftci degilsin."); return; }
                switch (sub) {
                    case "kirala" -> {
                        if (!p.data.take(u, c.getDouble("field-rent-price"))) { U.msg(pl, "Yetersiz bakiye."); return; }
                        rentUntil.put(u, now + 24L * 3600_000L); U.msg(pl, "Tarla 24 saat kiralandi.");
                    }
                    case "ek" -> { if (rentUntil.getOrDefault(u, 0L) < now) { U.msg(pl, "Once tarla kirala."); return; } plantedAt.put(u, now); U.msg(pl, "Ekildi."); }
                    case "topla" -> {
                        Long t = plantedAt.get(u);
                        if (t == null || t + c.getLong("grow-seconds") * 1000 > now) { U.msg(pl, "Henuz hazir degil."); return; }
                        plantedAt.remove(u);
                        for (int i = 0; i < c.getInt("raw-per-harvest"); i++) pl.getInventory().addItem(U.item(Material.WHEAT_SEEDS, "&7Ham Madde", "ham"));
                        U.msg(pl, "Hasat edildi. /tarla sat ile ham madde NPC'sine sat.");
                    }
                    case "sat" -> {
                        int n = U.count(pl, "ham"); if (n == 0) return;
                        U.remove(pl, "ham", n); rawStock += n;
                        p.data.add(u, n * c.getDouble("raw-sell-price")); p.data.addXp(u, n * 5L);
                        U.msg(pl, n + " ham madde satildi.");
                    }
                }
            }
            case "kimya" -> {
                if (!p.data.job(u).equals("kimyager")) { U.msg(pl, "Kimyager degilsin."); return; }
                switch (sub) {
                    case "al" -> {
                        int n = Math.min(10, rawStock); if (n == 0) { U.msg(pl, "Stok yok."); return; }
                        if (!p.bank.pay(u, n * c.getDouble("raw-buy-price"))) { U.msg(pl, "Yetersiz bakiye."); return; }
                        rawStock -= n; for (int i = 0; i < n; i++) pl.getInventory().addItem(U.item(Material.WHEAT_SEEDS, "&7Ham Madde", "ham"));
                    }
                    case "yap" -> {
                        if (U.count(pl, "ham") == 0) { U.msg(pl, "Ham madde yok."); return; }
                        if (refineAt.getOrDefault(u, 0L) > now) { U.msg(pl, "Uretim suruyor."); return; }
                        int n = U.count(pl, "ham"); U.remove(pl, "ham", n);
                        refineAt.put(u, now + c.getLong("refine-seconds") * 1000);
                        Bukkit.getScheduler().runTaskLater(p, () -> {
                            for (int i = 0; i < n; i++) pl.getInventory().addItem(U.item(Material.SUGAR, "&cYasakli Madde", "madde"));
                            U.msg(pl, "Uretim bitti. /kimya sat");
                        }, c.getLong("refine-seconds") * 20L);
                    }
                    case "sat" -> {
                        int n = U.count(pl, "madde"); if (n == 0) return;
                        U.remove(pl, "madde", n); goodsStock += n; p.data.add(u, n * c.getDouble("product-sell-price", 280));
                        U.msg(pl, "El alti NPC'sine " + n + " adet satildi.");
                    }
                }
            }
            case "elalti" -> {
                if (!p.data.job(u).equals("torbaci")) { U.msg(pl, "Torbaci degilsin."); return; }
                if (sub.equals("al")) {
                    int n = Math.min(5, goodsStock); if (n == 0) { U.msg(pl, "Stok yok."); return; }
                    if (!p.bank.pay(u, n * c.getDouble("dealer-buy-price", 280))) { U.msg(pl, "Yetersiz bakiye."); return; }
                    goodsStock -= n; for (int i = 0; i < n; i++) pl.getInventory().addItem(U.item(Material.SUGAR, "&cYasakli Madde", "madde"));
                } else if (sub.equals("teslim") && a.length > 1) {
                    Player t = Bukkit.getPlayer(a[1]);
                    if (t == null || t.getLocation().distance(pl.getLocation()) > 4 || U.count(pl, "madde") == 0) { U.msg(pl, "Teslim edilemedi."); return; }
                    U.remove(pl, "madde", 1);
                    p.data.add(u, c.getDouble("delivery-pay")); p.data.addXp(u, 30);
                    U.msg(pl, "Teslim edildi.");
                }
            }
        }
    }
}

package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Zirh zinciri: oyuncu -> demirci -> kargocu -> uretim -> oyuncu. */
public class OrderSystem {
    enum State { WAIT_SMITH, WAIT_CARGO, CARGO_ENROUTE, PRODUCING, READY }
    static class Order { int id; UUID customer, smith, cargo; String armor; State st = State.WAIT_SMITH; long readyAt; }

    private final ChicagoRP p;
    private final Map<Integer, Order> orders = new LinkedHashMap<>();
    private int seq = 1;

    public OrderSystem(ChicagoRP p) { this.p = p; }

    private Player randomWith(String job, UUID except) {
        List<Player> l = new ArrayList<>();
        for (Player x : Bukkit.getOnlinePlayers()) if (p.data.job(x.getUniqueId()).equals(job) && !x.getUniqueId().equals(except)) l.add(x);
        return l.isEmpty() ? null : l.get(new Random().nextInt(l.size()));
    }

    public void command(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        if (a.length == 0) { U.msg(pl, "/siparis ver <zirh> | kabul <id> | kargo <id> | teslimmat <id> | teslim | liste"); return; }
        switch (a[0]) {
            case "liste" -> {
                for (Order o : orders.values()) U.msg(pl, "#" + o.id + " " + o.armor + " " + o.st);
            }
            case "ver" -> {
                var c = p.getConfig().getConfigurationSection("armors." + (a.length > 1 ? a[1] : ""));
                if (c == null || c.getBoolean("police-only", false)) {
                    StringBuilder sb = new StringBuilder();
                    for (String k : p.getConfig().getConfigurationSection("armors").getKeys(false))
                        if (!p.getConfig().getBoolean("armors." + k + ".police-only", false)) sb.append(k).append(" (").append(p.getConfig().getInt("armors." + k + ".price")).append(" TL)  ");
                    U.msg(pl, "Zırhlar: " + sb); return;
                }
                Player smith = randomWith("demirci", u);
                if (smith == null) { U.msg(pl, "Aktif demirci yok."); return; }
                if (!p.bank.pay(u, c.getDouble("price"))) { U.msg(pl, "Yetersiz bakiye."); return; }
                Order o = new Order(); o.id = seq++; o.customer = u; o.armor = a[1]; orders.put(o.id, o);
                U.msg(smith, "Yeni zirh siparisi #" + o.id + " (" + o.armor + ") -> /siparis kabul " + o.id);
                U.msg(pl, "Siparis #" + o.id + " iletildi.");
            }
            case "kabul" -> {
                Order o = get(pl, a); if (o == null || o.st != State.WAIT_SMITH) return;
                for (Order x : orders.values()) if (u.equals(x.smith) && (x.st == State.PRODUCING || x.st == State.CARGO_ENROUTE || x.st == State.WAIT_CARGO)) { U.msg(pl, "Ayni anda tek is yapabilirsin."); return; }
                o.smith = u; o.st = State.WAIT_CARGO;
                Player cargo = randomWith("kargocu", u);
                if (cargo == null) { o.st = State.PRODUCING; o.readyAt = System.currentTimeMillis() + (p.getConfig().getLong("orders.prep-seconds") + p.getConfig().getLong("armors." + o.armor + ".produce")) * 1000; U.msg(pl, "Kargocu yok, uretim otomatik basladi."); return; }
                U.msg(cargo, "Malzeme teslimati #" + o.id + " -> /siparis kargo " + o.id);
                U.msg(pl, "Kargocu araniyor.");
            }
            case "kargo" -> {
                Order o = get(pl, a); if (o == null || o.st != State.WAIT_CARGO) return;
                o.cargo = u; o.st = State.CARGO_ENROUTE;
                Player s = Bukkit.getPlayer(o.smith);
                U.msg(pl, "Malzemeyi demirciye gotur (" + (s == null ? "?" : s.getName()) + "), yanina gidip /siparis teslimmat " + o.id);
            }
            case "teslimmat" -> {
                Order o = get(pl, a); if (o == null || o.st != State.CARGO_ENROUTE || !u.equals(o.cargo)) return;
                Player s = Bukkit.getPlayer(o.smith);
                if (s == null || s.getLocation().distance(pl.getLocation()) > 5) { U.msg(pl, "Demirciye yaklasmalisin (ısınlanma yok)."); return; }
                p.data.add(u, p.getConfig().getDouble("orders.cargo-pay"));
                o.st = State.PRODUCING;
                o.readyAt = System.currentTimeMillis() + (p.getConfig().getLong("orders.prep-seconds") + p.getConfig().getLong("armors." + o.armor + ".produce")) * 1000;
                U.msg(s, "Malzeme geldi, uretim basladi."); U.msg(pl, "Teslim edildi.");
            }
            case "teslim" -> {
                Iterator<Order> it = orders.values().iterator();
                while (it.hasNext()) {
                    Order o = it.next();
                    if (o.st == State.READY && o.customer.equals(u)) { give(pl, o.armor); it.remove(); }
                }
            }
        }
    }

    private Order get(Player pl, String[] a) {
        try { Order o = orders.get(Integer.parseInt(a[1])); if (o == null) U.msg(pl, "Siparis yok."); return o; }
        catch (Exception e) { U.msg(pl, "Gecerli id gir."); return null; }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        for (Order o : orders.values()) {
            if (o.st == State.PRODUCING && o.readyAt <= now) {
                o.st = State.READY;
                if (o.smith != null) p.data.add(o.smith, p.getConfig().getDouble("orders.smith-pay"));
                Player c = Bukkit.getPlayer(o.customer);
                if (c != null) U.msg(c, "Zirhin hazir! Magazadan /siparis teslim");
            }
        }
    }

    private void give(Player pl, String key) {
        ItemStack[] set = Armors.make(p, key);
        if (set == null) return;
        for (ItemStack i : set) pl.getInventory().addItem(i);
    }
}

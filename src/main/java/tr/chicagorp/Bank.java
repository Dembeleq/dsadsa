package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;

/** Banka: nakit (cuzdan) ve banka bakiyesi. Telefon uygulamasi + ATM (kursu blogu) + komutlar. */
public class Bank implements Listener {
    private final ChicagoRP p;
    public Bank(ChicagoRP p) { this.p = p; }

    /** Once nakitten, yetmezse banka bakiyesinden duser. */
    public boolean pay(UUID u, double amount) {
        if (amount <= 0) return true;
        double cash = p.data.money(u), bank = p.data.bank(u);
        if (cash >= amount) { p.data.add(u, -amount); return true; }
        if (!p.getConfig().getBoolean("gallery.pay-from-bank", true) || cash + bank < amount) return false;
        p.data.add(u, -cash);
        p.data.addBank(u, -(amount - cash));
        return true;
    }

    public void deposit(Player pl, double amt) {
        UUID u = pl.getUniqueId();
        amt = Math.min(amt, p.data.money(u));
        if (amt <= 0) { U.msg(pl, "&cYatırılacak nakit yok."); return; }
        p.data.add(u, -amt); p.data.addBank(u, amt);
        U.msg(pl, "&a" + (long) amt + " TL bankaya yatırıldı.");
    }

    public void withdraw(Player pl, double amt) {
        UUID u = pl.getUniqueId();
        amt = Math.min(amt, p.data.bank(u));
        if (amt <= 0) { U.msg(pl, "&cBankada para yok."); return; }
        p.data.addBank(u, -amt); p.data.add(u, amt);
        U.msg(pl, "&a" + (long) amt + " TL çekildi.");
    }

    public void transfer(Player pl, String name, double amt) {
        UUID u = pl.getUniqueId();
        OfflinePlayer t = Bukkit.getOfflinePlayerIfCached(name);
        if (t == null || !p.data.has(t.getUniqueId()) || t.getUniqueId().equals(u)) { U.msg(pl, "&cGeçersiz alıcı."); return; }
        if (amt <= 0) { U.msg(pl, "&cGeçersiz miktar."); return; }
        double fee = amt * p.getConfig().getDouble("bank.transfer-fee", 0.01);
        if (p.data.bank(u) < amt + fee) { U.msg(pl, "&cBanka bakiyen yetersiz (komisyon: " + (long) fee + " TL)."); return; }
        p.data.addBank(u, -(amt + fee)); p.data.addBank(t.getUniqueId(), amt);
        U.msg(pl, "&a" + (long) amt + " TL havale edildi: " + t.getName() + " &7(komisyon " + (long) fee + " TL)");
        Player o = Bukkit.getPlayer(t.getUniqueId());
        if (o != null) U.msg(o, "&a" + pl.getName() + " bankana " + (long) amt + " TL gönderdi.");
    }

    public void command(Player pl, String cmd, String[] a) {
        switch (cmd) {
            case "banka" -> open(pl);
            case "yatir", "cek" -> {
                if (a.length < 1) { U.msg(pl, "/" + cmd + " <miktar|hepsi>"); return; }
                double v = a[0].equalsIgnoreCase("hepsi") ? Double.MAX_VALUE : parse(a[0]);
                if (v <= 0) { U.msg(pl, "Geçersiz miktar."); return; }
                if (cmd.equals("yatir")) deposit(pl, v); else withdraw(pl, v);
            }
            case "havale" -> {
                if (a.length < 2) { U.msg(pl, "/havale <oyuncu> <miktar>"); return; }
                transfer(pl, a[0], parse(a[1]));
            }
        }
    }

    private static double parse(String s) { try { return Double.parseDouble(s); } catch (NumberFormatException e) { return -1; } }

    public void open(Player pl) {
        UUID u = pl.getUniqueId();
        Gui g = new Gui("banka", 36, "&8&lBANKA");
        for (int i = 0; i < 36; i++) g.put(i, U.icon(i < 9 ? Material.GREEN_STAINED_GLASS_PANE : i < 27 ? Material.LIME_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE, " "), null);
        g.put(3, U.app("app_para", Material.GOLD_INGOT, "&6Nakit (cüzdan)", "&f" + (long) p.data.money(u) + " TL"), null);
        g.put(5, U.app("app_banka", Material.EMERALD, "&aBanka hesabı", "&f" + (long) p.data.bank(u) + " TL"), null);
        g.put(9, U.icon(Material.HOPPER, "&a&lYATIR", "&7Nakit → banka"), null);
        long[] amts = {1000, 10000, 100000};
        for (int i = 0; i < 3; i++) { g.put(11 + i, U.icon(Material.LIME_DYE, "&a+" + amts[i] + " TL"), "dep:" + amts[i]); g.put(20 + i, U.icon(Material.ORANGE_DYE, "&e-" + amts[i] + " TL"), "wd:" + amts[i]); }
        g.put(14, U.icon(Material.EMERALD_BLOCK, "&aHepsini yatır"), "dep:all");
        g.put(18, U.icon(Material.DISPENSER, "&e&lÇEK", "&7Banka → nakit"), null);
        g.put(23, U.icon(Material.GOLD_BLOCK, "&eHepsini çek"), "wd:all");
        g.put(16, U.icon(Material.PAPER, "&bHavale", "&7/havale <oyuncu> <miktar>", "&7Komisyon: %" + (int) (p.getConfig().getDouble("bank.transfer-fee", 0.01) * 100)), null);
        g.put(35, U.app("app_kapat", Material.BARRIER, "&cKapat"), "close");
        pl.openInventory(g.inv);
    }

    @EventHandler public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Gui g) || !g.page.equals("banka")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player pl)) return;
        String a = g.act.get(e.getRawSlot());
        if (a == null) return;
        if (a.equals("close")) { pl.closeInventory(); return; }
        String[] sp = a.split(":");
        double v = sp[1].equals("all") ? Double.MAX_VALUE : Double.parseDouble(sp[1]);
        if (sp[0].equals("dep")) deposit(pl, v); else withdraw(pl, v);
        open(pl);
    }

    @EventHandler public void onAtm(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof TileState ts)) return;
        if ("atm".equals(ts.getPersistentDataContainer().get(U.SHOP, PersistentDataType.STRING))) { e.setCancelled(true); open(e.getPlayer()); }
    }

    public void adminMenu(CommandSender s, String name) {
        Player t = Bukkit.getPlayer(name);
        if (t == null) { U.msg(s, "Oyuncu bulunamadı."); return; }
        open(t);
    }
}

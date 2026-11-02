package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.UUID;

public class Commands implements CommandExecutor {
    private final ChicagoRP p;
    public Commands(ChicagoRP p) { this.p = p; }

    @Override public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        String cn = c.getName().toLowerCase();
        if (cn.equals("galerimenu") || cn.equals("modifiyemenu") || cn.equals("silahcimenu") || cn.equals("bankamenu") || cn.equals("aracver") || cn.equals("aracsil") || cn.equals("silahver") || cn.equals("mermiver")) {
            admin(s, cn, a);
            return true;
        }
        if (!(s instanceof Player pl)) { U.msg(s, "Bu komut oyuncular icin."); return true; }
        switch (c.getName().toLowerCase()) {
            case "meslek" -> p.jobs.command(pl, a);
            case "pvp" -> p.pvp.toggle(pl, a);
            case "profil" -> profil(pl, a);
            case "kimlik" -> kimlik(pl, a);
            case "ehliyet" -> ehliyet(pl);
            case "arsa" -> p.plots.command(pl, a);
            case "jenerator" -> p.plots.generator(pl, a);
            case "kilit" -> p.plots.lock(pl);
            case "tutukla" -> p.police.arrest(pl, a);
            case "polis" -> p.police.kit(pl);
            case "siparis" -> p.orders.command(pl, a);
            case "tarla", "kimya", "elalti" -> p.chain.command(c.getName().toLowerCase(), pl, a);
            case "sehir" -> p.city.command(pl, a);
            case "telefon" -> p.phone.open(pl);
            case "galeri" -> { if (pl.hasPermission("rp.galeri.use")) p.vehicles.openGallery(pl); else U.msg(pl, "Araç almak için galerideki satıcıyla konuş."); }
            case "banka", "yatir", "cek", "havale" -> p.bank.command(pl, cn, a);
            case "silah" -> silah(pl, a);
            case "levelim" -> levelim(pl);
            case "bossbar" -> U.msg(pl, p.bossbars.toggle(pl) ? "&aBoss bar açıldı." : "&eBoss bar kapatıldı.");
            case "taksi", "kargo", "durum", "kacak", "soygun" -> p.work.command(pl, cn, a);
            case "modifiye" -> { if (pl.hasPermission("rp.modifiye.use")) p.vehicles.openModify(pl); else U.msg(pl, "Modifiye için modifiyeciyle konuş."); }
            case "aracayar" -> aracayar(pl, a);
            case "arac" -> arac(pl, a);
            case "cete" -> p.gang.command(pl, a);
            case "ode" -> ode(pl, a);
            case "paraver" -> paraver(pl, a, false);
            case "paraayarla" -> paraver(pl, a, true);
            case "harita" -> { var m = p.minimap.item(); if (m == null) U.msg(pl, "Şehir henüz kurulmamış."); else pl.getInventory().addItem(m); }
            case "rpreload" -> rpreload(pl);
            case "para" -> U.msg(pl, "Nakit: &a" + (long) p.data.money(pl.getUniqueId()) + " TL &7| Banka: &a" + (long) p.data.bank(pl.getUniqueId()) + " TL");
        }
        return true;
    }

    private void admin(CommandSender s, String cn, String[] a) {
        if (!s.hasPermission("rp.admin")) { U.msg(s, "Yetkin yok."); return; }
        if (a.length < 1) { U.msg(s, "/" + cn + " <oyuncu> ..."); return; }
        Player t = Bukkit.getPlayer(a[0]);
        switch (cn) {
            case "galerimenu" -> { if (t != null) p.vehicles.openGallery(t); }
            case "modifiyemenu" -> { if (t != null) p.vehicles.openModify(t); }
            case "silahcimenu" -> { if (t != null) p.weapons.openShop(t); }
            case "bankamenu" -> p.bank.adminMenu(s, a[0]);
            case "aracver" -> { if (a.length < 2) U.msg(s, "/aracver <oyuncu> <arac>"); else p.vehicles.adminGive(s, a[0], a[1], true); }
            case "aracsil" -> { if (a.length < 2) U.msg(s, "/aracsil <oyuncu> <arac>"); else p.vehicles.adminGive(s, a[0], a[1], false); }
            case "silahver" -> { if (a.length < 2) U.msg(s, "/silahver <oyuncu> <silah> [adet]"); else p.weapons.adminGive(s, a[0], a[1], a.length > 2 ? Integer.parseInt(a[2]) : 1); }
            case "mermiver" -> { if (a.length < 3) U.msg(s, "/mermiver <oyuncu> <pistol|rifle|shell|sniper> <adet>"); else p.weapons.adminAmmo(s, a[0], a[1], Integer.parseInt(a[2])); }
        }
    }

    private void levelim(Player pl) {
        var u = pl.getUniqueId();
        long xp = p.data.xp(u);
        pl.sendMessage(U.L.deserialize("&8&m              &r &6&lSEVİYEN &8&m              "));
        pl.sendMessage(U.L.deserialize("&7Seviye: &e" + p.data.level(u) + " &8| &7XP: &f" + xp + " &8| &7Sonraki seviyeye: &f" + (1000 - xp % 1000)));
        pl.sendMessage(U.L.deserialize(Placeholders.get(p, pl, "xp_bar") + " &7(%" + (xp % 1000 / 10) + ")"));
        pl.sendMessage(U.L.deserialize("&7Meslek: &f" + Placeholders.get(p, pl, "job") + " &8| &7Çete: &f" + Placeholders.get(p, pl, "gang")));
    }

    private void aracayar(Player pl, String[] a) {
        if (!pl.hasPermission("rp.admin")) return;
        String[][] keys = {{"scale", "vehicle.horse-scale"}, {"seat-y", "vehicle.seat-y"}, {"model-y", "vehicle.model-y"}, {"yaw", "models.vehicle-yaw-offset"},
                {"ok-yaw", "models.arrow-yaw-offset"}, {"ivme", "vehicle.accel"}, {"fren", "vehicle.brake"}, {"idle", "vehicle.idle-speed"}};
        if (a.length < 2) {
            StringBuilder sb = new StringBuilder();
            for (String[] k : keys) sb.append(k[0]).append("=").append(p.getConfig().get(k[1])).append("  ");
            U.msg(pl, "/aracayar <anahtar> <değer>  →  " + sb);
            return;
        }
        for (String[] k : keys) if (k[0].equals(a[0])) {
            try { p.getConfig().set(k[1], Double.parseDouble(a[1])); } catch (NumberFormatException e) { U.msg(pl, "Sayı gir."); return; }
            p.saveConfig();
            U.msg(pl, k[0] + " = " + a[1] + " (aktif araçlara anında uygulanır)");
            return;
        }
        U.msg(pl, "Bilinmeyen anahtar.");
    }

    private void silah(Player pl, String[] a) {
        if (a.length >= 2 && a[0].equals("uret")) { p.weapons.produce(pl, a[1]); return; }
        StringBuilder sb = new StringBuilder();
        var ws = p.getConfig().getConfigurationSection("weapons");
        for (String id : ws.getKeys(false)) if (!ws.getBoolean(id + ".shop", false)) sb.append(id).append(" (").append(ws.getInt(id + ".production-cost")).append(" TL, ").append(ws.getInt(id + ".production-seconds") / 60).append(" dk)  ");
        U.msg(pl, "Silah Ustası üretimi: /silah uret <silah>  →  " + sb);
    }

    private void paraver(Player pl, String[] a, boolean set) {
        if (!pl.hasPermission("rp.admin")) { U.msg(pl, "Yetkin yok."); return; }
        if (a.length < 2) { U.msg(pl, set ? "/paraayarla <oyuncu> <miktar>" : "/paraver <oyuncu> <miktar>  (eksi sayı para siler)"); return; }
        Player t = Bukkit.getPlayer(a[0]);
        double m;
        try { m = Double.parseDouble(a[1]); } catch (NumberFormatException e) { U.msg(pl, "Geçersiz miktar."); return; }
        if (t == null) { U.msg(pl, "Oyuncu bulunamadı."); return; }
        if (set) p.data.set(t.getUniqueId(), "money", m); else p.data.add(t.getUniqueId(), m);
        U.msg(pl, "&a" + t.getName() + " bakiyesi: " + (long) p.data.money(t.getUniqueId()) + " TL");
        if (t != pl) U.msg(t, "&6Bakiyen güncellendi: &f" + (long) p.data.money(t.getUniqueId()) + " TL");
    }

    private void rpreload(Player pl) {
        if (!pl.hasPermission("rp.admin")) return;
        p.reloadConfig();
        U.msg(pl, "Config yeniden yüklendi.");
    }

    private void arac(Player pl, String[] a) {
        if (a.length > 0) switch (a[0]) {
            case "kilit" -> { p.vehicles.toggleLock(pl); return; }
            case "yolcu" -> { p.vehicles.toggleAnyone(pl); return; }
            case "izin" -> { if (a.length > 1) p.vehicles.toggleAllow(pl, a[1]); else U.msg(pl, "/arac izin <oyuncu>"); return; }
            case "at" -> { if (a.length > 1) p.vehicles.kick(pl, Bukkit.getPlayer(a[1])); else p.vehicles.kickAll(pl); return; }
            case "ayar" -> { p.vehicles.openSettings(pl); return; }
            case "bagaj" -> { p.vehicles.openTrunk(pl); return; }
            default -> { }
        }
        if (a.length > 0 && a[0].equals("park")) { p.vehicles.remove(pl.getUniqueId()); return; }
        if (a.length > 0 && a[0].equals("is")) { p.vehicles.spawnJobCar(pl); return; }
        p.vehicles.openGarage(pl);
    }

    private void ode(Player pl, String[] a) {
        if (a.length < 2) { U.msg(pl, "/ode <oyuncu> <miktar>"); return; }
        Player t = Bukkit.getPlayer(a[0]);
        double m;
        try { m = Double.parseDouble(a[1]); } catch (NumberFormatException e) { U.msg(pl, "Geçersiz miktar."); return; }
        if (t == null || t == pl || m <= 0) { U.msg(pl, "Geçersiz oyuncu veya miktar."); return; }
        if (!p.data.take(pl.getUniqueId(), m)) { U.msg(pl, "Yetersiz bakiye."); return; }
        p.data.add(t.getUniqueId(), m);
        U.msg(pl, "&a" + (long) m + " TL gönderildi: " + t.getName());
        U.msg(t, "&a" + pl.getName() + " sana " + (long) m + " TL gönderdi.");
    }

    private void profil(Player pl, String[] a) {
        Player t = a.length > 0 ? Bukkit.getPlayer(a[0]) : pl;
        if (t == null) { U.msg(pl, "Oyuncu bulunamadi."); return; }
        UUID u = t.getUniqueId();
        U.msg(pl, "&e" + t.getName() + " &7| Bakiye: &a" + (long) p.data.money(u) + " &7| Seviye: &b" + p.data.level(u)
                + " &7| Meslek: &f" + p.data.job(u) + " &7| Çete: &f" + p.gang.info(u) + " &7| Arsa: &f" + p.plots.owned(u) + " &7| Jenerator: &f" + p.data.gens(u));
    }

    private void kimlik(Player pl, String[] a) {
        Player t = pl;
        if (a.length > 0) {
            if (!p.police.isPolice(pl)) { U.msg(pl, "Baskasinin kimligini sadece polis gorebilir."); return; }
            t = Bukkit.getPlayer(a[0]);
            if (t == null) { U.msg(pl, "Oyuncu bulunamadi."); return; }
        }
        UUID u = t.getUniqueId();
        U.msg(pl, "&eKIMLIK &7TC: &f" + p.data.get(u, "tc", "?") + " &7Dogum: &f" + p.data.get(u, "dogum", "?")
                + " &7Kan: &f" + p.data.get(u, "kan", "?") + " &7Sabika: &f" + p.data.get(u, "sabika", 0));
    }

    private void ehliyet(Player pl) {
        UUID u = pl.getUniqueId();
        if ((boolean) p.data.get(u, "ehliyet", false)) { U.msg(pl, "Zaten ehliyetin var."); return; }
        double price = p.getConfig().getDouble("ehliyet-price");
        if (!p.data.take(u, price)) { U.msg(pl, "Yetersiz bakiye (" + (long) price + " TL)."); return; }
        p.data.set(u, "ehliyet", true);
        U.msg(pl, "Ehliyet alindi. Arac eklentin bu degeri kontrol etmeli: players.yml -> ehliyet");
    }
}

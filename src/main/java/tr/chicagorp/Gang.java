package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.*;

/** Temel cete sistemi: kur, davet, katil, ayril, bilgi, baris. Yeni cete baristir modda baslar. */
public class Gang {
    private final ChicagoRP p;
    private final File f;
    private final YamlConfiguration y;
    private final Map<UUID, String> invites = new HashMap<>();

    public Gang(ChicagoRP p) {
        this.p = p;
        p.getDataFolder().mkdirs();
        f = new File(p.getDataFolder(), "gangs.yml");
        y = YamlConfiguration.loadConfiguration(f);
    }
    public void save() { try { y.save(f); } catch (IOException e) { p.getLogger().warning(e.getMessage()); } }

    public String gangOf(UUID u) { return (String) p.data.get(u, "gang", null); }
    public String symbol(String g) { return y.getString("gangs." + g + ".symbol", p.getConfig().getString("gang.default-symbol", "✦")); }
    public String color(String g) { return y.getString("gangs." + g + ".color", p.getConfig().getString("gang.default-color", "&c")); }
    public int members(String g) { return y.getStringList("gangs." + g + ".members").size(); }
    public boolean same(UUID a, UUID b) { String g = gangOf(a); return g != null && g.equals(gangOf(b)); }

    public String info(UUID u) {
        String g = gangOf(u);
        if (g == null) return "Çetesiz";
        return g + " (" + y.getStringList("gangs." + g + ".members").size() + " üye, " + (y.getBoolean("gangs." + g + ".peaceful", true) ? "barışçıl" : "savaşa açık") + ")";
    }

    public void command(Player pl, String[] a) {
        UUID u = pl.getUniqueId();
        if (a.length == 0) { U.msg(pl, "/cete kur <ad> | davet <oyuncu> | katil <ad> | ayril | bilgi | baris | sembol <s> | renk <renk>"); return; }
        String g = gangOf(u);
        switch (a[0]) {
            case "kur" -> {
                if (g != null || a.length < 2) { U.msg(pl, g != null ? "Zaten çetedesin." : "/cete kur <ad>"); return; }
                String name = a[1];
                if (y.contains("gangs." + name)) { U.msg(pl, "Bu isim alınmış."); return; }
                if (!p.data.take(u, 25000)) { U.msg(pl, "Çete kurmak 25000 TL."); return; }
                y.set("gangs." + name + ".owner", u.toString());
                y.set("gangs." + name + ".members", new ArrayList<>(List.of(u.toString())));
                y.set("gangs." + name + ".peaceful", true);
                p.data.set(u, "gang", name); save(); p.tags.refresh(pl);
                U.msg(pl, "&aÇete kuruldu: " + name + " (barışçıl modda başlar, hazır olunca /cete baris).");
            }
            case "davet" -> {
                if (g == null || a.length < 2 || !u.toString().equals(y.getString("gangs." + g + ".owner"))) { U.msg(pl, "Sadece çete lideri davet edebilir."); return; }
                Player t = Bukkit.getPlayer(a[1]);
                if (t == null) { U.msg(pl, "Oyuncu yok."); return; }
                invites.put(t.getUniqueId(), g);
                U.msg(t, "&e" + pl.getName() + " seni &f" + g + " &eçetesine davet etti: /cete katil " + g);
                U.msg(pl, "Davet gönderildi.");
            }
            case "katil" -> {
                if (g != null || a.length < 2 || !a[1].equals(invites.get(u))) { U.msg(pl, "Geçerli davetin yok."); return; }
                List<String> m = y.getStringList("gangs." + a[1] + ".members"); m.add(u.toString());
                y.set("gangs." + a[1] + ".members", m); p.data.set(u, "gang", a[1]); invites.remove(u); save(); p.tags.refresh(pl);
                U.msg(pl, "Çeteye katıldın: " + a[1]);
            }
            case "ayril" -> {
                if (g == null) return;
                List<String> m = y.getStringList("gangs." + g + ".members"); m.remove(u.toString());
                if (m.isEmpty() || u.toString().equals(y.getString("gangs." + g + ".owner"))) {
                    for (String s : m) p.data.set(UUID.fromString(s), "gang", null);
                    y.set("gangs." + g, null);
                } else y.set("gangs." + g + ".members", m);
                p.data.set(u, "gang", null); save(); p.tags.refreshAll();
                U.msg(pl, "Çeteden ayrıldın.");
            }
            case "baris" -> {
                if (g == null || !u.toString().equals(y.getString("gangs." + g + ".owner"))) return;
                y.set("gangs." + g + ".peaceful", false); save();
                U.msg(pl, "Çeten artık normal modda (savaşa açık).");
            }
            case "renk" -> {
                if (g == null || !u.toString().equals(y.getString("gangs." + g + ".owner"))) { U.msg(pl, "Sadece çete lideri renk seçebilir."); return; }
                var cols = p.getConfig().getConfigurationSection("gang.colors");
                if (a.length < 2 || cols == null || !cols.contains(a[1].toLowerCase())) { U.msg(pl, "/cete renk <" + (cols == null ? "" : String.join("|", cols.getKeys(false))) + ">"); return; }
                y.set("gangs." + g + ".color", cols.getString(a[1].toLowerCase())); save(); p.tags.refreshAll();
                U.msg(pl, "Çete rengi değişti.");
            }
            case "sembol" -> {
                if (g == null || a.length < 2 || !u.toString().equals(y.getString("gangs." + g + ".owner"))) { U.msg(pl, "/cete sembol <sembol> (lider, en fazla 3 karakter)"); return; }
                String sym = a[1].replaceAll("[&§]", "");
                if (sym.isEmpty() || sym.length() > 3) { U.msg(pl, "Sembol 1-3 karakter olmalı (örn ★ ☠ ♛ ⚔)."); return; }
                y.set("gangs." + g + ".symbol", sym); save(); p.tags.refreshAll();
                U.msg(pl, "Çete sembolü: " + sym);
            }
            case "bilgi" -> U.msg(pl, "Çete: " + info(u));
        }
    }
}

package tr.chicagorp;

import org.bukkit.entity.Player;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dahili {anahtar} yer tutucular (boss bar, mesajlar) + PlaceholderAPI %chicagorp_anahtar% ayni degerleri kullanir. */
public final class Placeholders {
    private Placeholders() {}
    private static final Pattern P = Pattern.compile("\\{([a-z_]+)\\}");
    private static final NumberFormat NF = NumberFormat.getInstance(new Locale("tr", "TR"));

    public static String num(double d) { synchronized (NF) { return NF.format(Math.round(d)); } }

    public static String get(ChicagoRP p, Player pl, String key) {
        if (key.equals("server")) return p.getConfig().getString("server-name", "ChicagoRP");
        if (key.equals("online")) return String.valueOf(org.bukkit.Bukkit.getOnlinePlayers().size());
        if (pl == null) return "";
        UUID u = pl.getUniqueId();
        long xp = p.data.xp(u);
        switch (key) {
            case "player": return pl.getName();
            case "level": return String.valueOf(p.data.level(u));
            case "xp": return String.valueOf(xp);
            case "xp_next": return String.valueOf(1000 - xp % 1000);
            case "xp_percent": return String.valueOf(xp % 1000 / 10);
            case "xp_bar": { int f = (int) (xp % 1000 / 100); return "&a" + "▮".repeat(f) + "&7" + "▮".repeat(10 - f); }
            case "money": return num(p.data.money(u));
            case "bank": return num(p.data.bank(u));
            case "total": return num(p.data.money(u) + p.data.bank(u));
            case "money_raw": return String.valueOf((long) p.data.money(u));
            case "bank_raw": return String.valueOf((long) p.data.bank(u));
            case "job": return p.data.job(u).equals("yok") ? "İşsiz" : p.data.job(u);
            case "job_count": return String.valueOf(p.data.countJob(p.data.job(u)));
            case "job_capacity": { int c = p.jobs.capacity(p.data.job(u)); return c < 0 ? "∞" : String.valueOf(c); }
            case "gang": { String g = p.gang.gangOf(u); return g == null ? "Çetesiz" : g; }
            case "gang_symbol": { String g = p.gang.gangOf(u); return g == null ? "" : p.gang.symbol(g); }
            case "gang_color": { String g = p.gang.gangOf(u); return g == null ? "&7" : p.gang.color(g); }
            case "gang_members": { String g = p.gang.gangOf(u); return g == null ? "0" : String.valueOf(p.gang.members(g)); }
            case "gang_tag": { String g = p.gang.gangOf(u); return g == null ? "" : p.tags.prefixFor(g); }
            case "plots": return String.valueOf(p.plots.owned(u));
            case "generators": return String.valueOf(p.data.gens(u));
            case "wanted": return String.valueOf(p.data.get(u, "sabika", 0));
            case "car": return p.vehicles.carName(u);
            case "speed": return String.valueOf(p.vehicles.kmh(u));
            case "car_hp": return String.valueOf(p.vehicles.hp(u));
            case "license": return (boolean) p.data.get(u, "ehliyet", false) ? "Var" : "Yok";
            case "tc": return String.valueOf(p.data.get(u, "tc", "-"));
            case "pvp": return p.pvp.isOff(u) ? "Kapalı" : "Açık";
            default: return "";
        }
    }

    public static String apply(ChicagoRP p, Player pl, String text) {
        Matcher m = P.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) m.appendReplacement(sb, Matcher.quoteReplacement(get(p, pl, m.group(1))));
        m.appendTail(sb);
        String out = sb.toString();
        if (p.papi) out = PapiHook.parse(pl, out);
        return out;
    }
}

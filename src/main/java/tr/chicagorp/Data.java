package tr.chicagorp;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Data {
    private final ChicagoRP p;
    private final File f;
    private final YamlConfiguration y;

    public Data(ChicagoRP p) {
        this.p = p;
        p.getDataFolder().mkdirs();
        f = new File(p.getDataFolder(), "players.yml");
        y = YamlConfiguration.loadConfiguration(f);
    }
    public Object get(UUID u, String k, Object d) { return y.get(u + "." + k, d); }
    public void set(UUID u, String k, Object v) { y.set(u + "." + k, v); }
    public boolean has(UUID u) { return y.contains(u + ".money"); }
    public double money(UUID u) { return y.getDouble(u + ".money", 0); }
    public void add(UUID u, double d) { set(u, "money", money(u) + d); }
    public boolean take(UUID u, double d) { if (money(u) < d) return false; add(u, -d); return true; }
    public String job(UUID u) { return y.getString(u + ".job", "yok"); }
    public int gens(UUID u) { return y.getInt(u + ".gens", 0); }
    public int level(UUID u) { return (int) (y.getLong(u + ".xp", 0) / 1000) + 1; }
    public void addXp(UUID u, long x) { y.set(u + ".xp", y.getLong(u + ".xp", 0) + x); }
    public double bank(UUID u) { return y.getDouble(u + ".bank", 0); }
    public void addBank(UUID u, double d) { set(u, "bank", bank(u) + d); }
    public long xp(UUID u) { return y.getLong(u + ".xp", 0); }
    public java.util.Set<String> keys() { return y.getKeys(false); }
    public int countJob(String j) { int n = 0; for (String k : y.getKeys(false)) if (j.equals(y.getString(k + ".job"))) n++; return n; }
    public void save() { try { y.save(f); } catch (IOException e) { p.getLogger().warning(e.getMessage()); } }
}

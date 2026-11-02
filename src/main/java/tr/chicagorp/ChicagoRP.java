package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.kyori.adventure.title.Title;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Random;

public class ChicagoRP extends JavaPlugin implements Listener {
    public Data data;
    public PvpManager pvp;
    public PlotManager plots;
    public JobManager jobs;
    public Police police;
    public OrderSystem orders;
    public SupplyChain chain;
    public CityBuilder city;
    public Vehicles vehicles;
    public Phone phone;
    public Gang gang;
    public Minimap minimap;
    public Bank bank;
    public Weapons weapons;
    public Tags tags;
    public Work work;
    public BossBars bossbars;
    public boolean papi;
    private final java.util.Map<java.util.UUID, Long> lastMove = new java.util.HashMap<>();

    @Override public void onEnable() {
        saveDefaultConfig();
        migrateConfig();
        data = new Data(this);
        plots = new PlotManager(this);
        pvp = new PvpManager(this);
        jobs = new JobManager(this);
        police = new Police(this);
        orders = new OrderSystem(this);
        chain = new SupplyChain(this);
        city = new CityBuilder(this);
        vehicles = new Vehicles(this);
        phone = new Phone(this);
        gang = new Gang(this);
        bank = new Bank(this);
        weapons = new Weapons(this);
        tags = new Tags(this);
        work = new Work(this);
        bossbars = new BossBars(this);
        papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (papi) new PapiHook(this).register();
        minimap = new Minimap(this);
        Bukkit.getScheduler().runTask(this, minimap::init);
        for (Object l : new Object[]{this, plots, pvp, jobs, police, vehicles, phone, weapons, bank, tags, work, bossbars})
            Bukkit.getPluginManager().registerEvents((Listener) l, this);
        Commands cmds = new Commands(this);
        for (String c : getDescription().getCommands().keySet()) getCommand(c).setExecutor(cmds);
        Bukkit.getScheduler().runTaskTimer(this, () -> { data.save(); plots.save(); }, 6000L, 6000L);
        Bukkit.getScheduler().runTaskTimer(this, plots::tickGenerators, 1200L, 1200L);
        Bukkit.getScheduler().runTaskTimer(this, orders::tick, 200L, 200L);
        Bukkit.getScheduler().runTaskTimer(this, this::expireJobs, 1200L, 12000L);
        long pd = Math.max(1, getConfig().getInt("payday-minutes", 10)) * 1200L;
        Bukkit.getScheduler().runTaskTimer(this, this::payday, pd, pd);
    }

    @Override public void onDisable() { vehicles.removeAll(); data.save(); plots.save(); gang.save(); }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        Player pl = e.getPlayer();
        e.joinMessage(U.L.deserialize(U.fmt(getConfig().getString("join-message", ""), pl)));
        pl.showTitle(Title.title(U.L.deserialize(U.fmt(getConfig().getString("welcome-title", "&6&l{server}"), pl)),
                U.L.deserialize(U.fmt(getConfig().getString("welcome-subtitle", "&7Hoş geldin &f{player}"), pl))));
        String url = getConfig().getString("resource-pack.url", "");
        if (!url.isEmpty()) pl.setResourcePack(url, getConfig().getString("resource-pack.sha1", ""), getConfig().getBoolean("resource-pack.required", true));
        if (!data.has(pl.getUniqueId())) {
            var u = pl.getUniqueId();
            Random r = new Random();
            data.set(u, "money", getConfig().getDouble("start-money"));
            StringBuilder tc = new StringBuilder("" + (1 + r.nextInt(9)));
            for (int i = 0; i < 10; i++) tc.append(r.nextInt(10));
            data.set(u, "tc", tc.toString());
            data.set(u, "dogum", (1 + r.nextInt(28)) + "." + (1 + r.nextInt(12)) + "." + (1985 + r.nextInt(20)));
            data.set(u, "kan", new String[]{"A+", "A-", "B+", "0+", "0-", "AB+"}[r.nextInt(6)]);
        }
        phone.give(pl);
        weapons.deliverPending(pl);
        lastMove.put(pl.getUniqueId(), System.currentTimeMillis());
        data.set(pl.getUniqueId(), "seen", System.currentTimeMillis());
        var s = getConfig().getConfigurationSection("spawn");
        var w = Bukkit.getWorld(s.getString("world"));
        if (w != null && !pl.hasPlayedBefore())
            pl.teleport(new org.bukkit.Location(w, s.getDouble("x") + .5, s.getDouble("y"), s.getDouble("z") + .5));
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        data.set(e.getPlayer().getUniqueId(), "seen", System.currentTimeMillis());
        e.quitMessage(U.L.deserialize(U.fmt(getConfig().getString("quit-message", ""), e.getPlayer())));
    }

    @EventHandler public void onMove(PlayerMoveEvent e) {
        if (e.hasChangedPosition()) lastMove.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    /** Asil is mekanigi olmayan meslekler icin saatlik maas (AFK olmayanlara). */
    private void payday() {
        int mins = Math.max(1, getConfig().getInt("payday-minutes", 10));
        long now = System.currentTimeMillis();
        for (Player pl : Bukkit.getOnlinePlayers()) {
            double hourly = getConfig().getDouble("job-salary." + data.job(pl.getUniqueId()), 0);
            if (hourly <= 0 || now - lastMove.getOrDefault(pl.getUniqueId(), now) > 5 * 60_000L) continue;
            double pay = hourly * mins / 60.0;
            data.add(pl.getUniqueId(), pay);
            data.addXp(pl.getUniqueId(), 40);
            U.msg(pl, "&6Maaş: &a+" + (long) pay + " TL");
        }
    }

    /** job-expire-hours (varsayilan 48) saattir girmeyenleri meslekten atar. */
    private void expireJobs() {
        long lim = getConfig().getLong("job-expire-hours", 48) * 3600_000L, now = System.currentTimeMillis();
        for (String k : new java.util.ArrayList<>(data.keys())) {
            java.util.UUID u;
            try { u = java.util.UUID.fromString(k); } catch (IllegalArgumentException e) { continue; }
            if (data.job(u).equals("yok")) continue;
            if (Bukkit.getPlayer(u) != null) { data.set(u, "seen", now); continue; }
            Object seen = data.get(u, "seen", null);
            if (seen == null) { data.set(u, "seen", now); continue; }
            if (now - ((Number) seen).longValue() > lim) {
                getLogger().info(k + " " + data.job(u) + " mesleginden atildi (" + (lim / 3600_000L) + " saat girmedi).");
                data.set(u, "job", "yok");
            }
        }
    }

    private void copySection(YamlConfiguration def, String key) {
        ConfigurationSection src = def.getConfigurationSection(key);
        getConfig().set(key, null);
        if (src == null) return;
        for (String k : src.getKeys(true)) if (!src.isConfigurationSection(k)) getConfig().set(key + "." + k, src.get(k));
    }

    /** Eski config.yml'e yeni anahtarlari ekler; eski surumlerde ekonomi/arac/silah/zirh bolumlerini yeniler. */
    private void migrateConfig() {
        var in = getResource("config.yml");
        if (in == null) return;
        YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        int cur = def.getInt("config-version", 1);
        if (getConfig().getInt("config-version", 0) < cur) {
            for (String k : new String[]{"vehicles", "armors", "chain", "orders", "weapons", "ammo", "job-salary", "signs-def", "job-capacity", "jobs", "tuning", "job-vehicles", "taxi", "cargo", "durum", "smuggle", "robbery", "bossbar", "vehicle"}) copySection(def, k);
            getConfig().set("gang.tag-format", def.getString("gang.tag-format"));
            copySection(def, "gang.colors");
            getConfig().set("models.arrow-yaw-offset", def.getDouble("models.arrow-yaw-offset"));
            getConfig().set("job-expire-hours", def.getInt("job-expire-hours", 48));
            for (String k : new String[]{"fishing", "generator"}) copySection(def, k);
            for (String k : new String[]{"arrest-reward", "ehliyet-price", "daily-reward", "payday-minutes", "guns-enabled", "headshot-multiplier"}) getConfig().set(k, def.get(k));
            getConfig().set("resource-pack.sha1", def.getString("resource-pack.sha1"));
            getConfig().set("config-version", cur);
        }
        getConfig().setDefaults(def);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}

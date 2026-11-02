package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/** Binalarin ustundeki 3D yazi tabelalari (kaynak paketindeki sign_<id> modelleri). */
public final class Signs {
    private Signs() {}

    public static void add(ChicagoRP p, Blk b, String id, double fx, double y, double fz) {
        Location l = b.locf(fx, y, fz);
        l.setYaw(b.yaw());
        int n = p.getConfig().getInt("signs.count", 0);
        String k = "signs.list." + n;
        p.getConfig().set(k + ".id", id);
        p.getConfig().set(k + ".world", l.getWorld().getName());
        p.getConfig().set(k + ".x", l.getX()); p.getConfig().set(k + ".y", l.getY()); p.getConfig().set(k + ".z", l.getZ());
        p.getConfig().set(k + ".yaw", (double) l.getYaw());
        p.getConfig().set("signs.count", n + 1);
        spawn(p, id, l);
    }

    /** ATM: kursu blogu + 3D model. face: true = bina icine (on duvara dayali) bakar. */
    public static void atm(ChicagoRP p, Blk b, int x, int y, int z, boolean faceInterior) {
        var bl = b.at(x, y, z);
        bl.setType(Material.LECTERN, false);
        if (bl.getState() instanceof org.bukkit.block.TileState ts) { ts.getPersistentDataContainer().set(U.SHOP, org.bukkit.persistence.PersistentDataType.STRING, "atm"); ts.update(); }
        Location l = b.locf(x + 0.5, y + 0.5, z + 0.5);
        l.setYaw((b.yaw() + (faceInterior ? 180f : 0f)) % 360f);
        int n = p.getConfig().getInt("signs.count", 0);
        String k = "signs.list." + n;
        p.getConfig().set(k + ".id", "atm"); p.getConfig().set(k + ".world", l.getWorld().getName());
        p.getConfig().set(k + ".x", l.getX()); p.getConfig().set(k + ".y", l.getY()); p.getConfig().set(k + ".z", l.getZ()); p.getConfig().set(k + ".yaw", (double) l.getYaw());
        p.getConfig().set("signs.count", n + 1);
        spawn(p, "atm", l);
    }

    static void spawn(ChicagoRP p, String id, Location base) {
        boolean atm = id.equals("atm");
        double units = p.getConfig().getDouble("signs-def." + id + ".units", 20);
        double width = p.getConfig().getDouble("signs-def." + id + ".width", 10);
        float s = atm ? 1f : (float) (width * 16.0 / units);
        Location loc = base.clone();
        loc.setYaw((float) (base.getYaw() + p.getConfig().getDouble("models.sign-yaw-offset", 0)));
        ItemStack it = U.withModel(new ItemStack(Material.OAK_SIGN), atm ? "atm" : "sign_" + id);
        base.getWorld().spawn(loc, ItemDisplay.class, d -> {
            d.setItemStack(it);
            d.setTransformation(new Transformation(new Vector3f(0f, 0f, 0f), new AxisAngle4f(0f, 0f, 0f, 1f), new Vector3f(s, s, s), new AxisAngle4f(0f, 0f, 0f, 1f)));
            d.addScoreboardTag("rpsign");
            d.setViewRange(3f);
        });
    }

    /** Tum tabelalari config'ten yeniden kurar (yon ofseti degisince). */
    public static int respawnAll(ChicagoRP p) {
        var sec = p.getConfig().getConfigurationSection("signs.list");
        if (sec == null) return 0;
        int n = 0;
        for (String k : sec.getKeys(false)) {
            var c = sec.getConfigurationSection(k);
            World w = Bukkit.getWorld(c.getString("world", "sehir"));
            if (w == null) continue;
            Location l = new Location(w, c.getDouble("x"), c.getDouble("y"), c.getDouble("z"), (float) c.getDouble("yaw"), 0f);
            l.getChunk().load();
            for (Entity e : w.getNearbyEntities(l, 2, 3, 2)) if (e instanceof ItemDisplay && e.getScoreboardTags().contains("rpsign")) e.remove();
            spawn(p, c.getString("id"), l);
            n++;
        }
        return n;
    }
}

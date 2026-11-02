package tr.chicagorp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class U {
    public static final LegacyComponentSerializer L = LegacyComponentSerializer.legacyAmpersand();
    public static final NamespacedKey TAG = NamespacedKey.fromString("chicagorp:tag");
    public static final NamespacedKey SHOP = NamespacedKey.fromString("chicagorp:shop");

    public static String prefix() {
        ChicagoRP pl = org.bukkit.plugin.java.JavaPlugin.getPlugin(ChicagoRP.class);
        return pl.getConfig().getString("prefix", "&8[&6&l{server}&8] &7» &f").replace("{server}", pl.getConfig().getString("server-name", "ChicagoRP"));
    }

    public static String fmt(String t, org.bukkit.entity.Player pl) {
        ChicagoRP c = org.bukkit.plugin.java.JavaPlugin.getPlugin(ChicagoRP.class);
        return t.replace("{server}", c.getConfig().getString("server-name", "ChicagoRP"))
                .replace("{player}", pl == null ? "" : pl.getName())
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }

    public static void msg(CommandSender s, String m) { s.sendMessage(L.deserialize(prefix() + m)); }

    /** item_model bileseni ekler (kaynak paketindeki chicagorp:<name> modeli). */
    public static ItemStack withModel(ItemStack it, String name) {
        ChicagoRP c = org.bukkit.plugin.java.JavaPlugin.getPlugin(ChicagoRP.class);
        if (!c.getConfig().getBoolean("models.enabled", true)) return it;
        ItemMeta im = it.getItemMeta();
        im.setItemModel(NamespacedKey.fromString("chicagorp:" + name));
        it.setItemMeta(im);
        return it;
    }

    /** custom_model_data renk bilesenini ayarlar (arac govdesi boyasi; modelde tintindex 0). */
    public static ItemStack paint(ItemStack it, int rgb) {
        ItemMeta im = it.getItemMeta();
        var cmd = im.getCustomModelDataComponent();
        cmd.setColors(List.of(org.bukkit.Color.fromRGB(rgb & 0xFFFFFF)));
        im.setCustomModelDataComponent(cmd);
        it.setItemMeta(im);
        return it;
    }

    public static ItemStack app(String model, Material fallback, String name, String... lore) {
        return withModel(icon(fallback, name, lore), model);
    }

    public static ItemStack item(Material m, String name, String tag) {
        ItemStack i = new ItemStack(m);
        ItemMeta im = i.getItemMeta();
        im.displayName(L.deserialize(name).decoration(TextDecoration.ITALIC, false));
        if (tag != null) im.getPersistentDataContainer().set(TAG, PersistentDataType.STRING, tag);
        i.setItemMeta(im);
        return i;
    }

    public static String tagOf(ItemStack i) {
        if (i == null || !i.hasItemMeta()) return null;
        return i.getItemMeta().getPersistentDataContainer().get(TAG, PersistentDataType.STRING);
    }

    public static boolean hasTag(ItemStack i, String tag) {
        if (i == null || !i.hasItemMeta()) return false;
        String v = i.getItemMeta().getPersistentDataContainer().get(TAG, PersistentDataType.STRING);
        return tag.equals(v);
    }

    /** ItemsAdder varsa o esyayi, yoksa fallback'i dondurur. Mod gerektirmez. */
    public static ItemStack custom(String iaId, ItemStack fallback) {
        if (iaId == null || iaId.isEmpty() || Bukkit.getPluginManager().getPlugin("ItemsAdder") == null) return fallback;
        try {
            Class<?> c = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object cs = c.getMethod("getInstance", String.class).invoke(null, iaId);
            if (cs == null) return fallback;
            return (ItemStack) c.getMethod("getItemStack").invoke(cs);
        } catch (Throwable t) { return fallback; }
    }

    public static int count(org.bukkit.entity.Player p, String tag) {
        int n = 0;
        for (ItemStack i : p.getInventory().getContents()) if (hasTag(i, tag)) n += i.getAmount();
        return n;
    }

    public static void remove(org.bukkit.entity.Player p, String tag, int amt) {
        for (ItemStack i : p.getInventory().getContents()) {
            if (amt <= 0) return;
            if (hasTag(i, tag)) { int t = Math.min(amt, i.getAmount()); i.setAmount(i.getAmount() - t); amt -= t; }
        }
    }

    public static ItemStack icon(Material m, String name, String... lore) {
        ItemStack i = new ItemStack(m);
        ItemMeta im = i.getItemMeta();
        im.displayName(L.deserialize(name).decoration(TextDecoration.ITALIC, false));
        if (lore.length > 0) {
            List<Component> l = new ArrayList<>();
            for (String s : lore) l.add(L.deserialize(s).decoration(TextDecoration.ITALIC, false));
            im.lore(l);
        }
        i.setItemMeta(im);
        return i;
    }
}

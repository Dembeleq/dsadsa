package tr.chicagorp;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import java.util.List;

/** Config'teki armors.<anahtar> degerlerinden 4 parcalik zirh seti uretir (deri zirh + ozel zirh/dayaniklilik). */
public final class Armors {
    private Armors() {}

    public static ItemStack[] make(ChicagoRP p, String key) {
        var c = p.getConfig().getConfigurationSection("armors." + key);
        if (c == null) return null;
        double armor = c.getDouble("armor"), tough = c.getDouble("toughness");
        List<Integer> col = c.getIntegerList("color");
        Color color = col.size() == 3 ? Color.fromRGB(col.get(0), col.get(1), col.get(2)) : Color.GRAY;
        Material[] mats = {Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS};
        EquipmentSlotGroup[] slots = {EquipmentSlotGroup.HEAD, EquipmentSlotGroup.CHEST, EquipmentSlotGroup.LEGS, EquipmentSlotGroup.FEET};
        double[] share = {0.15, 0.40, 0.30, 0.15};
        String[] part = {"Şapka", "Yelek", "Pantolon", "Bot"};
        ItemStack[] out = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack it = U.item(mats[i], c.getString("name", key) + " &7" + part[i], "zirh_" + key);
            LeatherArmorMeta m = (LeatherArmorMeta) it.getItemMeta();
            m.setColor(color);
            m.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(NamespacedKey.fromString("chicagorp:armor_" + key + "_" + i), armor * share[i], AttributeModifier.Operation.ADD_NUMBER, slots[i]));
            if (tough > 0) m.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(NamespacedKey.fromString("chicagorp:tough_" + key + "_" + i), tough * share[i], AttributeModifier.Operation.ADD_NUMBER, slots[i]));
            m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
            out[i] = it;
        }
        return out;
    }
}

package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class Gui implements InventoryHolder {
    public final String page;
    public final Inventory inv;
    public final Map<Integer, String> act = new HashMap<>();

    public Gui(String page, int size, String title) {
        this.page = page;
        this.inv = Bukkit.createInventory(this, size, U.L.deserialize(title));
    }
    public void put(int slot, ItemStack it, String action) { inv.setItem(slot, it); if (action != null) act.put(slot, action); }
    @Override public Inventory getInventory() { return inv; }
}

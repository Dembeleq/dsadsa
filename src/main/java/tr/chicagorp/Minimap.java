package tr.chicagorp;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.*;

/** Sehrin haritasi (tum sehir tek haritada, oyuncu konumu isaretli). */
public class Minimap {
    private final ChicagoRP p;
    private byte[] pixels;

    public Minimap(ChicagoRP p) { this.p = p; }

    public void init() {
        int id = p.getConfig().getInt("minimap-id", -1);
        if (id < 0) return;
        MapView v = Bukkit.getMap(id);
        if (v != null) attach(v);
    }

    public ItemStack item() {
        World w = Bukkit.getWorld("sehir");
        if (w == null) return null;
        int id = p.getConfig().getInt("minimap-id", -1);
        MapView v = id < 0 ? null : Bukkit.getMap(id);
        if (v == null) {
            v = Bukkit.createMap(w);
            attach(v);
            p.getConfig().set("minimap-id", v.getId());
            p.saveConfig();
        }
        ItemStack it = new ItemStack(Material.FILLED_MAP);
        MapMeta mm = (MapMeta) it.getItemMeta();
        mm.setMapView(v);
        mm.displayName(U.L.deserialize("&6Şehir Haritası"));
        it.setItemMeta(mm);
        return it;
    }

    private void attach(MapView v) {
        v.setScale(MapView.Scale.CLOSE);
        v.setCenterX(0); v.setCenterZ(0);
        v.setTrackingPosition(false);
        for (MapRenderer r : new ArrayList<>(v.getRenderers())) v.removeRenderer(r);
        if (pixels == null) pixels = MapPalette.imageToBytes(draw());
        v.addRenderer(new MapRenderer(true) {
            private final Set<UUID> done = new HashSet<>();
            @Override public void render(MapView map, MapCanvas canvas, Player pl) {
                if (done.add(pl.getUniqueId()))
                    for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) canvas.setPixel(x, y, pixels[y * 128 + x]);
                MapCursorCollection cc = canvas.getCursors();
                while (cc.size() > 0) cc.removeCursor(cc.getCursor(0));
                if (!pl.getWorld().getName().equals("sehir")) return;
                Location l = pl.getLocation();
                int cx = (int) Math.max(-127, Math.min(127, Math.round(l.getX() / 2.0)));
                int cy = (int) Math.max(-127, Math.min(127, Math.round(l.getZ() / 2.0)));
                byte dir = (byte) (((int) ((((l.getYaw() % 360) + 360) % 360 + 11.25) / 22.5)) & 15);
                cc.addCursor(new MapCursor((byte) cx, (byte) cy, dir, MapCursor.Type.PLAYER, true));
            }
        });
    }

    private static int px(int wx) { return (wx + 128) / 2; }

    private BufferedImage draw() {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(70, 150, 65)); g.fillRect(0, 0, 128, 128);
        int X0 = CityBuilder.X0, P = CityBuilder.P, R = CityBuilder.R;
        g.setColor(new Color(85, 85, 92));
        for (int i = 0; i < 6; i++) {
            int a = px(X0 + i * P);
            g.fillRect(a, px(X0), 5, 126); g.fillRect(px(X0), a, 126, 5);
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));
        for (int j = 0; j < 5; j++) for (int i = 0; i < 5; i++) {
            char c = CityBuilder.MAP[j].charAt(i);
            int bx = px(X0 + i * P + R), bz = px(X0 + j * P + R), s = 19;
            switch (c) {
                case 'H' -> {
                    g.setColor(new Color(125, 195, 110)); g.fillRect(bx, bz, s, s);
                    g.setColor(new Color(215, 190, 150));
                    for (int k = 0; k < 4; k++) g.fillRect(bx + 2 + (k % 2) * 9, bz + 2 + (k / 2) * 9, 5, 5);
                }
                case 'A', 'B', 'C', 'D' -> {
                    g.setColor(new Color(220, 150, 85)); g.fillRect(bx, bz, s, s);
                    g.setColor(new Color(150, 90, 50));
                    for (int k = 0; k < 4; k++) g.fillRect(bx + 2 + (k % 2) * 9, bz + 2 + (k / 2) * 9, 6, 6);
                    if (c == 'C') { g.setColor(new Color(40, 40, 160)); g.fillRect(bx + 1, bz + 1, 17, 8); label(g, "G", bx + 6, bz + 8); }
                    if (c == 'D') { g.setColor(new Color(235, 235, 235)); g.fillRect(bx + 1, bz + 1, 17, 8); g.setColor(Color.RED); g.fillRect(bx + 8, bz + 2, 3, 6); g.fillRect(bx + 6, bz + 4, 7, 2); }
                }
                case 'K' -> { g.setColor(new Color(50, 80, 200)); g.fillRect(bx, bz, s, s); label(g, "K", bx + 6, bz + 14); }
                case 'S' -> { g.setColor(new Color(240, 205, 70)); g.fillRect(bx - 3, bz - 3, s + 6, s + 6); g.setColor(new Color(60, 130, 230)); g.fillOval(bx + 4, bz + 4, 11, 11); }
                case 'P' -> { g.setColor(new Color(40, 120, 50)); g.fillRect(bx, bz, s, s); g.setColor(new Color(25, 85, 35)); for (int k = 0; k < 6; k++) g.fillOval(bx + 2 + (k % 3) * 6, bz + 3 + (k / 3) * 8, 4, 4); }
                case 'L' -> { g.setColor(new Color(55, 115, 205)); g.fillRect(bx, bz, s, s); g.setColor(new Color(120, 85, 50)); g.fillRect(bx + 8, bz + 2, 3, 12); label(g, "L", bx + 6, bz + 18); }
                case 'F' -> { g.setColor(new Color(200, 180, 70)); g.fillRect(bx, bz, s, s); g.setColor(new Color(150, 130, 40)); for (int k = 0; k < 4; k++) g.fillRect(bx + 2, bz + 2 + k * 4, 15, 2); }
                default -> { }
            }
        }
        g.dispose();
        return img;
    }

    private static void label(Graphics2D g, String t, int x, int y) {
        g.setColor(Color.BLACK); g.drawString(t, x + 1, y + 1);
        g.setColor(Color.WHITE); g.drawString(t, x, y);
    }

    @SuppressWarnings("unused") private static Component unused() { return Component.empty(); }
}

package tr.chicagorp;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** PlaceholderAPI uzantisi: %chicagorp_level%, %chicagorp_money%, %chicagorp_gang% ... (Placeholders.get anahtarlari). */
public class PapiHook extends PlaceholderExpansion {
    private final ChicagoRP p;
    public PapiHook(ChicagoRP p) { this.p = p; }
    @Override public @NotNull String getIdentifier() { return "chicagorp"; }
    @Override public @NotNull String getAuthor() { return "ChicagoRP"; }
    @Override public @NotNull String getVersion() { return "1.0"; }
    @Override public boolean persist() { return true; }
    @Override public String onPlaceholderRequest(Player player, @NotNull String params) { return Placeholders.get(p, player, params.toLowerCase()); }
    public static String parse(Player pl, String text) { return PlaceholderAPI.setPlaceholders(pl, text); }
}

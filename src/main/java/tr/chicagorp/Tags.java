package tr.chicagorp;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/** Cete etiketi: isimden AYRI, sembollu ("[✦ CETE] Isim") - kafa ustu, tab listesi ve sohbette. */
public class Tags implements Listener {
    private final ChicagoRP p;
    public Tags(ChicagoRP p) { this.p = p; }

    public String prefixFor(String gang) {
        return p.getConfig().getString("gang.tag-format", "&8[{color}{symbol} &f{gang}&8] ")
                .replace("{color}", p.gang.color(gang)).replace("{symbol}", p.gang.symbol(gang)).replace("{gang}", gang);
    }

    private static final net.kyori.adventure.text.format.NamedTextColor[] COLS = {
            net.kyori.adventure.text.format.NamedTextColor.BLACK, net.kyori.adventure.text.format.NamedTextColor.DARK_BLUE, net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN,
            net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA, net.kyori.adventure.text.format.NamedTextColor.DARK_RED, net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE,
            net.kyori.adventure.text.format.NamedTextColor.GOLD, net.kyori.adventure.text.format.NamedTextColor.GRAY, net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY,
            net.kyori.adventure.text.format.NamedTextColor.BLUE, net.kyori.adventure.text.format.NamedTextColor.GREEN, net.kyori.adventure.text.format.NamedTextColor.AQUA,
            net.kyori.adventure.text.format.NamedTextColor.RED, net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE, net.kyori.adventure.text.format.NamedTextColor.YELLOW,
            net.kyori.adventure.text.format.NamedTextColor.WHITE};

    private net.kyori.adventure.text.format.NamedTextColor nameColor(String gang) {
        String c = p.gang.color(gang);
        int i = c.length() > 1 ? "0123456789abcdef".indexOf(Character.toLowerCase(c.charAt(1))) : -1;
        return i >= 0 ? COLS[i] : net.kyori.adventure.text.format.NamedTextColor.WHITE;
    }

    public void refresh(Player pl) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String gang = p.gang.gangOf(pl.getUniqueId());
        String want = gang == null ? null : "rpg_" + Integer.toHexString(gang.hashCode());
        for (Team t : sb.getTeams()) if (t.getName().startsWith("rpg_") && t.hasEntry(pl.getName()) && !t.getName().equals(want)) t.removeEntry(pl.getName());
        if (want == null) return;
        Team t = sb.getTeam(want);
        if (t == null) t = sb.registerNewTeam(want);
        t.prefix(U.L.deserialize(prefixFor(gang)));
        t.color(nameColor(gang));
        if (!t.hasEntry(pl.getName())) t.addEntry(pl.getName());
    }

    public void refreshAll() { for (Player pl : Bukkit.getOnlinePlayers()) refresh(pl); }

    @EventHandler public void onJoin(PlayerJoinEvent e) { Bukkit.getScheduler().runTaskLater(p, () -> refresh(e.getPlayer()), 5L); }

    @EventHandler public void onChat(AsyncChatEvent e) {
        String gang = p.gang.gangOf(e.getPlayer().getUniqueId());
        Component tag = gang == null ? Component.empty() : U.L.deserialize(prefixFor(gang));
        e.renderer(ChatRenderer.viewerUnaware((source, name, msg) -> tag.append(name).append(U.L.deserialize(" &8» &f")).append(msg)));
    }
}

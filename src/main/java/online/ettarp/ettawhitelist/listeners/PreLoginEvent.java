package online.ettarp.ettawhitelist.listeners;

import online.ettarp.ettawhitelist.EttaWhitelist;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.*;

public class PreLoginEvent implements Listener {
    private final EttaWhitelist plugin;
    private final Connection connection;

    public PreLoginEvent(EttaWhitelist plugin) {
        this.plugin = plugin;

        try {
            connection = plugin.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @EventHandler
    public void onPlayerConnect(AsyncPlayerPreLoginEvent event) {
        String target = event.getName();

        if(plugin.getServer().getPlayer(target) != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, plugin.getConfig().getString("text.already-in-server"));
        }

        try {
            PreparedStatement season = connection.prepareStatement("SELECT * FROM season_whitelist WHERE nickname = ?");
            season.setString(1, target);

            PreparedStatement month = connection.prepareStatement("SELECT * FROM whitelist WHERE nickname = ?");
            month.setString(1, target);

            ResultSet seasonResult = season.executeQuery();
            ResultSet monthResult = month.executeQuery();

            if(!seasonResult.next() && !monthResult.next()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, plugin.getConfig().getString("text.not-in-whitelist"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

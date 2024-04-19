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

            PreparedStatement endless = connection.prepareStatement("SELECT * FROM whitelist WHERE nickname = ?");
            endless.setString(1, target);

            ResultSet seasonResult = season.executeQuery();
            ResultSet monthResult = month.executeQuery();
            ResultSet endlessResult = endless.executeQuery();

            boolean isSeason = seasonResult.next(),
                    isMonth = monthResult.next(),
                    isEndless = endlessResult.next();

            if(!isSeason && !isMonth && !isEndless) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, plugin.getConfig().getString("text.not-in-whitelist"));
            }

            PreparedStatement statement;
            if(isSeason) {
                statement = connection.prepareStatement("UPDATE season_whitelist SET uuid = ? WHERE nickname = ?");

                statement.setString(1, event.getUniqueId().toString());
                statement.setString(2, event.getName());

                statement.execute();
            }
            else if(isMonth) {
                statement = connection.prepareStatement("UPDATE whitelist SET uuid = ? WHERE nickname = ?");

                statement.setString(1, event.getUniqueId().toString());
                statement.setString(2, event.getName());

                statement.execute();
            }
            else if(isEndless) {
                statement = connection.prepareStatement("UPDATE endless_whitelist SET uuid = ? WHERE nickname = ?");

                statement.setString(1, event.getUniqueId().toString());
                statement.setString(2, event.getName());

                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package online.ettarp.ettawhitelist;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import online.ettarp.ettawhitelist.commands.WhitelistCommand;
import online.ettarp.ettawhitelist.commands.WhitelistCompleter;
import online.ettarp.ettawhitelist.db.DBHandler;
import online.ettarp.ettawhitelist.listeners.PreLoginEvent;
import online.ettarp.ettawhitelist.runnable.WhitelistChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class EttaWhitelist extends JavaPlugin {
    DBHandler handler;
    TextChannel notificationChannel;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        handler = new DBHandler(this);

        getServer().getScheduler().runTaskAsynchronously(this, () -> {

            try {
                PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `whitelist` (`nickname` varchar(255) NOT NULL, `uuid` varchar(255), `date` datetime NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
                statement.executeUpdate();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `season_whitelist` (`nickname` varchar(255) NOT NULL, `uuid` varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `endless_whitelist` (`nickname` varchar(255) NOT NULL, `uuid` varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        new WhitelistChecker(this).runTaskTimerAsynchronously(this, 0, 60*60*20);

        getCommand("ewhitelist").setExecutor(new WhitelistCommand(this));
        getCommand("ewhitelist").setTabCompleter(new WhitelistCompleter());

        getServer().getPluginManager().registerEvents(new PreLoginEvent(this), this);
    }

    public TextChannel getNotificationChannel() {
        String channelId = this.getConfig().getString("discord.notification-channel");
        notificationChannel = DiscordUtil.getTextChannelById(channelId);
        return notificationChannel;
    }

    @Override
    public void onDisable() {
        try {
            handler.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return handler.getConnection();
    }
}

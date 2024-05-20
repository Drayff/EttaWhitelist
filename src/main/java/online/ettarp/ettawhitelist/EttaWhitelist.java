package online.ettarp.ettawhitelist;

import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import online.ettarp.ettawhitelist.commands.WhitelistCommand;
import online.ettarp.ettawhitelist.commands.WhitelistCompleter;
import online.ettarp.ettawhitelist.db.DBHandler;
import online.ettarp.ettawhitelist.db.NotifiedUserManager;
import online.ettarp.ettawhitelist.listeners.PreLoginEvent;
import online.ettarp.ettawhitelist.runnable.WhitelistChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class EttaWhitelist extends JavaPlugin {
    DBHandler handler;
    TextChannel notificationChannel;

    NotifiedUserManager nUM;

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

        new WhitelistChecker(this).runTaskTimerAsynchronously(this, 20 * 60 * 10, 20 * 60 * 60 * 4);
        nUM = new NotifiedUserManager(this);

        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                nUM.loadNotifiedUsers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                nUM.saveNotifiedUsers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 20 * 60L, 20 * 60 * 60L);

        getCommand("ewhitelist").setExecutor(new WhitelistCommand(this));
        getCommand("ewhitelist").setTabCompleter(new WhitelistCompleter());

        getServer().getPluginManager().registerEvents(new PreLoginEvent(this), this);
    }

    public NotifiedUserManager getNUM() {
        return nUM;
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

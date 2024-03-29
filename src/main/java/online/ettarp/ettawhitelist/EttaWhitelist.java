package online.ettarp.ettawhitelist;

import online.ettarp.ettawhitelist.db.DBHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public final class EttaWhitelist extends JavaPlugin {
    DBHandler handler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        handler = new DBHandler(this);

        try {
            getConnection().createStatement().execute("CREATE TABLE IF NOT EXISTS `whitelist` (`nickname` varchar(255) NOT NULL, `date` datetime NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            getConnection().createStatement().execute("CREATE TABLE IF NOT EXISTS `season_whitelist` (`nickname` varchar(255) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

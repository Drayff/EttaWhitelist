package online.ettarp.ettawhitelist.runnable;

import online.ettarp.ettawhitelist.EttaWhitelist;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.time.LocalDate;

public class WhitelistChecker extends BukkitRunnable {
    private final EttaWhitelist plugin;
    private final Connection connection;

    public WhitelistChecker(EttaWhitelist plugin) {
        this.plugin = plugin;

        try {
            connection = plugin.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM whitelist;");

            while (resultSet.next()) {
                Player target = plugin.getServer().getPlayer(resultSet.getString("nickname"));
                LocalDate date = resultSet.getDate("date").toLocalDate();

                if(LocalDate.now().isAfter(date)) {
                    if(target != null) {
                        target.kickPlayer(plugin.getConfig().getString("text.is-over"));
                    }

                    statement.execute("DELETE FROM whitelist WHERE nickname = '" + target.getName() + "'");
                }
            }

            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

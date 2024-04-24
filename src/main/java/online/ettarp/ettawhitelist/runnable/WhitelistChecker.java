package online.ettarp.ettawhitelist.runnable;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import online.ettarp.ettawhitelist.EttaWhitelist;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

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
                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("uuid")));
                LocalDate date = resultSet.getDate("date").toLocalDate();

                long daysDifference = ChronoUnit.DAYS.between(LocalDate.now(), date);
                long absDaysDifference = Math.abs(daysDifference);

                User user = DiscordUtil.getUserById(DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(UUID.fromString(resultSet.getString("uuid"))));
                String userAsTag = String.format("<@%s>", user.getId());

                if(absDaysDifference == 7) {
                    plugin.getNotificationChannel().sendMessageEmbeds(
                            buildEmbed(absDaysDifference, offlineTarget)
                    ).content(userAsTag).queue();
                }

                if(absDaysDifference == 3) {
                    plugin.getNotificationChannel().sendMessageEmbeds(
                            buildEmbed(absDaysDifference, offlineTarget)
                    ).content(userAsTag).queue();
                }

                if(absDaysDifference == 1) {
                    plugin.getNotificationChannel().sendMessageEmbeds(
                            buildEmbed(absDaysDifference, offlineTarget)
                    ).content(userAsTag).queue();
                }

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

    public MessageEmbed buildEmbed(long days, OfflinePlayer player) {
        EmbedBuilder builder = new EmbedBuilder();

        String daysAsText;
        if(days == 7) {
            daysAsText = "дней";
        } else if (days == 3) {
            daysAsText = "дня";
        } else {
            daysAsText = "день";
        }
        String warn = String.format("Ваша проходка закончится через %d %s", days, daysAsText);
        String buy = "Оплатите продление проходки на сайте:\nhttps://shop.ettarp.com/";
        String urlSkin = String.format("http://cravatar.eu/helmhead/%s.png", player.getName());

        builder.setAuthor(warn, urlSkin, urlSkin);
        builder.setDescription(buy);

        return builder.build();
    }
}

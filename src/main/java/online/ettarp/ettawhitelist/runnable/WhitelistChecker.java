package online.ettarp.ettawhitelist.runnable;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import online.ettarp.ettawhitelist.EttaWhitelist;
import online.ettarp.ettawhitelist.db.NotifiedUserManager;
import online.ettarp.ettawhitelist.models.NotifiedUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class WhitelistChecker extends BukkitRunnable {
    private final EttaWhitelist plugin;
    private final Connection connection;
    private final NotifiedUserManager nUM;

    public WhitelistChecker(EttaWhitelist plugin) {
        this.plugin = plugin;
        this.nUM = plugin.getNUM();
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

                long daysDifference = ChronoUnit.DAYS.between(LocalDate.now(), date);
                long absDaysDifference = Math.abs(daysDifference);

                if(resultSet.getString("uuid") != null) {

                    OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("uuid")));

                    User user = DiscordUtil.getUserById(DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(UUID.fromString(resultSet.getString("uuid"))));
                    NotifiedUser notifiedUser = nUM.getNotifiedUser(resultSet.getString("uuid"));

                    if (user == null) return;

                    if(notifiedUser == null) {
                        notifiedUser = new NotifiedUser(resultSet.getString("uuid"), false, false, false);
                    }

                    String userAsTag = String.format("<@%s>", user.getId());
                    if(absDaysDifference == 7 && !notifiedUser.getWeeklyNotified()) {
                        plugin.getNotificationChannel().sendMessageEmbeds(
                                buildEmbed(absDaysDifference, offlineTarget)
                        ).content(userAsTag).queue();
                        notifiedUser.setWeeklyNotified(true);
                    }

                    if(absDaysDifference == 3 && !notifiedUser.getThreeDaysNotified()) {
                        plugin.getNotificationChannel().sendMessageEmbeds(
                                buildEmbed(absDaysDifference, offlineTarget)
                        ).content(userAsTag).queue();
                        notifiedUser.setThreeDaysNotified(true);
                    }

                    if(absDaysDifference == 1 && !notifiedUser.getOneDayNotified()) {
                        plugin.getNotificationChannel().sendMessageEmbeds(
                                buildEmbed(absDaysDifference, offlineTarget)
                        ).content(userAsTag).queue();
                        notifiedUser.setOneDayNotified(true);
                    }
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

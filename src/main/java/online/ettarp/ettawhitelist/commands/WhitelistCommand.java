package online.ettarp.ettawhitelist.commands;

import online.ettarp.ettawhitelist.EttaWhitelist;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.*;
import java.time.LocalDate;

public class WhitelistCommand implements CommandExecutor {
    private final EttaWhitelist plugin;
    private final Connection connection;

    public WhitelistCommand(EttaWhitelist plugin) {
        this.plugin = plugin;

        try {
            connection = plugin.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("whitelist.command")) {
            sender.sendMessage("У вас нет прав на выполнение этой команды.");
            return true;
        }

        String target;

        if (args == null || args.length == 0) {
            sender.sendMessage("Использование: /ewhitelist [add, remove] [season, month] <Target> <Month Count>.");
            return true;
        }

        switch (args[0]) {
            case "add":
                if (args.length < 2) {
                    sender.sendMessage("Использование: /ewhitelist add [season, month] <Target> <Month Count>.");
                    return true;
                }

                target = args[2];

                if(args[1].equals("season")) {
                    if (args.length != 3) {
                        sender.sendMessage("Использование: /ewhitelist add season <Target>.");
                        return true;
                    }

                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            PreparedStatement statement = connection.prepareStatement("SELECT * FROM season_whitelist WHERE nickname = ?");
                            statement.setString(1, target);
                            ResultSet seasonResult = statement.executeQuery();

                            if (seasonResult.next()) {
                                sender.sendMessage("Игрок уже есть в вайтлисте.");
                                return;
                            }

                            statement = connection.prepareStatement("INSERT INTO season_whitelist (nickname) VALUES (?);");
                            statement.setString(1, target);
                            statement.execute();

                            sender.sendMessage("Игрок успешно добавлен в вайтлист.");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else if(args[1].equals("month")) {
                    if (args.length != 4) {
                        sender.sendMessage("Использование: /ewhitelist add month <Target> <Month Count>.");
                        return true;
                    }

                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            PreparedStatement statement = connection.prepareStatement("SELECT * FROM whitelist WHERE nickname = ?");
                            statement.setString(1, target);
                            ResultSet monthResult = statement.executeQuery();

                            if (monthResult.next()) {
                                sender.sendMessage("Игрок уже есть в вайтлисте.");
                                return;
                            }

                            statement = connection.prepareStatement("INSERT INTO whitelist (nickname, date) VALUES (?, ?);");
                            statement.setString(1, target);
                            statement.setDate(2, java.sql.Date.valueOf(LocalDate.now().plusDays(30L * Integer.parseInt(args[3]))));
                            statement.execute();

                            sender.sendMessage("Игрок успешно добавлен в вайтлист.");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    sender.sendMessage("Использование: /ewhitelist add [season, month] <Target> <Month Count>.");
                }

                break;

            case "remove":
                if (args.length != 2) {
                    sender.sendMessage("Использование: /ewhitelist remove <Target>.");
                    return true;
                }

                target = args[1];
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        PreparedStatement season = connection.prepareStatement("SELECT * FROM season_whitelist WHERE nickname = ?");
                        season.setString(1, target);

                        PreparedStatement month = connection.prepareStatement("SELECT * FROM whitelist WHERE nickname = ?");
                        month.setString(1, target);

                        ResultSet seasonResult = season.executeQuery();
                        ResultSet monthResult = month.executeQuery();

                        PreparedStatement statement;

                        if (seasonResult.next() || monthResult.next()) {
                            statement = connection.prepareStatement("DELETE FROM season_whitelist WHERE nickname = ?");
                            statement.setString(1, target);
                            statement.execute();

                            statement = connection.prepareStatement("DELETE FROM whitelist WHERE nickname = ?");
                            statement.setString(1, target);
                            statement.execute();

                            Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                                if (plugin.getServer().getPlayer(target) != null) {
                                    plugin.getServer().getPlayer(target).kickPlayer(plugin.getConfig().getString("text.deleted-from-whitelist"));
                                }
                            });

                            sender.sendMessage("Игрок успешно удалён.");
                        } else {
                            sender.sendMessage("Игрок не в вайтлисте.");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                break;
        }
        return true;
    }
}

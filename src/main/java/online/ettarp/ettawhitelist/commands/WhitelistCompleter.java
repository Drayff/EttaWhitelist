package online.ettarp.ettawhitelist.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class WhitelistCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> completions = new ArrayList<>();

        if(args.length == 1) {
            completions.add("add");
            completions.add("remove");
        } else if(args.length == 2) {
            if(args[0].equals("add")) {
                completions.add("season");
                completions.add("month");
                completions.add("endless");
            } else {
                completions = null;
            }
        } else if(args.length == 3) {
            if(args[0].equals("add")) {
                completions = null;
            }
        }

        return completions;
    }
}

package me.m1dnightninja.skinsetter.spigot;

import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.skinsetter.common.SkinUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkinCommand implements CommandExecutor, TabCompleter {

    private final SkinUtil util;

    public SkinCommand() {
        this.util = new SkinUtil();
    }

    private final List<String> subcommands = Arrays.asList("set", "reset", "save");

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {

        List<String> suggestions = new ArrayList<>();

        switch (args.length) {
            case 0:
            case 1:
                for(String str : subcommands) {
                    if(sender.hasPermission("skinstter.command." + str)) suggestions.add(str);
                }
                break;
            case 2:
                if(subcommands.contains(args[0]) && sender.hasPermission("skinsetter.command." + args[0])) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        suggestions.add(p.getDisplayName());
                    }
                }
                break;
            case 3:
                if(args[0].equals("set")) {
                    suggestions.addAll(util.getSkinNames());
                }
        }

        List<String> out = new ArrayList<>();
        for(String sug : suggestions) {
            if(sug.startsWith(args[args.length - 1])) {
                out.add(sug);
            }
        }

        return out;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {

        if(!sender.hasPermission("skinsetter.command")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
        }

        if(args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /skin <set/reset/save> <player> [id]");
            return true;
        }

        if(!sender.hasPermission("skinsetter.command." + args[0])) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
        }

        switch(args[0]) {
            case "set":
                if(args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skin set <player> [id/name]");
                    return true;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                String id = args[2];

                if(p == null) {
                    sender.sendMessage(ChatColor.RED + "That is not a valid player!");
                    return true;
                }
                Skin skin = util.getSavedSkin(id);
                if(skin == null) {

                    sender.sendMessage(ChatColor.GREEN + "Attempting to retrieve skin for player " + id + "...");
                    util.getSkinOnline(id, (uid, oskin) -> {
                        if(oskin == null) {
                            sender.sendMessage(ChatColor.RED + "That is not a valid player name!");
                            return;
                        }
                        util.setSkin(p.getUniqueId(), oskin);
                        sender.sendMessage(ChatColor.GREEN + "Skin set");
                    });

                } else {
                    util.setSkin(p.getUniqueId(), skin);
                    sender.sendMessage(ChatColor.GREEN + "Skin set");
                }

                break;
            case "reset":
                if(args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skin reset <player>");
                    return true;
                }

                Player r_p = Bukkit.getPlayerExact(args[1]);

                if(r_p == null) {
                    sender.sendMessage(ChatColor.RED + "That is not a valid player!");
                    return true;
                }

                util.resetSkin(r_p.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Skin reset");

                break;
            case "save":
                if(args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skin save <player> [id]");
                    return true;
                }

                Player s_p = Bukkit.getPlayerExact(args[1]);
                String s_id = args[2];

                if(s_p == null) {
                    sender.sendMessage(ChatColor.RED + "That is not a valid player!");
                    return true;
                }

                util.saveSkin(s_p.getUniqueId(), s_id);
                sender.sendMessage(ChatColor.GREEN + "Skin saved");

        }

        return true;
    }

}

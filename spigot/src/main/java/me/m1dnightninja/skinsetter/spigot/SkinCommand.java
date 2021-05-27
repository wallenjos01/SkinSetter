package me.m1dnightninja.skinsetter.spigot;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.SkinUtil;
import me.m1dnightninja.skinsetter.spigot.integration.CitizensIntegration;
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

    private final boolean CITIZENS_ENABLED;
    private final SkinUtil util;

    private final List<String> subcommands = Arrays.asList("set", "reset", "save", "reload");

    public SkinCommand(SkinUtil util) {
        this.util = util;
        this.CITIZENS_ENABLED = Bukkit.getPluginManager().isPluginEnabled("citizens");

        if(CITIZENS_ENABLED) {
            subcommands.add("setnpc");
        }

    }

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
                if(args[0].equals("reload")) break;
                if(subcommands.contains(args[0]) && sender.hasPermission("skinsetter.command." + args[0])) {
                    if (CITIZENS_ENABLED && args[0].equals("setnpc")) {
                        suggestions.addAll(util.getSkinNames());
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            suggestions.add(p.getDisplayName());
                        }
                    }
                }
                break;
            case 3:
                if(args[0].equals("reload")) break;
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
            sendArgs(sender);
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
                        util.setSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(p.getUniqueId()), oskin);
                        sender.sendMessage(ChatColor.GREEN + "Skin set");
                    });

                } else {
                    util.setSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(p.getUniqueId()), skin);
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

                util.resetSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(r_p.getUniqueId()));
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

                util.saveSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(s_p.getUniqueId()), s_id);
                sender.sendMessage(ChatColor.GREEN + "Skin saved");
                break;

            case "setnpc":
                if(!CITIZENS_ENABLED) {
                    sendArgs(sender);
                }
                if(!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use that command!");
                    return true;
                }
                if(args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skin setnpc <skin>");
                    return true;
                }

                Skin sn_s = util.getSavedSkin(args[1]);

                CitizensIntegration.setNPCSkin((Player) sender, sn_s).send(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(((Player) sender).getUniqueId()));
                break;

            case "reload":

                long time = System.currentTimeMillis();
                SkinSetterAPI.getInstance().reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "SkinSetter reloaded in " + (System.currentTimeMillis() - time) + "ms");

                break;

        }

        return true;
    }

    private void sendArgs(CommandSender sender) {

        StringBuilder builder = new StringBuilder(ChatColor.RED + "Usage: /skin <");
        int found = 0;
        for(String cmd : subcommands) {
            if(sender.hasPermission("skinsetter.command." + cmd)) {
                if(found > 0) {
                    builder.append("/");
                }
                builder.append(cmd);
                found++;
            }
        }
        builder.append(">");

        if(found == 0) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
        }

        sender.sendMessage(ChatColor.RED + builder.toString());

    }

}

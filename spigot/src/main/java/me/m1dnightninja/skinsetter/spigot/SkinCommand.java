package me.m1dnightninja.skinsetter.spigot;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.module.lang.LangModule;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import me.m1dnightninja.skinsetter.api.SavedSkin;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.util.SkinUtil;
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

    private final List<String> subcommands = Arrays.asList("set", "reset", "save", "reload", "setdefault", "cleardefault", "persistence", "setrandom", "edit", "head");

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
                // Only list subcommands the player can use
                for(String str : subcommands) {
                    if(sender.hasPermission("skinstter.command." + str)) suggestions.add(str);
                }
                break;

            case 2:
                // No options for reload or cleardefault
                if(args[0].equals("reload") || args[0].equals("cleardefault")) break;

                // Only list options for subcommands the player has
                if(subcommands.contains(args[0]) && sender.hasPermission("skinsetter.command." + args[0])) {

                    // List skin names
                    if ((CITIZENS_ENABLED && args[0].equals("setnpc")) ||
                            args[0].equals("setdefault") ||
                            args[0].equals("edit") ||
                            args[0].equals("head")) {

                        MPlayer mpl = null;
                        if(sender instanceof Player) {
                            mpl = SpigotPlayer.wrap(((Player) sender));
                        }
                        suggestions.addAll(util.getSkinNames(mpl));

                    // Enable and disable
                    } else if(args[0].equals("persistence")) {

                        suggestions.add("enable");
                        suggestions.add("disable");

                    // List player names
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            suggestions.add(p.getDisplayName());
                        }
                    }
                }
                break;

            case 3:
                switch (args[0]) {
                    case "set":
                        // List players
                        MPlayer mpl = null;
                        if (sender instanceof Player) {
                            mpl = SpigotPlayer.wrap(((Player) sender));
                        }
                        suggestions.addAll(util.getSkinNames(mpl));
                        break;

                    case "setrandom":
                        // List skin groups
                        mpl = null;
                        if (sender instanceof Player) {
                            mpl = SpigotPlayer.wrap(((Player) sender));
                        }
                        suggestions.addAll(util.getGroupNames(mpl, true));
                        break;

                    case "edit":
                        // Skin editing options
                        suggestions.add("item");
                        suggestions.add("name");
                        suggestions.add("groups");
                        suggestions.add("excludeInRandom");
                        break;

                    case "head":
                        for(Player p : Bukkit.getOnlinePlayers()) {
                            suggestions.add(p.getName());
                        }
                        break;
                }
                break;
            case 4:
                if(args[0].equals("set")) {
                    // Set online flag
                    suggestions.add("-o");

                } else if(args[0].equals("edit")) {

                    // Skin editing options
                    switch (args[2]) {

                        // Save and clear items
                        case "item":
                            suggestions.add("clear");
                            suggestions.add("save");
                            break;

                        // Add and remove groups
                        case "groups":
                            suggestions.add("add");
                            suggestions.add("remove");
                            break;

                        // Exclude in random selections
                        case "excludeInRandom":
                            suggestions.add("true");
                            suggestions.add("false");
                            break;
                    }
                }

            case 5:
                if(args[0].equals("edit") && args[2].equals("groups") && args[3].equals("remove")) {

                    // List groups a skin has
                    SavedSkin skin = util.getSavedSkin(args[1]);
                    if(skin != null) suggestions.addAll(skin.getGroups());
                }
        }

        // Filter suggestions
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

        // Check permissions
        if(!sender.hasPermission("skinsetter.command")) {
            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_permission");
            return true;
        }

        // Check usage
        if(args.length == 0) {
            sendArgs(sender);
            return true;
        }

        // Check permissions for subcommand
        if(!sender.hasPermission("skinsetter.command." + args[0])) {
            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_permission");
            return true;
        }

        switch(args[0]) {

            // Set subcommand
            case "set":

                if(args.length < 2) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin set <player> [id/name] (-o)"));
                    return true;
                }

                Player p = Bukkit.getPlayerExact(args[1]);

                if(p == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                MPlayer mp = SpigotPlayer.wrap(p);

                if(args.length < 3) {
                    if(!(sender instanceof Player)) {
                        LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin set <player> [id/name] (-o)"));
                        return true;
                    }

                    util.openGUI(mp, SpigotPlayer.wrap((Player) sender));
                    return true;
                }

                String id = args[2];
                SavedSkin saved = util.getSavedSkin(id);
                Skin skin;

                boolean original  = (args.length > 3 && args[3].equals("-o"));

                if(original || saved == null) {

                    Player other = Bukkit.getPlayerExact(id);
                    if(other == null || (original && !Bukkit.getServer().getOnlineMode())) {

                        LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.online", new CustomPlaceholderInline("name", id));

                        util.getSkinOnline(id, (uid, oskin) -> {
                            if (oskin == null) {
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                                return;
                            }
                            util.setSkin(mp, oskin);
                            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);
                        });

                    } else {

                        MPlayer mo = SpigotPlayer.wrap(other);
                        skin = original ? util.getLoginSkin(mo) : util.getSkin(mo);

                        util.setSkin(mp, skin);
                        LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);

                    }

                } else {

                    util.setSkin(mp, saved.getSkin());
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);
                }

                break;

            // Reset subcommand
            case "reset":

                if(args.length != 2) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin reset <player>"));
                    return true;
                }

                p = Bukkit.getPlayerExact(args[1]);

                if(p == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                mp = SpigotPlayer.wrap(p);

                util.resetSkin(mp);
                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.reset.result", mp);

                break;

            // Save subcommand
            case "save":

                if(args.length != 3) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin save <player> [id]"));
                    return true;
                }

                p = Bukkit.getPlayerExact(args[1]);
                id = args[2];

                if(util.getSavedSkin(id) != null && !sender.hasPermission("skinsetter.overwrite_skins")) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_overwrite");
                    return true;
                }

                if(p == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                mp = SpigotPlayer.wrap(p);

                util.saveSkin(mp, id);
                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.save.result", mp, new CustomPlaceholderInline("id", id));
                break;

            // Set NPC subcommand
            case "setnpc":

                if(!CITIZENS_ENABLED) {
                    sendArgs(sender);
                    return true;
                }
                if(!(sender instanceof Player)) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.not_player");
                    return true;
                }
                if(args.length != 2) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin setnpc <skin>"));
                    return true;
                }

                saved = util.getSavedSkin(args[1]);
                if(saved == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                    return true;
                }
                skin = saved.getSkin();

                CitizensIntegration.setNPCSkin((Player) sender, skin).send(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(((Player) sender).getUniqueId()));
                break;

            // Reload subcommand
            case "reload":

                long time = System.currentTimeMillis();
                SkinSetterAPI.getInstance().reloadConfig();
                time = System.currentTimeMillis() - time;
                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.reload.result", new CustomPlaceholderInline("time", time+""));

                break;

            // Set default subcommand
            case "setdefault":

                if(args.length != 2) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin setdefault <skin>"));
                    return true;
                }

                id = args[1];
                saved = SkinSetterAPI.getInstance().getSkinManager().getSkin(id);

                if(saved == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                    return true;
                }

                SkinSetterAPI.getInstance().setDefaultSkin(saved);
                SkinSetterAPI.getInstance().getConfig().set("default_skin", id);
                SkinSetterAPI.getInstance().saveConfig();

                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.setdefault.result", new CustomPlaceholderInline("id", id));

                break;

            // Clear default subcommand
            case "cleardefault":

                SkinSetterAPI.getInstance().setDefaultSkin(null);
                SkinSetterAPI.getInstance().getConfig().set("default_skin", "");
                SkinSetterAPI.getInstance().saveConfig();

                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.cleardefault.result");

                break;

            // Persistence subcommand
            case "persistence":

                if(args.length != 2) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin persistence <enable/disable>"));
                    return true;
                }

                String action = args[1];
                if(action.equals("enable")) {

                    SkinSetterAPI.getInstance().setPersistenceEnabled(true);
                    SkinSetterAPI.getInstance().getConfig().set("persistent_skins", true);
                    SkinSetterAPI.getInstance().saveConfig();

                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.persistence.result.enabled");

                } else if(action.equals("disable")) {

                    SkinSetterAPI.getInstance().setPersistenceEnabled(false);
                    SkinSetterAPI.getInstance().getConfig().set("persistent_skins", false);
                    SkinSetterAPI.getInstance().saveConfig();

                    IPlayerDataModule mod = MidnightCoreAPI.getInstance().getModule(IPlayerDataModule.class);

                    for(MPlayer pl : MidnightCoreAPI.getInstance().getPlayerManager()) {

                        mod.getGlobalProvider().getPlayerData(pl.getUUID()).set("skinsetter", null);
                        mod.getGlobalProvider().savePlayerData(pl.getUUID());
                    }

                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.persistence.result.disabled");

                } else {

                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin persistence <enable/disable>"));
                }

                break;

            // Set random subcommand
            case "setrandom":

                MPlayer mpl = null;
                if(sender instanceof Player) {
                    mpl = SpigotPlayer.wrap(((Player) sender));
                }

                String group = null;
                if(args.length > 2) {
                    group = args[2];
                }

                saved = util.getRandomSkin(mpl, group);
                if(saved == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_saved");
                    return true;
                }

                p = Bukkit.getPlayerExact(args[1]);
                if(p == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                mp = SpigotPlayer.wrap(p);
                util.setSkin(mp, saved.getSkin());

                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);

                break;

            // Edit subcommand
            case "edit":

                // Send error if improperly formatted
                if(args.length < 3) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> <item/name/groups/excludeInRandom>"));
                    return true;
                }

                saved = util.getSavedSkin(args[1]);
                if(saved == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                    return true;
                }

                // Determine subcommand
                switch(args[2]) {

                    case "item":

                        if(args.length < 4) {
                            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> item <clear/save>"));
                            return true;
                        }

                        switch (args[3]) {
                            case "save":

                                if(!(sender instanceof Player)) {
                                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.not_player");
                                    return true;
                                }

                                mpl = SpigotPlayer.wrap((Player) sender);

                                MItemStack is = mpl.getItemInMainHand();
                                if(is.getType().equals(MIdentifier.parseOrDefault("air"))) {
                                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_item");
                                    return true;
                                }

                                saved.setCustomItem(mpl.getItemInMainHand());
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.item.save.result", saved);

                                break;
                            case "clear":

                                saved.setCustomItem(null);
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.item.clear.result", saved);

                                break;
                            default:
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> item <clear/save>"));
                                return true;
                        }
                        break;

                    case "name":

                        if(args.length < 4) {
                            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> name <name>"));
                            return true;
                        }

                        StringBuilder builder = new StringBuilder();
                        for(int i = 3 ; i < args.length ; i++) {
                            if(i > 3) {
                                builder.append(" ");
                            }
                            builder.append(args[i]);
                        }

                        MComponent name = MComponent.Serializer.parse(builder.toString());
                        saved.setName(name);

                        LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.name.result", saved);
                        break;

                    case "groups":

                        if(args.length < 5) {
                            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> groups <add/remove> <group>"));
                            return true;
                        }

                        String groupName = args[4];
                        switch (args[3]) {
                            case "add":

                                saved.addGroup(groupName);

                                break;
                            case "remove":

                                saved.getGroups().remove(groupName);

                                break;
                            default:
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> groups <add/remove> <group>"));
                                return true;
                        }

                        LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.groups.result", saved);
                        break;

                    case "excludeInRandom":

                        if(args.length < 4) {
                            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> excludeInRandom <true/false>"));
                            return true;
                        }

                        switch (args[3]) {
                            case "true":

                                saved.setInRandom(false);
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.excludeInRandom.result.enabled", saved);

                                break;
                            case "false":

                                saved.setInRandom(true);
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.excludeInRandom.result.disabled", saved);

                                break;

                            default:
                                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> excludeInRandom <true/false>"));
                                return true;
                        }
                        break;

                    default:
                        // Invalid subcommand
                        LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin edit <skin> <item/name/groups/excludeInRandom>"));
                        return true;

                }
                break;

            case "head":

                if(args.length < 3) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", "/skin head <skin> <player>"));
                    return true;
                }

                saved = util.getSavedSkin(args[1]);
                if(saved == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                    return true;
                }

                p = Bukkit.getPlayerExact(args[2]);

                if(p == null) {
                    LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                mpl = SpigotPlayer.wrap(p);
                mpl.giveItem(saved.getHeadItem());

                LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.head.result", saved);
                break;

            default:
                sendArgs(sender);
        }

        return true;
    }

    private void sendArgs(CommandSender sender) {

        StringBuilder builder = new StringBuilder(ChatColor.RED + "/skin <");
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
            LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_permission");
        }

        LangModule.sendMessage(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", new CustomPlaceholderInline("usage", builder.toString()));

    }

}

package org.wallentines.skinsetter.spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.player.DataProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.util.MojangUtil;
import org.wallentines.midnightcore.spigot.item.SpigotItem;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.midnightcore.spigot.util.CommandUtil;
import org.wallentines.skinsetter.api.EditableSkin;
import org.wallentines.skinsetter.api.SavedSkin;
import org.wallentines.skinsetter.api.SkinSetterAPI;
import org.wallentines.skinsetter.common.SavedSkinImpl;
import org.wallentines.skinsetter.common.util.GuiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final List<String> subcommands = Arrays.asList("set", "reset", "save", "reload", "setdefault", "cleardefault", "persistence", "setrandom", "edit", "head");

    private final SkinSetter plugin;

    public MainCommand(SkinSetter plugin) {
        this.plugin = plugin;
    }


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        switch (args.length) {
            case 0:
            case 1:
                // Only list subcommands the player can use
                for(String str : subcommands) {
                    if(sender.hasPermission("skinsetter.command." + str)) suggestions.add(str);
                }
                break;

            case 2:
                // No options for reload or cleardefault
                if(args[0].equals("reload") || args[0].equals("cleardefault")) break;

                // Only list options for subcommands the player has
                if(subcommands.contains(args[0]) && sender.hasPermission("skinsetter.command." + args[0])) {

                    // List skin names
                    if (args[0].equals("setdefault") ||
                        args[0].equals("edit") ||
                        args[0].equals("head")) {

                        MPlayer mpl = null;
                        if(sender instanceof Player) {
                            mpl = SpigotPlayer.wrap(((Player) sender));
                        }
                        suggestions.addAll(plugin.getAPI().getSkinRegistry().getSkinNames(mpl));

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
                        suggestions.addAll(plugin.getAPI().getSkinRegistry().getSkinNames(mpl));
                        break;

                    case "setrandom":
                        // List skin groups
                        mpl = null;
                        if (sender instanceof Player) {
                            mpl = SpigotPlayer.wrap(((Player) sender));
                        }
                        suggestions.addAll(plugin.getAPI().getSkinRegistry().getGroupNames(mpl));
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
                    SavedSkin skin = plugin.getAPI().getSkinRegistry().getSkin(args[1]);
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
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String... args) {
        // Check permissions
        if(!sender.hasPermission("skinsetter.command")) {
            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_permission");
            return true;
        }

        // Check usage
        if(args.length == 0) {
            sendArgs(sender);
            return true;
        }

        // Check permissions for subcommand
        if(!sender.hasPermission("skinsetter.command." + args[0])) {
            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_permission");
            return true;
        }

        switch(args[0]) {

            // Set subcommand
            case "set":

                if(args.length < 2) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin set <player> [id/name] (-o)"));
                    return true;
                }

                Player p = Bukkit.getPlayerExact(args[1]);

                if(p == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                MPlayer mp = SpigotPlayer.wrap(p);

                if(args.length < 3) {
                    if(!(sender instanceof Player)) {
                        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin set <player> [id/name] (-o)"));
                        return true;
                    }

                    GuiUtil.openGUI(mp, plugin.getAPI().getLangProvider(), plugin.getAPI().getSkinRegistry().getSkins(mp, null), sk -> onCommand(sender, cmd, label, "set", args[1], sk.getId()));
                    return true;
                }

                String id = args[2];
                SavedSkin saved = plugin.getAPI().getSkinRegistry().getSkin(id);
                Skin skin;

                boolean original = args.length > 3 && args[3].equals("-o");

                if(original || saved == null) {

                    if(!mp.hasPermission("skinsetter.command.set.online")) {

                        mp.sendMessage(SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.invalid_skin", mp));
                        return true;
                    }

                    Player other = Bukkit.getPlayerExact(id);
                    if(other == null || (original && !Bukkit.getServer().getOnlineMode())) {

                        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.online", CustomPlaceholderInline.create("name", id));

                        new Thread(() -> {

                            UUID u = MojangUtil.getUUID(id);
                            Skin s = MojangUtil.getSkin(u);
                            if (s == null) {
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                                return;
                            }
                            mp.setSkin(s);
                            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);

                        }).start();

                    } else {

                        MPlayer mo = SpigotPlayer.wrap(other);

                        SkinModule mod = mo.getServer().getModule(SkinModule.class);

                        skin = original ? mod.getOriginalSkin(mo) : mod.getSkin(mo);

                        mp.setSkin(skin);
                        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);

                    }

                } else {

                    if(!saved.canUse(mp)) {

                        mp.sendMessage(SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.invalid_skin", mp));
                        return true;
                    }

                    mp.setSkin(saved.getSkin());
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);
                }

                break;

            // Reset subcommand
            case "reset":

                if(args.length != 2) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin reset <player>"));
                    return true;
                }

                p = Bukkit.getPlayerExact(args[1]);

                if(p == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                mp = SpigotPlayer.wrap(p);
                mp.resetSkin();

                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.reset.result", mp);

                break;

            // Save subcommand
            case "save":

                if(args.length != 3) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin save <player> [id]"));
                    return true;
                }

                p = Bukkit.getPlayerExact(args[1]);
                id = args[2];

                MPlayer mpl = null;
                if(sender instanceof Player) {
                    mpl = SpigotPlayer.wrap(((Player) sender));
                }

                saved = plugin.getAPI().getSkinRegistry().getSkin(id);
                if(saved != null && (!saved.canUse(mpl) || !sender.hasPermission("skinsetter.overwrite_skins"))) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_overwrite");
                    return true;
                }

                if(p == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                mp = SpigotPlayer.wrap(p);
                Skin mpSkin = mp.getSkin();

                if(mpSkin == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.null_skin", mp);
                    break;
                }

                SavedSkin sk = new SavedSkinImpl(id, mpSkin);
                plugin.getAPI().getSkinRegistry().registerSkin(sk);
                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.save.result", mp, CustomPlaceholderInline.create("id", id));
                break;

            // Reload subcommand
            case "reload":

                long time = System.currentTimeMillis();
                plugin.getAPI().reload();
                time = System.currentTimeMillis() - time;
                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.reload.result", CustomPlaceholderInline.create("time", time+""));

                break;

            // Set default subcommand
            case "setdefault":

                if(args.length != 2) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin setdefault <skin>"));
                    return true;
                }

                id = args[1];
                saved = SkinSetterAPI.getInstance().getSkinRegistry().getSkin(id);

                if(saved == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                    return true;
                }

                SkinSetterAPI.getInstance().setDefaultSkin(saved);
                SkinSetterAPI.getInstance().getConfig().set("default_skin", id);
                SkinSetterAPI.getInstance().saveConfig();

                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.setdefault.result", CustomPlaceholderInline.create("id", id));

                break;

            // Clear default subcommand
            case "cleardefault":

                SkinSetterAPI.getInstance().setDefaultSkin(null);
                SkinSetterAPI.getInstance().getConfig().set("default_skin", "");
                SkinSetterAPI.getInstance().saveConfig();

                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.cleardefault.result");

                break;

            // Persistence subcommand
            case "persistence":

                if(args.length != 2) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin persistence <enable/disable>"));
                    return true;
                }

                String action = args[1];
                if(action.equals("enable")) {

                    SkinSetterAPI.getInstance().setPersistenceEnabled(true);
                    SkinSetterAPI.getInstance().getConfig().set("persistent_skins", true);
                    SkinSetterAPI.getInstance().saveConfig();

                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.persistence.result.enabled");

                } else if(action.equals("disable")) {

                    SkinSetterAPI.getInstance().setPersistenceEnabled(false);
                    SkinSetterAPI.getInstance().getConfig().set("persistent_skins", false);
                    SkinSetterAPI.getInstance().saveConfig();

                    MServer server = MidnightCoreAPI.getRunningServer();
                    if(server == null) break;

                    DataProvider prov = SkinSetterAPI.getInstance().getDataProvider();
                    for(MPlayer pl : server.getPlayerManager()) {

                        prov.getData(pl).remove("skinsetter");
                        prov.saveData(pl);
                    }

                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.persistence.result.disabled");

                } else {

                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin persistence <enable/disable>"));
                }

                break;

            // Set random subcommand
            case "setrandom":

                mpl = null;
                if(sender instanceof Player) {
                    mpl = SpigotPlayer.wrap(((Player) sender));
                }

                String group = null;
                if(args.length > 2) {
                    group = args[2];
                }

                saved = plugin.getAPI().getSkinRegistry().getRandomSkin(mpl, group);
                if(saved == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_saved");
                    return true;
                }

                if(args.length > 1) {
                    p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                        return true;
                    }
                    mp = SpigotPlayer.wrap(p);
                } else {

                    mp = mpl;
                }

                mp.setSkin(saved.getSkin());

                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.set.result", mp);

                break;

            // Edit subcommand
            case "edit":

                // Send error if improperly formatted
                if(args.length < 3) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> <item/name/groups/excludeInRandom>"));
                    return true;
                }

                mpl = null;
                if(sender instanceof Player) {
                    mpl = SpigotPlayer.wrap(((Player) sender));
                }

                EditableSkin editable = plugin.getAPI().getSkinRegistry().createEditableSkin(args[1]);
                if(editable == null || !editable.canUse(mpl)) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                    return true;
                }

                // Determine subcommand
                switch(args[2]) {

                    case "item":

                        if(args.length < 4) {
                            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> item <clear/save>"));
                            return true;
                        }

                        switch (args[3]) {
                            case "save":

                                if(!(sender instanceof Player)) {
                                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.not_player");
                                    return true;
                                }

                                mpl = SpigotPlayer.wrap((Player) sender);

                                MItemStack is = mpl.getItemInMainHand();
                                if(((SpigotItem) is).getInternal().getType() == Material.AIR) {
                                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_item");
                                    return true;
                                }

                                editable.setDisplayItem(mpl.getItemInMainHand());
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.item.save.result", editable);

                                editable.save();

                                break;
                            case "clear":

                                editable.setDisplayItem(null);
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.item.clear.result", editable);

                                editable.save();

                                break;
                            default:
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> item <clear/save>"));
                                return true;
                        }
                        break;

                    case "name":

                        if(args.length < 4) {
                            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> name <name>"));
                            return true;
                        }

                        StringBuilder builder = new StringBuilder();
                        for(int i = 3 ; i < args.length ; i++) {
                            if(i > 3) {
                                builder.append(" ");
                            }
                            builder.append(args[i]);
                        }

                        MComponent name = MComponent.parse(builder.toString());
                        editable.setName(name);
                        editable.save();

                        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.name.result", editable);
                        break;

                    case "groups":

                        if(args.length < 5) {
                            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> groups <add/remove> <group>"));
                            return true;
                        }

                        String groupName = args[4];
                        switch (args[3]) {
                            case "add":

                                editable.addGroup(groupName);
                                editable.save();

                                break;
                            case "remove":

                                editable.removeGroup(groupName);
                                editable.save();

                                break;
                            default:
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> groups <add/remove> <group>"));
                                return true;
                        }

                        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.groups.result", editable);
                        break;

                    case "excludeInRandom":

                        if(args.length < 4) {
                            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> excludeInRandom <true/false>"));
                            return true;
                        }

                        switch (args[3]) {
                            case "true":

                                editable.excludeFromRandom(true);
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.excludeInRandom.result.enabled", editable);

                                editable.save();
                                break;
                            case "false":

                                editable.excludeFromRandom(false);
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.edit.excludeInRandom.result.disabled", editable);

                                editable.save();
                                break;

                            default:
                                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> excludeInRandom <true/false>"));
                                return true;
                        }
                        break;

                    default:
                        // Invalid subcommand
                        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin edit <skin> <item/name/groups/excludeInRandom>"));
                        return true;

                }
                break;

            case "head":

                if(args.length < 3) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", "/skin head <skin> <player>"));
                    return true;
                }

                saved = plugin.getAPI().getSkinRegistry().getSkin(args[1]);
                if(saved == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                    return true;
                }

                p = Bukkit.getPlayerExact(args[2]);

                if(p == null) {
                    CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_name");
                    return true;
                }

                mpl = SpigotPlayer.wrap(p);
                mpl.giveItem(saved.getHeadItem());

                CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.head.result", saved);
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
            CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_permission");
            return;
        }

        CommandUtil.sendFeedback(sender, SkinSetterAPI.getInstance().getLangProvider(), "command.error.usage", CustomPlaceholderInline.create("usage", builder.toString()));

    }

}

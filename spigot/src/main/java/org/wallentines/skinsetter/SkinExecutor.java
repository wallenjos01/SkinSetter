package org.wallentines.skinsetter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.SpigotCommandSender;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.SpigotPlayer;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;

import java.util.ArrayList;
import java.util.List;

public class SkinExecutor extends BukkitCommand {

    protected SkinExecutor() {
        super("skin");
    }
    private static final String[] subCommands = { "set", "reset", "save", "setrandom", "item", "persistence", "setdefault", "cleardefault", "edit", "reload" };

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {

        if(args.length == 0) {

            StringBuilder usage = new StringBuilder("/skin <");
            int subcommands = 0;
            for(String s : subCommands) {
                if(sender.hasPermission("skinsetter.command." + s)) {
                    if (subcommands++ > 0) {
                        usage.append("/");
                    }
                    usage.append(s);
                }
            }

            sendMessage(sender, usage("/skin " + usage));
            return true;
        }

        String sub = args[0];
        if(!sender.hasPermission("skinsetter.command." + sub)) {
            sendMessage(sender, SkinSetterServer.INSTANCE.get().getLangManager().component("error.no_permission"));
            return true;
        }

        switch (sub) {
            case "set": set(sender, args); break;
            case "reset": reset(sender, args); break;
            case "save": save(sender, args); break;
            case "setrandom": setRandom(sender, args); break;
            case "item": item(sender, args); break;
            case "persistence": persistence(sender, args); break;
            case "setdefault": setDefault(sender, args); break;
            case "cleardefault": SkinCommand.clearDefault(cmp -> sendMessage(sender, cmp)); break;
            case "edit": edit(sender, args); break;
            case "reload": SkinCommand.reload(cmp -> sendMessage(sender, cmp)); break;
        }

        return true;
    }

    private void set(CommandSender sender, String[] args) {

        try {
            if (args.length == 1) {

                String usage = "/skin set <player> [<skin>]";
                if (sender.hasPermission("skinsetter.command.set.online")) {
                    usage += " [-o]";
                }
                sendMessage(sender, usage(usage));
                return;
            }

            Server server = Server.RUNNING_SERVER.get();
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sendMessage(sender, SkinSetterServer.INSTANCE.get().getLangManager().component("error.player_not_found"));
                return;
            }

            if (args.length == 2) {

                if (!(sender instanceof Player)) {
                    sendMessage(sender, SkinSetterServer.INSTANCE.get().getLangManager().component("error.not_player"));
                    return;
                }

                SpigotPlayer spl = new SpigotPlayer(server, (Player) sender);
                SkinCommand.setGUI(spl, List.of(new SpigotPlayer(server, target)), cmp -> sendMessage(sender, cmp));
                return;
            }

            if (args.length == 3) {
                SpigotPlayer spl = new SpigotPlayer(server, target);
                SkinCommand.set(List.of(spl), args[2], false, SpigotCommandSender.of(sender));
                return;
            }

            if (args.length == 4) {
                if (!args[3].equals("-o")) {
                    String usage = "/skin set <player> [<skin>]";
                    if (sender.hasPermission("skinsetter.command.set.online")) {
                        usage += " [-o]";
                    }
                    sendMessage(sender, usage(usage));
                    return;
                }
                SpigotPlayer spl = new SpigotPlayer(server, target);
                SkinCommand.set(List.of(spl), args[2], true, SpigotCommandSender.of(sender));
            }
        } catch (Exception ex) {
            SkinSetterAPI.LOGGER.error("An error occurred while settings a skin!", ex);
        }
    }


    private void reset(CommandSender sender, String[] args) {

        if(args.length == 1) {
            sendMessage(sender, usage("/skin reset <player>"));
            return;
        }

        SpigotPlayer spl = getPlayer(sender, args);
        if(spl == null) return;

        SkinCommand.reset(List.of(spl), SpigotCommandSender.of(sender));
    }

    private void save(CommandSender sender, String[] args) {

        if(args.length < 3) {
            sendMessage(sender, usage("/skin save <player> <id>"));
            return;
        }

        SpigotPlayer spl = getPlayer(sender, args);
        if(spl == null) return;

        String skin = args[2];
        String file = null;

        if(args.length == 4) {
            file = args[3];
        }

        SkinCommand.save(spl, skin, file, SpigotCommandSender.of(sender));
    }

    private void setRandom(CommandSender sender, String[] args) {

        if(args.length <= 1) {
            sendMessage(sender, usage("/skin setrandom <player>"));
            return;
        }

        String group = null;
        if(args.length > 2) {
            group = args[2];
        }

        SpigotPlayer spl = getPlayer(sender, args);
        if(spl == null) return;

        SkinCommand.setRandom(List.of(spl), SpigotCommandSender.of(sender), group);
    }

    private void item(CommandSender sender, String[] args) {

        if(args.length < 3) {
            sendMessage(sender, usage("/skin item <player> <skin>"));
            return;
        }

        SpigotPlayer spl = getPlayer(sender, args);
        if(spl == null) return;

        String skin = args[2];
        SkinCommand.item(List.of(spl), skin, SpigotCommandSender.of(sender));
    }

    private void persistence(CommandSender sender, String[] args) {

        if (args.length == 1) {
            sendMessage(sender, usage("/skin persistence enable/disable"));
            return;
        }

        if(args[1].equals("enable")) {
            SkinCommand.persistence(true, cmp -> sendMessage(sender, cmp));
        } else if(args[1].equals("disable")) {
            SkinCommand.persistence(false, cmp -> sendMessage(sender, cmp));
        } else {
            sendMessage(sender, usage("/skin persistence enable/disable"));
        }
    }

    private void setDefault(CommandSender sender, String[] args) {

        if (args.length == 1) {
            sendMessage(sender, usage("/skin setdefault <skin>"));
            return;
        }

        String skin = args[1];
        SkinCommand.setDefault(skin, cmp -> sendMessage(sender, cmp));
    }

    private void edit(CommandSender sender, String[] args) {

        if(args.length < 3) {
            sendMessage(sender, usage("/skin edit <skin> name/permission/excludeInRandom/excludeInGUI/item"));
            return;
        }

        switch(args[2]) {
            case "name": editName(sender, args); break;
            case "permission": editPermission(sender, args); break;
            case "excludeInRandom": editExcludedInRandom(sender, args); break;
            case "excludeInGUI": editExcludedInGUI(sender, args); break;
            case "item": editItem(sender, args); break;
        }
    }

    private void editName(CommandSender sender, String[] args) {

        if(args.length == 3) {
            sendMessage(sender, usage("/skin edit <skin> name <name>"));
            return;
        }

        SkinCommand.editName(args[1], args[3], cmp -> sendMessage(sender, cmp));
    }

    private void editPermission(CommandSender sender, String[] args) {

        if(args.length == 3) {
            sendMessage(sender, usage("/skin edit <skin> permission <permission>"));
            return;
        }

        SkinCommand.editPermission(args[1], args[3], cmp -> sendMessage(sender, cmp));
    }

    private void editExcludedInRandom(CommandSender sender, String[] args) {
        if(args.length == 3) {
            sendMessage(sender, usage("/skin edit <skin> excludeInRandom true/false"));
            return;
        }
        if(args[3].equals("true")) {
            SkinCommand.editExcludedInRandom(args[1], true, cmp -> sendMessage(sender, cmp));
        } else if(args[3].equals("false")) {
            SkinCommand.editExcludedInRandom(args[1], false, cmp -> sendMessage(sender, cmp));
        } else {
            sendMessage(sender, usage("/skin edit <skin> excludeInRandom true/false"));
        }
    }

    private void editExcludedInGUI(CommandSender sender, String[] args) {
        if(args.length == 3) {
            sendMessage(sender, usage("/skin edit <skin> excludeInGUI true/false"));
            return;
        }
        if(args[3].equals("true")) {
            SkinCommand.editExcludedInGUI(args[1], true, cmp -> sendMessage(sender, cmp));
        } else if(args[3].equals("false")) {
            SkinCommand.editExcludedInGUI(args[1], false, cmp -> sendMessage(sender, cmp));
        } else {
            sendMessage(sender, usage("/skin edit <skin> excludeInGUI true/false"));
        }
    }

    private void editItem(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            sendMessage(sender, SkinSetterServer.INSTANCE.get().getLangManager().component("error.not_player"));
            return;
        }

        SpigotPlayer spl = new SpigotPlayer(Server.RUNNING_SERVER.get(), (Player) sender);
        SkinCommand.editItem(args[1], spl, cmp -> sendMessage(sender, cmp));
    }


    private void addSkinNames(SpigotCommandSender sender, List<String> list) {
        list.addAll(SkinSetterAPI.REGISTRY.get().getSkinIds(sender, null, SkinRegistry.ExcludeFlag.NONE));
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {

        SpigotCommandSender cph = SpigotCommandSender.of(sender);

        List<String> out = new ArrayList<>();
        if(args.length == 0 || args.length == 1) {
            for(String s : subCommands) {
                if(sender.hasPermission("skinsetter.command." + s)) {
                    out.add(s);
                }
            }
        }
        else if(sender.hasPermission("skinsetter.command." + args[0])) {
            if (args.length == 2) {
                switch (args[0]) {
                    case "set":
                    case "reset":
                    case "save":
                    case "setrandom":
                    case "item":
                        out.addAll(Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
                        break;
                    case "persistence":
                        out.add("enable");
                        out.add("disable");
                        break;
                    case "setdefault":
                    case "edit":
                        addSkinNames(cph, out);
                        break;
                    case "cleardefault":
                    case "reload":
                        break;
                }
            } else if (args.length == 3) {
                switch (args[0]) {
                    case "set":
                    case "item":
                        addSkinNames(cph, out);
                        break;
                    case "save":
                    case "persistence":
                    case "reset":
                    case "setdefault":
                    case "cleardefault":
                        break;
                    case "setrandom":
                        out.addAll(SkinSetterAPI.REGISTRY.get().getGroupNames(cph, SkinRegistry.ExcludeFlag.IN_RANDOM));
                        break;
                    case "edit":
                        out.add("name");
                        out.add("permission");
                        out.add("excludeInRandom");
                        out.add("excludeInGui");
                        out.add("item");
                        break;
                }
            } else if(args.length == 4 && args[0].equals("edit")) {
                switch (args[1]) {
                    case "name":
                    case "permission":
                    case "item":
                        break;
                    case "excludeInRandom":
                    case "excludeInGui":
                        out.add("true");
                        out.add("false");
                }
            }
        }

        return out.stream().filter(str -> str.startsWith(args[args.length - 1])).toList();
    }

    private SpigotPlayer getPlayer(CommandSender sender, String[] args) {
        Server server = Server.RUNNING_SERVER.get();
        Player target = Bukkit.getPlayer(args[1]);
        if(target == null) {
            sendMessage(sender, SkinSetterServer.INSTANCE.get().getLangManager().component("error.player_not_found"));
            return null;
        }
        return new SpigotPlayer(server, target);
    }

    private Component usage(String usage) {
        return SkinSetterServer.INSTANCE.get().getLangManager().component("error.usage", CustomPlaceholder.inline("usage", usage));
    }

    private void sendMessage(CommandSender sender, Component cmp) {

        if(sender instanceof Player) {
            Adapter.INSTANCE.get().sendMessage((Player) sender, ComponentResolver.resolveComponent(cmp, new SpigotPlayer(Server.RUNNING_SERVER.get(), (Player) sender)));
        } else {
            sender.sendMessage(ComponentResolver.resolveComponent(cmp).toLegacyText());
        }
    }
}

package me.m1dnightninja.skinsetter.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricItem;
import me.m1dnightninja.midnightcore.fabric.module.lang.LangModule;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import me.m1dnightninja.skinsetter.api.SavedSkin;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.util.SkinUtil;
import me.m1dnightninja.skinsetter.fabric.integragion.TaterzensIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SkinCommand {

    private final boolean TATERZENS_LOADED;

    private final SkinUtil util;

    public SkinCommand(SkinUtil util) {

        this.util = util;

        TATERZENS_LOADED = FabricLoader.getInstance().isModLoaded("taterzens");
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("skin")
            .requires(stack -> hasPermission(stack, "skinsetter.command"))
            .then(Commands.literal("set")
                .requires(stack -> hasPermission(stack, "skinsetter.command.set"))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> executeSet(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), null, false))
                    .then(Commands.argument("skin", StringArgumentType.word())
                        .suggests(((context, builder) -> {

                            MPlayer player = null;
                            try {
                                player = FabricPlayer.wrap(context.getSource().getPlayerOrException());
                            } catch (CommandSyntaxException ex) {
                                // Ignore
                            }
                            return SharedSuggestionProvider.suggest(util.getSkinNames(player), builder);
                        }))
                        .executes(context -> executeSet(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("skin", String.class), false))
                        .then(Commands.literal("-o")
                            .executes(context -> executeSet(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("skin", String.class), true))
                        )
                    )
                )
            )
            .then(Commands.literal("reset")
                .requires(stack -> hasPermission(stack, "skinsetter.command.reset"))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> executeReset(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource())))
                )
            )
            .then(Commands.literal("save")
                .requires(stack -> hasPermission(stack, "skinsetter.command.save"))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("id", StringArgumentType.word())
                        .executes(context -> executeSave(context, context.getArgument("player", EntitySelector.class).findSinglePlayer(context.getSource()), context.getArgument("id", String.class)))
                    )
                )
            )
            .then(Commands.literal("setdefault")
                .requires(stack -> hasPermission(stack, "skinsetter.command.setdefault"))
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(((context, builder) -> {

                        MPlayer player = null;
                        try {
                            player = FabricPlayer.wrap(context.getSource().getPlayerOrException());
                        } catch (CommandSyntaxException ex) {
                            // Ignore
                        }
                        return SharedSuggestionProvider.suggest(util.getSkinNames(player), builder);
                    }))
                    .executes(context -> executeSetDefault(context, context.getArgument("id", String.class)))
                )
            )
            .then(Commands.literal("cleardefault")
                .requires(stack -> hasPermission(stack, "skinsetter.command.setdefault"))
                .executes(this::executeClearDefault)
            )
            .then(Commands.literal("persistence")
                .requires(stack -> hasPermission(stack, "skinsetter.command.persistence"))
                .then(Commands.literal("enable")
                    .executes(this::executePersistenceEnable)
                )
                .then(Commands.literal("disable")
                    .executes(this::executePersistenceDisable)
                )
            )
            .then(Commands.literal("reload")
                .requires(stack -> hasPermission(stack, "skinsetter.command.reload"))
                .executes(this::executeReload)
            )
            .then(Commands.literal("setrandom")
                .requires(stack -> hasPermission(stack, "skinsetter.command.setrandom"))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> executeSetRandom(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), null))
                    .then(Commands.argument("group", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            MPlayer player = null;
                            try {
                                player = FabricPlayer.wrap(context.getSource().getPlayerOrException());
                            } catch (CommandSyntaxException ex) {
                                // Ignore
                            }
                            return SharedSuggestionProvider.suggest(util.getGroupNames(player, true), builder);
                        })
                        .executes(context -> executeSetRandom(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("group", String.class)))
                    )
                )
            )
            .then(Commands.literal("edit")
                .requires(stack -> PermissionUtil.checkOrOp(stack, "skinsetter.command.edit", 2))
                .then(Commands.argument("skin", StringArgumentType.word())
                    .suggests(((context, builder) -> {

                        MPlayer player = null;
                        try {
                            player = FabricPlayer.wrap(context.getSource().getPlayerOrException());
                        } catch (CommandSyntaxException ex) {
                            // Ignore
                        }
                        return SharedSuggestionProvider.suggest(util.getSkinNames(player), builder);
                    }))
                    .then(Commands.literal("name")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                            .executes(context -> executeEditName(context, context.getArgument("skin", String.class), context.getArgument("name", String.class)))
                        )
                    )
                    .then(Commands.literal("groups")
                        .then(Commands.literal("add")
                            .then(Commands.argument("group", StringArgumentType.word())
                                .executes(context -> executeEditAddGroup(context, context.getArgument("skin", String.class), context.getArgument("group", String.class)))
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("group", StringArgumentType.word())
                                .suggests((context, builder) -> {

                                    SavedSkin skin = util.getSavedSkin(context.getArgument("skin", String.class));
                                    if(skin != null) return SharedSuggestionProvider.suggest(skin.getGroups(), builder);

                                    return null;
                                })
                                .executes(context -> executeEditRemoveGroup(context, context.getArgument("skin", String.class), context.getArgument("group", String.class)))
                            )
                        )
                    )
                    .then(Commands.literal("item")
                        .then(Commands.literal("save")
                            .executes(context -> executeEditSaveItem(context, context.getArgument("skin", String.class), context.getSource().getPlayerOrException().getMainHandItem()))
                            .then(Commands.argument("item", ItemArgument.item())
                                .executes(context -> executeEditSaveItem(context, context.getArgument("skin", String.class), context.getArgument("item", ItemStack.class)))
                            )
                        )
                        .then(Commands.literal("clear")
                            .executes(context -> executeEditClearItem(context, context.getArgument("skin", String.class)))

                        )
                    )
                    .then(Commands.literal("excludeInRandom")
                        .then(Commands.argument("exclude", BoolArgumentType.bool())
                            .executes(context -> executeEditExcludeInRandom(context, context.getArgument("skin", String.class), context.getArgument("exclude", Boolean.class)))
                        )
                    )
                )
            )
            .then(Commands.literal("head")
                .requires(context -> PermissionUtil.checkOrOp(context, "skinsetter.command.head", 2))
                .then(Commands.argument("skin", StringArgumentType.word())
                    .suggests(((context, builder) -> {
                        MPlayer player = null;
                        try {
                            player = FabricPlayer.wrap(context.getSource().getPlayerOrException());
                        } catch (CommandSyntaxException ex) {
                            // Ignore
                        }
                        return SharedSuggestionProvider.suggest(util.getSkinNames(player), builder);
                    }))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> executeGiveHead(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("skin", String.class)))
                    )
                )
            );

        if(TATERZENS_LOADED) {
            cmd.then(Commands.literal("setnpc")
                .requires(stack -> hasPermission(stack, "skinsetter.command.setnpc"))
                .then(Commands.argument("skin", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        MPlayer player = null;
                        try {
                            player = FabricPlayer.wrap(context.getSource().getPlayerOrException());
                        } catch (CommandSyntaxException ex) {
                            // Ignore
                        }
                        return SharedSuggestionProvider.suggest(util.getSkinNames(player), builder);
                    })
                    .executes(context -> executeSetNpc(context, context.getArgument("skin", String.class)))
                )
            );
        }

        dispatcher.register(cmd);

    }

    private boolean hasPermission(CommandSourceStack st, String perm) {
        return PermissionUtil.checkOrOp(st, perm, 2);
    }

    private int executeSet(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String skin, boolean online) {

        if(skin == null) {

            MPlayer player = null;
            if(context.getSource().getEntity() != null) {
                try {
                    player = FabricPlayer.wrap(context.getSource().getPlayerOrException());
                } catch (CommandSyntaxException ex) {
                    // Ignore
                }
            }

            for(ServerPlayer pl : players) {
                util.openGUI(FabricPlayer.wrap(pl), player);
            }
            return players.size();
        }

        SavedSkin s = util.getSavedSkin(skin);
        Skin sk;

        if((s == null || online) && hasPermission(context.getSource(), "skinsetter.command.set.online")) {

            ServerPlayer player = MidnightCore.getServer().getPlayerList().getPlayerByName(skin);
            if(player == null || (online && !MidnightCore.getServer().usesAuthentication())) {

                return executeSetOnline(context, players, skin);

            } else {

                sk = util.getLoginSkin(FabricPlayer.wrap(player));
            }
        } else {

            if(s == null) {
                LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(),"command.error.invalid_skin");
                return 0;
            }

            if(context.getSource().getEntity() != null) {
                try {
                    if (!s.canUse(FabricPlayer.wrap(context.getSource().getPlayerOrException()))) {
                        LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
                        return 0;
                    }
                } catch (CommandSyntaxException ex) {
                    // Ignore, Console/CommandBlock executed command
                }
            }

            sk = s.getSkin();
        }

        for(ServerPlayer ent : players) {
            util.setSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(ent.getUUID()), sk);
        }

        sendFeedback(context, players.size() == 1 ? "command.set.result.single" : "command.set.result.multiple", new CustomPlaceholderInline("count", players.size()+""), FabricPlayer.wrap(players.get(0)));

        return players.size();
    }

    private int executeSetOnline(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String skin) {

        if(!hasPermission(context.getSource(), "skinsetter.command.set.online")) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(),"command.error.invalid_skin");
            return 0;
        }

        util.getSkinOnline(skin, (uid, skin1) -> {
            if(skin1 == null) {
                sendFeedback(context, "command.error.invalid_name");
                return;
            }

            MidnightCore.getServer().submit(() -> {
                for(ServerPlayer ent : players) {
                    util.setSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(ent.getUUID()), skin1);
                }

                sendFeedback(context, players.size() == 1 ? "command.set.result.single" : "command.set.result.multiple", new CustomPlaceholderInline("count", players.size()+""), FabricPlayer.wrap(players.get(0)));
            });
        });

        return players.size();
    }

    private int executeReset(CommandContext<CommandSourceStack> context, List<ServerPlayer> players) {

        for(ServerPlayer ent : players) {
            util.resetSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(ent.getUUID()));
        }

        MPlayer player = FabricPlayer.wrap(players.get(0));
        sendFeedback(context, players.size() == 1 ? "command.reset.result.single" : "command.reset.result.multiple", new CustomPlaceholderInline("count", players.size()+""), player);

        return players.size();
    }

    private int executeSave(CommandContext<CommandSourceStack> context, ServerPlayer player, String id) {

        util.saveSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUUID()), id);

        sendFeedback(context, "command.save.result", id, new CustomPlaceholderInline("id", id), FabricPlayer.wrap(player));

        return 1;
    }

    private int executeSetNpc(CommandContext<CommandSourceStack> context, String id) throws CommandSyntaxException {

        if(!TATERZENS_LOADED) return 0;

        MPlayer player = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(context.getSource().getPlayerOrException().getUUID());

        SavedSkin s = util.getSavedSkin(id);
        if(s == null) {
            SkinSetterAPI.getInstance().getLangProvider().sendMessage("command.error.invalid_skin", player);
            return 0;
        }

        ServerPlayer pl = ((FabricPlayer) player).getMinecraftPlayer();
        if(pl == null) {
            return 0;
        }

        TaterzensIntegration.setNPCSkin(pl, s.getSkin()).send(player);

        return 1;
    }

    private int executeSetDefault(CommandContext<CommandSourceStack> context, String skinId) {

        SavedSkin skin = SkinSetterAPI.getInstance().getSkinManager().getSkin(skinId);

        if(skin == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        SkinSetterAPI.getInstance().setDefaultSkin(skin);
        SkinSetterAPI.getInstance().getConfig().set("default_skin", skinId);
        SkinSetterAPI.getInstance().saveConfig();

        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false,"command.setdefault.result", skin, new CustomPlaceholderInline("id", skinId));

        return 1;
    }

    private int executeClearDefault(CommandContext<CommandSourceStack> context) {

        SkinSetterAPI.getInstance().setDefaultSkin(null);
        SkinSetterAPI.getInstance().getConfig().set("default_skin", "");
        SkinSetterAPI.getInstance().saveConfig();

        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false,"command.cleardefault.result");

        return 1;
    }

    private int executePersistenceEnable(CommandContext<CommandSourceStack> context) {

        SkinSetterAPI.getInstance().setPersistenceEnabled(true);
        SkinSetterAPI.getInstance().getConfig().set("persistent_skins", true);
        SkinSetterAPI.getInstance().saveConfig();

        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false,"command.persistence.result.enable");

        return 1;
    }

    private int executePersistenceDisable(CommandContext<CommandSourceStack> context) {

        SkinSetterAPI.getInstance().setPersistenceEnabled(false);
        SkinSetterAPI.getInstance().getConfig().set("persistent_skins", false);
        SkinSetterAPI.getInstance().saveConfig();

        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false,"command.persistence.result.disable");

        IPlayerDataModule dataModule = MidnightCoreAPI.getInstance().getModule(IPlayerDataModule.class);

        for(MPlayer pl : MidnightCoreAPI.getInstance().getPlayerManager()) {

            dataModule.getGlobalProvider().getPlayerData(pl.getUUID()).set("skinsetter", null);
            dataModule.getGlobalProvider().savePlayerData(pl.getUUID());
        }

        return 1;
    }

    private int executeReload(CommandContext<CommandSourceStack> context) {

        long time = System.currentTimeMillis();
        SkinSetterAPI.getInstance().reloadConfig();
        time = System.currentTimeMillis() - time;

        sendFeedback(context, "command.reload.result", new CustomPlaceholderInline("time", time+""));

        return (int) time;

    }

    private int executeSetRandom(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String group) {

        MPlayer mpl = null;
        if(context.getSource().getEntity() != null) {
            try {
                mpl = FabricPlayer.wrap(context.getSource().getPlayerOrException());
            } catch (CommandSyntaxException ex) {
                // Ignore
            }
        }

        for(ServerPlayer pl : players) {

            SavedSkin s = util.getRandomSkin(mpl, group);
            if(s == null) {

                LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.no_saved");
                return 0;
            }

            util.setSkin(FabricPlayer.wrap(pl), s.getSkin());
        }

        sendFeedback(context, players.size() == 1 ? "command.set.result.single" : "command.set.result.multiple", new CustomPlaceholderInline("count", players.size()+""), FabricPlayer.wrap(players.get(0)));

        return players.size();
    }

    private int executeEditExcludeInRandom(CommandContext<CommandSourceStack> context, String skin, Boolean exclude) {

        SavedSkin s = util.getSavedSkin(skin);
        if(s == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        s.setInRandom(exclude);

        String key = exclude ? "command.edit.excludeInRandom.result.enabled" : "command.edit.excludeInRandom.result.disabled";
        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false, key);

        return 1;
    }

    private int executeEditClearItem(CommandContext<CommandSourceStack> context, String skin) {

        SavedSkin s = util.getSavedSkin(skin);
        if(s == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        s.setCustomItem(null);
        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false, "command.edit.item.clear.result", s);

        return 1;
    }

    private int executeEditSaveItem(CommandContext<CommandSourceStack> context, String skin, ItemStack is) {

        SavedSkin s = util.getSavedSkin(skin);
        if(s == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        if(is == null || is.isEmpty()) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_item");
            return 0;
        }

        s.setCustomItem(new FabricItem(is));
        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false, "command.edit.item.save.result", s);

        return 1;

    }

    private int executeEditRemoveGroup(CommandContext<CommandSourceStack> context, String skin, String group) {

        SavedSkin s = util.getSavedSkin(skin);
        if(s == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        s.getGroups().remove(group);
        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false, "command.edit.groups.result", s);

        return 1;

    }

    private int executeEditAddGroup(CommandContext<CommandSourceStack> context, String skin, String group) {

        SavedSkin s = util.getSavedSkin(skin);
        if(s == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        s.addGroup(group);
        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false, "command.edit.groups.result", s);

        return 1;

    }

    private int executeEditName(CommandContext<CommandSourceStack> context, String skin, String name) {

        SavedSkin s = util.getSavedSkin(skin);
        if(s == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        MComponent newName = MComponent.Serializer.parse(name);
        s.setName(newName);

        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false, "command.edit.name.result", s);

        return 1;
    }

    private int executeGiveHead(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String skin) {

        SavedSkin s = util.getSavedSkin(skin);
        if(s == null) {
            LangModule.sendCommandFailure(context, SkinSetterAPI.getInstance().getLangProvider(), "command.error.invalid_skin");
            return 0;
        }

        MItemStack is = s.getHeadItem();
        for(ServerPlayer pl : players) {
            pl.addItem(((FabricItem) is).getMinecraftItem());
        }

        LangModule.sendCommandSuccess(context, SkinSetterAPI.getInstance().getLangProvider(), false, "command.head.result", s);

        return players.size();
    }

    private void sendFeedback(CommandContext<CommandSourceStack> context, String key, Object... args) {

        MPlayer u = (context.getSource().getEntity() instanceof ServerPlayer) ? FabricPlayer.wrap((ServerPlayer) context.getSource().getEntity()) : null;
        MComponent message = SkinSetterAPI.getInstance().getLangProvider().getMessage(key, u, args);

        context.getSource().sendSuccess(ConversionUtil.toMinecraftComponent(message), false);
    }

}

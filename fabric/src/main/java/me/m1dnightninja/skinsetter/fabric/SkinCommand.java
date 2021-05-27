package me.m1dnightninja.skinsetter.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.PermissionHelper;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.SkinUtil;
import me.m1dnightninja.skinsetter.fabric.integragion.TaterzensIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

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
                    .then(Commands.argument("skin", StringArgumentType.word())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(util.getSkinNames(), builder)))
                        .executes(context -> executeSet(context, context.getArgument("players", EntitySelector.class).findPlayers(context.getSource()), context.getArgument("skin", String.class)))
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
            .then(Commands.literal("reload")
                .requires(stack -> hasPermission(stack, "skinsetter.command.reload"))
                .executes(this::executeReload)
            );

        if(TATERZENS_LOADED) {
            cmd.then(Commands.literal("setnpc")
                .requires(stack -> hasPermission(stack, "skinsetter.command.setnpc"))
                .then(Commands.argument("skin", StringArgumentType.word())
                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(util.getSkinNames(), builder))
                    .executes(context -> executeSetNpc(context, context.getArgument("skin", String.class)))
                )
            );
        }

        dispatcher.register(cmd);

    }

    private boolean hasPermission(CommandSourceStack st, String perm) {
        return st.hasPermission(2) || PermissionHelper.check(st, perm);
    }

    private int executeSet(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String skin) {

        Skin s = util.getSavedSkin(skin);
        if(s == null) {
            return executeSetOnline(context, players, skin);
        }

        for(ServerPlayer ent : players) {
            util.setSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(ent.getUUID()), s);
        }

        sendFeedback(context, players.size() == 1 ? "command.set.result.single" : "command.set.result.multiple", new CustomPlaceholderInline("count", players.size()+""), FabricPlayer.wrap(players.get(0)));

        return players.size();
    }

    private int executeSetOnline(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String skin) {

        util.getSkinOnline(skin, (uid, skin1) -> {
            if(skin1 == null) {
                sendFeedback(context, "command.set.error");
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

        Skin s = util.getSavedSkin(id);
        if(s == null) {
            SkinSetterAPI.getInstance().getLangProvider().sendMessage("command.error.invalid_skin", player);
        }

        ServerPlayer pl = ((FabricPlayer) player).getMinecraftPlayer();
        if(pl == null) {
            return 0;
        }

        TaterzensIntegration.setNPCSkin(pl, s).send(player);

        return 1;
    }

    private int executeReload(CommandContext<CommandSourceStack> context) {

        long time = System.currentTimeMillis();
        SkinSetterAPI.getInstance().reloadConfig();
        time = System.currentTimeMillis() - time;

        sendFeedback(context, "command.reload.result", new CustomPlaceholderInline("time", time+""));

        return (int) time;

    }

    private void sendFeedback(CommandContext<CommandSourceStack> context, String key, Object... args) {

        MPlayer u = (context.getSource().getEntity() instanceof ServerPlayer) ? FabricPlayer.wrap((ServerPlayer) context.getSource().getEntity()) : null;
        MComponent message = SkinSetterAPI.getInstance().getLangProvider().getMessage(key, u, args);

        context.getSource().sendSuccess(ConversionUtil.toMinecraftComponent(message), false);
    }

}

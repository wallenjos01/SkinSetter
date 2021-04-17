package me.m1dnightninja.skinsetter.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.m1dnightninja.midnightcore.api.skin.Skin;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.PermissionHelper;
import me.m1dnightninja.skinsetter.common.SkinUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class SkinCommand {

    private final SkinUtil util;
    public SkinCommand() {

        this.util = new SkinUtil();
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> util.saveSkins());
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("skin")
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
        );

    }

    private boolean hasPermission(CommandSourceStack st, String perm) {
        return st.hasPermission(2) || PermissionHelper.check(st, perm);
    }

    private static final Style SUCCESS = Style.EMPTY.withColor(ChatFormatting.GREEN);

    private int executeSet(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String skin) {

        Skin s = util.getSavedSkin(skin);
        if(s == null) {
            return executeSetOnline(context, players, skin);
        }

        for(ServerPlayer ent : players) {
            util.setSkin(ent.getUUID(), s);
        }

        context.getSource().sendSuccess(new TextComponent("Changed the skin of " + players.size() + " players").setStyle(SUCCESS), false);

        return players.size();
    }

    private int executeSetOnline(CommandContext<CommandSourceStack> context, List<ServerPlayer> players, String skin) {

        util.getSkinOnline(skin, (uid, skin1) -> {
            if(skin1 == null) {
                context.getSource().sendFailure(new TextComponent("That is not a valid skin!"));
                return;
            }

            MidnightCore.getServer().submit(() -> {
                for(ServerPlayer ent : players) {
                    util.setSkin(ent.getUUID(), skin1);
                }

                context.getSource().sendSuccess(new TextComponent("Changed the skin of " + players.size() + " players").setStyle(SUCCESS), false);
            });
        });

        return players.size();
    }

    private int executeReset(CommandContext<CommandSourceStack> context, List<ServerPlayer> players) {

        for(ServerPlayer ent : players) {
            util.resetSkin(ent.getUUID());
        }

        context.getSource().sendSuccess(new TextComponent("Reset the skin of " + players.size() + " players").setStyle(SUCCESS), false);

        return players.size();
    }

    private int executeSave(CommandContext<CommandSourceStack> context, ServerPlayer player, String id) {

        util.saveSkin(player.getUUID(), id);

        context.getSource().sendSuccess(new TextComponent("Saved ").setStyle(SUCCESS).append(player.getName()).append(new TextComponent("'s skin as " + id)), false);

        return 1;
    }

}

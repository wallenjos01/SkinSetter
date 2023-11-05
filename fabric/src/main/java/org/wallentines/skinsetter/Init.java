package org.wallentines.skinsetter;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.wallentines.fbev.player.PlayerJoinEvent;
import org.wallentines.fbev.player.PlayerLeaveEvent;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.PermissionHolder;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.skin.Skinnable;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.midnightlib.event.Event;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

public class Init implements ModInitializer {

    @Override
    public void onInitialize() {

        Server.RUNNING_SERVER.setEvent.register(this, srv -> {
            ConfigSection lang;
            try {
                lang = JSONCodec.loadConfig(getClass().getResourceAsStream("/skinsetter/en_us.json")).asSection();
            } catch (IOException | DecodeException | IllegalStateException ex) {
                MidnightCoreAPI.LOGGER.error("Unable to enable SkinSetter! Lang defaults are missing or malformed!", ex);
                return;
            }
            SkinSetterServer.init(srv, LangRegistry.fromConfig(lang, PlaceholderManager.INSTANCE));
            srv.shutdownEvent().register(this, ev -> {
                SkinSetterServer.INSTANCE.get().onShutdown();
            });
        });

        Event.register(PlayerJoinEvent.class, this, ev -> {
            SkinSetterServer.INSTANCE.get().onJoin((Player) ev.getPlayer());
        });

        Event.register(PlayerLeaveEvent.class, this, ev -> {
            SkinSetterServer.INSTANCE.get().onLeave((Player) ev.getPlayer());
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, selection) -> {

            dispatcher.register(
                    Commands.literal("skin")
                            .requires(Permissions.require("skinsetter.command", 2))
                            .then(Commands.literal("set")
                                    .requires(Permissions.require("skinsetter.command.set", 2))
                                    .then(Commands.argument("targets", EntityArgument.entities())
                                            .executes(ctx -> SkinCommand.setGUI(
                                                    (Player) ctx.getSource().getPlayerOrException(),
                                                    getTargets(ctx),
                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                            ))
                                            .then(Commands.argument("skin", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                            SkinSetterAPI.REGISTRY.get().getSkinIds(
                                                                    (PermissionHolder) ctx.getSource(),
                                                                    null,
                                                                    SkinRegistry.ExcludeFlag.NONE),
                                                            builder))
                                                    .executes(ctx -> SkinCommand.set(
                                                            getTargets(ctx),
                                                            ctx.getArgument("skin", String.class),
                                                            false,
                                                            (str, lvl) -> Permissions.check(ctx.getSource(), str, lvl),
                                                            comp -> sendSuccess(ctx.getSource(), comp)
                                                    ))
                                                    .then(Commands.literal("-o")
                                                            .requires(Permissions.require("skinsetter.command.set.online", 2))
                                                            .executes(ctx -> SkinCommand.set(
                                                                    getTargets(ctx),
                                                                    ctx.getArgument("skin", String.class),
                                                                    true,
                                                                    (str, lvl) -> Permissions.check(ctx.getSource(), str, lvl),
                                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                                            ))
                                                    )
                                            )
                                    )
                            )
                            .then(Commands.literal("reset")
                                    .requires(Permissions.require("skinsetter.command.reset", 2))
                                    .then(Commands.argument("targets", EntityArgument.entities())
                                            .executes(ctx -> SkinCommand.reset(
                                                    getTargets(ctx),
                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                            ))
                                    )
                            )
                            .then(Commands.literal("save")
                                    .requires(Permissions.require("skinsetter.command.save", 2))
                                    .then(Commands.argument("target", EntityArgument.entity())
                                            .then(Commands.argument("name", StringArgumentType.string())
                                                    .executes(ctx -> SkinCommand.save(
                                                            (Skinnable) ctx.getArgument("target", EntitySelector.class).findSingleEntity(ctx.getSource()),
                                                            ctx.getArgument("name", String.class),
                                                            null,
                                                            (str, lvl) -> Permissions.check(ctx.getSource(), str, lvl),
                                                            comp -> sendSuccess(ctx.getSource(), comp)
                                                    ))
                                                    .then(Commands.argument("file", StringArgumentType.string())
                                                            .executes(ctx -> SkinCommand.save(
                                                                    (Skinnable) ctx.getArgument("target", EntitySelector.class).findSingleEntity(ctx.getSource()),
                                                                    ctx.getArgument("name", String.class),
                                                                    ctx.getArgument("file", String.class),
                                                                    (str, lvl) -> Permissions.check(ctx.getSource(), str, lvl),
                                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                                            ))
                                                    )
                                            )
                                    )
                            )
                            .then(Commands.literal("setrandom")
                                    .requires(Permissions.require("skinsetter.command.setrandom", 2))
                                    .then(Commands.argument("targets", EntityArgument.entities())
                                            .executes(ctx -> SkinCommand.setRandom(
                                                    getTargets(ctx),
                                                    (PermissionHolder) ctx.getSource(),
                                                    null,
                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                            ))
                                            .then(Commands.argument("group", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                            SkinSetterAPI.REGISTRY.get().getGroupNames(
                                                                    (PermissionHolder) ctx.getSource(),
                                                                    SkinRegistry.ExcludeFlag.NONE),
                                                            builder))
                                                    .executes(ctx -> SkinCommand.setRandom(
                                                            getTargets(ctx),
                                                            (PermissionHolder) ctx.getSource(),
                                                            ctx.getArgument("group", String.class),
                                                            comp -> sendSuccess(ctx.getSource(), comp)
                                                    ))
                                            )
                                    )
                            )
                            .then(Commands.literal("item")
                                    .requires(Permissions.require("skinsetter.command.item", 2))
                                    .then(Commands.argument("targets", EntityArgument.entities())
                                            .then(Commands.argument("skin", StringArgumentType.string())
                                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SkinSetterAPI.REGISTRY.get().getSkinIds(), builder))
                                                    .executes(ctx -> SkinCommand.item(
                                                            ctx.getArgument("targets", EntitySelector.class).findEntities(ctx.getSource()).stream().filter(en -> en instanceof Player).map(en -> (Player) en).collect(Collectors.toList()),
                                                            ctx.getArgument("skin", String.class),
                                                            (PermissionHolder) ctx.getSource(),
                                                            comp -> sendSuccess(ctx.getSource(), comp)
                                                    ))
                                            )
                                    )
                            )
                            .then(Commands.literal("persistence")
                                    .requires(Permissions.require("skinsetter.command.persistence", 2))
                                    .then(Commands.literal("enable")
                                            .executes(ctx -> SkinCommand.persistence(true, comp -> sendSuccess(ctx.getSource(), comp)))
                                    )
                                    .then(Commands.literal("disable")
                                            .executes(ctx -> SkinCommand.persistence(false, comp -> sendSuccess(ctx.getSource(), comp)))
                                    )
                            )
                            .then(Commands.literal("setdefault")
                                    .requires(Permissions.require("skinsetter.command.setdefault", 2))
                                        .then(Commands.argument("skin", StringArgumentType.string())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SkinSetterAPI.REGISTRY.get().getSkinIds(), builder))
                                                .executes(ctx -> SkinCommand.setDefault(
                                                        ctx.getArgument("skin", String.class),
                                                        comp -> sendSuccess(ctx.getSource(), comp)
                                                ))
                                    )
                            )
                            .then(Commands.literal("cleardefault")
                                    .requires(Permissions.require("skinsetter.command.cleardefault", 2))
                                    .executes(ctx -> SkinCommand.clearDefault(
                                            comp -> sendSuccess(ctx.getSource(), comp)
                                    ))
                            )
                            .then(Commands.literal("edit")
                                    .requires(Permissions.require("skinsetter.command.edit", 2))
                                    .then(Commands.argument("skin", StringArgumentType.string())
                                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SkinSetterAPI.REGISTRY.get().getSkinIds(), builder))
                                            .then(Commands.literal("name")
                                                    .then(Commands.argument("name", StringArgumentType.greedyString())
                                                            .executes(ctx -> SkinCommand.editName(
                                                                    ctx.getArgument("skin", String.class),
                                                                    ctx.getArgument("name", String.class),
                                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                                            ))
                                                    )
                                            )
                                            .then(Commands.literal("permission")
                                                    .then(Commands.argument("permission", StringArgumentType.string())
                                                            .executes(ctx -> SkinCommand.editPermission(
                                                                    ctx.getArgument("skin", String.class),
                                                                    ctx.getArgument("permission", String.class),
                                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                                            ))
                                                    )
                                            )
                                            .then(Commands.literal("excludeInRandom")
                                                    .then(Commands.argument("exclude", BoolArgumentType.bool())
                                                            .executes(ctx -> SkinCommand.editExcludedInRandom(
                                                                    ctx.getArgument("skin", String.class),
                                                                    ctx.getArgument("exclude", Boolean.class),
                                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                                            ))
                                                    )
                                            )
                                            .then(Commands.literal("excludeInGUI")
                                                    .then(Commands.argument("exclude", BoolArgumentType.bool())
                                                            .executes(ctx -> SkinCommand.editExcludedInGUI(
                                                                    ctx.getArgument("skin", String.class),
                                                                    ctx.getArgument("exclude", Boolean.class),
                                                                    comp -> sendSuccess(ctx.getSource(), comp)
                                                            ))
                                                    )
                                            )
                                            .then(Commands.literal("item")
                                                    .executes(ctx -> SkinCommand.editItem(
                                                            ctx.getArgument("skin", String.class),
                                                            (Player) ctx.getSource().getPlayerOrException(),
                                                            comp -> sendSuccess(ctx.getSource(), comp)
                                                    ))
                                            )
                                    )
                            )
                            .then(Commands.literal("reload")
                                    .requires(Permissions.require("skinsetter.command.reload", 2))
                                    .executes(ctx -> SkinCommand.reload(
                                            comp -> sendSuccess(ctx.getSource(), comp)
                                    ))
                            )
            );

        });
    }

    private static Collection<Skinnable> getTargets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getArgument("targets", EntitySelector.class).findEntities(ctx.getSource()).stream().filter(en -> en instanceof Skinnable).map(en -> (Skinnable) en).collect(Collectors.toList());
    }

    private static void sendSuccess(CommandSourceStack stack, Component comp) {

        stack.sendSuccess(() -> WrappedComponent.resolved(comp, stack.getPlayer()), false);
    }

}

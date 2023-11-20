package org.wallentines.skinsetter;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.SpigotPlayer;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.util.CommandUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.io.IOException;

public class SkinSetter extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        Server.RUNNING_SERVER.setEvent.register(this, srv -> {
            ConfigSection lang;
            try {
                lang = JSONCodec.loadConfig(getClass().getResourceAsStream("/en_us.json")).asSection();
            } catch (IOException | DecodeException | IllegalStateException ex) {
                MidnightCoreAPI.LOGGER.error("Unable to enable SkinSetter! Lang defaults are missing or malformed!", ex);
                return;
            }
            srv.shutdownEvent().register(this, ev -> {
                SkinSetterServer sks = SkinSetterServer.INSTANCE.getOrNull();
                if(sks != null) {
                    sks.onShutdown();
                }
            });
            SkinSetterServer.init(srv, LangRegistry.fromConfig(lang, PlaceholderManager.INSTANCE));
        });

        CommandUtil.registerCommand(this, new SkinExecutor());

        Bukkit.getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        SkinSetterServer.INSTANCE.get().onJoin(new SpigotPlayer(Server.RUNNING_SERVER.get(), event.getPlayer()));
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        SkinSetterServer.INSTANCE.get().onJoin(new SpigotPlayer(Server.RUNNING_SERVER.get(), event.getPlayer()));
    }
}

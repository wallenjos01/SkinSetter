package org.wallentines.skinsetter.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.skinsetter.common.LoginManager;

public class SkinListener implements Listener {

    private final SkinSetter plugin;

    public SkinListener(SkinSetter plugin) {

        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        LoginManager.applyLoginSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId()), plugin.getAPI().getSkinRegistry());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        LoginManager.savePersistentSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId()), plugin.getAPI().getDefaultSkin());
    }

}

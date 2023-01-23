package org.wallentines.skinsetter.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.skinsetter.common.LoginManager;

public class SkinListener implements Listener {

    private final SkinSetter plugin;

    public SkinListener(SkinSetter plugin) {

        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        SpigotPlayer player = SpigotPlayer.wrap(event.getPlayer());
        LoginManager.applyLoginSkin(player, plugin.getAPI().getSkinRegistry());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        SpigotPlayer player = SpigotPlayer.wrap(event.getPlayer());
        LoginManager.savePersistentSkin(player, plugin.getAPI().getDefaultSkin());
    }

}

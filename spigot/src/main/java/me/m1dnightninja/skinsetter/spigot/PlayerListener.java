package me.m1dnightninja.skinsetter.spigot;

import me.m1dnightninja.skinsetter.common.SkinUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private final SkinUtil util;

    public PlayerListener(SkinUtil util) {

        this.util = util;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        new BukkitRunnable() {
            @Override
            public void run() {
                util.applyLoginSkin(event.getPlayer().getUniqueId());
            }
        }.runTask(SkinSetter.getPlugin(SkinSetter.class));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeave(PlayerQuitEvent event) {

        util.savePersistentSkin(event.getPlayer().getUniqueId());
    }

}

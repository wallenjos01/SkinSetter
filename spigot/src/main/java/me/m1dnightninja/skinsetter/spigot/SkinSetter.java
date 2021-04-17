package me.m1dnightninja.skinsetter.spigot;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.common.JavaLogger;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SkinSetter extends JavaPlugin {

    @Override
    public void onEnable() {

        PluginCommand cmd = getCommand("skin");
        if(cmd == null) {
            getLogger().warning("plugin.yml has been corrupted!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if(!getDataFolder().exists() && !(getDataFolder().mkdir() && getDataFolder().setReadable(true) && getDataFolder().setWritable(true))) {
            getLogger().warning("Unable to create config file!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        JavaLogger log = new JavaLogger(this.getLogger());
        if(!MidnightCoreAPI.getInstance().areAllModulesLoaded("skin")) {
            log.warn("Unable to enable SkinSetter! One or more required MidnightCore modules are missing!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new SkinSetterAPI(log, u -> Bukkit.getPlayer(u) != null, getDataFolder());

        SkinCommand executor = new SkinCommand();
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);
    }

    @Override
    public void onDisable() {
        SkinSetterAPI api = SkinSetterAPI.getInstance();
        if(api == null) return;
        api.getSkinRegistry().saveSkins();
    }
}

package me.m1dnightninja.skinsetter.spigot;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.common.JavaLogger;
import me.m1dnightninja.midnightcore.spigot.config.YamlConfigProvider;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.SkinManagerImpl;
import me.m1dnightninja.skinsetter.common.SkinUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SkinSetter extends JavaPlugin {

    private SkinUtil util;

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
        if(!MidnightCoreAPI.getInstance().areAllModulesLoaded("midnightcore:skin","midnightcore:lang","midnightcore:player_data")) {

            log.warn("Unable to enable SkinSetter! One or more required MidnightCore modules are missing!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        ConfigSection sec = new YamlConfigProvider().loadFromStream(getResource("en_us.yml"));
        new SkinSetterAPI(log, u -> Bukkit.getPlayer(u) != null, getDataFolder(), sec, new SkinManagerImpl());

        util = new SkinUtil();

        SkinCommand executor = new SkinCommand(util);
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);

        getServer().getPluginManager().registerEvents(new PlayerListener(util), this);

    }

    @Override
    public void onDisable() {
        SkinSetterAPI api = SkinSetterAPI.getInstance();
        if(api == null) return;
        util.saveSkins();
    }
}

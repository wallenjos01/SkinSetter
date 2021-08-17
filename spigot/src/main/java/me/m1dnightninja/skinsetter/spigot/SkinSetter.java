package me.m1dnightninja.skinsetter.spigot;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.spigot.config.YamlConfigProvider;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.SkinSetterImpl;
import me.m1dnightninja.skinsetter.common.core.SkinManagerImpl;
import me.m1dnightninja.skinsetter.common.util.SkinUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

        if(!getDataFolder().exists() && !getDataFolder().mkdir()) {
            getLogger().warning("Unable to create config file!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Logger logger = LogManager.getLogger("SkinSetter");
        if(!MidnightCoreAPI.getInstance().areAllModulesLoaded("midnightcore:skin","midnightcore:lang","midnightcore:player_data")) {

            logger.warn("Unable to enable SkinSetter! One or more required MidnightCore modules are missing!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        YamlConfigProvider prov = YamlConfigProvider.INSTANCE;

        ConfigSection eng = prov.loadFromStream(getResource("en_us.yml"));
        ConfigSection esp = prov.loadFromStream(getResource("es_mx.yml"));

        ConfigSection cfg = prov.loadFromStream(getResource("config.yml"));

        SkinSetterAPI api = new SkinSetterImpl(getDataFolder(), eng, cfg, new SkinManagerImpl());

        api.getLangProvider().loadEntries("en_us", eng);
        api.getLangProvider().saveEntries("en_us");

        api.getLangProvider().loadEntries("es_mx", esp);
        api.getLangProvider().saveEntries("es_mx");

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

package org.wallentines.skinsetter.spigot;

import org.bukkit.command.PluginCommand;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.skinsetter.common.SkinSetterImpl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SkinSetter extends JavaPlugin {

    private SkinSetterImpl api;


    @Override
    public void onEnable() {

        // Determine the data folder
        Path dataFolder = Paths.get("config/SkinSetter");

        // Lang
        ConfigSection langDefaults = JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/lang/en_us.json"));
        ConfigSection esp = JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/lang/es_us.json"));

        api = new SkinSetterImpl(dataFolder, langDefaults);
        api.getLangProvider().loadEntries(esp, "es_us");

        getServer().getPluginManager().registerEvents(new SkinListener(this), this);

        PluginCommand cmd = getCommand("skin");
        if(cmd != null) {
            MainCommand exe = new MainCommand(this);
            cmd.setExecutor(exe);
            cmd.setTabCompleter(exe);
        }
    }

    @Override
    public void onDisable() {
        api.getSkinRegistry().save();
    }

    public SkinSetterImpl getAPI() {
        return api;
    }
}
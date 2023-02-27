package org.wallentines.skinsetter.spigot;

import org.bukkit.command.PluginCommand;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.text.LangRegistry;
import org.wallentines.midnightcore.spigot.config.YamlCodec;
import org.wallentines.mdcfg.ConfigSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.skinsetter.common.SkinSetterImpl;

import java.nio.file.Path;

public class SkinSetter extends JavaPlugin {

    private SkinSetterImpl api;


    @Override
    public void onEnable() {

        // Determine the data folder
        Path dataFolder = getDataFolder().toPath();

        // Lang
        ConfigSection langDefaults = YamlCodec.INSTANCE.decode(ConfigContext.INSTANCE, getClass().getResourceAsStream("/lang/en_us.yml")).asSection();
        ConfigSection esp = YamlCodec.INSTANCE.decode(ConfigContext.INSTANCE, getClass().getResourceAsStream("/lang/es_mx.yml")).asSection();

        api = new SkinSetterImpl(dataFolder, langDefaults);
        api.getLangProvider().loadEntries("es_mx", LangRegistry.fromConfigSection(esp));

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
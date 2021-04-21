package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.ILogger;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;

import java.io.File;
import java.util.UUID;

public class SkinSetterAPI {

    private static SkinSetterAPI INSTANCE;
    private static ILogger LOGGER;

    private final PlayerDelegate del;

    private final SkinRegistry registry;
    private final ConfigSection config;
    private final File skinFile;

    private final ILangProvider langProvider;

    public SkinSetterAPI(ILogger logger, PlayerDelegate delegate, File configFolder, ConfigSection defaultLang) {

        if(INSTANCE == null) {
            INSTANCE = this;
            LOGGER = logger;
        }

        ConfigProvider prov = MidnightCoreAPI.getInstance().getDefaultConfigProvider();
        skinFile = new File(configFolder, "config" + prov.getFileExtension());

        if(!skinFile.exists()) {
            prov.saveToFile(new ConfigSection(), skinFile);
        }

        langProvider = MidnightCoreAPI.getInstance().getModule(ILangModule.class).createLangProvider(new File(configFolder, "lang"), prov, defaultLang);

        this.del = delegate;
        this.config = prov.loadFromFile(skinFile);
        this.registry = new SkinRegistry();


    }

    public ConfigSection getConfig() {
        return config;
    }

    public boolean isOnline(UUID u) {
        return del.isOnline(u);
    }

    public ILangProvider getLangProvider() {
        return langProvider;
    }

    public SkinRegistry getSkinRegistry() {
        return registry;
    }

    public File getSkinFile() {
        return skinFile;
    }

    public static SkinSetterAPI getInstance() {
        return INSTANCE;
    }

    public static ILogger getLogger() {
        return LOGGER;
    }


    public interface PlayerDelegate {
        boolean isOnline(UUID u);
    }

}

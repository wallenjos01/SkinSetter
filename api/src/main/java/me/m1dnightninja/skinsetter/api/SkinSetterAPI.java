package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.ILogger;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;

import java.io.File;
import java.util.UUID;

public class SkinSetterAPI {

    private static SkinSetterAPI INSTANCE;
    private static ILogger LOGGER;

    private final PlayerDelegate del;
    private final ConfigProvider configProvider;

    private final SkinManager registry;
    private final File skinFile;

    private ConfigSection config;

    public boolean PERSISTENT_SKINS;
    public Skin DEFAULT_SKIN;

    private final ILangProvider langProvider;

    public SkinSetterAPI(ILogger logger, PlayerDelegate delegate, File configFolder, ConfigSection defaultLang, SkinManager registry) {

        if(INSTANCE == null) {
            INSTANCE = this;
            LOGGER = logger;
        }

        configProvider = MidnightCoreAPI.getInstance().getDefaultConfigProvider();
        skinFile = new File(configFolder, "config" + configProvider.getFileExtension());

        if(!skinFile.exists()) {
            configProvider.saveToFile(new ConfigSection(), skinFile);
        }

        langProvider = MidnightCoreAPI.getInstance().getModule(ILangModule.class).createLangProvider(new File(configFolder, "lang"), configProvider, defaultLang);

        this.del = delegate;
        this.config = configProvider.loadFromFile(skinFile);
        this.registry = registry;

        registry.init();

        reloadConfig();
        saveConfig();
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

    public SkinManager getSkinRegistry() {
        return registry;
    }

    public File getSkinFile() {
        return skinFile;
    }

    public void saveConfig() {
        configProvider.saveToFile(config, skinFile);
    }

    public void reloadConfig() {

        config = configProvider.loadFromFile(skinFile);

        if(!config.has("persistent_skins", Boolean.class)) {
            config.set("persistent_skins", false);
        }
        if(!config.has("default_skin")) {
            config.set("default_skin", "");
        }

        registry.loadSkins(config);

        PERSISTENT_SKINS = config.getBoolean("persistent_skins");
        DEFAULT_SKIN = registry.getSkin(config.getString("default_skin"));
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

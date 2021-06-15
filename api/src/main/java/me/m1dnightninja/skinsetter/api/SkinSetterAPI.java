package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.ILogger;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;

import java.io.File;

public class SkinSetterAPI {

    private static SkinSetterAPI INSTANCE;
    private static ILogger LOGGER;

    private final SkinManager registry;
    private final FileConfig config;

    public boolean PERSISTENT_SKINS;
    public Skin DEFAULT_SKIN;

    private final ConfigSection defaultConfig;
    private final ILangProvider langProvider;

    public SkinSetterAPI(ILogger logger, File configFolder, ConfigSection defaultLang, ConfigSection defaultConfig, SkinManager registry) {

        if(INSTANCE == null) {
            INSTANCE = this;
            LOGGER = logger;
        }

        ConfigProvider configProvider = MidnightCoreAPI.getInstance().getDefaultConfigProvider();

        File skinFile = new File(configFolder, "config" + configProvider.getFileExtension());
        this.config = new FileConfig(skinFile, configProvider);
        this.defaultConfig = defaultConfig;

        if(!skinFile.exists()) {
            config.save();
        }

        langProvider = MidnightCoreAPI.getInstance().getModule(ILangModule.class).createLangProvider(new File(configFolder, "lang"), configProvider, defaultLang);

        this.registry = registry;

        registry.init();

        reloadConfig();
        saveConfig();
    }

    public ConfigSection getConfig() {
        return config.getRoot();
    }

    public ILangProvider getLangProvider() {
        return langProvider;
    }

    public SkinManager getSkinRegistry() {
        return registry;
    }

    public void saveConfig() {

        config.save();
    }

    public void reloadConfig() {

        config.reload();
        config.getRoot().fill(defaultConfig);
        config.save();

        if(!config.getRoot().has("persistent_skins", Boolean.class)) {
            config.getRoot().set("persistent_skins", false);
        }
        if(!config.getRoot().has("default_skin")) {
            config.getRoot().set("default_skin", "");
        }

        registry.loadSkins(config.getRoot());

        PERSISTENT_SKINS = config.getRoot().getBoolean("persistent_skins");
        DEFAULT_SKIN = registry.getSkin(config.getRoot().getString("default_skin"));
    }

    public static SkinSetterAPI getInstance() {
        return INSTANCE;
    }

    public static ILogger getLogger() {
        return LOGGER;
    }

}

package me.m1dnightninja.skinsetter.common;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.skinsetter.api.SavedSkin;
import me.m1dnightninja.skinsetter.api.core.SkinManager;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;

import java.io.File;

public class SkinSetterImpl extends SkinSetterAPI {

    private final SkinManager registry;
    private final FileConfig config;

    private boolean persistence;
    private SavedSkin defaultSkin;

    private final ConfigSection defaultConfig;
    private final ILangProvider langProvider;

    public SkinSetterImpl(File configFolder, ConfigSection defaultLang, ConfigSection defaultConfig, SkinManager registry) {

        if(INSTANCE == null) {
            INSTANCE = this;
        }

        MidnightCoreAPI.getInstance().getConfigRegistry().registerSerializer(SavedSkin.class, SavedSkin.SERIALIZER);

        this.config = FileConfig.findOrCreate("config", configFolder);
        this.defaultConfig = defaultConfig;

        config.getRoot().fill(defaultConfig);
        config.save();

        ILangModule module = MidnightCoreAPI.getInstance().getModule(ILangModule.class);
        SavedSkin.registerPlaceholders(module);

        langProvider = module.createLangProvider(new File(configFolder, "lang"), defaultLang);


        this.registry = registry;

        registry.init();

        reloadConfig();
        saveConfig();
    }

    @Override
    public ConfigSection getConfig() {
        return config.getRoot();
    }

    @Override
    public ILangProvider getLangProvider() {
        return langProvider;
    }

    @Override
    public SkinManager getSkinManager() {
        return registry;
    }

    @Override
    public boolean isPersistenceEnabled() {
        return persistence;
    }

    @Override
    public void setPersistenceEnabled(boolean persist) {
        this.persistence = persist;
    }

    @Override
    public SavedSkin getDefaultSkin() {
        return defaultSkin;
    }

    @Override
    public void setDefaultSkin(SavedSkin skin) {
        this.defaultSkin = skin;
    }

    @Override
    public void saveConfig() {

        config.save();
    }

    @Override
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

        persistence = config.getRoot().getBoolean("persistent_skins");
        defaultSkin = registry.getSkin(config.getRoot().getString("default_skin"));

        langProvider.reloadAllEntries();
    }

}

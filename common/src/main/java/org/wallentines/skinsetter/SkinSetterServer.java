package org.wallentines.skinsetter;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.data.DataManager;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.skin.SkinModule;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.types.ResettableSingleton;

import java.io.File;
import java.nio.file.Path;

public class SkinSetterServer {

    public static final ResettableSingleton<SkinSetterServer> INSTANCE = new ResettableSingleton<>();

    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("persistence", false);

    private final FileWrapper<ConfigObject> config;
    private final LangManager manager;
    private final DataManager dataManager;
    private final Server server;
    private boolean persistence;
    private SavedSkin defaultSkin;

    private SkinSetterServer(Server server, LangRegistry langDefaults) {

        this.server = server;

        Path folderPath = server.getConfigDirectory().resolve("SkinSetter");
        File folder = folderPath.toFile();

        if(!folder.isDirectory() && !folder.mkdirs()) {
            throw new IllegalStateException("Unable to create data folder!");
        }

        config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", folder, DEFAULT_CONFIG);
        config.save();

        manager = new LangManager(langDefaults, folderPath.resolve("lang").toFile());
        SkinSetterAPI.REGISTRY.set(new SkinRegistry(folderPath.resolve("skins").toFile()));

        dataManager = new DataManager(folderPath.resolve("users").toFile());
        dataManager.cacheSize = 0;

        reload();
    }

    public FileWrapper<ConfigObject> getConfig() {
        return config;
    }

    public LangManager getLangManager() {
        return manager;
    }

    public boolean isPersistenceEnabled() {
        return persistence;
    }

    public void setPersistenceEnabled(boolean enabled) {
        persistence = enabled;
    }

    public SavedSkin getDefaultSkin() {

        return defaultSkin;
    }

    public void setDefaultSkin(SavedSkin defaultSkin) {
        this.defaultSkin = defaultSkin;
    }

    public void reload() {

        config.load();
        if(config.getRoot() == null) {
            throw new IllegalStateException("Unable to create config file!");
        }

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        reg.loadAll();

        persistence = config.getRoot().asSection().getBoolean("persistence");
        defaultSkin = config.getRoot().asSection().getOptional("default_skin", Serializer.STRING).map(reg::getSkin).orElse(null);
    }

    public void onJoin(Player player) {

        if(!persistence && defaultSkin == null) return;

        SkinModule mod = server.getModuleManager().getModule(SkinModule.class);
        if(mod == null) return;

        Skin skin = null;

        if(defaultSkin != null) {
            skin = defaultSkin.getSkin();
        }
        if(persistence) {
            String id = player.getUUID().toString();
            skin = dataManager.getData(id).get("skin", Skin.SERIALIZER);
        }

        if(skin != null) {
            mod.setSkin(player, skin);
        }
    }

    public void onLeave(Player player) {

        if(!persistence) return;

        SkinModule mod = server.getModuleManager().getModule(SkinModule.class);
        if(mod == null) return;

        String id = player.getUUID().toString();
        ConfigSection section = dataManager.getData(id).with("skin", mod.getSkin(player), Skin.SERIALIZER);
        dataManager.save(id, section);

    }

    public void onShutdown() {

        SkinSetterAPI.REGISTRY.get().saveAll();
        SkinSetterAPI.REGISTRY.reset();

        INSTANCE.reset();
    }

    public static void init(Server server, LangRegistry langDefaults) {

        INSTANCE.set(new SkinSetterServer(server, langDefaults));

    }

}

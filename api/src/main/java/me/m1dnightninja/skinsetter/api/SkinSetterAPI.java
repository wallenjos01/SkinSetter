package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.ILogger;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.io.File;
import java.util.UUID;

public class SkinSetterAPI {

    private static SkinSetterAPI INSTANCE;
    private static ILogger LOGGER;

    private final PlayerDelegate del;

    private final SkinRegistry registry;
    private final ConfigSection config;
    private final File skinFile;

    public SkinSetterAPI(ILogger logger, PlayerDelegate delegate, File configFolder) {

        if(INSTANCE == null) {
            INSTANCE = this;
            LOGGER = logger;
        }

        ConfigProvider prov = MidnightCoreAPI.getInstance().getDefaultConfigProvider();
        skinFile = new File(configFolder, "config" + prov.getFileExtension());

        if(!skinFile.exists()) {
            prov.saveToFile(new ConfigSection(), skinFile);
        }

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

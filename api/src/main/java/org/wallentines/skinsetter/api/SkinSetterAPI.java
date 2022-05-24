package org.wallentines.skinsetter.api;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightlib.config.ConfigSection;

import java.io.File;

public abstract class SkinSetterAPI {

    private static final Logger LOGGER = LogManager.getLogger("SkinSetter");
    private static SkinSetterAPI INSTANCE;

    protected SkinSetterAPI() {
        if(INSTANCE == null) INSTANCE = this;
    }

    /**
     * Returns the main config for this mod
     * 
     * @return The main config
     */
    public abstract ConfigSection getConfig();

    /**
     * Saves the config to the disk
     */
    public abstract void saveConfig();

    /**
     * Returns the folder where this mod stores data
     * 
     * @return The data folder
     */
    public abstract File getDataFolder();

    /**
     * Returns the global SkinManager object
     *
     * @return The skin manager
     */
    public abstract SkinRegistry getSkinRegistry();

    /**
     * Returns the default skin, if applicable
     *
     * @return The default skin
     */
    public abstract SavedSkin getDefaultSkin();

    /**
     * Changes the default skin
     *
     * @param skin The new default skin
     */
    public abstract void setDefaultSkin(SavedSkin skin);

    /**
     * Determines whether persistence is enabled
     *
     * @return Whether persistence is enabled
     */
    public abstract boolean isPersistenceEnabled();

    /**
     * Enables or disables persistence
     *
     * @param persistence Whether persistence should be enabled or disabled
     */
    public abstract void setPersistenceEnabled(boolean persistence);

    /**
     * Retrieves the lang provider for this mod instance
     *
     * @return the lang provider
     */
    public abstract LangProvider getLangProvider();


    /**
     * Returns the global MidnightCoreAPI instance
     *
     * @return The global api
     */
    public static SkinSetterAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the global MidnightCoreAPI logger
     *
     * @return The logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }

}

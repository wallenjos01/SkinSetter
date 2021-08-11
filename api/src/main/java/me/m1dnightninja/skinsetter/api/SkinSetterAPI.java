package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.skinsetter.api.core.SkinManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class SkinSetterAPI {

    protected static SkinSetterAPI INSTANCE;
    protected static Logger LOGGER = LogManager.getLogger("SkinSetter");

    /**
     * Retrieves the Config Section loaded from config.json / config.yml
     *
     * @return The main config as a ConfigSection
     */
    public abstract ConfigSection getConfig();

    /**
     * Retrieves the Lang Provider associated with the mod
     *
     * @return The LangProvider object
     */
    public abstract ILangProvider getLangProvider();

    /**
     * Retrieves the global Skin Manager object
     *
     * @return The SkinManager object
     */
    public abstract SkinManager getSkinManager();

    /**
     * Saves the main ConfigSection to the disk
     */
    public abstract void saveConfig();

    /**
     * Reloads the main ConfigSection from the disk
     */
    public abstract void reloadConfig();

    /**
     * Queries whether skin persistence is enabled
     *
     * @return Whether persistence is enabled, represented as a boolean
     */
    public abstract boolean isPersistenceEnabled();

    /**
     * Enables or disables skin persistence
     *
     * @param persist Whether to enable or disable persistence
     */
    public abstract void setPersistenceEnabled(boolean persist);

    /**
     * Retrieves the default skin
     *
     * @return The default skin as a SavedSkin
     */
    @Nullable
    public abstract SavedSkin getDefaultSkin();

    /**
     * Changes the default skin
     *
     * @param skin The new default skin, as a SavedSkin
     */
    public abstract void setDefaultSkin(@Nullable SavedSkin skin);

    /**
     * Returns the first created instance of the API (Usually by the mod itself)
     *
     * @return The SkinSetterAPI object
     */
    public static SkinSetterAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the single logger associated with the mod
     *
     * @return The Logger object
     */
    public static Logger getLogger() {
        return LOGGER;
    }

}

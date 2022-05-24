package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.PlaceholderSupplier;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.skinsetter.api.SavedSkin;

import java.util.ArrayList;
import java.util.Random;

public final class Constants {

    public static final String DEFAULT_NAMESPACE = "skinsetter";

    public static final String CONFIG_KEY_PERSISTENCE = "persistent_skins";
    public static final String CONFIG_KEY_DEFAULT_SKIN = "default_skin";
    public static final String CONFIG_KEY_SKINS = "skins";

    public static final Random RANDOM = new Random();

    public static final ConfigSection CONFIG_DEFAULTS = new ConfigSection()
            .with(CONFIG_KEY_SKINS, new ArrayList<>())
            .with(CONFIG_KEY_PERSISTENCE, false)
            .with(CONFIG_KEY_DEFAULT_SKIN, "");

    public static void registerIntegrations() {

        ConfigRegistry.INSTANCE.registerSerializer(SavedSkinImpl.class, SavedSkinImpl.SERIALIZER);

        LangModule module = MidnightCoreAPI.getInstance().getModuleManager().getModule(LangModule.class);
        module.registerInlinePlaceholder("skinsetter_skin_id", PlaceholderSupplier.create(SavedSkin.class, SavedSkin::getId));
        module.registerPlaceholder("skinsetter_skin_name", PlaceholderSupplier.create(SavedSkin.class, SavedSkin::getName));
    }

}

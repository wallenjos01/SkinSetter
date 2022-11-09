package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.skinsetter.api.SavedSkin;

import java.util.Random;

public final class Constants {

    public static final String DEFAULT_NAMESPACE = "skinsetter";

    public static final String CONFIG_KEY_PERSISTENCE = "persistent_skins";
    public static final String CONFIG_KEY_DEFAULT_SKIN = "default_skin";

    public static final Random RANDOM = new Random();

    public static final ConfigSection CONFIG_DEFAULTS = new ConfigSection()
            .with(CONFIG_KEY_PERSISTENCE, false)
            .with(CONFIG_KEY_DEFAULT_SKIN, "");

    public static void registerIntegrations() {

        ConfigRegistry.INSTANCE.registerSerializer(SavedSkinImpl.class, SavedSkinImpl.SERIALIZER);

        PlaceholderManager.INSTANCE.getInlinePlaceholders().register("skinsetter_skin_id", PlaceholderSupplier.create(SavedSkin.class, SavedSkin::getId));
        PlaceholderManager.INSTANCE.getPlaceholders().register("skinsetter_skin_name", PlaceholderSupplier.create(SavedSkin.class, SavedSkin::getName));
    }

}

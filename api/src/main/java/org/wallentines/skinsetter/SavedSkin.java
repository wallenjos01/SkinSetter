package org.wallentines.skinsetter;

import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ConfigSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.List;

public class SavedSkin {
    private final Skin skin;
    private final SkinConfiguration config;

    public SavedSkin(Skin skin, SkinConfiguration config) {
        this.skin = skin;
        this.config = config;
    }

    public SkinConfiguration getConfig() {
        return config;
    }

    public String getPermission() {
        return config.getPermission();
    }

    public Component getDisplayName() {
        return config.getDisplayName();
    }

    public Skin getSkin() {
        return skin;
    }

    public List<String> getGroups() {
        return config.getGroups();
    }

    public boolean isExcludedInRandom() {
        Boolean excluded = config.isExcludedInRandom();
        return excluded != null && excluded;
    }

    public boolean isExcludedInGUI() {
        Boolean excluded = config.isExcludedInGUI();
        return excluded != null && excluded;
    }

    public boolean canUse(Player player) {
        String permission = config.getPermission();
        return permission == null || player.hasPermission(permission, 4);
    }

    public ItemStack getDisplayItem() {
        if(config.getDisplayItem() == null) {
            Component displayName = getDisplayName();
            if(displayName == null) displayName = Component.empty();
            return ItemStack.Builder.headWithSkin(skin).withName(displayName).build();
        }
        return config.getDisplayItem().copy();
    }


    public static final Serializer<SavedSkin> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> serializeContext, SavedSkin savedSkin) {
            return SkinConfiguration.SERIALIZER.serialize(serializeContext, savedSkin.config)
                    .map(o -> Skin.SERIALIZER.serialize(serializeContext, savedSkin.skin)
                            .flatMap(o1 -> serializeContext.set("skin", o1, o))
                    );
        }

        @Override
        public <O> SerializeResult<SavedSkin> deserialize(SerializeContext<O> serializeContext, O o) {
            return SkinConfiguration.SERIALIZER.deserialize(serializeContext, o)
                    .map(config -> Skin.SERIALIZER.deserialize(serializeContext, serializeContext.get("skin", o))
                            .flatMap(skin -> new SavedSkin(skin, config))
                    );
        }
    };

    public static final Serializer<SavedSkin> LEGACY_SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("id", sk -> ""),
            Skin.SERIALIZER.entry("skin", SavedSkin::getSkin),
            ConfigSerializer.INSTANCE.<SavedSkin>entry("name", ss -> ss.getConfig().getDisplayName()).optional(),
            Serializer.BOOLEAN.<SavedSkin>entry("in_random_selection", ss -> !ss.getConfig().isExcludedInRandom()).orElse(false),
            ItemStack.SERIALIZER.<SavedSkin>entry("item", ss -> ss.getConfig().getDisplayItem()).optional(),
            Serializer.STRING.listOf().<SavedSkin>entry("groups", ss -> ss.getConfig().getGroups()).optional(),
            (id, sk, name, rand, item, groups) -> new SavedSkin(sk, new SkinConfiguration(name, "skinsetter.skin." + id, groups, !rand, false, item))
    );

}

package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.skinsetter.api.SavedSkin;

import java.util.*;

public class SavedSkinImpl implements SavedSkin {

    private final String id;
    private final Skin skin;

    private final MComponent name;
    private final boolean excludeFromRandom;

    private final MItemStack cachedItem;
    private final boolean customItem;

    private final Set<String> groups;

    public SavedSkinImpl(String id, Skin skin) {
        this(id, skin, new MTextComponent(id), false, null, new ArrayList<>());
    }

    public SavedSkinImpl(String id, Skin skin, MComponent name, boolean excludeFromRandom, MItemStack item, Collection<String> groups) {
        this.id = id;
        this.skin = skin;
        this.name = name;

        this.groups = new HashSet<>();
        this.groups.addAll(groups);

        this.excludeFromRandom = excludeFromRandom;

        this.customItem = item != null;
        this.cachedItem = customItem ? item : generateHeadItem(skin, name);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Skin getSkin() {
        return skin;
    }

    @Override
    public MComponent getName() {
        return name;
    }

    @Override
    public boolean inRandomSelection() {
        return excludeFromRandom;
    }

    @Override
    public MItemStack getDisplayItem() {
        return cachedItem;
    }

    @Override
    public MItemStack getCustomItem() {
        return customItem ? cachedItem : null;
    }

    @Override
    public MItemStack getHeadItem() {
        return customItem ? generateHeadItem(skin, name) : cachedItem;
    }

    @Override
    public boolean hasCustomItem() {
        return customItem;
    }

    @Override
    public Collection<String> getGroups() {
        return groups;
    }

    @Override
    public boolean hasGroup(String group) {
        return groups.contains(group);
    }

    @Override
    public boolean canUse(MPlayer player) {
        return canUse(this, player);
    }

    public static boolean canUse(SavedSkin sk, MPlayer player) {

        if(player == null) return true;

        for(String s : sk.getGroups()) {
            if(player.hasPermission(Constants.DEFAULT_NAMESPACE + ".group." + s)) return true;
        }
        return player.hasPermission(Constants.DEFAULT_NAMESPACE + ".skin." + sk.getSkin(), 2);
    }

    public static MItemStack generateHeadItem(Skin skin, MComponent name) {
        return MItemStack.Builder.headWithSkin(skin).withName(name.copy()).build();
    }

    public static final ConfigSerializer<SavedSkinImpl> SERIALIZER = ConfigSerializer.create(
            ConfigSerializer.entry(PrimitiveSerializers.STRING, "id", SavedSkinImpl::getId),
            ConfigSerializer.entry(Skin.SERIALIZER, "skin", SavedSkinImpl::getSkin),
            ConfigSerializer.entry(MComponent.INLINE_SERIALIZER, "name", SavedSkinImpl::getName).optional(),
            ConfigSerializer.entry(PrimitiveSerializers.BOOLEAN, "in_random_selection", SavedSkinImpl::inRandomSelection).orDefault(true),
            ConfigSerializer.entry(MItemStack.class, "item", SavedSkinImpl::getCustomItem).optional(),
            PrimitiveSerializers.STRING.listOf().entry("groups", SavedSkinImpl::getGroups).optional(),
            SavedSkinImpl::new
    );

    public static final ConfigSerializer<SavedSkinImpl> SERIALIZER_LEGACY = new ConfigSerializer<>() {
        @Override
        public SavedSkinImpl deserialize(ConfigSection section) {

            String id = section.getString("id");
            Skin skin = Skin.SERIALIZER.deserialize(section);

            MComponent name = section.getOrDefault("name", new MTextComponent(id), MComponent.class);
            boolean excludeFromRandom = section.getBoolean("in_random");
            MItemStack item = section.getOrDefault("item", null, MItemStack.class);
            List<String> groups = new ArrayList<>();
            if(section.has("groups")) groups.addAll(section.getListFiltered("groups", String.class));

            return new SavedSkinImpl(id, skin, name, excludeFromRandom, item, groups);
        }

        @Override
        public ConfigSection serialize(SavedSkinImpl object) {

            ConfigSection out = Skin.SERIALIZER.serialize(object.skin)
                    .with("id", object.id)
                    .with("name", object.name)
                    .with("in_random", !object.excludeFromRandom);

            if(object.customItem) out.set("item", object.cachedItem);
            if(!object.groups.isEmpty()) out.set("groups", new ArrayList<>(object.groups));

            return out;
        }
    };

    public static void updateFromOldConfig(ConfigSection section, SkinRegistryImpl registry) {

        for(ConfigSection sec : section.getListFiltered("skins", ConfigSection.class)) {

            SavedSkinImpl skin = SERIALIZER_LEGACY.deserialize(sec);
            registry.registerSkin(skin);
        }

        section.set("skins", null);
    }
}

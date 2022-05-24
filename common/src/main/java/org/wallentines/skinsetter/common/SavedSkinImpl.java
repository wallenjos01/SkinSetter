package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.skinsetter.api.SavedSkin;

import java.util.*;

public class SavedSkinImpl implements SavedSkin {

    private final String id;
    private final Skin skin;

    private MComponent name;
    private boolean excludeFromRandom;

    private MItemStack cachedItem;
    private boolean customItem;

    private final Set<String> groups;

    public SavedSkinImpl(String id, Skin skin) {
        this.id = id;
        this.skin = skin;
        this.name = new MTextComponent(id);

        this.groups = new HashSet<>();
        this.cachedItem = generateHeadItem();
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
    public void setName(MComponent name) {
        this.name = name;
        if(!customItem) cachedItem.setName(name.copy());
    }

    @Override
    public boolean excludedFromRandom() {
        return excludeFromRandom;
    }

    @Override
    public void excludeFromRandom(boolean exclude) {
        this.excludeFromRandom = exclude;
    }

    @Override
    public MItemStack getDisplayItem() {
        return cachedItem;
    }

    @Override
    public void setDisplayItem(MItemStack item) {

        if(item == null) {
            this.cachedItem = generateHeadItem();
            customItem = false;
        } else {
            this.cachedItem = item;
            customItem = true;
        }
    }

    public MItemStack getCustomItem() {
        return customItem ? cachedItem : null;
    }
    @Override
    public MItemStack getHeadItem() {
        return customItem ? generateHeadItem() : cachedItem;
    }

    @Override
    public Collection<String> getGroups() {
        return groups;
    }

    @Override
    public void addGroup(String group) {
        groups.add(group);
    }

    @Override
    public boolean hasGroup(String group) {
        return groups.contains(group);
    }

    @Override
    public boolean canUse(MPlayer player) {

        if(player == null) return true;

        for(String s : groups) {
            if(player.hasPermission(Constants.DEFAULT_NAMESPACE + ".group." + s)) return true;
        }
        return player.hasPermission(Constants.DEFAULT_NAMESPACE + ".skin." + id, 2);
    }

    private MItemStack generateHeadItem() {
        return MItemStack.Builder.headWithSkin(skin).withName(name.copy()).build();
    }

    public static final ConfigSerializer<SavedSkinImpl> SERIALIZER = ConfigSerializer.create(
            ConfigSerializer.entry(String.class, "id", SavedSkinImpl::getId),
            ConfigSerializer.entry(Skin.class, "skin", SavedSkinImpl::getSkin),
            ConfigSerializer.entry(MComponent.class, "name", SavedSkinImpl::getName).optional(),
            ConfigSerializer.entry(Boolean.class, "in_random_selection", SavedSkinImpl::excludedFromRandom).orDefault(false),
            ConfigSerializer.entry(MItemStack.class, "item", SavedSkinImpl::getCustomItem).optional(),
            ConfigSerializer.<String, SavedSkinImpl>listEntry("groups", sk -> new ArrayList<>(sk.getGroups())).optional(),
            (id, skin, name, random, item, groups) -> {

                SavedSkinImpl out = new SavedSkinImpl(id, skin);
                if(name != null) out.setName(name);
                out.excludeFromRandom(!random);
                if(item != null) out.setDisplayItem(item);
                if(groups != null) out.groups.addAll(groups);

                return out;
            }

    );

    public static final ConfigSerializer<SavedSkinImpl> SERIALIZER_LEGACY = new ConfigSerializer<>() {
        @Override
        public SavedSkinImpl deserialize(ConfigSection section) {

            String id = section.getString("id");
            Skin skin = Skin.SERIALIZER.deserialize(section);

            SavedSkinImpl out = new SavedSkinImpl(id, skin);
            if(section.has("name")) out.setName(section.get("name", MComponent.class));
            if(section.has("in_random", Boolean.class)) out.excludeFromRandom(!section.getBoolean("in_random"));
            if(section.has("item")) out.setDisplayItem(section.get("item", MItemStack.class));
            if(section.has("groups")) out.groups.addAll(section.getListFiltered("groups", String.class));

            return out;
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

        for(ConfigSection sec : section.getListFiltered("skins", section)) {

            SavedSkinImpl skin = SERIALIZER_LEGACY.deserialize(sec);
            registry.registerSkin(skin);
        }

        section.set("skins", null);
    }
}

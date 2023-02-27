package org.wallentines.skinsetter.common;

import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.mdcfg.ConfigSection;
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
        if(groups != null) this.groups.addAll(groups);

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

    public static final Serializer<SavedSkinImpl> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("id", SavedSkinImpl::getId),
            Skin.SERIALIZER.entry("skin", SavedSkinImpl::getSkin),
            MComponent.SERIALIZER.entry("name", SavedSkinImpl::getName).optional(),
            Serializer.BOOLEAN.entry("in_random_selection", SavedSkinImpl::inRandomSelection).orElse(true),
            MItemStack.SERIALIZER.entry("item", SavedSkinImpl::getCustomItem).optional(),
            Serializer.STRING.listOf().entry("groups", SavedSkinImpl::getGroups).optional(),
            SavedSkinImpl::new
    );

    public static final Serializer<SavedSkinImpl> SERIALIZER_LEGACY = new Serializer<>() {
        @Override
        public <O> SerializeResult<SavedSkinImpl> deserialize(SerializeContext<O> context, O value) {

            return Serializer.STRING.deserialize(context, context.get("id", value)).and(
                    Skin.SERIALIZER.deserialize(context, value),
                    MComponent.SERIALIZER.deserialize(context, context.get("name", value)),
                    Serializer.BOOLEAN.deserialize(context, context.get("in_random", value)),
                    MItemStack.SERIALIZER.deserialize(context, context.get("item", value)),
                    Serializer.STRING.listOf().deserialize(context, context.get("groups", value))
            ).flatMap(t6 -> new SavedSkinImpl(t6.p1, t6.p2, t6.p3, t6.p4, t6.p5, t6.p6));
        }

        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, SavedSkinImpl object) {

            ConfigSection out = Skin.SERIALIZER.serialize(ConfigContext.INSTANCE, object.skin).getOrThrow().asSection()
                    .with("id", object.id)
                    .with("name", object.name, MComponent.SERIALIZER)
                    .with("in_random", !object.excludeFromRandom);

            if(object.customItem) out.set("item", object.cachedItem, MItemStack.SERIALIZER);
            if(!object.groups.isEmpty()) out.set("groups", ConfigList.of(object.groups));

            return SerializeResult.success(ConfigContext.INSTANCE.convert(context, out));
        }
    };

    public static void updateFromOldConfig(ConfigSection section, SkinRegistryImpl registry) {

        for(ConfigSection sec : section.getListFiltered("skins", ConfigSection.SERIALIZER)) {

            SERIALIZER_LEGACY.deserialize(ConfigContext.INSTANCE, sec).get().ifPresent(registry::registerSkin);
        }

        section.remove("skins");
    }
}

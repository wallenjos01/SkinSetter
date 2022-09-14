package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.skinsetter.api.EditableSkin;
import org.wallentines.skinsetter.api.SavedSkin;

import java.util.Collection;
import java.util.HashSet;

public class EditableSkinImpl implements EditableSkin {

    private final SkinRegistryImpl registry;
    private final String file;

    private final String id;
    private final Skin skin;

    private MComponent name;
    private boolean excludeFromRandom;

    private MItemStack displayItem;
    private boolean customItem;

    private final HashSet<String> groups = new HashSet<>();

    public EditableSkinImpl(SavedSkin sk, SkinRegistryImpl registry, String file) {

        this.registry = registry;
        this.file = file;

        this.id = sk.getId();
        this.skin = sk.getSkin();

        this.name = sk.getName();
        this.excludeFromRandom = sk.excludedFromRandom();
        this.displayItem = sk.getCustomItem();
        this.customItem = sk.hasCustomItem();

        this.groups.addAll(sk.getGroups());
    }

    @Override
    public void setName(MComponent name) {
        this.name = name == null ? new MTextComponent(id) : name;
    }

    @Override
    public void excludeFromRandom(boolean exclude) {
        this.excludeFromRandom = exclude;
    }

    @Override
    public void setDisplayItem(MItemStack item) {
        this.customItem = item != null;
        this.displayItem = customItem ? item : getHeadItem();
    }

    @Override
    public void addGroup(String group) {
        groups.add(group);
    }

    @Override
    public void removeGroup(String group) {
        groups.remove(group);
    }

    @Override
    public void clearGroups() {
        groups.clear();
    }

    @Override
    public void save() {

        SavedSkinImpl out = new SavedSkinImpl(id, skin, name, excludeFromRandom, displayItem, groups);
        registry.updateSkin(out, file);
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
    public boolean excludedFromRandom() {
        return excludeFromRandom;
    }

    @Override
    public MItemStack getDisplayItem() {
        return displayItem;
    }

    @Override
    public MItemStack getHeadItem() {
        return customItem ? SavedSkinImpl.generateHeadItem(skin, name) : displayItem;
    }

    @Override
    public MItemStack getCustomItem() {
        return customItem ? displayItem : null;
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
        return SavedSkinImpl.canUse(this, player);
    }
}

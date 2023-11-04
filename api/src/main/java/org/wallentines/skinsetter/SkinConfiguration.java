package org.wallentines.skinsetter;

import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ConfigSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SkinConfiguration {

    private final Component name;
    private final String permission;
    private final List<String> groups = new ArrayList<>();
    private final Boolean excludedInRandom;
    private final Boolean excludedInGUI;
    private final ItemStack displayItem;

    public SkinConfiguration(Component name, String permission, Collection<String> groups, Boolean excludedInRandom, Boolean excludedInGUI, ItemStack displayItem) {
        this.name = name;
        this.permission = permission;
        this.groups.addAll(groups);
        this.excludedInRandom = excludedInRandom;
        this.excludedInGUI = excludedInGUI;
        this.displayItem = displayItem;
    }

    public Component getDisplayName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getGroups() {
        return groups;
    }

    public Boolean isExcludedInRandom() {
        return excludedInRandom;
    }

    public Boolean isExcludedInGUI() {
        return excludedInGUI;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public SavedSkin createSkin(Skin value) {
        return new SavedSkin(value, this);
    }

    public static final Serializer<SkinConfiguration> SERIALIZER = ObjectSerializer.create(
            ConfigSerializer.INSTANCE.entry("name", SkinConfiguration::getDisplayName).optional(),
            Serializer.STRING.entry("permission", SkinConfiguration::getPermission).optional(),
            Serializer.STRING.listOf().entry("groups", SkinConfiguration::getGroups).orElse(List.of()),
            Serializer.BOOLEAN.entry("excludeInRandom", SkinConfiguration::isExcludedInRandom).optional(),
            Serializer.BOOLEAN.entry("excludeInGUI", SkinConfiguration::isExcludedInGUI).optional(),
            ItemStack.SERIALIZER.entry("item", SkinConfiguration::getDisplayItem).optional(),
            SkinConfiguration::new
    );


    public static class Builder {

        private Component name;
        private String permission;
        private final List<String> groups = new ArrayList<>();
        private Boolean excludedInRandom;
        private Boolean excludedInGUI;
        private ItemStack displayItem;


        public Builder() { }
        public Builder(SkinConfiguration config) {
            this.name = config.name;
            this.permission = config.permission;
            this.groups.addAll(config.groups);
            this.excludedInRandom = config.excludedInRandom;
            this.excludedInGUI = config.excludedInGUI;
            this.displayItem = config.displayItem;
        }

        public Builder displayName(Component name) {
            this.name = name;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder group(String group) {
            this.groups.add(group);
            return this;
        }

        public Builder groups(Collection<String> group) {
            this.groups.addAll(group);
            return this;
        }

        public Builder excludeInRandom(boolean exclude) {
            this.excludedInRandom = exclude;
            return this;
        }

        public Builder excludeInGUI(boolean exclude) {
            this.excludedInGUI = exclude;
            return this;
        }

        public Builder displayItem(ItemStack itemStack) {
            this.displayItem = itemStack;
            return this;
        }

        public SkinConfiguration build() {
            return new SkinConfiguration(name, permission, groups, excludedInRandom, excludedInGUI, displayItem);
        }

    }

}

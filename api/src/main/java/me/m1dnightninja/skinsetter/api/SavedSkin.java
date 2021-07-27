package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.PlaceholderSupplier;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.ArrayList;
import java.util.List;

public class SavedSkin {

    public static void registerPlaceholders(ILangModule module) {

        module.registerInlinePlaceholderSupplier("skinsetter_skin_id", PlaceholderSupplier.create(SavedSkin.class, SavedSkin::getId));
        module.registerPlaceholderSupplier("skinsetter_skin_name", PlaceholderSupplier.create(SavedSkin.class, SavedSkin::getName));

    }

    private final String id;
    private final Skin skin;

    private MComponent name;

    private MItemStack cachedItem;
    private boolean customItem = false;
    private boolean inRandom = true;

    private final List<String> groups = new ArrayList<>();

    public SavedSkin(String id, Skin skin) {

        this.id = id;
        this.skin = skin;
        this.name = MComponent.createTextComponent(id);

    }

    public String getId() {
        return id;
    }

    public MComponent getName() {

        return name;
    }

    public Skin getSkin() {
        return skin;
    }

    public boolean excludedFromRandom() {
        return !inRandom;
    }

    public MItemStack getItemStack() {
        if(cachedItem == null) {
            cachedItem = MItemStack.Builder.headWithSkin(skin).withName(getName()).build();
        }

        return cachedItem;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void addGroup(String s) {

        if(!groups.contains(s)) {
            groups.add(s);
        }
    }

    public void setName(MComponent name) {
        if(name == null) {
            this.name = MComponent.createTextComponent(id);
        } else {
            this.name = name;
        }

        if(!customItem) cachedItem = null;
    }

    public void setCustomItem(MItemStack newItem) {

        this.customItem = newItem != null;
        this.cachedItem = newItem;
    }

    public void setInRandom(boolean inRandom) {
        this.inRandom = inRandom;
    }

    public boolean canUse(MPlayer player) {

        for(String s : groups) {
            if(player.hasPermission("skinsetter.group." + s)) return true;
        }

        return player.hasPermission("skinsetter.skin." + id);
    }

    public static ConfigSerializer<SavedSkin> SERIALIZER = new ConfigSerializer<SavedSkin>() {
        @Override
        public SavedSkin deserialize(ConfigSection section) {

            String id = section.getString("id");
            Skin s = Skin.SERIALIZER.deserialize(section);

            SavedSkin out = new SavedSkin(id, s);

            if(section.has("name")) {
                out.name = MComponent.Serializer.parse(section.getString("name"));
            }

            if(section.has("item", MItemStack.class)) {
                out.customItem = true;
                out.cachedItem = section.get("item", MItemStack.class);
            }

            if(section.has("groups", List.class)) {

                out.groups.addAll(section.getListFiltered("groups", String.class));
            }

            if(section.has("in_random", Boolean.class)) {

                out.inRandom = section.getBoolean("in_random");
            }

            return out;
        }

        @Override
        public ConfigSection serialize(SavedSkin object) {

            ConfigSection out = new ConfigSection();
            out.set("id", object.id);
            out.set("uid", object.skin.getUUID());
            out.set("b64", object.skin.getBase64());
            out.set("sig", object.skin.getSignature());
            out.set("name", MComponent.Serializer.serialize(object.name));
            out.set("groups", object.groups);
            if(object.customItem) {
                out.set("item", object.cachedItem);
            }
            if(!object.inRandom) {
                out.set("in_random", false);
            }

            return out;
        }
    };

}

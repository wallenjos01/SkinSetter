package org.wallentines.skinsetter;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.io.File;
import java.util.*;

public class SkinFile {

    private final FileWrapper<ConfigObject> file;
    private boolean hasChanged;
    private final StringRegistry<RegisteredSkin> skins = new StringRegistry<>();
    private PropertyDefaults defaults = new PropertyDefaults();

    public SkinFile(String prefix, File folder, FileCodecRegistry registry) {
        this.file = registry.findOrCreate(ConfigContext.INSTANCE, prefix, folder, new ConfigSection());
    }

    public SkinFile(File file, FileCodecRegistry registry) {
        this.file = registry.fromFile(ConfigContext.INSTANCE, file);
    }

    public void load() {

        skins.clear();
        hasChanged = false;

        file.load();
        if(!(file.getRoot() instanceof ConfigSection)) {
            SkinSetterAPI.LOGGER.warn("File " + file.getFile().getName() + " does not have a ConfigSection at its root!");
            defaults = new PropertyDefaults();
            return;
        }


        defaults = file.getRoot().asSection().getOptional("defaults", PropertyDefaults.SERIALIZER).orElse(new PropertyDefaults());

        ConfigObject obj = file.getRoot().asSection().get("skins");
        if(obj == null) {
            return;
        }

        // v4.0 file
        if(obj.isSection()) {
            for(String key : obj.asSection().getKeys()) {

                SerializeResult<SavedSkin> result = SavedSkin.SERIALIZER.deserialize(ConfigContext.INSTANCE, obj.asSection().get(key));
                if(result.isComplete()) {

                    String finalKey = defaults.applyId(key);
                    SavedSkin skin = result.getOrThrow();

                    skins.register(finalKey, RegisteredSkin.create(key, finalKey, defaults, skin.getSkin(), skin.getConfig()));

                } else {

                    SkinSetterAPI.LOGGER.error("Unable to deserialize skin with ID " + key + "! " + result.getError());
                }
            }

        // v3.0 file
        } else if(obj.isList()) {

            for(ConfigObject skin : obj.asList().values()) {

                SerializeResult<SavedSkin> result = SavedSkin.LEGACY_SERIALIZER.deserialize(ConfigContext.INSTANCE, skin);
                if(result.isComplete()) {

                    String id = skin.asSection().getString("id");

                    SavedSkin saved = result.getOrThrow();
                    skins.register(id, RegisteredSkin.create(id, id, defaults, saved.getSkin(), saved.getConfig()));
                    hasChanged = true;

                } else {
                    SkinSetterAPI.LOGGER.error("Unable to deserialize legacy skin in file " + file.getFile().getName() + "! " + result.getError());
                }
            }
        }
    }

    public SavedSkin getSkin(String name) {

        RegisteredSkin out = skins.get(name);
        if(out == null) return null;
        return out.registered;
    }

    public SavedSkin getSkin(int index) {

        RegisteredSkin out = skins.valueAtIndex(index);
        if(out == null) return null;
        return out.registered;
    }

    public SkinConfiguration getSavedConfiguration(String name) {
        RegisteredSkin out = skins.get(name);
        if(out == null) return null;
        return out.config;
    }

    public Collection<String> getSkinIds() {
        return skins.getIds();
    }

    public void registerSkin(String id, SavedSkin skin) {

        hasChanged = true;

        // Update existing skin if necessary
        RegisteredSkin rsk = skins.get(id);
        if(rsk != null) {
            rsk.config = skin.getConfig();
            rsk.registered = skin;
            return;
        }

        String finalId = defaults.applyId(id);
        skins.register(finalId, RegisteredSkin.create(id, finalId, defaults, skin.getSkin(), skin.getConfig()));
    }

    public void saveIfChanged() {

        if(!hasChanged) return;
        save();
    }

    public void save() {

        ConfigSection out = new ConfigSection();
        out.set("defaults", defaults, PropertyDefaults.SERIALIZER);

        ConfigSection skinSection = out.getOrCreateSection("skins");
        for(RegisteredSkin skin : skins) {
            skinSection.set(skin.id, skin.toConfigSkin(), SavedSkin.SERIALIZER);
        }

        file.setRoot(out);
        file.save();

        hasChanged = false;
    }

    public int getSize() {
        return skins.getSize();
    }

    private static class RegisteredSkin {

        final String id;
        SavedSkin registered;
        SkinConfiguration config;

        RegisteredSkin(String id, SavedSkin registered, SkinConfiguration config) {
            this.id = id;
            this.registered = registered;
            this.config = config;
        }

        SavedSkin toConfigSkin() {
            return new SavedSkin(registered.getSkin(), config);
        }

        static RegisteredSkin create(String rawId, String alteredId, PropertyDefaults defaults, Skin skin, SkinConfiguration config) {
            return new RegisteredSkin(rawId, new SavedSkin(skin, defaults.apply(alteredId, rawId, config)), config);
        }
    }


    private static class PropertyDefaults {

        private final String idPattern;
        private final String namePattern;
        private final String permissionPattern;
        private final Collection<String> additionalGroups;
        private final Boolean excludeInRandom;
        private final Boolean excludeInGui;

        public PropertyDefaults() {
            this(null, "&e%id%", null, null, null, null);
        }

        public PropertyDefaults(String idPattern, @NotNull String namePattern, String permissionPattern, Collection<String> additionalGroups, Boolean excludeInRandom, Boolean excludeInGui) {
            this.idPattern = idPattern;
            this.namePattern = namePattern;
            this.permissionPattern = permissionPattern;
            this.additionalGroups = additionalGroups;
            this.excludeInRandom = excludeInRandom;
            this.excludeInGui = excludeInGui;
        }

        public String applyId(String id) {
            if(idPattern != null) {
                PlaceholderContext ctx = new PlaceholderContext(List.of(CustomPlaceholder.inline("id", id)));
                return PlaceholderManager.INSTANCE.parseAndResolve(idPattern, ctx).allText();
            }
            return id;
        }

        public SkinConfiguration apply(String id, String preId, SkinConfiguration config) {
            PlaceholderContext ctx = new PlaceholderContext(List.of(CustomPlaceholder.inline("id", id), CustomPlaceholder.inline("raw_id", preId)));
            SkinConfiguration.Builder builder = new SkinConfiguration.Builder(config);

            if (config.getDisplayName() == null) {
                builder.displayName(PlaceholderManager.INSTANCE.parseAndResolve(namePattern, ctx));
            }

            if(permissionPattern != null) {

                builder.permission(PlaceholderManager.INSTANCE.parseAndResolve(permissionPattern, ctx).allText());
            }

            if(additionalGroups != null) {
                builder.groups(additionalGroups);
            }

            if(excludeInRandom != null) {
                builder.excludeInRandom(excludeInRandom);
            }

            if(excludeInGui != null) {
                builder.excludeInRandom(excludeInGui);
            }

            return builder.build();
        }

        static final Serializer<PropertyDefaults> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.<PropertyDefaults>entry("id", pd -> pd.idPattern).optional(),
                Serializer.STRING.<PropertyDefaults>entry("name", pd -> pd.namePattern).orElse("&e%id%"),
                Serializer.STRING.<PropertyDefaults>entry("permission", pd -> pd.permissionPattern).optional(),
                Serializer.STRING.listOf().<PropertyDefaults>entry("groups", pd -> pd.additionalGroups).optional(),
                Serializer.BOOLEAN.<PropertyDefaults>entry("excludeInRandom", pd -> pd.excludeInRandom).optional(),
                Serializer.BOOLEAN.<PropertyDefaults>entry("excludeInGUI", pd -> pd.excludeInGui).optional(),
                PropertyDefaults::new
        );

    }

}

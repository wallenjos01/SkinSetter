package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.skinsetter.api.SavedSkin;
import org.wallentines.skinsetter.api.SkinRegistry;
import org.wallentines.skinsetter.api.SkinSetterAPI;

import java.io.File;
import java.util.*;

public class SkinRegistryImpl implements SkinRegistry {

    private final File baseFolder;

    private final List<SavedSkin> skins = new ArrayList<>();
    private final Set<String> groups = new HashSet<>();
    private final HashMap<String, Integer> skinsById = new HashMap<>();
    private final HashMap<String, List<String>> skinNamesByFile = new HashMap<>();
    private final Set<String> modifiedFiles = new HashSet<>();

    public SkinRegistryImpl(File baseFolder) {
        this.baseFolder = baseFolder;

        loadFolder(baseFolder);
    }

    @Override
    public SavedSkin getSkin(String id) {
        Integer index = skinsById.get(id);
        if(index == null) return null;

        return skins.get(index);
    }

    @Override
    public Collection<SavedSkin> getAllSkins() {
        return skins;
    }

    @Override
    public Collection<SavedSkin> getSkins(String group) {

        if(group == null) return getAllSkins();

        List<SavedSkin> skinsWithGroup = new ArrayList<>();
        for(SavedSkin sk : getAllSkins()) {
            if(sk.getGroups().contains(group)) {
                skinsWithGroup.add(sk);
            }
        }

        return skinsWithGroup;
    }

    @Override
    public Collection<SavedSkin> getSkins(MPlayer user, String group) {

        List<SavedSkin> out = new ArrayList<>();

        Collection<SavedSkin> sks = getSkins(group);
        for(SavedSkin sk : sks) {
            if(sk.canUse(user)) out.add(sk);
        }

        return out;
    }

    @Override
    public Collection<String> getSkinNames() {
        return skinsById.keySet();
    }

    @Override
    public Collection<String> getGroupNames() {
        return groups;
    }

    @Override
    public Collection<String> getSkinNames(MPlayer user) {

        List<String> out = new ArrayList<>();
        for(SavedSkin sk : getAllSkins()) {
            if(sk.canUse(user)) out.add(sk.getId());
        }

        return out;
    }

    @Override
    public Collection<String> getGroupNames(MPlayer user) {

        Set<String> out = new HashSet<>();
        for(SavedSkin sk : getAllSkins()) {
            if(sk.canUse(user)) out.addAll(sk.getGroups());
        }

        return out;
    }

    @Override
    public SavedSkin getRandomSkin() {

        return getRandomSkin(getAllSkins());
    }

    @Override
    public SavedSkin getRandomSkin(MPlayer user, String group) {

        return getRandomSkin(getSkins(user, group));
    }

    @Override
    public void clear() {

        groups.clear();
        modifiedFiles.clear();
        skinsById.clear();
        skinNamesByFile.clear();

        skins.clear();
    }

    @Override
    public void reloadAll() {
        clear();
        loadFolder(baseFolder);
    }

    @Override
    public void save() {

        for(String s : modifiedFiles) {

            FileConfig conf = FileConfig.findOrCreate(s, baseFolder);

            List<SavedSkin> sks = new ArrayList<>();
            for(String name : skinNamesByFile.get(s)) {
                sks.add(getSkin(name));
            }

            conf.getRoot().set("skins", sks);
            conf.save();
        }

        modifiedFiles.clear();
    }

    @Override
    public void registerSkin(SavedSkin skin) {

        registerSkin(skin, "user");
    }

    @Override
    public void registerSkin(SavedSkin skin, String file) {

        if(skinsById.containsKey(skin.getId())) {
            throw new IllegalArgumentException("Attempt to register a skin with duplicate ID! (" + skin.getId() + ")");
        }

        skinsById.put(skin.getId(), skins.size());
        skins.add(skin);

        List<String> folderSkins = skinNamesByFile.computeIfAbsent(file, k -> new ArrayList<>());
        folderSkins.add(skin.getId());

        modifiedFiles.add(file);
        groups.addAll(skin.getGroups());

    }

    private static SavedSkin getRandomSkin(Collection<SavedSkin> skins) {

        final int index = Constants.RANDOM.nextInt(skins.size());
        int i = 0;

        for(SavedSkin sk : skins) {

            if(i == index) return sk;
            i++;
        }

        return null;
    }

    private void loadFolder(File baseFolder) {

        if(!baseFolder.isDirectory()) return;

        File[] files = baseFolder.listFiles();
        if(files == null) return;

        for(File f : files) {

            FileConfig conf = FileConfig.fromFile(f);

            List<String> groups;
            if (!conf.getRoot().has("groups", List.class)) {
                groups = null;
            } else {
                groups = conf.getRoot().getStringList("groups");
            }

            List<String> folderSkins = skinNamesByFile.computeIfAbsent(f.getName(), k -> new ArrayList<>());
            for(ConfigSection sec : conf.getRoot().getListFiltered("skins", ConfigSection.class)) {

                SavedSkin sk;
                try {
                    sk = SavedSkinImpl.SERIALIZER.deserialize(sec);
                } catch (Exception ex) {

                    SkinSetterAPI.getLogger().warn("An error occurred while parsing a skin!");
                    ex.printStackTrace();
                    continue;
                }
                if(groups != null) sk.getGroups().addAll(groups);

                folderSkins.add(sk.getId());
                if(skinsById.containsKey(sk.getId())) {
                    throw new IllegalArgumentException("Attempt to register a skin with duplicate ID! (" + sk.getId() + ")");
                }

                this.groups.addAll(sk.getGroups());
                skinsById.put(sk.getId(), skins.size());
                skins.add(sk);
            }

        }

    }

}

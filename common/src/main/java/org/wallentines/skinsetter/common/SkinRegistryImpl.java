package org.wallentines.skinsetter.common;

import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.skinsetter.api.EditableSkin;
import org.wallentines.skinsetter.api.SavedSkin;
import org.wallentines.skinsetter.api.SkinRegistry;
import org.wallentines.skinsetter.api.SkinSetterAPI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkinRegistryImpl implements SkinRegistry {

    private final File baseFolder;

    private final List<SavedSkin> skins = new ArrayList<>();
    private final Set<String> groups = new HashSet<>();
    private final HashMap<String, Integer> skinsById = new HashMap<>();
    private final HashMap<String, List<String>> skinNamesByFile = new HashMap<>();
    private final HashMap<String, String> fileNamesBySkin = new HashMap<>();
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
    public EditableSkin createEditableSkin(String id) {

        SavedSkin sk = getSkin(id);
        String file = fileNamesBySkin.get(id);

        return new EditableSkinImpl(sk, this, file);
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

        if (user == null) {
            return getSkins(group);
        } else {
            return getSkins(group).stream().filter(sk -> sk.canUse(user)).collect(Collectors.toList());
        }
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

            ConfigList sks = new ConfigList();
            for(String name : skinNamesByFile.get(s)) {
                sks.add((SavedSkinImpl) getSkin(name), SavedSkinImpl.SERIALIZER);
            }

            conf.getRoot().set("skins", sks);
            conf.save();
        }

        modifiedFiles.clear();
    }

    @Override
    public void registerSkin(SavedSkin skin) {

        registerSkin(skin, "user", true);
    }

    @Override
    public void registerSkin(SavedSkin skin, String file) {

        registerSkin(skin, file, true);
    }

    public void registerSkin(SavedSkin skin, String file, boolean save) {

        if(skinsById.containsKey(skin.getId())) {
            throw new IllegalArgumentException("Attempt to register a skin with duplicate ID! (" + skin.getId() + ")");
        }

        skinsById.put(skin.getId(), skins.size());
        skins.add(skin);

        fileNamesBySkin.put(skin.getId(), file);

        List<String> folderSkins = skinNamesByFile.computeIfAbsent(file, k -> new ArrayList<>());
        folderSkins.add(skin.getId());

        if(save) modifiedFiles.add(file);
        groups.addAll(skin.getGroups());

    }

    @Override
    public void deleteSkin(SavedSkin skin) {

        String id = skin.getId();

        if(!skinsById.containsKey(id)) {
            throw new IllegalArgumentException("Attempt to delete an unregistered skin! (" + skin.getId() + ")");
        }

        String file = fileNamesBySkin.get(id);

        int index = skinsById.remove(id);
        skins.remove(index);

        for(int i = index ; i < skins.size() ; i++) {
            skinsById.put(skins.get(i).getId(), i);
        }

        modifiedFiles.add(file);
        fileNamesBySkin.remove(id);

        skinNamesByFile.get(file).remove(id);

        HashSet<String> skinGroups = new HashSet<>(skin.getGroups());
        for(String s : skin.getGroups()) {
            if(!getSkins(s).isEmpty()) skinGroups.remove(s);
        }

        groups.removeAll(skinGroups);

    }

    @Override
    public InlineSerializer<SavedSkin> nameSerializer() {
        return InlineSerializer.of(SavedSkin::getId, this::getSkin);
    }

    void updateSkin(SavedSkin skin, String file) {

        String id = skin.getId();
        if(!skinsById.containsKey(id)) {
            throw new IllegalArgumentException("Attempt to update an unregistered skin! (" + skin.getId() + ") Use registerSkin() to register a new skin!");
        }

        SavedSkin old = getSkin(id);
        if(!old.getSkin().equals(skin.getSkin())) {
            throw new IllegalArgumentException("Updated skin with ID " + skin.getId() + " contains different texture than originally registered skin!");
        }

        String oldFile = fileNamesBySkin.get(id);
        if(!oldFile.equals(file)) {
            throw new IllegalArgumentException("Updated skin with ID " + skin.getId() + " contains different file name than originally registered skin!");
        }

        int index = skinsById.get(id);
        skins.set(index, skin);

        for(String s : old.getGroups()) {
            if(!skin.getGroups().contains(s) && getSkins(s).isEmpty()) groups.remove(s);
        }

        modifiedFiles.add(file);
    }

    private static SavedSkin getRandomSkin(Collection<SavedSkin> skins) {

        List<SavedSkin> filtered = skins.stream().filter(SavedSkin::inRandomSelection).collect(Collectors.toList());

        if(filtered.isEmpty()) {
            return null;
        }

        final int index = Constants.RANDOM.nextInt(filtered.size());
        return filtered.get(index);
    }

    private void loadFolder(File baseFolder) {

        if(!baseFolder.isDirectory()) return;

        try(Stream<Path> s = Files.list(baseFolder.toPath()).sorted()) {

            s.forEach(p -> {
                File f = p.toFile();
                FileConfig conf = new FileConfig(FileConfig.REGISTRY.forFile(f), f);
                conf.load();
                if(conf.getRoot() == null) return;

                List<String> groups;
                if (!conf.getRoot().hasList("groups")) {
                    groups = null;
                } else {
                    groups = conf.getRoot().getListFiltered("groups", Serializer.STRING);
                }

                for(ConfigSection sec : conf.getRoot().getListFiltered("skins", ConfigSection.SERIALIZER)) {

                    SavedSkin sk;
                    try {
                        sk = SavedSkinImpl.SERIALIZER.deserialize(ConfigContext.INSTANCE, sec).getOrThrow();
                    } catch (Exception ex) {

                        SkinSetterAPI.getLogger().warn("An error occurred while parsing a skin!");
                        ex.printStackTrace();
                        continue;
                    }

                    if(groups != null) sk.getGroups().addAll(groups);

                    String fileName = f.getName().contains(".") ? f.getName().substring(0, f.getName().lastIndexOf(".")) : f.getName();
                    registerSkin(sk, fileName, false);
                }
            });
        } catch (IOException ex) {

            SkinSetterAPI.getLogger().warn("An error occurred while listing files!");
        }
    }

}

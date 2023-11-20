package org.wallentines.skinsetter;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.PermissionHolder;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

public class SkinRegistry {

    private final File directory;
    private final StringRegistry<SkinFile> loadedFiles = new StringRegistry<>();
    private final HashMap<String, SkinFile> filesBySkin = new HashMap<>();

    public SkinRegistry(File directory) {
        this.directory = directory;
        if(!directory.isDirectory() && !directory.mkdirs()) {
            throw new IllegalStateException("Unable to create skin directory!");
        }
    }

    public void loadAll() {

        loadedFiles.clear();
        filesBySkin.clear();

        File[] files = directory.listFiles();

        if(!directory.isDirectory() || files == null) {
            return;
        }

        for(File f : files) {

            if(MidnightCoreAPI.FILE_CODEC_REGISTRY.forFile(f) == null) {
                continue;
            }

            String fileName = getFileName(f);
            if(loadedFiles.contains(fileName)) {
                SkinSetterAPI.LOGGER.warn("Found skin file with duplicate name \"" + fileName + "\"!");
                continue;
            }

            SkinFile sf = new SkinFile(f, MidnightCoreAPI.FILE_CODEC_REGISTRY);
            sf.load();

            loadedFiles.register(fileName, sf);

            for(String id : sf.getSkinIds()) {
                filesBySkin.put(id, sf);
            }
        }
    }

    public void registerSkin(String name, SavedSkin skin) {

        registerSkin(name, skin, null);
    }

    public void registerSkin(String name, SavedSkin skin, String file) {

        String fileName = file == null ? "user" : file;

        SkinFile sf = loadedFiles.get(fileName);
        if(sf == null) {
            sf = loadedFiles.register(fileName, new SkinFile(fileName, directory, MidnightCoreAPI.FILE_CODEC_REGISTRY));
        }

        filesBySkin.put(name, sf);
        sf.registerSkin(name, skin);
    }

    public int getSize() {
        return filesBySkin.size();
    }

    public Collection<String> getSkinIds() {
        return filesBySkin.keySet();
    }

    public Collection<String> getSkinIds(PermissionHolder permissionHolder, String group, ExcludeFlag exclude) {

        if(loadedFiles.getSize() == 0) return new ArrayList<>();

        List<String> out = new ArrayList<>();

        for(SkinFile file : loadedFiles) {
            for(int i = 0 ; i < file.getSize() ; i++) {
                SavedSkin sk = file.getSkin(i);
                if(matches(sk, permissionHolder, group, exclude)) {
                    out.add(file.getId(i));
                }
            }
        }
        return out;
    }

    public Set<String> getGroupNames(PermissionHolder permissionHolder, ExcludeFlag exclude) {
        Set<String> out = new HashSet<>();
        for(SavedSkin sk : getAllSkins(permissionHolder, null, exclude)) {
            out.addAll(sk.getGroups());
        }
        return out;
    }

    private boolean matches(SavedSkin sk, PermissionHolder permissionHolder, String group, ExcludeFlag exclude) {
        return (sk.canUse(permissionHolder))
                && !exclude.checker.test(sk)
                && (group == null || sk.getGroups().contains(group));
    }

    public List<SavedSkin> getSkins(int offset, int count) {
        return getSkins(offset, count, PermissionHolder.ALL, null, ExcludeFlag.NONE);
    }

    public List<SavedSkin> getSkins(int offset, int count, PermissionHolder permissionHolder, String group, ExcludeFlag exclude) {

        if(loadedFiles.getSize() == 0) return new ArrayList<>();

        int currentOffset = 0;
        int currentFile = 0;
        while(currentOffset < offset) {

            int matching = 0;
            SkinFile file = loadedFiles.valueAtIndex(currentFile);

            for(String id : file.getSkinIds()) {
                SavedSkin sk = file.getSkin(id);
                if(matches(sk, permissionHolder, group, exclude)) {
                    matching++;
                }
            }

            currentOffset += matching;
            if(currentOffset < offset) currentFile++;
        }

        int diff = currentOffset - offset;

        List<SavedSkin> out = new ArrayList<>();
        int skins = 0;

        while(skins < count && currentFile < loadedFiles.getSize()) {
            SkinFile file = loadedFiles.valueAtIndex(currentFile);
            for (int i = diff; i < file.getSize() && skins < count; i++) {
                SavedSkin sk = file.getSkin(i);
                if(matches(sk, permissionHolder, group, exclude)) {
                    out.add(sk);
                }
                skins++;
            }
            currentFile++;
            diff = 0;
        }

        return out;
    }

    public List<SavedSkin> getAllSkins() {
        return getSkins(0, getSize());
    }

    public List<SavedSkin> getAllSkins(PermissionHolder permissionHolder, String group, ExcludeFlag exclude) {
        return getSkins(0, getSize(), permissionHolder, group, exclude);
    }

    public SavedSkin getSkin(String id) {

        SkinFile file = filesBySkin.get(id);
        if(file == null) return null;

        return file.getSkin(id);
    }

    public SkinConfiguration getSavedConfiguration(String id) {

        SkinFile file = filesBySkin.get(id);
        if(file == null) return null;

        return file.getSavedConfiguration(id);
    }

    private static String getFileName(File file) {

        String name = file.getName();
        int index = name.indexOf('.');

        if(index == -1) {
            return name;
        }

        return name.substring(0, index);
    }

    public enum ExcludeFlag {
        NONE(sk -> false),
        IN_RANDOM(SavedSkin::isExcludedInRandom),
        IN_GUI(SavedSkin::isExcludedInGUI);

        final Predicate<SavedSkin> checker;

        ExcludeFlag(Predicate<SavedSkin> checker) {
            this.checker = checker;
        }
    }

}

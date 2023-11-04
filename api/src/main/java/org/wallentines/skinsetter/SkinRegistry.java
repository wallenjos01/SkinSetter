package org.wallentines.skinsetter;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
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

    public void saveAll() {

        for(SkinFile sf : loadedFiles) {
            sf.saveIfChanged();
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

    public List<SavedSkin> getSkins(int offset, int count) {
        return getSkins(offset, count, (perm, lvl) -> true, ExcludeFlag.NONE);
    }

    public List<SavedSkin> getSkins(int offset, int count, BiFunction<String, Integer, Boolean> permissionChecker, ExcludeFlag exclude) {

        if(loadedFiles.getSize() == 0) return new ArrayList<>();

        int currentOffset = 0;
        int currentFile = 0;
        while(currentOffset < offset) {

            int matching = 0;
            SkinFile file = loadedFiles.valueAtIndex(currentFile);

            for(String id : file.getSkinIds()) {
                SavedSkin sk = file.getSkin(id);
                if((sk.getPermission() == null || permissionChecker.apply(sk.getPermission(), 2)) && !exclude.checker.test(sk)) {
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
                if((sk.getPermission() == null || permissionChecker.apply(sk.getPermission(), 2)) && !exclude.checker.test(sk)) {
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

    public List<SavedSkin> getAllSkins(BiFunction<String, Integer, Boolean> permissionChecker, ExcludeFlag exclude) {
        return getSkins(0, getSize(), permissionChecker, exclude);
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

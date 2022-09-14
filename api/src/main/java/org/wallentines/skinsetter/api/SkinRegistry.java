package org.wallentines.skinsetter.api;

import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.Collection;

public interface SkinRegistry {

    SavedSkin getSkin(String id);

    EditableSkin createEditableSkin(String id);

    Collection<SavedSkin> getAllSkins();

    Collection<SavedSkin> getSkins(String group);

    Collection<SavedSkin> getSkins(MPlayer user, String group);

    Collection<String> getSkinNames();

    Collection<String> getGroupNames();

    Collection<String> getSkinNames(MPlayer user);

    Collection<String> getGroupNames(MPlayer user);

    SavedSkin getRandomSkin();

    SavedSkin getRandomSkin(MPlayer player, String group);

    void clear();

    void reloadAll();

    void save();

    void registerSkin(SavedSkin skin);

    void registerSkin(SavedSkin skin, String file);

    void deleteSkin(SavedSkin skin);

}

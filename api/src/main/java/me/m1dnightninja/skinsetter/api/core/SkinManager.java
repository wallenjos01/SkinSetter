package me.m1dnightninja.skinsetter.api.core;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.skinsetter.api.SavedSkin;

import java.util.List;

public interface SkinManager {

    void init();

    SavedSkin getSkin(String id);

    void saveSkin(SavedSkin s, String id);

    List<String> getSkinNames(MPlayer user, String group, boolean excludeNoRandom);

    default List<String> getAllSkinNames() {
        return getSkinNames(null, null, false);
    }

    List<SavedSkin> getSkins(MPlayer user, String group, boolean excludeNoRandom);

    default List<SavedSkin> getAllSkins() {
        return getSkins(null, null, false);
    }

    List<String> getGroupNames(MPlayer user, boolean excludeNoRandom);

    default List<String> getAllGroupNames(MPlayer user, boolean excludeNoRandom) {
        return getGroupNames(user, excludeNoRandom);
    }

    void loadSkins(ConfigSection sec);

    void saveSkins(ConfigSection sec);

}

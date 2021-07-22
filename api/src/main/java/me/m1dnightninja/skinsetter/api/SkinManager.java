package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

import java.util.List;

public interface SkinManager {

    void init();

    SavedSkin getSkin(String id);

    void saveSkin(SavedSkin s, String id);

    List<String> getSkinNames(MPlayer user);

    List<String> getRandomSkinNames(MPlayer user);

    List<SavedSkin> getSkins(MPlayer user);

    List<SavedSkin> getRandomSkins(MPlayer user);

    void loadSkins(ConfigSection sec);

    void saveSkins(ConfigSection sec);

}

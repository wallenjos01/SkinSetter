package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;

import java.util.List;

public interface SkinManager {

    void init();

    Skin getSkin(String id);

    void saveSkin(Skin s, String id);

    List<String> getSkinNames();

    void loadSkins(ConfigSection sec);

    void saveSkins(ConfigSection sec);

}

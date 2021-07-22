package me.m1dnightninja.skinsetter.common;

import com.google.gson.internal.LinkedTreeMap;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.skinsetter.api.SavedSkin;
import me.m1dnightninja.skinsetter.api.SkinManager;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.integration.HideAndSeekIntegration;

import java.util.ArrayList;
import java.util.List;

public class SkinManagerImpl implements SkinManager {

    private static final String SKIN_SECTION = "skins";

    protected final LinkedTreeMap<String, SavedSkin> skins = new LinkedTreeMap<>();
    private boolean hasIsPresent = false;

    public void init() {
        try {
            Class.forName("me.m1dnightninja.hideandseek.api.HideAndSeekAPI");
            hasIsPresent = true;
            SkinSetterAPI.getLogger().info("HideAndSeek found!");
        } catch(ClassNotFoundException ex) {
            // Ignore
        }
    }

    public final SavedSkin getSkin(String id) {
        if(skins.containsKey(id)) return skins.get(id);
        if(hasIsPresent) return HideAndSeekIntegration.getSkin(id);
        return null;
    }

    public final void saveSkin(SavedSkin s, String id) {
        skins.put(id, s);
    }

    private List<String> getSkinNames(MPlayer player, boolean random) {

        List<String> out = new ArrayList<>();
        for(SavedSkin sk : skins.values()) {

            if(player != null && !sk.canUse(player)) continue;
            if(random && sk.excludedFromRandom()) continue;

            out.add(sk.getId());

        }

        if(hasIsPresent) for(String skn : HideAndSeekIntegration.getSkinNames()) {
            if(player != null && !(player.hasPermission("hideandseek.skin." + skn) || player.hasPermission("skinsetter.group.hideandseek"))) {
                out.add(skn);
            }
        }

        return out;
    }

    private List<SavedSkin> getSkins(MPlayer player, boolean random) {

        List<SavedSkin> out = new ArrayList<>();
        for(SavedSkin sk : skins.values()) {

            if(player != null && !sk.canUse(player)) continue;
            if(random && sk.excludedFromRandom()) continue;

            out.add(sk);

        }

        if(hasIsPresent) for(SavedSkin skn : HideAndSeekIntegration.getSkins()) {
            if(skn.canUse(player)) out.add(skn);
        }

        return out;
    }

    public final List<String> getSkinNames(MPlayer player) {
        return getSkinNames(player, false);
    }

    public final List<String> getRandomSkinNames(MPlayer player) {
        return getSkinNames(player, true);
    }

    @Override
    public List<SavedSkin> getSkins(MPlayer user) {
        return getSkins(user, false);
    }

    @Override
    public List<SavedSkin> getRandomSkins(MPlayer user) {
        return getSkins(user, true);
    }


    public void loadSkins(ConfigSection sec) {

        if(sec.has(SKIN_SECTION, List.class)) {
            for(SavedSkin ss : sec.getListFiltered(SKIN_SECTION, SavedSkin.class)) {
                skins.put(ss.getId(), ss);
            }
        }
    }

    public void saveSkins(ConfigSection sec) {
        sec.set(SKIN_SECTION, new ArrayList<>(skins.values()));
    }

}

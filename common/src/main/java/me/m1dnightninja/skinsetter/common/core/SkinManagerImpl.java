package me.m1dnightninja.skinsetter.common.core;

import com.google.gson.internal.LinkedTreeMap;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.skinsetter.api.SavedSkin;
import me.m1dnightninja.skinsetter.api.core.SkinManager;
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

    @Override
    public List<String> getSkinNames(MPlayer user, String group, boolean excludeNoRandom) {

        List<String> out = new ArrayList<>();

        for(SavedSkin s : skins.values()) {
            if(user != null && !s.canUse(user)) continue;
            if(group != null && !s.getGroups().contains(group)) continue;
            if(excludeNoRandom && s.excludedFromRandom()) continue;

            out.add(s.getId());
        }

        return out;
    }

    @Override
    public List<SavedSkin> getSkins(MPlayer user, String group, boolean excludeNoRandom) {

        List<SavedSkin> out = new ArrayList<>();

        for(SavedSkin s : skins.values()) {
            if(user != null && !s.canUse(user)) continue;
            if(group != null && !s.getGroups().contains(group)) continue;
            if(excludeNoRandom && s.excludedFromRandom()) continue;

            out.add(s);
        }

        return out;
    }

    @Override
    public List<String> getGroupNames(MPlayer user, boolean excludeNoRandom) {

        List<String> out = new ArrayList<>();

        for(SavedSkin s : skins.values()) {

            if(user != null && !s.canUse(user)) continue;
            if(excludeNoRandom && s.excludedFromRandom()) continue;

            for(String group : s.getGroups()) {
                if(out.contains(group)) continue;

                out.add(group);
            }
        }

        return out;
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

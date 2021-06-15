package me.m1dnightninja.skinsetter.common;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.skinsetter.api.SkinManager;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import me.m1dnightninja.skinsetter.common.integration.HideAndSeekIntegration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkinManagerImpl implements SkinManager {

    protected final HashMap<String, Skin> skins = new HashMap<>();

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

    public final Skin getSkin(String id) {
        if(skins.containsKey(id)) return skins.get(id);
        if(hasIsPresent) return HideAndSeekIntegration.getSkin(id);
        return null;
    }

    public final void saveSkin(Skin s, String id) {
        skins.put(id, s);
    }

    public final List<String> getSkinNames() {

        List<String> s = new ArrayList<>(skins.keySet());
        if(hasIsPresent) s.addAll(HideAndSeekIntegration.getSkinNames());
        return s;
    }

    public void loadSkins(ConfigSection sec) {

        if(sec.has("skins", List.class)) {
            for(ConfigSection sct : sec.getListFiltered("skins", ConfigSection.class)) {
                skins.put(sct.getString("id"), Skin.SERIALIZER.deserialize(sct));
            }
        }
    }

    public void saveSkins(ConfigSection sec) {
        List<ConfigSection> list = new ArrayList<>();

        for(Map.Entry<String, Skin> ent : skins.entrySet()) {

            ConfigSection sct = Skin.SERIALIZER.serialize(ent.getValue());
            sct.set("id", ent.getKey());
            list.add(sct);

        }

        sec.set("skins", list);
    }

}

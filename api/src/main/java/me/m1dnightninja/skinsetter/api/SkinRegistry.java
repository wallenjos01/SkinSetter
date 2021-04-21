package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;

import java.io.File;
import java.util.*;

public class SkinRegistry {

    protected final HashMap<String, Skin> skins = new HashMap<>();

    private boolean hasIsPresent = false;

    public SkinRegistry() {
        try {
            Class.forName("me.m1dnightninja.hideandseek.api.HideAndSeekAPI");
            hasIsPresent = true;
            System.out.println("HideAndSeek found!");
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

    public void loadSkins() {
        loadSkins(SkinSetterAPI.getInstance().getSkinFile());
    }

    public void loadSkins(File f) {
        ConfigSection sec = MidnightCoreAPI.getInstance().getDefaultConfigProvider().loadFromFile(f);

        if(sec.has("skins", List.class)) {
            for(ConfigSection sct : sec.getList("skins", ConfigSection.class)) {
                skins.put(sct.getString("id"), Skin.SERIALIZER.deserialize(sct));
            }
        }
    }

    public void saveSkins() {
        saveSkins(SkinSetterAPI.getInstance().getSkinFile());
    }

    public void saveSkins(File f) {

        List<ConfigSection> sec = new ArrayList<>();

        for(Map.Entry<String, Skin> ent : skins.entrySet()) {
            ConfigSection sct = Skin.SERIALIZER.serialize(ent.getValue());
            sct.set("id", ent.getKey());
            sec.add(sct);
        }

        ConfigSection out = new ConfigSection();
        out.set("skins", sec);

        MidnightCoreAPI.getInstance().getDefaultConfigProvider().saveToFile(out, f);
    }

}

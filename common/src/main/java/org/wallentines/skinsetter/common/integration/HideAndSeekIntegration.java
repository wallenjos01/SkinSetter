package org.wallentines.skinsetter.common.integration;

import org.wallentines.hideandseek.api.event.ClassApplyEvent;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.skinsetter.api.SavedSkin;
import org.wallentines.skinsetter.api.SkinSetterAPI;
import org.wallentines.skinsetter.common.Constants;

import java.util.ArrayList;
import java.util.List;

public class HideAndSeekIntegration {


    public static void setup() {

        Event.register(ClassApplyEvent.class, HideAndSeekIntegration.class, event -> {

            SkinSetterAPI.getLogger().info("Received HideAndSeek class event. Changing skin of " + event.getPlayer().getUsername());

            ConfigSection ext = event.getPlayerClass().getExtraData();
            List<Skin> skins = new ArrayList<>();
            if(ext.has("skins", List.class)) {
                for(Object o : ext.getList("skins")) {

                    Skin s = obtainSkin(o);
                    if(s == null) continue;

                    skins.add(s);
                }
            }

            SkinSetterAPI.getLogger().info(skins.size() + " skins found");

            if(skins.isEmpty()) return;
            event.getPlayer().setSkin(skins.get(Constants.RANDOM.nextInt(skins.size())));
        });
    }

    private static Skin obtainSkin(Object o) {
        if(o instanceof String s) {
            SavedSkin sk = SkinSetterAPI.getInstance().getSkinRegistry().getSkin(s);
            return sk == null ? null : sk.getSkin();
        } else if(o instanceof ConfigSection conf) {
            return Skin.SERIALIZER.deserialize(conf);
        }
        return null;
    }

}

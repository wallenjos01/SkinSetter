package org.wallentines.skinsetter.common.integration;

import org.wallentines.hideandseek.api.event.ClassApplyEvent;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.skinsetter.api.SavedSkin;
import org.wallentines.skinsetter.api.SkinSetterAPI;
import org.wallentines.skinsetter.common.Constants;

import java.util.ArrayList;
import java.util.List;

public class HideAndSeekIntegration {


    public static void setup() {

        Event.register(ClassApplyEvent.class, HideAndSeekIntegration.class, event -> {

            ConfigSection ext = event.getPlayerClass().getExtraData();
            List<Skin> skins = new ArrayList<>();
            if(ext.hasList("skins")) {
                for(ConfigObject o : ext.getList("skins").values()) {

                    Skin s = obtainSkin(o);
                    if(s == null) continue;

                    skins.add(s);
                }
            }

            if(skins.isEmpty()) return;
            event.getPlayer().setSkin(skins.get(Constants.RANDOM.nextInt(skins.size())));
        });
    }

    private static Skin obtainSkin(ConfigObject o) {
        if(o.isString()) {
            SavedSkin sk = SkinSetterAPI.getInstance().getSkinRegistry().getSkin(o.asString());
            return sk == null ? null : sk.getSkin();
        } else if(o.isSection()) {
            ConfigSection conf = (ConfigSection) o;
            return Skin.SERIALIZER.deserialize(ConfigContext.INSTANCE, conf).getOrThrow();
        }
        return null;
    }

}

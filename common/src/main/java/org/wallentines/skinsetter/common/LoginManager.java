package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.data.DataModule;
import org.wallentines.midnightcore.api.module.data.DataProvider;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.skinsetter.api.SavedSkin;
import org.wallentines.skinsetter.api.SkinRegistry;
import org.wallentines.skinsetter.api.SkinSetterAPI;

public class LoginManager {

    public static void applyLoginSkin(MPlayer u, SkinRegistry registry) {

        DataProvider dataProvider = MidnightCoreAPI.getInstance().getModuleManager().getModule(DataModule.class).getGlobalProvider();

        if(SkinSetterAPI.getInstance().isPersistenceEnabled()) {

            ConfigSection data = dataProvider.getData(u);

            if (data != null && data.has(Constants.DEFAULT_NAMESPACE, ConfigSection.class)) {

                ConfigSection section = data.getSection(Constants.DEFAULT_NAMESPACE);
                if (section.has("skin", Skin.class)) {

                    Skin s = section.get("skin", Skin.class);
                    u.setSkin(s);
                    return;

                } else if (section.has("skin", String.class)) {

                    SavedSkin s = registry.getSkin(section.getString("skin"));
                    if (s == null) return;

                    u.setSkin(s.getSkin());
                    return;
                }
            }
        }

        if(SkinSetterAPI.getInstance().getDefaultSkin() != null) {
            u.setSkin(SkinSetterAPI.getInstance().getDefaultSkin().getSkin());
        }
    }

    public static void savePersistentSkin(MPlayer u, SavedSkin defaultSkin) {

        DataProvider dataProvider = MidnightCoreAPI.getInstance().getModuleManager().getModule(DataModule.class).getGlobalProvider();
        SkinModule skinModule = MidnightCoreAPI.getInstance().getModuleManager().getModule(SkinModule.class);

        if(SkinSetterAPI.getInstance().isPersistenceEnabled()) {

            Skin originalSkin = skinModule.getOriginalSkin(u);
            Skin currentSkin = skinModule.getSkin(u);

            if(currentSkin == originalSkin || currentSkin == defaultSkin.getSkin()) {

                dataProvider.getData(u).set(Constants.DEFAULT_NAMESPACE, null);

            } else {

                dataProvider.getData(u).getOrCreateSection(Constants.DEFAULT_NAMESPACE).set("skin", currentSkin);

            }
            dataProvider.saveData(u);
        }
    }

}

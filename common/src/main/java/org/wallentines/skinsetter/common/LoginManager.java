package org.wallentines.skinsetter.common;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.player.DataProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.skinsetter.api.SavedSkin;
import org.wallentines.skinsetter.api.SkinRegistry;
import org.wallentines.skinsetter.api.SkinSetterAPI;

public class LoginManager {

    public static void applyLoginSkin(MPlayer u, SkinRegistry registry) {

        DataProvider dataProvider = SkinSetterAPI.getInstance().getDataProvider();

        if(SkinSetterAPI.getInstance().isPersistenceEnabled()) {

            ConfigSection data = dataProvider.getData(u);

            if (data != null && data.hasSection(Constants.DEFAULT_NAMESPACE)) {

                ConfigSection section = data.getSection(Constants.DEFAULT_NAMESPACE);
                section.getOptional("skin", Skin.SERIALIZER).or(() -> section.getOptional("skin", registry.nameSerializer()).map(SavedSkin::getSkin)).ifPresent(u::setSkin);
            }
        }

        if(SkinSetterAPI.getInstance().getDefaultSkin() != null) {
            u.setSkin(SkinSetterAPI.getInstance().getDefaultSkin().getSkin());
        }
    }

    public static void savePersistentSkin(MPlayer u, SavedSkin defaultSkin) {

        DataProvider dataProvider = SkinSetterAPI.getInstance().getDataProvider();
        SkinModule skinModule = MidnightCoreAPI.getModule(SkinModule.class);

        if(skinModule == null) return;

        if(SkinSetterAPI.getInstance().isPersistenceEnabled()) {

            Skin originalSkin = skinModule.getOriginalSkin(u);
            Skin currentSkin = skinModule.getSkin(u);

            if(currentSkin == originalSkin || currentSkin == defaultSkin.getSkin()) {

                dataProvider.getData(u).remove(Constants.DEFAULT_NAMESPACE);

            } else {

                dataProvider.getData(u).getOrCreateSection(Constants.DEFAULT_NAMESPACE).set("skin", currentSkin, Skin.SERIALIZER);

            }
            dataProvider.saveData(u);
        }
    }

}

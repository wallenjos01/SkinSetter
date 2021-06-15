package me.m1dnightninja.skinsetter.common;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IPlayerDataModule;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.module.skin.SkinCallback;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.skinsetter.api.SkinManager;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class SkinUtil {

    private final ISkinModule skinModule;
    private final IPlayerDataModule dataModule;
    private final SkinManager reg;

    private final HashMap<MPlayer, Skin> skins = new HashMap<>();

    public SkinUtil() {
        skinModule = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        dataModule = MidnightCoreAPI.getInstance().getModule(IPlayerDataModule.class);

        if(skinModule == null) {
            SkinSetterAPI.getLogger().warn("Unable to load Skin Module!");
        }

        this.reg = SkinSetterAPI.getInstance().getSkinRegistry();
    }

    public final void setSkin(MPlayer player, Skin skin) {

        skins.put(player, skin);

        skinModule.setSkin(player, skin);
        skinModule.updateSkin(player);
    }

    public final void resetSkin(MPlayer player) {

        skins.remove(player);

        skinModule.resetSkin(player);
        skinModule.updateSkin(player);
    }

    public final Skin getSkin(MPlayer player) {

        if(!player.isOffline()) {
            return skinModule.getSkin(player);
        }

        return null;
    }

    public final void getSkinOnline(String playerName, SkinCallback cb) {
        new Thread(() -> {
            UUID u = MojangUtil.getUUID(playerName);
            cb.onSkinAvailable(u, skinModule.getOnlineSkin(u));
        }).start();
    }

    public final Skin getSavedSkin(String id) {
        return reg.getSkin(id);
    }

    public final List<String> getSkinNames() {

        return reg.getSkinNames();
    }

    public final void saveSkin(MPlayer player, String name) {

        Skin s = getSkin(player);

        if(s == null) return;

        reg.saveSkin(s, name);
    }

    public final void saveSkins() {

        reg.saveSkins(SkinSetterAPI.getInstance().getConfig());
        SkinSetterAPI.getInstance().saveConfig();
    }

    public final void reloadSkins() {
        reg.loadSkins(SkinSetterAPI.getInstance().getConfig());
    }

    public final void applyLoginSkin(MPlayer u) {

        if(SkinSetterAPI.getInstance().PERSISTENT_SKINS) {

            ConfigSection data = dataModule.getPlayerData(u.getUUID());

            if (data != null && data.has("skinsetter", ConfigSection.class)) {

                ConfigSection skinsetter = data.getSection("skinsetter");
                if (skinsetter.has("skin", Skin.class)) {

                    Skin s = skinsetter.get("skin", Skin.class);

                    setSkin(u, s);
                    return;

                } else if (skinsetter.has("skin", String.class)) {

                    Skin s = getSavedSkin(skinsetter.getString("skin"));
                    if (s == null) return;

                    setSkin(u, s);
                    return;
                }
            }
        }

        if(SkinSetterAPI.getInstance().DEFAULT_SKIN != null) {
            setSkin(u, SkinSetterAPI.getInstance().DEFAULT_SKIN);
        }
    }

    public final void savePersistentSkin(MPlayer u) {

        if(SkinSetterAPI.getInstance().PERSISTENT_SKINS) {

            if(!skins.containsKey(u) || skins.get(u).equals(SkinSetterAPI.getInstance().DEFAULT_SKIN)) {

                dataModule.getPlayerData(u.getUUID()).set("skinsetter", null);
                dataModule.savePlayerData(u.getUUID());

            } else {

                Skin s = skins.get(u);

                dataModule.getPlayerData(u.getUUID()).getOrCreateSection("skinsetter").set("skin", s);
                dataModule.savePlayerData(u.getUUID());

            }
        }

        skins.remove(u);
    }

}

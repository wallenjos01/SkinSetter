package me.m1dnightninja.skinsetter.common;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.module.ISkinModule;
import me.m1dnightninja.midnightcore.api.skin.Skin;
import me.m1dnightninja.midnightcore.api.skin.SkinCallback;
import me.m1dnightninja.midnightcore.common.MojangUtil;
import me.m1dnightninja.skinsetter.api.SkinRegistry;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;

import java.util.List;
import java.util.UUID;

public final class SkinUtil {

    private final ISkinModule mod;
    private final SkinRegistry reg;

    public SkinUtil() {
        mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);

        if(mod == null) {
            SkinSetterAPI.getLogger().warn("Unable to load Skin Module!");
        }

        this.reg = SkinSetterAPI.getInstance().getSkinRegistry();

        reloadSkins();
    }

    public final void setSkin(UUID player, Skin skin) {

        mod.setSkin(player, skin);
        mod.updateSkin(player);
    }

    public final void resetSkin(UUID player) {

        mod.resetSkin(player);
        mod.updateSkin(player);
    }

    public final Skin getSkin(UUID player) {

        if(SkinSetterAPI.getInstance().isOnline(player)) {
            return mod.getSkin(player);
        }

        return null;
    }

    public final void getSkinOnline(String playerName, SkinCallback cb) {
        new Thread(() -> {
            UUID u = MojangUtil.getUUID(playerName);
            cb.onSkinAvailable(u, mod.getOnlineSkin(u));
        }).start();
    }

    public final Skin getSavedSkin(String id) {
        return reg.getSkin(id);
    }

    public final List<String> getSkinNames() {

        return reg.getSkinNames();
    }

    public final void saveSkin(UUID player, String name) {

        Skin s = getSkin(player);

        if(s == null) return;

        reg.saveSkin(s, name);
    }

    public final void saveSkins() {
        reg.saveSkins(SkinSetterAPI.getInstance().getSkinFile());
    }

    public final void reloadSkins() {
        reg.loadSkins(SkinSetterAPI.getInstance().getSkinFile());
    }

}

package me.m1dnightninja.skinsetter.common;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.module.IPlayerDataModule;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.module.skin.SkinCallback;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.skinsetter.api.SavedSkin;
import me.m1dnightninja.skinsetter.api.SkinManager;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;

import java.util.*;

public final class SkinUtil {

    private final ISkinModule skinModule;
    private final IPlayerDataModule dataModule;
    private final SkinManager reg;

    private final Random random;

    private final HashMap<MPlayer, Skin> skins = new HashMap<>();

    public SkinUtil() {
        skinModule = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        dataModule = MidnightCoreAPI.getInstance().getModule(IPlayerDataModule.class);

        if(skinModule == null) {
            SkinSetterAPI.getLogger().warn("Unable to load Skin Module!");
        }

        this.random = new Random();
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

    public final Skin getLoginSkin(MPlayer player) {

        if(!player.isOffline()) {
            return skinModule.getOriginalSkin(player);
        }

        return null;
    }

    public final void getSkinOnline(String playerName, SkinCallback cb) {
        new Thread(() -> {
            UUID u = MojangUtil.getUUID(playerName);
            cb.onSkinAvailable(u, skinModule.getOnlineSkin(u));
        }).start();
    }

    public final SavedSkin getSavedSkin(String id) {
        return reg.getSkin(id);
    }

    public final List<String> getSkinNames(MPlayer player) {

        return reg.getSkinNames(player);
    }

    public final SavedSkin getRandomSkin(MPlayer player) {

        List<SavedSkin> sks = reg.getRandomSkins(player);
        if(sks.size() == 0) return null;

        return sks.get(random.nextInt(sks.size()));
    }

    public final void saveSkin(MPlayer player, String name) {

        if(player.isOffline()) return;

        Skin s = skinModule.getSkin(player);
        reg.saveSkin(new SavedSkin(name, s), name);
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

                    SavedSkin s = getSavedSkin(skinsetter.getString("skin"));
                    if (s == null) return;

                    setSkin(u, s.getSkin());
                    return;
                }
            }
        }

        if(SkinSetterAPI.getInstance().DEFAULT_SKIN != null) {
            setSkin(u, SkinSetterAPI.getInstance().DEFAULT_SKIN.getSkin());
        }
    }

    public final void savePersistentSkin(MPlayer u) {

        if(SkinSetterAPI.getInstance().PERSISTENT_SKINS) {

            if(!skins.containsKey(u) || skins.get(u).equals(SkinSetterAPI.getInstance().DEFAULT_SKIN.getSkin())) {

                dataModule.getPlayerData(u.getUUID()).set("skinsetter", null);

            } else {

                Skin s = skins.get(u);

                dataModule.getPlayerData(u.getUUID()).getOrCreateSection("skinsetter").set("skin", s);

            }
            dataModule.savePlayerData(u.getUUID());
        }

        skins.remove(u);
    }

    public final void openGUI(MPlayer player, MPlayer perms) {

        try {
            AbstractInventoryGUI gui = MidnightCoreAPI.getInstance().createInventoryGUI(SkinSetterAPI.getInstance().getLangProvider().getMessage("gui.set.title", player));

            List<SavedSkin> skins = reg.getSkins(perms);

            int pages = skins.size() < 55 ? 1 : (skins.size() / 45) + 1;

            MItemStack nextPage = MItemStack.Builder.of(MIdentifier.parseOrDefault("lime_stained_glass_pane")).withName(SkinSetterAPI.getInstance().getLangProvider().getMessage("gui.next_page", player)).build();
            MItemStack prevPage = MItemStack.Builder.of(MIdentifier.parseOrDefault("red_stained_glass_pane")).withName(SkinSetterAPI.getInstance().getLangProvider().getMessage("gui.prev_page", player)).build();

            AbstractInventoryGUI.ClickAction next = (type, user) -> gui.open(user, gui.getPlayerPage(user) + 1);
            AbstractInventoryGUI.ClickAction prev = (type, user) -> gui.open(user, gui.getPlayerPage(user) - 1);

            if (pages > 1) {
                for (int i = 0; i < pages; i++) {

                    int offset = i * 54;

                    if (i > 0) {
                        gui.setItem(prevPage, offset + 45, prev);
                    }
                    if (i + 1 < pages) {
                        gui.setItem(nextPage, offset + 53, next);
                    }
                }

                int index = 0;
                int page = 0;
                for (SavedSkin skin : skins) {

                    gui.setItem(skin.getItemStack(), (page * 54) + index, (type, user) -> {
                        setSkin(user, skin.getSkin());
                        gui.close(user);
                    });

                    index++;
                    if(index > 44) {
                        page++;
                        index = 0;
                    }
                }

            } else {

                int index = 0;
                for (SavedSkin skin : skins) {

                    gui.setItem(skin.getItemStack(), index, (type, user) -> {
                        setSkin(user, skin.getSkin());
                        gui.close(user);
                    });
                    index++;
                }
            }

            gui.open(player, 0);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

}

package me.m1dnightninja.skinsetter.common.util;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MInventoryGUI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataModule;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataProvider;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.module.skin.SkinCallback;
import me.m1dnightninja.midnightcore.api.module.skin.Skinnable;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.skinsetter.api.SavedSkin;
import me.m1dnightninja.skinsetter.api.core.SkinManager;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;

import java.util.*;

public final class SkinUtil {

    private final ISkinModule skinModule;
    private final IPlayerDataProvider dataProvider;
    private final SkinManager reg;

    private final Random random;

    private final HashMap<MPlayer, Skin> skins = new HashMap<>();

    public SkinUtil() {
        skinModule = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        dataProvider = MidnightCoreAPI.getInstance().getModule(IPlayerDataModule.class).getGlobalProvider();

        if(skinModule == null) {
            SkinSetterAPI.getLogger().warn("Unable to load Skin Module!");
        }

        this.random = new Random();
        this.reg = SkinSetterAPI.getInstance().getSkinManager();
    }

    public void setSkin(MPlayer player, Skin skin) {

        skins.put(player, skin);

        skinModule.setSkin(player, skin);
        skinModule.updateSkin(player);
    }

    public void setSkin(UUID u, Skinnable ent, Skin skin) {

        if(u.version() == 4) {
            setSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(u), skin);
            return;
        }

        ent.setSkin(skin);
    }

    public void resetSkin(MPlayer player) {

        skins.remove(player);

        skinModule.resetSkin(player);
        skinModule.updateSkin(player);
    }

    public void resetSkin(UUID u, Skinnable ent) {

        if(u.version() == 4) {
            resetSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(u));
            return;
        }

        ent.resetSkin();
    }

    public Skin getSkin(MPlayer player) {

        if(!player.isOffline()) {
            return skinModule.getSkin(player);
        }

        return null;
    }

    public Skin getSkin(Skinnable ent) {

        return ent.getSkin();
    }

    public Skin getLoginSkin(MPlayer player) {

        if(!player.isOffline()) {
            return skinModule.getOriginalSkin(player);
        }

        return null;
    }

    public void getSkinOnline(String playerName, SkinCallback cb) {
        new Thread(() -> {
            UUID u = MojangUtil.getUUID(playerName);
            cb.onSkinAvailable(u, skinModule.getOnlineSkin(u));
        }).start();
    }

    public SavedSkin getSavedSkin(String id) {
        return reg.getSkin(id);
    }

    public List<String> getSkinNames(MPlayer player) {

        return reg.getSkinNames(player, null, false);
    }

    public SavedSkin getRandomSkin(MPlayer player, String group) {

        List<SavedSkin> sks = reg.getSkins(player, group, true);
        if(sks.size() == 0) return null;

        return sks.get(random.nextInt(sks.size()));
    }

    public List<String> getGroupNames(MPlayer player, boolean random) {

        return reg.getGroupNames(player, random);
    }

    public void saveSkin(MPlayer player, String name) {

        if(player.isOffline()) return;

        Skin s = skinModule.getSkin(player);
        reg.saveSkin(new SavedSkin(name, s), name);
    }

    public void saveSkin(Skinnable ent, String name) {

        Skin s = ent.getSkin();
        reg.saveSkin(new SavedSkin(name, s), name);
    }

    public void saveSkins() {

        reg.saveSkins(SkinSetterAPI.getInstance().getConfig());
        SkinSetterAPI.getInstance().saveConfig();
    }

    public void reloadSkins() {
        reg.loadSkins(SkinSetterAPI.getInstance().getConfig());
    }

    public void applyLoginSkin(MPlayer u) {

        if(SkinSetterAPI.getInstance().isPersistenceEnabled()) {

            ConfigSection data = dataProvider.getPlayerData(u.getUUID());

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

        if(SkinSetterAPI.getInstance().getDefaultSkin() != null) {
            setSkin(u, SkinSetterAPI.getInstance().getDefaultSkin().getSkin());
        }
    }

    public void savePersistentSkin(MPlayer u) {

        if(SkinSetterAPI.getInstance().isPersistenceEnabled()) {

            if(!skins.containsKey(u) || (SkinSetterAPI.getInstance().getDefaultSkin() != null && skins.get(u).equals(SkinSetterAPI.getInstance().getDefaultSkin().getSkin()))) {

                dataProvider.getPlayerData(u.getUUID()).set("skinsetter", null);

            } else {

                Skin s = skins.get(u);

                dataProvider.getPlayerData(u.getUUID()).getOrCreateSection("skinsetter").set("skin", s);

            }
            dataProvider.savePlayerData(u.getUUID());
        }

        skins.remove(u);
    }

    public void openGUI(MPlayer player, MPlayer perms) {

        MInventoryGUI gui = MidnightCoreAPI.getInstance().createInventoryGUI(SkinSetterAPI.getInstance().getLangProvider().getMessage("gui.set.title", player));

        List<SavedSkin> skins = reg.getSkins(perms, null, false);

        if(skins.size() == 0) {

            player.sendMessage(SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.no_saved", player));
            return;
        }

        int pages = 1;
        if(skins.size() > 55) {
            pages = skins.size() / 45;
            if(skins.size() % 45 != 0) pages += 1;
        }

        MItemStack nextPage = MItemStack.Builder.of(MIdentifier.parseOrDefault("lime_stained_glass_pane")).withName(SkinSetterAPI.getInstance().getLangProvider().getMessage("gui.next_page", player)).build();
        MItemStack prevPage = MItemStack.Builder.of(MIdentifier.parseOrDefault("red_stained_glass_pane")).withName(SkinSetterAPI.getInstance().getLangProvider().getMessage("gui.prev_page", player)).build();

        MInventoryGUI.ClickAction next = (type, user) -> gui.open(user, gui.getPlayerPage(user) + 1);
        MInventoryGUI.ClickAction prev = (type, user) -> gui.open(user, gui.getPlayerPage(user) - 1);

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
    }

}

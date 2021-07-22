package me.m1dnightninja.skinsetter.common.integration;

import me.m1dnightninja.hideandseek.api.HideAndSeekAPI;
import me.m1dnightninja.hideandseek.api.game.SavedSkin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.ArrayList;
import java.util.List;

public class HideAndSeekIntegration {

    public static me.m1dnightninja.skinsetter.api.SavedSkin getSkin(String id) {

        me.m1dnightninja.hideandseek.api.game.SavedSkin opt = HideAndSeekAPI.getInstance().getRegistry().getSkin(id);
        if(opt == null) return null;

        me.m1dnightninja.skinsetter.api.SavedSkin out = new me.m1dnightninja.skinsetter.api.SavedSkin(opt.getId(), opt.getSkin()) {
            @Override
            public boolean canUse(MPlayer player) {
                return player.hasPermission("hideandseek.skin." + id) || player.hasPermission("skinsetter.group.hideandseek");
            }
        };
        out.setName(MComponent.createTextComponent(opt.getDisplayName()));
        out.getGroups().add("hideandseek");

        return out;
    }

    public static List<me.m1dnightninja.skinsetter.api.SavedSkin> getSkins() {

        List<me.m1dnightninja.skinsetter.api.SavedSkin> out = new ArrayList<>();
        for(SavedSkin opt : HideAndSeekAPI.getInstance().getRegistry().getSkins()) {
            out.add(getSkin(opt.getId()));
        }
        return out;

    }

    public static List<String> getSkinNames() {

        List<String> out = new ArrayList<>();
        for(SavedSkin opt : HideAndSeekAPI.getInstance().getRegistry().getSkins()) {
            out.add(opt.getId());
        }
        return out;

    }

}

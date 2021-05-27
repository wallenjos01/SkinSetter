package me.m1dnightninja.skinsetter.common.integration;

import me.m1dnightninja.hideandseek.api.HideAndSeekAPI;
import me.m1dnightninja.hideandseek.api.game.SavedSkin;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;

import java.util.ArrayList;
import java.util.List;

public class HideAndSeekIntegration {

    public static Skin getSkin(String id) {

        SavedSkin opt = HideAndSeekAPI.getInstance().getRegistry().getSkin(id);
        return opt == null ? null : opt.getSkin();
    }

    public static List<String> getSkinNames() {

        List<String> out = new ArrayList<>();
        for(SavedSkin opt : HideAndSeekAPI.getInstance().getRegistry().getSkins()) {
            out.add(opt.getId());
        }
        return out;

    }

}

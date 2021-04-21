package me.m1dnightninja.skinsetter.api;

import me.m1dnightninja.hideandseek.api.HideAndSeekAPI;
import me.m1dnightninja.hideandseek.api.game.SkinOption;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;

import java.util.ArrayList;
import java.util.List;

public class HideAndSeekIntegration {

    static Skin getSkin(String id) {

        SkinOption opt = HideAndSeekAPI.getInstance().getRegistry().getSkin(id);
        return opt == null ? null : opt.getSkin();
    }

    static List<String> getSkinNames() {

        List<String> out = new ArrayList<>();
        for(SkinOption opt : HideAndSeekAPI.getInstance().getRegistry().getSkins()) {
            out.add(opt.getId());
        }
        return out;

    }

}

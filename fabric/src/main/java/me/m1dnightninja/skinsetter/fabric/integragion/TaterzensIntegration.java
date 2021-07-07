package me.m1dnightninja.skinsetter.fabric.integragion;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.UUID;

public class TaterzensIntegration {

    public static MComponent setNPCSkin(ServerPlayer player, Skin skin) {

        ITaterzenEditor editor = (ITaterzenEditor) player;
        UUID u = player.getUUID();

        MPlayer pl = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(u);

        TaterzenNPC npc = editor.getNpc();
        if(npc == null) return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.no_npc", pl);

        if(npc.getGameProfile() == null) return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.invalid_npc", pl);

        CompoundTag tag = new CompoundTag();
        tag.putString("value", skin.getBase64());
        tag.putString("signature", skin.getSignature());

        npc.setSkinFromTag(tag);
        npc.sendProfileUpdates();

        return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.setnpc.result", pl, new CustomPlaceholderInline("npc_name", npc.getGameProfile().getName()));

    }

}

package me.m1dnightninja.skinsetter.fabric.integragion;

import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.UUID;

public class TaterzensIntegration {

    public static MComponent setNPCSkin(ServerPlayer player, Skin skin) {

        TaterzenEditor editor = (TaterzenEditor) player;
        UUID u = player.getUUID();

        TaterzenNPC npc = editor.getNpc();
        if(npc == null) return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.no_npc", u);

        if(npc.getGameProfile() == null) return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.invalid_npc", u);

        CompoundTag tag = new CompoundTag();
        tag.putString("value", skin.getBase64());
        tag.putString("signature", skin.getSignature());

        npc.setSkinFromTag(tag);
        npc.sendProfileUpdates();

        return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.setnpc.result", u, npc.getGameProfile().getName());

    }

}

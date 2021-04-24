package me.m1dnightninja.skinsetter.spigot.integration;

import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.skinsetter.api.SkinSetterAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CitizensIntegration {

    public static MComponent setNPCSkin(Player player, Skin skin) {

        UUID u = player.getUniqueId();

        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);

        if(npc == null) return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.no_npc", u);

        SkinTrait trait = npc.getTraitNullable(SkinTrait.class);
        if(trait == null) return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.error.invalid_npc", u);

        trait.setSkinPersistent(skin.getUUID().toString(), skin.getSignature(), skin.getBase64());

        return SkinSetterAPI.getInstance().getLangProvider().getMessage("command.setnpc.result", u, npc.getName());

    }

}

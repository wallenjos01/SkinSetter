package me.m1dnightninja.skinsetter.fabric.mixin;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.module.skin.Skinnable;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer implements Skinnable {

    @Override
    public void setSkin(Skin skin) {

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        mod.setSkin(FabricPlayer.wrap((ServerPlayer) (Object) this), skin);
        mod.updateSkin(FabricPlayer.wrap((ServerPlayer) (Object) this));
    }

    @Override
    public void resetSkin() {

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        mod.resetSkin(FabricPlayer.wrap((ServerPlayer) (Object) this));
        mod.updateSkin(FabricPlayer.wrap((ServerPlayer) (Object) this));

    }

    @Override
    public Skin getSkin() {

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        return mod.getSkin(FabricPlayer.wrap((ServerPlayer) (Object) this));
    }
}

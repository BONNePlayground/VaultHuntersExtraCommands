//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.data.VirtualWorlds;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;


@Mixin(value = VirtualWorlds.class, remap = false)
public class MixinVirtualWorlds
{
    @Inject(method = "deregister", at = @At("HEAD"))
    private static void removeFromSave(VirtualWorld world, CallbackInfoReturnable<VirtualWorld> cir)
    {
        ExtraCommandsData data = ExtraCommandsData.get(world);

        if (data != null)
        {
            if (data.paused.remove(world.dimension().location()))
            {
                data.setDirty();
            }
        }
    }
}

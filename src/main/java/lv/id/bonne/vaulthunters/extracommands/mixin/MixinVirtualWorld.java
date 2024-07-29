//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;


@Mixin(value = VirtualWorld.class)
public class MixinVirtualWorld
{
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void removeFromSave(BooleanSupplier hasTimeLeft, CallbackInfo ci)
    {
        VirtualWorld world = ((VirtualWorld) (Object)this);

        ServerVaults.get(world).ifPresent(vault -> {
            vault.ifPresent(Vault.CLOCK, tickClock -> {
                if (tickClock.has(TickClock.PAUSED))
                {
                    ExtraCommandsData extraCommandsData = ExtraCommandsData.get(world);

                    if (extraCommandsData != null &&
                        extraCommandsData.paused.getOrDefault(world.dimension().location(), false))
                    {
                        ci.cancel();
                    }
                }
            });
        });
    }
}

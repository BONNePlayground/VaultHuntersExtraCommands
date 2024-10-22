//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsWorldData;


@Mixin(value = VirtualWorld.class)
public abstract class MixinVirtualWorld
{
    @Shadow
    public abstract boolean isMarkedForDeletion();


    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickServerPause(BooleanSupplier hasTimeLeft, CallbackInfo ci)
    {
        if (this.isMarkedForDeletion())
        {
            // Maybe processing clock while world is deleting causes ome issues?
            // It would be weird, but that is my only remaining code that could affect
            // getting into loop
            return;
        }

        VirtualWorld world = ((VirtualWorld) (Object)this);

        ServerVaults.get(world).ifPresent(vault -> {
            vault.ifPresent(Vault.CLOCK, tickClock -> {
                if (tickClock.has(TickClock.PAUSED))
                {
                    if (!world.players().isEmpty() && ExtraCommandsWorldData.get(world).isTickStop())
                    {
                        ci.cancel();
                    }
                }
            });
        });
    }
}

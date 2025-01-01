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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsWorldData;
import net.minecraftforge.server.ServerLifecycleHooks;


@Mixin(value = VirtualWorld.class)
public abstract class MixinVirtualWorld
{
    @Shadow(remap = false)
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

        VirtualWorld world = ((VirtualWorld) (Object) this);

        // Get thread save way.
        CompletableFuture<ExtraCommandsWorldData> submitFuture =
            ServerLifecycleHooks.getCurrentServer().submit(() -> ExtraCommandsWorldData.get(world));

        try
        {
            ExtraCommandsWorldData data = submitFuture.get(60, TimeUnit.MILLISECONDS);

            if (!data.isTickStop())
            {
                // Not tickstop so do not even check vaults?
                return;
            }

            // Load worlds into cache.
            ServerLifecycleHooks.getCurrentServer().execute(() -> ServerVaults.get(world));

            ServerVaults.get(world).ifPresent(vault ->
            {
                if (vault.has(Vault.FINISHED))
                {
                    return;
                }

                vault.ifPresent(Vault.CLOCK, tickClock ->
                {
                    if (tickClock.has(TickClock.PAUSED))
                    {
                        if (!world.players().isEmpty() && data.isTickStop())
                        {
                            ci.cancel();
                        }
                    }
                });
            });
        }
        catch (ExecutionException | InterruptedException | TimeoutException ex)
        {
            ExtraCommands.LOGGER.error("Could not safely get tick-stop property.");
        }
    }
}

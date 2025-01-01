//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import com.refinedmods.refinedstorage.util.TimeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;


@Mixin(value = Listener.class, remap = false)
public class MixinListener
{
    @Inject(method = "lambda$tickServer$0", at = @At("RETURN"), cancellable = true)
    private static void replaceValue(ServerPlayer player,
        VirtualWorld world,
        BlockPos start,
        CallbackInfoReturnable<Boolean> cir)
    {
        CompletableFuture<ExtraCommandsWorldData> submitFuture =
            ServerLifecycleHooks.getCurrentServer().submit(() -> ExtraCommandsWorldData.get(world));

        try
        {
            if (submitFuture.get(60, TimeUnit.MILLISECONDS).isPaused())
            {
                cir.setReturnValue(false);
            }
        }
        catch (ExecutionException | InterruptedException | TimeoutException e)
        {
            ExtraCommands.LOGGER.error("Could not safely get pause property.");
        }
    }

}

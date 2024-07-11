//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;


@Mixin(value = Listener.class, remap = false)
public class MixinListener
{
    @Inject(method = "lambda$tickServer$0", at = @At("RETURN"), cancellable = true)
    private static void replaceValue(ServerPlayer player,
        VirtualWorld world,
        BlockPos start,
        CallbackInfoReturnable<Boolean> cir)
    {
        ExtraCommandsData extraCommandsData = ExtraCommandsData.get(world);

        ResourceKey<Level> dimension = world.dimension();

        if (extraCommandsData != null &&
            extraCommandsData.paused.getOrDefault(dimension.location(), false))
        {
            cir.setReturnValue(false);
        }
    }

}

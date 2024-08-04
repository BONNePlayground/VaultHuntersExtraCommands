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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.objective.SpeedrunCrystalObjective;
import net.minecraft.nbt.CompoundTag;


@Mixin(value = SpeedrunCrystalObjective.class, remap = false)
public class MixinSpeedrunCrystalObjective
{
    @Shadow
    protected IntRoll target;

    @Inject(method = "readNbt(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    public void readNbt(CompoundTag nbt, CallbackInfo ci)
    {
        this.target = Adapters.INT_ROLL.readNbt(nbt.getCompound("target")).orElse(null);
    }


    @Inject(method = "writeNbt", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void readNbt(CallbackInfoReturnable<Optional<CompoundTag>> cir, CompoundTag nbt)
    {
        Adapters.INT_ROLL.writeNbt(this.target).ifPresent((target) -> nbt.put("target", target));
    }
}

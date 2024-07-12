//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import iskallia.vault.altar.AltarInfusionRecipe;
import iskallia.vault.block.entity.VaultAltarTileEntity;


@Mixin(value = VaultAltarTileEntity.class, remap = false)
public interface VaultAltarTileEntityAccessor
{
    @Invoker("updateDisplayedIndex")
    void callUpdateDisplayedIndex(AltarInfusionRecipe infusionRecipe);
}

//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

import iskallia.vault.config.gear.VaultGearTierConfig;


@Mixin(VaultGearTierConfig.class)
public interface VaultGearTierConfigAccessor
{
    @Accessor
    Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> getModifierGroup();
}

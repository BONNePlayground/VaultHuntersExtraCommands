//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

import iskallia.vault.config.gear.VaultGearTierConfig;


@Mixin(value = VaultGearTierConfig.class, remap = false)
public interface VaultGearTierConfigAccessor
{
    @Accessor("modifierGroup")
    Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> getModifierGroup();
}

//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

import iskallia.vault.task.GodAltarTask;


@Mixin(value = GodAltarTask.class, remap = false)
public interface GodAltarTaskAccessor
{
    @Accessor("vaultUuid")
    UUID getVaultUuid();
}

//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

import iskallia.vault.world.data.GodAltarData;


@Mixin(value = GodAltarData.class, remap = false)
public interface GodAltarDataAccessor
{
    @Accessor("entries")
    Map<UUID, GodAltarData.Entry> getEntries();
}

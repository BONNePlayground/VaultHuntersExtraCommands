//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

import iskallia.vault.world.data.EternalsData;


@Mixin(value = EternalsData.class, remap = false)
public interface EternalsDataAccessor
{
    @Accessor("playerMap")
    Map<UUID, EternalsData.EternalGroup> getPlayerMap();
}

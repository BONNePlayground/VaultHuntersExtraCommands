//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

import iskallia.vault.nbt.VMapNBT;
import iskallia.vault.world.data.PlayerGreedData;


@Mixin(value = PlayerGreedData.class, remap = false)
public interface PlayerGreedDataAccessor
{
    @Accessor("data")
    VMapNBT<UUID, PlayerGreedData.ArtifactData> getData();
}

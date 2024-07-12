//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

import iskallia.vault.nbt.VMapNBT;
import iskallia.vault.world.data.PlayerReputationData;


@Mixin(value = PlayerReputationData.class, remap = false)
public interface PlayerReputationDataAccessor
{
    @Accessor("entries")
    VMapNBT<UUID, Object> getEntries();
}

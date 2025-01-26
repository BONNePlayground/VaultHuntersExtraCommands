//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Objects;
import java.util.UUID;

import iskallia.vault.nbt.VMapNBT;
import iskallia.vault.world.data.VaultPlayerStats;


@Mixin(value = VaultPlayerStats.class, remap = false)
public interface VaultPlayerStatsAccessor
{
    @Accessor("pending")
    VMapNBT<UUID, Objects> getPending();
}

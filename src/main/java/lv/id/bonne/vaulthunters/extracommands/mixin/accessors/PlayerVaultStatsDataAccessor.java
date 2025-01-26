//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.world.data.PlayerVaultStatsData;


@Mixin(value = PlayerVaultStatsData.class, remap = false)
public interface PlayerVaultStatsDataAccessor
{
    @Accessor("playerMap")
    Map<UUID, PlayerVaultStats> getPlayerMap();
}

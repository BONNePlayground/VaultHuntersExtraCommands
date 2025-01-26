//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

import iskallia.vault.discoverylogic.DiscoveryGoalsState;
import iskallia.vault.world.data.DiscoveryGoalStatesData;


@Mixin(value = DiscoveryGoalStatesData.class, remap = false)
public interface DiscoveryGoalStatesDataAccessor
{
    @Accessor("playerMap")
    Map<UUID, DiscoveryGoalsState> getPlayerMap();
}

//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;


@Mixin(value = PlayerAbilitiesData.class, remap = false)
public interface PlayerAbilitiesDataAccessor
{
    @Accessor("playerMap")
    Map<UUID, AbilityTree> getPlayerMap();


    @Accessor("scheduledMerge")
    Set<UUID> getScheduledMerge();


    @Accessor("scheduledRefund")
    Set<UUID> getScheduledRefund();


    @Accessor("scheduledCorruptionCheck")
    Set<UUID> getScheduledCorruptionCheck();
}

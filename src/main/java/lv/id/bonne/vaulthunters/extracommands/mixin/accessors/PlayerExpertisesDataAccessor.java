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

import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;


@Mixin(value = PlayerExpertisesData.class, remap = false)
public interface PlayerExpertisesDataAccessor
{
    @Accessor("playerMap")
    Map<UUID, ExpertiseTree> getPlayerMap();


    @Accessor("scheduledMerge")
    Set<UUID> getScheduledMerge();
}

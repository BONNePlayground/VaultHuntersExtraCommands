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

import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.world.data.PlayerTalentsData;


@Mixin(value = PlayerTalentsData.class, remap = false)
public interface PlayerTalentsDataAccessor
{
    @Accessor("playerMap")
    Map<UUID, TalentTree> getPlayerMap();


    @Accessor("scheduledMerge")
    Set<UUID> getScheduledMerge();
}

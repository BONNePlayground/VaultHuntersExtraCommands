//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

import iskallia.vault.research.ResearchTree;
import iskallia.vault.world.data.PlayerResearchesData;


@Mixin(value = PlayerResearchesData.class,remap = false)
public interface PlayerResearchesDataAccessor
{
    @Accessor("playerMap")
    Map<UUID, ResearchTree> getPlayerMap();
}
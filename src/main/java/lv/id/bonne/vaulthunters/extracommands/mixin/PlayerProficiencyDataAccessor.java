//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.world.data.PlayerProficiencyData;


@Mixin(value = PlayerProficiencyData.class, remap = false)
public interface PlayerProficiencyDataAccessor
{
    @Accessor("playerProficiencies")
    Map<UUID, Map<ProficiencyType, Integer>> getPlayerProficiencies();
}
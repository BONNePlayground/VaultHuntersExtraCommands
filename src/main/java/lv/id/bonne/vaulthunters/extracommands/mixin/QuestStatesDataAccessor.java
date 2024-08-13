//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

import iskallia.vault.quest.QuestState;
import iskallia.vault.world.data.QuestStatesData;


@Mixin(value = QuestStatesData.class, remap = false)
public interface QuestStatesDataAccessor
{
    @Accessor("STATES")
    Map<UUID, QuestState> getSTATES();
}

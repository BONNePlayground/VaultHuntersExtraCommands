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

import iskallia.vault.world.data.DiscoveredAlchemyEffectsData;


@Mixin(value = DiscoveredAlchemyEffectsData.class, remap = false)
public interface DiscoveredAlchemyEffectsDataAccessor
{
    @Accessor("discoveredEffects")
    Map<UUID, Set<String>> getDiscoveredEffects();
}

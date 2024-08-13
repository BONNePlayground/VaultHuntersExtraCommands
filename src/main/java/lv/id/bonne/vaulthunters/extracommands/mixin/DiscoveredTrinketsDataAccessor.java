//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import iskallia.vault.world.data.DiscoveredTrinketsData;
import net.minecraft.resources.ResourceLocation;


@Mixin(value = DiscoveredTrinketsData.class, remap = false)
public interface DiscoveredTrinketsDataAccessor
{
    @Accessor("collectedTrinkets")
    Map<UUID, Set<ResourceLocation>> getCollectedTrinkets();
}

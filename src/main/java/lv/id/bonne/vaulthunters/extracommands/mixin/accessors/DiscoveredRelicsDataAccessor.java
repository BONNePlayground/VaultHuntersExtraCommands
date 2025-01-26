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

import iskallia.vault.world.data.DiscoveredRelicsData;
import net.minecraft.resources.ResourceLocation;


@Mixin(value = DiscoveredRelicsData.class, remap = false)
public interface DiscoveredRelicsDataAccessor
{
    @Accessor("discoveredRelics")
    Map<UUID, Set<ResourceLocation>> getDiscoveredRelics();
}

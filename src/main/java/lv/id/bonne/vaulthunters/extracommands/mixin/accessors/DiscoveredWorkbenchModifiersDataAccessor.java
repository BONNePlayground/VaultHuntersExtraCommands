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

import iskallia.vault.world.data.DiscoveredWorkbenchModifiersData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;


@Mixin(value = DiscoveredWorkbenchModifiersData.class, remap = false)
public interface DiscoveredWorkbenchModifiersDataAccessor
{
    @Accessor("discoveredCrafts")
    Map<UUID, Map<Item, Set<ResourceLocation>>> getDiscoveredCrafts();
}

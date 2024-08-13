//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;
import java.util.UUID;

import iskallia.vault.bounty.BountyList;
import iskallia.vault.world.data.BountyData;


@Mixin(value = BountyData.class, remap = false)
public interface BountyDataAccessor
{
    @Accessor("active")
    HashMap<UUID, BountyList> getActive();


    @Accessor("available")
    HashMap<UUID, BountyList> getAvailable();


    @Accessor("complete")
    HashMap<UUID, BountyList> getComplete();


    @Accessor("legendary")
    HashMap<UUID, BountyList> getLegendary();
}

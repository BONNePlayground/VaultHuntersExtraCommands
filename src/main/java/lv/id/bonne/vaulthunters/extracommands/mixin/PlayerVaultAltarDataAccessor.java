//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import iskallia.vault.world.data.PlayerVaultAltarData;
import net.minecraft.core.BlockPos;


@Mixin(value = PlayerVaultAltarData.class, remap = false)
public interface PlayerVaultAltarDataAccessor
{
    @Accessor("playerAltars")
    HashMap<UUID, List<BlockPos>> getPlayerAltars();
}

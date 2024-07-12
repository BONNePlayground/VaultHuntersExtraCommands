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

import iskallia.vault.world.data.PlayerSpiritRecoveryData;


@Mixin(value = PlayerSpiritRecoveryData.class, remap = false)
public interface PlayerSpiritRecoveryDataAccessor
{
    @Accessor("vaultSpiritData")
    Map<UUID, Set<PlayerSpiritRecoveryData.SpiritData>> getVaultSpiritData();


    @Accessor("spiritRecoveryMultipliers")
    Map<UUID, Float> getSpiritRecoveryMultipliers();


    @Accessor("heroDiscounts")
    Map<UUID, Float> getHeroDiscounts();


    @Accessor("playerSpiritRecoveries")
    Map<UUID, Integer> getPlayerSpiritRecoveries();
}

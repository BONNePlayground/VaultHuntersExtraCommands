//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.PlayerDataStorage;


@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor
{
    @Accessor("playerDataStorage")
    PlayerDataStorage getPlayerDataStorage();
}

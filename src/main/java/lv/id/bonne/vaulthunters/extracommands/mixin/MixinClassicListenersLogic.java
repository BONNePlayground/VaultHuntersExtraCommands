//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;


@Mixin(value = ClassicListenersLogic.class, remap = false)
public class MixinClassicListenersLogic
{
    @Inject(method = "onJoin",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/core/vault/player/ClassicListenersLogic;set(Liskallia/vault/core/data/key/FieldKey;)Liskallia/vault/core/data/DataObject;"))
    private void addExtraTime(VirtualWorld world,
        Vault vault,
        Listener listener,
        CallbackInfoReturnable<Boolean> cir)
    {
        ExtraCommandsData extraCommandsData = ExtraCommandsData.get(world);

        if (listener.has(Listener.ID) &&
            extraCommandsData.time.containsKey(listener.get(Listener.ID)))
        {
            int extraTicks = extraCommandsData.time.getOrDefault(listener.get(Listener.ID), 0) * 20;

            ExtraCommands.LOGGER.info("Adding extra ticks to the player vault " + extraTicks);

            vault.ifPresent(Vault.CLOCK, clock ->
                clock.setIfPresent(TickClock.DISPLAY_TIME,
                    clock.get(TickClock.DISPLAY_TIME) + extraTicks));
        }
    }
}

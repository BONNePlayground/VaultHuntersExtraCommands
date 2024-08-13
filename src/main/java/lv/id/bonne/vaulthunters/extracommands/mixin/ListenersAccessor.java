//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;


@Mixin(value = Listeners.class, remap = false)
public interface ListenersAccessor
{
    @Accessor("MAP")
    static FieldKey<Listener.Map> getMAP()
    {
        throw new UnsupportedOperationException();
    }
}

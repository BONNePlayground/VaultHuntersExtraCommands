//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import iskallia.vault.task.ProgressConfiguredTask;


@Mixin(value = ProgressConfiguredTask.class, remap = false)
public interface ProgressConfiguredTaskAccessor
{
    @Accessor("currentCount")
    public void setCurrentCount(Number currentCount);

    @Accessor("targetCount")
    public Number getTargetCount();
}

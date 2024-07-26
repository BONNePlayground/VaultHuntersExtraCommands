//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

import iskallia.vault.block.AngelBlock;


@Mixin(AngelBlock.class)
public interface AngelBlockAccessor
{
    @Accessor("angelBlocks")
    Set<Object> getAngelBlocks();
}

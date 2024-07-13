//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;

import iskallia.vault.altar.AltarInfusionRecipe;
import iskallia.vault.altar.RequiredItems;
import iskallia.vault.block.entity.VaultAltarTileEntity;
import iskallia.vault.world.data.PlayerVaultAltarData;
import lv.id.bonne.vaulthunters.extracommands.mixin.VaultAltarTileEntityAccessor;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;


public class VaultAltarCompleteCommand
{
    /**
     * Registers the command that toggles a pause for the vault.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> mainLiteral = Commands.literal("altar");

        LiteralArgumentBuilder<CommandSourceStack> complete = Commands.literal("complete").
            executes(ctx -> completeGodAltar(ctx.getSource().getPlayerOrException())).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeGodAltar(EntityArgument.getPlayer(ctx, "player"))));

        dispatcher.register(baseLiteral.then(mainLiteral.then(complete)));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int completeGodAltar(ServerPlayer player)
    {
        PlayerVaultAltarData data = PlayerVaultAltarData.get(player.getLevel());

        AltarInfusionRecipe recipe = data.getRecipe(player);

        List<RequiredItems> incompleteRequiredItems = recipe.getIncompleteRequiredItems();

        if (!incompleteRequiredItems.isEmpty())
        {
            incompleteRequiredItems.forEach(item -> item.setCurrentAmount(item.getAmountRequired()));

            List<BlockPos> altars = data.getAltars(player.getUUID());

            altars.stream().
                filter(pos -> player.getLevel().isLoaded(pos)).
                map(pos -> player.getLevel().getBlockEntity(pos)).
                filter(te -> te instanceof VaultAltarTileEntity).
                map(te -> (VaultAltarTileEntity) te).
                forEach(VaultAltarTileEntity::sendUpdates);

            data.setDirty();

            Util.sendGodMessageToPlayer(player, "I finished your crystal recipe! Waiting you in the vault!");
        }

        return 1;
    }
}
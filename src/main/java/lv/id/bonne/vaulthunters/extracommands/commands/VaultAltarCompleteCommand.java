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
            executes(ctx -> completeAltar(ctx.getSource().getPlayerOrException())).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeAltar(EntityArgument.getPlayer(ctx, "player"))));

        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").
            executes(ctx -> reloadAltar(ctx.getSource().getPlayerOrException())).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> reloadAltar(EntityArgument.getPlayer(ctx, "player"))));

        dispatcher.register(baseLiteral.then(mainLiteral.then(complete).then(reload)));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int completeAltar(ServerPlayer player)
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


    /**
     * Reload atlar recipe for player.
     * @param player Player whos recipe need to be reloaded
     * @return 1
     */
    private static int reloadAltar(ServerPlayer player)
    {
        PlayerVaultAltarData data = PlayerVaultAltarData.get(player.getLevel());

        if (data.hasRecipe(player.getUUID()))
        {
            AltarInfusionRecipe recipe = data.getRecipe(player);

            if (!recipe.getIncompleteRequiredItems().isEmpty())
            {
                data.removeRecipe(player.getUUID());
                data.getAltars(player.getUUID()).stream().
                    filter(pos -> player.getLevel().isLoaded(pos)).
                    map(pos -> player.getLevel().getBlockEntity(pos)).
                    filter(te -> te instanceof VaultAltarTileEntity).
                    map(te -> (VaultAltarTileEntity) te).
                    forEach(vaultAltarTileEntity -> {
                        data.getRecipe(player, vaultAltarTileEntity.getBlockPos());
                        vaultAltarTileEntity.sendUpdates();
                    });

                data.setDirty();

                Util.sendGodMessageToPlayer(player, "I know it was too hard. I changed your recipe!");
            }
        }

        return 1;
    }
}

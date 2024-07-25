//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;


/**
 * This command manages vault timer.
 */
public class VaultPermanentTimeCommand
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
        LiteralArgumentBuilder<CommandSourceStack> vaultTime = Commands.literal("vault_time");

        LiteralArgumentBuilder<CommandSourceStack> add = Commands.literal("add").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("seconds", IntegerArgumentType.integer(0)).
                    executes(ctx -> storeSecondsData(EntityArgument.getPlayer(ctx, "player"),
                        IntegerArgumentType.getInteger(ctx, "seconds")))));
        LiteralArgumentBuilder<CommandSourceStack> remove = Commands.literal("remove").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("seconds", IntegerArgumentType.integer(0)).
                    executes(ctx -> storeSecondsData(EntityArgument.getPlayer(ctx, "player"),
                        -IntegerArgumentType.getInteger(ctx, "seconds")))));
        LiteralArgumentBuilder<CommandSourceStack> get = Commands.literal("get").
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> getStoredData(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))));

        dispatcher.register(baseLiteral.then(vaultTime.then(add).then(remove).then(get)));
    }


    private static int storeSecondsData(ServerPlayer player, int seconds)
    {
        ExtraCommandsData extraCommandsData = ExtraCommandsData.get(player.getLevel());

        if (extraCommandsData != null)
        {
            Integer value = extraCommandsData.time.putIfAbsent(player.getUUID(), 0);

            if (value != null && value + seconds == 0)
            {
                // Remove if time is 0.
                extraCommandsData.time.remove(player.getUUID());
            }
            else
            {
                // add time to the data.
                extraCommandsData.time.put(player.getUUID(),
                    value != null ? value + seconds : seconds);
            }

            int extraSeconds = extraCommandsData.time.get(player.getUUID());

            if (extraSeconds < 0)
            {
                Util.sendGodMessageToPlayer(player,
                    "You have been punished! I remove " + (-extraSeconds)  + " seconds from your all next vaults!");
            }
            else
            {
                Util.sendGodMessageToPlayer(player,
                    "You have been blessed with extra " + extraSeconds  + " seconds for your all next vaults!");
            }

            extraCommandsData.setDirty();
        }

        return 1;
    }


    private static int getStoredData(CommandSourceStack source, ServerPlayer player)
    {
        ExtraCommandsData extraCommandsData = ExtraCommandsData.get(player.getLevel());

        if (extraCommandsData != null)
        {
            Integer value = extraCommandsData.time.getOrDefault(player.getUUID(), 0);
            String message;

            if (value > 0)
            {
                message = "Player " + player.getDisplayName().getString() + " has " + value + " seconds extra in each vault!";
            }
            else if (value < 0)
            {
                message = "Player " + player.getDisplayName().getString() + " has " + value + " seconds less in each vault!";
            }
            else
            {
                message = "Player " + player.getDisplayName().getString() + " does not have extra time in each vault!";
            }

            try
            {
                Util.sendGodMessageToPlayer(source.getPlayerOrException(), message);
            }
            catch (CommandSyntaxException e)
            {
                ExtraCommands.LOGGER.info(message);
            }
        }

        return 1;
    }
}

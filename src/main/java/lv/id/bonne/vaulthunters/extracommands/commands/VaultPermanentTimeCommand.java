//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;
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

        dispatcher.register(baseLiteral.then(vaultTime.then(add).then(remove)));
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

            extraCommandsData.setDirty();

        }

        return 1;
    }
}

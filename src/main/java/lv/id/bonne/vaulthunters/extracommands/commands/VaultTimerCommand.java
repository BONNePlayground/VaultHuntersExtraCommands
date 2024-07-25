//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;


/**
 * This command manages vault timer.
 */
public class VaultTimerCommand
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
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("vault");

        LiteralArgumentBuilder<CommandSourceStack> togglePause = Commands.literal("pause").
            executes(ctx -> togglePause(ctx.getSource().getPlayerOrException().getLevel(), false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> togglePause(EntityArgument.getPlayer(ctx, "player").getLevel(), false)).
                then(Commands.argument("complete", BoolArgumentType.bool()).
                    executes(ctx -> togglePause(EntityArgument.getPlayer(ctx, "player").getLevel(),
                        BoolArgumentType.getBool(ctx, "complete")))));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(togglePause)));
    }


    /**
     * This method applies pause or removes if for the Vault that is linked to given level.
     * @param level Level which vault needs to be paused.
     * @return 1
     */
    private static int togglePause(ServerLevel level, boolean tickStop)
    {
        ServerVaults.get(level).ifPresentOrElse(vault ->
        {
            TickClock tickClock = vault.get(Vault.CLOCK);

            if (tickClock.has(TickClock.PAUSED))
            {
                ExtraCommands.LOGGER.info("Vault timer resumed!");
                tickClock.remove(TickClock.PAUSED);

                Util.sendGodMessageToAll(level,
                    "I am back! Clock is now ticking!");

                ExtraCommandsData extraCommandsData = ExtraCommandsData.get(level);

                if (extraCommandsData != null)
                {
                    extraCommandsData.paused.remove(level.dimension().location());
                    extraCommandsData.setDirty();
                }
            }
            else
            {
                ExtraCommands.LOGGER.info("Vault timer paused!");
                tickClock.set(TickClock.PAUSED);

                ExtraCommandsData extraCommandsData = ExtraCommandsData.get(level);

                if (extraCommandsData != null)
                {
                    extraCommandsData.paused.put(level.dimension().location(), tickStop);
                    extraCommandsData.setDirty();
                }

                Util.sendGodMessageToAll(level,
                    "I have some pending tasks to do! Can you wait a bit?");
            }
        },
        () -> ExtraCommands.LOGGER.warn("Dimension does not have registered vault!"));

        return 1;
    }
}

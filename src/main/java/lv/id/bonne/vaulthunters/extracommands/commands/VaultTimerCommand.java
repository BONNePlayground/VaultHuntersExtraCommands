//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.modifier.PylonExtension;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsWorldData;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


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
        LiteralArgumentBuilder<CommandSourceStack> pause = Commands.literal("pause");
        pause.requires(stack ->
        {
            try
            {
                return ExtraCommands.CONFIGURATION.getPlayersWithPausePermission().
                    contains(stack.getPlayerOrException().getUUID()) ||
                    stack.hasPermission(1);
            }
            catch (CommandSyntaxException e)
            {
                ExtraCommands.LOGGER.info("PAUSE can be run only as player.");
                return false;
            }
        });
        pause.executes(ctx -> togglePause(ctx.getSource().getLevel(), true));

        LiteralArgumentBuilder<CommandSourceStack> pauseAdd = Commands.literal("add").
            requires(stack -> stack.hasPermission(1)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> configPlayer(EntityArgument.getPlayer(ctx, "player"), true)));
        LiteralArgumentBuilder<CommandSourceStack> pauseRemove = Commands.literal("remove").
            requires(stack -> stack.hasPermission(1)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> configPlayer(EntityArgument.getPlayer(ctx, "player"), false)));

        dispatcher.register(pause.then(pauseAdd).then(pauseRemove));

        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("vault");

        LiteralArgumentBuilder<CommandSourceStack> togglePause = Commands.literal("pause").
            executes(ctx -> togglePause(ctx.getSource().getPlayerOrException().getLevel(), true)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> togglePause(EntityArgument.getPlayer(ctx, "player").getLevel(), true)));

        LiteralArgumentBuilder<CommandSourceStack> timeLiteral = Commands.literal("time");

        LiteralArgumentBuilder<CommandSourceStack> addTime = Commands.literal("increase").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("seconds", IntegerArgumentType.integer(1)).
                    executes(ctx -> addVaultTime(EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "seconds")))));

        LiteralArgumentBuilder<CommandSourceStack> removeTime = Commands.literal("reduce").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("seconds", IntegerArgumentType.integer(1)).
                    executes(ctx -> addVaultTime(EntityArgument.getPlayer(ctx, "player"), -IntegerArgumentType.getInteger(ctx, "seconds")))));

        LiteralArgumentBuilder<CommandSourceStack> stop = Commands.literal("stop").
            executes(ctx -> togglePause(ctx.getSource().getPlayerOrException().getLevel(), false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> togglePause(EntityArgument.getPlayer(ctx, "player").getLevel(), false)));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(togglePause).then(timeLiteral.then(addTime).then(removeTime).then(stop))));
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

                ExtraCommandsWorldData extraCommandsData = ExtraCommandsWorldData.get(level);
                extraCommandsData.setPaused(false);
                extraCommandsData.setDirty();
            }
            else
            {
                ExtraCommands.LOGGER.info("Vault timer paused!");
                tickClock.set(TickClock.PAUSED);

                ExtraCommandsWorldData extraCommandsData = ExtraCommandsWorldData.get(level);
                extraCommandsData.setPaused(tickStop);
                extraCommandsData.setDirty();

                Util.sendGodMessageToAll(level,
                    "I have some pending tasks to do! Can you wait a bit?");
            }
        },
        () -> ExtraCommands.LOGGER.warn("Dimension does not have registered vault!"));

        return 1;
    }


    private static int addVaultTime(ServerPlayer player, int seconds)
    {
        ServerLevel level = player.getLevel();

        ServerVaults.get(level).ifPresentOrElse(vault ->
            {
                TickClock tickClock = vault.get(Vault.CLOCK);
                tickClock.addModifier(new PylonExtension(player, seconds * 20));

                if (seconds > 0)
                {
                    Util.sendGodMessageToAll(level, "You have been blessed with extra " + seconds + " seconds in the Vault!");
                    ExtraCommands.LOGGER.warn("Added extra " + seconds + " seconds to the vault!");
                }
                else
                {
                    Util.sendGodMessageToAll(level, "You have been punished! I removed " + (-seconds) + " seconds from your Vault!", VaultGod.TENOS);
                    ExtraCommands.LOGGER.warn("Removed " + (-seconds) + " seconds from the vault!");
                }
            },
            () -> ExtraCommands.LOGGER.warn("Dimension does not have registered vault!"));

        return 1;
    }


    private static int configPlayer(ServerPlayer player, boolean add)
    {
        if (add)
        {
            ExtraCommands.CONFIGURATION.addPlayerToPauseList(player.getUUID());
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " added to pause list.");
        }
        else
        {
            ExtraCommands.CONFIGURATION.removePlayerFromPauseList(player.getUUID());
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " removed from pause list.");
        }

        return 1;
    }
}

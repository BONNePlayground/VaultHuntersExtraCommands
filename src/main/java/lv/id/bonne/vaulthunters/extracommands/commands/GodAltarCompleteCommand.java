//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import iskallia.vault.task.*;
import iskallia.vault.task.source.EntityTaskSource;
import iskallia.vault.task.source.TaskSource;
import iskallia.vault.world.data.GodAltarData;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.mixin.accessors.GodAltarDataAccessor;
import lv.id.bonne.vaulthunters.extracommands.mixin.accessors.GodAltarTaskAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;


public class GodAltarCompleteCommand
{
    /**
     * Registers the command that toggles a pause for the vault.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("vault");
        LiteralArgumentBuilder<CommandSourceStack> mainLiteral = Commands.literal("godAltar");

        LiteralArgumentBuilder<CommandSourceStack> complete = Commands.literal("complete").
            executes(ctx -> completeGodAltar(ctx.getSource().getPlayerOrException(), true)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeGodAltar(EntityArgument.getPlayer(ctx, "player"), true)));
        LiteralArgumentBuilder<CommandSourceStack> fail = Commands.literal("fail").
            executes(ctx -> completeGodAltar(ctx.getSource().getPlayerOrException(), false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeGodAltar(EntityArgument.getPlayer(ctx, "player"), false)));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(mainLiteral.then(complete).then(fail))));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int completeGodAltar(ServerPlayer player, boolean complete)
    {
        if (GodAltarData.contains(player))
        {
            // Player has god altar.

            GodAltarDataAccessor godAltarData = (GodAltarDataAccessor) GodAltarData.get();

            Map<UUID, GodAltarData.Entry> entries = godAltarData.getEntries();

            Iterator<GodAltarData.Entry> var1 = entries.values().iterator();

            while (var1.hasNext())
            {
                GodAltarData.Entry entry = var1.next();
                TaskSource var4 = entry.getSource();

                if (var4 instanceof EntityTaskSource entitySource)
                {
                    if (entitySource.matches(player))
                    {
                        if (entry.getTask() instanceof GodAltarTask godTask)
                        {
                            ServerVaults.get(((GodAltarTaskAccessor) godTask).getVaultUuid()).ifPresent(vault -> {
                                if (!complete)
                                {
                                    godTask.onFail(vault, TaskContext.of(var4, player.getServer()));
                                    ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " failed God Altar!");
                                }
                                else
                                {
                                    godTask.onSucceed(vault, TaskContext.of(var4, player.getServer()));
                                    ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " completed God Altar!");
                                }
                            });
                        }
                    }
                }
            }
        }
        else
        {
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " has no active God Altar!");
        }

        return 1;
    }
}

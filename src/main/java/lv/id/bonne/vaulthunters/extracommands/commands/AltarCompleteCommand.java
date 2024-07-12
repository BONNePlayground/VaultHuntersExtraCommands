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

import iskallia.vault.task.FailGodAltarTask;
import iskallia.vault.task.NodeTask;
import iskallia.vault.task.ProgressConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.source.EntityTaskSource;
import iskallia.vault.task.source.TaskSource;
import iskallia.vault.world.data.GodAltarData;
import lv.id.bonne.vaulthunters.extracommands.mixin.GodAltarDataAccessor;
import lv.id.bonne.vaulthunters.extracommands.mixin.ProgressConfiguredTaskAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;


public class AltarCompleteCommand
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
        LiteralArgumentBuilder<CommandSourceStack> mainLiteral = Commands.literal("godAltar");

        LiteralArgumentBuilder<CommandSourceStack> complete = Commands.literal("complete").
            executes(ctx -> completeGodAltar(ctx.getSource().getPlayerOrException(), true)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeGodAltar(EntityArgument.getPlayer(ctx, "player"), true)));
        LiteralArgumentBuilder<CommandSourceStack> fail = Commands.literal("fail").
            executes(ctx -> completeGodAltar(ctx.getSource().getPlayerOrException(), false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeGodAltar(EntityArgument.getPlayer(ctx, "player"), false)));

        dispatcher.register(baseLiteral.then(mainLiteral.then(complete).then(fail)));
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
                        NodeTask task = (NodeTask) entry.getTask();

                        for (NodeTask child : task.getChildren())
                        {
                            Task delegate = child.getDelegate();

                            if (!complete && delegate instanceof FailGodAltarTask fail)
                            {
                                fail.onStop(var4);
                            }
                            else if (complete && delegate instanceof ProgressConfiguredTask progressTask)
                            {
                                ProgressConfiguredTaskAccessor accessor = (ProgressConfiguredTaskAccessor) progressTask;
                                accessor.setCurrentCount(accessor.getTargetCount());
                            }
                        }
                    }
                }
            }
        }


        return 1;
    }
}

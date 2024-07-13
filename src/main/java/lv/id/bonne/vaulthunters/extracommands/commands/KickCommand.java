//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;


/**
 * This command completed bounty asigned to the player.
 */
public class KickCommand
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

        LiteralArgumentBuilder<CommandSourceStack> kick = Commands.literal("kick").
            executes(ctx -> completeBounty(ctx.getSource().getPlayerOrException(), false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeBounty(EntityArgument.getPlayer(ctx, "player"), false)).
                then(Commands.argument("complete", BoolArgumentType.bool()).
                    executes(ctx -> completeBounty(EntityArgument.getPlayer(ctx, "player"),
                        BoolArgumentType.getBool(ctx, "complete")))));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(kick)));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int completeBounty(ServerPlayer player, boolean complete)
    {
        for (Vault vault : ServerVaults.getAll())
        {
            vault.ifPresent(Vault.LISTENERS, (listeners) ->
            {
                Listener listener = listeners.get(player.getUUID());

                ServerVaults.getWorld(vault).ifPresent((world) ->
                {
                    listeners.remove(world, vault, listener);

                    vault.ifPresent(Vault.STATS, (collector) ->
                    {
                        StatCollector stats = collector.get(listener.get(Listener.ID));

                        stats.set(StatCollector.COMPLETION, complete ? Completion.COMPLETED : Completion.BAILED);
                    });
                });
            });
        }

        return 1;
    }
}
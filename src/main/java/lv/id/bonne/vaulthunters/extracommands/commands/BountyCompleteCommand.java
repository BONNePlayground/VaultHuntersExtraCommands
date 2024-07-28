//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.*;

import iskallia.vault.bounty.Bounty;
import iskallia.vault.world.data.BountyData;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsData;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;


/**
 * This command completed bounty asigned to the player.
 */
public class BountyCompleteCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BOUNTY = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateSuggestions(context),
            builder));


    private static List<String> generateSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        BountyData data = BountyData.get();

        if (data != null)
        {
            return data.getAllActiveFor(EntityArgument.getPlayer(context, "player").getUUID()).
                stream().
                map(Bounty::getId).
                map(UUID::toString).
                toList();
        }
        else
        {
            return Collections.emptyList();
        }
    }


    /**
     * Registers the command that toggles a pause for the vault.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> mainLiteral = Commands.literal("bounty");

        LiteralArgumentBuilder<CommandSourceStack> togglePause = Commands.literal("complete").
            executes(ctx -> completeBounty(ctx.getSource().getPlayerOrException(), null)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeBounty(EntityArgument.getPlayer(ctx, "player"), null)).
                then(Commands.argument("uuid", UuidArgument.uuid()).
                    suggests(SUGGEST_BOUNTY).
                    executes(ctx -> completeBounty(EntityArgument.getPlayer(ctx, "player"),
                        UuidArgument.getUuid(ctx, "uuid")))));

        dispatcher.register(baseLiteral.then(mainLiteral.then(togglePause)));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int completeBounty(ServerPlayer player, UUID bountyUUID)
    {
        BountyData data = BountyData.get();

        if (bountyUUID == null)
        {
            List<UUID> activeBountyList = data.getAllActiveFor(player.getUUID()).stream().map(Bounty::getId).toList();
            activeBountyList.forEach(bountyID -> data.complete(player, bountyID));

            if (!activeBountyList.isEmpty())
            {
                Util.sendGodMessageToPlayer(player, "You have been blessed with free bounty!");
                ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " completed all bounties!");
            }
        }
        else
        {
            data.complete(player, bountyUUID);
            Util.sendGodMessageToPlayer(player, "You have been blessed with free bounty!");
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " completed bounty with id: " + bountyUUID + "!");
        }

        return 1;
    }
}

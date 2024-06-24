//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.List;
import java.util.UUID;

import iskallia.vault.bounty.Bounty;
import iskallia.vault.world.data.BountyData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;


/**
 * This command completed bounty asigned to the player.
 */
public class BountyCompleteCommand
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
        LiteralArgumentBuilder<CommandSourceStack> mainLiteral = Commands.literal("bounty");

        LiteralArgumentBuilder<CommandSourceStack> togglePause = Commands.literal("complete").
            executes(ctx -> completeBounty(ctx.getSource().getPlayerOrException(), null)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> completeBounty(EntityArgument.getPlayer(ctx, "player"), null)).
                then(Commands.argument("uuid", UuidArgument.uuid()).
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
        }
        else
        {
            data.complete(player, bountyUUID);
        }

        return 1;
    }
}

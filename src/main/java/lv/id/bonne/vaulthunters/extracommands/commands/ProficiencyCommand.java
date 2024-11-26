//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.world.data.PlayerProficiencyData;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;


public class ProficiencyCommand
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
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("proficiency");

        LiteralArgumentBuilder<CommandSourceStack> add = Commands.literal("increase").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("level", IntegerArgumentType.integer(1)).
                    executes(ctx -> addProficiency(EntityArgument.getPlayer(ctx, "player"),
                        IntegerArgumentType.getInteger(ctx, "level")))));

        LiteralArgumentBuilder<CommandSourceStack> reduce = Commands.literal("reduce").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("level", IntegerArgumentType.integer(1)).
                    executes(ctx -> removeProficiency(EntityArgument.getPlayer(ctx, "player"),
                        IntegerArgumentType.getInteger(ctx, "level")))));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(add).then(reduce)));
    }


    /**
     * @return 1
     */
    private static int addProficiency(ServerPlayer player, int number)
    {
        PlayerProficiencyData proficiencyData = PlayerProficiencyData.get(player.getLevel());
        proficiencyData.setAbsoluteProficiency(player.getUUID(),
            proficiencyData.getAbsoluteProficiency(player) + number);

        Component component = new TextComponent("You have been blessed with extra crafting proficiency!").
            withStyle(ChatFormatting.WHITE);

        Util.sendGodMessageToPlayer(player, component);
        ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " proficiency increased by " + number);

        return 1;
    }


    /**
     * @return 1
     */
    private static int removeProficiency(ServerPlayer player, int number)
    {
        PlayerProficiencyData proficiencyData = PlayerProficiencyData.get(player.getLevel());
        proficiencyData.setAbsoluteProficiency(player.getUUID(),
            Math.max(proficiencyData.getAbsoluteProficiency(player) - number, 0));

        Component component = new TextComponent("You have been punished and I reduced your crafting proficiency!").
            withStyle(ChatFormatting.WHITE);

        Util.sendGodMessageToPlayer(player, component);
        ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " proficiency reduced by " + number);

        return 1;
    }
}

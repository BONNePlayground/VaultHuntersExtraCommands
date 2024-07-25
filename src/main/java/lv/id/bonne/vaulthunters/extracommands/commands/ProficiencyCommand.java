//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.world.data.PlayerProficiencyData;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
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
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("proficiency");

        LiteralArgumentBuilder<CommandSourceStack> add = Commands.literal("increase").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("type", EnumArgument.enumArgument(ProficiencyType.class)).
                    then(Commands.argument("level", IntegerArgumentType.integer(1)).
                        executes(ctx -> addProficiency(EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "type"),
                            IntegerArgumentType.getInteger(ctx, "level"))))));

        LiteralArgumentBuilder<CommandSourceStack> reduce = Commands.literal("reduce").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("type", EnumArgument.enumArgument(ProficiencyType.class)).
                    then(Commands.argument("level", IntegerArgumentType.integer(1)).
                        executes(ctx -> removeProficiency(EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "type"),
                            IntegerArgumentType.getInteger(ctx, "level"))))));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(add).then(reduce)));
    }


    /**
     * @return 1
     */
    private static int addProficiency(ServerPlayer player, String typeName, int number)
    {
        Optional<ProficiencyType> ifPresent = Enums.getIfPresent(ProficiencyType.class, typeName.toUpperCase());

        if (ifPresent.isPresent())
        {
            ProficiencyType type = ifPresent.get();
            PlayerProficiencyData proficiencyData = PlayerProficiencyData.get(player.getLevel());
            proficiencyData.setProficiency(player.getUUID(),
                type,
                proficiencyData.getProficiency(player, type) + number);

            Util.sendGodMessageToPlayer(player, "You have been blessed with extra proficiency in " + type.getDisplayName() + "!");
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " proficiency in " + type.getDisplayName() + " increased by " + number);
        }
        else
        {
            ExtraCommands.LOGGER.info("Unknown proficiency type: " + typeName + "!");
        }

        return 1;
    }


    /**
     * @return 1
     */
    private static int removeProficiency(ServerPlayer player, String typeName, int number)
    {
        Optional<ProficiencyType> ifPresent = Enums.getIfPresent(ProficiencyType.class, typeName.toUpperCase());

        if (ifPresent.isPresent())
        {
            ProficiencyType type = ifPresent.get();
            PlayerProficiencyData proficiencyData = PlayerProficiencyData.get(player.getLevel());
            proficiencyData.setProficiency(player.getUUID(),
                type,
                Math.max(proficiencyData.getProficiency(player, type) - number, 0));

            Util.sendGodMessageToPlayer(player, "You have been punished and I reduced your proficiency in " + type.getDisplayName() + "!");
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " proficiency in " + type.getDisplayName() + " reduced by " + number);
        }
        else
        {
            ExtraCommands.LOGGER.info("Unknown proficiency type: " + typeName + "!");
        }

        return 1;
    }
}

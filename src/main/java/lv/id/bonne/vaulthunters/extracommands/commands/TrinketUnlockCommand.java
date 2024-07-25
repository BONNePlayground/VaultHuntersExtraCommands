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

import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.TrinketEffectRegistry;
import iskallia.vault.world.data.DiscoveredTrinketsData;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.ForgeRegistryEntry;


public class TrinketUnlockCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateSuggestions(context),
            builder));


    private static List<String> generateSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");

        DiscoveredTrinketsData trinketsData = DiscoveredTrinketsData.get(player.getLevel());

        return TrinketEffectRegistry.getOrderedEntries().stream().
            filter(trinket -> !trinketsData.hasDiscovered(player, trinket)).
            map(ForgeRegistryEntry::getRegistryName).
            filter(Objects::nonNull).
            map(ResourceLocation::toString).
            toList();
    }

    /**
     * Registers the command that toggles a pause for the vault.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("trinket").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("trinket", ResourceLocationArgument.id()).
                    suggests(SUGGESTIONS).
                    executes(ctx -> unlockTrinket(EntityArgument.getPlayer(ctx, "player"),
                        ResourceLocationArgument.getId(ctx, "trinket")))
                )
            );

        dispatcher.register(baseLiteral.then(command));
    }


    private static int unlockTrinket(ServerPlayer player, ResourceLocation resourceKey)
    {
        Optional<TrinketEffect<?>> trinketOptional = TrinketEffectRegistry.getOrderedEntries().
            stream().
            filter(effect -> resourceKey.equals(effect.getRegistryName())).
            findFirst();

        if (trinketOptional.isEmpty())
        {
            ExtraCommands.LOGGER.info("Do not know what is " + resourceKey + " but it is not a trinket!");
            return 1;
        }

        TrinketEffect<?> trinket = trinketOptional.get();

        DiscoveredTrinketsData trinketsData = DiscoveredTrinketsData.get(player.getLevel());

        if (trinketsData.discoverTrinket(player.getUUID(), trinket))
        {
            trinketsData.syncTo(player);

            Util.sendGodMessageToPlayer(player,
                new TextComponent("Hey lucky you! You won a lottery and the prize is ").
                    append((new TextComponent(trinket.getTrinketConfig().getName())).
                        setStyle(Style.EMPTY.withColor(trinket.getTrinketConfig().getComponentColor())).
                    append(" trinket!")).
                    withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " unlocked " +
                resourceKey + " trinket!");
        }
        else
        {
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " had already unlocked " +
                resourceKey + " trinket");
        }

        return 1;
    }
}

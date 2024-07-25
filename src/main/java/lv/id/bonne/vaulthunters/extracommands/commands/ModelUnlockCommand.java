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

import iskallia.vault.dynamodel.registry.DynamicModelRegistry;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModItems;
import iskallia.vault.world.data.DiscoveredModelsData;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import lv.id.bonne.vaulthunters.extracommands.util.Util.ItemType;
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
import net.minecraftforge.server.command.EnumArgument;


public class ModelUnlockCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODEL = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateSuggestions(context),
            builder));


    private static List<String> generateSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ItemType item = context.getArgument("item", ItemType.class);
        ServerPlayer player = EntityArgument.getPlayer(context, "player");

        DiscoveredModelsData modelsData = DiscoveredModelsData.get(player.server);
        Set<ResourceLocation> discoveredModels = modelsData.getDiscoveredModels(player.getUUID());

        Optional<DynamicModelRegistry<?>> associatedRegistry = switch (item)
        {
            case SWORD -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.SWORD);
            case AXE -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.AXE);
            case BOOTS -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.BOOTS);
            case CHESTPLATE -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.CHESTPLATE);
            case HELMET -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.HELMET);
            case LEGGINGS -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.LEGGINGS);
            case SHIELD -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.SHIELD);
            case FOCUS -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.FOCUS);
            case WAND -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.WAND);
            case MAGNET -> ModDynamicModels.REGISTRIES.getAssociatedRegistry(ModItems.MAGNET);
        };

        return associatedRegistry.map(dynamicModelRegistry -> dynamicModelRegistry.getIds().
            stream().
            filter(resource -> !discoveredModels.contains(resource)).
            map(ResourceLocation::toString).toList()).
            orElse(Collections.emptyList());
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

        LiteralArgumentBuilder<CommandSourceStack> discovery = Commands.literal("discovery");

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("model").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("item", EnumArgument.enumArgument(ItemType.class)).
                    then(Commands.argument("model", ResourceLocationArgument.id()).
                        suggests(SUGGEST_MODEL).
                        executes(ctx -> unlockModel(EntityArgument.getPlayer(ctx, "player"),
                            ctx.getArgument("item", ItemType.class),
                            ResourceLocationArgument.getId(ctx, "model")))
                    )
                )
            );

        dispatcher.register(baseLiteral.then(discovery.then(command)));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int unlockModel(ServerPlayer player, ItemType item, ResourceLocation resourceLocation)
    {
        DiscoveredModelsData modelsData = DiscoveredModelsData.get(player.server);
        if (modelsData.discoverModel(player.getUUID(), resourceLocation))
        {
            modelsData.syncTo(player);

            ModDynamicModels.REGISTRIES.getModelAndAssociatedItem(resourceLocation).ifPresent(
                itemPair -> Util.sendGodMessageToPlayer(player,
                    new TextComponent("Ups, I dropped something! Looks like you picked up ").
                        append((new TextComponent(itemPair.getFirst().getDisplayName())).
                            setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))).
                        append(" model for " + item.name().toLowerCase() + "!").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
            );

            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " unlocked " +
                resourceLocation + " for " + item.name());
        }
        else
        {
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " had already unlocked " +
                resourceLocation + " for " + item.name());
        }

        return 1;
    }
}

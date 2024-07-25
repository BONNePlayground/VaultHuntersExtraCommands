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
import java.util.List;

import iskallia.vault.config.gear.VaultGearWorkbenchConfig;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.DiscoveredWorkbenchModifiersData;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;


public class WorkbenchUnlockCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateSuggestions(context),
            builder));


    private static List<String> generateSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ItemType item = context.getArgument("item", ItemType.class);

        DiscoveredWorkbenchModifiersData workbenchData = DiscoveredWorkbenchModifiersData.get(player.getLevel());

        return ModConfigs.VAULT_GEAR_WORKBENCH_CONFIG.get(item.item()).getAllCraftableModifiers().
            stream().
            map(VaultGearWorkbenchConfig.CraftableModifierConfig::getWorkbenchCraftIdentifier).
            filter(modifierIdentifier -> !workbenchData.hasDiscoveredCraft(player, item.item(), modifierIdentifier)).
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

        LiteralArgumentBuilder<CommandSourceStack> discovery = Commands.literal("discovery");

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("workbench").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("item", EnumArgument.enumArgument(ItemType.class)).
                    then(Commands.argument("modifier", ResourceLocationArgument.id()).
                    suggests(SUGGESTIONS).
                    executes(ctx -> unlockModifier(EntityArgument.getPlayer(ctx, "player"),
                        ctx.getArgument("item", ItemType.class),
                        ResourceLocationArgument.getId(ctx, "modifier")))
                    )
                )
            );

        dispatcher.register(baseLiteral.then(discovery.then(command)));
    }


    private static int unlockModifier(ServerPlayer player, ItemType item, ResourceLocation resourceLocation)
    {
        VaultGearWorkbenchConfig vaultGearWorkbenchConfig = ModConfigs.VAULT_GEAR_WORKBENCH_CONFIG.get(item.item());
        VaultGearWorkbenchConfig.CraftableModifierConfig config = vaultGearWorkbenchConfig.getConfig(resourceLocation);

        if (config == null)
        {
            ExtraCommands.LOGGER.error("Something went worng with unlocking " + resourceLocation + " in alchemy table!");
            return 1;
        }

        DiscoveredWorkbenchModifiersData workbenchData =
            DiscoveredWorkbenchModifiersData.get(player.getLevel());

        if (!workbenchData.hasDiscoveredCraft(player, item.item(), resourceLocation))
        {
            workbenchData.compoundDiscoverWorkbenchCraft(player, item.item(), resourceLocation);

            config.createModifier().ifPresent(modifier -> {
                ItemStack stack = new ItemStack(item.item());

                if (stack.getItem() instanceof VaultGearItem) {
                    VaultGearData vgData = VaultGearData.read(stack);
                    vgData.setState(VaultGearState.IDENTIFIED);
                    vgData.setRarity(VaultGearRarity.COMMON);
                    vgData.write(stack);
                }

                modifier.getConfigDisplay(stack).ifPresent(configDisplay -> {
                    Util.sendGodMessageToPlayer(player,
                        new TextComponent("You have impressed us! Take our gift: ").
                            append(configDisplay.
                                setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).
                                append(" modifier!")).
                            withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
                });
            });

            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " unlock " +
                resourceLocation + " modifier for " + item.name() + "!");
        }
        else
        {
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " already knows " +
                resourceLocation + " modifier for " + item.name() + "!");
        }

        return 1;
    }
}

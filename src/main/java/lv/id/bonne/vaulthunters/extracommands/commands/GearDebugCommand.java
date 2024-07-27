//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.*;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.world.data.DiscoveredModelsData;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;


public class GearDebugCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODEL = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateSuggestions(context),
            builder));


    private static List<String> generateSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();

        DiscoveredModelsData modelsData = DiscoveredModelsData.get(player.server);
        Set<ResourceLocation> discoveredModels = modelsData.getDiscoveredModels(player.getUUID());

        Optional<DynamicModelRegistry<?>> associatedRegistry =
            ModDynamicModels.REGISTRIES.getAssociatedRegistry(player.getMainHandItem().getItem());

        return associatedRegistry.map(dynamicModelRegistry -> dynamicModelRegistry.getIds().
                stream().
                filter(resource -> !discoveredModels.contains(resource)).
                map(ResourceLocation::toString).toList()).
            orElse(Collections.emptyList());
    }


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> gearDebug = Commands.literal("gear_debug");

        // Legendary rolls
        LiteralArgumentBuilder<CommandSourceStack> legendary = Commands.literal("rollLegendary").
            executes(ctx -> forceLegendary(ctx.getSource().getPlayerOrException(), Roll.NONE)).
            then(Commands.argument("roll", EnumArgument.enumArgument(Roll.class)).
                executes(ctx -> forceLegendary(ctx.getSource().getPlayerOrException(),
                    ctx.getArgument("roll", Roll.class))));
        // Repair commands
        LiteralArgumentBuilder<CommandSourceStack> repair = Commands.literal("repairs");

        LiteralArgumentBuilder<CommandSourceStack> breakGear = Commands.literal("break").
            executes(ctx -> repairs(ctx.getSource().getPlayerOrException(), true));
        LiteralArgumentBuilder<CommandSourceStack> fixGear = Commands.literal("fix").
            executes(ctx -> repairs(ctx.getSource().getPlayerOrException(), false));
        LiteralArgumentBuilder<CommandSourceStack> setSlots = Commands.literal("setSlots").
            then(Commands.argument("slots", IntegerArgumentType.integer(1)).
                executes(ctx -> setRepairSlots(ctx.getSource().getPlayerOrException(),
                    IntegerArgumentType.getInteger(ctx, "slots"))));

        LiteralArgumentBuilder<CommandSourceStack> rarity = Commands.literal("rarity").
            then(Commands.argument("roll", EnumArgument.enumArgument(VaultGearRarity.class)).
                executes(ctx -> setRarity(ctx.getSource().getPlayerOrException(), ctx.getArgument("roll", VaultGearRarity.class))));

        LiteralArgumentBuilder<CommandSourceStack> model = Commands.literal("model").
            then(Commands.argument("resource", ResourceLocationArgument.id()).
                suggests(SUGGEST_MODEL).
                executes(ctx -> setModel(ctx.getSource().getPlayerOrException(), ResourceLocationArgument.getId(ctx, "resource"))));

        dispatcher.register(baseLiteral.then(gearDebug.
            then(legendary).
            then(rarity).
            then(model).
            then(repair.then(breakGear).then(fixGear).then(setSlots))));
    }


    private static int setModel(ServerPlayer player, ResourceLocation model)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultGearItem))
        {
            player.sendMessage(new TextComponent("No vaultgear held in hand"), net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not vaultgear in hand");
        }

        VaultGearData data = VaultGearData.read(mainHandItem);

        if (data.getState() != VaultGearState.IDENTIFIED)
        {
            player.sendMessage(new TextComponent("Only identified gear can change its model"),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        data.updateAttribute(ModGearAttributes.GEAR_MODEL, model);
        data.write(mainHandItem);

        Util.sendGodMessageToPlayer(player,
            new TextComponent("I updated your gear model as requested.").
                withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        return 1;
    }


    private static int setRarity(ServerPlayer player, VaultGearRarity gearRarity)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultGearItem))
        {
            player.sendMessage(new TextComponent("No vaultgear held in hand"), net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not vaultgear in hand");
        }

        VaultGearData data = VaultGearData.read(mainHandItem);

        if (data.getState() != VaultGearState.IDENTIFIED)
        {
            player.sendMessage(new TextComponent("Only identified gear can change its rarity"),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        VaultGearRarity old = data.getRarity();

        if (old != gearRarity)
        {
            data.setRarity(gearRarity);
            data.write(mainHandItem);
        }

        Util.sendGodMessageToPlayer(player,
            new TextComponent("Oh, it is just a display thing! Did you wanted something else?").
                withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        return 1;
    }


    private static int setRepairSlots(ServerPlayer player, int newSlotCount)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultGearItem))
        {
            player.sendMessage(new TextComponent("No vaultgear held in hand"), net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not vaultgear in hand");
        }

        VaultGearData data = VaultGearData.read(mainHandItem);

        if (data.getState() != VaultGearState.IDENTIFIED)
        {
            player.sendMessage(new TextComponent("Only identified gear can be fixed or broken"),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        int old = data.getRepairSlots();

        if (old != newSlotCount)
        {
            data.setRepairSlots(newSlotCount);
            data.write(mainHandItem);
        }

        if (old > newSlotCount)
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("I do not like this tool! I reduce number of repairs you can have on it!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        }
        else
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("Your tool impresses me! You can use is a bit longer now!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        }

        return 1;
    }


    private static int repairs(ServerPlayer player, boolean breakItem)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultGearItem))
        {
            player.sendMessage(new TextComponent("No vaultgear held in hand"), net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not vaultgear in hand");
        }

        VaultGearData data = VaultGearData.read(mainHandItem);

        if (data.getState() != VaultGearState.IDENTIFIED)
        {
            player.sendMessage(new TextComponent("Only identified gear can be fixed or broken"),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        int usedRepairSlots = data.getUsedRepairSlots() + (breakItem ? 1 : -1);

        if (usedRepairSlots > data.getRepairSlots())
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("I am god, not a magician! I cannot break thing that is already broken!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
        else if (usedRepairSlots < 0)
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("I am god, not a magician! I cannot fix thing that is fully fixed!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
        else
        {
            data.setUsedRepairSlots(usedRepairSlots);
            data.write(mainHandItem);

            if (breakItem)
            {
                Util.sendGodMessageToPlayer(player,
                    new TextComponent("You were naughty! I think you deserve less usage of this tool!").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            }
            else
            {
                Util.sendGodMessageToPlayer(player,
                    new TextComponent("Your good behaviour resulted inspired me! I fixed your tool!").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            }
        }

        return 0;
    }


    private static int forceLegendary(ServerPlayer player, Roll roll)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultGearItem))
        {
            player.sendMessage(new TextComponent("No vaultgear held in hand"), net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not vaultgear in hand");
        }

        VaultGearData data = VaultGearData.read(mainHandItem);

        if (data.getState() != VaultGearState.IDENTIFIED)
        {
            player.sendMessage(new TextComponent("Only identified gear can roll legendary"),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        if (!data.isModifiable())
        {
            throw new IllegalArgumentException("Gear is not modifiable!!!");
        }

        VaultGearTierConfig config = VaultGearTierConfig.getConfig(mainHandItem).orElse(null);

        if (config == null)
        {
            throw new IllegalArgumentException("Unknown VaultGear");
        }

        // This method rolls legendary modifier on your gear.

        if (roll == Roll.ADD || roll == Roll.NONE && getLegendaryAttribute(config, data).isEmpty())
        {
            VaultGearModifierHelper.generateLegendaryModifier(mainHandItem, GearRollHelper.rand);

            Util.sendGodMessageToPlayer(player,
                new TextComponent("Your tool has been blessed!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        }
        else if (roll == Roll.REROLL)
        {
            getLegendaryAttribute(config, data).ifPresent(legendaryPair ->
                upgradeModifier(player, mainHandItem, data, config, legendaryPair));
        }
        else if (roll == Roll.IMPLICIT)
        {
            List<Tuple<VaultGearModifier.AffixType, VaultGearModifier<?>>> modifiers = new ArrayList<>();
            data.getModifiers(VaultGearModifier.AffixType.IMPLICIT).forEach(modifier ->
            {
                if (!shouldRemove(modifier, config))
                {
                    modifiers.add(new Tuple<>(VaultGearModifier.AffixType.IMPLICIT, modifier));
                }
            });

            Util.getRandom(modifiers).ifPresent(modifierPair ->
                upgradeModifier(player, mainHandItem, data, config, modifierPair));
        }
        else if (roll == Roll.NONE && getLegendaryAttribute(config, data).isPresent())
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("Your are asking too much from us!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
        }

        return 1;
    }


    private static void upgradeModifier(ServerPlayer player, ItemStack mainHandItem,
        VaultGearData data,
        VaultGearTierConfig config,
        Tuple<VaultGearModifier.AffixType, VaultGearModifier<?>> legendary)
    {
        // Upgrade modifier
        VaultGearModifier<?> newMod = config.maxAndIncreaseTier(legendary.getA(),
            legendary.getB(),
            data.getItemLevel(),
            2,
            GearRollHelper.rand);

        if (newMod == null)
        {
            throw new IllegalArgumentException("Could not upgrade your vault gear modifiers!");
        }
        else
        {
            newMod.setCategory(VaultGearModifier.AffixCategory.LEGENDARY);

            if (data.removeModifier(legendary.getB()))
            {
                data.addModifierFirst(legendary.getA(), newMod);

                Util.sendGodMessageToPlayer(player,
                    new TextComponent("Your tool has been blessed!").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
            }

            data.write(mainHandItem);
        }
    }


    private static Optional<Tuple<VaultGearModifier.AffixType, VaultGearModifier<?>>> getLegendaryAttribute(
        VaultGearTierConfig config,
        VaultGearData data)
    {
        if (!data.isModifiable())
        {
            return Optional.empty();
        }

        List<Tuple<VaultGearModifier.AffixType, VaultGearModifier<?>>> modifiers = new ArrayList<>();
        data.getModifiers(VaultGearModifier.AffixType.IMPLICIT).
            forEach((modifier) -> modifiers.add(new Tuple<>(VaultGearModifier.AffixType.IMPLICIT, modifier)));
        data.getModifiers(VaultGearModifier.AffixType.PREFIX).
            forEach((modifier) -> modifiers.add(new Tuple<>(VaultGearModifier.AffixType.PREFIX, modifier)));
        data.getModifiers(VaultGearModifier.AffixType.SUFFIX).
            forEach((modifier) -> modifiers.add(new Tuple<>(VaultGearModifier.AffixType.SUFFIX, modifier)));
        modifiers.removeIf(pair -> shouldRemove(pair.getB(), config));

        if (modifiers.isEmpty())
        {
            // No modifiers.
            return Optional.empty();
        }

        // Reorder modifiers.
        Collections.shuffle(modifiers);

        return modifiers.stream().
            filter(mod -> mod.getB().getCategory() == VaultGearModifier.AffixCategory.LEGENDARY).
            findAny();
    }


    private static boolean shouldRemove(VaultGearModifier<?> modifier, VaultGearTierConfig config)
    {
        VaultGearTierConfig.ModifierTierGroup group = config.getTierGroup(modifier.getModifierIdentifier());

        if (!modifier.getCategory().isModifiableByArtisanFoci())
        {
            return true;
        }
        if (group == null)
        {
            return false;
        }
        else
        {
            return group.size() <= 1 || group.getTags().contains("noLegendary");
        }
    }


    enum Roll
    {
        REROLL,
        ADD,
        NONE,
        IMPLICIT
    }
}

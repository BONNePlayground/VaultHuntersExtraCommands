//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.*;
import java.util.stream.Collectors;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;
import iskallia.vault.gear.*;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.config.*;
import iskallia.vault.gear.attribute.custom.effect.EffectAvoidanceGearAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.world.data.DiscoveredModelsData;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.mixin.accessors.VaultGearTierConfigAccessor;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.EnumArgument;


public class GearDebugCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODEL = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateSuggestions(context),
            builder));

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIBUTES = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateAttributesSuggestions(context),
            builder));

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_VALUES = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateAttributeValuesSuggestions(context),
            builder));

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_EXISTING_MODIFIERS = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateExistingModifiersSuggestions(context),
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


    private static List<String> generateAttributesSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        VaultGearModifier.AffixType affix = context.getArgument("affix", VaultGearModifier.AffixType.class);

        Optional<VaultGearTierConfig> config = VaultGearTierConfig.getConfig(player.getMainHandItem());

        if (config.isPresent())
        {
            VaultGearTierConfig vaultGearTierConfig = config.get();

            VaultGearTierConfig.ModifierAffixTagGroup affixGroup =
                VaultGearTierConfig.ModifierAffixTagGroup.ofAffixType(affix);

            VaultGearTierConfig.AttributeGroup attributeGroup =
                ((VaultGearTierConfigAccessor) vaultGearTierConfig).getModifierGroup().get(affixGroup);

            return attributeGroup.stream().
                map(VaultGearTierConfig.ModifierTierGroup::getAttribute).
                map(ResourceLocation::toString).
                collect(Collectors.toList());
        }
        else
        {
            // Return every known attribute
            return VaultGearAttributeRegistry.getRegistry().getKeys().stream().
                map(ResourceLocation::toString).
                collect(Collectors.toList());
        }
    }


    private static List<String> generateAttributeValuesSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ResourceLocation modifier = ResourceLocationArgument.getId(context, "modifier");

        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(modifier);

        List<String> returnText = new ArrayList<>();

        if (attribute.getGenerator() instanceof FloatAttributeGenerator)
        {
            returnText.add("<float>");
        }
        else if (attribute.getGenerator() instanceof IntegerAttributeGenerator)
        {
            returnText.add("<integer>");
        }
        else if (attribute.getGenerator() instanceof DoubleAttributeGenerator)
        {
            returnText.add("<double>");
        }
        else if (attribute.getGenerator() instanceof BooleanFlagGenerator)
        {
            returnText.add("<boolean>");
        }
        else if (attribute.getGenerator() instanceof IdentityObjectGenerator<?>)
        {
            returnText.add("<text>");
        }
        else if (attribute.getGenerator().getClass().getName().equals("iskallia.vault.gear.attribute.custom.EffectAvoidanceGearAttribute$Generator"))
        {
            ForgeRegistries.MOB_EFFECTS.getKeys().forEach(key -> returnText.add("\"" + key.toString() + "\""));
        }
        else if (attribute.getGenerator().getClass().getName().equals("iskallia.vault.gear.attribute.ability.AbilityLevelAttribute$1"))
        {
            returnText.addAll(abilityIDs());
        }

        return returnText;
    }


    private static List<String> generateExistingModifiersSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        VaultGearModifier.AffixType affix = context.getArgument("affix", VaultGearModifier.AffixType.class);

        VaultGearData data = VaultGearData.read(player.getMainHandItem());

        if (data.getState() != VaultGearState.IDENTIFIED)
        {
            return Collections.emptyList();
        }

        List<String> returnList = new ArrayList<>();

        for (VaultGearModifier<?> modifier : data.getModifiers(affix))
        {
            VaultGearAttribute<?> attribute = modifier.getAttribute();

            if (attribute != null)
            {
                ResourceLocation registryName = attribute.getRegistryName();

                if (registryName != null)
                {
                    returnList.add(registryName.toString());
                }
            }
        }

        return returnList;
    }


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
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

        // Rarity
        LiteralArgumentBuilder<CommandSourceStack> rarity = Commands.literal("rarity").
            then(Commands.argument("roll", EnumArgument.enumArgument(VaultGearRarity.class)).
                executes(ctx -> setRarity(ctx.getSource().getPlayerOrException(), ctx.getArgument("roll", VaultGearRarity.class))));
        // Model
        LiteralArgumentBuilder<CommandSourceStack> model = Commands.literal("model").
            then(Commands.argument("resource", ResourceLocationArgument.id()).
                suggests(SUGGEST_MODEL).
                executes(ctx -> setModel(ctx.getSource().getPlayerOrException(), ResourceLocationArgument.getId(ctx, "resource"))));
        // Proficiency
        LiteralArgumentBuilder<CommandSourceStack> potential = Commands.literal("potential").
            then(Commands.argument("value", IntegerArgumentType.integer()).
                executes(ctx -> setPotential(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "value"))));
        //Add Modifier
        LiteralArgumentBuilder<CommandSourceStack> addModifier = Commands.literal("addModifier").
            then(Commands.argument("affix", EnumArgument.enumArgument(VaultGearModifier.AffixType.class)).
                executes(ctx -> addModifier(ctx.getSource().getPlayerOrException(),
                    false,
                    ctx.getArgument("affix", VaultGearModifier.AffixType.class), null)).
                then(Commands.argument("modifier", ResourceLocationArgument.id()).
                    suggests(SUGGEST_ATTRIBUTES).
                    then(Commands.argument("value_1", StringArgumentType.string()).
                        suggests(SUGGEST_VALUES).
                        executes(ctx -> addModifier(ctx.getSource().getPlayerOrException(),
                            false,
                            ctx.getArgument("affix", VaultGearModifier.AffixType.class),
                            ResourceLocationArgument.getId(ctx, "modifier"),
                            StringArgumentType.getString(ctx, "value_1"))).
                        then(Commands.argument("number", StringArgumentType.string()).
                            executes(ctx -> addModifier(ctx.getSource().getPlayerOrException(),
                                false,
                                ctx.getArgument("affix", VaultGearModifier.AffixType.class),
                                ResourceLocationArgument.getId(ctx, "modifier"),
                                StringArgumentType.getString(ctx, "value_1"),
                                StringArgumentType.getString(ctx, "number")))
                        )
                    )
                )
            );
        //Add Modifier by force
        LiteralArgumentBuilder<CommandSourceStack> addModifierBypass = Commands.literal("addModifierForce").
            then(Commands.argument("affix", EnumArgument.enumArgument(VaultGearModifier.AffixType.class)).
                executes(ctx -> addModifier(ctx.getSource().getPlayerOrException(),
                    true,
                    ctx.getArgument("affix", VaultGearModifier.AffixType.class), null)).
                then(Commands.argument("modifier", ResourceLocationArgument.id()).
                    suggests(SUGGEST_ATTRIBUTES).
                    then(Commands.argument("value_1", StringArgumentType.string()).
                        suggests(SUGGEST_VALUES).
                        executes(ctx -> addModifier(ctx.getSource().getPlayerOrException(),
                            true,
                            ctx.getArgument("affix", VaultGearModifier.AffixType.class),
                            ResourceLocationArgument.getId(ctx, "modifier"),
                            StringArgumentType.getString(ctx, "value_1"))).
                        then(Commands.argument("number", StringArgumentType.string()).
                            executes(ctx -> addModifier(ctx.getSource().getPlayerOrException(),
                                true,
                                ctx.getArgument("affix", VaultGearModifier.AffixType.class),
                                ResourceLocationArgument.getId(ctx, "modifier"),
                                StringArgumentType.getString(ctx, "value_1"),
                                StringArgumentType.getString(ctx, "number")))
                        )
                    )
                )
            );
        //Add Modifier
        LiteralArgumentBuilder<CommandSourceStack> removeModifier = Commands.literal("removeModifier").
            then(Commands.argument("affix", EnumArgument.enumArgument(VaultGearModifier.AffixType.class)).
                executes(ctx -> removeModifier(ctx.getSource().getPlayerOrException(), ctx.getArgument("affix", VaultGearModifier.AffixType.class), null)).
                then(Commands.argument("modifier", ResourceLocationArgument.id()).
                    suggests(SUGGEST_EXISTING_MODIFIERS).
                    executes(ctx -> removeModifier(ctx.getSource().getPlayerOrException(),
                        ctx.getArgument("affix", VaultGearModifier.AffixType.class),
                        ResourceLocationArgument.getId(ctx, "modifier")))
                )
            );

        dispatcher.register(baseLiteral.then(gearDebug.
            then(legendary).
            then(rarity).
            then(model).
            then(potential).
            then(addModifier).
            then(addModifierBypass).
            then(removeModifier).
            then(repair.then(breakGear).then(fixGear).then(setSlots))));
    }


    private static int removeModifier(ServerPlayer player, VaultGearModifier.AffixType type, ResourceLocation resourceLocation)
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
            player.sendMessage(new TextComponent("Only identified gear can change its attributes."),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        if (resourceLocation == null)
        {
            List<VaultGearModifier<?>> modifiers = new ArrayList<>(data.getModifiers(type));
            modifiers.removeIf(modifier -> !modifier.hasNoCategoryMatching(VaultGearModifier.AffixCategory::cannotBeModifiedByArtisanFoci));

            if (!modifiers.isEmpty())
            {
                VaultGearModifier<?> randomMod = MiscUtils.getRandomEntry(modifiers, new Random());
                data.removeModifier(randomMod);
                data.write(mainHandItem);

                player.sendMessage(new TextComponent("The " + type.name() + " removed from gear"),
                    net.minecraft.Util.NIL_UUID);
            }
        }
        else
        {
            List<VaultGearModifier<?>> modifiers = new ArrayList<>(data.getModifiers(type));
            modifiers.removeIf(modifier -> modifier.getAttribute() == null ||
                !resourceLocation.equals(modifier.getAttribute().getRegistryName()));

            if (!modifiers.isEmpty())
            {
                VaultGearModifier<?> randomMod = MiscUtils.getRandomEntry(modifiers, new Random());
                data.removeModifier(randomMod);
                data.write(mainHandItem);

                player.sendMessage(new TextComponent("The " + type.name() + " removed from gear"),
                    net.minecraft.Util.NIL_UUID);
                ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " removed modifier from gear!");
            }
            else
            {
                player.sendMessage(new TextComponent("Cannot find " + resourceLocation + " in " + type.name()),
                    net.minecraft.Util.NIL_UUID);
            }
        }

        return 1;
    }


    private static int addModifier(ServerPlayer player,
        boolean byPassLimit,
        VaultGearModifier.AffixType type,
        ResourceLocation modifier,
        String... value)
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
            player.sendMessage(new TextComponent("Only identified gear can change its attributes."),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        if (modifier == null)
        {
            if (type == VaultGearModifier.AffixType.IMPLICIT)
            {
                player.sendMessage(new TextComponent("Only identified gear can change its attributes."),
                    net.minecraft.Util.NIL_UUID);
                throw new IllegalArgumentException("You cannot add implicits");
            }

            VaultGearModifierHelper.generateModifiersOfAffix(mainHandItem, type, new Random());
            return 1;
        }

        if (!byPassLimit)
        {
            if (type == VaultGearModifier.AffixType.IMPLICIT)
            {
                player.sendMessage(new TextComponent("Cannot add more implicits!"),
                    net.minecraft.Util.NIL_UUID);
                return 0;
            }
            else if (type == VaultGearModifier.AffixType.PREFIX && !VaultGearModifierHelper.hasOpenPrefix(mainHandItem))
            {
                player.sendMessage(new TextComponent("Cannot add more prefixes!"),
                    net.minecraft.Util.NIL_UUID);
                return 0;
            }
            else if (type == VaultGearModifier.AffixType.SUFFIX && !VaultGearModifierHelper.hasOpenSuffix(mainHandItem))
            {
                player.sendMessage(new TextComponent("Cannot add more suffixes!"),
                    net.minecraft.Util.NIL_UUID);
                return 0;
            }
        }

        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(modifier);

        if (attribute == null)
        {
            throw new IllegalArgumentException("Unknown attribute: " + modifier);
        }

        boolean added;

        if (attribute.getGenerator() instanceof FloatAttributeGenerator)
        {
            added = data.addModifier(type, new VaultGearModifier<>((VaultGearAttribute<Float>) attribute, Float.parseFloat(value[0])));
        }
        else if (attribute.getGenerator() instanceof IntegerAttributeGenerator)
        {
            added = data.addModifier(type, new VaultGearModifier<>((VaultGearAttribute<Integer>) attribute, Integer.parseInt(value[0])));
        }
        else if (attribute.getGenerator() instanceof DoubleAttributeGenerator)
        {
            added = data.addModifier(type, new VaultGearModifier<>((VaultGearAttribute<Double>) attribute, Double.parseDouble(value[0])));
        }
        else if (attribute.getGenerator() instanceof BooleanFlagGenerator)
        {
            added = data.addModifier(type, new VaultGearModifier<>((VaultGearAttribute<Boolean>) attribute, Boolean.parseBoolean(value[0])));
        }
        else if (attribute.getGenerator() instanceof IdentityObjectGenerator<?>)
        {
            added = data.addModifier(type, new VaultGearModifier<>((VaultGearAttribute<String>) attribute, value[0]));
        }
        else if (attribute.getGenerator().getClass().getName().equals("iskallia.vault.gear.attribute.custom.EffectAvoidanceGearAttribute$Generator"))
        {
            // Because that is how true coders does it :D
            String[] split = value[0].split(":");

            if (split.length != 2)
            {
                throw new IllegalArgumentException("Could not get the correct mob effect. The format example: \"minecraft:slowness\"");
            }

            ResourceLocation resourceLocation = new ResourceLocation(split[0] + ":" + split[1]);
            float chance = Float.parseFloat(value[1]);

            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(resourceLocation);
            EffectAvoidanceGearAttribute effectAttribute = new EffectAvoidanceGearAttribute(mobEffect, chance);

            added = data.addModifier(type, new VaultGearModifier<>((VaultGearAttribute<EffectAvoidanceGearAttribute>) attribute, effectAttribute));
        }
        else if (attribute.getGenerator().getClass().getName().equals("iskallia.vault.gear.attribute.ability.AbilityLevelAttribute$1"))
        {
            if (!abilityIDs().contains(value[0]))
            {
                throw new IllegalArgumentException("Could not get the correct ability name. Check if you wrote it correctly!");
            }

            AbilityLevelAttribute abilityAttribute = new AbilityLevelAttribute(value[0], Integer.parseInt(value[1]));

            added = data.addModifier(type, new VaultGearModifier<>((VaultGearAttribute<AbilityLevelAttribute>) attribute, abilityAttribute));
        }
        else
        {
            throw new IllegalArgumentException("I do not know how to add it. Ask BONNe to add it!!! " + modifier.toString());
        }

        if (added)
        {
            data.write(mainHandItem);

            Util.sendGodMessageToPlayer(player,
                new TextComponent("Your blessing has worked. Modifier has been added to your gear!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " added modifier to the gear!");
        }
        else
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("I could not add this modifier. Are you sure you pick correct modifier?").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }

        return 1;
    }


    private static Set<String> abilityIDs()
    {
        return ModConfigs.ABILITIES.get().orElseThrow().skills.stream().
            filter(skill -> skill instanceof SpecializedSkill).
            map(skill -> (SpecializedSkill) skill).
            flatMap(spec -> spec.getSpecializations().stream()).
            map(Skill::getId).
            collect(Collectors.toSet());
    }


    private static int setPotential(ServerPlayer player, int value)
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
            player.sendMessage(new TextComponent("Only identified gear can change its potential."),
                net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not identified vaultgear in hand");
        }

        int oldPotential = data.getFirstValue(ModGearAttributes.CRAFTING_POTENTIAL).orElse(0);

        data.createOrReplaceAttributeValue(ModGearAttributes.CRAFTING_POTENTIAL, value);
        data.write(mainHandItem);

        Util.sendGodMessageToPlayer(player,
            new TextComponent("Your crafting potential for this gear piece changed from " + oldPotential + " to " + value + "!").
                withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " changed crafting potential!");

        return 1;
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

        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, model);
        data.write(mainHandItem);

        Util.sendGodMessageToPlayer(player,
            new TextComponent("I updated your gear model as requested.").
                withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " changed gear model!");

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
        ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " changed gear rarity!");

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
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " decreased repair slot count!");
        }
        else
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("Your tool impresses me! You can use is a bit longer now!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " increased repair slot count!");
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

                ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " broke repair slot!");
            }
            else
            {
                Util.sendGodMessageToPlayer(player,
                    new TextComponent("Your good behaviour resulted inspired me! I fixed your tool!").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

                ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " fixed repair slot!");
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
            VaultGearLegendaryHelper.generateImprovedModifier(mainHandItem,
                2,
                GearRollHelper.rand,
                List.of(VaultGearModifier.AffixCategory.LEGENDARY));

            Util.sendGodMessageToPlayer(player,
                new TextComponent("Your tool has been blessed!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));

            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " applied legendary modifier!");
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
            newMod.addCategory(VaultGearModifier.AffixCategory.LEGENDARY);

            if (data.removeModifier(legendary.getB()))
            {
                data.addModifierFirst(legendary.getA(), newMod);

                Util.sendGodMessageToPlayer(player,
                    new TextComponent("Your tool has been blessed!").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));

                ExtraCommands.LOGGER.info(player.getDisplayName().getString() +
                    " applied legendary modifier!");
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
            filter(mod -> mod.getB().hasCategory(VaultGearModifier.AffixCategory.LEGENDARY)).
            findAny();
    }


    private static boolean shouldRemove(VaultGearModifier<?> modifier, VaultGearTierConfig config)
    {
        VaultGearTierConfig.ModifierTierGroup group = config.getTierGroup(modifier.getModifierIdentifier());

        if (modifier.hasNoCategoryMatching(VaultGearModifier.AffixCategory::cannotBeModifiedByArtisanFoci))
        {
            return true;
        }
        else if (group == null)
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

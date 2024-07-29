package lv.id.bonne.vaulthunters.extracommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.Optional;
import java.util.stream.Collectors;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;


/**
 * This class adds extra rules command for clients
 */
public class ModifiersCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = ((context, builder) ->
        SharedSuggestionProvider.suggestResource(VaultModifierRegistry.getAll().
            map(VaultModifier::getId).
            collect(Collectors.toList()), builder));

    /**
     * Registers the command.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("vault");

        LiteralArgumentBuilder<CommandSourceStack> baseAdd = Commands.literal("addModifier");
        LiteralArgumentBuilder<CommandSourceStack> baseRemove = Commands.literal("removeModifier");

        // Add effects.

        baseAdd.then(Commands.literal("positive").
            executes(ctx -> positiveEffect(ctx.getSource().getPlayerOrException().getLevel(), 1, true)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> positiveEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1, true)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> positiveEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count"), true))
                )
            )
        );

        baseAdd.then(Commands.literal("negative").
            executes(ctx -> negativeEffect(ctx.getSource().getPlayerOrException().getLevel(), 1, true)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> negativeEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1, true)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> negativeEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count"), true))
                )
            )
        );

        baseAdd.then(Commands.literal("curse").
            executes(ctx -> curseEffect(ctx.getSource().getPlayerOrException().getLevel(), 1, true)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> curseEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1, true)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> curseEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count"), true))
                )
            )
        );

        baseAdd.then(Commands.literal("chaotic").
            executes(ctx -> chaoticEffect(ctx.getSource().getPlayerOrException().getLevel(), 1)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> chaoticEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> chaoticEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count")))
                )
            )
        );

        baseAdd.then(Commands.literal("specific").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("modifier", ResourceLocationArgument.id()).
                    suggests(SUGGEST_MODIFIER).
                    executes(ctx -> specificEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), ResourceLocationArgument.getId(ctx, "modifier"), 1, true)).
                    then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                        executes(ctx -> specificEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), ResourceLocationArgument.getId(ctx, "modifier"), IntegerArgumentType.getInteger(ctx, "count"), true))
                    )
                )
            )
        );

        // Remove effects.

        baseRemove.then(Commands.literal("positive").
            executes(ctx -> positiveEffect(ctx.getSource().getPlayerOrException().getLevel(), 1, false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> positiveEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1, false)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> positiveEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count"), false))
                )
            )
        );

        baseRemove.then(Commands.literal("negative").
            executes(ctx -> negativeEffect(ctx.getSource().getPlayerOrException().getLevel(), 1, false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> negativeEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1, false)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> negativeEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count"), false))
                )
            )
        );

        baseRemove.then(Commands.literal("curse").
            executes(ctx -> curseEffect(ctx.getSource().getPlayerOrException().getLevel(), 1, false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> curseEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1, false)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> curseEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count"), false))
                )
            )
        );

        baseRemove.then(Commands.literal("specific").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("modifier", ResourceLocationArgument.id()).
                    suggests(SUGGEST_MODIFIER).
                    executes(ctx -> specificEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), ResourceLocationArgument.getId(ctx, "modifier"), 1, false)).
                    then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                        executes(ctx -> specificEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), ResourceLocationArgument.getId(ctx, "modifier"), IntegerArgumentType.getInteger(ctx, "count"), false))
                    )
                )
            )
        );

        baseRemove.then(Commands.literal("random").
            executes(ctx -> appliedEffect(ctx.getSource().getPlayerOrException().getLevel(), 1)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> appliedEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), 1)).
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> appliedEffect(EntityArgument.getPlayer(ctx, "player").getLevel(), IntegerArgumentType.getInteger(ctx, "count")))
                )
            )
        );

        dispatcher.register(baseLiteral.then(vaultLiteral.then(baseAdd).then(baseRemove)));
    }


    /**
     * This method adds (or removes) positive effect to the given Vault.
     * @param source The server level
     * @param amount Amount of effects
     * @param add add or remove
     * @return 1
     */
    private static int positiveEffect(ServerLevel source, int amount, boolean add)
    {
        for (int i = 0; i < amount; i++)
        {
            Util.getRandom(ExtraCommands.CONFIGURATION.getPositiveModifiers().stream().
                filter(modifier -> !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier)).toList()).
                map(VaultModifierRegistry::getOpt).
                flatMap(v -> v).
                ifPresent(rl -> effect(rl, add, 1, source.getLevel()));
        }

        return 1;
    }


    /**
     * This method adds (or removes) negative effect to the given Vault.
     * @param source The server level
     * @param amount Amount of effects
     * @param add add or remove
     * @return 1
     */
    private static int negativeEffect(ServerLevel source, int amount, boolean add)
    {
        for (int i = 0; i < amount; i++)
        {
            Util.getRandom(ExtraCommands.CONFIGURATION.getNegativeModifiers().stream().
                filter(modifier -> !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier)).toList()).
                map(VaultModifierRegistry::getOpt).
                flatMap(v -> v).
                ifPresent(rl -> effect(rl, add, 1, source.getLevel()));
        }

        return 1;
    }


    /**
     * This method adds (or removes) curse effect to the given Vault.
     * @param source The server level
     * @param amount Amount of effects
     * @param add add or remove
     * @return 1
     */
    private static int curseEffect(ServerLevel source, int amount, boolean add)
    {
        for (int i = 0; i < amount; i++)
        {
            Util.getRandom(ExtraCommands.CONFIGURATION.getCurseModifiers().stream().
                filter(modifier -> !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier)).toList()).
                map(VaultModifierRegistry::getOpt).
                flatMap(v -> v).
                ifPresent(rl -> effect(rl, add, 1, source.getLevel()));
        }

        return 1;
    }


    /**
     * This method adds random effect from chaotic list.
     * @param source The server level
     * @param amount Amount of effects
     * @return 1
     */
    private static int chaoticEffect(ServerLevel source, int amount)
    {
        for (int i = 0; i < amount; i++)
        {
            Util.getRandom(ExtraCommands.CONFIGURATION.getChaoticModifiers().stream().
                filter(modifier -> !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier)).toList()).
                map(VaultModifierRegistry::getOpt).
                flatMap(v -> v).
                ifPresent(rl -> effect(rl, true, 1, source.getLevel()));
        }

        return 1;
    }


    /**
     * This method adds specific effect to the Vault.
     * @param source The server level
     * @param modifier Modifier that will be added
     * @param amount Amount of effects
     * @param add add or remove
     * @return 1
     */
    private static int specificEffect(ServerLevel source, ResourceLocation modifier, int amount, boolean add)
    {
        if (!ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier))
        {
            VaultModifierRegistry.getOpt(modifier).
                ifPresent(rl -> effect(rl, add, amount, source.getLevel()));
        }
        else
        {
            ExtraCommands.LOGGER.warn("Tried to " + (add ? "add" : "remove") + " protected modifier!");
        }

        return 1;
    }


    /**
     * This method removes applied effect from the Vault.
     * @param source The server level
     * @param amount Amount of effects
     * @return 1
     */
    private static int appliedEffect(ServerLevel source, int amount)
    {
        ServerVaults.get(source).ifPresentOrElse(vault -> {
            vault.ifPresent(Vault.MODIFIERS, modifiers -> {
                for (int i = 0; i < amount; i++)
                {
                    Util.getRandom(modifiers.getModifiers().stream().
                            filter(modifier -> !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier.getId())).toList()).
                        ifPresentOrElse(vaultModifier -> {
                            effect(vaultModifier, false, 1, source);
                        },  Util.logError("Could not find modifier to remove."));
                }
            });
        }, Util.logError("The requested world does not contain Vault!"));

        return 1;
    }


    /**
     * This method adds given vault modifier to the vault.
     * @param effect VaultModifier to be added
     * @param add boolean that indicate if effect should be added or removed
     * @param count Number of effects to be added
     * @param level Level where it happens.
     */
    private static void effect(VaultModifier<?> effect, boolean add, int count, ServerLevel level)
    {
        boolean good = isPositiveModifier(effect);

        ServerVaults.get(level).
            flatMap(vault -> vault.getOptional(Vault.MODIFIERS)).
            ifPresentOrElse(modifiers ->
            {
                if (add)
                {
                    modifiers.addModifier(effect, count, true, JavaRandom.ofInternal(0));

                    Component component;

                    if (good)
                    {
                        component = new TextComponent("You are blessed with " + (count > 1 ? count + " " : "")).
                            withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).
                            append(new TextComponent(effect.getDisplayName()).
                                withStyle(style -> Style.EMPTY.
                                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponent(effect.getDisplayDescription()))).
                                    withColor(effect.getDisplayTextColor())));
                    }
                    else
                    {
                        component = new TextComponent("You are punished by adding " + (count > 1 ? count + " " : "")).
                            withStyle(Style.EMPTY.withColor(ChatFormatting.RED)).
                            append(new TextComponent(effect.getDisplayName()).
                                withStyle(style -> Style.EMPTY.
                                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponent(effect.getDisplayDescription()))).
                                    withColor(effect.getDisplayTextColor())));
                    }

                    Util.sendGodMessageToAll(level, component);
                }
                else
                {
                    Optional<Modifiers.Entry> anyMatchingModifier = modifiers.getEntries().stream().
                        filter(entry -> entry.getModifier().map(mod -> mod.equals(effect)).orElse(false)).
                        findAny();

                    anyMatchingModifier.ifPresentOrElse(entry -> {
                        modifiers.getEntries().remove(entry);

                        Component component;

                        if (!good)
                        {
                            component = new TextComponent("You are blessed by removing " + (count > 1 ? count + " " : "")).
                                withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).
                                append(new TextComponent(effect.getDisplayName()).
                                    withStyle(style -> Style.EMPTY.
                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new TextComponent(effect.getDisplayDescription()))).
                                        withColor(effect.getDisplayTextColor())));
                        }
                        else
                        {
                            component = new TextComponent("You are punished by removing " + (count > 1 ? count + " " : "")).
                                withStyle(Style.EMPTY.withColor(ChatFormatting.RED)).
                                append(new TextComponent(effect.getDisplayName()).
                                    withStyle(style -> Style.EMPTY.
                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new TextComponent(effect.getDisplayDescription()))).
                                        withColor(effect.getDisplayTextColor())));
                        }

                        Util.sendGodMessageToAll(level, component);
                    }, Util.logError("Could not find any matching modifiers to vault: " + effect.getDisplayName()));
                }
            }, Util.logError("Given dimension does not have Vault."));
    }


    /**
     * Returns if given effect is in Positive effect set.
     * @param effect Effect that need to be checked.
     * @return true if effect is in positive configuration set.
     */
    private static boolean isPositiveModifier(VaultModifier<?> effect)
    {
        return ExtraCommands.CONFIGURATION.getPositiveModifiers().contains(effect.getId());
    }
}

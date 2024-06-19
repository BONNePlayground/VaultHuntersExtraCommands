package lv.id.bonne.vaulthunters.extracommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.HoverEvent;
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
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> baseAdd = Commands.literal("addModifier");
        LiteralArgumentBuilder<CommandSourceStack> baseRemove = Commands.literal("removeModifier");

        baseAdd.then(Commands.literal("positive").
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> positiveEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("count", Integer.class),
                        true))).
                executes(ctx -> positiveEffect(ctx.getSource().getLevel(),
                    1,
                    true))).
            then(Commands.literal("negative").then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> negativeEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("count", Integer.class),
                        true))).
                executes(ctx -> negativeEffect(ctx.getSource().getLevel(),
                    1,
                    true))).
            then(Commands.literal("curse").then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> curseEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("count", Integer.class),
                        true))).
                executes(ctx -> curseEffect(ctx.getSource().getLevel(),
                    1,
                    true))).
            then(Commands.literal("chaotic").then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                executes(ctx -> chaoticEffect(ctx.getSource().getLevel(), ctx.getArgument("count", Integer.class))))).
            then(Commands.literal("specific").
                then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).
                    then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                        executes(ctx -> specificEffect(ctx.getSource().getLevel(),
                            ctx.getArgument("modifier", ResourceLocation.class),
                            ctx.getArgument("count", Integer.class),
                            true))).
                    executes(ctx -> specificEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("modifier", ResourceLocation.class),
                        1,
                        true))));

        baseRemove.then(Commands.literal("positive").
                then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> positiveEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("count", Integer.class),
                        false))).
                executes(ctx -> positiveEffect(ctx.getSource().getLevel(),
                    1,
                    false))).
            then(Commands.literal("negative").then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> negativeEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("count", Integer.class),
                        false))).
                executes(ctx -> negativeEffect(ctx.getSource().getLevel(),
                    1,
                    false))).
            then(Commands.literal("curse").then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> curseEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("count", Integer.class),
                        false))).
                executes(ctx -> curseEffect(ctx.getSource().getLevel(),
                    1,
                    false))).
            then(Commands.literal("specific").
                then(Commands.argument("modifier", ResourceLocationArgument.id()).
                    suggests(SUGGEST_MODIFIER).
                    then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                        executes(ctx -> specificEffect(ctx.getSource().getLevel(),
                            ctx.getArgument("modifier", ResourceLocation.class),
                            ctx.getArgument("count", Integer.class),
                            false))).
                    executes(ctx -> specificEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("modifier", ResourceLocation.class),
                        1,
                        false)))).
            then(Commands.literal("random").then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).
                    executes(ctx -> appliedEffect(ctx.getSource().getLevel(),
                        ctx.getArgument("count", Integer.class)))).
                executes(ctx -> appliedEffect(ctx.getSource().getLevel(),
                    1)));

        dispatcher.register(baseLiteral.then(baseAdd).then(baseRemove));
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
            getRandom(ExtraCommands.CONFIGURATION.getPositiveModifiers().stream().
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
            getRandom(ExtraCommands.CONFIGURATION.getNegativeModifiers().stream().
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
            getRandom(ExtraCommands.CONFIGURATION.getCurseModifiers().stream().
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
            getRandom(ExtraCommands.CONFIGURATION.getChaoticModifiers().stream().
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
        ServerVaults.get(source).ifPresent(vault -> {
            vault.ifPresent(Vault.MODIFIERS, modifiers -> {
                for (int i = 0; i < amount; i++)
                {
                    getRandom(modifiers.getModifiers().stream().filter(modifier ->
                        !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier.getId())).toList()).
                        ifPresent(vaultModifier -> {
                            effect(vaultModifier, false, 1, source);
                        });
                }
            });
        });

        return 1;
    }


    /**
     * This method returns random element from given list.
     * @param input List of input elements.
     * @return Optional of random element from list.
     */
    private static <T> Optional<T> getRandom(List<T> input)
    {
        int count = (int) (input.size() * Math.random());

        return input.stream().skip(count).findAny();
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
        ServerVaults.get(level).
            flatMap(vault -> vault.getOptional(Vault.MODIFIERS)).
            ifPresent(modifiers ->
            {
                if (add)
                {
                    modifiers.addModifier(effect, count, true, JavaRandom.ofInternal(0));
                    sendMessage(level, "You are blessed with " + (count > 1 ? count + " " : "") + effect.getDisplayName());
                }
                else
                {
                    Optional<Modifiers.Entry> anyMatchingModifier = modifiers.getEntries().stream().
                        filter(entry -> entry.getModifier().
                            map(mod -> mod.equals(effect)).orElse(false)).
                        findAny();

                    anyMatchingModifier.ifPresent(entry -> {
                        modifiers.getEntries().remove(entry);

                        entry.getModifier().ifPresent(modifier -> {
                            sendMessage(level, "You are punished by removing " + (count > 1 ? count + " " : "") + modifier.getDisplayName());
                        });
                    });
                }
            });
    }


    /**
     * This method sends all players in given level given message.
     * @param level Level that receives message.
     * @param text The message.
     */
    private static void sendMessage(ServerLevel level, String text)
    {
        getRandom(Arrays.stream(VaultGod.values()).toList()).ifPresent(sender ->
        {
            TextComponent senderTxt = new TextComponent("[VG] ");

            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                append((new TextComponent(sender.getName())).withStyle(sender.getChatColor())).
                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));

            senderTxt.withStyle(style ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sender.getHoverChatComponent())));

            level.players().forEach(player -> player.sendMessage(
                senderTxt.append(new TextComponent(text).withStyle(ChatFormatting.WHITE)), ChatType.SYSTEM, Util.NIL_UUID));
        });
    }
}

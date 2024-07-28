//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;

import iskallia.vault.config.AlchemyTableConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.DiscoveredAlchemyEffectsData;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;


public class AlchemyUnlockCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateSuggestions(context),
            builder));


    private static List<String> generateSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");

        DiscoveredAlchemyEffectsData discoveredEffectsData = DiscoveredAlchemyEffectsData.get(player.getLevel());

        return ModConfigs.VAULT_ALCHEMY_TABLE.getCraftableEffects().
            stream().
            filter(effectCfg -> (effectCfg.getUnlockCategory() == AlchemyTableConfig.UnlockCategory.VAULT_DISCOVERY &&
                !discoveredEffectsData.hasDiscoveredEffect(player, effectCfg.getEffectId()))).
            map(AlchemyTableConfig.CraftableEffectConfig::getEffectId).
            toList();
    }


    /**
     * Registers the command that toggles a pause for the vault.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
            requires(stack -> stack.hasPermission(1));

        LiteralArgumentBuilder<CommandSourceStack> discovery = Commands.literal("discovery");

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("alchemy").
            then(Commands.argument("player", EntityArgument.players()).
                then(Commands.argument("effect", StringArgumentType.word()).
                    suggests(SUGGESTIONS).
                    executes(ctx -> unlockEffect(EntityArgument.getPlayer(ctx, "player"),
                        StringArgumentType.getString(ctx, "effect")))
                )
            );

        dispatcher.register(baseLiteral.then(discovery.then(command)));
    }


    private static int unlockEffect(ServerPlayer player, String effect)
    {
        AlchemyTableConfig.CraftableEffectConfig effectCfg = ModConfigs.VAULT_ALCHEMY_TABLE.getConfig(effect);

        if (effectCfg == null)
        {
            ExtraCommands.LOGGER.error("Something went worng with unlocking " + effect + " in alchemy table!");
            return 1;
        }

        DiscoveredAlchemyEffectsData effectsData = DiscoveredAlchemyEffectsData.get(player.getLevel());

        if (!effectsData.hasDiscoveredEffect(player, effect))
        {
            effectsData.compoundDiscoverEffect(player, effect);

            Util.sendGodMessageToPlayer(player,
                new TextComponent("We decided to bless you with knowledge. You now know how to get ").
                    append((new TextComponent(effectCfg.getEffectName())).
                        setStyle(Style.EMPTY.withColor(effectCfg.getColor())).
                        append(" in alchemy table!")).
                    withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " unlocked " +
                effect + " in alchemy table!");
        }
        else
        {
            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " already knows " +
                effect + " in alchemy table!");
        }

        return 1;
    }
}

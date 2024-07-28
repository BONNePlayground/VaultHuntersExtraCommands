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
import java.util.stream.Collectors;

import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.objective.*;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;


public class CrystalCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_THEME = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateThemeSuggestions(context),
            builder));


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> crystalLiteral = Commands.literal("crystal");

        LiteralArgumentBuilder<CommandSourceStack> objective = Commands.literal("setObjective").
            then(Commands.argument("objective", EnumArgument.enumArgument(Objective.class)).
                executes(ctx -> setObjective(ctx.getSource().getPlayerOrException(), ctx.getArgument("objective", Objective.class))));

        LiteralArgumentBuilder<CommandSourceStack> theme = Commands.literal("setTheme").
            then(Commands.argument("theme", ResourceLocationArgument.id()).
                suggests(SUGGEST_THEME).
                executes(ctx -> setTheme(ctx.getSource().getPlayerOrException(), ResourceLocationArgument.getId(ctx, "theme"))));

        dispatcher.register(baseLiteral.then(crystalLiteral.then(objective).then(theme)));
    }


    private static int setObjective(ServerPlayer player, Objective objective)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultCrystalItem))
        {
            player.sendMessage(new TextComponent("No crystal held in hand"), net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not crystal in hand");
        }

        CrystalData data = CrystalData.read(mainHandItem);
        data.setObjective(objective.getObjective());
        data.write(mainHandItem);

        player.sendMessage(new TextComponent("Crystal objective changed"), net.minecraft.Util.NIL_UUID);

        return 1;
    }


    private static int setTheme(ServerPlayer player, ResourceLocation theme)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultCrystalItem))
        {
            player.sendMessage(new TextComponent("No crystal held in hand"), net.minecraft.Util.NIL_UUID);
            throw new IllegalArgumentException("Not crystal in hand");
        }

        CrystalData data = CrystalData.read(mainHandItem);
        data.setTheme(new ValueCrystalTheme(theme));
        data.write(mainHandItem);

        player.sendMessage(new TextComponent("Crystal theme changed"), net.minecraft.Util.NIL_UUID);

        return 1;
    }


    private static List<String> generateThemeSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        return VaultRegistry.THEME.getKeys().stream().
            map(ThemeKey::getId).
            map(ResourceLocation::toString).
            collect(Collectors.toList());
    }


    enum Objective
    {
        ASCENSION(new AscensionCrystalObjective()),
        BOSS(new BossCrystalObjective()),
        CAKE(new CakeCrystalObjective()),
        COMPOUND(new CompoundCrystalObjective()),
        ELIXIR(new ElixirCrystalObjective()),
        EMPTY(new EmptyCrystalObjective()),
        HERALD(new HeraldCrystalObjective()),
        MONOLITH(new MonolithCrystalObjective()),
        NULL(NullCrystalObjective.INSTANCE),
        PARADOX(new ParadoxCrystalObjective()),
        POLL(new PoolCrystalObjective()),
        SCAVENGER(new ScavengerCrystalObjective()),
        SPEEDRUN(new SpeedrunCrystalObjective());

        Objective(CrystalObjective objective)
        {
            this.objective = objective;
        }


        public CrystalObjective getObjective()
        {
            return objective;
        }


        private final CrystalObjective objective;
    }
}
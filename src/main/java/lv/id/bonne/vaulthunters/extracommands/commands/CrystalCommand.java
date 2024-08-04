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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.layout.*;
import iskallia.vault.item.crystal.objective.*;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.server.command.EnumArgument;


public class CrystalCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_THEME = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateThemeSuggestions(context),
            builder));

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_NBT = ((context, builder) ->
        SharedSuggestionProvider.suggest(
            generateNBTSuggestions(context),
            builder));


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> crystalLiteral = Commands.literal("crystal");

        LiteralArgumentBuilder<CommandSourceStack> objective = Commands.literal("setObjective").
            then(Commands.argument("objective", EnumArgument.enumArgument(Objective.class)).
                executes(ctx -> setObjective(ctx.getSource().getPlayerOrException(), ctx.getArgument("objective", Objective.class), new CompoundTag())).
                then(Commands.argument("parameters", NbtTagArgument.nbtTag()).
                    suggests(SUGGEST_NBT).
                    executes(ctx -> setObjective(ctx.getSource().getPlayerOrException(),
                        ctx.getArgument("objective", Objective.class),
                        NbtTagArgument.getNbtTag(ctx, "parameters")))));

        LiteralArgumentBuilder<CommandSourceStack> theme = Commands.literal("setTheme").
            then(Commands.argument("theme", ResourceLocationArgument.id()).
                suggests(SUGGEST_THEME).
                executes(ctx -> setTheme(ctx.getSource().getPlayerOrException(), ResourceLocationArgument.getId(ctx, "theme"))));

        LiteralArgumentBuilder<CommandSourceStack> layout = Commands.literal("setLayout").
            then(Commands.argument("layout", EnumArgument.enumArgument(Layout.class)).
                executes(ctx -> setLayout(ctx.getSource().getPlayerOrException(), ctx.getArgument("layout", Layout.class))).
                then(Commands.argument("tunnelSpan", StringArgumentType.word()).
                    executes(ctx -> setLayout(ctx.getSource().getPlayerOrException(),
                        ctx.getArgument("layout", Layout.class),
                        StringArgumentType.getString(ctx, "tunnelSpan"))).
                    then(Commands.argument("radius", StringArgumentType.word()).
                        executes(ctx -> setLayout(ctx.getSource().getPlayerOrException(),
                            ctx.getArgument("layout", Layout.class),
                            StringArgumentType.getString(ctx, "tunnelSpan"),
                            StringArgumentType.getString(ctx, "radius"))).
                        then(Commands.argument("rotation", EnumArgument.enumArgument(Rotation.class)).
                            executes(ctx -> setLayout(ctx.getSource().getPlayerOrException(),
                                ctx.getArgument("layout", Layout.class),
                                StringArgumentType.getString(ctx, "tunnelSpan"),
                                StringArgumentType.getString(ctx, "radius"),
                                ctx.getArgument("rotation", Rotation.class).name()))
                        )
                    )
                )
            );

        dispatcher.register(baseLiteral.then(crystalLiteral.
            then(objective).
            then(theme).
            then(layout)));
    }


    private static int setObjective(ServerPlayer player, Objective objective, Tag parameters)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultCrystalItem))
        {
            player.sendMessage(new TextComponent("No crystal held in hand"), Util.NIL_UUID);
            throw new IllegalArgumentException("Not crystal in hand");
        }

        switch (objective)
        {
            case ASCENSION ->
            {
                if (!(parameters instanceof CompoundTag tag) || tag.isEmpty() || !tag.contains("stack"))
                {
                    player.sendMessage(new TextComponent("Missing parameters. Needs like `{stack:1b,player_name:name,player_uuid:UUID,modifiers:{}}`"), Util.NIL_UUID);
                    throw new IllegalArgumentException("Missing objective parameters");
                }
            }
            case BOSS ->
            {
                if (!(parameters instanceof CompoundTag tag) || tag.isEmpty() || !tag.contains("target") || !tag.contains("wave"))
                {
                    player.sendMessage(new TextComponent("Missing parameters. Needs like `{target:{min:1b,max:3b,type:\"uniform\"},wave:{count:4b,type:\"constant\"}}`"), Util.NIL_UUID);
                    throw new IllegalArgumentException("Missing objective parameters");
                }
            }
            case COMPOUND ->
            {
                if (!(parameters instanceof CompoundTag tag) || tag.isEmpty())
                {
                    player.sendMessage(new TextComponent("Missing parameters."), Util.NIL_UUID);
                    throw new IllegalArgumentException("Missing objective parameters");
                }
            }
            case MONOLITH ->
            {
                if (!(parameters instanceof CompoundTag tag) || tag.isEmpty() || !tag.contains("target"))
                {
                    player.sendMessage(new TextComponent("Missing parameters. Needs like `{target:{min:1b,max:3b,type:\"uniform\"}}`"), Util.NIL_UUID);
                    throw new IllegalArgumentException("Missing objective parameters");
                }
            }
            case PARADOX ->
            {
                if (!(parameters instanceof CompoundTag tag) || tag.isEmpty() || !tag.contains("goal"))
                {
                    player.sendMessage(new TextComponent("Missing parameters. Needs like `{goal:BUILD,player_name:name,player_uuid:UUID,expiry:0L,reset_timer:1}`"), Util.NIL_UUID);
                    throw new IllegalArgumentException("Missing objective parameters");
                }
            }
            case POLL ->
            {
                if (!(parameters instanceof CompoundTag tag) || tag.isEmpty() || !tag.contains("id"))
                {
                    player.sendMessage(new TextComponent("Missing parameters. Needs like `{id:resource}`"), Util.NIL_UUID);
                    throw new IllegalArgumentException("Missing objective parameters");
                }
            }
            case SPEEDRUN ->
            {
                if (!(parameters instanceof CompoundTag tag) || tag.isEmpty() || !tag.contains("target"))
                {
                    player.sendMessage(new TextComponent("Missing parameters. Needs like `{target:{count:1b,type:\"constant\"}`"), Util.NIL_UUID);
                    throw new IllegalArgumentException("Missing objective parameters");
                }
            }
        }

        CrystalData data = CrystalData.read(mainHandItem);

        if (parameters instanceof CompoundTag tag && !tag.isEmpty())
        {
            objective.getObjective().readNbt(tag);
        }

        data.setObjective(objective.getObjective());
        data.write(mainHandItem);

        player.sendMessage(new TextComponent("Crystal objective changed"), Util.NIL_UUID);

        return 1;
    }


    private static int setTheme(ServerPlayer player, ResourceLocation theme)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultCrystalItem))
        {
            player.sendMessage(new TextComponent("No crystal held in hand"), Util.NIL_UUID);
            throw new IllegalArgumentException("Not crystal in hand");
        }

        CrystalData data = CrystalData.read(mainHandItem);
        data.setTheme(new ValueCrystalTheme(theme));
        data.write(mainHandItem);

        player.sendMessage(new TextComponent("Crystal theme changed"), Util.NIL_UUID);

        return 1;
    }


    private static int setLayout(ServerPlayer player, Layout layoutID, String... args)
    {
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof VaultCrystalItem))
        {
            player.sendMessage(new TextComponent("No crystal held in hand"), Util.NIL_UUID);
            throw new IllegalArgumentException("Not crystal in hand");
        }

        CrystalLayout layout = switch (layoutID)
        {
            case ARCHITECT -> new ArchitectCrystalLayout();
            case CIRCLE -> {
                if (args.length == 0)
                {
                    yield new ClassicCircleCrystalLayout();
                }
                else
                {
                    yield new ClassicCircleCrystalLayout(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                }
            }
            case INFINITE ->
            {
                if (args.length == 0)
                {
                    yield new ClassicInfiniteCrystalLayout();
                }
                else
                {
                    yield new ClassicInfiniteCrystalLayout(Integer.parseInt(args[0]));
                }
            }
            case POLYGON -> new ClassicPolygonCrystalLayout();
            case SPIRAL ->
            {
                if (args.length != 3)
                {
                    yield new ClassicSpiralCrystalLayout();
                }
                else
                {
                    yield new ClassicSpiralCrystalLayout(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Rotation.valueOf(args[2].toUpperCase()));
                }
            }
            case COMPOUND -> new CompoundCrystalLayout();
            case HERALD -> new HeraldCrystalLayout();
            case NULL -> NullCrystalLayout.INSTANCE;
            case PARADOX -> new ParadoxCrystalLayout();
        };

        CrystalData data = CrystalData.read(mainHandItem);
        data.setLayout(layout);
        data.write(mainHandItem);

        player.sendMessage(new TextComponent("Crystal layout changed"), Util.NIL_UUID);

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


    private static List<String> generateNBTSuggestions(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        Objective objective = context.getArgument("objective", Objective.class);

        String returnMessage = switch (objective)
        {
            case ASCENSION ->  "{stack:1b,player_name:name,player_uuid:UUID,modifiers:{}}";
            case BOSS -> "{target:{min:1b,max:3b,type:\"uniform\"},wave:{count:4b,type:\"constant\"}}";
            case COMPOUND -> "{children:{<tag>}}";
            case MONOLITH -> "{target:{min:1b,max:3b,type:\"uniform\"}}";
            case PARADOX -> "{goal:BUILD,player_name:name,player_uuid:UUID,expiry:0L,reset_timer:1}";
            case POLL -> "{id:resource}";
            case SPEEDRUN -> "{target:{count:1b,type:\"constant\"}}";
            default -> "";
        };

        return Collections.singletonList(returnMessage);
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
        SPEEDRUN(new SpeedrunCrystalObjective(IntRoll.ofConstant(5), 0));

        Objective(CrystalObjective objective)
        {
            this.objective = objective;
        }


        public <T extends CrystalObjective> T getObjective()
        {
            return (T) objective;
        }


        private final CrystalObjective objective;
    }


    enum Layout
    {
        ARCHITECT,
        CIRCLE,
        INFINITE,
        POLYGON,
        SPIRAL,
        COMPOUND,
        HERALD,
        NULL,
        PARADOX
    }
}

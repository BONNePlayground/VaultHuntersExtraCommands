//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.*;

import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ClientboundHunterParticlesMessage;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.ServerScheduler;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.mixin.AngelBlockAccessor;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkDirection;


public class LocateAngelBlockCommand
{
    /**
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal(ExtraCommands.CONFIGURATION.getCommandTag()).
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("locate");

        LiteralArgumentBuilder<CommandSourceStack> complete = Commands.literal("angel_block").
            executes(ctx -> spamAngelBlocks(ctx.getSource().getPlayerOrException())).
            then(Commands.argument("distance", IntegerArgumentType.integer(1)).
                executes(ctx -> searchAngelBlocks(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"distance"))));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(complete)));
    }


    private static int spamAngelBlocks(ServerPlayer player)
    {
        Set<Object> angelBlocks = ((AngelBlockAccessor) ModBlocks.ANGEL_BLOCK).getAngelBlocks();;

        if (angelBlocks.isEmpty())
        {
            Util.sendGodMessageToPlayer(player,
                new TextComponent("I do not know what you are searching but it does not exist!").
                    withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        }

        try
        {
            Class<?> dimensionPosClass = Class.forName("iskallia.vault.block.AngelBlock$DimensionPos");
            // Get the fields of the DimensionPos record
            Field dimensionField = ObfuscationReflectionHelper.findField(dimensionPosClass, "dimension");
            Field posField = ObfuscationReflectionHelper.findField(dimensionPosClass, "pos");

            List<Pair<BlockPos, ResourceKey<Level>>> blockPairs = new ArrayList<>(angelBlocks.size());

            for (Object position : angelBlocks)
            {
                // Make fields accessible
                dimensionField.setAccessible(true);
                posField.setAccessible(true);

                // Get the field values
                ResourceKey<Level> dimension = (ResourceKey<Level>) dimensionField.get(position);
                BlockPos pos = (BlockPos) posField.get(position);

                blockPairs.add(new Pair<>(pos, dimension));
            }

            Vec3i playerPos = new Vec3i(player.getBlockX(), player.getBlockY(), player.getBlockZ());

            Util.sendGodMessageToPlayer(player, "I will share my knowledge about angel blocks:");

            blockPairs.stream().sorted(new PairComparator(playerPos)).
                forEachOrdered(pair -> {
                    MutableComponent message = new TextComponent("- ");

                    BlockPos pos = pair.getFirst();
                    ResourceKey<Level> dimension = pair.getSecond();

                    message.append((new TextComponent(pos.toShortString() + " in (" + dimension.location().getPath() + ")")).
                        withStyle((s) -> s.withColor(ChatFormatting.GREEN).
                            withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/execute in " + dimension.location() + " run teleport @p " + pos.getX() + " " + pos.getY() + " " + pos.getZ())).
                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponent("Click to teleport to angel block")))));

                    player.sendMessage(message, player.getUUID());
                });

            ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " searched lost angel blocks.");
        }
        catch (ClassNotFoundException | IllegalAccessException e)
        {
            ExtraCommands.LOGGER.error("There was some issues with displaying angel blocks. ", e);
        }

        return 1;
    }


    /**
     * @return 1
     */
    private static int searchAngelBlocks(ServerPlayer player, int distance)
    {
        Set<Object> angelBlocks = ((AngelBlockAccessor) ModBlocks.ANGEL_BLOCK).getAngelBlocks();

        try
        {
            Class<?> dimensionPosClass = Class.forName("iskallia.vault.block.AngelBlock$DimensionPos");
            // Get the fields of the DimensionPos record
            Field dimensionField = ObfuscationReflectionHelper.findField(dimensionPosClass, "dimension");
            Field posField = ObfuscationReflectionHelper.findField(dimensionPosClass, "pos");

            List<BlockPos> angelBlockLocations = new ArrayList<>(angelBlocks.size());

            for (Object position : angelBlocks)
            {
                // Make fields accessible
                dimensionField.setAccessible(true);
                posField.setAccessible(true);

                // Get the field values
                ResourceKey<Level> dimension = (ResourceKey<Level>) dimensionField.get(position);
                BlockPos pos = (BlockPos) posField.get(position);

                if (player.getLevel().dimension().equals(dimension) && pos.closerThan(player.blockPosition(), distance))
                {
                    angelBlockLocations.add(pos);
                }
            }

            if (!angelBlockLocations.isEmpty())
            {
                Util.sendGodMessageToPlayer(player,
                    new TextComponent("My senses says that you need to search something shiny and green!").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

                runTickDisplay(player, angelBlockLocations);
            }
            else
            {
                Util.sendGodMessageToPlayer(player,
                    new TextComponent("I do not see anything around you. Are you in correct location?").
                        withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            }
        }
        catch (ClassNotFoundException | IllegalAccessException e)
        {
            ExtraCommands.LOGGER.error("There was some issues with displaying angel blocks. ", e);
        }

        return 1;
    }


    private static void runTickDisplay(ServerPlayer player, List<BlockPos> locations)
    {
        for(int delay = 0; delay < 40; ++delay) {
            ServerScheduler.INSTANCE.schedule(delay * 5, () -> {
                Color color = Color.GREEN;

                locations.forEach(pos -> {
                    for(int i = 0; i < 8; ++i) {
                        Vec3 v = MiscUtils.getRandomOffset(pos, player.getLevel().getRandom());
                        ModNetwork.CHANNEL.sendTo(new ClientboundHunterParticlesMessage(v.x, v.y, v.z, null, color.getRGB()),
                            player.connection.getConnection(),
                            NetworkDirection.PLAY_TO_CLIENT);
                    }
                });
            });
        }
    }


    private static class PairComparator implements Comparator<Pair<BlockPos, ResourceKey<Level>>>
    {
        public PairComparator(Vec3i playerPos)
        {
            this.playerPos = playerPos;
        }


        @Override
        public int compare(Pair<BlockPos, ResourceKey<Level>> o1, Pair<BlockPos, ResourceKey<Level>> o2)
        {
            int i = o1.getSecond().compareTo(o2.getSecond());

            if (i != 0)
            {
                return i;
            }

            return Double.compare(o1.getFirst().distSqr(playerPos), o2.getFirst().distSqr(playerPos));
        }


        private final Vec3i playerPos;
    }
}

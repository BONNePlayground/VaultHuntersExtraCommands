//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.pylon.PylonBuff;
import iskallia.vault.effect.PylonEffect;
import iskallia.vault.init.ModEffects;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.PylonConsumeParticleMessage;
import iskallia.vault.world.data.PlayerPylons;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.PacketDistributor;


public class RedeemCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("redeem").
            requires(stack -> stack.hasPermission(1));

        LiteralArgumentBuilder<CommandSourceStack> positive = Commands.literal("positive").
            executes(ctx -> redeemModifiers(ctx.getSource().getPlayerOrException(), 1, null, false)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> redeemModifiers(EntityArgument.getPlayer(ctx, "player"), 1, null, false)).
                then(Commands.argument("amount", IntegerArgumentType.integer(1)).
                    executes(ctx -> redeemModifiers(EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), null, false)).
                    then(Commands.argument("caller", StringArgumentType.word()).
                        executes(ctx -> redeemModifiers(EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), StringArgumentType.getString(ctx, "caller"), false))
                    )
                )
            );

        LiteralArgumentBuilder<CommandSourceStack> negative = Commands.literal("negative").
            executes(ctx -> redeemModifiers(ctx.getSource().getPlayerOrException(), 1, null, true)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> redeemModifiers(EntityArgument.getPlayer(ctx, "player"), 1, null, true)).
                then(Commands.argument("amount", IntegerArgumentType.integer(1)).
                    executes(ctx -> redeemModifiers(EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), null, true)).
                    then(Commands.argument("caller", StringArgumentType.word()).
                        executes(ctx -> redeemModifiers(EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), StringArgumentType.getString(ctx, "caller"), true))
                    )
                )
            );

        dispatcher.register(baseLiteral.then(positive).then(negative));
    }



    private static int redeemModifiers(ServerPlayer player, int amount, String caller, boolean negative)
    {
        Random random = new Random();

        int pylonEffects = negative ? 0 : random.nextInt(amount);
        int modifiers = amount - pylonEffects;

        for (int i = 0; i < pylonEffects; i++)
        {
            applyRandomPylon(player, caller);
        }

        for (int i = 0; i < modifiers; i++)
        {
            if (negative)
            {
                applyRandomNegativeModifier(player, caller);
            }
            else
            {
                applyRandomPositiveModifier(player, caller);
            }
        }


        return 1;
    }


    private static void applyRandomPylon(ServerPlayer player, String name)
    {
        PylonBuff.Config config = Util.getRandom(ExtraCommands.CONFIGURATION.getPylonEffects()).orElse(null);

        PlayerPylons.add(ServerVaults.get(player.level).orElse(null), player, config);
        player.getLevel().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 2.0F);
        player.getLevel().playSound(null, player.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 2.0F);

        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
            new PylonConsumeParticleMessage(player.position(), player.getId(), config.getColor()));

        PylonEffect effect;

        if (player.hasEffect(ModEffects.PYLON))
        {

            effect = ModEffects.PYLON_OVERFLOW;
        }
        else
        {
            effect = ModEffects.PYLON;
        }

        effect.setDescription(config.getDescription());
        PylonBuff<?> buff = config.build();
        PylonBuff.Config<?> buffConfig = buff.getConfig();
        int duration = buffConfig.getDuration();

        if (duration > 0)
        {
            player.addEffect(new MobEffectInstance(effect, duration, 60, false, false, true));
        }

        TextComponent senderTxt;

        if (name != null && !name.isEmpty())
        {
            senderTxt = new TextComponent("[Twitch] ");
            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                append((new TextComponent(name)).withStyle(ChatFormatting.DARK_GREEN)).
                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
        }
        else
        {
            Optional<VaultGod> random = Util.getRandom(Arrays.stream(VaultGod.values()).toList());

            if (random.isPresent())
            {
                VaultGod god = random.get();
                senderTxt = new TextComponent("[VG] ");

                senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                    append((new TextComponent(god.getName())).withStyle(god.getChatColor())).
                    append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
            }
            else
            {
                senderTxt = new TextComponent("[uVG] ");

                senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                    append((new TextComponent("BONNe")).withStyle(ChatFormatting.DARK_RED)).
                    append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
            }
        }

        senderTxt.append((new TextComponent("I have blessed you with ")).
            append((new TextComponent(config.getDescription())).
                setStyle(Style.EMPTY.withColor(config.getColor()))).
            append(" Pylon!"));

        player.sendMessage(senderTxt, net.minecraft.Util.NIL_UUID);
    }



    private static void applyRandomPositiveModifier(ServerPlayer player, String name)
    {
        Util.getRandom(ExtraCommands.CONFIGURATION.getPositiveModifiers().stream().
                filter(modifier -> !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier)).toList()).
            map(VaultModifierRegistry::getOpt).
            flatMap(v -> v).
            ifPresent(effect -> {
                ServerVaults.get(player.getLevel()).
                    flatMap(vault -> vault.getOptional(Vault.MODIFIERS)).
                    ifPresent(modifiers ->
                    {
                        modifiers.addModifier(effect, 1, true, JavaRandom.ofInternal(0));

                        TextComponent senderTxt;

                        if (name != null && !name.isEmpty())
                        {
                            senderTxt = new TextComponent("[Twitch] ");
                            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                                append((new TextComponent(name)).withStyle(ChatFormatting.DARK_GREEN)).
                                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
                        }
                        else
                        {
                            Optional<VaultGod> random = Util.getRandom(Arrays.stream(VaultGod.values()).toList());

                            if (random.isPresent())
                            {
                                VaultGod god = random.get();
                                senderTxt = new TextComponent("[VG] ");

                                senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                                    append((new TextComponent(god.getName())).withStyle(god.getChatColor())).
                                    append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
                            }
                            else
                            {
                                senderTxt = new TextComponent("[uVG] ");

                                senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                                    append((new TextComponent("BONNe")).withStyle(ChatFormatting.DARK_RED)).
                                    append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
                            }
                        }



                        senderTxt.append(new TextComponent("You are blessed with ").
                            append((new TextComponent(effect.getDisplayName())).
                                setStyle(Style.EMPTY.withColor(effect.getDisplayTextColor()))));

                        player.getLevel().players().forEach(p -> p.sendMessage(senderTxt, net.minecraft.Util.NIL_UUID));
                    });
            });

    }


    private static void applyRandomNegativeModifier(ServerPlayer player, String name)
    {
        Util.getRandom(ExtraCommands.CONFIGURATION.getNegativeModifiers().stream().
                filter(modifier -> !ExtraCommands.CONFIGURATION.getProtectedModifiers().contains(modifier)).toList()).
            map(VaultModifierRegistry::getOpt).
            flatMap(v -> v).
            ifPresent(effect -> {
                ServerVaults.get(player.getLevel()).
                    flatMap(vault -> vault.getOptional(Vault.MODIFIERS)).
                    ifPresent(modifiers ->
                    {
                        modifiers.addModifier(effect, 1, true, JavaRandom.ofInternal(0));

                        TextComponent senderTxt;

                        if (name != null && !name.isEmpty())
                        {
                            senderTxt = new TextComponent("[Twitch] ");
                            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                                append((new TextComponent(name)).withStyle(ChatFormatting.DARK_GREEN)).
                                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
                        }
                        else
                        {
                            Optional<VaultGod> random = Util.getRandom(Arrays.stream(VaultGod.values()).toList());

                            if (random.isPresent())
                            {
                                VaultGod god = random.get();
                                senderTxt = new TextComponent("[VG] ");

                                senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                                    append((new TextComponent(god.getName())).withStyle(god.getChatColor())).
                                    append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
                            }
                            else
                            {
                                senderTxt = new TextComponent("[uVG] ");

                                senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                                    append((new TextComponent("BONNe")).withStyle(ChatFormatting.DARK_RED)).
                                    append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
                            }
                        }

                        senderTxt.append(new TextComponent("You are punished with ").
                            append((new TextComponent(effect.getDisplayName())).
                            setStyle(Style.EMPTY.withColor(effect.getDisplayTextColor()))));

                        player.getLevel().players().forEach(p -> p.sendMessage(senderTxt, net.minecraft.Util.NIL_UUID));
                    });
            });

    }
}

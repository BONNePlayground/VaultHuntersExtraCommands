//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.pylon.PylonBuff;
import iskallia.vault.effect.PylonEffect;
import iskallia.vault.init.ModEffects;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.PylonConsumeParticleMessage;
import iskallia.vault.world.data.PlayerPylons;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;


/**
 * This class triggers pylon effect on the player.
 */
public class VaultPylonCommand
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_EFFECTS = ((context, builder) ->
        SharedSuggestionProvider.suggest(ExtraCommands.CONFIGURATION.getPylonEffects().
            stream().
            map(config -> config.serializeNBT().toString()).
            collect(Collectors.toList()), builder));

    /**
     * Registers the command that toggles a pause for the vault.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> vaultLiteral = Commands.literal("vault");

        LiteralArgumentBuilder<CommandSourceStack> addBuff = Commands.literal("pylon").
            executes(ctx -> addPylonEffect(null,
                Optional.empty(),
                ctx.getSource().getPlayerOrException())).
            then(Commands.argument("name", StringArgumentType.word()).
                executes(ctx -> addPylonEffect(ctx.getArgument("name", String.class),
                    Optional.empty(),
                    ctx.getSource().getPlayerOrException())).
                then(Commands.argument("pylon", NbtTagArgument.nbtTag()).
                    suggests(SUGGEST_EFFECTS).
                    executes(ctx -> addPylonEffect(ctx.getArgument("name", String.class),
                        Optional.of(NbtTagArgument.getNbtTag(ctx, "pylon")),
                        ctx.getSource().getPlayerOrException())).
                    then(Commands.argument("player", EntityArgument.players()).
                        executes(ctx -> addPylonEffect(ctx.getArgument("name", String.class),
                            Optional.of(NbtTagArgument.getNbtTag(ctx, "pylon")),
                            EntityArgument.getPlayer(ctx, "player"))))));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(addBuff)));
    }


    /**
     * This method applies pylon config to the player.
     * @param player that receives pylon buff.
     * @return 1
     */
    private static int addPylonEffect(String name, Optional<Tag> optionalValue, ServerPlayer player)
    {
        Level world = player.getLevel();

        if (!world.isClientSide)
        {
            PylonBuff.Config config;

            if (optionalValue.isPresent() && !optionalValue.get().getAsString().isEmpty())
            {
                ExtraCommands.LOGGER.debug("Parsing " + optionalValue.get().getAsString() + " NBT tag as PylonBuff.");

                config = PylonBuff.Config.fromNBT((CompoundTag) optionalValue.get());
            }
            else
            {
                ExtraCommands.LOGGER.debug("Getting random PylonBuff.");
                config = getRandom(ExtraCommands.CONFIGURATION.getPylonEffects()).orElse(null);
            }

            if (config == null)
            {
                ExtraCommands.LOGGER.error("Could not parse " + (optionalValue.orElse(new CompoundTag()) + " as pylon effect."));
                return 1;
            }

            PlayerPylons.add(ServerVaults.get(player.level).orElse(null), player, config);
            world.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 2.0F);
            world.playSound(null, player.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 2.0F);

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
                senderTxt = new TextComponent("");
                senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                    append((new TextComponent(name)).withStyle(ChatFormatting.DARK_GREEN)).
                    append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
            }
            else
            {
                Optional<VaultGod> random = getRandom(Arrays.stream(VaultGod.values()).toList());

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
                    senderTxt = new TextComponent("[VG] ");

                    senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                        append((new TextComponent("[BONNe]")).withStyle(ChatFormatting.DARK_RED)).
                        append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));
                }
            }

            senderTxt.append((new TextComponent("I have blessed you with ")).
                append((new TextComponent(config.getDescription())).
                    setStyle(Style.EMPTY.withColor(config.getColor()))).
                append(" Pylon!"));

            player.sendMessage(senderTxt, Util.NIL_UUID);
        }

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
}

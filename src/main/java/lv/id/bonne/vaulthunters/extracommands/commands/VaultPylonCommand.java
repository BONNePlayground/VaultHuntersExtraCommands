//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.Optional;
import java.util.stream.Collectors;

import iskallia.vault.core.vault.pylon.PylonBuff;
import iskallia.vault.effect.PylonEffect;
import iskallia.vault.init.ModEffects;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.PylonConsumeParticleMessage;
import iskallia.vault.world.data.PlayerPylons;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
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
            executes(ctx -> addPylonEffect(ctx.getSource().getPlayerOrException(), Optional.empty())).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> addPylonEffect(EntityArgument.getPlayer(ctx, "player"), Optional.empty())).
                then(Commands.argument("pylon", NbtTagArgument.nbtTag()).
                    suggests(SUGGEST_EFFECTS).
                    executes(ctx -> addPylonEffect(EntityArgument.getPlayer(ctx, "player"),
                    Optional.of(NbtTagArgument.getNbtTag(ctx, "pylon"))))));

        dispatcher.register(baseLiteral.then(vaultLiteral.then(addBuff)));
    }


    /**
     * This method applies pylon config to the player.
     * @param player that receives pylon buff.
     * @return 1
     */
    private static int addPylonEffect(ServerPlayer player, Optional<Tag> optionalValue)
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
                config = Util.getRandom(ExtraCommands.CONFIGURATION.getPylonEffects()).orElse(null);
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

            Util.sendGodMessageToPlayer(player,
                new TextComponent("I have blessed you with ").
                    append((new TextComponent(config.getDescription())).
                        setStyle(Style.EMPTY.withColor(config.getColor()))).
                    append(" Pylon!"));
        }

        return 1;
    }
}

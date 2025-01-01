//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import iskallia.vault.block.entity.VaultAltarTileEntity;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.UpdateTitlesDataMessage;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.world.data.*;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.mixin.*;
import lv.id.bonne.vaulthunters.extracommands.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.command.EnumArgument;


public class ResetCommand
{
    private enum Mode
    {
        COMPLETE,
        ALTAR,
        STATS,
        SKILLS,
        QUESTS,
        HISTORY,
        BOUNTY,
        DISCOVERIES,
        ETERNALS,
        PARADOX,
        BLACK_MARKET,
        GREED,
        PROFICIENCY,
        REPUTATION,
        SPIRIT,
        TITLES,
        SKILL_ALTAR
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

        LiteralArgumentBuilder<CommandSourceStack> complete = Commands.literal("reset").
            executes(ctx -> clearPlayerData(ctx.getSource(), ctx.getSource().getPlayerOrException(), Mode.COMPLETE)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> clearPlayerData(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), Mode.COMPLETE)).
                then(Commands.argument("mode", EnumArgument.enumArgument(Mode.class)).
                    executes(ctx -> clearPlayerData(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), ctx.getArgument("mode", Mode.class)))
                )
            ).
            then(Commands.argument("uuid", UuidArgument.uuid()).
                executes(ctx -> clearPlayerData(ctx.getSource(), UuidArgument.getUuid(ctx, "uuid"), Mode.COMPLETE)).
                then(Commands.argument("mode", EnumArgument.enumArgument(Mode.class)).
                    executes(ctx -> clearPlayerData(ctx.getSource(), UuidArgument.getUuid(ctx, "uuid"), ctx.getArgument("mode", Mode.class)))
                )
            );

        dispatcher.register(baseLiteral.then(complete));
    }


    private static int clearPlayerData(CommandSourceStack sourceStack, ServerPlayer serverPlayer, Mode mode)
    {
        runDataRemoval(serverPlayer.getUUID(), mode, serverPlayer, sourceStack.getLevel(), message ->
        {
            Util.sendGodMessageToPlayer(serverPlayer, message);

            try
            {
                ServerPlayer caller = sourceStack.getPlayerOrException();

                if (caller != serverPlayer)
                {
                    sourceStack.sendSuccess(new TextComponent(message), true);
                }
            }
            catch (CommandSyntaxException e)
            {
                sourceStack.sendSuccess(new TextComponent(message), true);
            }
        });

        return 1;
    }


    private static int clearPlayerData(CommandSourceStack sourceStack, UUID playerUUID, Mode mode)
    {
        ServerPlayer serverPlayer = sourceStack.getServer().getPlayerList().getPlayer(playerUUID);

        runDataRemoval(playerUUID, mode, serverPlayer, sourceStack.getLevel(), message ->
        {
            if (serverPlayer != null)
            {
                Util.sendGodMessageToPlayer(serverPlayer, message);
            }

            try
            {
                ServerPlayer caller = sourceStack.getPlayerOrException();

                if (caller != serverPlayer)
                {
                    sourceStack.sendSuccess(new TextComponent(message), true);
                }
            }
            catch (CommandSyntaxException e)
            {
                sourceStack.sendSuccess(new TextComponent(message), true);
            }
        });

        return 1;
    }

    
    private static void runDataRemoval(UUID playerUUID, Mode mode, @Nullable ServerPlayer player, ServerLevel level, Consumer<String> message)
    {
        switch (mode)
        {
            case ALTAR ->
            {
                removeVaultAltarData(playerUUID, level);
                message.accept("Vault Altar Data removed");
            }
            case STATS ->
            {
                removeVaultStatsData(playerUUID, level);
                message.accept("Vault Stats Data removed");
            }
            case SKILLS ->
            {
                properlyResetSkills(player, playerUUID, level);
                message.accept("Player Skills and Knowledge Data removed");
            }
            case QUESTS ->
            {
                resetQuests(playerUUID);
                message.accept("Player Quests Data removed");
            }
            case HISTORY ->
            {
                removeVaultHistory(playerUUID, level);
                message.accept("Player History Data removed");
            }
            case BOUNTY ->
            {
                resetBounties(playerUUID);
                message.accept("Player Bounty Data removed");
            }
            case DISCOVERIES ->
            {
                // Reset discoveries
                removeDiscoveries(player, playerUUID, level);
                message.accept("Player Discoveries Data removed");
            }
            case ETERNALS ->
            {
                removeEternals(playerUUID, level);
                message.accept("Player Eternals Data removed");
            }
            case PARADOX ->
            {
                removeParadox(playerUUID, level);
                message.accept("Player Paradox Data removed");
            }
            case BLACK_MARKET ->
            {
                removeBlackMarket(playerUUID, level);
                message.accept("Player Black Market Data removed");
            }
            case GREED ->
            {
                resetGreedData(playerUUID);
                message.accept("Player Greed Data removed");
            }
            case PROFICIENCY ->
            {
                resetProficiencyData(player, playerUUID, level);
                message.accept("Player Proficiency Data removed");
            }
            case REPUTATION ->
            {
                resetReputationData(playerUUID);
                message.accept("Player Reputation Data removed");
            }
            case SPIRIT ->
            {
                resetSpiritData(playerUUID, level);
                message.accept("Player Spirit Data removed");
            }
            case TITLES ->
            {
                resetTitlesData(player, playerUUID);
                message.accept("Player Titles Data removed");
            }
            case SKILL_ALTAR ->
            {
                resetSkillAltarData(player, playerUUID, level);
                message.accept("Player Skill Altar Data removed");
            }
            default ->
            {
                resetBounties(playerUUID);
                removeVaultAltarData(playerUUID, level);
                removeVaultStatsData(playerUUID, level);
                properlyResetSkills(player, playerUUID, level);
                resetQuests(playerUUID);
                removeVaultHistory(playerUUID, level);
                removeDiscoveries(player, playerUUID, level);
                removeEternals(playerUUID, level);
                removeParadox(playerUUID, level);
                removeBlackMarket(playerUUID, level);
                resetGreedData(playerUUID);
                resetProficiencyData(player, playerUUID, level);
                resetReputationData(playerUUID);
                resetSpiritData(playerUUID, level);
                resetTitlesData(player, playerUUID);
                resetSkillAltarData(player, playerUUID, level);

                if (player != null)
                {
                    message.accept("You have been punished! Now you must return to the start!");
                }
                else
                {
                    File playerDataFolder = ((MinecraftServerAccessor) level.getServer()).getPlayerDataStorage().
                        getPlayerDataFolder();

                    File playerDatFile = new File(playerDataFolder, playerUUID.toString() + ".dat");

                    if (playerDatFile.exists())
                    {
                        playerDatFile.delete();
                    }

                    message.accept("Player data " + playerUUID + " completely removed including <uuid>.dat");
                }
            }
        }

        ExtraCommands.LOGGER.info(playerUUID + " data reset in mode: " + mode);
    }


    private static void resetQuests(UUID playerUUID)
    {
        ((QuestStatesDataAccessor) QuestStatesData.get()).getSTATES().remove(playerUUID);
    }
    
    
    private static void resetBounties(UUID playerUUID)
    {
        BountyData.get().getAllLegendaryFor(playerUUID).clear();
        BountyData.get().resetAllBounties(playerUUID);
        ((BountyDataAccessor) BountyData.get()).getActive().remove(playerUUID);
        ((BountyDataAccessor) BountyData.get()).getAvailable().remove(playerUUID);
        ((BountyDataAccessor) BountyData.get()).getComplete().remove(playerUUID);
        ((BountyDataAccessor) BountyData.get()).getLegendary().remove(playerUUID);
    }
    
    
    private static void removeVaultAltarData(UUID playerUUID, ServerLevel level)
    {
        PlayerVaultAltarData data = PlayerVaultAltarData.get(level);

        List<BlockPos> altars = data.getAltars(playerUUID);
        altars.stream().
            filter(level::isLoaded).
            map(level::getBlockEntity).
            filter(te -> te instanceof VaultAltarTileEntity).
            map(te -> (VaultAltarTileEntity)te).
            filter(altar -> (altar.getAltarState() == VaultAltarTileEntity.AltarState.ACCEPTING)).
            forEach(altar -> altar.onRemoveInput(playerUUID));

        data.removeRecipe(playerUUID);
        altars.stream().toList().forEach(altar -> data.removeAltar(playerUUID, altar));
        ((PlayerVaultAltarDataAccessor) data).getPlayerAltars().remove(playerUUID);

        data.setDirty();
    }


    private static void removeVaultStatsData(UUID playerUUID, ServerLevel level)
    {
        PlayerStatsData data = PlayerStatsData.get();

        if (((PlayerStatsDataAccessor) data).getPlayerStats().remove(playerUUID) != null)
        {
            data.setDirty();
        }

        VaultPlayerStats vaultData = VaultPlayerStats.get(level.getServer());

        if (((VaultPlayerStatsAccessor) vaultData).getPending().remove(playerUUID) != null)
        {
            vaultData.setDirty();
        }
    }


    private static void removeVaultHistory(UUID playerUUID, ServerLevel level)
    {
        VaultDeathSnapshotData.get(level).removeSnapshots(playerUUID);
        VaultJoinSnapshotData.get(level).removeSnapshots(playerUUID);
        
        for (Iterator<VaultSnapshot> iter = VaultSnapshots.getAll().iterator(); iter.hasNext();)
        {
            VaultSnapshot snapshot = iter.next();

            if (snapshot.getEnd() != null &&
                (snapshot.getEnd().get(Vault.STATS)).getMap().containsKey(playerUUID))
            {
                snapshot.getEnd().get(Vault.STATS).getMap().remove(playerUUID);

                if (snapshot.getEnd().get(Vault.STATS).getMap().isEmpty())
                {
                    iter.remove();
                }
            }
        }

        VaultSnapshots.get(level.getServer()).setDirty(true);

        PlayerHistoricFavoritesData favoritesData = PlayerHistoricFavoritesData.get(level);
        favoritesData.getPlayerMap().remove(playerUUID);
        favoritesData.setDirty();
    }


    private static void removeDiscoveries(ServerPlayer player, UUID playerUUID, ServerLevel level)
    {
        // Models
        DiscoveredModelsData.get(level.getServer()).reset(playerUUID);

        // Alchemy
        DiscoveredAlchemyEffectsData effectsData = DiscoveredAlchemyEffectsData.get(level);

        if (((DiscoveredAlchemyEffectsDataAccessor) effectsData).getDiscoveredEffects().remove(playerUUID) != null)
        {
            if (player != null) effectsData.syncTo(player);
            effectsData.setDirty();
        }

        // Relics
        DiscoveredRelicsData relicsData = DiscoveredRelicsData.get(level);

        if (((DiscoveredRelicsDataAccessor) relicsData).getDiscoveredRelics().remove(playerUUID) != null)
        {
            relicsData.setDirty();
        }

        // Trinkets
        DiscoveredTrinketsData trinketsData = DiscoveredTrinketsData.get(level);

        if (((DiscoveredTrinketsDataAccessor) trinketsData).getCollectedTrinkets().remove(playerUUID) != null)
        {
            if (player != null) trinketsData.syncTo(player);
            trinketsData.setDirty();
        }

        // Workbench
        DiscoveredWorkbenchModifiersData workbenchData =
            DiscoveredWorkbenchModifiersData.get(level);

        if (((DiscoveredWorkbenchModifiersDataAccessor) workbenchData).getDiscoveredCrafts().remove(playerUUID) != null)
        {
            if (player != null) workbenchData.syncTo(player);
            workbenchData.setDirty();
        }

        // Discovery
        DiscoveryGoalStatesData goalStatesData = DiscoveryGoalStatesData.get(level);

        if (((DiscoveryGoalStatesDataAccessor) goalStatesData).getPlayerMap().remove(playerUUID) != null)
        {
            goalStatesData.setDirty();
        }
    }


    private static void removeEternals(UUID playerUUID, ServerLevel level)
    {
        EternalsData eternalsData = EternalsData.get(level);
        EternalsData.EternalGroup eternalGroup = eternalsData.getEternals(playerUUID);
        eternalGroup.getEternals().forEach(eternal -> eternalGroup.removeEternal(eternal.getId()));
        ((EternalsDataAccessor) eternalsData).getPlayerMap().remove(playerUUID);
        eternalsData.syncAll();
    }


    private static void removeParadox(UUID playerUUID, ServerLevel level)
    {
        ParadoxCrystalData paradoxCrystalData = ParadoxCrystalData.get(level);

        if (((ParadoxCrystalDataAccessor) paradoxCrystalData).getEntries().remove(playerUUID) != null)
        {
            paradoxCrystalData.setDirty();
        }
    }


    private static void removeBlackMarket(UUID playerUUID, ServerLevel level)
    {
        PlayerBlackMarketData blackMarketData = PlayerBlackMarketData.get(level);

        if (blackMarketData.getPlayerMap().remove(playerUUID) != null)
        {
            blackMarketData.setDirty();
        }
    }


    private static void resetGreedData(UUID playerUUID)
    {
        PlayerGreedData greedData = PlayerGreedData.get();

        if (((PlayerGreedDataAccessor) greedData).getData().remove(playerUUID) != null)
        {
            greedData.setDirty();
        }
    }


    private static void resetProficiencyData(ServerPlayer player, UUID playerUUID, ServerLevel level)
    {
        PlayerProficiencyData proficiencyData = PlayerProficiencyData.get(level);

        if (((PlayerProficiencyDataAccessor) proficiencyData).getPlayerProficiency().remove(playerUUID) != null)
        {
            if (player != null) proficiencyData.sendProficiencyInformation(player);
            proficiencyData.setDirty();
        }
    }


    private static void resetReputationData(UUID playerUUID)
    {
        PlayerReputationData reputationData = PlayerReputationData.get();

        if (((PlayerReputationDataAccessor) reputationData).getEntries().remove(playerUUID) != null)
        {
            reputationData.setDirty();
        }
    }


    private static void resetTitlesData(ServerPlayer player, UUID playerUUID)
    {
        PlayerTitlesData titlesData = PlayerTitlesData.get();

        if (titlesData.entries.remove(playerUUID) != null)
        {
            ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new UpdateTitlesDataMessage(titlesData.entries));
            titlesData.setDirty();
        }

        if (player != null) player.refreshTabListName();
    }


    private static void resetSpiritData(UUID playerUUID, ServerLevel level)
    {
        PlayerSpiritRecoveryData spiritData = PlayerSpiritRecoveryData.get(level);

        PlayerSpiritRecoveryDataAccessor accessor = (PlayerSpiritRecoveryDataAccessor) spiritData;

        if (accessor.getVaultSpiritData().remove(playerUUID) != null)
        {
            spiritData.setDirty();
        }

        if (accessor.getHeroDiscounts().remove(playerUUID) != null)
        {
            spiritData.setDirty();
        }

        if (accessor.getSpiritRecoveryMultipliers().remove(playerUUID) != null)
        {
            spiritData.setDirty();
        }

        if (accessor.getPlayerSpiritRecoveries().remove(playerUUID) != null)
        {
            spiritData.setDirty();
        }
    }


    private static void resetSkillAltarData(ServerPlayer player, UUID playerUUID, ServerLevel level)
    {
        SkillAltarData skillAltarData = SkillAltarData.get(level);

        if (((SkillAltarDataAccessor) skillAltarData).getPlayerSkillTemplates().remove(playerUUID) != null)
        {
            if (player != null)
            {
                skillAltarData.getSkillTemplates(playerUUID);
                skillAltarData.syncTo(player);
            }

            skillAltarData.setDirty();
        }
    }


    private static void properlyResetSkills(ServerPlayer player, UUID playerUUID, ServerLevel level)
    {
        PlayerVaultStatsData statsData = PlayerVaultStatsData.get(level);
        PlayerResearchesData researchesData = PlayerResearchesData.get(level);

        if (player != null)
        {
            statsData.resetLevelAbilitiesAndExpertise(player);
            researchesData.resetResearchTree(player);
            statsData.resetKnowledge(player);

            new PlayerVaultStats(playerUUID).sync(level.getServer());
            new PlayerResearchesData().sync(player);
        }

        // Remove player entry completely.
        if (((PlayerVaultStatsDataAccessor) statsData).getPlayerMap().remove(playerUUID) != null)
        {
            statsData.setDirty();
        }

        if (((PlayerResearchesDataAccessor) researchesData).getPlayerMap().remove(playerUUID) != null)
        {
            researchesData.setDirty();
        }

        // Remove abilities data
        PlayerAbilitiesData abilitiesData = PlayerAbilitiesData.get(level);

        if (((PlayerAbilitiesDataAccessor) abilitiesData).getPlayerMap().remove(playerUUID) != null)
        {
            ((PlayerAbilitiesDataAccessor) abilitiesData).getScheduledCorruptionCheck().remove(playerUUID);
            ((PlayerAbilitiesDataAccessor) abilitiesData).getScheduledRefund().remove(playerUUID);
            ((PlayerAbilitiesDataAccessor) abilitiesData).getScheduledMerge().remove(playerUUID);
            abilitiesData.setDirty();
        }

        // Remove talents
        PlayerTalentsData talentsData = PlayerTalentsData.get(level);

        if (((PlayerTalentsDataAccessor) talentsData).getPlayerMap().remove(playerUUID) != null)
        {
            ((PlayerTalentsDataAccessor) talentsData).getScheduledMerge().remove(playerUUID);
            talentsData.setDirty();
        }

        // Remove expertises
        PlayerExpertisesData expertisesData = PlayerExpertisesData.get(level);

        if (((PlayerExpertisesDataAccessor) expertisesData).getPlayerMap().remove(playerUUID) != null)
        {
            ((PlayerExpertisesDataAccessor) expertisesData).getScheduledMerge().remove(playerUUID);
            expertisesData.setDirty();
        }
    }
}

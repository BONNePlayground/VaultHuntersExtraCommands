//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.*;

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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.command.EnumArgument;


public class ClearCommand
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
            executes(ctx -> clearPlayerData(ctx.getSource().getPlayerOrException(), Mode.COMPLETE)).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> clearPlayerData(EntityArgument.getPlayer(ctx, "player"), Mode.COMPLETE))).
                then(Commands.argument("mode", EnumArgument.enumArgument(Mode.class)).
                    executes(ctx -> clearPlayerData(EntityArgument.getPlayer(ctx, "player"), ctx.getArgument("mode", Mode.class)))
            );

        dispatcher.register(baseLiteral.then(complete));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int clearPlayerData(ServerPlayer player, Mode clearMode)
    {
        switch (clearMode)
        {
            case ALTAR -> removeVaultAltarData(player);
            case STATS -> removeVaultStatsData(player);
            case SKILLS -> properlyResetSkills(player);
            case QUESTS -> QuestStatesData.get().getState(player).reset();
            case HISTORY ->
            {
                VaultDeathSnapshotData.get(player.getLevel()).removeSnapshots(player);
                VaultJoinSnapshotData.get(player.getLevel()).removeSnapshots(player);
                removeVaultHistory(player);
            }
            case BOUNTY ->
            {
                // Remove bounty data
                BountyData.get().getAllLegendaryFor(player.getUUID()).clear();
                BountyData.get().resetAllBounties(player.getUUID());
            }
            case DISCOVERIES ->
            {
                // Reset discoveries
                DiscoveredModelsData.get(player.getLevel()).reset(player.getUUID());
                removeDiscoveries(player);
            }
            case ETERNALS -> removeEternals(player);
            case PARADOX -> removeParadox(player);
            case BLACK_MARKET -> removeBlackMarket(player);
            case GREED -> resetGreedData(player);
            case PROFICIENCY -> resetProficiencyData(player);
            case REPUTATION -> resetReputationData(player);
            case SPIRIT -> resetSpiritData(player);
            case TITLES -> resetTitlesData(player);
            case SKILL_ALTAR -> resetSkillAltarData(player);
            default ->
            {
                removeVaultAltarData(player);
                removeVaultStatsData(player);
                properlyResetSkills(player);
                QuestStatesData.get().getState(player).reset();

                VaultDeathSnapshotData.get(player.getLevel()).removeSnapshots(player);
                VaultJoinSnapshotData.get(player.getLevel()).removeSnapshots(player);
                removeVaultHistory(player);

                BountyData.get().getAllLegendaryFor(player.getUUID()).clear();
                BountyData.get().resetAllBounties(player.getUUID());

                DiscoveredModelsData.get(player.getLevel()).reset(player.getUUID());
                removeDiscoveries(player);

                removeEternals(player);
                removeParadox(player);
                removeBlackMarket(player);
                resetGreedData(player);
                resetProficiencyData(player);
                resetReputationData(player);
                resetSpiritData(player);
                resetTitlesData(player);
                resetSkillAltarData(player);

                Util.sendGodMessageToPlayer(player, "You have been punished! Now you must return to the start!");
            }
        }

        ExtraCommands.LOGGER.info(player.getDisplayName().getString() + " player " + clearMode.name() + " data are cleared!");

        return 1;
    }


    private static void removeVaultAltarData(ServerPlayer player)
    {
        PlayerVaultAltarData data = PlayerVaultAltarData.get(player.getLevel());

        List<BlockPos> altars = data.getAltars(player.getUUID());
        altars.stream().
            filter(pos -> player.getLevel().isLoaded(pos)).
            map(pos -> player.getLevel().getBlockEntity(pos)).
            filter(te -> te instanceof VaultAltarTileEntity).
            map(te -> (VaultAltarTileEntity)te).
            filter(altar -> (altar.getAltarState() == VaultAltarTileEntity.AltarState.ACCEPTING)).
            forEach(altar -> altar.onRemoveInput(player.getUUID()));

        data.removeRecipe(player.getUUID());
        altars.stream().toList().forEach(altar -> data.removeAltar(player.getUUID(), altar));

        data.setDirty();
    }


    private static void removeVaultStatsData(ServerPlayer player)
    {
        PlayerStatsData data = PlayerStatsData.get();

        if (((PlayerStatsDataAccessor) data).getPlayerStats().remove(player.getUUID()) != null)
        {
            data.setDirty();
        }
    }


    private static void removeVaultHistory(ServerPlayer player)
    {
        for (Iterator<VaultSnapshot> iter = VaultSnapshots.getAll().iterator(); iter.hasNext();)
        {
            VaultSnapshot snapshot = iter.next();

            if (snapshot.getEnd() != null &&
                (snapshot.getEnd().get(Vault.STATS)).getMap().containsKey(player.getUUID()))
            {
                snapshot.getEnd().get(Vault.STATS).getMap().remove(player.getUUID());

                if (snapshot.getEnd().get(Vault.STATS).getMap().isEmpty())
                {
                    iter.remove();
                }
            }
        }

        VaultSnapshots.get(player.server).setDirty(true);

        PlayerHistoricFavoritesData favoritesData = PlayerHistoricFavoritesData.get(player.getLevel());

        favoritesData.getPlayerMap().remove(player.getUUID());
        favoritesData.setDirty();
    }


    private static void removeDiscoveries(ServerPlayer player)
    {
        // Alchemy
        DiscoveredAlchemyEffectsData effectsData = DiscoveredAlchemyEffectsData.get(player.getLevel());

        if (((DiscoveredAlchemyEffectsDataAccessor) effectsData).getDiscoveredEffects().remove(player.getUUID()) != null)
        {
            effectsData.syncTo(player);
            effectsData.setDirty();
        }

        // Relics
        DiscoveredRelicsData relicsData = DiscoveredRelicsData.get(player.getLevel());

        if (!relicsData.getDiscoveredRelics(player.getUUID()).isEmpty())
        {
            relicsData.getDiscoveredRelics(player.getUUID()).clear();
            relicsData.setDirty();
        }

        // Trinkets
        DiscoveredTrinketsData trinketsData = DiscoveredTrinketsData.get(player.getLevel());

        if (!trinketsData.getDiscoveredTrinkets(player).isEmpty())
        {
            trinketsData.getDiscoveredTrinkets(player).clear();
            trinketsData.syncTo(player);
            trinketsData.setDirty();
        }

        // Workbench
        DiscoveredWorkbenchModifiersData workbenchData =
            DiscoveredWorkbenchModifiersData.get(player.getLevel());

        if (((DiscoveredWorkbenchModifiersDataAccessor) workbenchData).getDiscoveredCrafts().remove(player.getUUID()) != null)
        {
            workbenchData.syncTo(player);
            workbenchData.setDirty();
        }

        // Discovery
        DiscoveryGoalStatesData goalStatesData = DiscoveryGoalStatesData.get(player.getLevel());

        if (((DiscoveryGoalStatesDataAccessor) goalStatesData).getPlayerMap().remove(player.getUUID()) != null)
        {
            goalStatesData.setDirty();
        }
    }


    private static void removeEternals(ServerPlayer player)
    {
        EternalsData eternalsData = EternalsData.get(player.getLevel());
        EternalsData.EternalGroup eternalGroup = eternalsData.getEternals(player);
        eternalGroup.getEternals().forEach(eternal -> eternalGroup.removeEternal(eternal.getId()));
    }


    private static void removeParadox(ServerPlayer player)
    {
        ParadoxCrystalData paradoxCrystalData = ParadoxCrystalData.get(player.getLevel());

        if (((ParadoxCrystalDataAccessor) paradoxCrystalData).getEntries().remove(player.getUUID()) != null)
        {
            paradoxCrystalData.setDirty();
        }
    }


    private static void removeBlackMarket(ServerPlayer player)
    {
        PlayerBlackMarketData blackMarketData = PlayerBlackMarketData.get(player.getLevel());

        if (blackMarketData.getPlayerMap().remove(player.getUUID()) != null)
        {
            blackMarketData.setDirty();
        }
    }


    private static void resetGreedData(ServerPlayer player)
    {
        PlayerGreedData greedData = PlayerGreedData.get();

        if (((PlayerGreedDataAccessor) greedData).getData().remove(player.getUUID()) != null)
        {
            greedData.setDirty();
        }
    }


    private static void resetProficiencyData(ServerPlayer player)
    {
        PlayerProficiencyData proficiencyData = PlayerProficiencyData.get(player.getLevel());

        if (((PlayerProficiencyDataAccessor) proficiencyData).getPlayerProficiencies().remove(player.getUUID()) != null)
        {
            proficiencyData.sendProficiencyInformation(player);
            proficiencyData.setDirty();
        }
    }


    private static void resetReputationData(ServerPlayer player)
    {
        PlayerReputationData reputationData = PlayerReputationData.get();

        if (((PlayerReputationDataAccessor) reputationData).getEntries().remove(player.getUUID()) != null)
        {
            reputationData.setDirty();
        }
    }


    private static void resetTitlesData(ServerPlayer player)
    {
        PlayerTitlesData titlesData = PlayerTitlesData.get();

        if (titlesData.entries.remove(player.getUUID()) != null)
        {
            ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new UpdateTitlesDataMessage(titlesData.entries));
            titlesData.setDirty();
        }

        player.refreshTabListName();
    }


    private static void resetSpiritData(ServerPlayer player)
    {
        PlayerSpiritRecoveryData spiritData = PlayerSpiritRecoveryData.get(player.getLevel());

        PlayerSpiritRecoveryDataAccessor accessor = (PlayerSpiritRecoveryDataAccessor) spiritData;

        if (accessor.getVaultSpiritData().remove(player.getUUID()) != null)
        {
            spiritData.setDirty();
        }

        if (accessor.getHeroDiscounts().remove(player.getUUID()) != null)
        {
            spiritData.setDirty();
        }

        if (accessor.getSpiritRecoveryMultipliers().remove(player.getUUID()) != null)
        {
            spiritData.setDirty();
        }

        if (accessor.getPlayerSpiritRecoveries().remove(player.getUUID()) != null)
        {
            spiritData.setDirty();
        }
    }


    private static void resetSkillAltarData(ServerPlayer player)
    {
        SkillAltarData skillAltarData = SkillAltarData.get(player.getLevel());

        if (!skillAltarData.getSkillTemplates(player.getUUID()).isEmpty())
        {
            skillAltarData.getSkillTemplates(player.getUUID()).clear();
            skillAltarData.syncTo(player);
            skillAltarData.setDirty();
        }
    }


    private static void properlyResetSkills(ServerPlayer player)
    {
        PlayerVaultStatsData statsData = PlayerVaultStatsData.get(player.getLevel());
        PlayerResearchesData researchesData = PlayerResearchesData.get(player.getLevel());

        statsData.resetLevelAbilitiesAndExpertise(player);
        researchesData.resetResearchTree(player);
        statsData.resetKnowledge(player);

        // Remove player entry completely.
        if (((PlayerVaultStatsDataAccessor) statsData).getPlayerMap().remove(player.getUUID()) != null)
        {
            // Sync empty data with server
            new PlayerVaultStats(player.getUUID()).sync(player.getLevel().getServer());
            statsData.setDirty();
        }

        if (((PlayerResearchesDataAccessor) researchesData).getPlayerMap().remove(player.getUUID()) != null)
        {
            new PlayerResearchesData().sync(player);
            researchesData.setDirty();
        }
    }
}

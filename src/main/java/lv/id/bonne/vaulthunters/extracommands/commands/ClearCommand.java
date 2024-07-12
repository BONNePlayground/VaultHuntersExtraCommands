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
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.discoverylogic.DiscoveryGoalsState;
import iskallia.vault.entity.eternal.EternalData;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.nbt.VMapNBT;
import iskallia.vault.network.message.UpdateTitlesDataMessage;
import iskallia.vault.world.data.*;
import lv.id.bonne.vaulthunters.extracommands.mixin.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;


public class ClearCommand
{
    /**
     * Registers the command that toggles a pause for the vault.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));

        LiteralArgumentBuilder<CommandSourceStack> complete = Commands.literal("reset").
            executes(ctx -> clearPlayerData(ctx.getSource().getPlayerOrException())).
            then(Commands.argument("player", EntityArgument.players()).
                executes(ctx -> clearPlayerData(EntityArgument.getPlayer(ctx, "player"))));

        dispatcher.register(baseLiteral.then(complete));
    }


    /**
     * This method completes given player active bounty.
     * @param player Player which bounty need to be completed.
     * @return 1
     */
    private static int clearPlayerData(ServerPlayer player)
    {
        // Remove Vault Altar data
        removeVaultAltarData(player);
        removeVaultStatsData(player);

        PlayerVaultStatsData.get(player.getLevel()).resetLevelAbilitiesAndExpertise(player);
        PlayerResearchesData.get(player.getLevel()).resetResearchTree(player);
        PlayerVaultStatsData.get(player.getLevel()).resetKnowledge(player);

        // Remove quests
        QuestStatesData.get().getState(player).reset();

        // Remove history
        VaultDeathSnapshotData.get(player.getLevel()).removeSnapshots(player);
        VaultJoinSnapshotData.get(player.getLevel()).removeSnapshots(player);
        removeVaultHistory(player);

        // Remove bounty data
        BountyData.get().getAllLegendaryFor(player.getUUID()).clear();
        BountyData.get().resetAllBounties(player.getUUID());

        // Reset discoveries
        DiscoveredModelsData.get(player.getLevel()).reset(player.getUUID());
        removeDiscoveries(player);

        // Reset eternals
        removeEternals(player);

        // Reset paradox
        removeParadox(player);

        // Reset blackmarket
        removeBlackMarket(player);

        // Reset greed data
        resetGreedData(player);

        // Reset proficiency
        resetProficiencyData(player);

        // Reset reputation
        resetReputationData(player);

        // Reset spirits
        resetSpiritData(player);

        // Reset titles
        resetTitlesData(player);

        // Reset skill altar data
        resetSkillAltarData(player);

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
            ModNetwork.CHANNEL.sendTo(new UpdateTitlesDataMessage(titlesData.entries),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT);
            titlesData.setDirty();
        }
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
}

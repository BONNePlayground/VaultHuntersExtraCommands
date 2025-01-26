//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.vaulthunters.extracommands.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import lv.id.bonne.vaulthunters.extracommands.data.ExtraCommandsWorldData;
import lv.id.bonne.vaulthunters.extracommands.util.ExtraEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;


@Mixin(value = Vault.class, remap = false)
public class MixinVault
{
    @Inject(method = "initServer", at = @At("TAIL"))
    private void addCustomListeners(VirtualWorld world, CallbackInfo ci)
    {
        Vault vault = (Vault) (Object) this;

        final boolean pauseVaultTime = ExtraCommands.CONFIGURATION.getLoginTimerStop();
        final boolean preventPlayerTargeting = ExtraCommands.CONFIGURATION.getPlayerTargetProtection();

        if (!pauseVaultTime && !preventPlayerTargeting)
        {
            // Login time stop is not enabled. Skip this.
            return;
        }

        // Player leaves vault
        CommonEvents.LISTENER_LEAVE.register(vault, data ->
        {
            // Player leaves the vault. Remove him from tracking list.

            data.getListener().getPlayer().ifPresent(player ->
                this.commands$movementTrackingPlayers.remove(player.getUUID()));
        });

        // Player disconnects from server.
        CommonEvents.ENTITY_LEAVE.register(vault, data ->
        {
            // Player leaves dimension. If he is the only one, enable pause.

            if (data.getWorld() != world)
            {
                // Not vault world.
                return;
            }

            // Remove player from tracking list.
            this.commands$movementTrackingPlayers.remove(data.getEntity().getUUID());

            if (!data.getWorld().players().isEmpty())
            {
                // More than 1 player in vault?
                return;
            }

            if (pauseVaultTime && data.getEntity() instanceof ServerPlayer)
            {
                // Pause vault timer
                ExtraCommandsWorldData worldData = ExtraCommandsWorldData.get(world);
                worldData.setPaused(true, false);
            }
        });

        // Player joins the server
        CommonEvents.ENTITY_JOIN.register(vault, data ->
        {
            if (data.getWorld() != world)
            {
                // Not vault world.
                return;
            }

            if (data.getEntity() instanceof ServerPlayer player &&
                (preventPlayerTargeting || ExtraCommandsWorldData.get(world).isPaused()))
            {
                // World is paused. Add player to tracking list.
                this.commands$movementTrackingPlayers.put(player.getUUID(), player.position());
            }
        });

        // Player tick event
        CommonEvents.PLAYER_TICK.register(vault, data ->
        {
            Vec3 position = this.commands$movementTrackingPlayers.getOrDefault(data.player.getUUID(), null);

            if (position == null)
            {
                // Is not tracking player.
                return;
            }


            if (!MixinVault.commands$insideProtectionRange(position, data.player.position()))
            {
                // Remove player from tracking list and disable pause.
                this.commands$movementTrackingPlayers.remove(data.player.getUUID());

                if (pauseVaultTime)
                {
                    // Resume vault time if timer has been paused.
                    ExtraCommandsWorldData worldData = ExtraCommandsWorldData.get(world);
                    worldData.setPaused(false, false);
                }
            }
        });

        // Disable event registry if target protection is disabled.
        if (!preventPlayerTargeting)
        {
            // Target Protection is not enabled. Skip this.
            return;
        }

        // Entity target player event
        ExtraEvents.ENTITY_TARGET_EVENT.register(vault, data ->
        {
            if (data.getNewTarget() == null)
            {
                return;
            }

            Vec3 position = this.commands$movementTrackingPlayers.getOrDefault(data.getNewTarget().getUUID(), null);

            if (position == null)
            {
                // Is not tracking player.
                return;
            }

            if (MixinVault.commands$insideProtectionRange(position, data.getNewTarget().position()))
            {
                // Do not allow to target player if player have not moved far enough.
                data.setCanceled(true);
            }
        });
    }


    /**
     * This method returns if start and end position is closer than max distance
     * @param start The start position.
     * @param end The end position.
     * @return {@code true} if position is closer than max distance, {@code false} otherwise.
     */
    @Unique
    private static boolean commands$insideProtectionRange(Vec3 start, Vec3 end)
    {
        double dX = end.x() - start.x();
        double dZ = end.z() - start.z();

        return dX * dX + dZ * dZ <= ExtraCommands.CONFIGURATION.getMaxDistance();
    }


    /**
     * The map that stores tracked players and their starting position.
     */
    @Unique
    private final Map<UUID, Vec3> commands$movementTrackingPlayers = new HashMap<>();
}

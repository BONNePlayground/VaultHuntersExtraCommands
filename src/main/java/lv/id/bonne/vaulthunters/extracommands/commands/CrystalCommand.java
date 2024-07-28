//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.objective.*;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;


public class CrystalCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("the_vault_extra").
            requires(stack -> stack.hasPermission(1));
        LiteralArgumentBuilder<CommandSourceStack> crystalLiteral = Commands.literal("crystal");

        LiteralArgumentBuilder<CommandSourceStack> objective = Commands.literal("setObjective").
            then(Commands.argument("objective", EnumArgument.enumArgument(Objective.class)).
                executes(ctx -> setObjective(ctx.getSource().getPlayerOrException(), ctx.getArgument("objective", Objective.class))));

        dispatcher.register(baseLiteral.then(crystalLiteral.then(objective)));
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
        return 1;
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

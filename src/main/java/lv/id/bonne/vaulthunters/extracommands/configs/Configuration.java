package lv.id.bonne.vaulthunters.extracommands.configs;



import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.pylon.PylonBuff;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;


/**
 * The configuration handling class. Holds all the config values.
 */
public class Configuration
{
    /**
     * The constructor for the config.
     */
    public Configuration()
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        this.positiveModifiers = builder.
            comment("The list of positive modifiers").
            defineList("positive",
                List.of("the_vault:prismatic",
                    "the_vault:prosperous",
                    "the_vault:soul_surge",
                    "the_vault:treasure",
                    "the_vault:hoard",
                    "the_vault:enlighted",
                    "the_vault:champion_chance",
                    "the_vault:hoard",
                    "the_vault:copiously",
                    "the_vault:item_rarity",
                    "the_vault:fortuitous",
                    "the_vault:energizing",
                    "the_vault:regeneration",
                    "the_vault:strength",
                    "the_vault:swift",
                    "the_vault:extended",
                    "the_vault:plentiful",
                    "the_vault:ornate_cascade",
                    "the_vault:coin_cascade",
                    "the_vault:living_cascade",
                    "the_vault:gilded_cascade",
                    "the_vault:wooden_cascade",
                    "the_vault:soul_boost",
                    "the_vault:dungeon_doors",
                    "the_vault:treasure_doors",
                    "the_vault:xp_gain"),
                entry -> entry instanceof String value &&
                    (VaultModifierRegistry.getAll().findAny().isEmpty() ||
                        VaultModifierRegistry.getOpt(ResourceLocation.tryParse(value)).isPresent()));

        this.negativeModifiers = builder.
            comment("The list of negative modifiers").
            defineList("negative",
                List.of("the_vault:mob_increase",
                    "the_vault:infuriated_mobs",
                    "the_vault:brutal_mobs",
                    "the_vault:chunky_mobs2",
                    "the_vault:chunky_mobs",
                    "the_vault:slowed",
                    "the_vault:draining",
                    "the_vault:mob_increase",
                    "the_vault:injured",
                    "the_vault:ruthless_mobs",
                    "the_vault:champion_paradox",
                    "the_vault:shortened",
                    "the_vault:grievous_wounds",
                    "the_vault:trapped"),
                entry -> entry instanceof String value &&
                    (VaultModifierRegistry.getAll().findAny().isEmpty() ||
                        VaultModifierRegistry.getOpt(ResourceLocation.tryParse(value)).isPresent()));

        this.curseModifiers = builder.
            comment("The list of curse modifiers").
            defineList("curse",
                List.of("the_vault:antiheal",
                    "the_vault:hunger",
                    "the_vault:tired",
                    "the_vault:slowed",
                    "the_vault:weakened",
                    "the_vault:shulker",
                    "the_vault:explosive",
                    "the_vault:jupiter_gravity",
                    "the_vault:confused",
                    "the_vault:crab_walk",
                    "the_vault:volcanic",
                    "the_vault:collapsing",
                    "the_vault:minefield",
                    "the_vault:drained",
                    "the_vault:wounded",
                    "the_vault:rotten",
                    "the_vault:frenzy",
                    "the_vault:locked",
                    "the_vault:inert",
                    "the_vault:ethereal",
                    "the_vault:mana_leak",
                    "the_vault:crit_mobs",
                    "the_vault:lost_quantity",
                    "the_vault:enervated",
                    "the_vault:soulless",
                    "the_vault:fading"
                ),
                entry -> entry instanceof String value &&
                    (VaultModifierRegistry.getAll().findAny().isEmpty() ||
                        VaultModifierRegistry.getOpt(ResourceLocation.tryParse(value)).isPresent()));

        this.chaoticModifiers = builder.
            comment("The list of modifiers that will be added with chaotic command").
            defineList("chaotic",
                List.of("the_vault:wild",
                    "the_vault:furious_mobs",
                    "the_vault:chunky_mobs",
                    "the_vault:trapped",
                    "the_vault:infuriated_mobs",
                    "the_vault:gilded_cascade",
                    "the_vault:ornate_cascade",
                    "the_vault:living_cascade",
                    "the_vault:wooden_cascade",
                    "the_vault:plentiful",
                    "the_vault:item_quantity",
                    "the_vault:item_rarity"),
                entry -> entry instanceof String value &&
                    (VaultModifierRegistry.getAll().findAny().isEmpty() ||
                        VaultModifierRegistry.getOpt(ResourceLocation.tryParse(value)).isPresent()));

        this.protectedModifiers = builder.
            comment("The list of modifiers that will be protected from adding or removing. \n Blacklist essentially.").
            defineList("blacklist",
                List.of("the_vault:soul_flame",
                    "the_vault:chaotic",
                    "the_vault:soul_objective"),
                entry -> entry instanceof String value &&
                    (VaultModifierRegistry.getAll().findAny().isEmpty() ||
                        VaultModifierRegistry.getOpt(ResourceLocation.tryParse(value)).isPresent()));

        this.pylonEffects = builder.
            comment("The list of pylon effects that can be applied to a player. Pylon effects can be found in `pylon_placeholder.json` files in ").
            comment("the_vault/gen/1.0/palettes/generic/").
            defineList("pylonEffects",
                List.of(
                    "the_vault:pylon{Config:{type:effect,effect:regeneration,amplifier:3,duration:300,color:695fe80d,description:\"Grants +3 Regeneration for 15 seconds\"}}",
                    "the_vault:pylon{Config:{type:attribute,attribute:generic.max_health,amount:8.0,operation:ADDITION,duration:2400,color:6900FF00,description:\"Grants 4 bonus hearts for 2 minutes\"}}",
                    "the_vault:pylon{Config:{type:stat,stat:ITEM_RARITY,addend:0.20,duration:1200,color:69ff9c00,description:\"+20% Item Rarity for 1 minute\"}}",
                    "the_vault:pylon{Config:{type:effect,effect:regeneration,amplifier:10,duration:60,color:69A3FF67,description:\"Regenerates all your health\"}}",
                    "the_vault:pylon{Config:{type:effect,effect:regeneration,amplifier:1,duration:2400,color:69ff42ec,description:\"Grants +1 Regeneration for 2 minutes\"}}",
                    "the_vault:pylon{Config:{type:effect,effect:speed,amplifier:1,duration:3600,color:69e115f3,description:\"Grants +1 Speed for 3 minutes\"}}",
                    "the_vault:pylon{Config:{type:effect,effect:strength,amplifier:1,duration:3600,color:69e21b1b,description:\"Grants +1 Strength for 3 minutes\"}}",
                    "the_vault:pylon{Config:{type:effect,effect:absorption,amplifier:1,duration:2400,color:6900FF00,description:\"Grants 2 absorption hearts for 2 minutes\"}}",
                    "the_vault:pylon{Config:{type:potion,charges:2,color:6900FF00,description:\"Recharges 2 potion charges\"}}",
                    "the_vault:pylon{Config:{type:mana,missingManaPercent:1.0,color:692060b7,description:\"Refills your mana\"}}",
                    "the_vault:pylon{Config:{type:time,ticks:1200,color:69FFFF00,description:\"Adds 1 minute to the vault timer\"}}",
                    "the_vault:pylon{Config:{type:time,ticks:2400,color:69fff600,description:\"Adds 2 minutes to the vault timer\"}}",
                    "the_vault:pylon{Config:{type:stat,stat:ITEM_RARITY,addend:0.20,duration:1200,color:69EAFF00,description:\"+20% Item Rarity for 1 minute\"}}",
                    "the_vault:pylon{Config:{type:stat,stat:ITEM_QUANTITY,addend:0.20,duration:1200,color:69ff9c00,description:\"+20% Item Quantity for 1 minute\"}}",
                    "the_vault:pylon{Config:{type:stat,stat:DURABILITY_WEAR_REDUCTION,addend:0.5,duration:6000,color:6985e8e7,description:\"+50% Durability Damage Reduction for 5 minutes\"}}",
                    "the_vault:pylon{Config:{type:stat,stat:DURABILITY_WEAR_REDUCTION,addend:1.0,duration:6000,color:69c5f0f0,description:\"+100% Durability Damage Reduction for 5 minutes\"}}",
                    "the_vault:pylon{Config:{type:stat,stat:ITEM_RARITY,addend:1.00,duration:6000,color:69EAFF00,description:\"+100% Item Rarity for 5 minutes\",uber:1b,uberColor:69ff8a14}}",
                    "the_vault:pylon{Config:{type:stat,stat:ITEM_QUANTITY,addend:1.00,duration:6000,color:69ff9c00,description:\"+100% Item Quantity for 5 minutes\",uber:1b,uberColor:69ff9c00}}",
                    "the_vault:pylon{Config:{type:time,ticks:6000,color:69FFFF00,description:\"Adds 5 minutes to the vault timer\",uber:1b,uberColor:6954f9ff}}",
                    "the_vault:pylon{Config:{type:effect,effect:speed,amplifier:3,duration:6000,color:69e115f3,description:\"Grants +3 Speed for 5 minutes\",uber:1b,uberColor:69ffff87}}",
                    "the_vault:pylon{Config:{type:effect,effect:strength,amplifier:10,duration:6000,color:69e21b1b,description:\"Grants +10 Strength for 5 minutes\",uber:1b,uberColor:69fa52a9}}",
                    "the_vault:pylon{Config:{type:effect,effect:regeneration,amplifier:2,duration:6000,color:69ff42ec,description:\"Grants +2 Regeneration for 5 minutes\",uber:1b,uberColor:695fe80d}}",
                    "the_vault:pylon{Config:{type:stat,stat:LUCKY_HIT_CHANCE,addend:0.50,duration:6000,color:696df5a3,description:\"+50% Lucky Hit Chance for 5 minutes\",uber:1b,uberColor:69fff705}}",
                    "the_vault:pylon{Config:{type:stat,stat:SOUL_CHANCE,addend:1.0,duration:6000,color:696900cc,description:\"+100% Soul Shard Chance for 5 minutes\",uber:1b,uberColor:69f03232}}",
                    "the_vault:pylon{Config:{type:stat,stat:COPIOUSLY,addend:0.3,duration:6000,color:69f74780,description:\"+30% Copiously Chance for 5 minutes\",uber:1b,uberColor:69ffffff}}",
                    "the_vault:pylon{Config:{type:potion,charges:6,color:6900FF00,description:\"Recharges your potion fully\"},uber:1b,uberColor:69ffffff}}"
                ),
                obj -> obj instanceof String val && val.startsWith("the_vault:pylon{Config:") && val.endsWith("}"));

        builder.push("Command Section");

        this.pauseAblePlayers =
            builder.comment("List of UUID's that can run `/pause` command in the vault that tick-freezes it.").
               defineList("pauseUUID", Collections.emptyList(), o -> o instanceof String);

        builder.pop();

        Configuration.GENERAL_SPEC = builder.build();
    }


    public List<ResourceLocation> getPositiveModifiers()
    {
        return toResourceLocation(this.positiveModifiers.get());
    }


    public List<ResourceLocation> getNegativeModifiers()
    {
        return toResourceLocation(this.negativeModifiers.get());
    }


    public List<ResourceLocation> getCurseModifiers()
    {
        return toResourceLocation(this.curseModifiers.get());
    }


    public List<ResourceLocation> getChaoticModifiers()
    {
        return toResourceLocation(this.chaoticModifiers.get());
    }


    public List<ResourceLocation> getProtectedModifiers()
    {
        return toResourceLocation(this.protectedModifiers.get());
    }

    public Set<UUID> getPlayersWithPausePermission()
    {
        return this.pauseAblePlayers.get().stream().map(UUID::fromString).collect(Collectors.toUnmodifiableSet());
    }

    public List<? extends PylonBuff.Config> getPylonEffects()
    {
        return toPylonBuff(this.pylonEffects.get());
    }


    public void addPlayerToPauseList(UUID uuid)
    {
        Set<UUID> playerSet = this.getPlayersWithPausePermission();

        if (!playerSet.contains(uuid))
        {
            List<UUID> playerList = new ArrayList<>(playerSet);
            playerList.add(uuid);
            this.pauseAblePlayers.set(playerList.stream().sorted().map(UUID::toString).collect(Collectors.toList()));
            this.pauseAblePlayers.save();
        }
    }


    public void removePlayerFromPauseList(UUID uuid)
    {
        Set<UUID> playerSet = this.getPlayersWithPausePermission();

        if (playerSet.contains(uuid))
        {
            List<UUID> playerList = new ArrayList<>(playerSet);
            playerList.remove(uuid);
            this.pauseAblePlayers.set(playerList.stream().sorted().map(UUID::toString).toList());
            this.pauseAblePlayers.save();
        }
    }


    private static List<ResourceLocation> toResourceLocation(List<? extends String> list)
    {
        return list.stream().
            map(ResourceLocation::tryParse).
            filter(value -> VaultModifierRegistry.getOpt(value).isPresent()).
            toList();
    }


    /**
     * This method transforms all input strings as pylon config buffs.
     * @param list List of strings that need to be converted
     * @return List of pylon buff configs.
     */
    private static List<? extends PylonBuff.Config> toPylonBuff(List<? extends String> list)
    {
        return list.stream().
            map(text ->
            {
                try
                {
                    return TagParser.parseTag(text.substring(23, text.length() - 1));
                }
                catch (CommandSyntaxException ignored)
                {
                    return null;
                }
            }).
            filter(Objects::nonNull).
            map(PylonBuff.Config::fromNBT).
            filter(Objects::nonNull).
            toList();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * The config value for listing positive modifiers
     */
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> positiveModifiers;

    /**
     * The config value for listing negative modifiers
     */
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> negativeModifiers;

    /**
     * The config value for listing curse modifiers
     */
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> curseModifiers;

    /**
     * The config value for listing chaotic modifiers
     */
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> chaoticModifiers;

    /**
     * The config value for listing protected modifiers
     */
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> protectedModifiers;

    /**
     * The config value for listing pylon effects
     */
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> pylonEffects;

    /**
     * The config value for listing pylon effects
     */
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> pauseAblePlayers;

    /**
     * The general config spec.
     */
    public static ForgeConfigSpec GENERAL_SPEC;
}

package lv.id.bonne.vaulthunters.extracommands.configs;



import java.util.Arrays;
import java.util.List;

import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
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
                Arrays.asList("the_vault:prismatic",
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
                Arrays.asList("the_vault:mob_increase",
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
                Arrays.asList("the_vault:antiheal",
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
                Arrays.asList("the_vault:wild",
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
                Arrays.asList("the_vault:soul_flame",
                    "the_vault:chaotic",
                    "the_vault:soul_objective"),
                entry -> entry instanceof String value &&
                    (VaultModifierRegistry.getAll().findAny().isEmpty() ||
                        VaultModifierRegistry.getOpt(ResourceLocation.tryParse(value)).isPresent()));

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


    private static List<ResourceLocation> toResourceLocation(List<? extends String> list)
    {
        return list.stream().
            map(ResourceLocation::tryParse).
            filter(value -> VaultModifierRegistry.getOpt(value).isPresent()).
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
     * The general config spec.
     */
    public static ForgeConfigSpec GENERAL_SPEC;
}

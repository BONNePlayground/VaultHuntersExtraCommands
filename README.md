<a href="https://www.curseforge.com/minecraft/mc-mods/vault-hunters-extra-game-commands"><img src="http://cf.way2muchnoise.eu/1062425.svg" alt="CF"></a>

# Vault Hunters Extra Commands

This simple mod adds some new Game Commands to allow more adjustments to the Vault.

## Game Commands

Adding Vault Modifiers Commands:
- `/the_vault_extra vault addModifier positive [<player>] [<number>]` - adds random modifier (-s) from positive pool in config
- `/the_vault_extra vault addModifier negative [<player>] [<number>]` - adds random modifier (-s) from negative  pool in config
- `/the_vault_extra vault addModifier curse [<player>] [<number>]` - adds random modifier (-s) from curse pool in config
- `/the_vault_extra vault addModifier specific <player> <modifier> [<number>]` - adds specific modifier (-s)
- `/the_vault_extra vault addModifier chaotic [<player>] <number>` - adds random modifiers from chaotic pool in config

Removing Vault Modifiers Commands:
- `/the_vault_extra vault removeModifier positive [<player>] [<number>]` - removes random modifier (-s) from positive pool in config
- `/the_vault_extra vault removeModifier negative [<player>] [<number>]` - removes random modifier (-s) from negative  pool in config
- `/the_vault_extra vault removeModifier curse [<player>] [<number>]` - removes random modifier (-s) from curse pool in config
- `/the_vault_extra vault removeModifier specific <player> <modifier> [<number>]` - removes specific modifier (-s)
- `/the_vault_extra vault removeModifier random [<player>] [<number>]` - removes random modifier (-s) that are applied 

Adding Vault Pylon effects Command:
- `/the_vault_extra vault pylon [<player>] [<pylon>]` - triggers random pylon effect on player from pylon pool in config

Toggling Vault Clock Commands:
- `/the_vault_extra vault pause [<player>]` - toggles vault to a complete stop. Mobs cannot move.
- `/the_vault_extra vault time increase <player> <seconds>` - adds seconds to player vault timer.
- `/the_vault_extra vault time reduce <player> <seconds>` - reduces seconds from player vault timer.
- `/the_vault_extra vault time stop <player>` - toggles vault time stop only. Mobs still can move.

- `/pause` - alias to toggle vault complete stop (tick freeze), however, server owner can speccify in config who can run it.
- `/pause add <player>` - allows server owner to add player to the person list who can execute pause command.
- `/pause remove <player>` - allows server owner to remove player from the person list who can execute pause command.

Kicking from the vault Command:
- `/the_vault_extra vault kick [<player>] [true]` - kicks player from vault. Adding true at the end allows to kick with completion trigger.

God Altar in the vault Commands:
- `/the_vault_extra vault godAltar complete [<player>]` - completes god altar for a player.
- `/the_vault_extra vault godAltar fail [<player>]` - fails god altar for a player.

Bounty Commands:
- `/the_vault_extra bounty complete [<player>] [<bounty_id>]` - completes bounty for a player.

Permanent Time Adjusting Commands:
- `/the_vault_extra vault_time add <player> <seconds>` - adds seconds to all player's vaults permanently.
- `/the_vault_extra vault_time remove <player> <seconds>` - remove seconds from all player's vaults permanently.
- `/the_vault_extra vault_time get <player>` - returns current permanently added seconds for player.

Altar Commands:
- `/the_vault_extra altar complete [<player>]` - completes altar for a player.
- `/the_vault_extra altar reload [<player>]` - reloads altar for a player.

Reset Command:
- `/the_vault_extra reset [<player>] [<mode>]` - resets player data. Without mode, it clears everything.

Proficiency Commands:
- `/the_vault_extra proficiency increase <player> <type> <amount>` - increases players proficiency in selected type.
- `/the_vault_extra proficiency reduce <player> <type> <amount>` - reduces players proficiency in selected type.

Discoveries Unlock Command:
- `/the_vault_extra discoveries model <player> <item> <model>` - unlocks requested model for player.
- `/the_vault_extra discoveries trinket <player> <trinket>` - unlocks requested trinket for player. 
- `/the_vault_extra discoveries alchemy <player> <effect>` - unlocks requested alchemy effect for player. 
- `/the_vault_extra discoveries workbench <player> <item> <modifier>` - unlocks requested workbench modifier for player. 

Locate Commands:
- `/the_vault_extra locate angel_block` - command lists all known (loaded) angel blocks and allows to teleport to them
- `/the_vault_extra locate angel_block <distance>` - command sends green particles around angel blocks around player in requested distance

Gear Debug commands:
- `/the_vault_extra gear_debug rollLegendary [<mode>]` - allows to rollLegendary modifier on your gear: 
  - NONE - rolls once, if it has legendary modifier, then you do not get another chance.
  - ADD - rolls on all modifiers, and allow to have multiple (only prefixes/suffixes)
  - REROLL - finds legendary modifier and rerolls it.
  - IMPLICIT - adds legendary modifier to the implicit modifiers.
- `/the_vault_extra gear_debug repairs fix` - removes used gear slot by 1 on gear player is holding
- `/the_vault_extra gear_debug repairs break` - adds used gear slot by 1 on gear player is holding
- `/the_vault_extra gear_debug repairs setSlots <number>` - sets the repair solt count on gear player is holding.
- `/the_vault_extra gear_debug rarity <rarity>` - sets gear piece rarity. (this is only display thing)
- `/the_vault_extra gear_debug model <model>` - sets gear piece model
- `/the_vault_extra gear_debug potential <number>` - sets gear piece crafting potential value 
- `/the_vault_extra gear_debug addModifier <affix> [<modifier> <value> <number>]` - adds modifier* with given value to affix type. This method does not bypass limits.
- `/the_vault_extra gear_debug addModifierForce <affix> [<modifier> <value> <number>]` - adds modifier* with given value to affix type. This method bypass limits.
- `/the_vault_extra gear_debug removeModifier <affix> [<modifier>]` - removes modifier from given affix.

Crystal Commands:
- `/the_vault_extra crystal setObjective <Objective>` - sets objective to a crystal in player hands.
- `/the_vault_extra crystal setTheme <Theme>` - sets theme to a crystal in player hands.
- `/the_vault_extra crystal setLayout <layout> [<tunnelLength> <radius> <rotation>]` - sets layout to a crystal in player hands. Some layouts require extra parameters.

### Configuration
The configuration file allows to:
- change the base command tag (`the_vault_extra`). Setting it to `the_vault` will merge commands. 
- list of player uuids that can run `/pause` command.
- list of positive modifiers
- list of negative modifiers
- list of curse modifiers
- list of chaos modifiers
- list of pylon effects
- blacklist of modifiers that can not be added.
- login protection:
  - max distance - a distance of blocks where protection works
  - time pause - ability to stop vault timer while player is in protection area
  - target protection - ability to prevent player to be targeted while in protection area

### Note:
Not all gear modifiers can be added. Some of them requires too many input fields, like Effect Clouds. The issue is with chat input size, as it is too small to write all required text.
Commands can be executed by console (requires <player> to be filled) or by OP (server operator). (except /pause)
Commands have auto-complete, but they are available only if the mod is installed on the client side (due to how Minecraft commands work).

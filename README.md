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
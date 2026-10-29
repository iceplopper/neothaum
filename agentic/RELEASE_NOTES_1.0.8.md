# NeoThaum 1.0.8 (alpha)

Includes everything from [1.0.7](https://github.com/iceplopper/neothaum/releases/tag/v1.0.7-alpha), plus the changes below. This release
fills the last gaps known from Thaumcraft 4.2.3.5.

Two downloads:
- **`neothaum-1.0.8.jar`:** the mod alone.
- **`NeoThaum-1.0.8.mrpack`:** a Modrinth modpack. It contains the mod with Fabric API, ModMenu, JEI, Jade and Xaero's Minimap and World Map, set up for Minecraft 26.3 with Fabric Loader 0.20.1.

## Major features

- **Obsidian totems.** About one chunk in 360 in the Overworld has an obsidian totem pillar on an obsidian tile. A new **Charged Obsidian Totem** sits on top and holds a dark aura node. Breaking the charged totem gives ethereal essences from its node. The config screen has a slider for how often totems appear.
- **Nodes over temples.** Desert temples, jungle temples and witch huts now have an aura node floating 3 blocks above them, as in TC4.
- **Tin, silver and lead.** If another mod adds tin, silver or lead ingots, TC4's native clusters and nuggets for that metal turn on:
  - the purification and transmutation research
  - crucible recipes
  - smelting clusters into ingots, and crafting nuggets into ingots and back
  - the arcane bore and excavation focus turning those ores into clusters
  - extra nuggets when smelting the ore
  - Pech trades

  Without such a mod, nothing changes.
- **`/neothaum` command (alias `/nt`).** TC4's admin command, for operators:
  - `research <player> <key|all|reset>` (a single research comes with its requirements) and `research list`
  - `aspect <player> <aspect|all> <amount>`
  - `warp <player> <add|set> <amount> [PERM|TEMP]`

## Minor features

- **Floating special items.** Primordial pearls dropped by bosses, crucible products and a Thaumonomicon made from a bookshelf now hover in place and survive explosions.
- **Sounds:** the crucible's boiling, loot urns breaking and the cultist portal's hum now use TC4's sounds.
- **Eldritch Obelisk Placer:** a creative-only item that raises an eldritch altar and obelisk.
- **Smaller fixes:**
  - **Runic shielding now protects players.** Several protections and mob effects never applied to players because of a bug, and now do:
    - runic shields from rings, amulets, girdles and armour
    - fortress armour's damage reduction and its masks
    - champion mobs' poison, wither, fire, hunger, life-drain and warp hits
  - **Hoe of Growth:** it no longer uses up durability on a single greatwood sapling, which can't grow. Used on a full 2×2 of greatwood saplings, it now always grows the tree.
  - The silver wand cap research appears only when a mod adds silver ingots, as in TC4.

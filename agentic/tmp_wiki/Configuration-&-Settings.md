# Configuration & In-Game Settings

**Category:** Getting Started | [Return to Wiki Home](Home)

---

## Overview

NeoThaum features an extensive configuration architecture designed for modpack creators and server administrators. All settings are serialized in `config/neothaum.json` and can be adjusted live in-game using the **ModMenu** interface.

---

## In-Game Configuration GUI (ModMenu)

If you have [ModMenu](Mod-Compatibility) installed:
1. Open the Minecraft pause menu and select **Mods**.
2. Locate **NeoThaum** in the list.
3. Click the **Configure** button in the top-right corner.

Settings are organized into six distinct tabs:

### 1. Aura Nodes Tab
| Setting | Default | Description |
| :--- | :---: | :--- |
| **Node Spawn Rarity** | `1 in 18 chunks` | Frequency with which raw aura nodes attempt generation in new chunks. |
| **Pure Node Weight** | `12%` | Relative spawn rate of Pure (Silverwood-aligned) aura nodes. |
| **Dark / Sinister Node Weight** | `10%` | Relative spawn rate of Dark nodes (often found near barrows and obelisks). |
| **Unstable Node Weight** | `8%` | Relative spawn rate of nodes whose aspect composition fluctuates. |
| **Hungry Node Weight** | `3%` | Relative spawn rate of gravitational black-hole nodes that consume nearby matter. |
| **Tainted Node Weight** | `5%` | Relative spawn rate of nodes infected by fibrous taint. |

### 2. Biomes Tab
| Setting | Default | Description |
| :--- | :---: | :--- |
| **Generate Magical Forest** | `Enabled` | Master toggle for the vibrant turquoise Magical Forest biome. |
| **Magical Forest Size** | `Medium (4)` | Regional scale of Magical Forest biome clusters. |
| **Generate Tainted Lands** | `Enabled` | Master toggle for naturally generating infected Tainted Land biomes. |
| **Tainted Land Size** | `Small (2)` | Regional scale of Tainted Land blights. |

### 3. World Structures Tab
Controls spawn spacing and frequency for individual world structures:
- **Eldritch Rings & Obelisks**: Obsidian monuments topped with runic capstones.
- **Barrow Mounds & Hilltop Stones**: Ancient stone arches guarding subterranean crypts.
- **Wizard Spire Towers**: Multi-tier towers with research lofts and brewing basements.
- **Village Banker Houses**: Thaumic trade offices within NPC villages.

### 4. Taint Spread Tab
| Setting | Default | Description |
| :--- | :---: | :--- |
| **Taint Spread Enabled** | `True` | Whether fibrous taint actively infects adjacent dirt, stone, and trees. |
| **Spread Interval Ticks** | `100 ticks` | Speed of taint propagation cycles. |
| **Mob Corruption Mode** | `Corrupt Existing` | If set to `Corrupt Existing`, passive animals dying on tainted soil transform into tainted beasts. If set to `Spawn Only`, tainted mobs only spawn via natural mob spawning. |

### 5. Flux & Alchemy Tab
| Setting | Default | Description |
| :--- | :---: | :--- |
| **Crucible Flux Spill Multiplier** | `1.0x` | Scaling factor for flux gas and flux goo generated when boiling aspects dissolve. |
| **Infernal Furnace Smelt Speed** | `1.0x` | Smelting tick duration multiplier for the multiblock furnace. |
| **Infernal Bonus Nugget Yield** | `1.0x` | Frequency multiplier for bonus rare nuggets during metal smelting. |

### 6. Developer & Debug Tab
- **Show Creative Debug Tab**: Toggles visibility of internal developer testing tools and aspect generators in the creative inventory.

---

## Manual File Configuration (`neothaum.json`)

On dedicated servers or automated modpack builders, the configuration file is located at:
`<server_root>/config/neothaum.json`

Example snippet:
```json
{
  "nodeRarity": 18,
  "nodeWeights": {
    "normal": 62,
    "pure": 12,
    "dark": 10,
    "unstable": 8,
    "hungry": 3,
    "tainted": 5
  },
  "taintSpread": true,
  "taintSpreadInterval": 100,
  "corruptExistingMobs": true,
  "fluxSpillMultiplier": 1.0,
  "infernalFurnaceSpeedMultiplier": 1.0,
  "showDebugCreativeTab": false
}
```

---

## Related Topics
- [Getting Started](Getting-Started)
- [Mod Compatibility](Mod-Compatibility)
- [Aura Nodes](Aura-Nodes)
- [Taint Mechanics](Taint-Mechanics)
- [Home](Home)

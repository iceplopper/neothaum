import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

getting_started = """# Getting Started with NeoThaum

**Category:** Getting Started | [Return to Wiki Home](Home)

---

## Overview

Welcome to **NeoThaum**, a faithful recreation of Thaumcraft 4.2.3.5 for modern Minecraft. Thaumaturgy is the study of magical energy (called **Vis**) and the extraction, manipulation, and crystallization of physical matter through its constituent spiritual **Aspects**.

This guide outlines your path from an ordinary adventurer to an enlightened Thaumaturge.

---

## 1. Crafting Your First Wand

The **Iron-Capped Wooden Wand** is the fundamental implement of every novice Thaumaturge. Without a wand, you cannot manipulate thaumaturgical blocks, form multiblocks, or craft arcane items.

### Materials Required
- 1x Stick
- 2x Iron Nuggets

### Crafting Recipe
In a standard vanilla Crafting Table:
```
[ Iron Nugget ] [    Empty    ] [    Empty    ]
[    Empty    ] [    Stick    ] [    Empty    ]
[    Empty    ] [    Empty    ] [ Iron Nugget ]
```

> [!NOTE]
> The basic Wooden Wand can store up to **25 Vis** of each of the six primal aspects (Aer, Terra, Ignis, Aqua, Ordo, and Perditio). Because of its crude iron caps, it carries a **110% vis cost penalty** (spells and recipes consume 10% more vis).

---

## 2. Obtaining the Thaumonomicon

The **Thaumonomicon** is your research journal and mystical grimoire. It documents every discovery, recipe, multiblock structure, and arcane mechanic in the mod.

### Step 1: Craft Salis Mundus (World Salt)
Salis Mundus is an alchemical powder that awakens latent magic in mundane objects.

**Recipe (Shapeless):**
- 1x Bowl
- 1x Redstone Dust
- 1x Flint
- 1x Elemental Crystal Shard (Air, Earth, Fire, Water, Order, or Entropy — mined from subterranean Infused Stones)

### Step 2: Awaken the Bookshelf
1. Place a mundane **Bookshelf** anywhere in the world.
2. Hold your **Iron-Capped Wooden Wand** in your main hand and right-click the Bookshelf.
3. The bookshelf will dissolve in a swirl of arcane runes, dropping your personal **Thaumonomicon**.

```
[Bookshelf] + [Right-Click with Wand] ➔ [Thaumonomicon]
```

---

## 3. Crafting the Thaumometer

To unlock entries in the Thaumonomicon, you must understand the elemental makeup of the physical world. The **Thaumometer** is an optical brass instrument that allows you to scan blocks, items, and entities to discover their constituent aspects.

### Crafting Recipe
- 4x Gold Nuggets
- 1x Glass Pane

```
[    Empty    ] [ Gold Nugget ] [    Empty    ]
[ Gold Nugget ] [ Glass Pane  ] [ Gold Nugget ]
[    Empty    ] [ Gold Nugget ] [    Empty    ]
```

### How to Use the Thaumometer
- Hold the Thaumometer and hold **Right-Click** while looking at any block, dropped item, or living mob.
- If you have the prerequisite aspect knowledge, the scan will succeed with a chime, rewarding you with research aspect points and displaying the item's aspect breakdown in the bottom-right corner.
- If an object contains aspects you haven't discovered yet, the HUD will warn: *"You lack the research to understand this."* (See [Aspects Overview](Aspects-Overview) to learn how to combine aspects).

---

## 4. Setting Up the Arcane Worktable & Research Table

### The Arcane Worktable
Ordinary crafting tables cannot channel primal vis.
1. Place a standard vanilla **Crafting Table** on the ground.
2. Right-click it with your **Wand**.
3. It will morph into an **Arcane Worktable**. Place your wand in the top-right slot of its GUI to power wand-crafted recipes.

### The Research Table
The Research Table is where you solve research notes using Scribing Tools and Paper.
1. Craft **two Wood Tables** (3x Wood Slabs across the top, 2x Wood Planks underneath).
2. Place the two tables side-by-side.
3. Craft **Scribing Tools** (1x Glass Phial, 1x Ink Sac, 1x Feather in a shapeless recipe).
4. Right-click the adjacent tables with the Scribing Tools.
5. They will join into a single **Research Table** featuring parchment, inkwells, and an illuminated aspect wheel.

---

## 5. Crafting the Goggles of Revealing

As soon as you obtain a small amount of brass/thaumium and vis, prioritize crafting the **Goggles of Revealing**.

### Benefits
- **Aura Nodes**: Allows you to see hidden Aura Nodes floating in the sky and caves, including their aspect composition.
- **Essentia Jars & Alembics**: Displays the exact quantity and aspect type stored in jars and crucibles without needing a Thaumometer.
- **Vis Discount**: Provides a **5% Vis Discount** on all wand casting.

---

## 6. Video Tutorial

For a visual demonstration of the early-game progression:

[![Thaumcraft 4 Early Game Tutorial](https://img.youtube.com/vi/aZ3eH7T4b98/0.jpg)](https://www.youtube.com/watch?v=aZ3eH7T4b98)
*(Tutorial courtesy of Direwolf20 / TC4 Mod Spotlight)*

---

## Related Topics
- [Aspects Overview](Aspects-Overview)
- [Research Mechanics](Research-Mechanics)
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Aura Nodes](Aura-Nodes)
- [Home](Home)
"""

config_settings = """# Configuration & In-Game Settings

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
"""

mod_compat = """# Mod Compatibility & Integrations

**Category:** Getting Started | [Return to Wiki Home](Home)

---

## Overview

NeoThaum is built to integrate with the modern Fabric modding ecosystem. It includes native hooks, custom renderers, and custom GUI integration for popular utility and quality-of-life mods.

---

## Supported Integrations

### 1. Just Enough Items (JEI)
NeoThaum registers dedicated JEI plugin categories that display all custom thaumaturgical crafting methods:

- **Arcane Crafting**: Displays recipes requiring primal vis channeled from wands in the Arcane Worktable. Shows required vis crystals and wand aspect costs.
- **Crucible Alchemy**: Displays the boiling aspect requirements, catalyst item, and resulting transmutation output.
- **Infusion Altar Rituals**: Shows the central item, surrounding pedestal sacrifice ingredients, required liquid Essentia aspects, and instability rating (Negligible, Minor, Moderate, High, Dangerous).

```mermaid
graph LR
    JEI["JEI Screen"] --> AW["Arcane Worktable Tab"]
    JEI --> CR["Crucible Transmutation Tab"]
    JEI --> INF["Infusion Ritual Tab"]
```

---

### 2. Jade / Waila HUD
NeoThaum provides contextual in-world tooltips via Jade:

- **Aura Nodes**: When wearing the **Goggles of Revealing**, hovering over an Aura Node displays its exact node type (e.g. *Bright Hungry*, *Pure Normal*), and a full bar breakdown of its current vs maximum Vis for all present aspects.
- **Crucibles**: Shows whether the water is currently boiling and lists the dissolved aspect slurry contained within.
- **Essentia Jars & Alembics**: Displays the stored aspect name, icon, and current count (e.g., `48 / 64 Ignis`).
- **Pedestals & Matrix**: Displays the current item resting on pedestals and infusion progress.

---

### 3. ModMenu
NeoThaum implements the `ModMenuApi` interface, offering an interactive in-game settings screen that eliminates the need to edit raw JSON configuration files.

---

### 4. Xaero's World Map & Minimap
- **Discovered Node Overlay**: NeoThaum adds a dedicated button to Xaero's World Map GUI (placed neatly in the map controls column). Clicking this button toggles the display of discovered Aura Nodes on the world map.
- **Radar Entity Integration**: Minimap radar properly renders icons and threat rings for Tainted creatures, Eldritch Guardians, Crimson Cultists, and Champion mobs.

---

## Recommended Companion Mods Table

| Mod | Platform | Function |
| :--- | :--- | :--- |
| **[JEI](https://modrinth.com/mod/jei)** | Modrinth / CurseForge | Recipe viewer for Crucible, Arcane, and Infusion recipes. |
| **[Jade](https://modrinth.com/mod/jade)** | Modrinth / CurseForge | In-world block and entity HUD inspector. |
| **[ModMenu](https://modrinth.com/mod/modmenu)** | Modrinth / CurseForge | In-game configuration and settings editor. |
| **[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map)** | Modrinth / CurseForge | Fullscreen global map with Aura Node markers. |
| **[Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)** | Modrinth / CurseForge | Radar and waypoint tracking. |

---

## Related Topics
- [Getting Started](Getting-Started)
- [Configuration & Settings](Configuration-&-Settings)
- [Home](Home)
"""

faq = """# Troubleshooting & FAQ

**Category:** Help & Support | [Return to Wiki Home](Home)

---

## Frequently Asked Questions

### 1. General & Early Game

#### Q: How do I open the Thaumonomicon?
Right-click a placed vanilla **Bookshelf** with any crafted **Wand** (such as the Iron-Capped Wooden Wand).

#### Q: Why can't I scan certain items with the Thaumometer?
If you scan an item and receive the message *"You lack the research to understand this"*, it means that item contains a compound aspect you have not yet synthesized or discovered. Open your Thaumonomicon to the Research Table and combine lower-tier aspects to discover the missing compound.

#### Q: My wand ran out of Vis! How do I recharge it?
1. **Find an Aura Node**: Equip Goggles of Revealing or hold a Thaumometer to locate a glowing sphere in the sky or subterranean caverns.
2. **Right-Click**: Hold right-click while aiming at the node with your wand to siphon vis.
3. *Warning*: Do not drain a node down to 0 vis, or it may permanently decay into a Pale or Fading node!

---

### 2. Alchemy & Crucibles

#### Q: Why is my Crucible not melting items?
1. The Crucible must contain water (right-click with a water bucket).
2. The Crucible must be **boiling**. Place a heat source directly underneath it: Fire, Lava, Magma Block, or a **Nitor** flame. Wait until you see steam rising from the surface.

#### Q: Purple smoke and slime are bubbling out of my Crucible! What happened?
When aspects sit in a boiling crucible for too long without being consumed by a catalyst, they decompose into **Flux**. This spawns purple flux gas (which inflicts potion blights) and **Flux Slime** entities. Clean your crucibles promptly or construct a [Thaumatorium](Thaumatorium) to automate recipes cleanly.

---

### 3. Infusion Altars

#### Q: My Infusion Altar keeps exploding or knocking items off pedestals!
Your altar is suffering from **Instability**.
1. **Check Symmetry**: Every Arcane Pedestal, Skull, Candle, and Crystal Cluster must have an identical match on the exact opposite side of the central Runic Matrix ($X_{altar} - \Delta x$, $Z_{altar} - \Delta z$).
2. **Add Stabilizers**: Place Mob Heads, Skulls, and Infused Candles in symmetrical patterns on the floor beneath the altar.
3. **Verify Essentia**: Make sure you have enough essentia jars within range before beginning the craft.

---

### 4. Technical & Performance

#### Q: Where can I report a bug or crash?
Please report all bugs on our official GitHub Issue Tracker:
👉 **[https://github.com/iceplopper/neothaum/issues](https://github.com/iceplopper/neothaum/issues)**

When filing an issue, please include:
1. Minecraft and Fabric Loader versions.
2. Your mod list.
3. The crash log from `.minecraft/crash-reports/` or `.minecraft/logs/latest.log`.

---

## Related Topics
- [Getting Started](Getting-Started)
- [Infusion Altar](Infusion-Altar)
- [Crucible Alchemy](Crucible-Alchemy)
- [Home](Home)
"""

for fname, text in [
    ("Getting-Started.md", getting_started),
    ("Configuration-&-Settings.md", config_settings),
    ("Mod-Compatibility.md", mod_compat),
    ("Troubleshooting-&-FAQ.md", faq)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

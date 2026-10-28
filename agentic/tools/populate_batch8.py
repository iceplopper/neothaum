import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

biomes_flora = """# Biomes & Thaumic Flora

**Category:** World Generation | [Return to Wiki Home](Home)

---

## Overview

NeoThaum enriches Minecraft's natural world with enchanted biomes, towering mystical trees, and magical botanicals necessary for alchemy and research.

---

## Thaumic Biomes

### 1. 🌲 The Magical Forest
- **Appearance:** Vibrant turquoise grass, deep twilight blue sky, and glowing particles drifting through the air.
- **Flora:** Densely populated with **Greatwood** and **Silverwood** trees. Wild **Shimmerleaf** grows along riverbanks, and **Mana Beans** hang from tree foliage.
- **Creatures:** Home to wild **Pechs** (small mischievous goblin-like traders).

### 2. 🟣 The Tainted Land
- **Appearance:** Corrupted purple wasteland with withered trees and bubbling geysers.
- **Dangers:** Taint infection damages non-tainted creatures, slowly corrupting the player and transforming local mobs into mutated beasts.

---

## Sacred Trees

### Greatwood Trees
- Massive hardwoods with trunks 2x2 to 4x4 blocks wide and expansive foliage canopies.
- Drops **Greatwood Saplings** and produces dark, durable wood planks used to craft Greatwood Wands and robust laboratory furniture.

### Silverwood Trees
- Sacred white trees with shimmering pale blue leaves.
- **Pure Node Core**: The trunk of a mature Silverwood tree naturally contains a **Pure Aura Node**. This node steadily repels Taint and converts the surrounding terrain into Magical Forest biome!

---

## Thaumic Botanicals

| Plant | Native Biome | Yielded Aspects | Usage |
| :--- | :--- | :--- | :--- |
| **Cinderpearl** | Deserts | *Ignis*, *Lux* | Ground into Blaze Powder; used to craft alchemical fire. |
| **Shimmerleaf** | Magical Forest / Silverwood base | *Aqua*, *Herba*, *Sano* | Key ingredient in brewing healing remedies and crafting **Ethereal Blooms**. |
| **Vishroom** | Dark caves, Swamp, Taint | *Venenum*, *Terra* | Toxic glowing mushrooms; inflicts Hallucination if stepped on. |
| **Mana Pod** | Magical Forest (hanging on trees) | Variable Aspects | Farmable aspect pods; can be bred to yield specific compound aspects. |

---

## Related Topics
- [Taint Mechanics](Taint-Mechanics)
- [Aura Nodes](Aura-Nodes)
- [Structures](Structures)
- [Home](Home)
"""

taint_mechanics = """# Taint Mechanics & Corruption

**Category:** World Generation | [Return to Wiki Home](Home)

---

## Overview

**Taint** is an invasive mystical blight—a virulent magical pathogen that infects the terrain, transforms living animals into aggressive husks, and pollutes the atmospheric aura. If left unchecked, a localized taint spore can consume entire biomes.

---

## Propagation Dynamics

```mermaid
graph TD
    N["Tainted Aura Node / Flux Slime"] --> S["Fibrous Taint Spread"]
    S --> T["Tainted Soil & Crusted Stone"]
    T --> P["Taint Spores & Tendrils"]
    P --> M["Corrupted Tainted Mobs"]
```

1. **Fibrous Taint**: Creeps across the ground like a purple fungus, converting grass and dirt into **Tainted Soil**, and stone into **Crusted Taint**.
2. **Taint Spores**: Explosive fungal pods that burst when approached, showering victims with poison and spreading fresh taint.
3. **Tendrils**: Attacking tentacles that emerge from infected blocks to strike nearby players.

---

## Tainted Creatures

When ordinary passive animals dwell on tainted soil or are struck by taint attacks, they mutate:
- **Tainted Sheep**: Wool falls off, replaced by purple crust.
- **Tainted Pigs / Cows**: Aggressive monstrosities that hunt down uninfected wildlife.
- **Tainted Crawlers**: Vicious swarming centipedes that scuttle through infected caves.

---

## Curing and Repelling Taint

To protect your base from corruption:

### 1. The Ethereal Bloom
- A radiant white flower crafted in the [Crucible](Crucible-Alchemy) using Shimmerleaf, *Praecantatio*, *Sano*, and *Herba*.
- Radiates a 16-block purifying aura that permanently cures tainted soil back to clean grass and kills taint tendrils.

### 2. Planting Silverwoods
- Planting Silverwood saplings creates a **Pure Node**, naturally reclaiming tainted land into lush Magical Forest.

---

## Related Topics
- [Biomes & Thaumic Flora](Biomes-&-Flora)
- [Crucible Alchemy](Crucible-Alchemy)
- [Configuration & Settings](Configuration-&-Settings)
- [Home](Home)
"""

structures = """# World Structures & Dungeons

**Category:** World Generation | [Return to Wiki Home](Home)

---

## Overview

Ancient civilizations and lost orders of mages have left monuments and fortifications scattered across the world. Exploring these ruins yields rare research artifacts, aspect scrolls, and valuable treasure.

---

## Prominent Structures

### 1. Hilltop Megaliths & Barrow Mounds
- **Appearance**: Circular rings of weathered stone arches set atop grassy knolls.
- **The Crypt**: Digging beneath the center reveals a subterranean brick tomb guarded by **Furious Zombies** and a sinister dark node.
- **Loot**: Chests containing ancient gold coins, scribing tools, and aspect knowledge scrolls.

### 2. Wizard Spire Towers
- **Appearance**: Multi-story stone spires rising above forest canopies.
- **Floors**:
  - Ground floor entrance hall.
  - Intermediate library with bookshelves and research tables.
  - Spire observatory containing an **Arcane Worktable**, brewing apparatus, and treasure chests.

### 3. Village Banker Houses
- **Appearance**: Specialized stone-and-wood offices integrated naturally into NPC villages.
- **NPCs**: Inhabited by Thaumic Banker villagers who exchange elemental shards and gold ingots for arcane coins.

---

## Related Topics
- [Biomes & Thaumic Flora](Biomes-&-Flora)
- [Ore Generation](Ore-Generation)
- [Eldritch Structures](Eldritch-Worldgen)
- [Home](Home)
"""

ore_gen = """# Ores & Geological Deposits

**Category:** World Generation | [Return to Wiki Home](Home)

---

## Overview

Thaumaturgical manufacturing requires specialized elemental crystals and chemical reagents found deep in subterranean rock strata.

---

## Subterranean Minerals

### 1. 💎 Infused Stones (Elemental Crystals)
Infused Stones are shimmering crystal veins embedded in stone. When mined, they drop elemental **Crystal Shards**:

| Mineral Variant | Aspect Contained | Common Depth |
| :--- | :---: | :--- |
| **Air Infused Stone** | 💨 Aer | High mountain peaks, surface cliffs ($Y > 64$) |
| **Earth Infused Stone** | 🪨 Terra | Deep subterranean caves ($Y < 32$) |
| **Fire Infused Stone** | 🔥 Ignis | Near subterranean lava pools and the Nether |
| **Water Infused Stone** | 💧 Aqua | Ocean floors, riverbeds, and wet caverns |
| **Order Infused Stone** | ⚖️ Ordo | Deep underground, crystalline geodes |
| **Entropy Infused Stone** | 💥 Perditio | Deep cave floors, gravel and sand pockets |

*Note: Shards are the fundamental ingredient in Salis Mundus and crystal cluster stabilizers.*

---

### 2. 🔴 Cinnabar Ore
- **Appearance**: Deep red crystalline mineral veins found in the lower third of the world.
- **Smelting**: Smelts in a standard furnace or [Infernal Furnace](Infernal-Furnace) into **Quicksilver** (Mercury).
- **Uses**: Used in crafting Thaumometers, Alchemical Centrifuges, and magical mirrors.

---

### 3. 🟡 Amber Ore
- **Appearance**: Translucent golden gemstone deposits encased in stone.
- **Harvest**: Yields raw **Amber Gems**.
- **Uses**: Used in crafting optical lenses, Goggles of Revealing, Arcane Stone, and runic baubles.

---

## Related Topics
- [Getting Started](Getting-Started)
- [Aspects Overview](Aspects-Overview)
- [Infernal Furnace](Infernal-Furnace)
- [Home](Home)
"""

for fname, text in [
    ("Biomes-&-Flora.md", biomes_flora),
    ("Taint-Mechanics.md", taint_mechanics),
    ("Structures.md", structures),
    ("Ore-Generation.md", ore_gen)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

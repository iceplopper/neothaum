import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

arcane_worktable = """# The Arcane Worktable

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

The **Arcane Worktable** is an enhanced crafting table capable of channeling primal vis directly from a casting wand into physical materials. Standard vanilla crafting benches lack the mystical channels required to bind enchanted tools, wand assemblies, and alchemical apparatus.

---

## Construction

1. Place a standard vanilla **Crafting Table** anywhere in the world.
2. Hold any **Wand** in your hand and right-click the table.
3. The crafting table morphs into an **Arcane Worktable**, marked with brass corner inlays and six colored primal aspect orbs.

---

## Interface & Operation

The Arcane Worktable interface features:
- **3x3 Crafting Grid**: Where physical ingredients are arranged.
- **Wand Slot (Top Right)**: Where your casting wand, scepter, or staff is placed.
- **Primal Vis Display**: Six crystalline indicators showing the current vis stored within the docked wand.

### Crafting with Vis
Many recipes require both physical items and a specific amount of primal vis (Aer, Terra, Ignis, Aqua, Ordo, Perditio).
- When a valid recipe is placed in the grid, the required vis amounts illuminate around the output slot.
- If your docked wand holds sufficient vis, the crafted item appears in the output slot.
- Removing the output deducts the vis directly from your wand.

> [!TIP]
> **Use a Casting Scepter for Workshop Crafting!** Scepters hold 1.5x more vis than wands and provide an extra 5% vis discount, making them the ideal dedicated tool to leave resting inside your Arcane Worktable.

---

## Related Topics
- [Getting Started](Getting-Started)
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Vis Storage & Discount Robes](Vis-Storage-&-Discount-Armor)
- [Home](Home)
"""

infusion_altar = """# The Infusion Altar

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

The **Infusion Altar** is the crowning achievement of Thaumaturgy. By constructing a sacred multiblock temple of Arcane Stone and channeling the cosmic energies of the **Runic Matrix**, you can infuse mundane items with pure liquid Essentia and mystical catalysts, creating relics of godlike power.

---

## Multiblock Temple Construction

### Required Blocks:
- 1x **Runic Matrix**
- 8x **Arcane Stone Blocks**
- 4x **Arcane Stone Bricks**
- 4+ **Arcane Pedestals**

```
Layer 3:          [ Runic Matrix ]
                     (Suspended in air)

Layer 2 (Pillars): [Pillar] [ Air ] [Pillar]
                   [ Air  ] [ Air ] [ Air  ]
                   [Pillar] [ Air ] [Pillar]

Layer 1 (Floor):   [Brick] [Stone] [Brick]
                   [Stone] [Pedestal] [Stone]
                   [Brick] [Stone] [Brick]
```

### Assembly Steps:
1. Construct the 3x3 platform on the floor: 1 central **Arcane Pedestal**, 4 **Arcane Stone Bricks** on the corners, and 4 **Arcane Stone Blocks** on the edges.
2. Build 4 pillars of **Arcane Stone Blocks** (2 blocks tall each) on the corners.
3. Suspend the **Runic Matrix** in the air directly above the central pedestal.
4. Right-click the Runic Matrix with a **Wand** holding at least 25 vis of each primal.
5. The matrix will ignite, rotating slowly in mid-air with mystical runes.

---

## Symmetry & Instability

Every infusion recipe has an **Instability Rating**: *Negligible*, *Minor*, *Moderate*, *High*, or *Dangerous*.

As the altar draws essentia and items, instability builds up. If instability is not controlled, catastrophic events occur:
- **Lightning Strikes**: Blasts the player with high magical damage.
- **Pedestal Ejection**: Knocks catalyst items off pedestals onto the floor.
- **Item Destruction**: Permanently destroys pedestal ingredients.
- **Flux Gas & Slime**: Pollutes the room with purple flux.

### The Golden Rule of Altar Symmetry
The altar calculates instability by checking pairs of blocks across the center:
$$\text{Offset Pair: } (X_c + \Delta x, Z_c + \Delta z) \longleftrightarrow (X_c - \Delta x, Z_c - \Delta z)$$

1. **Pedestals must be in identical opposite pairs**: If you place a pedestal 3 blocks North of the center, you **must** place another pedestal 3 blocks South!
2. **Pedestal items must balance**: Opposing pedestals should ideally hold identical items or items with matching aspect weights.
3. **Stabilizers**: Place symmetrical pairs of **Mob Skulls**, **Skeleton Heads**, **Crystal Clusters**, and **Infused Candles** on the floor around the altar. Each symmetrical pair cancels out instability!

---

## Video Tutorial: Infusion Altar Mastery

[![Thaumcraft 4 Infusion Altar Guide](https://img.youtube.com/vi/aZ3eH7T4b98/0.jpg)](https://www.youtube.com/watch?v=aZ3eH7T4b98)
*(Detailed walkthrough of altar construction and stabilization)*

---

## Related Topics
- [Infusion Enchanting](Infusion-Enchanting)
- [Essentia Storage](Essentia-Storage)
- [Baubles & Runic Armor](Baubles-&-Runed-Armor)
- [Home](Home)
"""

infusion_enchant = """# Infusion Enchanting

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

Unlike the unpredictable, random results of the vanilla Enchanting Table, **Infusion Enchanting** offers 100% deterministic, custom enchantments using the [Infusion Altar](Infusion-Altar).

---

## Custom Thaumic Enchantments

| Enchantment | Applicable Gear | Effect |
| :--- | :--- | :--- |
| **Repair (I - II)** | Tools, Weapons, Armor | Automatically regenerates item durability over time using ambient vis. |
| **Haste (I - III)** | Boots, Tools | Increases player movement speed on armor, or increases mining speed on picks and axes. |
| **Frugal (I - III)** | Wand Focuses | Decreases vis cost per spell activation. |
| **Potency (I - III)** | Wand Focuses | Amplifies spell damage and mining efficiency. |

---

## The Enchanting Process

1. Place the tool or weapon on the central altar pedestal.
2. Arrange the required catalyst items (shards, gold nuggets, spider eyes, etc.) on surrounding symmetrical pedestals.
3. Ensure the required liquid Essentia jars are located within 12 blocks of the matrix.
4. Right-click the Runic Matrix with your wand to initiate the ritual.

---

## Related Topics
- [The Infusion Altar](Infusion-Altar)
- [Baubles & Runic Armor](Baubles-&-Runed-Armor)
- [Home](Home)
"""

arcane_bore = """# The Arcane Bore

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

The **Arcane Bore** is an automated mechanical drilling cannon mounted on an iron tripod. Powered by an internal **Excavation Focus**, it carves deep mining shafts and quarries across the terrain, depositing all mined resources into an adjacent chest.

---

## Construction & Mounting

1. Craft the **Arcane Bore** base and the **Arcane Bore Tripod**.
2. Place the tripod on firm ground.
3. Place the Arcane Bore cannon atop the tripod.
4. Right-click the bore to open its internal GUI:
   - **Wand Focus Slot**: Insert a **Wand Focus: Excavation**.
   - **Pickaxe Slot**: Insert any standard Pickaxe (Iron, Diamond, Thaumium). The bore uses the pickaxe to break blocks, inheriting the pickaxe's **Silk Touch** or **Fortune** enchantments!
   - **Direction Lever**: Aim the cannon horizontally or tilt it vertically into the ground.
5. Apply a **Redstone signal** to fire the drilling beam.

---

## Related Topics
- [Wand Focuses](Wand-Focuses)
- [Hungry Chest](Hungry-Chest)
- [Home](Home)
"""

infernal_furnace = """# The Infernal Furnace

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

The **Infernal Furnace** is a $3 \\times 3 \\times 3$ hollow stone multiblock fueled by trapped elemental fire. It smelts ores and cookable items at hyper speed without consuming coal or fuel, and grants a significant chance of producing **rare bonus metal nuggets** and liquid experience slurry.

---

## Multiblock Construction

### Required Blocks:
- 12x **Netherrack**
- 12x **Obsidian**
- 1x **Iron Bars**

```
Layer 3 (Top):    [Obsidian]   [Obsidian]   [Obsidian]
                  [Obsidian]   [  Air   ]   [Obsidian]
                  [Obsidian]   [Obsidian]   [Obsidian]

Layer 2 (Middle): [Obsidian]   [IronBars]   [Obsidian]  <-- Front has Iron Bars
                  [Netherrack] [  Air   ]   [Netherrack]
                  [Obsidian]   [Netherrack] [Obsidian]

Layer 1 (Bottom): [Obsidian]   [Netherrack] [Obsidian]
                  [Netherrack] [Netherrack] [Netherrack]
                  [Obsidian]   [Netherrack] [Obsidian]
```

### Activation:
Right-click the central front **Iron Bars** with any **Wand**. The structure transforms into an open furnace glowing with molten lava.

---

## Operating Mechanics

- **Input**: Drop ores or raw items into the top chimney opening (using hoppers, water streams, or dropping them by hand).
- **Output**: Smelted ingots, bonus nuggets, and XP orbs are ejected out through the front iron gate.
- **Arcane Bellows**: Attach up to **3 Arcane Bellows** to the side and rear vents. Each bellows increases smelting speed by 25% and dramatically raises the drop rate of bonus rare nuggets!

---

## Related Topics
- [Configuration & Settings](Configuration-&-Settings)
- [Crucible Alchemy](Crucible-Alchemy)
- [Home](Home)
"""

baubles_runed = """# Baubles & Runic Shielding

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

**Baubles** are wearable enchanted jewelry, belts, rings, and talismans that grant continuous passive enchantments. Through [Infusion Enchanting](Infusion-Enchanting), you can also coat your armor and baubles with **Runic Shielding**, generating an invulnerable kinetic forcefield.

---

## Prominent Thaumic Baubles

### 1. Boots of the Traveller
- **Crafting:** Infusion Altar (Raw Leather Boots + Feather + Fish + Aer/Iter essentia).
- **Abilities:**
  - Increases walking and sprinting velocity.
  - Grants auto step-up (steps up full 1-block steps without jumping).
  - Doubles jumping height and drastically reduces fall damage.

### 2. Ring of Runic Shielding
- Grants rechargeable kinetic shield points represented by purple/golden pips above your health bar.
- Kinetic shielding absorbs all incoming damage (including environmental, fall, and void damage) before touching player health.
- Shields recharge automatically after a few seconds without taking damage.

### 3. Paving Stones of Travel & Warding
- **Paving Stone of Travel**: Increases player walking speed by 50% when walked upon.
- **Paving Stone of Warding**: Emits an invisible barrier that prevents hostile mobs from stepping onto the road.

---

## Related Topics
- [The Infusion Altar](Infusion-Altar)
- [Vis Storage & Discount Robes](Vis-Storage-&-Discount-Armor)
- [Home](Home)
"""

mirror_magic = """# Mirror Magic

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

**Mirrors** are trans-dimensional conduits that teleport physical items and liquid essentia across infinite distances, even across different dimensions.

---

## Types of Magic Mirrors

### 1. Magic Mirrors (Item Transportation)
- Crafted in the Infusion Altar.
- Created in linked pairs: hold one mirror and right-click another mirror to pair their dimensional frequencies.
- Any item dropped into one mirror immediately pops out of the linked mirror on the other side.
- **Hand Mirror**: A portable handheld mirror linked to a chest in your home base. Allows you to send loot back to storage while mining deep in caves!

### 2. Essentia Mirrors (Wireless Essentia)
- Paired identical to Magic Mirrors.
- Place one Essentia Mirror directly above your [Warded Jar](Essentia-Storage) warehouse, and place the linked mirror adjacent to your [Infusion Altar](Infusion-Altar).
- The altar will wirelessly pull essentia directly from your jars through the mirror, eliminating the need to carry jars back and forth!

---

## Related Topics
- [The Infusion Altar](Infusion-Altar)
- [Essentia Storage](Essentia-Storage)
- [Home](Home)
"""

hungry_chest = """# The Hungry Chest

**Category:** Artifice & Ancient Machinery | [Return to Wiki Home](Home)

---

## Overview

The **Hungry Chest** is a living, carnivorous storage container. When closed, it appears as an ordinary wooden chest, but when dropped items come into physical contact with it, the lid snaps open like a pair of jaws, instantly swallowing the item into its internal inventory.

---

## Practical Applications

- **Quarry Depots**: Place beneath an [Arcane Bore](Arcane-Bore) to capture falling mined blocks without relying on complex hopper chains.
- **Mob Farms**: Place at the bottom of drop shafts to capture mob drops instantly.
- **Furnace Feeding**: Capture items spat out by the [Infernal Furnace](Infernal-Furnace).

> [!WARNING]
> Do not jump onto a Hungry Chest yourself! Stepping on an open Hungry Chest will cause it to snap shut on your legs, dealing continuous crushing damage.

---

## Related Topics
- [The Arcane Bore](Arcane-Bore)
- [The Infernal Furnace](Infernal-Furnace)
- [Home](Home)
"""

for fname, text in [
    ("Arcane-Worktable.md", arcane_worktable),
    ("Infusion-Altar.md", infusion_altar),
    ("Infusion-Enchanting.md", infusion_enchant),
    ("Arcane-Bore.md", arcane_bore),
    ("Infernal-Furnace.md", infernal_furnace),
    ("Baubles-&-Runed-Armor.md", baubles_runed),
    ("Mirror-Magic.md", mirror_magic),
    ("Hungry-Chest.md", hungry_chest)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

wands_staffs = """# Wands, Scepters & Staffs

**Category:** Thaumaturgy | [Return to Wiki Home](Home)

---

## Overview

The casting wand is the primary conduit through which a Thaumaturge manifests will into reality. Whether crafting arcane apparatus, transmuting matter, or firing devastating magical spells, your choice of wand rod and caps determines your vis storage capacity and spellcasting efficiency.

---

## Anatomical Components of a Wand

Every wand is crafted in an [Arcane Worktable](Arcane-Worktable) by combining two **Wand Caps** with a central **Wand Rod**.

```
[ Top Cap  ] [    Empty   ] [    Empty   ]
[   Empty  ] [  Wand Rod  ] [    Empty   ]
[   Empty  ] [    Empty   ] [ Bottom Cap ]
```

---

## Wand Rods Comparison

| Rod Type | Base Vis Capacity | Special Passive Traits |
| :--- | :---: | :--- |
| **Wood** | 25 Vis | Novice starter rod. No special properties. |
| **Greatwood** | 50 Vis | Durable hardwood capable of holding moderate charges. |
| **Silverwood** | 100 Vis | Pure magical wood. Highly receptive to all aspects. |
| **Obsidian** | 150 Vis | High earth affinity. Very resilient. |
| **Blaze** | 150 Vis | Passively regenerates **Ignis** vis over time up to 10%. |
| **Ice** | 150 Vis | Passively regenerates **Aqua** vis over time up to 10%. |
| **Quartz** | 150 Vis | Passively regenerates **Ordo** vis over time up to 10%. |
| **Bone** | 150 Vis | Passively regenerates **Perditio** vis over time up to 10%. |
| **Reed** | 150 Vis | Passively regenerates **Aer** vis over time up to 10%. |
| **Primal Staff Core** | 250 Vis | Forged in the Infusion Altar. Two-handed weapon with 2.5x storage. |

---

## Wand Caps & Discounts

Wand caps act as conduits, reducing the friction of vis channeling. A higher discount means you consume fewer vis points for every action.

| Cap Material | Vis Cost Modifier | Net Discount |
| :--- | :---: | :---: |
| **Iron** | 110% | -10% penalty |
| **Copper** | 105% | -5% penalty |
| **Gold** | 100% | 0% (baseline) |
| **Silver** | 95% | +5% discount |
| **Thaumium** | 90% | +10% discount |
| **Void Metal** | 80% | +20% discount (inflicts mild warp) |

---

## Specialized Casting Implements

### 1. Casting Scepters
Scepters are heavy-headed crafting wands assembled with **three caps** in an L-shaped recipe.
- **Traits:** Holds **150% of the base rod's vis capacity** and gives an additional **5% built-in discount**.
- **Limitation:** Cannot equip Wand Focuses. Scepters are dedicated purely to workshop crafting in Arcane Worktables.

### 2. Battle Staves
By infusing two wand rods with primal nodes, you create a **Staff Core**.
- **Traits:** Holds **250 Vis**, deals substantial melee knockback and physical damage equivalent to a diamond sword.
- **Limitation:** Cannot be used inside an Arcane Worktable; strictly reserved for focus spellcasting and combat.

---

## Related Topics
- [Wand Focuses](Wand-Focuses)
- [Focal Manipulator](Focal-Manipulator)
- [Vis Storage & Discount Robes](Vis-Storage-&-Discount-Armor)
- [Home](Home)
"""

wand_foci = """# Wand Focuses

**Category:** Thaumaturgy | [Return to Wiki Home](Home)

---

## Overview

**Wand Focuses** are cut magical crystals that channel the raw primal vis stored in your wand into specific spell effects. By swapping focuses, a single wand can transition instantaneously from a flamethrower to an excavation drill or an impenetrable forcefield.

---

## Managing Your Focuses

### The Radial Selection Menu
- With a wand in your main hand, press the **F key** (or your configured focus swap key) to open the interactive circular focus wheel.
- Hover your mouse over the desired focus to equip it instantly.

### The Focus Pouch
- Craft a leather **Focus Pouch** and equip it in your belt or carry it in your inventory.
- It provides a dedicated internal inventory holding up to 18 wand focuses, keeping your hotbar uncluttered.

---

## Complete Catalog of Wand Focuses

### 1. 🔥 Focus: Fire
- **Cost:** Ignis
- **Effect:** Unleashes a continuous cone of roaring elemental flames that incinerates enemies and ignites blocks. Highly effective for crowd control against swarming mobs.

### 2. ❄️ Focus: Frost
- **Cost:** Aqua + Gelum
- **Effect:** Launches a heavy crystalline orb of frost that shatters on impact, dealing blunt ice damage and applying severe slowness to targets.

### 3. ⚡ Focus: Shock
- **Cost:** Aer
- **Effect:** Fires instantaneous arcs of chain lightning. The bolt automatically arcs between nearby hostile targets, bypassing armor.

### 4. ⛏️ Focus: Excavation
- **Cost:** Terra + Perditio
- **Effect:** Projects a sonic digging beam that instantly carves out blocks at long range. When upgraded with the *Silky* or *Fortune* enchantment in the [Focal Manipulator](Focal-Manipulator), it rivals top-tier pickaxes.

### 5. 🕳️ Focus: Portable Hole
- **Cost:** Vacuos + Alienis
- **Effect:** Right-clicking any solid wall opens a temporary $3 \\times 3 \\times 33$ trans-dimensional tunnel. The passage remains open for several seconds before collapsing harmlessly, returning all original blocks without damaging terrain.

### 6. 🔄 Focus: Equal Trade
- **Cost:** Permutatio + Terra
- **Effect:** Sneak-right-click to select a block from the world (or your inventory). Right-clicking any surface swaps the clicked blocks with your chosen block across a wide radius. Essential for builders and architectural terraforming.

### 7. 🦇 Focus: Hellbat
- **Cost:** Ignis + Bestia
- **Effect:** Summons a homing Nether Hellbat that flies directly at your targeted enemy, detonating in an explosion upon impact.

### 8. 🛡️ Focus: Warding
- **Cost:** Terra + Ordo
- **Effect:** Renders targeted blocks completely indestructible and immune to explosions, wither attacks, and player mining. Only the caster with the warding focus can unward the blocks.

### 9. 🌌 Focus: Primal
- **Cost:** All 6 Primals
- **Effect:** Forbidden eldritch focus. Discharges chaotic balls of raw elemental matter that produce unpredictable, devastating explosions, firestorms, or lightning strikes. Inflicts temporary warp on the caster.

---

## Related Topics
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Focal Manipulator](Focal-Manipulator)
- [Home](Home)
"""

focal_manipulator = """# The Focal Manipulator

**Category:** Thaumaturgy | [Return to Wiki Home](Home)

---

## Overview

The **Focal Manipulator** is an advanced enchanting station that allows you to customize and upgrade your [Wand Focuses](Wand-Focuses). Rather than vanilla anvil enchanting, the Focal Manipulator features branching specialization trees powered by player experience and primal vis.

---

## Construction & Setup

### Crafting Recipe (Arcane Worktable)
- 4x Arcane Stone Bricks
- 1x Quartz Block
- 1x Thaumium Ingot
- 1x Goggles of Revealing

Place the block in your workshop. To power high-tier upgrades, place it near an **Energized Vis Relay** or keep a charged wand in your inventory.

---

## Upgrade Categories

When a focus is placed into the manipulator, you can select up to **5 tiers of upgrades**:

```mermaid
graph TD
    B["Base Focus (Tier 0)"] --> T1["Tier 1 Upgrade"]
    T1 --> T2["Tier 2 Upgrade"]
    T2 --> T3["Tier 3 Upgrade"]
    T3 --> T4["Tier 4 Branch (Specialization A)"]
    T3 --> T5["Tier 4 Branch (Specialization B)"]
    T4 --> F1["Tier 5 Master Upgrade"]
    T5 --> F2["Tier 5 Master Upgrade"]
```

### Universal Upgrades
- **Potency (I - V)**: Increases the primary damage or mining speed of the focus.
- **Frugal (I - V)**: Reduces the vis cost per activation by up to 35%.
- **Enlarged Area**: Expands the area-of-effect radius for Excavation and Fire.
- **Range**: Extends the maximum lock-on distance for projectile focuses.

### Specialization Branches
- **Fire Focus**: Can branch into *Alchemical Fire* (leaves lingering napalm puddles) or *Fireball* (launches explosive fireballs instead of a flame stream).
- **Excavation Focus**: Can branch into *Silk Touch* or *Fortune* (up to Fortune III).
- **Shock Focus**: Can branch into *Chain Lightning* (targets up to 5 additional mobs) or *Earth Shock* (summons ground-shattering lightning traps).

---

## Related Topics
- [Wand Focuses](Wand-Focuses)
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Home](Home)
"""

vis_robes = """# Vis Storage & Discount Robes

**Category:** Thaumaturgy | [Return to Wiki Home](Home)

---

## Overview

Casting high-level spells and operating complex Arcane Worktable recipes consumes vast amounts of primal vis. Wearing **Vis Discount Apparel** reduces the vis consumed by your wands, allowing you to craft recipes that would otherwise exceed your wand's storage capacity.

---

## Thaumaturge's Robe Set

Crafted from **Enchanted Fabric** (woven from string and wool immersed in Praecantatio essentia):

| Armor Piece | Defense | Vis Discount | Special Properties |
| :--- | :---: | :---: | :--- |
| **Goggles of Revealing** | +2 Armor | +5% | Highlights Aura Nodes, shows Jar aspect counts and Crucible contents. |
| **Thaumaturge's Robe Chest** | +5 Armor | +3% | Can be dyed with standard dyes. |
| **Thaumaturge's Leggings** | +4 Armor | +3% | Reduces vis consumption. |
| **Boots of the Traveller** | +2 Armor | +2% | High step-up (1 block), enhanced sprint speed, and high jump boost. |
| **Full Set Bonus** | — | **+13% Total** | Complete novice casting set. |

---

## Void Thaumaturge Set (Endgame)

Forged in the [Infusion Altar](Infusion-Altar) using Void Metal and Eldritch fabrics:
- **Total Vis Discount:** Up to **+20%**.
- **Self-Repair:** Automatically mends durability by drawing energy from the void.
- **Sanity Cost:** Slowly trickles temporary warp into the wearer's subconscious mind.

---

## Vis Storage Baubles

### 1. Amulet of Vis
- Worn around the neck in the Amulet bauble slot.
- Can be recharged at a **Recharge Pedestal**.
- Automatically siphons vis into whatever wand you hold in your hand whenever its charge drops below 100%.

### 2. Ring of Runic Shielding
- Forged with kinetic shields. Grants a protective rechargeable forcefield over your health bar that absorbs damage before touching player hearts.

---

## Related Topics
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Baubles & Runic Armor](Baubles-&-Runed-Armor)
- [Warp Mechanics & Sanity](Warp-Mechanics)
- [Home](Home)
"""

for fname, text in [
    ("Wands-and-Staffs.md", wands_staffs),
    ("Wand-Focuses.md", wand_foci),
    ("Focal-Manipulator.md", focal_manipulator),
    ("Vis-Storage-&-Discount-Armor.md", vis_robes)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

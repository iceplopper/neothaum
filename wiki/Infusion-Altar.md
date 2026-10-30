# The Infusion Altar

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
$$	ext{Offset Pair: } (X_c + \Delta x, Z_c + \Delta z) \longleftrightarrow (X_c - \Delta x, Z_c - \Delta z)$$

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

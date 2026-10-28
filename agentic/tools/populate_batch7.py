import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

warp_mechanics = """# Warp Mechanics & Sanity

**Category:** Eldritch Mysteries | [Return to Wiki Home](Home)

---

## Overview

**Warp** is the mystical psychosis and psychological corruption that infects a Thaumaturge who delves into forbidden lore, meddles with the dark arts, or handles raw eldritch matter. As your Warp accumulates, the fabric of your mind frays, producing horrific hallucinations, bodily afflictions, and otherworldly incursions.

---

## The Three Tiers of Warp

Warp exists in three distinct layers in your character's soul:

```mermaid
graph TD
    T["1. Temporary Warp (Transient)"] -->|Decays passively over time| C["Natural Recovery"]
    S["2. Sticky Warp (Persistent)"] -->|Removed via Soap & Bath Salts| B["Sanitizing Rituals"]
    P["3. Permanent Warp (Soul-Bound)"] -->|Incurable tattoo on the soul| F["Forever Present"]
```

### 1. Temporary Warp
- **Gained from:** Casting dark spells (e.g. *Focus: Primal*), crafting dark items, or being struck by Eldritch Guardians.
- **Duration:** Gradually dissipates on its own over time.

### 2. Sticky Warp
- **Gained from:** Unlocking purple-bordered forbidden research entries in the Thaumonomicon.
- **Duration:** Does not fade passively; must be cleansed using alchemical purifiers.

### 3. Permanent Warp
- **Gained from:** Discovering epochal forbidden revelations (such as the *Eldritch Epiphany*).
- **Duration:** Cannot be cleansed; permanently stays with the player.

---

## Warp Symptoms & Manifestations

As your warp level rises, random psychotomimetic warp events will trigger unexpectedly:

| Symptom | Severity | Description |
| :--- | :---: | :--- |
| **Auditory Whispers** | Low | Eerie whispers, heartbeat thumping, and cave rattles playing in your ears. |
| **Phantom Spiders** | Low | Illusory ghostly spiders swarm your screen; they disappear when struck. |
| **Unnatural Hunger** | Medium | Extreme ravenous hunger; eating food inflicts Rotting Flesh sickness. |
| **Sun Sickness** | Medium | Standing under direct sunlight causes burning blindness and nausea. |
| **Eldritch Vision** | Medium | Shifting rainbow fog and cosmic tunnel vision. |
| **Mind Spiders** | High | Actual physical poisonous spiders burst from your body. |
| **Guardian Incursion** | High | Tall shadowy **Eldritch Guardians** tear through the veil and attack! |

---

## Cleansing Warp & Protecting Your Sanity

### 1. Sanitizing Soap
- Crafted from tallow, water, and pure essentia.
- Right-clicking and using the soap scrubs away a portion of your **Sticky Warp**.

### 2. Purifying Bath Salts
- Tossed into a pool of water to create a soothing spa.
- Soaking in the bath grants the **Warp Ward** buff, completely suppressing all warp manifestations for several real-time hours!

---

## Related Topics
- [Eldritch Monuments](Eldritch-Worldgen)
- [The Crimson Cult](Crimson-Cult)
- [Void Metal Equipment](Void-Metal-Equipment)
- [Home](Home)
"""

eldritch_worldgen = """# Eldritch Structures & Monuments

**Category:** Eldritch Mysteries | [Return to Wiki Home](Home)

---

## Overview

Ancient, cyclopean monuments built by unknown cosmic hands can be found throughout the overworld. These obsidian structures act as dimensional anchors to the [Outer Lands](The-Outer-Lands).

---

## Eldritch Obelisks

**Eldritch Obelisks** are massive monolithic spires constructed from indestructible obsidian and ancient stone, wrapped in bands of glowing glyphs:

- **The Capstone**: A floating runic monolith hovers above the spire, held in place by magnetic vis fields.
- **The Sinister Node**: A dark, shadowy Aura Node sits trapped within the heart of every obelisk, casting an aura of perpetual gloom and spawning Furious Zombies.
- **The Outer Pedestals**: 4 stone pillars surround the base of the obelisk.

---

## Opening the Eldritch Portal

To pierce the dimensional veil and enter the Outer Lands:

1. Locate an **Eldritch Altar** (often situated at the center of an Eldritch Ring).
2. Slay Crimson Cultists to acquire the **Crimson Rites** book and research the *Eldritch Eye*.
3. Craft **4 Eldritch Eyes** (Ender Eyes infused with Alienis, Praecantatio, and Vitium).
4. Insert an Eldritch Eye into each of the four altar sockets surrounding the center.
5. Strike the center altar with your **Wand** (requires 100 vis of each primal).
6. A swirling black-and-purple **Eldritch Portal** will open in the sky above the altar!

> [!CAUTION]
> Step through the portal only when fully equipped with diamond/thaumium armor, ample food, and weapons. The Outer Lands is an inescapable maze dimension until you defeat the boss at its core!

---

## Related Topics
- [Warp Mechanics & Sanity](Warp-Mechanics)
- [The Crimson Cult](Crimson-Cult)
- [The Outer Lands](The-Outer-Lands)
- [Home](Home)
"""

crimson_cult = """# The Crimson Cult

**Category:** Eldritch Mysteries | [Return to Wiki Home](Home)

---

## Overview

The **Crimson Cult** is a fanatical order of trans-dimensional zealots clad in blood-red robes. They construct ritual circles around Eldritch Obelisks in an attempt to summon the dark entities of the Outer Lands into our world.

---

## Cultist Ranks

### 1. Crimson Knights
- **Role:** Heavy melee vanguards.
- **Equipment:** Crimson Plate Armor, broadswords, and heavy shields.
- **Combat Style:** Charges players with high forward velocity and powerful knockback.

### 2. Crimson Clerics
- **Role:** Ritual spellcasters.
- **Equipment:** Crimson Robes.
- **Combat Style:** Keeps distance while casting homing crimson eldritch bolts and summoning portals of lightning.

### 3. The Crimson Praetor (Boss)
- **Role:** Commander of the cult forces.
- **Combat Style:** Wields an enchanted Void Broadsword; casts life-stealing spells and possesses immense health pools.

---

## The Crimson Rites Grimoire

Slaying Crimson Clerics has a rare chance to drop the **Crimson Rites** book.
- Right-clicking and reading this tome inflicts significant Warp, but immediately unlocks the final, secret **Eldritch Research Tab** in your Thaumonomicon!

---

## Related Topics
- [Warp Mechanics & Sanity](Warp-Mechanics)
- [Eldritch Structures](Eldritch-Worldgen)
- [Eldritch Horrors & Bosses](Eldritch-Mobs)
- [Home](Home)
"""

eldritch_mobs = """# Eldritch Horrors & Bosses

**Category:** Eldritch Mysteries | [Return to Wiki Home](Home)

---

## Overview

Entities native to the Outer Lands are alien to our reality. They do not conform to standard biological laws and often possess armor-bypassing void attacks.

---

## Eldritch Bestiary

### 1. 👁️ Eldritch Guardians
- **Appearance:** Towering, cloaked wraiths with elongated limbs and glowing purple eyes.
- **Attacks:** Projects explosive eldritch orbs that bypass physical armor and inflict direct magical damage accompanied by temporary warp.
- **Weakness:** Ranged projectile combat and runic shielding.

### 2. 🦀 Eldritch Crabs
- **Appearance:** Spiny crystalline face-huggers that nest inside ancient stone blocks.
- **Behavior:** When disturbed, they leap onto the player's head, blinding the screen and dealing rapid damage.

### 3. 🗿 Eldritch Golems
- **Appearance:** Colossal animated stone titans constructed by the ancient architects.
- **Combat:** Acts as the labyrinth vault guardian. Immune to projectiles; must be brought down in close-quarters melee combat.

---

## Related Topics
- [The Crimson Cult](Crimson-Cult)
- [The Outer Lands](The-Outer-Lands)
- [Void Metal Equipment](Void-Metal-Equipment)
- [Home](Home)
"""

void_metal = """# Void Metal & Eldritch Relics

**Category:** Eldritch Mysteries | [Return to Wiki Home](Home)

---

## Overview

**Void Metal** is a living, reality-warping alloy forged by dissolving Void Seeds into molten metal in the [Crucible](Crucible-Alchemy). It produces the most potent tools, weapons, and armor in the mod, but demands a toll on the user's sanity.

---

## Unique Traits of Void Metal

- **Living Metal (Endless Self-Repair)**: Void metal tools and armor continuously regenerate their durability points without needing an anvil or mending.
- **Inherent Warp**: Carrying or wearing Void Metal equipment slowly feeds temporary warp into your subconscious mind.
- **Exceptional Enchantability**: Has the highest natural enchantment affinity of any material in the game.

---

## Void Relics

### 1. Void Thaumaturge Robes
The pinnacle casting attire. Grants a massive **20% Vis Discount** and high elemental defense.

### 2. Sanity Checker
A small brass instrument with a pendulum. Right-clicking displays your exact current Warp levels (Temporary, Sticky, and Permanent).

### 3. The Primordial Pearl
A shimmering, iridescent sphere obtained only by defeating the champion of the [Outer Lands](The-Outer-Lands). Used to craft the ultimate endgame devices: the *Primal Staff* and *Creative Node*.

---

## Related Topics
- [Warp Mechanics & Sanity](Warp-Mechanics)
- [The Outer Lands](The-Outer-Lands)
- [Vis Storage & Discount Robes](Vis-Storage-&-Discount-Armor)
- [Home](Home)
"""

outer_lands = """# The Outer Lands

**Category:** Eldritch Mysteries | [Return to Wiki Home](Home)

---

## Overview

The **Outer Lands** is a separate, pocket dimension constructed out of indestructible ancient stone floating in a starless cosmic void. It is designed as an infinite subterranean labyrinth filled with trapped keystones, eldritch guardians, and forgotten vaults.

---

## Navigating the Maze

1. **Indestructible Terrain**: The walls, ceilings, and floors of the Outer Lands cannot be broken by ordinary tools, TNT, or even Excavation Focuses.
2. **Ancient Lock Doors**: Passage between maze sectors is blocked by glowing runic doorways. You must find the **Runic Key** hidden in side chambers to open each doorway.
3. **Crusted Openings**: Spawns swarms of Eldritch Crabs and Guardians from biological wall nests.

---

## The Central Vault & Boss Fight

At the heart of the labyrinth lies the central Boss Arena:
- Confront one of the four legendary Eldritch Bosses (The Eldritch Construct, The Crimson Praetor, or The Champion Guardian).
- Defeating the vault boss unlocks the final chest containing the **Primordial Pearl**, the ultimate artifact in Thaumaturgy.

---

## Related Topics
- [Eldritch Structures](Eldritch-Worldgen)
- [Eldritch Horrors & Bosses](Eldritch-Mobs)
- [Void Metal Equipment](Void-Metal-Equipment)
- [Home](Home)
"""

for fname, text in [
    ("Warp-Mechanics.md", warp_mechanics),
    ("Eldritch-Worldgen.md", eldritch_worldgen),
    ("Crimson-Cult.md", crimson_cult),
    ("Eldritch-Mobs.md", eldritch_mobs),
    ("Void-Metal-Equipment.md", void_metal),
    ("The-Outer-Lands.md", outer_lands)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

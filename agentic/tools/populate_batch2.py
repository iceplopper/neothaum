import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

aspects_overview = """# Aspects of Magic Overview

**Category:** Aspects of Magic | [Return to Wiki Home](Home)

---

## Overview

In the world of **NeoThaum**, every block, creature, and item is composed of invisible spiritual essences known as **Aspects**. Aspects represent fundamental concepts, physical traits, and mystical affinities.

There are **6 Primal Aspects** and **48 Compound Aspects**, forming a vast hierarchical web of knowledge.

```mermaid
graph TD
    P["6 Primal Aspects"] --> C1["Tier 1 Compounds"]
    C1 --> C2["Tier 2 Compounds"]
    C2 --> C3["Complex Compounds"]
    C3 --> H["Humanus & High-Tier Disciplines"]
```

---

## The Forms of Aspects

Aspects exist in three distinct forms throughout your journey:

### 1. Research Aspects (Knowledge Pool)
Stored in the player's mind. When you scan items with a [Thaumometer](Scanning-&-Thaumometer) or deconstruct items at a Research Table, aspects are credited to your knowledge pool. These are consumed when solving research hexes in the Thaumonomicon.

### 2. Environmental Vis (Aura Nodes)
Ambient primal energy suspended in the aura. Unrefined vis exists naturally only as the six primal elements, stored inside [Aura Nodes](Aura-Nodes).

### 3. Physical Essentia (Liquid Magic)
When matter is smelted in an [Alchemical Furnace](Essentia-Distillation), its physical shell is destroyed, leaving behind pure liquid **Essentia**. Essentia is stored in [Warded Jars](Essentia-Storage), piped through tubes, and used to fuel [Infusion Altar](Infusion-Altar) rituals and [Crucible Alchemy](Crucible-Alchemy).

---

## Primal vs Compound Aspects

- **Primal Aspects** cannot be broken down further. They are the six foundation stones of the universe:
  - **Aer** (Air)
  - **Terra** (Earth)
  - **Ignis** (Fire)
  - **Aqua** (Water)
  - **Ordo** (Order)
  - **Perditio** (Entropy)

- **Compound Aspects** are formed by combining two simpler aspects. For example:
  $$\text{Aqua} + \text{Terra} = \text{Victus (Life)}$$
  $$\text{Victus} + \text{Perditio} = \text{Mortuus (Death)}$$
  $$\text{Mortuus} + \text{Motus} = \text{Exanimis (Undead)}$$

---

## Research Aspect Combining

At the **Research Table**, if you run low on a specific aspect, you can synthesize it by clicking two component aspects on the illuminated wheel. 

> [!TIP]
> Always maintain a healthy pool of primal aspects by scanning environmental features (water, stone, obsidian, torch flames, skies) and tapping crafting tables with scribing tools.

---

## Related Topics
- [The Six Primal Aspects](Primal-Aspects)
- [Compound Aspects Chart](Compound-Aspects-Chart)
- [Scanning & Thaumometer](Scanning-&-Thaumometer)
- [Research Mechanics](Research-Mechanics)
- [Home](Home)
"""

primal_aspects = """# The Six Primal Aspects

**Category:** Aspects of Magic | [Return to Wiki Home](Home)

---

## Overview

The six **Primal Aspects** are the irreducible building blocks of reality. Every compound aspect and every spell effect can be traced back to these six primordial forces.

---

## The Primal Six

### 1. 💨 Aer (Air)
- **Symbol / Affinity:** Pale yellow wind swirling in a vortex.
- **Concepts:** Flight, wind, sky, lightning, freedom, ether, speed.
- **Common Sources:** Feathers, Wool, Bows, Arrows, Dandelion, Sugar Cane, Snow, Sand.
- **Thaumaturgical Uses:** Foci of Shock and Flight, Wand recharge from sky nodes, Boots of the Traveller.

### 2. 🪨 Terra (Earth)
- **Symbol / Affinity:** Deep mossy green square.
- **Concepts:** Foundation, soil, stone, durability, density, mineral stability.
- **Common Sources:** Dirt, Stone, Cobblestone, Clay, Obsidian, Bricks, Ores.
- **Thaumaturgical Uses:** Wand Focus: Excavation, Arcane Stone construction, Golem armor plating.

### 3. 🔥 Ignis (Fire)
- **Symbol / Affinity:** Bright fiery orange flame.
- **Concepts:** Heat, flame, combustion, light, fury, kinetic energy.
- **Common Sources:** Torches, Coal, Charcoal, Lava, Blazes, Netherrack, Gunpowder.
- **Thaumaturgical Uses:** Wand Focus: Fire, Infernal Furnace smelting, Nitor lighting.

### 4. 💧 Aqua (Water)
- **Symbol / Affinity:** Crisp oceanic cyan droplet.
- **Concepts:** Fluidity, cleansing, oceans, rainfall, hydration, coolness.
- **Common Sources:** Water Buckets, Lily Pads, Clay Balls, Fish, Snowballs, Ice.
- **Thaumaturgical Uses:** Wand Focus: Frost, Crucible boiling replenishment, Alchemical dilution.

### 5. ⚖️ Ordo (Order)
- **Symbol / Affinity:** Pure crystal silver-white diamond.
- **Concepts:** Symmetry, structure, crystal clarity, logic, purification, enchantment.
- **Common Sources:** Smooth Stone, Glass, Quartz, Redstone, Silverwood, Chiseled Stone Bricks.
- **Thaumaturgical Uses:** Purifying tainted areas, stabilizing Infusion Altars, crafting Golem sorting cores.

### 6. 💥 Perditio (Entropy)
- **Symbol / Affinity:** Pitch black shattered void.
- **Concepts:** Chaos, decay, dissolution, destruction, entropy, breakdown.
- **Common Sources:** Gunpowder, TNT, Cobblestone, Rotten Flesh, Sand, Bones.
- **Thaumaturgical Uses:** Wand Focus: Primal, Alchemical centrifuging, decomposing complex essentia.

---

## Primal Opposition & Synergy

Primal aspects exist in dynamic polarity:
- **Ignis (Fire)** opposes **Aqua (Water)** $\rightarrow$ Combines to form **Gelum (Frost)**.
- **Aer (Air)** opposes **Terra (Earth)** $\rightarrow$ Combines to form **Motus (Motion)**.
- **Ordo (Order)** opposes **Perditio (Entropy)** $\rightarrow$ Represents the balance of creation and decay.

---

## Related Topics
- [Aspects Overview](Aspects-Overview)
- [Compound Aspects Chart](Compound-Aspects-Chart)
- [Scanning & Thaumometer](Scanning-&-Thaumometer)
- [Home](Home)
"""

compound_chart = """# Compound Aspects & Combination Chart

**Category:** Aspects of Magic | [Return to Wiki Home](Home)

---

## Overview

In **NeoThaum** (as in Thaumcraft 4.2.3.5), all compound aspects are created from the six primal elements. This reference chart documents every combination formula required to complete your research book.

---

## Full Aspect Combination Tree

| Aspect | Meaning | Component 1 | Component 2 | Common Item Sources |
| :--- | :--- | :---: | :---: | :--- |
| **Gelum** | Frost / Cold | Ignis | Aqua | Ice, Snow, Packed Ice |
| **Lux** | Light | Aer | Ignis | Torch, Glowstone, Lantern |
| **Motus** | Motion | Aer | Aqua | Trapdoor, Rail, Boat |
| **Permutatio** | Exchange / Trade | Motus | Ordo | Copper, Hoppers, Redstone |
| **Potentia** | Energy / Power | Ordo | Ignis | Redstone, Charcoal, Glowstone |
| **Tempestas** | Weather / Storm | Aer | Aqua | Lightning Rod, Weathered Copper |
| **Vacuos** | Void / Emptiness | Aer | Perditio | Glass Bottle, Bowl, Chest |
| **Venenum** | Poison | Aqua | Perditio | Spider Eye, Pufferfish |
| **Victus** | Life | Aqua | Terra | Seeds, Raw Meats, Saplings |
| **Vitreus** | Glass / Crystal | Terra | Ordo | Glass, Sand, Quartz, Amethyst |
| **Bestia** | Beasts / Animals | Motus | Victus | Leather, Wool, Eggs, Feathers |
| **Fames** | Hunger | Vacuos | Victus | Rotten Flesh, Raw Meat |
| **Herba** | Flora / Plants | Terra | Victus | Leaves, Grass, Flowers |
| **Iter** | Travel | Motus | Terra | Minecart, Saddle, Boots |
| **Limus** | Slime | Victus | Aqua | Slimeball, Sticky Piston |
| **Metallum** | Metal | Terra | Vitreus | Iron Ingot, Gold Ingot, Anvil |
| **Mortuus** | Death | Victus | Perditio | Bone, Gunpowder, Rotten Flesh |
| **Praecantatio** | Magic | Vacuos | Potentia | Ender Pearl, Nether Wart, Shard |
| **Sano** | Healing | Ordo | Victus | Golden Apple, Potion of Healing |
| **Tenebrae** | Darkness | Lux | Vacuos | Obsidian, Ink Sac, Coal |
| **Vinculum** | Trap / Binding | Motus | Perditio | Cobweb, Lead, Soul Sand |
| **Volatus** | Flight | Aer | Motus | Feather, Elytra, Phantom Membrane |
| **Alienis** | Eldritch / Alien | Tenebrae | Vacuos | Ender Pearl, Eye of Ender |
| **Arbor** | Trees / Wood | Aer | Herba | Wood Logs, Planks, Saplings |
| **Auram** | Aura / Pure Vis | Aer | Praecantatio | Ethereal Bloom, Pure Node |
| **Corpus** | Flesh / Body | Bestia | Mortuus | Porkchop, Steak, Flesh |
| **Exanimis** | Undead | Motus | Mortuus | Bone, Rotten Flesh, Skull |
| **Spiritus** | Spirit / Soul | Mortuus | Victus | Soul Sand, Ghast Tear |
| **Vitium** | Taint / Blight | Perditio | Praecantatio | Tainted items, Flux Slime |
| **Cognitio** | Mind / Thought | Ignis | Spiritus | Paper, Book, Bookshelf |
| **Sensus** | Senses / Sight | Aer | Spiritus | Redstone, Carrot, Eye |
| **Gula** | Gluttony | Fames | Vacuos | Cake, Golden Carrot |
| **Invidia** | Envy | Sensus | Fames | Emerald, Gold Nugget |
| **Desidia** | Sloth | Vinculum | Spiritus | Bed, Clock |
| **Superbia** | Pride | Volatus | Vacuos | Diamond, Crown |
| **Ira** | Wrath | Ignis | Telum | Gunpowder, TNT |
| **Humanus** | Humanity | Bestia | Cognitio | Rotten Flesh, Villager items |
| **Instrumentum** | Tool | Humanus | Ordo | Stick, Pickaxe, Shovel, Axe |
| **Lucrum** | Greed / Wealth | Fames | Humanus | Gold Ingot, Emerald |
| **Messis** | Harvest / Crops | Herba | Humanus | Wheat, Carrot, Potato, Bread |
| **Perfodio** | Mining / Excavation | Humanus | Terra | Pickaxe, Iron Ore |
| **Telum** | Weapon / Arrow | Instrumentum | Ignis | Sword, Bow, Arrow |
| **Tutamen** | Armor / Defense | Instrumentum | Terra | Shield, Chestplate, Helm |
| **Fabrico** | Crafting | Humanus | Instrumentum | Crafting Table, Workbench |
| **Machina** | Mechanism | Instrumentum | Motus | Piston, Dispenser, Redstone |
| **Metus** | Fear | Spiritus | Vacuos | Skull, Creepers |
| **Pannus** | Cloth / Fabric | Instrumentum | Bestia | Wool, String, Leather |

---

## Combining Tips for Research

1. **Short Paths**: When connecting nodes on research notes, look for aspects that share primal origins. For example, `Lux` (Aer + Ignis) easily links with `Potentia` (Ordo + Ignis) through `Ignis`.
2. **Abundant Aspects**: Crafting and scanning cheap mundane items like Paper (`Cognitio`), Torches (`Lux`), and Crafting Tables (`Fabrico`) is the fastest way to replenish depleted pools.

---

## Related Topics
- [Aspects Overview](Aspects-Overview)
- [Research Mechanics](Research-Mechanics)
- [Scanning & Thaumometer](Scanning-&-Thaumometer)
- [Home](Home)
"""

scanning = """# Scanning & The Thaumometer

**Category:** Basic Information & Research | [Return to Wiki Home](Home)

---

## Overview

The **Thaumometer** is the primary scientific apparatus of a Thaumaturge. Pointing and scanning the Thaumometer at the world uncovers the invisible aspect composition of matter and deposits knowledge directly into your research inventory.

---

## How to Scan

1. Place the **Thaumometer** in your main hand.
2. Aim your crosshair at any world block, dropped entity item, or living mob.
3. Hold **Right-Click** for approximately 1.5 seconds.
4. You will see runic particles concentrate toward your reticle:
   - **Success (Chime & Aspects Displayed)**: The item's constituent aspects appear in the lower-right corner of your screen, and points are added to your research ledger.
   - **Failure (You lack the research to understand this)**: The target contains a compound aspect you have not yet synthesized or discovered.
   - **Already Scanned**: The HUD notifies you that you have already analyzed this subject.

---

## Scanning Progression Roadmap

To build your aspect knowledge efficiently, follow this early-game scanning sequence:

```mermaid
graph TD
    A["1. The Primals: Water, Dirt, Sky, Fire, Torch, Cobble"] --> B["2. Early Compounds: Glass (Vitreus), Coal (Potentia), Sand (Perditio)"]
    B --> C["3. Living Matter: Trees, Saplings, Flowers (Herba), Animals (Bestia)"]
    C --> D["4. Civilized Tools: Crafting Table (Fabrico), Chest (Vacuos), Paper (Cognitio)"]
    D --> E["5. Advanced Artifacts: Obsidian (Alienis), Redstone (Machina), Gold (Lucrum)"]
```

### Essential Early Scans:
- **Water Bucket / Ocean**: Unlocks abundant `Aqua`.
- **Torch / Fire**: Unlocks `Ignis` and `Lux`.
- **Dirt / Stone / Sand**: Unlocks `Terra` and `Perditio`.
- **Wool / Leather / Raw Beef**: Unlocks `Bestia` and `Victus`.
- **Crafting Table**: Unlocks `Fabrico`, `Instrumentum`, and `Arbor`.
- **Bookshelf / Paper**: Unlocks `Cognitio` and `Spiritus`.

---

## Research Point Economy

- Each unique scan permanently grants between **1 to 5 research points** for each aspect contained within that object.
- Mobs reward higher point yields upon first scan (e.g. Creepers provide `Ignis`, `Perditio`, and `Metus`).
- Celestial bodies (Sun, Moon) and weather conditions (Rain, Thunderstorm) can also be scanned!

---

## Related Topics
- [Aspects Overview](Aspects-Overview)
- [The Six Primal Aspects](Primal-Aspects)
- [Research Mechanics](Research-Mechanics)
- [Home](Home)
"""

research_mechanics = """# Research Mechanics & The Thaumonomicon

**Category:** Basic Information & Research | [Return to Wiki Home](Home)

---

## Overview

Research is the core progression loop of **NeoThaum**. Rather than relying on simple linear crafting recipes, arcane secrets must be deciphered using the **Research Table** through an engaging hexagonal puzzle minigame.

---

## Setting Up Your Research Laboratory

### Materials Needed:
1. **Research Table**: Place two wooden Tables side by side and right-click with **Scribing Tools**.
2. **Scribing Tools**: 1x Glass Phial, 1x Ink Sac, 1x Feather. (Holds 100 ink charges).
3. **Paper**: Crafted from sugar cane.
4. **Thaumonomicon**: Your research book.

---

## Solving Research Notes: The Hex Mini-Game

1. Open your **Thaumonomicon** and find a research entry with a parchment icon.
2. Click it to extract a **Research Note** (consumes 1 Paper and 1 Ink from your inventory).
3. Place the Research Note into the active slot on the **Research Table**.
4. You will see a grid of hexagonal tiles with several fixed aspect runes:
   - Your objective is to create an unbroken connection between all starting runes.
   - Two adjacent hexes can connect if and only if **they share a parent-child relationship** according to the [Compound Aspects Chart](Compound-Aspects-Chart).

### Connection Rules Example:
$$\text{Aer} \longleftrightarrow \text{Lux} \longleftrightarrow \text{Ignis}$$
Because $\text{Aer} + \text{Ignis} = \text{Lux}$, `Lux` can bridge directly between `Aer` and `Ignis`.

$$\text{Victus} \longleftrightarrow \text{Bestia} \longleftrightarrow \text{Motus} \longleftrightarrow \text{Iter} \longleftrightarrow \text{Terra}$$
Here, `Bestia` connects to `Victus`, `Motus` connects to `Bestia`, `Iter` connects to `Motus`, and `Terra` connects to `Iter`.

---

## Warp & Forbidden Knowledge

Some research entries in the Thaumonomicon are surrounded by a **deep purple pulsing border** and marked as **Forbidden Knowledge**:

> [!WARNING]
> Delving into Forbidden Knowledge permanently warps your mind. As your Warp increases, you will suffer auditory hallucinations, spontaneous blindness, unnatural hunger, and surprise ambushes by Eldritch Guardians. See [Warp Mechanics](Warp-Mechanics) for full details and sanitizing remedies.

---

## Video Tutorial: Hex Grid Mastery

[![Thaumcraft 4 Research Mini-game Guide](https://img.youtube.com/vi/Dq0XyZ2Z3Uo/0.jpg)](https://www.youtube.com/watch?v=Dq0XyZ2Z3Uo)
*(Tutorial courtesy of Mischief of Mice)*

---

## Related Topics
- [Aspects Overview](Aspects-Overview)
- [Compound Aspects Chart](Compound-Aspects-Chart)
- [Scanning & Thaumometer](Scanning-&-Thaumometer)
- [Warp Mechanics & Sanity](Warp-Mechanics)
- [Home](Home)
"""

aura_nodes = """# Aura Nodes

**Category:** Basic Information & Research | [Return to Wiki Home](Home)

---

## Overview

**Aura Nodes** are spherical fonts of ambient magical energy floating throughout the world. Invisible to the naked eye, they can only be perceived when peering through a **Thaumometer** or wearing **Goggles of Revealing**.

Nodes are essential for recharging wands, powering vis relays, and establishing automated arcane machinery.

---

## Node Types

Every Aura Node possesses a specific type that dictates its environmental interaction:

| Type | Visual Appearance | Unique Behavior |
| :--- | :--- | :--- |
| **Normal** | Gentle steady shimmering sphere | Neutral node. Slowly replenishes its aspects over time. |
| **Pure** | Brilliant turquoise / silvery light | Naturally embedded within the trunks of Silverwood trees. Repels taint and purifies surrounding soil into Magical Forest biome. |
| **Dark (Sinister)** | Inky black and purple vortex | Found at Barrow Mounds and Eldritch Obelisks. Spawns Furious Zombies and creates localized sinister darkness. |
| **Unstable** | Sparking erratic particles | Aspects fluctuate randomly between discharges. |
| **Hungry** | Violent vortex with gravitational rings | **Extremely Dangerous**. Acts as a miniature black hole, vacuuming up dropped items, pulling players and mobs inward, and devouring terrain blocks to grow in aspect capacity. |
| **Tainted** | Oozing purple fibrous tendrils | Infects adjacent land with Taint, corrupting passive mobs into tainted monstrosities. |

---

## Node Modifiers

In addition to its type, a node has a vitality modifier:
- **Bright**: Recharges its stored vis twice as fast as normal. Highly prized for energized vis networks!
- **Normal**: Standard recharge rate.
- **Pale**: Slower than normal recharge rate.
- **Fading**: Extremely slow recharge. In danger of dying if drained too greedily.

---

## Relocating Nodes: Node in a Jar

To move a node to your base:
1. Build a $3 \\times 3 \\times 3$ glass cage around the node (with wooden slabs on top and bottom).
2. Right-click the glass cage with a wand holding at least **70 Vis** of all six primals.
3. The structure collapses into a portable **Node in a Jar** item.

> [!CAUTION]
> Jarring a node causes traumatic damage to its structure: there is a high probability that the node's vitality drops down one step (e.g. Bright $\rightarrow$ Normal, Normal $\rightarrow$ Pale).

---

## Energized Nodes & Centi-Vis (cV)

In the mid-to-late game, you can convert raw nodes into permanent **Energized Nodes**:
1. Place a **Node Stabilizer** directly beneath the node.
2. Place a **Node Transducer** directly above the node.
3. Apply a redstone signal to the Transducer.
4. The node will convert into an energized crystalline beam, producing a continuous stream of **Centi-Vis (cV)** per second. This power can be broadcast across your workshop through **Vis Relays** to recharge wands on pedestals, power Focal Manipulators, and run Infernal Furnaces.

---

## Related Topics
- [Node Tapping & Recharging](Node-Tapping-&-Recharging)
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Biomes & Thaumic Flora](Biomes-&-Flora)
- [Home](Home)
"""

node_tapping = """# Wand Recharging & Node Management

**Category:** Basic Information & Research | [Return to Wiki Home](Home)

---

## Overview

Wands, staves, and scepters require continuous supplies of primal vis to perform spellcasting and power Arcane Worktable crafting. Managing your vis reserves and caring for your local Aura Nodes is a primary duty of every Thaumaturge.

---

## Methods of Recharging Wands

### 1. Manual Node Draining (Tapping)
- Equip your wand in your main hand and aim at an Aura Node within range.
- Hold **Right-Click** to siphon vis. A swirling beam of light connects the node to the wand tip, rapidly replenishing the wand's primal reservoirs.

> [!WARNING]
> **Do not over-drain nodes!** If you siphon an aspect completely down to 0, there is a substantial chance that the node permanently loses that aspect capacity, or degrades into a Pale/Fading node. Always release right-click when a node reaches 1 or 2 remaining points!

### 2. Vis Shard Drops
- Slaying hostile mobs has a chance to drop elemental vis shards.
- Carrying a wand in your hotbar while picking up shards automatically absorbs their aspect charge directly into the wand.

### 3. Recharge Pedestal & Energized Vis Relays
- Place an **Arcane Pedestal** near an energized vis network.
- Upgrade it into a **Recharge Pedestal**.
- Resting any wand or staff atop the pedestal will wirelessly siphon cV from the network, restoring full charge hands-free in seconds!

### 4. Amulet of Vis
- A wearable bauble worn in the amulet slot.
- Can be charged at a recharge pedestal and gradually trickles vis into whichever wand you are currently carrying.

---

## Wand Storage Capacities

| Casting Implement | Primal Capacity | Vis Discount |
| :--- | :---: | :---: |
| **Iron-Capped Wooden Wand** | 25 Vis | -10% (110% cost) |
| **Gold-Capped Greatwood Wand** | 50 Vis | 0% (100% cost) |
| **Thaumium-Capped Silverwood Wand** | 100 Vis | +10% (90% cost) |
| **Silverwood Scepter** | 150 Vis | +15% (85% cost) |
| **Primal Staff** | 250 Vis | +20% (80% cost) |

---

## Related Topics
- [Aura Nodes](Aura-Nodes)
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Vis Storage & Discount Robes](Vis-Storage-&-Discount-Armor)
- [Home](Home)
"""

for fname, text in [
    ("Aspects-Overview.md", aspects_overview),
    ("Primal-Aspects.md", primal_aspects),
    ("Compound-Aspects-Chart.md", compound_chart),
    ("Scanning-&-Thaumometer.md", scanning),
    ("Research-Mechanics.md", research_mechanics),
    ("Aura-Nodes.md", aura_nodes),
    ("Node-Tapping-&-Recharging.md", node_tapping)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

crucible_alchemy = """# Crucible Alchemy

**Category:** Alchemy | [Return to Wiki Home](Home)

---

## Overview

**Crucible Alchemy** is the science of transmuting base matter into magical items and materials. By dissolving objects into boiling water, you reduce them to their constituent spiritual aspects. Once the correct aspect combination is present, tossing in a final **catalyst** item triggers a chemical reaction that consumes the aspects and produces the desired enchanted output.

---

## Constructing the Crucible

1. Place a mundane iron **Cauldron** in the world.
2. Ensure there is a heat source directly beneath the cauldron:
   - **Fire** (Netherrack lit with flint & steel)
   - **Lava** (flowing or source block)
   - **Nitor** (alchemical flame block that never burns out)
3. Fill the cauldron with a **Water Bucket**.
4. Right-click the cauldron with any **Wand**.
5. The cauldron transforms into a mystical **Crucible** featuring gold trim and rune engravings.

```
       [   Crucible (Water)  ]
[ Heat Source: Fire / Lava / Nitor ]
```

When properly heated, steam particles and bubbling bubbles will rise from the surface.

---

## The Transmutation Process

1. **Verify Heat & Water**: Ensure the crucible is visibly boiling.
2. **Dissolve Aspect Ingredients**: Throw items with the necessary aspects into the boiling water. The aspects immediately dissolve into the liquid broth.
3. **Inspect Contents**: Wear [Goggles of Revealing](Vis-Storage-&-Discount-Armor) to see a floating aspect breakdown HUD above the crucible.
4. **Toss the Catalyst**: Drop the designated catalyst item into the broth.
5. **Collection**: The crafted item pops out of the cauldron, and the consumed aspect quantities are deducted from the water.

### Example Recipe: Alchemical Brass
- **Aspect Requirements:** 2x *Instrumentum*, 2x *Metallum*
- **Source Ingredients:** 1x Iron Ingot (provides *Metallum*) + 1x String or Stick
- **Catalyst:** 1x Copper Ingot
- **Result:** 1x Alchemical Brass Ingot

---

## Flux & Aspect Decomposition

> [!CAUTION]
> **Do not leave unconsumed aspects sitting in a boiling crucible!**
> Aspects begin dissolving and evaporating after 10-15 seconds. When aspects evaporate, they turn directly into **Flux**:
> - **Flux Gas**: Purple noxious clouds that rise into the ceiling and inflict Nausea and Warp.
> - **Flux Slime**: Hostile purple ooze slimes that hop out of the cauldron and attack players.
>
> If you make an error or need to empty a crucible, shift-right-click it with an empty wand to dump all contents harmlessly as mundane water.

---

## Related Topics
- [Essentia Distillation](Essentia-Distillation)
- [The Thaumatorium](Thaumatorium)
- [Aspects Overview](Aspects-Overview)
- [Home](Home)
"""

essentia_distill = """# Essentia Distillation

**Category:** Alchemy | [Return to Wiki Home](Home)

---

## Overview

While the Crucible dissolves items directly into boiling water, advanced alchemy requires harvesting pure, refined **Liquid Essentia** for piping, jar storage, and Infusion Altar rituals. This is accomplished through **Essentia Distillation**.

---

## The Distillation Apparatus

A distillation setup consists of two core components:
1. **Alchemical Furnace**: Smelts down physical matter into raw aspect slurry.
2. **Arcane Alembics**: Stacked atop the furnace to condense aspect vapor into liquid essentia.

```
[  Arcane Alembic #3  ]
[  Arcane Alembic #2  ]
[  Arcane Alembic #1  ]
[ Alchemical Furnace  ]
[ Fuel: Coal / Ignis  ]
```

### Stacking Alembics
You can stack up to **5 Arcane Alembics** directly on top of a single Alchemical Furnace. Each alembic can independently condense and hold up to **32 points of a single aspect**.

---

## Operating the Furnace

1. Place fuel (Coal, Charcoal, or Alchemical Coal) into the lower slot of the Alchemical Furnace.
2. Insert the items to be distilled into the top input slot.
3. As items burn, their aspects evaporate into the alembics above:
   - Alembic #1 will capture the first aspect type.
   - If an item contains multiple aspects, excess aspects rise to Alembic #2, #3, etc.
   - If all alembics are full or occupied by other aspects, the furnace will halt with an error chime.

---

## Harvesting Liquid Essentia

- **Using Glass Phials**: Right-clicking an alembic with an empty Glass Phial extracts **8 points of Essentia**, creating an *Essentia Phial*.
- **Using Warded Jars**: Right-clicking an alembic with an empty or matching [Warded Jar](Essentia-Storage) drains all stored essentia (up to 64 points) into the jar.
- **Using Essentia Tubes**: Connect [Essentia Tubes](Essentia-Transport-&-Piping) to the side or top of alembics to pipe liquid essentia automatically into automated jar sorting rooms.

---

## Related Topics
- [Essentia Transport & Piping](Essentia-Transport-&-Piping)
- [Essentia Storage](Essentia-Storage)
- [The Thaumatorium](Thaumatorium)
- [Home](Home)
"""

essentia_pipes = """# Essentia Transport & Piping

**Category:** Alchemy | [Return to Wiki Home](Home)

---

## Overview

**Essentia Tubes** are specialized brass pipes lined with thaumic glass that transport pure liquid Essentia between alembics, jars, crucibles, and Infusion Altars. Unlike standard item pipes, Essentia fluid dynamics are governed strictly by **Suction Pressure**.

---

## Suction Physics

Liquid essentia does not flow by gravity; it travels toward the **highest source of suction**.

### Types of Suction:
1. **Untyped Suction**: Generated by empty jars and open pipes. Pulls any available essentia, but is easily overridden by typed suction.
2. **Typed Suction**: Generated by [Warded Jars](Essentia-Storage) with labels, or filtered valves. Pulls *only* a specific aspect (e.g. *Ignis*), pulling with maximum force (suction value: 32).

> [!IMPORTANT]
> **Suction Loss**: Every block of plain Essentia Tube reduces suction strength by **1**. If a pipe runs for 33 blocks without a pump or buffer, suction drops to 0 and fluid stops moving.

---

## Piping Components

| Component | Function |
| :--- | :--- |
| **Essentia Tube** | Standard piping segment. Reduces suction by 1 per block. |
| **Directional Tube** | A one-way check valve pipe that allows essentia to flow in only one direction, preventing backflow. |
| **Filtered Tube** | Only allows a specified aspect to pass through. Set by right-clicking with a labeled phial. |
| **Essentia Valve** | Can be opened or closed manually by right-clicking or powered via a redstone signal. |
| **Essentia Buffer** | A dual-chamber brass tank that balances conflicting suction signals. Crucial for connecting multiple alembics to a shared jar highway! |

---

## Best Practices for Essentia Plumbing

1. **Avoid Tube Loops**: Closed pipe loops create competing suction loops that freeze fluid flow. Always use branching trees or directional valves.
2. **Use Buffers at Junctions**: When multiple alembic lines converge into a central sorting tube, place an **Essentia Buffer** at the intersection. You can adjust the suction on each side of the buffer by right-clicking its ports with a wand.
3. **Keep Lines Short**: Position your Warded Jar storage wall directly adjacent to or above your Alchemical Furnace array to minimize pipe length.

---

## Related Topics
- [Essentia Distillation](Essentia-Distillation)
- [Essentia Storage](Essentia-Storage)
- [The Thaumatorium](Thaumatorium)
- [Home](Home)
"""

essentia_storage = """# Essentia Storage

**Category:** Alchemy | [Return to Wiki Home](Home)

---

## Overview

Safeguarding your liquid essentia is vital for large-scale crafting and infusion rituals. NeoThaum provides three tiers of essentia containment vessels.

---

## Storage Vessels

### 1. Glass Phials
- **Capacity:** Exactly **8 Essentia**.
- **Usage:** Crafted from glass. Used to extract essentia from alembics and jars for compact crafting recipes and aspect labels.

### 2. Warded Jars
- **Capacity:** Up to **64 Essentia** of a single aspect type.
- **Traits:** Made from reinforced warded glass and arcane wood. Jars generate moderate untyped suction when empty, and strong typed suction when holding an aspect.
- **In-World HUD:** When holding a jar or looking at it with Goggles of Revealing, an aspect symbol and fill level bar are rendered in real time.

### 3. Void Jars
- **Capacity:** Up to **64 Essentia**.
- **Traits:** Forged by combining a Warded Jar with an obsidian void shard.
- **Function:** Behaves identically to a normal Warded Jar, except that **any additional essentia piped in beyond 64 is immediately voided into nothingness**.
- **Why use Void Jars?** Prevents mass distillation systems from backing up and clogging alembics when common byproduct aspects (like *Terra* or *Aqua*) overfill!

---

## Aspect Labels

By default, an empty Warded Jar will accept whichever aspect arrives first. To permanently dedicate a jar to a specific aspect:
1. Craft an **Aspect Label** (Paper + Scribing Tools).
2. Combine the label in a crafting grid with an **Essentia Phial** of the desired aspect.
3. Shift-right-click the label onto a placed Warded Jar.
4. The jar will display the aspect crest on its side and will **never accept any other aspect**, even if emptied to 0!

---

## Related Topics
- [Essentia Distillation](Essentia-Distillation)
- [Essentia Transport & Piping](Essentia-Transport-&-Piping)
- [The Thaumatorium](Thaumatorium)
- [Home](Home)
"""

thaumatorium = """# The Thaumatorium

**Category:** Alchemy | [Return to Wiki Home](Home)

---

## Overview

The **Thaumatorium** is an advanced multiblock alchemical construct that automates crucible transmutation without exposing the workshop to volatile boiling water, aspect decay, or flux pollution.

---

## Multiblock Construction

### Required Blocks:
- 1x **Crucible**
- 2x **Alchemical Constructs**

### Assembly Steps:
1. Place the **Crucible** on the floor above a heat source (Fire, Lava, or Nitor).
2. Stack two **Alchemical Constructs** directly on top of the Crucible.
3. Right-click the lower Alchemical Construct with any **Wand**.
4. The blocks seamlessly fuse into the unified **Thaumatorium** machine.

```
[ Alchemical Construct ]
[ Alchemical Construct ]  ➔ [ Right-Click with Wand ] ➔ [ Unified Thaumatorium ]
[       Crucible       ]
[      Heat Source     ]
```

---

## Operating the Thaumatorium

1. **Supply Water & Heat**: Ensure the bottom crucible retains water and heat.
2. **Open the GUI**: Right-click the front brass hatch to access the recipe selector.
3. **Select Recipe**: The GUI lists every crucible formula you have unlocked in your Thaumonomicon. Click a recipe to lock it into the active chamber.
4. **Pipe In Essentia**: Connect [Essentia Tubes](Essentia-Transport-&-Piping) to the ports on the side or rear of the machine. The Thaumatorium will automatically exert suction to pull only the exact aspects required by the locked recipe!
5. **Feed Catalysts**: Drop catalyst items into the top hatch (or pipe them in using hoppers or [Animation Golems](Animation-Cores)).
6. **Ejection**: The finished transmuted items are automatically ejected from the front chute into a hopper or chest.

---

## Advantages Over Manual Crucibles

- **Zero Flux Evaporation**: Aspects held in the internal buffer never decompose into flux gas or slime.
- **Precision Consumption**: Only the exact required aspects are pulled from jars; zero excess waste.
- **Hands-Free Automation**: Completely compatible with automated factories and golemancy logistics.

---

## Related Topics
- [Crucible Alchemy](Crucible-Alchemy)
- [Essentia Transport & Piping](Essentia-Transport-&-Piping)
- [Golemancy](Golem-Basics-&-Materials)
- [Home](Home)
"""

centrifuge = """# Centrifuge & Crystallizer

**Category:** Alchemy | [Return to Wiki Home](Home)

---

## Overview

As your alchemy operations grow, you will encounter situations where you need raw primal aspects that are trapped inside complex compound essentia, or where liquid storage becomes unwieldy. The **Alchemical Centrifuge** and **Essentia Crystallizer** solve these logistics challenges.

---

## The Alchemical Centrifuge

The Centrifuge is an electric/arcane motorized device that spins liquid essentia at hyper speed, separating compound aspects into their two constituent components.

### Operation:
1. Connect an Essentia Tube input to the bottom port of the Centrifuge.
2. Pipe in any compound essentia (for example, *Lux*).
3. The Centrifuge will spin with high-pitched gears and output its two parent aspects (*Aer* and *Ignis*) from its upper output ports.

```mermaid
graph TD
    IN["Input: Compound Essentia (e.g. Lux)"] --> C["Alchemical Centrifuge"]
    C --> OUT1["Output A: Primal (Aer)"]
    C --> OUT2["Output B: Primal (Ignis)"]
```

> [!TIP]
> The Centrifuge is the best way to generate rare or high-demand primals (like *Ordo* or *Perditio*) from abundant junk compounds like *Vitreus* or *Motus*.

---

## The Essentia Crystallizer

The Crystallizer transforms volatile liquid essentia into stable, stackable **Aspect Shards**.

### Benefits:
- **Compact Storage**: Shards stack up to 64 in standard chests, freeing up hundreds of glass jars.
- **Alchemical Feedstock**: Solid shards can be tossed directly back into Crucibles or dissolved into alembics whenever needed.

---

## Related Topics
- [Essentia Distillation](Essentia-Distillation)
- [Essentia Transport & Piping](Essentia-Transport-&-Piping)
- [The Thaumatorium](Thaumatorium)
- [Home](Home)
"""

for fname, text in [
    ("Crucible-Alchemy.md", crucible_alchemy),
    ("Essentia-Distillation.md", essentia_distill),
    ("Essentia-Transport-&-Piping.md", essentia_pipes),
    ("Essentia-Storage.md", essentia_storage),
    ("Thaumatorium.md", thaumatorium),
    ("Alchemical-Centrifuge-&-Crystallizer.md", centrifuge)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

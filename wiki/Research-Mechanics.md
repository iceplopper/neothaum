# Research Mechanics & The Thaumonomicon

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
$$	ext{Aer} \longleftrightarrow 	ext{Lux} \longleftrightarrow 	ext{Ignis}$$
Because $	ext{Aer} + 	ext{Ignis} = 	ext{Lux}$, `Lux` can bridge directly between `Aer` and `Ignis`.

$$	ext{Victus} \longleftrightarrow 	ext{Bestia} \longleftrightarrow 	ext{Motus} \longleftrightarrow 	ext{Iter} \longleftrightarrow 	ext{Terra}$$
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

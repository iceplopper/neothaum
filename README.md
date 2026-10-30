# NeoThaum 4

<div align="center">

![NeoThaum Banner](src/main/resources/assets/neothaum/icon.png)

### *The ancient art of Thaumaturgy, reborn for modern Minecraft.*

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric%200.20%2B-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-All--Rights--Reserved-red.svg)](LICENSE)

</div>

---

## 🔮 What is NeoThaum?

**NeoThaum** is a faithful, 1:1 recreation of Azanor's legendary magic mod **Thaumcraft 4** (specifically TC 4.2.3.5) engineered from the ground up for modern Minecraft (**26.3 / 1.21.4+**) using the **Fabric** toolchain.

Our core mission is to make the mod **visually, mechanically, and audibly indistinguishable** from the original 1.7.10 masterpiece, while leveraging modern rendering pipelines, data-driven registries, and high performance.

---

## ✨ Features & Thaumaturgical Systems

### 📜 The Thaumonomicon & Research Progression
- Complete interactive research book with aspect hex linking, category navigation, and discovery milestones.
- Research entries unlock arcane blueprints, device schematics, and lost eldritch lore.

### 🪄 Wands, Staves & Focus Casting
- **Wands**: Wooden, Greatwood, and Silverwood rods equipped with Iron, Gold, Copper, Silver, Thaumium, and Void Metal caps.
- **Combat Staves**: Heavy casting instruments crafted from dual-rod staff cores featuring 2.5× vis storage and high melee combat damage.
- **Casting Foci**: Fire, Frost, Shock, Excavation, Portable Hole, Hellbat, Primal, Warding, and Pech trade foci.
- **HUD & Radial Selector**: Real-time primal vis display (Aer, Terra, Ignis, Aqua, Ordo, Perditio) and focus selection wheel.

### 🧪 Essentia Distillation & Alchemy
- **Alchemical Furnace & Stacked Alembics**: Break down items into liquid Essentia.
- **Piping & Storage**: Warded Jars, Void Jars, Essentia Tubes, Directional Valves, Buffers, Centrifuges, and Crystallizers.
- **The Crucible & Thaumatorium**: Dynamic fluid boiling, Thaumometer aspect overlays, and automated catalyst production.

### ⚡ Aura Nodes & The Vis Network
- Naturally generating Aura Nodes with distinct types (Normal, Pure, Dark, Unstable, Hungry, Tainted) and modifiers (Bright, Pale, Fading).
- Node manipulation: Node in a Jar, Node Stabilizers, and Node Transducers feeding energizing vis networks to recharge pedestals and vis relays.

### 🔮 Infusion Enchanting & Runic Matrix
- Multiblock Infusion Altar surrounded by Arcane Pedestals.
- Instability mechanics, symmetry calculations, and runic shielding.

### 🤖 Golemancy & Automation
- Clay, Straw, Wood, Flesh, Iron, Thaumium, and Tallow Golems.
- Animation Cores: Gather, Harvest, Guard, Butcher, Decant, Alchemy, Fill, Empty, Use, Sort, and Fish.
- Golem accessories, equipment upgrades, and Traveling Trunks.

### 👁️ Eldritch Mysteries & Warp
- Permanent, sticky, and temporary Warp triggering psychological hallucinations, guardian attacks, and eldritch blights.
- Eldritch Obelisks, Eldritch Altars, Eldritch Portals, Crimson Cultists, Crimson Praetors, and Eldritch Guardians.

### 🌲 Thaumic World Generation
- Towering Greatwood and Silverwood trees with naturally embedded pure nodes.
- Cinderpearls, Shimmerleaf, Vishrooms, Mana Pods, Infused Stone, Cinnabar, and Amber deposits.
- Magical Forest biomes and localized inland Tainted Land blights.
- TC4's world regeneration: switch on what to regenerate, change the regen key, and existing chunks get it as they load.

### 🌐 Multiplayer
- Works on dedicated servers. The server's research difficulty, aspect cap, hard nodes and wuss mode apply to everyone on it, without
  touching each player's own config.
- `/neothaum` (or `/nt`): research, aspects, warp, and `config` for server settings (`hardnodes`, `wussmode`, `researchdifficulty`,
  `aspectcap`).

---

## 🔌 Mod Compatibility

NeoThaum natively integrates with the modern Fabric modding ecosystem:

| Mod | Compatibility Highlights |
| :--- | :--- |
| **[Trinkets Updated](https://modrinth.com/mod/trinkets-updated)** *(required)* | Amulets, rings and belts go in its necklace, ring and belt slots. |
| **[Roughly Enough Items (REI)](https://modrinth.com/mod/rei)** | The same recipe pages as JEI: arcane worktable, crucible, infusion, infusion enchanting and aspect combinations. |
| **[Sodium](https://modrinth.com/mod/sodium)** & **[Iris](https://modrinth.com/mod/iris)** | NeoThaum's glowing effects work under shader packs (checked with Complementary Reimagined and BSL); bright nodes show a lens flare. Glowing blocks ship LabPBR emission maps. |
| **[LambDynamicLights](https://modrinth.com/mod/lambdynamiclights)** | Wisps light up their surroundings as they fly. |
| **[AppleSkin](https://modrinth.com/mod/appleskin)** | Shows NeoThaum's food values. |
| **[Terralith](https://modrinth.com/mod/terralith)** & **[Biomes O' Plenty](https://modrinth.com/mod/biomes-o-plenty)** | Magical Forest and Tainted Land generate alongside their biomes, and their biomes give aura nodes aspects. |
| **[Just Enough Items (JEI)](https://modrinth.com/mod/jei)** | Native recipe categories for **Arcane Worktable Crafting**, **Crucible Alchemy**, and **Infusion Altar Rituals**. |
| **[Jade](https://modrinth.com/mod/jade)** | In-world HUD overlays showing **Aura Node aspect breakdowns** (when wearing Goggles of Revealing), **Crucible heat & dissolved essentia**, and **Jar fill levels**. |
| **[ModMenu](https://modrinth.com/mod/modmenu)** | The in-game configuration page (see below). |
| **[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map)** | Dedicated toggleable button on the fullscreen world map to view discovered Aura Nodes across the globe without cluttering the main HUD. |
| **[Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)** | Smooth radar integration with full support for tainted entities and champion mobs. |

---

## 📥 Installation Guide

### Option A: Modrinth Modpack (Recommended / One-Click)
The fastest way to play with all companion mods and optimal settings configured out of the box:

1. Download the latest **`NeoThaum-x.x.x.mrpack`** from the [Releases](https://github.com/iceplopper/neothaum/releases) page.
2. Open your launcher of choice (**Prism Launcher**, **Modrinth App**, or **ATLauncher**).
3. Select **Add Instance** $\rightarrow$ **Import from zip / mrpack** $\rightarrow$ choose the downloaded `.mrpack`.
4. Launch and enjoy! The pack includes Fabric API, Trinkets Updated, ModMenu, JEI, Jade, Xaero's Minimap and World Map, Sodium, Iris,
   LambDynamicLights, AppleSkin and Terralith. Iris comes without a shader pack: add your own.

### Option B: Standalone Fabric Mod JAR
If adding NeoThaum to an existing modpack:

1. Install [Fabric Loader](https://fabricmc.net/) (v0.20.1 or higher) for **Minecraft 26.3**.
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api) and **[Trinkets Updated](https://modrinth.com/mod/trinkets-updated)**
   (required, 4.2.1 or later) into your `.minecraft/mods` directory.
3. Download the latest **`neothaum-x.x.x.jar`** from the [Releases](https://github.com/iceplopper/neothaum/releases) page and place it in
   `.minecraft/mods`.
4. *(Optional but highly recommended)* Add **JEI**, **Jade**, **ModMenu**, and **Xaero's World Map** for the full experience.

---

## ⚙️ In-Game Configuration

NeoThaum's configuration is saved in `config/neothaum.json` and editable in-game via **ModMenu**. Every setting explains itself when you
hover over it, and "(TC4)" marks Thaumcraft 4's own value.

- **TC4's options, with TC4's defaults and units:** research difficulty (easy, normal, hard), the aspect cap, world generation and
  regeneration switches, monster spawning switches, golem delays, link quality and chest opening, runic shielding speed, delay and cost,
  colour blind mode, always showing aspects, the wand dial at the bottom, notification speed and count, the portable hole blacklist, hard
  mode nodes, wuss mode and champion mobs.
- **NeoThaum's own tuning:** node, structure and biome frequency multipliers, taint spread, flux, the Infernal Furnace, aspect orb drops,
  bright node light and lens flare, nodes on Xaero's World Map, and the developer creative tab.

### NeoThaum Additions
Features that are **not in Thaumcraft 4**, on their own tab. A master switch, **off by default**, turns them on; each also has its own
switch.

- **Shared Understanding:** a research that lets you teach another player your research, for warp.
- **Essentia overlay:** a thaumium fortress helm fitted with goggles shows an essentia tube's contents, suction, open sides and filter.
- **Warp readout:** your warp beside the hotbar.
- **Golem marker copy:** copy one golemancer's bell's markers to another bell and its golem.
- **Modded item aspects:** items from other mods with no aspects take them from their common tags (ores, ingots, gems, logs, food...).

---

## 🛠️ Building From Source

To compile NeoThaum yourself:

```bash
# Clone the repository
git clone https://github.com/iceplopper/neothaum.git
cd neothaum

# Compile server and client sources (requires JDK 25)
./gradlew compileJava compileClientJava

# Package the runnable mod jar
./gradlew build -x test

# Launch a test client
./gradlew runClient
```

The compiled mod JAR will be located in `build/libs/neothaum-<version>.jar`.

---

## 📜 Credits & License
- **Original Thaumcraft 4**: Created by **Azanor**. All original concept designs, textures, lore, and audio remain the intellectual property of their original creator.
- **NeoThaum**: Modern Fabric adaptation and engineering.

---

### ⚠️ Disclaimer
This project was developed with the assistance of artificial intelligence (AI). NeoThaum does not utilize or incorporate any source code from the original Thaumcraft 4 mod. All systems and mechanics have been engineered cleanly from scratch for modern Minecraft, and all accompanying assets have been recreated with AI assistance to produce this mod.


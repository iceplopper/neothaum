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

---

## 🔌 Mod Compatibility

NeoThaum natively integrates with the modern Fabric modding ecosystem:

| Mod | Compatibility Highlights |
| :--- | :--- |
| **[Just Enough Items (JEI)](https://modrinth.com/mod/jei)** | Native recipe categories for **Arcane Worktable Crafting**, **Crucible Alchemy**, and **Infusion Altar Rituals**. |
| **[Jade](https://modrinth.com/mod/jade)** | In-world HUD overlays showing **Aura Node aspect breakdowns** (when wearing Goggles of Revealing), **Crucible heat & dissolved essentia**, and **Jar fill levels**. |
| **[ModMenu](https://modrinth.com/mod/modmenu)** | In-game configuration screen allowing live tuning of worldgen rates, node variants, taint spread, and gameplay multipliers. |
| **[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map)** | Dedicated toggleable button on the fullscreen world map to view discovered Aura Nodes across the globe without cluttering the main HUD. |
| **[Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)** | Smooth radar integration with full support for tainted entities and champion mobs. |

---

## 📥 Installation Guide

### Option A: Modrinth Modpack (Recommended / One-Click)
The fastest way to play with all companion mods and optimal settings configured out of the box:

1. Download the latest **`NeoThaum-x.x.x.mrpack`** from the [`versions/`](versions/) folder.
2. Open your launcher of choice (**Prism Launcher**, **Modrinth App**, or **ATLauncher**).
3. Select **Add Instance** $\rightarrow$ **Import from zip / mrpack** $\rightarrow$ choose the downloaded `.mrpack`.
4. Launch and enjoy!

### Option B: Standalone Fabric Mod JAR
If adding NeoThaum to an existing modpack:

1. Install [Fabric Loader](https://fabricmc.net/) (v0.20.1 or higher) for **Minecraft 26.3**.
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api) into your `.minecraft/mods` directory.
3. Download the latest **`neothaum-x.x.x.jar`** from the [`versions/`](versions/) directory and place it in `.minecraft/mods`.
4. *(Optional but highly recommended)* Add **JEI**, **Jade**, **ModMenu**, and **Xaero's World Map** for the full experience.

---

## ⚙️ In-Game Configuration

NeoThaum features an extensive configuration system saved in `config/neothaum.json`, fully editable in-game via **ModMenu**:

- **Aura Nodes**: Global spawn rarity (1 in $N$ chunks), variant occurrence rates, and individual weights for Dark, Unstable, Pure, Hungry, and Tainted nodes.
- **Biomes**: Generation toggles and regional percentage sizes for Magical Forests and Tainted Lands.
- **World Structures**: Independent spawn frequency sliders for Eldritch Rings, Barrow Mounds, Hilltop Stones, Wizard Towers, and Banker Houses.
- **Taint Mechanics**: Taint spread master toggle, spread rate intervals, and mob corruption mode (transforming dying animals vs. natural spawn-only).
- **Magic & Alchemy**: Spilled flux multiplier, Infernal Furnace smelting speed, and bonus nugget yield frequency.
- **Creative & Debug Tools**: Toggle visibility of the developer utility creative tab.

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

# Launch an interactive desktop test client
./start-test-client.bat
```

The compiled mod JAR will be located in `build/libs/neothaum-<version>.jar`.

---

## 📜 Credits & License
- **Original Thaumcraft 4**: Created by **Azanor**. All original concept designs, textures, lore, and audio remain the intellectual property of their original creator.
- **NeoThaum**: Modern Fabric adaptation and engineering.

---

### ⚠️ Disclaimer
This project was developed with the assistance of artificial intelligence (AI). NeoThaum does not utilize or incorporate any source code from the original Thaumcraft 4 mod. All systems and mechanics have been engineered cleanly from scratch for modern Minecraft, and all accompanying assets have been recreated with AI assistance to produce this mod.


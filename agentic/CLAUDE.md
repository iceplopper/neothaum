# Claude Code Project Guide - NeoThaum 4

> **For Claude Opus 5.5 (running in Claude Code CLI):**  
> This file is the primary context and operating guide for collaborating on the NeoThaum 4 codebase.
>
> ⚠️ **STRICT REPOSITORY POLICY:**
> **ANYTHING THAT IS NOT PERTINENT TO THE MOD ITSELF MUST GO INTO THIS gentic/ FOLDER.**
> Do NOT create loose markdown notes, plans, scratchpad files, diagnostic logs, or transcripts in the root directory. Keep all non-mod files inside gentic/.

---

## 1. Project Overview & Rules
* **Project:** Faithful 1:1 recreation of Thaumcraft 4.2.3.5 for Minecraft 1.21.4 / 26.3 on Fabric Loom.
* **Goal:** Visually, mechanically, and audibly indistinguishable from original TC4.
* **Original 1.7.10 Reference Source:** `original/decompiled/thaumcraft/` and assets in `original/decompiled/assets/thaumcraft/`.
* **Tools & Reference Mappings:** `original/tools/idmap.py` has the official ID mappings across all items/blocks.

### Critical User Rules
1. **Checkpoint After Each Task:** Stop after completing each assigned task or feature. Provide an itemized summary of everything accomplished and implemented, verify clean builds, and wait for the user before proceeding. Do NOT chain multiple large tasks unattended.
2. **Creative Tab Categories:** Every new item or block MUST be placed into one of the 5 custom creative tabs in `TVTab.java`:
   - `ESSENTIA`: phials, ethereal essences, shards.
   - `TOOLS` (Tools & Utilities): tools, weapons, armor, wands/parts, foci, Thaumonomicon, baubles, equipment, thaumometer, goggles, scribing tools, spawn eggs.
   - `NATURAL` (Natural Blocks): ores, crystal clusters, logs, leaves, saplings, taint blocks, plants.
   - `BUILDING` (Building & Functional Blocks): furniture, tables, planks, arcane stone, essentia distillation blocks (furnace, alembic, jars, tubes, bellows), candles.
   - `OTHER`: raw materials, food, dev tools.
3. **Client Launching:** Never use `--quickPlaySingleplayer`; launch to the main menu using `start-test-client.bat` or `./gradlew runClient`. Unprompted launches are suspended—only launch when the user explicitly requests it.
4. **Addons:** Non-TC4 features and modern QoL extensions go in `dev.neothaum.addon`.
5. **Thaumonomicon as Source of Truth:** The Thaumonomicon serves as the visible in-game source of truth for what features have been implemented so far. Do NOT create custom non-TC4 pages; ensure original TC4 research entries and their recipe pages are activated as soon as their underlying feature/item is added.

---

## 2. Essential Commands

All commands run in PowerShell in the repository root (`C:\Users\harri\Documents\Phoenix Code\neothaum-4-gemini`):

```powershell
# Fast compilation check (Server + Client):
./gradlew compileJava compileClientJava

# Full build without tests (produces runnable mod jar):
./gradlew build -x test

# Run game tests:
./gradlew runClientGameTest

# Launch test client (only when requested by user):
./gradlew runClient
```

---

## 3. Directory Layout & Architecture
- **Root:** `C:\Users\harri\Documents\Phoenix Code\neothaum-4-gemini`
- **Main Server / Common Code:** `src/main/java/dev/neothaum/`
  - `api/`: Aspect, node, wand, and golem API interfaces.
  - `addon/`: Addon modules (`VanillaCandles`, QoL and mini-map integrations).
  - `aspects/`: Aspect definitions, combinations, and item registration mappings.
  - `blocks/`: Block classes (machinery, multiblock parts, world blocks).
  - `config/`: `NeoThaumConfig` reading/writing `config/neothaum.json`.
  - `entity/`: Entities (`AspectOrbEntity`, cultists, eldritch bosses, wisps, golems).
  - `events/`: Event handlers (`AspectOrbEvents`, death callbacks).
  - `golem/`: Golemancy system, cores, upgrades, AI goals, screens, and renderer.
  - `items/`: Tools, armor, baubles, foci, wands, consumables.
  - `network/`: Packets and synchronization (`TVNetwork`).
  - `registry/`: Central registries (`TVBlocks`, `TVItems`, `TVEntities`, `TVBlockEntities`, `TVSounds`, `TVMobEffects`, `TVTab`).
  - `research/`: `ResearchManager`, `ScanManager`, player knowledge attachment.
  - `tiles/`: Block entities (`NodeBlockEntity`, `CrucibleBlockEntity`, `InfusionMatrixBlockEntity`, etc.).
  - `warp/`: Warp system, hallucination events, corruption ticking.
  - `world/`: Biomes, features, worldgen decorators (`TCWorldFeature`, `TVBiomes`).
- **Client Code:** `src/client/java/dev/neothaum/client/`
  - `entity/`: Mob and orb renderers (`AspectOrbRenderer`, `CultistRenderer`, `EldritchWardenRenderer`, etc.).
  - `gui/`: Thaumonomicon (`ResearchScreen`, `ResearchPagesScreen`), focal manipulator, golem GUI.
  - `hud/`: Goggles overlay, runic shield hud, focus radial menu (`FocusRadial`), sanity checker gauge.
  - `render/`: Multiblock BERs, `ArchitectOverlay` (warded glass preview), `NodeRenderer`.
- **Resources:**
  - `src/main/resources/assets/neothaum/`: textures, models, blockstates, lang files (`en_us.json`).
  - `src/main/resources/data/neothaum/`: recipes, research (`research.json`), advancements (`advancement/`).

---

## 4. Most Recent Progress (Features 10, 12, 13 Complete)
- **Feature 10 (Eldritch Overworld Structures, Altars, Obelisks, Portals & Cultist Portal Boss) (Commit `81c2d30`):**
  - Eldritch Blocks: Obsidian Tile, Eldritch Stone (smooth, cracked, glyph variants), Eldritch Altar, Eldritch Capstone, Eldritch Obelisk, and Eldritch Portal.
  - Eldritch Altar Ritual: Accepts 1 to 4 Eldritch Eyes. Summons cultist/guardian defenders when armed. 100-primal vis wand strike triggers lightning, opens Eldritch Portal above.
  - Eldritch Obelisk: Monolith shaft buffing eldritch mobs with Strength and Resistance within 6 blocks.
  - Eldritch Portal: Swirling dark dimensional tear with animated vortex BER, ambient audio loop, granting `ENTEROUTER` research upon contact.
  - Cultist Portal Boss: 500 HP stationary boss mob with boss bar, proximity zap defenses, 12 summoning waves (Knights, Clerics, cardinal Crimson Banners), summoning the Crimson Praetor, and dropping Primordial Pearl and Crimson Rites upon defeat.
  - Overworld Generation: `EldritchRingFeature` hooked into `TCWorldFeature` (1/66 chunk chance).
- **Feature 12 (Remaining Overworld Structures & Villages) (Commit `4a4d172`):**
  - `BarrowMoundFeature`: 18x18 subterranean vault, Zombie/Skeleton spawners, 2 dungeon chests, hovering Sinister aura node.
  - `HilltopStonesFeature`: High altitude (`y >= 85`) 7x7 Arcane Stone circle, trapped chest, Wisp spawner, hovering aura node.
  - `WizardTowerFeature`: 5x5 three-story stone brick tower, entrance door, ladder shaft, bookshelves, arcane worktable, wizard chest.
  - `BankerHomeFeature`: Village banker house with banking counter, chest, and furnishings.
- **Feature 13 (Champion Mobs System & Ethereal Bloom) (Commit `48f4d0a`):**
  - `EtherealBloomBlock` & `EtherealBloomBlockEntity`: Light level 12 flower cleanses 8-block radius: converts Tainted Land & Eerie biomes to Plains, purifies tainted soil, destroys crusted taint & taint fibres, dissipates flux fluids. Crucible recipe unlocked by `ETHEREALBLOOM` research.
  - `ChampionEvents` & `ChampionModifier`: 13 TC4 modifiers (Bold, Spined, Armored, Mighty, Grim, Warded, Warp, Undying, Fiery, Sickly, Venomous, Vampiric, Infested). 2.5% spawn rate on hostile mobs, +30 Max HP, colored custom nameplate, combat triggers, undying revival, guaranteed loot bag drop + 2-4 aspect orbs + extra XP.
- **Next Task:** Feature 11 (The Outer Lands Dimension).

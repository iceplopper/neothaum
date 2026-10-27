# Claude Code Project Guide - Thaumvibe 4

> **For Claude Opus 5.5 (running in Claude Code CLI):**  
> This file is the primary context and operating guide for collaborating on the Thaumvibe 4 codebase.

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
4. **Addons:** Non-TC4 features and modern QoL extensions go in `dev.thaumvibe.addon`.
5. **Thaumonomicon as Source of Truth:** The Thaumonomicon serves as the visible in-game source of truth for what features have been implemented so far. Do NOT create custom non-TC4 pages; ensure original TC4 research entries and their recipe pages are activated as soon as their underlying feature/item is added.

---

## 2. Essential Commands

All commands run in PowerShell in the repository root (`C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini`):

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
- **Root:** `C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini`
- **Main Server / Common Code:** `src/main/java/dev/thaumvibe/`
  - `api/`: Aspect, node, wand, and golem API interfaces.
  - `addon/`: Addon modules (`VanillaCandles`, QoL and mini-map integrations).
  - `aspects/`: Aspect definitions, combinations, and item registration mappings.
  - `blocks/`: Block classes (machinery, multiblock parts, world blocks).
  - `config/`: `ThaumvibeConfig` reading/writing `config/thaumvibe.json`.
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
- **Client Code:** `src/client/java/dev/thaumvibe/client/`
  - `entity/`: Mob and orb renderers (`AspectOrbRenderer`, `CultistRenderer`, `EldritchWardenRenderer`, etc.).
  - `gui/`: Thaumonomicon (`ResearchScreen`, `ResearchPagesScreen`), focal manipulator, golem GUI.
  - `hud/`: Goggles overlay, runic shield hud, focus radial menu (`FocusRadial`), sanity checker gauge.
  - `render/`: Multiblock BERs, `ArchitectOverlay` (warded glass preview), `NodeRenderer`.
- **Resources:**
  - `src/main/resources/assets/thaumvibe/`: textures, models, blockstates, lang files (`en_us.json`).
  - `src/main/resources/data/thaumvibe/`: recipes, research (`research.json`), advancements (`advancement/`).

---

## 4. Most Recent Progress (Feature 14 Complete)
- **Aspect Orbs:** `AspectOrbEntity` (gravity, wand vis filling, research knowledge pool filling), `AspectOrbRenderer` (aspect-tinted billboard glow), `AspectOrbEvents` (dropped on mob kills by players).
- **Node Lightning Strikes:** Uncontained unstable, hungry, and sinister aura nodes periodically discharge `TVNetwork.sendZap` lightning bolts at nearby living entities dealing magic damage.
- **Shovel of the Earthmover Polish:**
  - `ArchitectOverlay` hooked for `ElementalShovelItem` to render the authentic shifting warded glass 3x3 placement preview.
  - Misc key (`G`) toggles placement orientation (Parallel, Vertical, Horizontal) with action bar notifications and click audio.
- **Config System:** `ThaumvibeConfig` reads and writes `config/thaumvibe.json` (controls taint spread, node rarity, mob spawn chances, aspect orb drop chance, warp toggles).
- **Advancement Tree:** Authentic TC4 advancements in `data/thaumvibe/advancement/` (`root`, `goggles`, `crucible`, `infusion`, `golem`, `warp`, `eldritch`) with full localizations.
- **Full Verification:** Zero compiler errors, clean Gradle build.

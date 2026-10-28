# NeoThaum 4 — Handoff Briefing for Claude Opus

> **Date:** October 27, 2026  
> **Repository:** `C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini`  
> **Current Git Head:** `d099a66` (`fix(client): lazy-init eyeStack in EldritchAltarRenderer, add Jade config keys, and harden client particles`)  
> **Status:** Compiles cleanly with 0 errors (`./gradlew compileJava compileClientJava` passes in 6s).

---

## ⚠️ STRICT OPERATING RULES FOR CLAUDE

1. **The `agentic/` Directory Rule:**
   - **ANYTHING THAT IS NOT PERTINENT TO THE MOD ITSELF MUST GO INTO THE `agentic/` FOLDER.**
   - Do NOT create loose markdown notes, plans, scratchpad files, transcripts, or test logs in the root repository.
   - Update `agentic/TODO.md` as items are completed.
2. **Checkpoint After Each Task:**
   - Stop after completing each task or logical group. Summarize your changes, verify compilation, and ask the user before moving to the next.
3. **GitHub Remote & Continuous Sync:**
   - The remote \origin\ is configured to \https://github.com/iceplopper/neothaum.git\.
   - After completing each task/checkpoint and committing, push your branch (\git push origin <branch>\) to keep GitHub in sync.
4. **Creative Tab Placement:**
   - Every item/block MUST be registered in one of `TVTab.java`'s 5 creative tabs: `ESSENTIA`, `TOOLS`, `NATURAL`, `BUILDING`, `OTHER`.
4. **Client Launching:**
   - Never use `--quickPlaySingleplayer`.
   - The user runs `start-test-client.bat` or `./gradlew runClient` on their Windows desktop with their RTX GPU. Do not launch unprompted background clients that linger.

---

## 📌 Context: What Was Just Completed

1. **Global Rebrand to NeoThaum (`b4c5b16`):**
   - Mod ID changed from `thaumvibe` to `neothaum`.
   - Packages renamed from `dev.thaumvibe.*` to `dev.neothaum.*`.
   - Resource namespaces renamed to `assets/neothaum/` and `data/neothaum/`.
   - Build outputs: `archivesName = 'neothaum'`, root project `'neothaum'`, group `dev.neothaum`.
2. **Baubles Inventory Integration Polish (`fe86d69`):**
   - Added 4 shadowed slot silhouette icons rendered at `0x60FFFFFF` (`bauble_amulet.png`, `bauble_ring.png`, `bauble_belt.png`).
   - Cleaned container title to `"container.baubles": "Baubles"`.
   - 3D-beveled inventory button matching vanilla inventory frames.
3. **Client Startup Crash Resolution (`d099a66`):**
   - `EldritchAltarRenderer`: Converted `eyeStack` to lazy initialization so it does not instantiate `new ItemStack(...)` before Minecraft item components are bound during client resource reload.
   - Jade Config Keys: Added `config.jade.plugin_neothaum.crucible`, `jar`, `aura_node`, and `runic_matrix` to `en_us.json`.
4. **Editable Configurations & ModMenu Integration (v1.0.1):**
   - Implemented full in-game configuration GUI (`NeoThaumConfigScreen.java`) integrated via `ModMenuApi` (`NeoThaumModMenu.java`).
   - Added all requested settings in `NeoThaumConfig.java` reading/writing `config/neothaum.json`:
     - Node generation rarity, variant rarity, and individual node type/modifier weights.
     - Biome generation toggles and size percentage slices for Magical Forest and Tainted Land.
     - Individual generation rarities for Eldritch Ring, Barrow Mound, Hilltop Stones, Wizard Tower, and Banker House.
     - Taint spread toggles and tick rate.
     - `corruptExistingMobs` toggle (converting dying mobs vs. spawn-only).
     - `fluxMultiplier` scaling spilled flux from crucibles/furnaces.
     - Infernal furnace speed multiplier and bonus item yield multiplier.
     - `showCreativeDebugTab` toggle hiding the dev tools creative tab when disabled.
   - Recompiled as `neothaum-1.0.1.jar` and packaged into `NeoThaum-1.0.1.mrpack` with `modmenu-21.0.0.jar`.

---

## 🎯 Master Task Breakdown & Technical Solutions

The master backlog is cataloged in [`agentic/TODO.md`](file:///C:/Users/harri/Documents/Phoenix%20Code/thaumvibe-4-gemini/agentic/TODO.md). Here is the technical context for each item:

### 1. Golem Animation Core Icons
- **Symptom:** In the creative tab, each animation core displays 3 vertical frames squished into one slot.
- **Cause:** The PNG is a 16x48 texture containing 3 animation frames.
- **Fix:** Add a `.png.mcmeta` file for each core texture in `assets/neothaum/textures/item/` specifying `{"animation": {"frametime": 8, "interpolate": true}}`.

### 2. Spawn Eggs Modern Design Overhaul
- **Missing Textures:** Crimson Praetor, Cultist Portal, Eldritch Warden, Eldritch Golem.
- **Basic Designs to Modernize:** Mind Spider, Eldritch Guardian, Crimson Knight, Crimson Cleric, Eldritch Crab, Shambling Husk.
- **Requirement:** Modern Minecraft spawn egg styling with distinct primary and secondary egg shell/speckle colors resembling the actual entity palette, using `item/template_spawn_egg` or custom high-fidelity textures.

### 3. Banner Inventory Icons
- **Symptom:** Banners show missing purple/black textures in the inventory.
- **Fix:** Ensure item models in `assets/neothaum/models/item/banner_*.json` point to valid 2D item textures or hook into the custom banner renderer.

### 4. Machine & Device Inventory Models
- **Symptom:** Arcane Spa, Advanced Alchemical Construct, Deconstruction Table, Arcane Bore Base, Arcane Bore, Flux Scrubber render fine in the world, but show missing textures in the inventory.
- **Fix:** Update their item model JSONs in `assets/neothaum/models/item/` to correctly reference parent block models or dedicated item models.

### 5. Eldritch Altar & Eldritch Capstone Textures
- **Symptom:** Broken geometry in-world; faces show grass/stone textures or invalid UV bounds.
- **Fix:** Check `assets/neothaum/models/block/eldritch_altar.json` and `eldritch_capstone.json` and fix texture variable mappings (`#all`, `#texture`) and UV coordinates.

### 6. Ethereal Bloom
- **Inventory:** Fix missing texture in `assets/neothaum/models/item/ethereal_bloom.json`.
- **World:** In TC4, the bloom sways and has an animated growing/breathing cycle. Recreate this animation.

### 7. Primordial Pearl, Crimson Rites, Eldritch Eye, Runed Tablet
- **Fix:** Verify texture file paths in `assets/neothaum/textures/item/` and ensure item model JSONs point to the correct files.

### 8. Copper Golem Polish
- **Inventory Icon:** Create a 16x16 icon in the style of existing clay/straw/flesh golem items.
- **Creative Tab:** Move to the proper golem/tools creative tab.
- **Model UV Mapping:** Fix distorted UV coordinates in `CopperGolemModel.java`.

### 9. Tooltip Text Accuracy
- Audit item descriptions in `assets/neothaum/lang/en_us.json` (especially golem accessories) against original TC4 `en_US.lang` in `original/decompiled/assets/thaumcraft/lang/en_US.lang`.

### 10. Thaumonomicon Golemancy Tab
- Populate `assets/neothaum/thaumonomicon/category_golemancy.json` with all original TC4 research nodes: Basic Golemancy, Straw/Wood/Clay/Flesh/Iron/Thaumium/Tallow/Stone Golems, Animation Cores (Gather, Harvest, Guard, Butcher, Decant, Fill, Empty, Use, Sort), Golem Accessories, Traveling Trunk, Biothaumic Mind.

### 11. Xaero's Map Aura Node Viewing
- **Strict User Requirement:**
  - Nodes MUST ONLY appear on the **fullscreen Xaero's WorldMap**.
  - Must be toggleable via a dedicated button in the fullscreen map UI.
  - Nodes MUST NOT appear on the HUD minimap.
  - Nodes MUST NOT appear as in-world waypoints in 1st/3rd person view.
- **Files:** Check `dev.neothaum.addon.xaero.*` and `dev.neothaum.client.hud.NodeRenderer`.

### 12. Pech Forager Trading
- Fix befriending logic (throwing valuables on the ground) and right-click interaction to open the trading/barter interface in `dev.neothaum.entity.monster.Pech.java`.

### 13. Thaumic Slime Sounds
- Override `getHurtSound`, `getDeathSound`, `getSquishSound`, and `getJumpSound` in `ThaumicSlime.java` to return `SoundEvents.SLIME_*` sounds.

### 14. Worldgen Frequency & Village Wizard House
- Increase spacing/separation in `data/neothaum/worldgen/structure_set/*.json`.
- Move the Thaumcraft Village House from standalone placement into vanilla village Jigsaw template pools so it only generates within actual villages.

### 15. Mob Texture Mappings & Gaps
- Affected mobs: Eldritch Guardian, Crimson Knight, Crimson Cleric, Crimson Praetor, Shambling Husk, Eldritch Warden, Eldritch Golem.
- Fix UV definitions in each entity model's `createBodyLayer()` to match the original TC4 texture maps in `original/decompiled/assets/thaumcraft/textures/models/`.

### 16. Eldritch Obelisk Custom Model & Floating Capstone
- **Symptom:** Eldritch Obelisk currently uses an incorrect basic block model and textures.
- **TC4 Design:** A 4-block multiblock pillar topped by a floating, bobbing runic capstone suspended over a swirling dark particle/void vortex.
- **Reference:** `original/decompiled/thaumcraft/client/renderers/tile/TileEldritchObeliskRenderer.java`, `textures/models/obelisk_cap.obj`, `obelisk_cap.png`, `obelisk_side.png`, `tunnel.png`.

### 17. Land Validation for Structure Spawns (Open Water Fix)
- **Symptom:** Structures generate floating in open water (oceans, lakes, and rivers).
- **Cause:** `heightAt()` in `TCWorldFeature.java` uses `MOTION_BLOCKING_NO_LEAVES`, stopping on water fluid surfaces.
- **Fix:** Validate that the target position and surrounding footprint are solid blocks and not water/fluids (`!world.getFluidState(pos).is(FluidTags.WATER) && world.getBlockState(pos).isSolid()`).

### 18. Wizard Tower Bottom Floor Doorway Clearance
- **Symptom:** The bottom floor blocks the door with wooden planks.
- **Cause:** `WizardTowerFeature.java` line 58 places oak planks across the interior at `y = 1` (`cy + 1`), which is the exact height of the bottom half of the door.
- **Fix:** In TC4 (`ComponentWizardTower.java` line 59), foundation planks are placed at `y = 0` (`cy`), and `y = 1` to `y = 4` is open air. Place the ground planks at `y = 0`, keeping `y = 1` clear.

### 19. Authentic Randomized TC4 Structure Loot Tables
- **Symptom:** Loot in structures is hardcoded with identical item layouts across all spawns.
- **Fix:** Implement dedicated loot tables in `data/neothaum/loot_table/chests/` for `wizard_tower.json`, `barrow_mound.json`, `banker_house.json`, and `eldritch_ring.json` based on original TC4 `ChestGenHooks` pools.

### 20. Staves Architecture & Implementation
- **Original TC4 Mechanics Explained:**
  - Staves in TC4 are not just craftable sticks; they are heavy combat/casting variants of the wand.
  - **Crafting:** In the Arcane Worktable, craft a **Staff Core** using 2 wand rods of the same type + primal charms/inert cores. Combine the Staff Core with 2 wand caps.
  - **Capacity:** Staves hold **2.5× the base rod capacity** (e.g. Greatwood Staff = 125 vis, Silverwood Staff = 250 vis, Primal Staff = 250 vis).
  - **Combat Weapon:** Staves possess significant melee attack damage and knockback attributes, functioning as a high-tier melee weapon.
  - **Worktable Restriction:** Staves CANNOT be placed into the Arcane Worktable's wand slot (they are too large and unwieldy for bench craft; strictly for foci casting and combat).
- **Implementation Strategy:** Ensure each staff core has a corresponding craftable staff item or wand variant with the 2.5× capacity multiplier, weapon attack damage, and worktable slot exclusion.

---

## 🛠️ Verification Commands

```powershell
# 1. Fast incremental compilation check
./gradlew compileJava compileClientJava

# 2. Full jar packaging
./gradlew build -x test

# 3. Launch test client (interactive desktop test)
./start-test-client.bat
```

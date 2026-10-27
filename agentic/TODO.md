# NeoThaum 4 — Master Priority TODO & Defect Backlog

> **Policy Reminder:** Keep this file updated inside `agentic/`. Complete tasks one by one, verify `./gradlew compileJava compileClientJava` passes, and stop for user review after each milestone.

---

## 🎨 1. Textures, Item Models & Inventory Icons

- [ ] **1.1 Golem Animation Core Textures & Animations**
  - **Issue:** In the creative inventory, golem animation core icons render distorted (showing all 3 frames squished vertically into a single 16x16 slot).
  - **Root Cause:** The textures in `assets/neothaum/textures/item/` are 16x48 strips (3 stacked frames). In Minecraft 1.21.4+, animated item textures require an accompanying `<texture>.png.mcmeta` file:
    ```json
    {
      "animation": {
        "frametime": 8,
        "interpolate": true
      }
    }
    ```
  - **Files to check:** `core_*.png` and `core_*.png.mcmeta` in `src/main/resources/assets/neothaum/textures/item/`.

- [ ] **1.2 Spawn Eggs Modern Design Overhaul**
  - **Missing textures:**
    - `crimson_praetor_spawn_egg`
    - `cultist_portal_spawn_egg`
    - `eldritch_warden_spawn_egg`
    - `eldritch_golem_spawn_egg`
  - **Needs modern palette redesign:**
    - `mind_spider_spawn_egg`
    - `eldritch_guardian_spawn_egg`
    - `crimson_knight_spawn_egg`
    - `crimson_cleric_spawn_egg`
    - `eldritch_crab_spawn_egg`
    - `shambling_husk_spawn_egg`
  - **Requirement:** Modern Minecraft spawn egg styling with distinct primary and secondary egg shell/speckle colors resembling the actual entity palette, using `item/template_spawn_egg` or custom high-fidelity textures.
  - **Files to check:** `TVItems.java`, `assets/neothaum/models/item/*_spawn_egg.json`.

- [ ] **1.3 Banner Inventory Icons**
  - **Issue:** Banner items in inventory display missing purple/black textures instead of the banner model.
  - **Root Cause:** Item models in `assets/neothaum/models/item/` for TC banners point to non-existent 2D textures or fail to inherit from banner renderers.
  - **Files to check:** `assets/neothaum/models/item/banner_*.json` and `BannerRenderer.java`.

- [ ] **1.4 Machine & Device Inventory Models**
  - **Issue:** The in-world placed blocks render fine, but the inventory icons show missing textures for:
    - Arcane Spa (`arcane_spa`)
    - Advanced Alchemical Construct (`advanced_alchemy_furnace` / `adv_alchemical_construct`)
    - Deconstruction Table (`deconstruction_table`)
    - Arcane Bore Base (`arcane_bore_base`)
    - Arcane Bore (`arcane_bore`)
    - Flux Scrubber (`flux_scrubber`)
  - **Files to check:** `assets/neothaum/items/` or `assets/neothaum/models/item/` and ensure item models point to valid block models or dedicated item models.

- [ ] **1.5 Artifacts & Curios Missing Textures**
  - **Items missing textures in inventory:**
    - Primordial Pearl (`primordial_pearl`)
    - Crimson Rites (`crimson_rites`)
    - Eldritch Eye (`eldritch_eye`)
    - Runed Tablet (`runed_tablet`)
  - **Files to check:** Check model JSONs in `assets/neothaum/models/item/` and ensure referenced PNG textures exist in `assets/neothaum/textures/item/`.

- [ ] **1.6 Ethereal Bloom Inventory Icon & World Animation**
  - **Inventory:** Item model missing texture (`assets/neothaum/models/item/ethereal_bloom.json`).
  - **World:** In TC4, the placed Ethereal Bloom has an animated growing/breathing visual cycle. Add animation mcmeta or dynamic model scale logic to match original TC4.

- [ ] **1.7 Copper Golem Polish**
  - **Inventory Icon:** Missing; create 16x16 icon in the style of existing clay/straw/flesh golem items.
  - **Creative Tab:** Currently in the wrong tab; move to `TVTab.TOOLS` or `TVTab.BUILDING` alongside other golem items.
  - **Model UV Mapping:** The entity texture is distorted and misaligned with the model boxes. Fix UV coordinates in `CopperGolemModel.java`.

---

## 🏛️ 2. World Models & Block Renderers

- [ ] **2.1 Eldritch Altar & Eldritch Capstone Model Distortion**
  - **Issue:** When placed in the world, the Eldritch Altar and Capstone have broken geometry with grass or stone mapped to faces.
  - **Root Cause:** `assets/neothaum/models/block/eldritch_altar.json` and `eldritch_capstone.json` have misconfigured texture variables (`#all`, `#texture`) or invalid UV bounds outside 0–16.
  - **Files to check:** `assets/neothaum/models/block/eldritch_altar.json`, `eldritch_capstone.json`, and `EldritchAltarRenderer.java`.

- [ ] **2.2 Eldritch Obelisk Custom Model & Textures**
  - **Issue:** The Eldritch Obelisk does not have the correct textures or model.
  - **Original TC4 Appearance:** A 4-block high multiblock pillar topped with a floating, bobbing runic capstone suspended above an eerie swirling dark void / particle field.
  - **Reference Files:** `original/decompiled/thaumcraft/client/renderers/tile/TileEldritchObeliskRenderer.java`, `textures/models/obelisk_cap.obj`, `obelisk_cap.png`, `obelisk_side.png`, `tunnel.png`.
  - **Files to check:** `EldritchObeliskBlock.java`, `EldritchObeliskBlockEntity.java`, and create/update its dedicated Block Entity Renderer or baked model.

---

## 👾 3. Entity Models, Textures & Audio

- [ ] **3.1 Eldritch & Cultist Mob UV Mapping & Texture Gaps**
  - **Affected Mobs:**
    - Eldritch Guardian
    - Crimson Knight
    - Crimson Cleric
    - Crimson Praetor
    - Shambling Husk
    - Eldritch Warden
    - Eldritch Golem
  - **Issue:** Gaps, shearing, and distorted UV mapping on entity limbs, torsos, and robes.
  - **Action:** Compare entity model box UV coordinates in `createBodyLayer()` against the original TC4 texture maps in `original/decompiled/assets/thaumcraft/textures/models/`.

- [ ] **3.2 Thaumic Slime Sounds**
  - **Issue:** Thaumic Slimes do not make slime noises.
  - **Fix:** In `ThaumicSlime.java`, override `getHurtSound`, `getDeathSound`, `getSquishSound`, and `getJumpSound` to return `SoundEvents.SLIME_*` or custom thaumic slime squish sounds.

- [ ] **3.3 Pech Forager Trading Interaction**
  - **Issue:** Players are unable to trade with the Pech Forager.
  - **TC4 Mechanic:** Pechs are wary by default. Players must throw valuables (gold ingots, gold nuggets, gems) on the ground near them. Once befriended, right-clicking opens the TC4 Pech trading/bartering interface.
  - **Files to check:** `dev.neothaum.entity.monster.Pech.java`, right-click interaction handler, and container/screen registration.

---

## 🗺️ 4. Xaero's Map Node Viewing Overhaul

- [ ] **4.1 Aura Node Map Display Restrictions**
  - **Issue:** Nodes currently appear everywhere (minimap, waypoints in 1st/3rd person).
  - **Strict User Requirement:**
    - Aura nodes MUST ONLY be visible on the **fullscreen Xaero's WorldMap**.
    - Must be controlled by a **toggleable button inside the fullscreen Xaero's map UI**.
    - Nodes MUST NOT appear on the standard HUD minimap.
    - Nodes MUST NOT appear as in-world waypoints in 1st/3rd person view.
  - **Files to check:** `dev.neothaum.addon.xaero.*` and `dev.neothaum.client.hud.NodeRenderer`.

---

## 📖 5. Thaumonomicon & Tooltips

- [ ] **5.1 Golemancy Tab Population in Thaumonomicon**
  - **Issue:** Golemancy category is completely empty except for the Hungry Chest.
  - **Action:** Add all original TC4 research nodes to `assets/neothaum/thaumonomicon/category_golemancy.json`:
    - Basic Golemancy
    - Straw, Wood, Clay, Flesh, Iron, Thaumium, Tallow, Stone Golems
    - Animation Cores (Gather, Harvest, Guard, Butcher, Decant, Fill, Empty, Use, Sort)
    - Golem Accessories (Top Hat, Spectacles, Bow Tie, Fez, Dart Gun, Iron Plating, etc.)
    - Traveling Trunk
    - Biothaumic Mind
  - Ensure all research entry prerequisites and connected recipe pages match TC4.

- [ ] **5.2 Tooltip Accuracy Audit**
  - **Issue:** Golem accessories and several other items display inaccurate or placeholder text.
  - **Action:** Compare item descriptions in `assets/neothaum/lang/en_us.json` against `original/decompiled/assets/thaumcraft/lang/en_US.lang`.

---

## 🌍 6. World Generation & Structures

- [ ] **6.1 Structure Spawn Frequency & Village Placement**
  - **Issues:**
    - NeoThaum structures (mound, pillar, obelisk, circle) spawn too frequently.
    - The Thaumaturge Village House spawns randomly in the wild across the overworld instead of only inside villages.
  - **Fixes:**
    - Increase spacing and separation in `data/neothaum/worldgen/structure_set/*.json`.
    - Move the Thaumcraft Village House into vanilla village Jigsaw template pools (`village/plains/houses`, `village/desert/houses`, etc.) so it exclusively generates as part of a village.

- [ ] **6.2 Solid Land Validation for Structure Spawns**
  - **Issue:** Structures are generating on open water bodies (oceans, lakes, and rivers).
  - **Root Cause:** `TCWorldFeature.java` determines structure height using `world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)`, which stops on water fluid surfaces.
  - **Fix:** In `TCWorldFeature.java`, verify that the placement position is solid ground and NOT water/fluid (`!world.getFluidState(pos).is(FluidTags.WATER) && world.getBlockState(pos).isSolid()`) across the structure footprint before triggering generation.

- [ ] **6.3 Wizard Tower Bottom Floor Doorway Fix**
  - **Issue:** Wizard Tower bottom floor blocks the door with wooden planks.
  - **Root Cause:** In `WizardTowerFeature.java` line 58, the interior floor is generated at `y = 1` (`cy + 1`), placing solid oak planks across the ground level right where the door is placed at `cy + 1`. In original TC4 (`ComponentWizardTower.java` line 59), the floor planks are at `y = 0` (`cy`), and `y = 1` through `y = 4` is open air so the doorway is completely unobstructed.
  - **Fix:** Move the ground floor oak planks down to `y = 0` (or `cy`), leaving `y = 1` clear for the entryway.

- [ ] **6.4 Authentic Randomized TC4 Structure Loot Tables**
  - **Issue:** Loot in all generated structures is identical in items and layout (or using vanilla generic tables).
  - **Fix:** Replace hardcoded `chest.setItem(...)` slots with dedicated JSON loot tables in `data/neothaum/loot_table/chests/` mirroring original TC4 `ChestGenHooks`:
    - `chests/wizard_tower.json`: Paper, Gold Nuggets, Glass Bottles, Potions, Knowledge Fragments, Alumentum, Nitor, Thaumium Ingots, Thaumonomicon.
    - `chests/barrow_mound.json`: Eldritch artifacts, amber, coins, cursed trinkets.
    - `chests/banker_house.json`: Coins, emeralds, arcane gold keys, ledgers.
    - `chests/eldritch_ring.json`: Eldritch Eyes, void seeds, primordial pearl fragments.

---

## 🪄 7. Staves Architecture & Implementation

- [ ] **7.1 Staff Mechanics & Item Representation**
  - **Original TC4 Mechanics:**
    - A Staff is an advanced combat/casting casting instrument crafted using a **Staff Core** (crafted from 2 wand rods + primal charms/inert cores) + 2 Wand Caps in the Arcane Worktable.
    - **Capacity:** Staves hold **2.5× the base rod capacity** (e.g. Greatwood Staff = 125, Silverwood Staff = 250, Primal Staff = 250).
    - **Melee Weapon:** High physical and magical melee damage attributes.
    - **Arcane Crafting Restriction:** CANNOT be placed in the Arcane Worktable wand slot (too large/cumbersome for fine benchcraft; foci casting & combat only).
  - **Action:** Provide distinct staff item definitions or wand variants for each staff core with 2.5× capacity, melee attributes, and worktable restrictions.

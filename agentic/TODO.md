# NeoThaum 4 — Master Priority TODO & Defect Backlog

> **Policy Reminder:** Keep this file updated inside `agentic/`. Complete tasks one by one, verify `./gradlew compileJava compileClientJava` passes, and stop for user review after each milestone.

---

## 🎨 1. Textures, Item Models & Inventory Icons

- [x] **1.1 Golem Animation Core Textures & Animations**
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

- [x] **1.2 Spawn Eggs Modern Design Overhaul**
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

- [x] **1.3 Banner Inventory Icons**
  - **Issue:** Banner items in inventory display missing purple/black textures instead of the banner model.
  - **Root Cause:** Item models in `assets/neothaum/models/item/` for TC banners point to non-existent 2D textures or fail to inherit from banner renderers.
  - **Files to check:** `assets/neothaum/models/item/banner_*.json` and `BannerRenderer.java`.

- [x] **1.4 Machine & Device Inventory Models**
  - **Issue:** The in-world placed blocks render fine, but the inventory icons show missing textures for:
    - Arcane Spa (`arcane_spa`)
    - Advanced Alchemical Construct (`advanced_alchemy_furnace` / `adv_alchemical_construct`)
    - Deconstruction Table (`deconstruction_table`)
    - Arcane Bore Base (`arcane_bore_base`)
    - Arcane Bore (`arcane_bore`)
    - Flux Scrubber (`flux_scrubber`)
  - **Files to check:** `assets/neothaum/items/` or `assets/neothaum/models/item/` and ensure item models point to valid block models or dedicated item models.

- [x] **1.5 Artifacts & Curios Missing Textures**
  - **Items missing textures in inventory:**
    - Primordial Pearl (`primordial_pearl`)
    - Crimson Rites (`crimson_rites`)
    - Eldritch Eye (`eldritch_eye`)
    - Runed Tablet (`runed_tablet`)
  - **Files to check:** Check model JSONs in `assets/neothaum/models/item/` and ensure referenced PNG textures exist in `assets/neothaum/textures/item/`.

- [x] **1.6 Ethereal Bloom Inventory Icon & World Animation**
  - **Inventory:** Item model missing texture (`assets/neothaum/models/item/ethereal_bloom.json`).
  - **World:** In TC4, the placed Ethereal Bloom has an animated growing/breathing visual cycle. Add animation mcmeta or dynamic model scale logic to match original TC4.

- [x] **1.7 Copper Golem Polish**
  - **Inventory Icon:** Missing; create 16x16 icon in the style of existing clay/straw/flesh golem items.
  - **Creative Tab:** Currently in the wrong tab; move to `TVTab.TOOLS` or `TVTab.BUILDING` alongside other golem items.
  - **Model UV Mapping:** The entity texture is distorted and misaligned with the model boxes. Fix UV coordinates in `CopperGolemModel.java`.

---

## 🏛️ 2. World Models & Block Renderers

- [x] **2.1 Eldritch Altar & Eldritch Capstone Model Distortion**
  - **Issue:** When placed in the world, the Eldritch Altar and Capstone have broken geometry with grass or stone mapped to faces.
  - **Root Cause:** `assets/neothaum/models/block/eldritch_altar.json` and `eldritch_capstone.json` have misconfigured texture variables (`#all`, `#texture`) or invalid UV bounds outside 0–16.
  - **Files to check:** `assets/neothaum/models/block/eldritch_altar.json`, `eldritch_capstone.json`, and `EldritchAltarRenderer.java`.

- [x] **2.2 Eldritch Obelisk Custom Model & Textures**
  - **Issue:** The Eldritch Obelisk does not have the correct textures or model.
  - **Original TC4 Appearance:** A 4-block high multiblock pillar topped with a floating, bobbing runic capstone suspended above an eerie swirling dark void / particle field.
  - **Reference Files:** `original/decompiled/thaumcraft/client/renderers/tile/TileEldritchObeliskRenderer.java`, `textures/models/obelisk_cap.obj`, `obelisk_cap.png`, `obelisk_side.png`, `tunnel.png`.
  - **Files to check:** `EldritchObeliskBlock.java`, `EldritchObeliskBlockEntity.java`, and create/update its dedicated Block Entity Renderer or baked model.

---

## 👾 3. Entity Models, Textures & Audio

- [x] **3.1 Eldritch & Cultist Mob UV Mapping & Texture Gaps**
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

- [x] **3.2 Thaumic Slime Sounds**
  - **Issue:** Thaumic Slimes do not make slime noises.
  - **Fix:** In `ThaumicSlime.java`, override `getHurtSound`, `getDeathSound`, `getSquishSound`, and `getJumpSound` to return `SoundEvents.SLIME_*` or custom thaumic slime squish sounds.

- [x] **3.3 Pech Forager Trading Interaction**
  - **Issue:** Players are unable to trade with the Pech Forager.
  - **TC4 Mechanic:** Pechs are wary by default. Players must throw valuables (gold ingots, gold nuggets, gems) on the ground near them. Once befriended, right-clicking opens the TC4 Pech trading/bartering interface.
  - **Files to check:** `dev.neothaum.entity.monster.Pech.java`, right-click interaction handler, and container/screen registration.

---

## 🗺️ 4. Xaero's Map Node Viewing Overhaul

- [x] **4.1 Aura Node Map Display Restrictions**
  - **Issue:** Nodes currently appear everywhere (minimap, waypoints in 1st/3rd person).
  - **Strict User Requirement:**
    - Aura nodes MUST ONLY be visible on the **fullscreen Xaero's WorldMap**.
    - Must be controlled by a **toggleable button inside the fullscreen Xaero's map UI**.
    - Nodes MUST NOT appear on the standard HUD minimap.
    - Nodes MUST NOT appear as in-world waypoints in 1st/3rd person view.
  - **Files to check:** `dev.neothaum.addon.xaero.*` and `dev.neothaum.client.hud.NodeRenderer`.

---

## 📖 5. Thaumonomicon & Tooltips

- [x] **5.1 Golemancy Tab Population in Thaumonomicon**
  - **Issue:** Golemancy category is completely empty except for the Hungry Chest.
  - **Action:** Add all original TC4 research nodes to `assets/neothaum/thaumonomicon/category_golemancy.json`:
    - Basic Golemancy
    - Straw, Wood, Clay, Flesh, Iron, Thaumium, Tallow, Stone Golems
    - Animation Cores (Gather, Harvest, Guard, Butcher, Decant, Fill, Empty, Use, Sort)
    - Golem Accessories (Top Hat, Spectacles, Bow Tie, Fez, Dart Gun, Iron Plating, etc.)
    - Traveling Trunk
    - Biothaumic Mind
  - Ensure all research entry prerequisites and connected recipe pages match TC4.

- [x] **5.2 Tooltip Accuracy Audit**
  - **Issue:** Golem accessories and several other items display inaccurate or placeholder text.
  - **Action:** Compare item descriptions in `assets/neothaum/lang/en_us.json` against `original/decompiled/assets/thaumcraft/lang/en_US.lang`.

---

## 🌍 6. World Generation & Structures

- [x] **6.1 Structure Spawn Frequency & Village Placement**
  - **Issues:**
    - NeoThaum structures (mound, pillar, obelisk, circle) spawn too frequently.
    - The Thaumaturge Village House spawns randomly in the wild across the overworld instead of only inside villages.
  - **Fixes:**
    - Increase spacing and separation in `data/neothaum/worldgen/structure_set/*.json`.
    - Move the Thaumcraft Village House into vanilla village Jigsaw template pools (`village/plains/houses`, `village/desert/houses`, etc.) so it exclusively generates as part of a village.

- [x] **6.2 Solid Land Validation for Structure Spawns**
  - **Issue:** Structures are generating on open water bodies (oceans, lakes, and rivers).
  - **Root Cause:** `TCWorldFeature.java` determines structure height using `world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)`, which stops on water fluid surfaces.
  - **Fix:** In `TCWorldFeature.java`, verify that the placement position is solid ground and NOT water/fluid (`!world.getFluidState(pos).is(FluidTags.WATER) && world.getBlockState(pos).isSolid()`) across the structure footprint before triggering generation.

- [x] **6.3 Wizard Tower Bottom Floor Doorway Fix**
  - **Issue:** Wizard Tower bottom floor blocks the door with wooden planks.
  - **Root Cause:** In `WizardTowerFeature.java` line 58, the interior floor is generated at `y = 1` (`cy + 1`), placing solid oak planks across the ground level right where the door is placed at `cy + 1`. In original TC4 (`ComponentWizardTower.java` line 59), the floor planks are at `y = 0` (`cy`), and `y = 1` through `y = 4` is open air so the doorway is completely unobstructed.
  - **Fix:** Move the ground floor oak planks down to `y = 0` (or `cy`), leaving `y = 1` clear for the entryway.

- [x] **6.4 Authentic Randomized TC4 Structure Loot Tables**
  - **Issue:** Loot in all generated structures is identical in items and layout (or using vanilla generic tables).
  - **Fix:** Replace hardcoded `chest.setItem(...)` slots with dedicated JSON loot tables in `data/neothaum/loot_table/chests/` mirroring original TC4 `ChestGenHooks`:
    - `chests/wizard_tower.json`: Paper, Gold Nuggets, Glass Bottles, Potions, Knowledge Fragments, Alumentum, Nitor, Thaumium Ingots, Thaumonomicon.
    - `chests/barrow_mound.json`: Eldritch artifacts, amber, coins, cursed trinkets.
    - `chests/banker_house.json`: Coins, emeralds, arcane gold keys, ledgers.
    - `chests/eldritch_ring.json`: Eldritch Eyes, void seeds, primordial pearl fragments.

---

## 🪄 7. Staves Architecture & Implementation

- [x] **7.1 Staff Mechanics & Item Representation**
  - **Original TC4 Mechanics:**
    - A Staff is an advanced combat/casting casting instrument crafted using a **Staff Core** (crafted from 2 wand rods + primal charms/inert cores) + 2 Wand Caps in the Arcane Worktable.
    - **Capacity:** Staves hold **2.5× the base rod capacity** (e.g. Greatwood Staff = 125, Silverwood Staff = 250, Primal Staff = 250).
    - **Melee Weapon:** High physical and magical melee damage attributes.
    - **Arcane Crafting Restriction:** CANNOT be placed in the Arcane Worktable wand slot (too large/cumbersome for fine benchcraft; foci casting & combat only).
  - **Action:** Provide distinct staff item definitions or wand variants for each staff core with 2.5× capacity, melee attributes, and worktable restrictions.


---

## ✅ Completed 2026-10-27 (Claude) — verified with gametest scenes (`NEOTHAUM_SCENES=...`)

Root causes differed from the handoff's guesses in several places; what was actually wrong and fixed:

- **1.1** Models pointed at `textures/item/` copies without the `.mcmeta`; 47 duplicate textures removed, models repointed to `textures/items/` (`agentic/tools/dedupe_textures.py`).
- **1.2** Four eggs had no item definition; ten eldritch/cultist eggs redrawn (`agentic/tools/make_eggs.py`).
- **1.3/1.4** Bore, bore base, flux scrubber, banners, eldritch altar/cap/obelisk get inventory renderers (`DeviceItemRenderers`); spa, construct, deconstruction table get item definitions. Banner recipes (16 colours) added.
- **1.5** TC4 icons: eye = eldritch_object, rites, tablet = eldritch_object_2, pearl = eldritch_object_3.
- **1.6** Ported `TileEtherealBloomRenderer` (block has no model in TC4); icon = purifier_seed.
- **1.7** Copper golem: icon, Tools tab, and TC4-layout skins at 4 oxidation stages (`make_copper_golem.py`) — vanilla's copper golem skin does not fit the TC4 golem model.
- **2.1/2.2** Altar, caps and obelisk are BER-only in TC4: OBJ caps; obelisk = end-portal-shader void column (tunnel + particlefield), rune sides, bobbing caps, 4 solid invisible shaft blocks; breaking one collapses the set. Obelisk buff fixed to TC4's Strength + Regeneration on IEldritchMobs. Eldritch ring regenerated to TC4 layout (capped totem pillars, banners, node, floating obelisk).
- **3.1** Models ported 1:1 from TC4 via `agentic/tools/port_model.py` (guardian/warden incl. translucency/fade, warden glow eye & rise-from-ground, golem, crab + overlay). Cultists: TC4 skin on 1.7 biped + TC4 custom armour models (robe/knight/praetor, plus fortress & void robe) through Fabric `ArmorRenderer` (`client/render/armor/`). Inhabited zombie (Shambling Husk) uses czombie.png + zombie plate. Mind spider ghosted per TC4.
- **3.2** TC4's slimes were all but silent (0.1·√size, no small-slime jump sound); now vanilla-slime sounds by request (comment marks the deviation).
- **3.3** Trading opened fine; payouts were lost (`moveItemStackTo` respects the output slots' `mayPlace`). Fixed.
- **4.1** Minimap waypoints removed. Nodes drawn only on Xaero's fullscreen World Map (mixin into `GuiMap`), toggle button bottom-right, remembered per world, **only nodes the player has scanned** and has seen through goggles. Jade node info likewise needs the node scanned and goggles worn or a thaumometer in hand.
- **5.1** 40 Golemancy entries were dropped at load (icon ids didn't match registered names) and none of their recipes existed. Icons fixed, 38 recipes converted from ConfigRecipes (idmap fixed), Golem Fetter block ported.
- **5.2** Golem cores/accessories/bell/placer names and tooltips now match TC4 (no invented "traits" line).
- **6.x** TC4 chain restored: one roll per chunk — mound 1/150, else ring 1/66, else hilltop 1/40, with TC4's ground checks (no water). Mound ported exactly (2,443 placements, `tc_structure/barrow_mound.txt`), hilltop ported with obsidian totems; loot urns/crates added. Wizard tower and banker's house are now village-only jigsaw pieces (`structure/village/*.nbt`, generated by the `templates` scene) at about TC4's frequency. TC4's chest-loot additions injected into vanilla dungeon/temple/mineshaft/stronghold/smith chests; tower chest = `chests/wizard_tower`.
- **7.1** Already matched TC4 (125/175/250 vis, +6 melee, refused by the worktable slot); verified.

## 🔜 Follow-ups found while working

- TC4's wizard and banker villagers (custom trades) are not ported; the village buildings have no resident.
- Apprentice's rings (bauble blanks 3–8) are not ported, so they're missing from chest loot.
- The warden/golem/praetor AI is largely invented (e.g. the warden fires golem orbs on a screech timer); port EntityEldritchWarden/Golem behaviour.
- Thaumostatic harness still uses the vanilla armour model (ModelHoverHarness not ported).
- **Golem AI still invented / missing (from the bell work):** the Use core presses nearby buttons (copper-golem add-on behaviour) instead of
  TC4's AIUseItem on marked blocks; the Liquid (Decant) and Essentia (Alchemy) cores have no AI yet; Empty-core marker colours are not yet
  matched against filter-slot colours. Fill/Empty/Sorting now work from bell markers.
- Giant Taintacle: TC4 dropped a primordial pearl when the last one in an area died; not ported.

## ✅ Completed 2026-10-28 (Claude) — the Outer Lands (Feature 11), verified with the `outer` gametest scene

- The neothaum:outer_lands dimension (WorldProviderOuter: no sky light, fixed time, dark purple fog) with the Eldritch biome and its spawns;
  a void flat world whose only feature builds each chunk's maze cell.
- Ported MazeGenerator / MazeThread / MazeHandler (kept in overworld saved data with MapBossData's boss counter) and every room:
  portal hall, 2x2 boss room with sealed doorways and locks, key room (Runed Tablet + 2-4 guardians), nests, libraries, and the plain,
  trapped, crusted, tainted and spider-hall corridors, with urns, pedestals, crystals, crab vents and runed traps.
- New TC4 blocks: Ancient Stone/Rock/Stone (no spawns), Crusted Stone, Ancient Pedestal, stairs and slab, Glowing Crusted Stone, Glyphed
  Stone (knowledge fragment), Ancient Doorway, Ancient Locking Mechanism, Crusted Opening (crab spawner), Runed Stone (trap), Nothing
  (starfield void), Impassable, Strange Crystals. Gemini's invented "Eldritch Stone" variants are gone.
- The altar now lays out a maze as its eyes go in and only opens over a sinister node once the maze exists; the portal carries players
  both ways (5 s cooldown) and grants ENTEROUTER; it collapses if its frame is broken.
- Fitting a Runed Tablet wakes the next boss (golem, warden, crimson portal, champion taintacles) in its TC4 arena.
- Tainted corridors turn their biome to Tainted Land; crab vents puff TC4's FXVent steam; away from the Outer Lands a guardian's gaze
  (PacketMiscEvent 2) draws TC4's fiddled fog in for ten seconds, and the warp mist (event 1) for two minutes.
- Guardians wear a regrowing ward in the Outer Lands; champions are likelier there (key rooms and libraries most); the portable hole won't
  work there; OUTERREV triggers on glyphed and runed stone.

## ✅ Completed 2026-10-28 (Claude) — Gemini audit fixes (agentic/AUDIT_GEMINI.md), verified with the `audit` gametest scene

- Arcane Lamp: TC4 sky-or-block light check and its own invisible lamp light (BlockAiry 3); lights cleared however the lamp is removed.
- Arcane keys: one blank in, one imprinted key out; TC4 key sound; chat messages. Infernal furnace: 1 tick of speed per centivis.
- Boots of the Traveller: only TC4's step (full block), jump (+0.275), push and fall softening.
- Liquid Death: 4-quanta finite fluid, level+1 armour-piercing "dissolve" damage, crystallized essence drops. Purifying Fluid: real
  flowing fluid (no new sources, water-tagged), Warp Ward from source blocks.
- Harness: TC4 hover drag instead of extra flying speed. Hard Nodes toggle now works.
- Primal arrows: Air/Order ignore armour; Slowness/Weakness V. Rare loot bag: Primordial Pearl.
- Eldritch Altar: needs a sinister node above, no lightning, TC4 crystal sound, TC4 defender spawning (eyes 3+ wake guardians).
- Cultist Portal: TC4 stage timing, 1 HP/tick healing, touch zap, loot crates, keeps summoning after the Praetor, harmless 2.0 burst,
  one Primordial Pearl.
- Champions rebuilt to TC4 (whitelist, difficulty/biome odds, +30 HP, x3 damage, every modifier's real effect, boss naming, loot bag
  and 5-7 XP); aspect orbs per primal at 50%; warp milestones @BATHSALTS/ELDRITCHMINOR/ELDRITCHMAJOR.
- Removed inventions: node zaps, lodestone warp, mana pod harvest/bonemeal, fragment sound, champion orbs and always-on nameplates.
- Golemancer's Bell ported (select, mark/unmark/cycle colours, sneak clears, F clears all, hit to pick up, sneak-hit to strip, home and
  marker overlay); golems keep markers, also through the placer.
- Branding: creative tabs read "NeoThaum: ...", golem entity is "Golem". JEI catalyst crash (itemless research table) fixed.

## 🧩 Mod integrations to support eventually (addon code in `dev.neothaum.addon` / `client.addon`; all optional "suggests" deps)
- [ ] **Trinkets / Accessories** — put amulets, rings, belts, Hover Girdle and Vis Amulet in the shared accessory slots (the modern Baubles).
- [ ] **EMI and REI** — crucible, arcane worktable, infusion and item-aspect pages, like the existing JEI plugin.
- [ ] **Sodium / Iris** — compatibility pass: check every custom renderer (nodes, jars, infusion, tubes, golems, eldritch portal) with Sodium and with Iris shaders on.
- [ ] **WTHIT** — node, jar and device info, matching the Jade plugin (same scanned + goggles/thaumometer gating).
- [ ] **Xaero's Minimap / JourneyMap** — JourneyMap gets the scanned-node layer on its fullscreen map; check the Xaero World Map toggle and waypoint buttons with Xaero's Minimap installed.
- [ ] **AppleSkin** — food values for NeoThaum foods (zombie brain, mana beans, etc.).

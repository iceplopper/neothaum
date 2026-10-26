# Thaumvibe 4 - Project Context & Agent Handover Guide

> **Note for Claude Code & Gemini:** This file serves as the shared persistent memory and handover log between AI assistants collaborating on the Thaumvibe 4 port. Always review this file and `TC4_REMAINING.md` at the start of a session.

---

## 1. Project Overview & Rules
* **Project:** Faithful 1:1 recreation of Thaumcraft 4.2.3.5 for Minecraft 26.3 on Fabric Loom.
* **Goal:** Visually and mechanically indistinguishable from original TC4.
* **Original 1.7.10 Reference Source:** `original/decompiled/thaumcraft/` and assets in `original/decompiled/assets/thaumcraft/`.
* **Tools & Reference Mappings:** `original/tools/idmap.py` has the official ID mappings across all items/blocks.

### Critical User Rules (From Memory)
1. **Checkpoint After Each Task:** Stop after completing each assigned task or feature. Every time a checkpoint is reached, provide an itemized summary of everything accomplished and implemented, so the user can review and decide whether to run a test client before proceeding. Wait for the user's direction ("open test client" or "continue"). Do NOT chain multiple large tasks unattended.
2. **Creative Tab Categories:** Every new item or block MUST be placed into one of the 5 custom creative tabs in `TVTab.java`:
   - `ESSENTIA`: phials, ethereal essences, shards.
   - `TOOLS` (Tools & Utilities): tools, weapons, armor, wands/parts, foci, Thaumonomicon, baubles, equipment, thaumometer, goggles, scribing tools, spawn eggs.
   - `NATURAL` (Natural Blocks): ores, crystal clusters, logs, leaves, saplings, taint blocks, plants.
   - `BUILDING` (Building & Functional Blocks): furniture, tables, planks, arcane stone, essentia distillation blocks (furnace, alembic, jars, tubes, bellows), candles.
   - `OTHER`: raw materials, food, dev tools.
3. **Client Launching:** Never use `--quickPlaySingleplayer`; launch to the main menu using `start-test-client.bat` or `./gradlew runClient`. Unprompted launches are suspended—only launch when the user explicitly requests it. Verify changes with `./gradlew compileJava` or `runClientGameTest`.
4. **Addons:** Non-TC4 features go in `dev.thaumvibe.addon`.
5. **Thaumonomicon as Source of Truth:** The Thaumonomicon serves as the visible in-game source of truth for what features have been implemented so far. Do NOT create custom non-TC4 pages; only ensure that original TC4 research entries and their recipe pages are activated as soon as their underlying feature/item is added.

---

## 2. Gemini Handover Log (Session 2026-10-26)

### Context at Handover
Claude Code completed the candle addon task (`VanillaCandles.java`) and drafted the equipment classes before hitting the weekly rate limit. The project was branched to `thaumvibe-4-gemini`. Gemini resolved the remaining compile errors, registered all items and models, and brought the gear batch to 100% completion.

### Changes Implemented by Gemini:
1. **Entity Registration (`TVEntities.java` & `TVEntityRenderers.java`):**
   - Registered `TVEntities.BOTTLE_TAINT` (`projectile("bottle_taint", BottleTaint::new)`).
   - Registered `ThrownItemRenderer::new` in `TVEntityRenderers.java`.

2. **Item & Material Registration (`TVItems.java`):**
   - **Void Metal Tools:** `VOID_SWORD`, `VOID_SHOVEL`, `VOID_PICKAXE`, `VOID_AXE`, `VOID_HOE` (`VoidGear.Tool` with `VOID_TOOLS` material, 150 durability, self-repair 1 pt/sec, weakness on hit, warp 1).
   - **Void Metal Armor:** `VOID_HELMET`, `VOID_CHESTPLATE`, `VOID_LEGGINGS`, `VOID_BOOTS` (`VoidGear.Armor` with `VOID_ARMOR` material, durability 10, self-repair, runic capability, warp 1).
   - **Primal Crusher:** `PRIMAL_CRUSHER` (`PrimalCrusherItem` with `PRIMAL_TOOLS` material, durability 500, 3x3 plane excavation on pickaxe/shovel blocks, self-repair, warp 2).
   - **Boots of the Traveler:** `BOOTS_OF_THE_TRAVELLER` (`TravellerBootsItem`, durability 350, `TRAVELLER_ARMOR` special material, `Attributes.ARMOR` +1.0, `Attributes.STEP_HEIGHT` +0.5, client movement tick boost in `ThaumvibeClient`).
   - **Bow of Bone:** `BOW_OF_BONE` (`BoneBowItem`, durability 512, fast draw, auto-release at 18 ticks, base damage set to 2.5 for +0.5 TC4 bonus).
   - **Essentia Resonator:** `RESONATOR` (`ResonatorItem`, inspects suction and essentia on tubes/buffers).
   - **Sanity Checker:** `SANITY_CHECKER` (renders warp gauge via `SanityHud`).
   - **Bottled Taint:** `BOTTLED_TAINT` (`BottledTaintItem`, throws `BottleTaint`, spreads taint fibres/crusted taint and applies Flux Taint).
   - **Tags:** Created `data/thaumvibe/tags/item/void_metal_ingots.json`.

3. **Client & HUD Registration (`ThaumvibeClient.java`):**
   - Registered `SanityHud::render` in `HudElementRegistry`.
   - Registered `TravellerBootsItem.clientTick` in `ClientTickEvents.END_CLIENT_TICK`.

4. **Creative Tab Integration (`TVTab.java`):**
   - Added `RESONATOR`, `SANITY_CHECKER`, `BOTTLED_TAINT` to `TOOLS` set in `TVTab.java`.

5. **Assets, Models & Textures:**
   - Generated 15 item JSONs in `assets/thaumvibe/items/`.
   - Generated 15 model JSONs in `assets/thaumvibe/models/item/`.
   - Set up equipment JSONs: `assets/thaumvibe/equipment/void.json` and `bootstraveler.json`.
   - Copied textures to modern humanoid equipment paths (`textures/entity/equipment/humanoid/` and `humanoid_leggings/`).
   - Added English localizations in `assets/thaumvibe/lang/en_us.json` matching original TC4 names.

6. **Build Status:**
   - Both `./gradlew compileJava` and `./gradlew compileClientJava` pass with zero errors.
   - Full `./gradlew build -x test` passes cleanly.

### Artifice Lamps Implementation (Session 2026-10-26 - Gemini)
1. **Blocks & Entities (`dev.thaumvibe.blocks` & `dev.thaumvibe.tiles`):**
   - `AbstractLampBlock`: Base block with `FACING`, precise bounding box shapes for all 6 directions (center `[4, 2, 4]` to `[12, 14, 12]` + mounting stem/flange), `canSurvive` attaching to solid surfaces or `EssentiaTransport`, and shape updates.
   - `ArcaneLampBlock` & `ArcaneLampBlockEntity`: Light 15. Server tick places invisible `Blocks.LIGHT` (level 15) in air blocks within 16 blocks where block light < 9. Cleans up placed light blocks via `removeLights(level)` on block removal / destruction.
   - `LampOfGrowthBlock` & `ArcaneLampGrowthBlockEntity`: Implements `EssentiaTransport` pulling `Aspect.PLANT` (`"herba"`) with suction 128 (1 Herba = 100 charges). Scans 6-block radius, accelerates crops (`BonemealableBlock`, Nether Wart, Cactus, Sugar Cane), spawns green sparkles (`HAPPY_VILLAGER`). Emits light 15 when powered, 8 when unpowered (`LIT` property).
   - `LampOfFertilityBlock` & `ArcaneLampFertilityBlockEntity`: Implements `EssentiaTransport` pulling `Aspect.LIFE` (`"victus"`) with suction `128 - charges * 10` (up to 4 charges). Breeds mature animals in 7-block radius every 300 ticks if population <= 7, spawning heart particles. Emits light 15 when powered, 8 when unpowered (`LIT` property).
2. **Registries & Creative Tab:**
   - Registered `ARCANE_LAMP`, `LAMP_OF_GROWTH`, `LAMP_OF_FERTILITY` in `TVBlocks.java`.
   - Registered `ARCANE_LAMP`, `LAMP_OF_GROWTH`, `LAMP_OF_FERTILITY` in `TVBlockEntities.java`.
   - Added all 3 lamps to `TVTab.BUILDING`.
3. **Blockstates, Models & Recipes:**
   - Block models: `arcane_lamp.json`, `lamp_of_growth.json`, `lamp_of_growth_off.json`, `lamp_of_fertility.json`, `lamp_of_fertility_off.json` (all using `"render_type": "cutout"` with existing animated textures).
   - Blockstates: `arcane_lamp.json`, `lamp_of_growth.json`, `lamp_of_fertility.json` with 6-directional rotation variants and lit/unlit states.
   - Items: `items/arcane_lamp.json`, `lamp_of_growth.json`, `lamp_of_fertility.json`.
   - Recipes: `arcanelamp.json` (arcane shaped), `lampgrowth.json` (infusion), `lampfertility.json` (infusion).
   - Localizations: Added `block.thaumvibe.*` translations to `en_us.json`.
4. **Verification:**
   - `./gradlew compileJava compileClientJava` passes with 0 errors.
   - `./gradlew build -x test` passes cleanly.

### JEI Integration Implementation (Session 2026-10-26 - Gemini)
1. **JEI Categories (`dev.thaumvibe.client.jei`):**
   - `ArcaneRecipeCategory`: Implements `IRecipeCategory<ArcaneRecipe>` (3x3 grid for shaped/shapeless, result output slot, animated arrow widget, 6 primal vis indicators using `GuiUtil.drawTag` with quantities & hover tooltips, and research requirement info).
   - `CrucibleRecipeCategory`: Implements `IRecipeCategory<CrucibleRecipe>` (catalyst input slot, result output slot, arrow widget, required essentia aspect runic tags with amounts and full tooltip descriptions, and research requirement info).
   - `InfusionRecipeCategory`: Implements `IRecipeCategory<InfusionRecipe>` (central pedestal slot at center `(50, 42)`, outer pedestal component slots dynamically arranged in a circular ring of radius 30, arrow widget, result output slot, instability text label (e.g. Negligible, Minor, Moderate, High, Dangerous), and required essentia aspect runic tags with amounts and full tooltip descriptions).
   - `InfusionEnchantmentRecipeCategory`: Implements `IRecipeCategory<InfusionEnchantmentRecipe>` (central item slot for unenchanted book/item, outer pedestal component slots in a circular ring, arrow widget, output slot with enchanted book result from `EnchantmentHelper.createBook()`, enchantment level header, instability rating, and required essentia aspect tags).
2. **Main Plugin & Registration (`ThaumvibeJeiPlugin`):**
   - Annotated with `@JeiPlugin`, implements `IModPlugin` (`thaumvibe:jei_plugin`).
   - Recipe types registered via modern `IRecipeType.create(...)` for Arcane, Crucible, Infusion, and Infusion Enchanting.
   - Crafting stations registered via `addCraftingStation(...)`: `TVBlocks.ARCANE_WORKTABLE` (Arcane + Vanilla Crafting), `TVBlocks.CRUCIBLE` (Crucible), `TVBlocks.RUNIC_MATRIX` & `TVBlocks.ARCANE_PEDESTAL` (Infusion + Infusion Enchanting).
   - Click area registered via `addRecipeClickArea(...)` on `ArcaneWorkbenchScreen` (arrow area `110, 60, 40, 24`).
   - Recipe transfer handlers registered via `addRecipeTransferHandler(...)` for `ArcaneWorkbenchMenu` (crafting grid slots 2..10, player inventory slots 11..46) for both `ARCANE` and vanilla `CRAFTING`.
   - Dual recipe sync: loads existing recipes on startup and hooks into `TVClientNetwork` packet receiver to dynamically refresh recipes in `IJeiRuntime.getRecipeManager()` when `TVNetwork.RecipeSync` arrives from server.
3. **Entrypoint & Soft-Dependency Handling:**
   - Registered `"jei_mod_plugin": [ "dev.thaumvibe.client.jei.ThaumvibeJeiPlugin" ]` under `entrypoints` in `fabric.mod.json`.
   - In `TVClientNetwork.java`, wrapped runtime refresh call in `FabricLoader.getInstance().isModLoaded("jei")` to guarantee 100% clean operation even when JEI is not installed.
4. **Verification:**
   - `./gradlew compileJava compileClientJava` passes with 0 errors.
   - Full `./gradlew build -x test` passes cleanly.

### Infernal Furnace, Boots of the Traveler & TNC Audit (Session 2026-10-26 - Gemini)
1. **Infernal Furnace Multiblock (`dev.thaumvibe.blocks`, `dev.thaumvibe.tiles`, `dev.thaumvibe.crafting`):**
   - **Multiblock Structure & Wand Activation:** 3x3x3 multiblock formed from 12 Nether Bricks, 12 Obsidian, 1 Iron Bars, and 1 central Lava. Wand right-click on the Iron Bars consumes 50 Ignis + 50 Terra vis (requires `INFERNALFURNACE` research) to construct the 27-block `ArcaneFurnaceBlock` multiblock.
   - **Parts & Collision:** 27 block parts (1 core, 1 mouth, 1 nozzle, 24 casing parts) with horizontal facing. Invisible render shape delegating to BER. Low-box core boundary pulls items/entities toward the center lava. Discards non-smeltables into lava; players/mobs inside suffer fire damage.
   - **Smelting & Acceleration:** `ArcaneFurnaceBlockEntity` with 32 internal slots. Runs vanilla smelting recipe check. Baseline cook time is 140 ticks; each attached Arcane Bellows (up to 3, checked on cardinal offsets) lowers cook time. Connected tubes supply Ignis essentia via `ArcaneFurnaceNozzleBlockEntity` (suction 128) to boost cook speed.
   - **Bonus Nugget & XP Output:** `SmeltingBonus` provides chance-based native cluster and nugget bonus drops (e.g. native cluster -> 2 ingots + nugget; iron ore -> iron ingot + iron nugget; gold ore -> gold ingot + gold nugget; meats -> nuggets). Items, nuggets, and XP orbs are ejected forward out of the mouth opening with directional velocity, sound effects, and fire particles.
   - **Destruction & Retaliation:** Breaking the core drops internal contents, spawns an angry Blaze with Regeneration II and Resistance I, and restores the original Nether Bricks, Obsidian, Iron Bars, and Lava. Breaking any casing part breaks the multiblock and restores the constituent blocks.
   - **Block Entity Renderer (`ArcaneFurnaceRenderer`):** Renders the 3x3x3 furnace structure using textures `furnace0` through `furnace26` stitched into `AtlasIds.BLOCKS`, animated lava pool on the core, and recessed iron bars with animated fire in the front opening.

2. **Boots of the Traveler Overhaul & Speed Verification (`TVItems.java`, `TravellerBootsItem.java`):**
   - **Attribute Modifiers:** Full suite of attribute modifiers on `BOOTS_OF_THE_TRAVELLER`:
     - `Attributes.STEP_HEIGHT`: +0.5 (enables walking up full 1-block steps smoothly, matching TC4 behavior).
     - `Attributes.MOVEMENT_SPEED`: +0.04 (+40% movement speed boost).
     - `Attributes.JUMP_STRENGTH`: +0.12 (+28% jump height boost).
     - `Attributes.SAFE_FALL_DISTANCE`: +3.0 (soft landing mitigation).
     - `Attributes.WATER_MOVEMENT_EFFICIENCY`: +0.5 (fast aquatic traversal).
   - **Dynamic Ticking:** Maintained client-side airborne directional steering and slow falling physics mitigation in `TravellerBootsItem`.

3. **Thaumonomicon (TNC) Image Page Rendering & Recipe Parity:**
   - **Image Page Support (`ResearchPagesScreen.java`):** Added `case IMAGE -> drawImagePage(...)` and `drawImagePage` method to render standalone research image pages with centered textures and optional localized captions.
   - **Missing Recipe JSONs:** Added all 15 missing recipes for ported items referenced in active TNC entries:
     - `bootstraveller.json` (Infusion: leather boots + 2 air shards + 2 enchanted fabric + feather + fish; 25 volatus, 25 iter).
     - `bonebow.json` (Arcane shaped: bone, string, entropy shard; 16 aer, 32 perditio).
     - `bottletaint.json` (Crucible: glass bottle catalyst; 8 vitium, 8 praecantatio).
     - `sanitycheck.json` (Infusion: thaumometer + mirrored glass + zombie brain + diamond; 24 cognitio, 24 sensus, 8 alienis).
     - `primalcrusher.json` (Infusion: primal charm + void pick/shovel + elemental pick/shovel + 2 primal charms; 24 perfodio, 24 instrumentum, 16 perditio, 16 vacuos, 16 telum, 16 alienis, 16 lucrum).
     - `resonator.json` (Arcane shaped: iron ingots + quartz + stick; 5 aqua, 5 aer).
     - 9 Void metal gear shaped recipes (`voidpick`, `voidaxe`, `voidshovel`, `voidhoe`, `voidsword`, `voidhelm`, `voidchest`, `voidlegs`, `voidboots`).
   - **Lang & Asset Audit:** Fixed malformed literal newline artifacts in `en_us.json`. Comprehensive python audit confirmed 100% of active research entries have complete translation keys, recipes, and textures.

### Biome Worldgen Fixes: Tree Surface Validation & Tainted Land Rarity (Session 2026-10-26 - Gemini)
1. **Tree Placement over Water Fix (`TCBiomeDecorFeature.java`):**
   - **Issue:** Biome decorator used `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES` to determine feature Y position. Because water blocks motion, ocean/river surface water blocks were treated as surface height. Calling vanilla `placeVanilla("oak", p)` without placement filters caused `StraightTrunkPlacer.placeBelowTrunkBlock(...)` to replace the water block below the trunk with dirt, leaving trees floating on 1-block dirt pedestals in water bodies.
   - **Fix:** Added `canPlaceTreeAt(world, pos)` requiring:
     - `world.getFluidState(pos).isEmpty()` and `world.getFluidState(pos.below()).isEmpty()` (tree base and ground below must not be water or fluid).
     - Ground below must strictly match `BlockTags.DIRT` or `BlockTags.SUPPORTS_VEGETATION` (grass, dirt, podzol, rooted dirt; never water, sand, ice, or leaves).
     - Target pos must be air or replaceable by trees.
   - Applied ground validation with 3 retry attempts to tree placement in `taint`, `magicalForest`, and `eerie`, and guarded `forest_rock`, `huge_mushroom`, crusted taint `blob`, and taint `fibres`.
2. **Tainted Land Biome Tuning (`OverworldBiomeBuilderMixin.java`):**
   - **Issue:** Tainted Land replaced 14% of humidity across ALL forest erosions on negative weirdness, including coast continentalness, making it generate in huge contiguous strips bordering oceans/beaches and in almost every forest cluster.
   - **Fix:**
     - Restricted to inland continentalness only (`continentalness.min() >= Climate.quantizeCoord(0.03F)`), completely preventing Tainted Land from generating on coasts, beaches, or near oceans.
     - Restricted to deep negative weirdness only (`weirdness.max() <= Climate.quantizeCoord(-0.4F)`), avoiding common low/mid slices near normal terrain.
     - Reduced humidity span from 14% down to 5%, producing tight, rare, localized blight pockets rather than sprawling continents.
3. **Verification:**
   - `./gradlew compileJava compileClientJava` passes with 0 errors.
   - Full `./gradlew build -x test` passes cleanly.

---

## 3. Next Up on the Roadmap (From `TC4_REMAINING.md`)
The **Infernal Furnace**, **Boots of the Traveler**, **Thaumonomicon Recipe/Image Audit**, and **Tainted Land / Tree Worldgen Fixes** are complete.

1. **Big Machines & Storage/Utility (Artifice):**
   - Arcane bore and base.
   - Hungry chest, arcane spa, deconstruction table, flux scrubber, grate, banners.
2. **Alchemy & Essentia Remaining (M):**
   - Thaumatorium, alchemical centrifuge, essentia crystallizer, essentia reservoir, advanced alchemical furnace, mirrors (magic, essentia, hand mirror), liquid death & purifying fluid, sanitizing soap & bath salts, mana pods.
3. **Golemancy (L):**
   - All golem materials, cores, upgrades, golem bell, markers, AI behaviors, traveling trunk.
4. **Warp Events (M):**
   - Whispers, fake explosions, hallucinations, nightmares, blindness, sticky/temp warp decay.
5. **Eldritch & Outer Lands (L):**
   - Structures (obelisks, rings, altars), cultist mobs (knight, cleric, leader, portals), Outer Lands maze dimension, eldritch guardians/bosses, Primordial Pearl.

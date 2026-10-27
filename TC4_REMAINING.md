# Thaumcraft 4.2.3.5 features not yet in Thaumvibe 4

Last updated: 2026-10-26, after Lamps, JEI integration, Doors & Warding, Infernal Furnace, Boots Overhaul, TNC Audit, and Worldgen Fixes.
Size guide: **S** = an hour or two, **M** = a session, **L** = several sessions.

## Already done (for reference)

- **Research and aspects:** aspects and scanning, the Thaumonomicon and research table, the thaumometer and goggles.
- **Wands and crafting:** every wand cap and rod, staffs and sceptres (with TC4's 3D wand model), the crucible and arcane worktable.
- **Foci:** all ten foci with their upgrades, the F radial menu and G architect key, the focus pouch (bauble too), the focal manipulator.
- **Vis network:** node stabilizers, the node transducer and energized nodes, vis relays, the vis charge relay, the wand recharge pedestal
  and compound recharge focus; vis amulets refill from relays.
- **Enchantments:** Haste and Repair (also on the infusion enchanting pages and in the Pech mage's trades).
- **Aura nodes:** all node types and modifiers, and node world generation.
- **Node jars:** catching a node in a jar with a wand (glass and wooden slab frame, NODEJAR research), carrying it as an item, and freeing it.
- **Node effects on land:** pure nodes turn tainted land (or, inside a silverwood, any land) into magical forest, sinister nodes make it eerie
  and call up giant zombies in the dark, tainted nodes spread tainted land and fibres, and nodes in tainted land can turn tainted.
- **World and plants:** ores and infused stone, greatwood and silverwood trees, and the plants.
- **Artifice, Lamps & Infernal Furnace:**
  - **Infernal Furnace:** 3x3x3 multiblock formed from 12 Nether Bricks, 12 Obsidian, 1 Iron Bars, and 1 central Lava. Wand right-click on Iron Bars consumes 50 Ignis + 50 Terra vis. 27-block structure with BER animated lava, front mouth opening with fire/bars, automatic entity suction, 32-slot smelting queue, bonus nuggets/clusters/XP ejection with directional velocity, Arcane Bellows cook time acceleration & bonus chance boost, Ignis essentia input via nozzle (suction 128), and angry Blaze retaliatory spawn on break.
  - **Thaumonomicon Parity & Auditing:** Added `case IMAGE` page rendering to `ResearchPagesScreen`, added 15 missing recipe JSONs (`bootstraveller`, `bonebow`, `bottletaint`, `sanitycheck`, `primalcrusher`, `resonator`, and 9 Void metal gear recipes), verified 100% active research entries match original TC4 pages.
  - **Arcane Lamp:** attaches to 6 faces (floor, ceiling, wall); emits light 15 and places invisible `Blocks.LIGHT` up to 16 blocks away where light < 9; cleans up placed light blocks when broken/removed.
  - **Lamp of Growth:** attaches to 6 faces (tubes, walls, floors); draws Herba essentia via `EssentiaTransport` (1 Herba = 100 charges); scans 6-block radius and accelerates crop/plant growth with green bonemeal sparkles; dynamic lighting (15 powered, 8 unpowered).
  - **Lamp of Fertility:** attaches to 6 faces (tubes, walls, floors); draws Victus essentia via `EssentiaTransport` (up to 4 charges); breeds animal pairs in 7-block radius every 300 ticks when population <= 7; dynamic lighting (15 powered, 8 unpowered).
  - **Hungry Chest:** 27-slot sentient wooden chest with sharp chomping teeth along the seam; scans volume above and collision bounds to automatically consume dropped items; plays eating crunch sounds and snappy jaw animation; arcane worktable recipe (wooden trapdoor + planks + 5 Aer, 3 Ordo, 3 Perditio); 3D special item model rendering.
- **Doors, warding, travel and utility:**
  - paving stone of travel (`paving_stone_of_travel`, grants Speed II and Jump Boost I, sparkle effects on step)
  - item grate (`item_grate`, open/closed hatch impassable to players/mobs but dropped items fall right through, hopper auto-eject downwards)
  - arcane door (`arcane_door` block & item, unbreakable/unopenable to non-permitted, access list sync)
  - warded glass (`warded_glass`, translucent glass unbreakable to non-owners)
  - paving stone of warding (`paving_stone_of_warding` + invisible `warding_aura` barrier, repels mobs while letting players pass, deactivated by redstone)
  - arcane pressure plate (`arcane_pressure_plate`, 3-mode toggle: all, except owner/permitted, only owner/permitted; opens adjacent arcane doors; unbreakable to non-owners)
  - arcane ear (`arcane_ear`, 6-directional sensor listening for note block pitch/instrument matches within 64 blocks and outputting 10-tick redstone pulse)
  - arcane levitator (`arcane_levitator`, 6-directional levitator lifting entities up to 10 blocks + 10 per stacked levitator, sneaking softly descends, resets fall distance)
  - arcane keys (`iron_arcane_key` & `gold_arcane_key`, right-click imprint on door/plate, consume to grant permanent access)
- **Essentia:**
  - warded and void jars, labels and the brain in a jar
  - every tube kind and the buffer
  - the alembic, alchemical furnace and bellows
  - phials, plus your suction priority rules
  - **Alchemical Centrifuge:** splits compound essentia back into one of its component aspects; directional suction (128 empty, 64 running from below, outputs split component above); animated spinning rotor and counterweight flasks.
  - **Essentia Crystallizer:** condenses liquid essentia drawn through tubes into solid crystallized essence items (`crystal_essence`), with rotating aspect-colored crystal formation geometry and auto-ejection into adjacent containers.
  - **Essentia Reservoir:** holds up to 256 essentia of any mix; 24 suction when not full; wand-configurable connection face; comparator signal proportional to fill level; interior liquid render with smoothly transitioning aspect tints and creaking audio; infusion crafting recipe.
  - **Node in a Jar Aspect Viewing:** trapped aura nodes now display floating 3D aspect tags when worn with Goggles of Revealing or inspected with Thaumometer, fully scannable and visible in HUD.
- **Infusion:** the matrix, pedestals, pillars, stabilisers and instability, and infusion enchanting (partial, see below).
- **Equipment:**
  - runic shielding and runic baubles, vis amulets, the hover girdle, elemental tools, thaumium tools and armour, and thaumaturge's robes.
  - **Thaumium Fortress Armor:** helm, cuirass, and thigh guards with heavy physical protection (40 durability, [3,7,6,3] defense), 25 enchantability, up to 25% cumulative damage reduction, Revealer Goggles upgrade attachment, and three mystical masks: Grinning Devil, Angry Ghost (withering retaliation on hit), and Sipping Fiend (lifesteal on melee strikes); infusion recipes.
  - **Void metal:** void tools (sword, shovel, pickaxe, axe, hoe) and void armour (helm, chestplate, leggings, boots) with warp, self-repair, and weakening strikes.
  - **Boots of the Traveler:** step-height boost (+0.5), slow falling (-0.25/tick), runic augmentable, and client movement speed boost.
  - **Bow of Bone:** fast draw, auto-release at 18 ticks, and +0.5 base arrow damage bonus.
  - **Primal Crusher:** 3x3 plane excavation on pickaxe/shovel blocks, self-repair, and warp 2.
  - **Essentia Resonator:** knocks on tubes/devices and reports current suction amount/type and essentia contents.
  - **Sanity Checker:** corner gauge HUD display rendering permanent, sticky, and temporary warp.
  - **Bottled Taint:** throwable splash projectile spreading taint blocks/fibres and applying Flux Taint effect.
- **Flux:** flux goo and gas, all nine potion effects, and basic warp gain.
- **Biomes & Worldgen Fixes:**
  - Magical Forest, Tainted Land and Eerie, plus taint blocks and spread.
  - **Tree Ground Validation:** fixed trees generating over water bodies on 1-block dirt pedestals by enforcing strict soil and fluid checks (`canPlaceTreeAt`).
  - **Tainted Land Tuning:** removed coastal tainted land generation and restricted it to rare, deep-inland pockets with a narrow 5% humidity slice.
- **Mobs:**
  - angry and furious zombies
  - wisp, firebat, Pech (with trading), thaumic slime
  - taint spore, swarmer, swarm and crawler
  - taintacle (small, normal and giant)
  - tainted chicken, cow, pig, sheep, villager and creeper
- **Curatives & Fluids:**
  - **Liquid Death:** corrosive magenta fluid dealing 4 magic damage on touch, lavapop sound and witch particles, scooped via bucket (`bucket_of_liquid_death`), crucible recipe (mortuus 32, venenum 32, perditio 32).
  - **Purifying Fluid:** restorative silvery water granting Warp Ward when stepped into (duration scaled inversely to permanent warp), scooped via bucket (`bucket_of_purifying_fluid`).
  - **Purifying Bath Salts:** dissolves when used or tossed into water source blocks, transforming them into Purifying Fluid with bubbling sounds; crucible recipe.
  - **Sanitizing Soap:** washable bar that clears all temporary warp, and has a 33% chance (+25% with Warp Ward, +25% in Purifying Fluid) to reduce sticky warp; crucible recipe.
- **Other items:** mana beans, ethereal essence.
- **Addon features (in `dev.thaumvibe.addon`):**
  - Vanilla candles as tallow candles (interchangeable for infusion altar stabilization, 1:1 color exchange crafting recipes, Thaumonomicon entry in tallow node).

## Still to do

### Alchemy and essentia, the rest (M)
- **Machines:** the Thaumatorium (multi-recipe alchemy).
- **Storage:** the advanced alchemical furnace.
- **Mirrors:** the magic mirror, essentia mirror and hand mirror.
- **Mana pods** (block and worldgen), and planting mana beans on logs to grow them.

### Artifice and utility blocks (M)
- **Big machines:** the arcane bore and its base.
- **Spa and deconstruction:** the arcane spa and the deconstruction table.
- **Misc:** the flux scrubber and banners (cultist and the Thaumcraft colours).

### Remaining Equipment and gear (M)
- **Void fortress robes** (infusion upgrade).
- **The thaumostatic harness.**
- **Weapons:** the crimson blade and primal arrows.
- **Cultist gear:** cultist robes, plate and leader armour, and cultist boots.
- **Throwables:** throwing alumentum.
- **Small items:** the loot bag, the stone compass, and using knowledge fragments.

### Golemancy (L)
- **Golems:** all material types, with cores, upgrades and decorations.
- **Tools:** the golem bell and the golem's markers.
- **Behaviour:** the full golem AI set (gather, fill, empty, harvest, guard, fish, sort, liquid and essentia work).
- **Travelling trunk.**

### Eldritch and the Outer Lands (L)
- **The Outer Lands dimension:** maze generation, rooms, locks, nests and boss rooms.
- **Overworld structures:** eldritch obelisks, altars and rings, the eldritch portal and keys.
- **Mobs:** eldritch guardian, eldritch warden, eldritch golem (bosses), eldritch crab, mind spider, inhabited zombie, and the watcher.
- **Cultists:** crimson knight, cleric and leader (boss), and the cultist portal.
- **Items:** the primordial pearl, crimson rites, eldritch eye and runed tablet.
- **Champion mobs:** the champion modifiers TC4 adds to some mobs (the giant taintacle uses one).
- **Research:** eldritch minor and major, forbidden knowledge.

### Warp, fully (M)
- **Warp events:** whispers, fake explosions, spider and guardian ambushes, nightmares, blindness and hunger.
- **Sticky and temporary warp decay**, plus the research that grants and needs warp.

### World generation, the rest (M)
- **Structures:** barrow mounds with loot urns and crates, hilltop stones, and eldritch rings.
- **Villages:** the Thaumcraft villages' wizard tower and banker (moneychanger) houses.
- **Ethereal bloom** (a taint-cleansing plant).

### Polish and integration (S to M)
- **Node in a Jar aspect viewing:** show node aspects, node type, and node modifier in HUD overlay when looking at a placed jarred node in the world with Goggles of Revealing or Thaumometer (parity with wild nodes and jar item tooltip).
- **Aspect orbs** dropped by mobs on death and by the primal charm.
- **Lightning** from nodes to players (nodeBolt).
- **The stone compass's** pull toward sinister nodes (TC4 records them from the node tick; nothing reads that yet).
- **Shovel of the Earthmover:** the orientation keybind and placement preview.
- **Config file:** the TC4 options (spawn toggles, taint spread rate, biome weights and so on).
- **Achievements and advancements.**

### Test coverage still missing
- **Gametests** for the elemental tools and baubles, and Pech trading. (Taint conversion, peaceful rules, wands, foci and the vis network have scenes now.)
- **Not ported on purpose:** TC4's Frugal, Potency and Wand Fortune enchantments only applied to the old focus API that no 4.2 focus used; focus upgrades replaced them.

## Suggested order

1. **Artifice blocks and the remaining alchemy machines.**
2. **Remaining gear (Fortress armor, Harness, Cultist armor, Crimson blade, Primal arrows).**
3. **Golemancy.**
4. **Warp events.**
5. **Eldritch content and the Outer Lands.**
6. **Polish.**

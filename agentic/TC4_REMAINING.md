# Thaumcraft 4.2.3.5 features not yet in NeoThaum 4

Last updated: 2026-10-28, after 1.0.7 (golem and boss AI ports, villages, bore, harness).
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
  - **Thaumatorium & Mnemonic Matrix:**
    - **Thaumatorium:** 2-block multiblock machine formed by wand-right clicking 2 Alchemical Constructs placed over a Crucible (consuming 15 Fire, 30 Order, 30 Water vis). Automated crucible alchemy: draws essentia via connected tubes (128 suction for required aspect) or remote mirrors, processes catalyst items in slot 0 without creating flux or slurry, vents steam with extinguish sounds, ejects products into front containers or into the world with velocity; front plate 3D cycling preview of active recipes; dedicated GUI (`ThaumatoriumMenu` / `ThaumatoriumScreen`) displaying recipe catalog, toggle buttons, progress bars, and aspect requirements.
    - **Alchemical Construct:** solid machine block (`alchemyblock` texture); arcane crafting recipe (Greatwood planks, tubes, valve, vis filter).
    - **Mnemonic Matrix:** attachable upgrade block (`brainbox` texture with connector neck) placing onto Thaumatorium sides; increases active recipe capacity by +2 per matrix; arcane crafting recipe (Zombie brain, amber, iron).
  - **Advanced Alchemical Furnace & Constructs:**
    - **Advanced Alchemical Furnace:** 3x3x2 multiblock structure formed by wand-right clicking the structure (1 Alchemical Furnace, 8 Advanced Alchemical Constructs on bottom layer; 4 Alchemical Constructs on sides, 4 Arcane Alembics on corners, center empty on top layer) consuming 50 Fire, 50 Water, 50 Order vis. High-speed distillation powered by centivis drained directly from energized nodes via the aura grid: Ignis (heat), Perditio (breakdown), and Aqua (stabilizing). 500-vis internal essentia capacity, 100% breakdown efficiency without flux slurry, and auto-smelts any items thrown down its central chimney; 4 outward-facing suction nozzles connect to tubes and jars; comparator signal output; 3D OBJ model (`adv_alch_furnace.obj`) with dynamic base texture switching (`alch_furnace_on`) and animated liquid tanks (`alch_furnace_tank_on`).
    - **Advanced Alchemical Construct:** casing block (`alchemyblockadv` texture); arcane recipe (`advalchemyconstruct.json`) using Void Ingot, Alchemical Construct, and Primordial Pearl.
    - **Eldritch Items:** Primordial Pearl (`primordial_pearl`, epic rarity, tooltip lore), Crimson Rites (`crimson_rites`, right-click research grant), Eldritch Eye (`eldritch_eye`), and Runed Tablet (`runed_tablet`).
  - **Node in a Jar Aspect Viewing:** trapped aura nodes now display floating 3D aspect tags when worn with Goggles of Revealing or inspected with Thaumometer, fully scannable and visible in HUD.
- **Infusion:** the matrix, pedestals, pillars, stabilisers and instability, and infusion enchanting (partial, see below).
- **Equipment:**
  - runic shielding and runic baubles, vis amulets, the hover girdle, elemental tools, thaumium tools and armour, and thaumaturge's robes.
  - **Thaumium Fortress Armor:** helm, cuirass, and thigh guards with heavy physical protection (40 durability, [3,7,6,3] defense), 25 enchantability, up to 25% cumulative damage reduction, Revealer Goggles upgrade attachment, and three mystical masks: Grinning Devil, Angry Ghost (withering retaliation on hit), and Sipping Fiend (lifesteal on melee strikes); infusion recipes.
  - **Thaumostatic Harness:** flight harness (`thaumostatic_harness`) fueled by Potentia essentia from an inserted warded jar via dedicated single-slot GUI (`HoverHarnessMenu`), 'H' keybind toggle with activation sounds (`hhon`/`hhoff`) and ambient buzzing (`jacobs`), flight speed scaling with Haste enchantment and the Thaumostatic Girdle, and infusion recipe (`hoverharness.json`).
  - **Void metal:** void tools (sword, shovel, pickaxe, axe, hoe) and void armour (helm, chestplate, leggings, boots) with warp, self-repair, and weakening strikes.
  - **Void Fortress Robes:** Void Thaumaturge Hood, Robe, and Leggings (`VoidRobeArmorItem`). Infusion upgrades from Void metal armor, 5% vis discount per piece, 2 warp per piece, continuous self-repair (1 hp / 20 ticks), dyeable with cauldron wash support, built-in Goggles of Revealing on hood, runic shielding augmentable.
  - **Cultist Equipment:** Complete Crimson Cult gear line:
    - **Cultist Robes:** Hood, Robe, and Leggings (`CultistRobeArmorItem`) with 1% vis discount and 1 warp per piece.
    - **Cultist Boots:** Heavy iron boots (`CultistBootsItem`) with 1% vis discount and 1 warp.
    - **Cultist Plate:** Knight's plate armor helm, chestplate, and greaves (`CultistPlateArmorItem`) with iron defense and runic shielding support.
    - **Cultist Leader Armor:** Praetor's plate armor helm, chestplate, and greaves (`CultistLeaderArmorItem`) with thaumium fortress defense (40 durability factor, [3,7,6,3] protection) and runic shielding support.
  - **Crimson Blade:** Jagged eldritch void sword (`CrimsonSwordItem`), 3.5 bonus attack damage, self-repairing, inflicts Weakness (3s) and Hunger (6s) on strike, warp 2, and "Greater Sapping" lore tooltip.
  - **Primal Arrows:** Six elemental arrows (`PrimalArrowItem` / `PrimalArrowEntity` / `PrimalArrowRenderer`): Air (armor piercing), Fire (5s burn), Water (Slowness IV), Earth (1.5x damage), Order (Weakness IV), and Entropy (Wither). Arcane shaped crafting with primal shards; aspect-colored flight particles.
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
- **Curatives, Fluids & Arcane Spa:**
  - **Liquid Death:** corrosive magenta fluid dealing 4 magic damage on touch, lavapop sound and witch particles, scooped via bucket (`bucket_of_liquid_death`), crucible recipe (mortuus 32, venenum 32, perditio 32).
  - **Purifying Fluid:** restorative silvery water granting Warp Ward when stepped into (duration scaled inversely to permanent warp), scooped via bucket (`bucket_of_purifying_fluid`).
  - **Purifying Bath Salts:** dissolves when used or tossed into water source blocks, transforming them into Purifying Fluid with bubbling sounds; crucible recipe.
  - **Sanitizing Soap:** washable bar that clears all temporary warp, and has a 33% chance (+25% with Warp Ward, +25% in Purifying Fluid) to reduce sticky warp; crucible recipe.
  - **Arcane Spa:** block with 5,000 mB fluid tank, bath salts slot, and mix toggle (`SpaBlockEntity` / `SpaMenu` / `SpaScreen`). Mixes water with bath salts to produce Purifying Fluid, automatically pumping it out to create a 5x5 rejuvenating bath pool above the spa; arcane crafting recipe (`arcanespa.json`).
  - **Deconstruction Table:** table machine (`DeconstructionTableBlock` / `DeconstructionTableBlockEntity` / `DeconstructionTableMenu` / `DeconstructionTableScreen`). Slot 0 takes items and breaks them down tick-by-tick (40 ticks) into primal aspects based on the item's vis breakdown, displaying a claimable primal aspect rune in GUI which clicks into the player's research aspect pool (`TVNetwork.sendAspectPool`); arcane crafting recipe (`deconstructor.json`).
- **Mirrors & Dimensional Transport:**
  - **Magic Mirror:** 6-face wall/floor/ceiling mounting, 1-pixel thin frame; paired linkage via right-clicking placed mirror with an unlinked mirror item. Transports dropped item entities and hopper-inserted stacks across dimensions to destination mirror with portal particles and sound (`portal.travel`); destination mirror queues and dispenses items outward.
  - **Essentia Mirror:** 6-face mounting, paired linkage; implements `AspectSource`, remotely querying and draining essentia via `EssentiaHandler.drainEssentia` within an 8-block radius of the destination mirror across dimensions for Infusion Altar matrices and Crucible alchemy.
  - **Hand Mirror:** paired to a Magic Mirror; right-clicking in air opens 1-slot GUI (`HandMirrorMenu` / `HandMirrorScreen`) allowing items placed in the slot to be transported directly home to the linked Magic Mirror anywhere in the world.
  - Infusion recipes for Magic Mirror, Essentia Mirror, and Hand Mirror.
  - **Arcane Bore & Base:**
    - **Arcane Bore Base:** Turret mount oriented by wand or placement, with suction 128 pulling Perditio essentia through tube network, and ejection nozzle directing mined block drops into adjacent inventories (chests/hoppers) or dispensing forward into the world.
    - **Arcane Bore:** Redstone-activated rotating drilling turret mounted above or below the base. Holds an Excavation Focus and Pickaxe tool (`ArcaneBoreMenu` / `ArcaneBoreScreen`). Casts an excavation ray with spiral dig pattern up to 64 blocks, harvesting dropped items, degrading tool durability based on block hardness, and applying tool enchantments (Fortune, Silk Touch, Efficiency, and Native Clusters). Wand right-click reorients yaw/pitch smoothly with animated turret rotation.
    - Arcane shaped recipe for Arcane Bore Base and Infusion recipe for Arcane Bore.
- **Mana Pods & Log Planting:**
  - **Mana Pods:** Hanging plant block (`ManaPodBlock` / `ManaPodBlockEntity` / `ManaPodRenderer`) growing from the underside of logs in magical biomes across 8 growth stages (0..7), emitting light matching its age. Right-clicking the underside of logs with Mana Beans plants them.
  - **Aspect Breeding & Dynamics:** Cross-breeds aspects from adjacent horizontal mana pods at stage 3, producing compound aspect beans from parental combinations (`Aspect.getCombination`). Right-clicking a mature pod or harvesting yields aspect-infused Mana Beans.
  - **Rendering:** Exact 1:1 TC4 visual model with hanging stem, pulsing glowing inner core at stage 3+ with sine-wave scale animation, and translucent outer pod shell blending from pale green to aspect-specific color.
  - **Worldgen:** Generates naturally hanging from logs throughout the Magical Forest biome (`TCBiomeDecorFeature.generateManaPods`).
- **Flux Scrubber & Banners:**
  - **Flux Scrubber:** Directional artifice device (`FluxScrubberBlock` / `FluxScrubberBlockEntity` / `FluxScrubberRenderer`). Connects to Aer centivis via vis network (5 cv/cleansing action), scans 16-block cuboid for `FLUX_GOO` and `FLUX_GAS` blocks, dissolves flux with sparkle effects, accumulates charges, converts every 4 charges into Praecantatio liquid essentia (up to 4), and outputs into tube networks via suction nozzle. 3D OBJ model (`obelisk_cap.obj`) with animated bobbing floating tip.
  - **Banners:** Decorative hanging flags (`BannerBlock` / `BannerBlockEntity` / `BannerRenderer` / `BannerItem`). Mounts on floors with 16 rotation angles or walls with directional facing. Shift-right-click clears aspect, essentia phial stamps aspect insignia with cloth audio, drops `BannerItem` preserving dye color and aspect. Features 16 Thaumcraft dye colors plus the Cultist Banner. Animated cloth swaying in wind.
- **Void Fortress Robes, Cultist Gear, Crimson Blade & Primal Arrows:**
  - **Void Fortress Robes:** Void Thaumaturge Hood, Robe, Leggings (`VoidRobeArmorItem`). 5% vis discount and 2 warp per piece, passive self-repair (1 hp / 20 ticks), dyeable with cauldron washing, built-in Goggles of Revealing on hood (`showNodes`), augmentable with runic shielding. Infusion crafting recipes with instability 6.
  - **Cultist Equipment:** Cultist Robes (`CultistRobeArmorItem`: hood, robe, leggings; 1% discount, 1 warp), Cultist Boots (`CultistBootsItem`: 1% discount, 1 warp), Cultist Plate (`CultistPlateArmorItem`: knight helm, chest, greaves; iron defense, runic shielding), Cultist Leader Armor (`CultistLeaderArmorItem`: praetor helm, chest, greaves; thaumium fortress defense, runic shielding).
  - **Crimson Blade:** Jagged eldritch void sword (`CrimsonSwordItem`), 3.5 bonus damage, self-repairing, inflicts Weakness (3s) and Hunger (6s) on strike, 2 permanent warp, and "Greater Sapping" lore tooltip.
  - **Primal Arrows:** Six elemental arrows (`PrimalArrowItem` / `PrimalArrowEntity` / `PrimalArrowRenderer`): Air (armor piercing), Fire (5s ignite), Water (Slowness IV), Earth (1.5x damage), Order (Weakness IV), Entropy (Wither 5s). Aspect trail particles, `#minecraft:arrows` tag, arcane shaped crafting recipes.
- **Throwables & Small Items:**
  - **Throwing Alumentum:** `AlumentumEntity` throwable projectile extending `ThrowableItemProjectile` (velocity 0.75F, trailing wisp and sparkle particles, 1.66F explosion on impact respecting `mobGriefing`). Thrown via `AlumentumItem` with bow sound, and compatible with vanilla dispensers via `ProjectileItem`. Retains 6,400 tick cooking fuel.
  - **Loot Bags:** Common, Uncommon, and Rare treasure bags (`LootBagItem`). Dropped by champions, cultists, and eldritch guardians. Right-click drops 8-12 items from TC4's authentic weighted loot table (gold coins, eldritch eyes, nether stars, gems, baubles, enchanted books, golden apples, potions, and enchanted gear) with `coins` sound.
  - **Knowledge Fragments:** Right-click consumes 1 fragment and grants 1-2 points to all 6 primal aspect pools in the player's research pool (`KnowledgeFragmentItem`). 9 fragments crafted 3x3 into `research_notes` (`knowfrag.json`).
  - **Sinister Lodestone:** Attuned stone compass (`SinisterLodestoneItem`). Tracks Sinister nodes within 256 blocks, testing the player's view cone (0.66F aperture ≈ 38°); dynamically switches model to glowing active state (`sinister_stone_active`) via `TVComponents.ACTIVE`. Harbors 1 point of permanent warp. Infusion recipe (`sinstone.json`).
- **Golemancy:**
  - **Golems & Cores:** Straw, Wood, Tallow, Clay, Flesh, Stone, Iron, Thaumium, and Copper golems with material-specific stats (HP, armor, fire immunity, speed, carrying capacity). Animation cores (Blank, Fill, Empty, Gather, Harvest, Guard, Decanting, Alchemy, Chop, Use, Butcher, Sorting, Fishing).
  - **Copper Golem Specialization:** Dedicated metallic tier with dynamic oxidation (Unaffected, Exposed, Weathered, Oxidized) scaling movement speed; temporary waxing with beeswax (decays over time) and permanent mystical sealing with Magic Tallow; axe scraping to de-oxidize or strip wax; redstone button/lever interaction with Use Core; mobile vis accumulator charging held and pedestal wands during thunderstorms or while supercharged; vanilla spawning disabled via block pattern mixin; authentic Thaumonomicon entry (`GOLEMCOPPER`), crucible recipe, and live GUI title status (`[Oxidation • Wax Status ⚡]`).
  - **Upgrades & Accessories:** Primal elemental upgrades (Air, Earth, Fire, Water, Order, Entropy) and cosmetic accessories with functional attributes (Top Hat, Spectacles, Bowtie, Fez, Dart Launcher with ranged darts, Visor, Iron Plating, Mace Arm with heavy knockback).
  - **Rendering & Visuals:** Exact 1:1 `ModelGolem` (128x128 texture, walking swing, carrying poses, bootup sequence), `golem_decoration.png` accessory overlay, slotted chest core gem layer glowing with signature core colors, cracked damage layer proportional to health loss, held item rendering.
  - **Configuration GUI:** `guigolem.png` interface with 6 filter ghost slots per page, slot syncing, Order upgrade color markers (16 dye colors), scroll tabs, Guard checkbuttons, Fill amount selector, Use toggles, and localized blurbs.
  - **AI Behaviors:** Item pickup/delivery, crop harvesting/replanting, tree lumberjacking, guard targeting (hostiles/animals/players/creepers) with ranged dart shooting and mace melee, butcher preserving pairs, chest restocking/emptying, and home container returning.
  - **Tools:** Golemancer's Bell (`GolemBellItem`) for linking golems to home containers and directional faces, marker placement, and picking up golems.
  - **Traveling Trunk:** Sentient companion chest (`TravelingTrunkEntity`) with 75 HP, fire and fall damage immunity, following/teleporting owner, 27/36 slot inventory (`GENERIC_9x3` / `GENERIC_9x4`), upgrade traits (Air speed, Earth extra row, Fire guard, Water lock & damage reduction, Order item preservation on pickup, Entropy vacuum suction), food feeding healing, and animated chest lid. Spawner item (`traveling_trunk` / `trunk_spawner`).
- **Eldritch Mobs & Cultists:**
  - **Crimson Knight:** Melee armored cultist (`CrimsonKnightEntity` / `CultistRenderer.knight`) with heavy cultist plate, sword, door opening, and alliance mechanics.
  - **Crimson Cleric:** Caster cultist (`CrimsonClericEntity` / `CultistRenderer.cleric`) launching fireballs, homing eldritch orbs, and chanting ritual buffs.
  - **Crimson Praetor (Cultist Leader):** Colossal 125 HP boss (`CrimsonLeaderEntity` / `CultistRenderer.leader`), unique historical titles, boss bar, Strength II cultist aura, 35-damage cap with enrage phase, and Rare Loot Bag drop.
  - **Eldritch Crab:** 20 HP skittering arthropod (`EldritchCrabEntity` / `EldritchCrabRenderer`), helmet armor (+5 defense) popping off at <= 50% HP, face-hugger head leaping, and crab claw attacks.
  - **Inhabited Zombie:** 30 HP cultist-armored zombie (`InhabitedZombieEntity` / `ZombieRenderer`), erupting into a helmeted Eldritch Crab on death with explosive particles.
  - **Eldritch Warden:** 200 HP Outer Lands boss (`EldritchWardenEntity` / `EldritchWardenRenderer`), Lovecraftian titles, runic shielding, sonic screech with temporary warp affliction, and Primordial Pearl drop.
  - **Eldritch Golem:** 250 HP Outer Lands boss (`EldritchGolemEntity` / `EldritchGolemRenderer`), knockback immunity, fling attacks, headless phase transition on near-death with electric neck-beam barrages, and Primordial Pearl drop.
  - **Golem Orb:** Homing eldritch projectile (`GolemOrbEntity` / `FocusProjectileRenderers.GolemOrbRenderer`) with magic shock damage and colored particle trails.
- **Warp System & Mechanics:**
  - **Periodic Warp Check:** Server-tick evaluator running every 2,000 ticks (100s) on active players without Warp Ward (`WarpEvents.checkWarpEvent`). Total warp calculates permanent + sticky + temporary + held/worn `WarpingGear`.
  - **Counter & Decay:** Dynamic `warpCounter` with square-root threshold triggering; decays by $\max(5, \lfloor\sqrt{\text{warpCounter}}\times 2\rfloor)$. Automatic passive temporary warp decay (-1 per 2,000 ticks).
  - **Hallucinations & Afflictions:** Whispers granting primal research aspects (`warp.text.3`), ambient paranoia sounds (`warp.text.11`, `warp.text.12`, `warp.text.13`), Vis Exhaustion, Thaumarhia, Unnatural Hunger (curable with rotten flesh/zombie brain), Sun Scorned (ignites in sunlight), Death Gaze (withers looking entities), Blurred Vision, Nightmares, and moments of clarity (cures 1 sticky warp).
  - **Entities:** Mind Spiders (`MindSpiderEntity`, 0.3 scale, harmless hallucinations visible only to afflicted player or real), and Eldritch Guardians (`EldritchGuardianEntity`, 50 HP, magic resistance, ranged wither blast, and screech attack).
  - **Research Milestones:** Automatic unlocking of `ELDRITCHMINOR` (>10 actual warp), `ELDRITCHWARPED` (>25 actual warp), and `ELDRITCHMAJOR` (>50 actual warp).
- **Polish and immersion details:**
  - **Aspect orbs:** `AspectOrbEntity` with aspect-tinted billboard glow, gravitation toward player, filling wand vis & knowledge pools, dropped by mobs on kill via `AspectOrbEvents` and unstable node bleed.
  - **Node lightning strikes:** uncontained unstable, hungry, and dark nodes strike nearby living entities with electric bolts and magic damage.
  - **Shovel of the Earthmover:** G key orientation cycling (parallel, vertical, horizontal) with chat display and click sound; connected texture warded glass placement preview in `ArchitectOverlay`.
  - **Configuration:** `config/neothaum.json` managed by `NeoThaumConfig` (taint spread, hard nodes, node rarity, aspect orb drop rate, warp toggle, wisp spawn rate, champion mobs).
  - **Advancements:** authentic TC4 advancement tree (`root`, `goggles`, `crucible`, `infusion`, `golem`, `warp`, `eldritch`).
- **Other items:** mana beans, ethereal essence.
- **Addon features (in `dev.neothaum.addon`):**
  - Vanilla candles as tallow candles (interchangeable for infusion altar stabilization, 1:1 color exchange crafting recipes, Thaumonomicon entry in tallow node).

## Still to do

Re-audited 2026-10-28 (after 1.0.7) by comparing TC4's registered entities, items, blocks, research, sounds, potions, world generation and
commands with NeoThaum's. Everything in the old list (Outer Lands, eldritch structures, cultist portal, champions, barrows, hilltop stones,
villages, ethereal bloom) is done. What is left:

### World generation (S-M)
- **Obsidian totems:** ThaumcraftWorldGenerator.generateTotem, in the overworld and the Nether (1 in nodeRarity x 10 chunks): an obsidian
  tile base, an obsidian totem pillar, and a *charged* obsidian totem (blockCosmeticSolid 8) with an aura node in it. Neither the
  generation nor the charged totem block exists.
- **Nodes over vanilla structures:** TC4 put a random aura node 3 blocks above every scattered feature (desert and jungle temples, witch
  huts) the first time a chunk near it generated.

### Small behaviour and polish (S)
- **EntitySpecialItem:** boss pearls, crucible products and wand-made items (the Thaumonomicon from a bookshelf) float gently upward and
  ignore explosions; NeoThaum drops ordinary items.
- **Sounds:** the cultist portal's ambient "monolith" hum (every 540 ticks), loot urns' "urnbreak" break sound, and the crucible's "spill"
  sound when items dissolve (NeoThaum plays a bubble instead).
- **Eldritch Obelisk Placer** (creative/debug item, ItemEldritchObject 4).

### Commands (S)
- **/thaumcraft:** research list / give / all / reset, aspect add, warp add / set, for a player. NeoThaum registers no commands.

### Cross-mod (M, optional)
- **Native tin, silver and lead clusters** and the tin/silver/lead nuggets, with the PURETIN/PURESILVER/PURELEAD and TRANS* research,
  active only when another mod supplies those ores (c: tags). The research entries exist but their recipe pages are placeholders.

### Test coverage still missing
- **Gametests** for the elemental tools and baubles, and Pech trading.
- **Not ported on purpose:** TC4's Frugal, Potency and Wand Fortune enchantments only applied to the old focus API that no 4.2 focus used.

## Suggested order

1. **/thaumcraft command.**
2. **Sounds and the Eldritch Obelisk Placer.**
3. **Floating special items.**
4. **Obsidian totems and nodes over vanilla structures.**
5. **Native tin, silver and lead clusters (cross-mod).**

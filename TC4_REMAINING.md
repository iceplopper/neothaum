# Thaumcraft 4.2.3.5 features not yet in Thaumvibe 4

Last updated: 2026-10-26, after node jars and node effects on land.
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
- **Essentia:**
  - warded and void jars, labels and the brain in a jar
  - every tube kind and the buffer
  - the alembic, alchemical furnace and bellows
  - phials, plus your suction priority rules
- **Infusion:** the matrix, pedestals, pillars, stabilisers and instability, and infusion enchanting (partial, see below).
- **Equipment:** runic shielding and runic baubles, vis amulets, the hover girdle, elemental tools, thaumium tools and armour, and thaumaturge's robes.
- **Flux:** flux goo and gas, all nine potion effects, and basic warp gain.
- **Biomes:** Magical Forest, Tainted Land and Eerie, plus taint blocks and spread.
- **Mobs:**
  - angry and furious zombies
  - wisp, firebat, Pech (with trading), thaumic slime
  - taint spore, swarmer, swarm and crawler
  - taintacle (small, normal and giant)
  - tainted chicken, cow, pig, sheep, villager and creeper
- **Other items:** mana beans, ethereal essence.

## Still to do

### Alchemy and essentia, the rest (M)
- **Machines:** the Thaumatorium (multi-recipe alchemy), the alchemical centrifuge and the essentia crystallizer.
- **Storage:** the essentia reservoir and the advanced alchemical furnace.
- **Mirrors:** the magic mirror, essentia mirror and hand mirror.
- **Fluid buckets:** bucket of death and bucket of pure (liquid death, purifying fluid).
- **Cures:** sanitizing soap and bath salts.
- **Mana pods** (block and worldgen), and planting mana beans on logs to grow them.

### Artifice and utility blocks (M)
- **Lamps:** arcane lamp, lamp of growth and lamp of fertility.
- **Doors, warding and redstone:**
  - arcane door, warded glass and warding stone
  - arcane pressure plate and arcane ear
  - arcane levitator
- **Big machines:** the arcane bore and its base, and the infernal (arcane) furnace multiblock.
- **Storage:** the hungry chest.
- **Spa and deconstruction:** the arcane spa and the deconstruction table.
- **Misc:** the flux scrubber, the grate, and banners (cultist and the Thaumcraft colours).

### Equipment and gear (M to L)
- **Void metal:** void tools, armour and robe, with warp and self-repair.
- **Fortress armour:** its custom model, masks and goggles upgrades.
- **Boots of the Traveller** and the thaumostatic harness.
- **Weapons:** the crimson blade, bone bow and primal arrows, and the primal crusher.
- **Cultist gear:** cultist robes, plate and leader armour, and cultist boots.
- **Relics:** the resonator and the sanity checker.
- **Throwables:** bottled taint, and throwing alumentum.
- **Small items:** the loot bag, the iron and gold arcane keys, the stone compass, and using knowledge fragments.

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
- **The sanity checker** display.

### World generation, the rest (M)
- **Structures:** barrow mounds with loot urns and crates, hilltop stones, and eldritch rings.
- **Villages:** the Thaumcraft villages' wizard tower and banker (moneychanger) houses.
- **Ethereal bloom** (a taint-cleansing plant).

### Polish and integration (S to M)
- **Aspect orbs** dropped by mobs on death and by the primal charm.
- **Lightning** from nodes to players (nodeBolt).
- **The stone compass's** pull toward sinister nodes (TC4 records them from the node tick; nothing reads that yet).
- **Shovel of the Earthmover:** the orientation keybind and placement preview.
- **Missing Thaumonomicon pages:** helm goggles and mask infusions, runic augmentation.
- **JEI plugin:** arcane, crucible and infusion categories, plus aspect lookups.
- **Config file:** the TC4 options (spawn toggles, taint spread rate, biome weights and so on).
- **Achievements and advancements.**

### Test coverage still missing
- **Gametests** for the elemental tools and baubles, and Pech trading. (Taint conversion, peaceful rules, wands, foci and the vis network
  have scenes now.)
- **Not ported on purpose:** TC4's Frugal, Potency and Wand Fortune enchantments only applied to the old focus API that no 4.2 focus used;
  focus upgrades replaced them.

### Addon features (new, not in TC4; they go in their own package, `dev.thaumvibe.addon`)
- **Vanilla candles as tallow candles.** Keep TC4's tallow candles, and let vanilla candles stand in for them wherever tallow candles
  count (they stabilise infusion altars). A vanilla candle only counts while lit, and stacking more candles on one block adds nothing.
  - **Exchange recipes:** a candle alone in the crafting grid becomes the other kind of the same colour, both ways, for every colour
    (and the plain undyed candle).
  - **Thaumonomicon:** a page in the tallow entry explaining this, showing the vanilla candle recipe and the exchange recipe cycling
    through the colours.

## Suggested order

1. **Void metal and the remaining gear.**
2. **Artifice blocks and the remaining alchemy machines.**
3. **Golemancy.**
4. **Warp events.**
5. **Eldritch content and the Outer Lands.**
6. **JEI, config and polish.**

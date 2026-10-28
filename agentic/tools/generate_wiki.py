import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"
os.makedirs(WIKI_DIR, exist_ok=True)

# Define the structure of pages: (Filename, Title, Category, Summary, Sections, RelatedPages)
PAGES = [
    # Meta / Core
    (
        "Getting-Started.md",
        "Getting Started with NeoThaum",
        "Getting Started",
        "Your first steps into Thaumaturgy: crafting your first wand, creating the Thaumonomicon, and unlocking the mysteries of the arcane.",
        [
            ("1. Crafting Your First Wand", "How to assemble the Iron-Capped Wooden Wand using an iron nugget, stick, and workbench."),
            ("2. Obtaining the Thaumonomicon", "Crafting Salis Mundus (World Salt) and using it on a mundane Bookshelf to reveal the ancient research grimoire."),
            ("3. Crafting the Thaumometer", "Assembling gold nuggets and a glass pane into a Thaumometer to scan objects and begin discovering aspects."),
            ("4. Setting Up the Research Table", "Placing two tables side-by-side and right-clicking with Scribing Tools to build a dedicated Research Table."),
            ("5. Goggles of Revealing", "Crafting the iconic brass goggles to view Aura Node aspects and jar contents in real-time.")
        ],
        ["Aspects-Overview", "Research-Mechanics", "Wands-and-Staffs", "Home"]
    ),
    (
        "Configuration-&-Settings.md",
        "Configuration & In-Game Settings",
        "Getting Started",
        "Detailed explanation of NeoThaum's customizable settings in `config/neothaum.json` and how to adjust them via ModMenu.",
        [
            ("Aura Node Settings", "Global spawn rarity, variant rates (Dark, Unstable, Pure, Hungry, Tainted), and aspect richness multipliers."),
            ("Biome Generation", "Toggling generation and configuring size scales for Magical Forests and Tainted Lands."),
            ("Structure Frequency", "Configuring spawn rates for Eldritch Rings, Barrow Mounds, Hilltop Stones, Wizard Towers, and Banker Houses."),
            ("Taint Spread & Corruption", "Enabling/disabling taint spread, adjusting spread tick intervals, and choosing whether taint corrupts existing mobs or spawns fresh tainted beasts."),
            ("Flux & Alchemy Multipliers", "Flux spill scaling, Infernal Furnace smelting speed, and bonus nugget drop rates."),
            ("Creative & Debug Tools", "Toggling the visibility of the internal debug creative tab.")
        ],
        ["Getting-Started", "Mod-Compatibility", "Home"]
    ),
    (
        "Mod-Compatibility.md",
        "Mod Compatibility & Integrations",
        "Getting Started",
        "Overview of third-party companion mod integrations supported natively by NeoThaum on Fabric.",
        [
            ("Just Enough Items (JEI)", "Integration details for Arcane Worktable recipes, Crucible Alchemy reactions, and Infusion Altar rituals."),
            ("Jade / Waila HUD", "In-world tooltip displays for Aura Nodes (with Goggles of Revealing), Crucible heat/essentia levels, and Jar capacities."),
            ("ModMenu", "In-game settings screen configuration and credits."),
            ("Xaero's World Map", "Dedicated toggleable map button for viewing discovered Aura Nodes on the global map."),
            ("Xaero's Minimap", "Minimap entity radar support for tainted mobs and champion entities.")
        ],
        ["Configuration-&-Settings", "Home"]
    ),
    (
        "Troubleshooting-&-FAQ.md",
        "Troubleshooting & FAQ",
        "Help & Support",
        "Frequently asked questions, common setup issues, and debugging tips.",
        [
            ("Common Issues", "Wand not charging, Crucible boiling requirements, Infusion instability explosions."),
            ("Crash Reports & Logs", "Where to find logs and what information to provide when reporting bugs."),
            ("Known Limitations", "Differences between TC4 1.7.10 and modern Minecraft Fabric architecture.")
        ],
        ["Getting-Started", "Home"]
    ),

    # Aspects
    (
        "Aspects-Overview.md",
        "Aspects of Magic Overview",
        "Aspects of Magic",
        "Aspects are the fundamental building blocks of all matter, energy, and magic in the Thaumic universe.",
        [
            ("What are Aspects?", "Explanation of primal vis vs compound aspects."),
            ("Discovering Aspects", "Scanning objects, items, and mobs with the Thaumometer to expand your aspect knowledge pool."),
            ("Research Aspect Pool", "How aspects are stored in the player's thaumic knowledge and consumed in research hexes."),
            ("Aspect Essentia", "Liquid physical manifestation of aspects used in alchemy and infusion.")
        ],
        ["Primal-Aspects", "Compound-Aspects-Chart", "Scanning-&-Thaumometer", "Home"]
    ),
    (
        "Primal-Aspects.md",
        "The Six Primal Aspects",
        "Aspects of Magic",
        "The six primordial elements from which all compound aspects and thaumaturgical phenomena originate.",
        [
            ("Aer (Air)", "The aspect of wind, flight, speed, and ether."),
            ("Terra (Earth)", "The aspect of soil, stone, foundation, and stability."),
            ("Ignis (Fire)", "The aspect of heat, flame, destruction, and energy."),
            ("Aqua (Water)", "The aspect of fluidity, oceans, life, and cooling."),
            ("Ordo (Order)", "The aspect of symmetry, purity, logic, and structure."),
            ("Perditio (Entropy)", "The aspect of chaos, decay, breakdown, and dissolution.")
        ],
        ["Aspects-Overview", "Compound-Aspects-Chart", "Home"]
    ),
    (
        "Compound-Aspects-Chart.md",
        "Compound Aspects & Combination Chart",
        "Aspects of Magic",
        "Complete reference tree showing how compound aspects are formed from primal and lower-tier aspects in TC 4.2.3.5.",
        [
            ("Tier 1 Compounds", "Lux (Light), Motus (Motion), Gelum (Cold), Victus (Life), Vacuos (Void), Potentia (Power)..."),
            ("Tier 2 Compounds", "Bestia (Beasts), Fames (Hunger), Herba (Flora), Metallum (Metal), Mortuus (Death), Praecantatio (Magic)..."),
            ("Tier 3 Compounds", "Iter (Travel), Limus (Slime), Sano (Heal), Tenebrae (Darkness), Vinculum (Trap)..."),
            ("Tier 4 Compounds", "Alienis (Eldritch), Cognitio (Mind), Instrumentum (Tool), Messis (Crops), Sensus (Senses)..."),
            ("Tier 5+ Compounds", "Corpus (Flesh), Exanimis (Undead), Humanus (Human), Lucrum (Greed), Machina (Mechanism)..."),
            ("Full Aspect Grid Reference", "Ascii / Markdown table mapping every aspect to its exact formula.")
        ],
        ["Aspects-Overview", "Primal-Aspects", "Research-Mechanics", "Home"]
    ),
    (
        "Scanning-&-Thaumometer.md",
        "Scanning & The Thaumometer",
        "Basic Information & Research",
        "The primary tool for uncovering the aspect composition of the world.",
        [
            ("Crafting the Thaumometer", "Recipe and usage basics."),
            ("Scanning Mechanics", "Point-and-click scanning of blocks, dropped items, and living entities."),
            ("Scan Prerequisites", "Why certain objects cannot be scanned until their prerequisite component aspects are discovered."),
            ("Research Point Yields", "How scanning rewards players with primal and compound aspect research points.")
        ],
        ["Aspects-Overview", "Research-Mechanics", "Home"]
    ),
    (
        "Research-Mechanics.md",
        "Research Mechanics & The Thaumonomicon",
        "Basic Information & Research",
        "The cerebral minigame of linking aspect runes on hex parchment to unlock ancient knowledge.",
        [
            ("The Research Table", "Setting up tables with Scribing Tools and Paper."),
            ("Research Notes", "Selecting uncompleted research from the Thaumonomicon."),
            ("The Hex Grid Mini-Game", "Rules of connecting starting aspect nodes using chain links of parent/child compound aspects."),
            ("Warp from Research", "Forbidden Knowledge entries marked with purple borders and sticky/permanent warp gain.")
        ],
        ["Scanning-&-Thaumometer", "Compound-Aspects-Chart", "Warp-Mechanics", "Home"]
    ),
    (
        "Aura-Nodes.md",
        "Aura Nodes",
        "Basic Information & Research",
        "Mystical fonts of ambient vis suspended throughout the world, invisible to the naked eye.",
        [
            ("Detecting Nodes", "Using the Thaumometer or Goggles of Revealing to locate and inspect nodes."),
            ("Node Types", "Normal, Pure (warding taint), Dark (sinister aura), Unstable (shifting aspects), Hungry (gravitational pull), Tainted."),
            ("Node Modifiers", "Bright (fast recharge), Normal, Pale (slow recharge), Fading (decaying vis)."),
            ("Jarring Nodes", "Using glass panes, wooden slabs, and wand vis to encapsulate a node for transport."),
            ("Node Energizing & Transducers", "Stabilizing and transducing a node into an energized centi-vis (cV) network.")
        ],
        ["Node-Tapping-&-Recharging", "Getting-Started", "Home"]
    ),
    (
        "Node-Tapping-&-Recharging.md",
        "Wand Recharging & Node Management",
        "Basic Information & Research",
        "Methods for replenishing primal vis in wands, staves, and scepters.",
        [
            ("Direct Node Draining", "Holding right-click on an aura node to siphon primal vis into your wand."),
            ("Preventing Node Depletion", "Why you must never drain a node's last vis point (risk of fading or destruction)."),
            ("Recharge Pedestals", "Passive wand recharging placed atop or near energized vis networks."),
            ("Vis Relays & Piping", "Distributing energized cV across your thaumaturgical workshop.")
        ],
        ["Aura-Nodes", "Wands-and-Staffs", "Home"]
    ),

    # Thaumaturgy
    (
        "Wands-and-Staffs.md",
        "Wands, Scepters & Staffs",
        "Thaumaturgy",
        "The primary casting tools of the Thaumaturge, capable of manipulating world blocks, crafting in arcane tables, and discharging focuses.",
        [
            ("Wand Rods", "Wood (25 vis), Greatwood (50 vis), Silverwood (100 vis)."),
            ("Wand Caps", "Iron, Gold, Copper, Silver, Thaumium, Void Metal (vis discounts from 110% down to 80%)."),
            ("Casting Scepters", "Specialized non-focus crafting wands holding 1.5× vis with built-in discount bonus."),
            ("Combat Staves", "Two-handed battle staves holding 2.5× vis capable of striking enemies for high physical and magical damage."),
            ("Assembling Wands", "Arcane Worktable recipes and wand assembly.")
        ],
        ["Wand-Focuses", "Vis-Storage-&-Discount-Armor", "Home"]
    ),
    (
        "Wand-Focuses.md",
        "Wand Focuses",
        "Thaumaturgy",
        "Interchangeable crystals that channel a wand's stored vis into offensive, defensive, and utility spells.",
        [
            ("Equipping & Radial Selector", "Holding the Focus pouch key / 'F' key to swap active focuses on the fly."),
            ("Focus: Fire", "Projecting streams of incendiary flame."),
            ("Focus: Frost", "Launching freezing crystalline orbs that slow and damage foes."),
            ("Focus: Shock", "Discharging instant chain lightning arcs."),
            ("Focus: Excavation", "Rapidly mining stone, dirt, and minerals at range."),
            ("Focus: Portable Hole", "Creating temporary trans-dimensional passages through solid walls."),
            ("Focus: Equal Trade", "Swapping surface blocks in a wide radius with blocks from your inventory."),
            ("Focus: Hellbat & Primal", "Summoning explosive nether bats and unleashed primal elemental storms."),
            ("Focus: Warding", "Rendering blocks completely indestructible and tamper-proof.")
        ],
        ["Wands-and-Staffs", "Focal-Manipulator", "Home"]
    ),
    (
        "Focal-Manipulator.md",
        "The Focal Manipulator",
        "Thaumaturgy",
        "An advanced arcane device used to customize, upgrade, and enchant wand focuses with branching upgrade paths.",
        [
            ("Constructing the Manipulator", "Recipe and placement."),
            ("Upgrade Trees", "Potency, Frugal (vis reduction), Range, Enlarged Area, and element-specific alterations."),
            ("XP & Vis Costs", "Experience level requirements and vis consumption during focus enchantment.")
        ],
        ["Wand-Focuses", "Home"]
    ),
    (
        "Vis-Storage-&-Discount-Armor.md",
        "Vis Storage & Discount Robes",
        "Thaumaturgy",
        "Protective garments and magical trinkets that reduce vis expenditure and buffer primal vis.",
        [
            ("Thaumaturge's Robes", "Crafting fabric infused with magic to grant vis discounts across all primal types."),
            ("Vis Purifying Helmets & Goggles", "Goggles of Revealing integration with helmets."),
            ("Amulet of Vis", "Wearable bauble that stores vis and automatically recharges carried wands.")
        ],
        ["Wands-and-Staffs", "Home"]
    ),

    # Alchemy
    (
        "Crucible-Alchemy.md",
        "Crucible Alchemy",
        "Alchemy",
        "Transmuting mundane materials into enchanted goods by dissolving items into boiling alchemical water.",
        [
            ("Constructing the Crucible", "Right-clicking a vanilla Cauldron with a wand."),
            ("Heating & Boiling", "Placing fire, lava, or a Nitor block beneath the Crucible."),
            ("Adding Aspect Ingredients", "Throwing items into the bubbling cauldron to dissolve their constituent aspects."),
            ("Throwing the Catalyst", "Tossing the final catalyst item to claim the transmutation output."),
            ("Aspect Waste & Taint Flux", "Why unused aspects boil away into dangerous purple flux gas and flux slime.")
        ],
        ["Essentia-Distillation", "Thaumatorium", "Home"]
    ),
    (
        "Essentia-Distillation.md",
        "Essentia Distillation",
        "Alchemy",
        "Extracting pure liquid Essentia from physical objects using heat and alembic condensers.",
        [
            ("Alchemical Furnace", "Smelting items down into raw aspect slurry."),
            ("Arcane Alembics", "Stacking alembics above the furnace to condense individual aspect vapors."),
            ("Extracting into Phials & Jars", "Right-clicking alembics with glass phials or warded jars to harvest pure liquid essentia.")
        ],
        ["Crucible-Alchemy", "Essentia-Storage", "Essentia-Transport-&-Piping", "Home"]
    ),
    (
        "Essentia-Transport-&-Piping.md",
        "Essentia Transport & Piping",
        "Alchemy",
        "Piping systems for routing liquid essentia across your laboratory.",
        [
            ("Essentia Tubes", "Basic transport pipes."),
            ("Suction Mechanics", "How untyped vs aspect-filtered suction dictates fluid flow."),
            ("Valves & Directional Tubes", "Restricting backflow and manual toggling."),
            ("Essentia Buffers", "Balancing disparate suction pressures and multiple pipe inputs."),
            ("Alchemical Centrifuge", "Breaking compound essentia down into constituent simpler aspects.")
        ],
        ["Essentia-Distillation", "Essentia-Storage", "Thaumatorium", "Home"]
    ),
    (
        "Essentia-Storage.md",
        "Essentia Storage",
        "Alchemy",
        "Vessels for safeguarding and organizing harvested essentia.",
        [
            ("Glass Phials", "8 essentia portion containers used in recipes."),
            ("Warded Jars", "64 essentia containment jars displaying labels and aspect levels."),
            ("Void Jars", "Specialized jars that automatically void excess essentia when full to prevent system clogging."),
            ("Aspect Labels", "Affixing labels to designate jars for specific aspects only.")
        ],
        ["Essentia-Distillation", "Essentia-Transport-&-Piping", "Home"]
    ),
    (
        "Thaumatorium.md",
        "The Thaumatorium",
        "Alchemy",
        "An advanced multiblock construct that automates crucible recipes cleanly without flux spill.",
        [
            ("Multiblock Assembly", "Alchemical Construct, Crucible, and wand activation."),
            ("Selecting Recipes", "Opening the Thaumatorium GUI to pick known crucible formulas."),
            ("Piping In Essentia", "Connecting essentia tubes to supply required aspects automatically."),
            ("Automated Catalyst Feeder", "Dropping catalysts via hoppers for hands-free manufacturing.")
        ],
        ["Crucible-Alchemy", "Essentia-Transport-&-Piping", "Home"]
    ),
    (
        "Alchemical-Centrifuge-&-Crystallizer.md",
        "Centrifuge & Crystallizer",
        "Alchemy",
        "Specialized alchemical constructs for refining and solidifying essentia.",
        [
            ("The Alchemical Centrifuge", "Deconstructing compound essentia into its two component aspects."),
            ("The Essentia Crystallizer", "Solidifying liquid essentia into crystalline aspect shards for compact storage.")
        ],
        ["Essentia-Transport-&-Piping", "Essentia-Storage", "Home"]
    ),

    # Artifice
    (
        "Arcane-Worktable.md",
        "The Arcane Worktable",
        "Artifice",
        "A 3x3 crafting grid enhanced with wand vis channels for creating magical artifacts.",
        [
            ("Construction", "Right-clicking a mundane crafting table with any wand."),
            ("Wand Vis Channeling", "Resting a wand in the top-right slot to power crafts with primal vis."),
            ("Aspect Crafting Recipes", "Recipes that consume specific vis amounts (Aer, Terra, Ignis, Aqua, Ordo, Perditio).")
        ],
        ["Getting-Started", "Wands-and-Staffs", "Home"]
    ),
    (
        "Infusion-Altar.md",
        "The Infusion Altar",
        "Artifice",
        "The pinnacle of Thaumic crafting: binding primal essentia and physical sacrifices into legendary relics.",
        [
            ("Multiblock Architecture", "Runic Matrix, 4 Arcane Stone pillars, 4 Arcane Stone base blocks, and Arcane Pedestals."),
            ("Starting an Infusion", "Placing the center item, surround items on pedestals, ensuring essentia supply, and wand activation."),
            ("Instability & Catastrophes", "How unstable infusions trigger lightning strikes, item ejection, and flux gas."),
            ("Stabilizing the Altar", "Symmetrical placement of skull candles, Mob Heads, and Crystal Clusters.")
        ],
        ["Infusion-Enchanting", "Baubles-&-Runed-Armor", "Home"]
    ),
    (
        "Infusion-Enchanting.md",
        "Infusion Enchanting",
        "Artifice",
        "Applying non-standard and high-tier enchantments directly to tools and weapons via the Infusion Altar.",
        [
            ("Custom Enchantments", "Haste, Repair, Frugal, Potency, and Corrosive."),
            ("Deterministic Enchanting", "Bypassing the vanilla random enchanting table with 100% predictable outcomes.")
        ],
        ["Infusion-Altar", "Home"]
    ),
    (
        "Arcane-Bore.md",
        "The Arcane Bore",
        "Artifice",
        "A tripod-mounted automated mining cannon that carves out deep subterranean shafts.",
        [
            ("Assembly", "Arcane Bore base, tripod mount, and Wand Focus: Excavation."),
            ("Pickaxe Durability", "Inserting enchanted pickaxes to grant Fortune and Silk Touch to excavated blocks."),
            ("Collection & Chest Alignment", "Automatic item sorting and adjacent chest collection.")
        ],
        ["Wand-Focuses", "Home"]
    ),
    (
        "Infernal-Furnace.md",
        "The Infernal Furnace",
        "Artifice",
        "A 3x3 hollow stone furnace powered by trapped elemental fire that smelts ores at super speed and produces rare bonus nuggets.",
        [
            ("Multiblock Assembly", "Netherrack, Obsidian, Iron Bars, and wand activation."),
            ("Smelting Bonuses", "Chance of yielding rare metal nuggets and native clusters."),
            ("XP Slurry & Bellows", "Attaching Arcane Bellows to increase speed and eliminate residual flux.")
        ],
        ["Configuration-&-Settings", "Home"]
    ),
    (
        "Baubles-&-Runed-Armor.md",
        "Baubles & Runic Shielding",
        "Artifice",
        "Wearable enchanted jewelry and force-field personal shielding.",
        [
            ("Boots of the Traveller", "Enhanced movement speed, high step-up, and jump boosts."),
            ("Runic Shielding", "Infusing rings, belts, amulets, and armor with rechargeable kinetic force fields."),
            ("Paving Stones of Travel & Warding", "Road blocks that accelerate players or prevent hostile mob transit.")
        ],
        ["Infusion-Altar", "Home"]
    ),
    (
        "Mirror-Magic.md",
        "Mirror Magic",
        "Artifice",
        "Trans-dimensional conduits for instant item and essentia transmission across infinite distances.",
        [
            ("Magic Mirrors", "Paired item teleportation portals."),
            ("Essentia Mirrors", "Wirelessly feeding essentia from remote jar storages into Infusion Altars.")
        ],
        ["Infusion-Altar", "Essentia-Transport-&-Piping", "Home"]
    ),
    (
        "Hungry-Chest.md",
        "The Hungry Chest",
        "Artifice",
        "A living, ravenous storage chest that snaps shut on dropped items colliding with it.",
        [
            ("Crafting", "Infusing a chest with an Arcane Door latch."),
            ("Automation Mechanics", "Instant intake of items from conveyor belts, drop chutes, and mob farms.")
        ],
        ["Artifice", "Home"]
    ),

    # Golemancy
    (
        "Golem-Basics-&-Materials.md",
        "Golem Basics & Materials",
        "Golemancy",
        "Constructing autonomous magical servants out of physical blocks.",
        [
            ("Golem Materials", "Straw (super fast), Wood (light), Clay (fireproof), Flesh (self-healing), Iron (tanky), Thaumium (armored), Tallow."),
            ("Material Attributes", "Speed, Carry Capacity, Health, and Armor comparison table."),
            ("Crafting Golems", "Crucible crafting recipes for golem poppets.")
        ],
        ["Animation-Cores", "Golem-Upgrades-&-Accessories", "Home"]
    ),
    (
        "Animation-Cores.md",
        "Animation Cores",
        "Golemancy",
        "The animated brain that defines a golem's job and behavior.",
        [
            ("Core: Gather", "Collecting dropped items and depositing them into designated containers."),
            ("Core: Harvest", "Reaping fully-grown crops and replanting seeds."),
            ("Core: Guard & Butcher", "Attacking hostile entities or livestock breeding control."),
            ("Core: Fill & Empty", "Moving items between inventories matching filter conditions."),
            ("Core: Decant & Alchemy", "Transporting liquid essentia and attending to crucibles."),
            ("Core: Use & Sort", "Operating buttons, levers, and complex sorting networks.")
        ],
        ["Golem-Basics-&-Materials", "Golem-Control-&-Markers", "Home"]
    ),
    (
        "Golem-Upgrades-&-Accessories.md",
        "Golem Upgrades & Accessories",
        "Golemancy",
        "Enhancing golem abilities with elemental power upgrades and vanity equipment.",
        [
            ("Elemental Upgrades", "Air (speed), Earth (strength), Fire (damage), Water (perception), Order (efficiency), Entropy (demolition)."),
            ("Equipping Accessories", "Tiny Top Hats, Spectacles, Fezzes, Bowties, and Iron Visors with specific functional bonuses.")
        ],
        ["Golem-Basics-&-Materials", "Home"]
    ),
    (
        "Golem-Control-&-Markers.md",
        "Golem Control & The Bell",
        "Golemancy",
        "Tools for binding, directing, and color-coding golem work zones.",
        [
            ("The Golemancy Bell", "Binding golems to home containers and assigning work targets."),
            ("Color Markers", "Placing colored chalk markers to configure multi-container routes.")
        ],
        ["Animation-Cores", "Home"]
    ),
    (
        "Traveling-Trunk.md",
        "The Traveling Trunk",
        "Golemancy",
        "A loyal, animated wooden chest with little legs that follows its master across the world.",
        [
            ("Taming & Binding", "Right-clicking with a wand to bind the trunk to your player."),
            ("Combat & Healing", "How trunks defend their owners and heal from damage.")
        ],
        ["Golem-Basics-&-Materials", "Home"]
    ),

    # Eldritch
    (
        "Warp-Mechanics.md",
        "Warp Mechanics & Sanity",
        "Eldritch Mysteries",
        "The psychological and mystical corruption inflicted upon Thaumaturges who delve into forbidden knowledge.",
        [
            ("The Three Tiers of Warp", "Temporary Warp (fades over time), Sticky Warp (curable through rituals), Permanent Warp (indelible mark on your soul)."),
            ("Warp Effects", "Hallucinations, phantom spiders, blinding eldritch visions, unnatural hunger, sun sickness, Eldritch Guardian attacks."),
            ("Relieving Warp", "Purifying Bath Salts, Sanitizing Soap, and Warp Ward potion effects.")
        ],
        ["Eldritch-Worldgen", "Crimson-Cult", "Home"]
    ),
    (
        "Eldritch-Worldgen.md",
        "Eldritch Structures & Monuments",
        "Eldritch Mysteries",
        "Ancient, foreboding monoliths scattered across the landscape.",
        [
            ("Eldritch Obelisks", "Towering obsidian pillars wrapped in runic bands topped with floating capstones."),
            ("Eldritch Altars", "Runic altars surrounded by sinister nodes and cultist ritual sites."),
            ("The Eldritch Eye", "Crafting and socketing the Eldritch Eye to break the seal and open an Eldritch Portal.")
        ],
        ["Warp-Mechanics", "Crimson-Cult", "The-Outer-Lands", "Home"]
    ),
    (
        "Crimson-Cult.md",
        "The Crimson Cult",
        "Eldritch Mysteries",
        "Fanatical zealots from beyond the veil seeking to usher in the Eldritch Lords.",
        [
            ("Crimson Knights", "Heavily armored melee warriors wielding broadswords."),
            ("Crimson Clerics", "Spellcasters launching fiery eldritch projectiles."),
            ("Crimson Praetor", "A lethal boss commander leading ritual strikes."),
            ("The Crimson Rites Book", "Rare grimoire dropped by cultists that unlocks the Eldritch research tab.")
        ],
        ["Eldritch-Worldgen", "Eldritch-Mobs", "Home"]
    ),
    (
        "Eldritch-Mobs.md",
        "Eldritch Horrors & Bosses",
        "Eldritch Mysteries",
        "Nightmarish beings hailing from the Outer Lands.",
        [
            ("Eldritch Guardians", "Tall, shadowy entities firing life-draining eldritch spheres."),
            ("Eldritch Crabs", "Vicious face-hugging scuttlers nesting inside ancient stone."),
            ("Eldritch Golems", "Massive biomechanical constructs guarding labyrinth chambers.")
        ],
        ["The-Outer-Lands", "Void-Metal-Equipment", "Home"]
    ),
    (
        "Void-Metal-Equipment.md",
        "Void Metal & Eldritch Relics",
        "Eldritch Mysteries",
        "Living, reality-warping metal drawn from the abyss.",
        [
            ("Properties of Void Metal", "Endless self-repair and inherent warp accumulation."),
            ("Void Thaumaturge Robes", "Ultimate casting garments granting supreme vis discounts."),
            ("Sanity Checker & Soap", "Tools for monitoring and cleansing internal warp.")
        ],
        ["Warp-Mechanics", "The-Outer-Lands", "Home"]
    ),
    (
        "The-Outer-Lands.md",
        "The Outer Lands",
        "Eldritch Mysteries",
        "The mysterious pocket dimension labyrinth of the Eldritch Architects.",
        [
            ("Opening the Portal", "Activating an Eldritch Portal atop an Eldritch Altar using 4 Eldritch Eyes."),
            ("The Maze of Stone", "Navigating indestructible obsidian corridors, trapped doorways, and runic keystones."),
            ("The Boss Vault", "Confronting the ancient lords of the labyrinth for primordial pearls.")
        ],
        ["Eldritch-Worldgen", "Eldritch-Mobs", "Void-Metal-Equipment", "Home"]
    ),

    # Worldgen
    (
        "Biomes-&-Flora.md",
        "Biomes & Thaumic Flora",
        "World Generation",
        "The magical landscapes and mystical botanicals introduced into Minecraft's ecosystem.",
        [
            ("Magical Forest Biome", "Vibrant turquoise foliage, naturally spawning Mana Beans, Shimmerleaf, and Pechs."),
            ("Tainted Land Biome", "Corrupted wasteland with tainted grass, crusted fibrous blight, and geysers."),
            ("Greatwood Trees", "Giant sprawling hardwood trees with wide trunks and expansive canopies."),
            ("Silverwood Trees", "Sacred glowing white trees housing pure aura nodes within their hearts."),
            ("Botanicals", "Cinderpearls (Ignis desert flowers), Shimmerleaf (Aqua/Herba pure plants), Vishrooms (toxic fungal growths).")
        ],
        ["Taint-Mechanics", "Structures", "Home"]
    ),
    (
        "Taint-Mechanics.md",
        "Taint Mechanics & Corruption",
        "World Generation",
        "A biological and magical infection that devours landscapes and transforms living organisms.",
        [
            ("Spread Dynamics", "Fibrous taint creeping across stone and dirt, transforming soil into tainted earth."),
            ("Taint Spores & Tendrils", "Explosive spore pods and attacking tendrils."),
            ("Tainted Creatures", "Tainted Crawlers, Tainted Sheep, Tainted Pigs, and mutated beasts."),
            ("Cleansing Taint", "Planting Silverwood saplings, using Pure Nodes, or deploying Ethereal Blooms to repel corruption.")
        ],
        ["Biomes-&-Flora", "Configuration-&-Settings", "Home"]
    ),
    (
        "Structures.md",
        "World Structures & Dungeons",
        "World Generation",
        "Forgotten ruins and wizard encampments across the world.",
        [
            ("Hilltop Stones & Barrow Mounds", "Ancient standing stones and buried crypts guarding loot chests."),
            ("Wizard Towers", "Tall spire towers containing arcane worktables, alchemical apparatus, and lost spellbooks."),
            ("Banker Houses", "Thaumic community structures in villages with coin exchangers and banker villagers.")
        ],
        ["Biomes-&-Flora", "Ore-Generation", "Home"]
    ),
    (
        "Ore-Generation.md",
        "Ores & Geological Deposits",
        "World Generation",
        "Subterranean minerals vital to thaumaturgical manufacturing.",
        [
            ("Infused Stone", "Six elemental crystal ores (Air, Earth, Fire, Water, Order, Entropy) embedded in underground rock."),
            ("Cinnabar", "Red mercurial ore smelted into Quicksilver."),
            ("Amber Ore", "Fossilized resin veins providing Amber gems for magical optics and runic construction.")
        ],
        ["Getting-Started", "Aspects-Overview", "Home"]
    )
]

def generate_pages():
    for filename, title, category, summary, sections, related in PAGES:
        path = os.path.join(WIKI_DIR, filename)
        content = f"# {title}\n\n"
        content += f"**Category:** {category} | [Return to Wiki Home](Home)\n\n"
        content += f"---\n\n"
        content += f"## Overview\n\n{summary}\n\n"
        content += f"---\n\n"
        
        for sec_title, sec_desc in sections:
            content += f"## {sec_title}\n\n{sec_desc}\n\n*(Full guide and documentation for this section will be added here.)*\n\n"
            
        content += f"---\n\n"
        content += f"## Related Topics\n\n"
        for rel in related:
            clean_name = rel.replace("-", " ")
            content += f"- [{clean_name}]({rel})\n"
            
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Generated: {filename}")

def generate_sidebar():
    sidebar_path = os.path.join(WIKI_DIR, "_Sidebar.md")
    content = """## [NeoThaum 4 Wiki](Home)

### 🚀 Getting Started
- [Getting Started](Getting-Started)
- [Configuration & Settings](Configuration-&-Settings)
- [Mod Compatibility](Mod-Compatibility)
- [Troubleshooting & FAQ](Troubleshooting-&-FAQ)

### 🔮 Aspects of Magic
- [Aspects Overview](Aspects-Overview)
- [The Six Primal Aspects](Primal-Aspects)
- [Compound Aspects Chart](Compound-Aspects-Chart)
- [Scanning & Thaumometer](Scanning-&-Thaumometer)
- [Research Mechanics](Research-Mechanics)
- [Aura Nodes](Aura-Nodes)
- [Node Tapping & Recharging](Node-Tapping-&-Recharging)

### 🪄 Thaumaturgy
- [Wands, Scepters & Staffs](Wands-and-Staffs)
- [Wand Focuses](Wand-Focuses)
- [Focal Manipulator](Focal-Manipulator)
- [Vis Storage & Robes](Vis-Storage-&-Discount-Armor)

### 🧪 Alchemy & Essentia
- [Crucible Alchemy](Crucible-Alchemy)
- [Essentia Distillation](Essentia-Distillation)
- [Essentia Piping](Essentia-Transport-&-Piping)
- [Essentia Storage](Essentia-Storage)
- [The Thaumatorium](Thaumatorium)
- [Centrifuge & Crystallizer](Alchemical-Centrifuge-&-Crystallizer)

### ⚙️ Artifice & Devices
- [Arcane Worktable](Arcane-Worktable)
- [The Infusion Altar](Infusion-Altar)
- [Infusion Enchanting](Infusion-Enchanting)
- [The Arcane Bore](Arcane-Bore)
- [The Infernal Furnace](Infernal-Furnace)
- [Baubles & Runic Armor](Baubles-&-Runed-Armor)
- [Mirror Magic](Mirror-Magic)
- [The Hungry Chest](Hungry-Chest)

### 🤖 Golemancy
- [Golem Basics & Materials](Golem-Basics-&-Materials)
- [Animation Cores](Animation-Cores)
- [Upgrades & Accessories](Golem-Upgrades-&-Accessories)
- [Golem Control & Bell](Golem-Control-&-Markers)
- [The Traveling Trunk](Traveling-Trunk)

### 👁️ Eldritch & Warp
- [Warp Mechanics & Sanity](Warp-Mechanics)
- [Eldritch Structures](Eldritch-Worldgen)
- [The Crimson Cult](Crimson-Cult)
- [Eldritch Horrors & Bosses](Eldritch-Mobs)
- [Void Metal Equipment](Void-Metal-Equipment)
- [The Outer Lands](The-Outer-Lands)

### 🌲 World Generation
- [Biomes & Thaumic Flora](Biomes-&-Flora)
- [Taint Mechanics](Taint-Mechanics)
- [World Structures](Structures)
- [Ores & Deposits](Ore-Generation)
"""
    with open(sidebar_path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Generated: _Sidebar.md")

def generate_footer():
    footer_path = os.path.join(WIKI_DIR, "_Footer.md")
    content = """---
<div align="center">
  <small>
    <b>NeoThaum 4</b> | A faithful 1:1 recreation of Thaumcraft 4.2.3.5 for Minecraft 26.3 (Fabric) |
    <a href="https://github.com/iceplopper/neothaum">GitHub Repository</a> |
    <a href="https://github.com/iceplopper/neothaum/issues">Issue Tracker</a>
  </small>
</div>
"""
    with open(footer_path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Generated: _Footer.md")

def generate_home():
    home_path = os.path.join(WIKI_DIR, "Home.md")
    content = """# Welcome to the NeoThaum Wiki

<div align="center">

### *The comprehensive compendium of Arcane Knowledge, Thaumaturgy, and Eldritch Mysteries.*

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-blue.svg)](https://fabricmc.net/)
[![Repository](https://img.shields.io/badge/GitHub-neothaum-purple.svg)](https://github.com/iceplopper/neothaum)

</div>

---

## 📖 About NeoThaum

**NeoThaum** is an authentic recreation of Azanor's legendary magic mod **Thaumcraft 4.2.3.5** designed for modern Minecraft (**26.3 / 1.21.4+**) running on the Fabric toolchain. 

Whether you are an aspiring Thaumaturge picking up an Iron-Capped Wand for the very first time, or a veteran alchemist seeking to construct stabilized Infusion Altars and automate golem logistics, this wiki serves as your complete guide.

---

## 🧭 Navigation & Major Chapters

```mermaid
graph TD
    A["Getting Started"] --> B["Aspects of Magic"]
    B --> C["Thaumaturgy (Wands & Staves)"]
    B --> D["Alchemy (Essentia & Distillation)"]
    C --> E["Artifice (Infusion & Machinery)"]
    D --> E
    E --> F["Golemancy (Automation & Servants)"]
    E --> G["Eldritch Mysteries (Warp & The Abyss)"]
```

### 1. 🚀 [Getting Started](Getting-Started)
Everything you need to begin your magical career:
- [Getting Started Guide](Getting-Started) — Your first wand, Salis Mundus, and the Thaumonomicon.
- [Configuration & Settings](Configuration-&-Settings) — Customizing world generation, taint spread, and gameplay multipliers.
- [Mod Compatibility](Mod-Compatibility) — Native integrations with JEI, Jade, ModMenu, and Xaero's Maps.
- [Troubleshooting & FAQ](Troubleshooting-&-FAQ) — Resolving common questions and issues.

---

### 2. 🔮 [Aspects of Magic & Research](Aspects-Overview)
The building blocks of existence and how to decode them:
- [Aspects Overview](Aspects-Overview) — Fundamentals of primal vis and compound aspect synthesis.
- [The Six Primal Aspects](Primal-Aspects) — Aer, Terra, Ignis, Aqua, Ordo, and Perditio.
- [Compound Aspects Chart](Compound-Aspects-Chart) — Complete combination formulas for all compound aspects.
- [Scanning & Thaumometer](Scanning-&-Thaumometer) — Analyzing blocks, entities, and items.
- [Research Mechanics](Research-Mechanics) — The hex parchment research mini-game.
- [Aura Nodes](Aura-Nodes) — Types, modifiers, jar containment, and energizing transducers.
- [Node Tapping & Recharging](Node-Tapping-&-Recharging) — Recharging casting wands without depleting nodes.

---

### 3. 🪄 [Thaumaturgy](Wands-and-Staffs)
The tools of spellcasting and energy manipulation:
- [Wands, Scepters & Staffs](Wands-and-Staffs) — Rods, caps, scepters, and two-handed combat staves.
- [Wand Focuses](Wand-Focuses) — Fire, Frost, Shock, Excavation, Portable Hole, Warding, and more.
- [The Focal Manipulator](Focal-Manipulator) — Branching upgrade trees and focus customization.
- [Vis Storage & Robes](Vis-Storage-&-Discount-Armor) — Thaumaturge robes, goggles, and discount baubles.

---

### 4. 🧪 [Alchemy & Essentia](Crucible-Alchemy)
Refining matter into pure spiritual fluids:
- [Crucible Alchemy](Crucible-Alchemy) — Transmutation, boiling, and managing flux waste.
- [Essentia Distillation](Essentia-Distillation) — The Alchemical Furnace and Arcane Alembics.
- [Essentia Piping & Transport](Essentia-Transport-&-Piping) — Suction physics, directional tubes, buffers, and valves.
- [Essentia Storage](Essentia-Storage) — Warded Jars, Void Jars, Phials, and Aspect Labels.
- [The Thaumatorium](Thaumatorium) — Automated hands-free crucible transmutations.
- [Centrifuge & Crystallizer](Alchemical-Centrifuge-&-Crystallizer) — Splitting compound aspects and solidifying essentia.

---

### 5. ⚙️ [Artifice & Ancient Machinery](Infusion-Altar)
Constructing enchanted devices and multiblock wonders:
- [The Arcane Worktable](Arcane-Worktable) — Primal vis-powered item manufacturing.
- [The Infusion Altar](Infusion-Altar) — Multiblock runic matrix crafting, symmetry balancing, and instability.
- [Infusion Enchanting](Infusion-Enchanting) — Deterministic high-tier enchanting.
- [The Arcane Bore](Arcane-Bore) — Automated subterranean laser mining.
- [The Infernal Furnace](Infernal-Furnace) — Smelting with netherrack fire and bonus nugget yields.
- [Baubles & Runic Armor](Baubles-&-Runed-Armor) — Boots of the Traveller and rechargeable force fields.
- [Mirror Magic](Mirror-Magic) — Wireless inter-dimensional item and essentia conduits.
- [The Hungry Chest](Hungry-Chest) — Vacuum collection storage.

---

### 6. 🤖 [Golemancy](Golem-Basics-&-Materials)
Automated constructs that manage your base:
- [Golem Basics & Materials](Golem-Basics-&-Materials) — Clay, Wood, Straw, Flesh, Iron, Thaumium, and Tallow.
- [Animation Cores](Animation-Cores) — Gather, Harvest, Guard, Butcher, Decant, Fill, Empty, Use, and Sort.
- [Golem Upgrades & Accessories](Golem-Upgrades-&-Accessories) — Elemental upgrades, spectacles, and tiny hats.
- [Golem Control & The Bell](Golem-Control-&-Markers) — Task assignments and color-coded chest networks.
- [The Traveling Trunk](Traveling-Trunk) — Loyal walking companion chests.

---

### 7. 👁️ [Eldritch Mysteries & Warp](Warp-Mechanics)
Diving into forbidden knowledge and the darkness between the stars:
- [Warp Mechanics & Sanity](Warp-Mechanics) — Temporary, sticky, and permanent warp; psychic attacks and cures.
- [Eldritch Monuments](Eldritch-Worldgen) — Obelisks, runic altars, and portal frames.
- [The Crimson Cult](Crimson-Cult) — Crimson Knights, Clerics, Praetors, and the Crimson Rites.
- [Eldritch Horrors & Bosses](Eldritch-Mobs) — Eldritch Guardians, Crabs, and Golems.
- [Void Metal Equipment](Void-Metal-Equipment) — Self-repairing abyssal gear.
- [The Outer Lands](The-Outer-Lands) — Opening the portal to the labyrinth dimension.

---

### 8. 🌲 [World Generation & Flora](Biomes-&-Flora)
- [Biomes & Thaumic Flora](Biomes-&-Flora) — Magical Forests, Silverwood & Greatwood trees, and native plants.
- [Taint Mechanics](Taint-Mechanics) — Infection spread, tainted mobs, fibrous tendrils, and purification.
- [World Structures](Structures) — Barrow Mounds, Hilltop Stones, and Wizard Towers.
- [Ores & Deposits](Ore-Generation) — Infused Stone, Cinnabar, and Amber deposits.

---

*Use the **Sidebar** to jump directly to any topic.*
"""
    with open(home_path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Generated: Home.md")

if __name__ == "__main__":
    generate_pages()
    generate_sidebar()
    generate_footer()
    generate_home()
    print("Wiki generation complete!")

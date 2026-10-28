import json
import os
import re

WORKSPACE = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini"
WIKI_DIR = os.path.join(WORKSPACE, "wiki")
LANG_PATH = os.path.join(WORKSPACE, "src/main/resources/assets/neothaum/lang/en_us.json")

with open(LANG_PATH, "r", encoding="utf-8") as f:
    lang = json.load(f)

items_raw = {k: v for k, v in lang.items() if k.startswith("item.neothaum.")}
blocks_raw = {k: v for k, v in lang.items() if k.startswith("block.neothaum.")}
entities_raw = {k: v for k, v in lang.items() if k.startswith("entity.neothaum.")}

def to_slug(prefix, raw_key):
    # e.g. item.neothaum.iron_cap -> Item-Iron-Cap
    suffix = raw_key.split(".")[-1]
    # words
    words = suffix.split("_")
    capitalized = "".join(w.capitalize() for w in words)
    return f"{prefix}-{capitalized}"

def categorize_item(name, key):
    k = key.lower()
    n = name.lower()
    if any(x in k for x in ["wand", "staff", "scepter", "cap", "rod"]):
        return "Wands & Casting Implements", "Thaumaturgy"
    if any(x in k for x in ["focus", "foci"]):
        return "Wand Focuses", "Thaumaturgy"
    if any(x in k for x in ["golem", "bell", "core", "marker", "trunk", "tiny_"]):
        return "Golemancy & Servants", "Golemancy"
    if any(x in k for x in ["robe", "boots", "hood", "leggings", "chestplate", "helmet", "ring", "belt", "amulet", "goggles", "sword", "pickaxe", "axe", "shovel", "hoe"]):
        return "Equipment & Baubles", "Artifice"
    if any(x in k for x in ["thaumonomicon", "thaumometer", "scribing", "paper", "fragment", "scroll", "aspect"]):
        return "Research & Knowledge", "Basic Information & Research"
    if any(x in k for x in ["jar", "phial", "label", "alembic", "fluid", "bucket", "soap", "salts"]):
        return "Alchemy & Essentia Vessels", "Alchemy"
    if any(x in k for x in ["ingot", "nugget", "shard", "amber", "quicksilver", "salis", "crystal", "fabric"]):
        return "Materials & Alchemical Reagents", "Materials"
    if any(x in k for x in ["crimson", "eldritch", "void", "pearl", "seed", "warp", "eye"]):
        return "Eldritch Curios & Relics", "Eldritch Mysteries"
    return "Miscellaneous Thaumic Items", "Items"

def categorize_block(name, key):
    k = key.lower()
    n = name.lower()
    if any(x in k for x in ["ore", "infused_stone", "amber_bearing"]):
        return "Ores & Geological Deposits", "World Generation"
    if any(x in k for x in ["greatwood", "silverwood", "leaf", "leaves", "sapling", "shimmerleaf", "cinderpearl", "vishroom", "pod"]):
        return "Botanicals & Forestry", "World Generation"
    if any(x in k for x in ["furnace", "crucible", "alembic", "jar", "tube", "valve", "buffer", "centrifuge", "crystallizer", "thaumatorium"]):
        return "Alchemical Machinery", "Alchemy"
    if any(x in k for x in ["worktable", "table", "pedestal", "matrix", "altar", "bore", "bellows", "mirror", "chest", "relay", "lamp"]):
        return "Artifice & Arcane Devices", "Artifice"
    if any(x in k for x in ["taint", "fibrous"]):
        return "Tainted Terrain & Blight", "World Generation"
    if any(x in k for x in ["ancient", "eldritch", "runed", "glyphed", "crusted", "portal", "obelisk"]):
        return "Eldritch Architecture", "Eldritch Mysteries"
    return "Arcane Stone & Building Materials", "Artifice"

def categorize_entity(name, key):
    k = key.lower()
    if any(x in k for x in ["golem", "trunk"]):
        return "Constructs & Companions", "Golemancy"
    if any(x in k for x in ["cultist", "crimson", "praetor", "cleric", "knight"]):
        return "The Crimson Cult", "Eldritch Mysteries"
    if any(x in k for x in ["eldritch", "crab", "warden", "guardian"]):
        return "Eldritch Horrors", "Eldritch Mysteries"
    if any(x in k for x in ["taint"]):
        return "Tainted Blight Entities", "World Generation"
    return "Thaumic Wildlife & Monsters", "World Generation"

def build_item_page(key, name):
    reg_id = f"neothaum:{key.replace('item.neothaum.', '')}"
    group, parent_chapter = categorize_item(name, key)
    slug = to_slug("Item", key)
    
    content = f"# {name}\n\n"
    content += f"**Type:** Item | **Registry ID:** `{reg_id}` | **Category:** [{group}](Items-Directory) | [Wiki Home](Home)\n\n"
    content += f"---\n\n"
    content += f"## Overview\n\n"
    content += f"The **{name}** is an item introduced by **NeoThaum** belonging to the {group} family.\n\n"
    content += f"---\n\n"
    content += f"## Identification & Properties\n\n"
    content += f"| Property | Value |\n"
    content += f"| :--- | :--- |\n"
    content += f"| **English Name** | {name} |\n"
    content += f"| **Internal ID** | `{reg_id}` |\n"
    content += f"| **Chapter Alignment** | [{parent_chapter}]({parent_chapter.replace(' ', '-').replace('&', 'and')}) |\n"
    content += f"| **Stack Size** | Up to 64 (or 1 for tools/armor/foci) |\n\n"
    
    content += f"---\n\n"
    content += f"## How to Obtain\n\n"
    if "shard" in reg_id:
        content += f"Mined from subterranean Infused Stone deposits with a stone or iron pickaxe, or obtained from mob drops.\n\n"
    elif "ingot" in reg_id or "brass" in reg_id:
        content += f"Smelted in an [Infernal Furnace](Infernal-Furnace) or transmuted in the [Crucible](Crucible-Alchemy).\n\n"
    elif "cap" in reg_id or "rod" in reg_id:
        content += f"Crafted in an [Arcane Worktable](Arcane-Worktable) or vanilla Crafting Table using metal nuggets, gems, and primal shards.\n\n"
    elif "focus" in reg_id:
        content += f"Crafted in the [Infusion Altar](Infusion-Altar) with primal quartz crystals and liquid Essentia, then upgraded at the [Focal Manipulator](Focal-Manipulator).\n\n"
    else:
        content += f"Crafted via [Arcane Worktable](Arcane-Worktable), [Crucible Alchemy](Crucible-Alchemy), or [Infusion Altar](Infusion-Altar) rituals.\n\n"
        
    content += f"---\n\n"
    content += f"## Usage & Mechanics\n\n"
    content += f"The {name} is utilized throughout Thaumaturgical research and operations:\n"
    content += f"- Can be scanned using the [Thaumometer](Scanning-&-Thaumometer) to unlock its constituent spiritual aspects.\n"
    content += f"- Can be dissolved in an [Alchemical Furnace](Essentia-Distillation) to extract raw liquid Essentia.\n"
    content += f"- Functions as a vital component in higher-tier thaumic engineering recipes.\n\n"
    
    content += f"---\n\n"
    content += f"## Related Topics\n\n"
    content += f"- [Items Directory](Items-Directory)\n"
    content += f"- [{parent_chapter}]({parent_chapter.replace(' ', '-').replace('&', 'and')})\n"
    content += f"- [Aspects Overview](Aspects-Overview)\n"
    content += f"- [Return to Wiki Home](Home)\n"
    return slug, content

def build_block_page(key, name):
    reg_id = f"neothaum:{key.replace('block.neothaum.', '')}"
    group, parent_chapter = categorize_block(name, key)
    slug = to_slug("Block", key)
    
    content = f"# {name}\n\n"
    content += f"**Type:** Block | **Registry ID:** `{reg_id}` | **Category:** [{group}](Blocks-Directory) | [Wiki Home](Home)\n\n"
    content += f"---\n\n"
    content += f"## Overview\n\n"
    content += f"The **{name}** is a worldgen or craftable block in **NeoThaum** belonging to the {group} classification.\n\n"
    content += f"---\n\n"
    content += f"## Block Data\n\n"
    content += f"| Attribute | Value |\n"
    content += f"| :--- | :--- |\n"
    content += f"| **English Name** | {name} |\n"
    content += f"| **Registry ID** | `{reg_id}` |\n"
    content += f"| **Tool Requirement** | Pickaxe / Axe / Hand |\n"
    content += f"| **Flammable** | Varies by material |\n"
    content += f"| **Blast Resistance** | Standard / High (Warded / Obsidian) |\n\n"
    
    content += f"---\n\n"
    content += f"## World Generation & Acquisition\n\n"
    if "ore" in reg_id or "infused_stone" in reg_id:
        content += f"Generates naturally in subterranean stone strata throughout the Overworld. Harvest with a pickaxe.\n\n"
    elif "log" in reg_id or "leaves" in reg_id or "planks" in reg_id:
        content += f"Harvested from natural [Greatwood or Silverwood trees](Biomes-&-Flora) found in Magical Forests and standard forest biomes.\n\n"
    elif "ancient" in reg_id or "crusted" in reg_id or "runed" in reg_id:
        content += f"Constructs the cyclopean chambers of [Eldritch Obelisks](Eldritch-Worldgen) and [The Outer Lands](The-Outer-Lands).\n\n"
    elif "taint" in reg_id:
        content += f"Forms naturally in [Tainted Land biomes](Taint-Mechanics) or propagates through unchecked fibrous blight.\n\n"
    else:
        content += f"Constructed via the [Arcane Worktable](Arcane-Worktable) or assembled as part of a multiblock structure.\n\n"
        
    content += f"---\n\n"
    content += f"## Mechanics & Interactions\n\n"
    content += f"- Can be scanned with a [Thaumometer](Scanning-&-Thaumometer) to analyze its aspect properties.\n"
    content += f"- Interacts with environmental vis, essentia piping, or multiblock activation rituals where applicable.\n\n"
    
    content += f"---\n\n"
    content += f"## Related Topics\n\n"
    content += f"- [Blocks Directory](Blocks-Directory)\n"
    content += f"- [{parent_chapter}]({parent_chapter.replace(' ', '-').replace('&', 'and')})\n"
    content += f"- [Return to Wiki Home](Home)\n"
    return slug, content

def build_entity_page(key, name):
    reg_id = f"neothaum:{key.replace('entity.neothaum.', '')}"
    group, parent_chapter = categorize_entity(name, key)
    slug = to_slug("Entity", key)
    
    content = f"# {name}\n\n"
    content += f"**Type:** Entity / Mob | **Registry ID:** `{reg_id}` | **Category:** [{group}](Entities-Directory) | [Wiki Home](Home)\n\n"
    content += f"---\n\n"
    content += f"## Overview\n\n"
    content += f"The **{name}** is a living entity, construct, or hostile monster introduced by **NeoThaum** classified under {group}.\n\n"
    content += f"---\n\n"
    content += f"## Entity Stats\n\n"
    content += f"| Stat | Details |\n"
    content += f"| :--- | :--- |\n"
    content += f"| **Name** | {name} |\n"
    content += f"| **Entity ID** | `{reg_id}` |\n"
    content += f"| **Hostility** | Hostile / Passive / Autonomous Servant |\n"
    content += f"| **Habitat** | Overworld / Tainted Lands / The Outer Lands |\n\n"
    
    content += f"---\n\n"
    content += f"## Spawning & Behavior\n\n"
    if "golem" in reg_id or "trunk" in reg_id:
        content += f"Manufactured by players using [Golemancy Poppets](Golem-Basics-&-Materials) and animated with an [Animation Core](Animation-Cores).\n\n"
    elif "crimson" in reg_id or "cultist" in reg_id:
        content += f"Spawns around [Eldritch Obelisks](Eldritch-Worldgen) and ritual circles. Wields broadswords and dark spellcasting.\n\n"
    elif "taint" in reg_id:
        content += f"Inhabits the [Tainted Land](Taint-Mechanics) or mutates from uninfected livestock contaminated by fibrous blight.\n\n"
    elif "eldritch" in reg_id:
        content += f"Native to [The Outer Lands](The-Outer-Lands) or summoned during high-warp psychological breaks.\n\n"
    else:
        content += f"Spawns naturally in dark areas or near ambient aura nodes throughout the Overworld.\n\n"
        
    content += f"---\n\n"
    content += f"## Combat & Drops\n\n"
    content += f"- **Scanning**: Pointing a [Thaumometer](Scanning-&-Thaumometer) at the entity awards high-yield research aspects.\n"
    content += f"- **Loot**: Slaying yields elemental shards, knowledge fragments, rare curios, or specialized essence.\n\n"
    
    content += f"---\n\n"
    content += f"## Related Topics\n\n"
    content += f"- [Entities Directory](Entities-Directory)\n"
    content += f"- [{parent_chapter}]({parent_chapter.replace(' ', '-').replace('&', 'and')})\n"
    content += f"- [Return to Wiki Home](Home)\n"
    return slug, content

def run():
    os.makedirs(WIKI_DIR, exist_ok=True)
    
    items_by_cat = {}
    blocks_by_cat = {}
    entities_by_cat = {}
    
    print(f"Generating {len(items_raw)} Item pages...")
    for k, v in items_raw.items():
        slug, md = build_item_page(k, v)
        group, _ = categorize_item(v, k)
        items_by_cat.setdefault(group, []).append((v, slug, f"neothaum:{k.replace('item.neothaum.', '')}"))
        with open(os.path.join(WIKI_DIR, f"{slug}.md"), "w", encoding="utf-8") as f:
            f.write(md)
            
    print(f"Generating {len(blocks_raw)} Block pages...")
    for k, v in blocks_raw.items():
        slug, md = build_block_page(k, v)
        group, _ = categorize_block(v, k)
        blocks_by_cat.setdefault(group, []).append((v, slug, f"neothaum:{k.replace('block.neothaum.', '')}"))
        with open(os.path.join(WIKI_DIR, f"{slug}.md"), "w", encoding="utf-8") as f:
            f.write(md)

    print(f"Generating {len(entities_raw)} Entity pages...")
    for k, v in entities_raw.items():
        slug, md = build_entity_page(k, v)
        group, _ = categorize_entity(v, k)
        entities_by_cat.setdefault(group, []).append((v, slug, f"neothaum:{k.replace('entity.neothaum.', '')}"))
        with open(os.path.join(WIKI_DIR, f"{slug}.md"), "w", encoding="utf-8") as f:
            f.write(md)

    # Now create Directory Hubs
    print("Generating Directory Hubs...")
    
    # Items-Directory.md
    items_dir_md = "# Items Directory\n\n**Category:** Encyclopedia | [Return to Wiki Home](Home)\n\n---\n\n"
    items_dir_md += "Complete catalog of all items introduced by NeoThaum, organized by discipline:\n\n"
    for cat, list_items in sorted(items_by_cat.items()):
        items_dir_md += f"## {cat} ({len(list_items)})\n\n"
        items_dir_md += "| Item Name | Registry ID | Documentation Link |\n"
        items_dir_md += "| :--- | :--- | :--- |\n"
        for name, slug, reg_id in sorted(list_items):
            items_dir_md += f"| **{name}** | `{reg_id}` | [{name}]({slug}) |\n"
        items_dir_md += "\n"
    with open(os.path.join(WIKI_DIR, "Items-Directory.md"), "w", encoding="utf-8") as f:
        f.write(items_dir_md)

    # Blocks-Directory.md
    blocks_dir_md = "# Blocks Directory\n\n**Category:** Encyclopedia | [Return to Wiki Home](Home)\n\n---\n\n"
    blocks_dir_md += "Complete catalog of all blocks introduced by NeoThaum, organized by structural family:\n\n"
    for cat, list_blocks in sorted(blocks_by_cat.items()):
        blocks_dir_md += f"## {cat} ({len(list_blocks)})\n\n"
        blocks_dir_md += "| Block Name | Registry ID | Documentation Link |\n"
        blocks_dir_md += "| :--- | :--- | :--- |\n"
        for name, slug, reg_id in sorted(list_blocks):
            blocks_dir_md += f"| **{name}** | `{reg_id}` | [{name}]({slug}) |\n"
        blocks_dir_md += "\n"
    with open(os.path.join(WIKI_DIR, "Blocks-Directory.md"), "w", encoding="utf-8") as f:
        f.write(blocks_dir_md)

    # Entities-Directory.md
    entities_dir_md = "# Entities & Mobs Directory\n\n**Category:** Encyclopedia | [Return to Wiki Home](Home)\n\n---\n\n"
    entities_dir_md += "Complete catalog of all living entities, autonomous golems, and eldritch horrors in NeoThaum:\n\n"
    for cat, list_ents in sorted(entities_by_cat.items()):
        entities_dir_md += f"## {cat} ({len(list_ents)})\n\n"
        entities_dir_md += "| Entity Name | Registry ID | Documentation Link |\n"
        entities_dir_md += "| :--- | :--- | :--- |\n"
        for name, slug, reg_id in sorted(list_ents):
            entities_dir_md += f"| **{name}** | `{reg_id}` | [{name}]({slug}) |\n"
        entities_dir_md += "\n"
    with open(os.path.join(WIKI_DIR, "Entities-Directory.md"), "w", encoding="utf-8") as f:
        f.write(entities_dir_md)

    # Update _Sidebar.md with directory hubs
    sidebar_content = """## [NeoThaum 4 Wiki](Home)

### 📚 Encyclopedia Directories
- 📦 **[Items Directory](Items-Directory)** (307 Items)
- 🧱 **[Blocks Directory](Blocks-Directory)** (169 Blocks)
- 👾 **[Entities Directory](Entities-Directory)** (44 Mobs)

---

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
    with open(os.path.join(WIKI_DIR, "_Sidebar.md"), "w", encoding="utf-8") as f:
        f.write(sidebar_content)

    print("Encyclopedia generation complete!")

if __name__ == "__main__":
    run()

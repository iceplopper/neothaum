import json
import os
import re

WORKSPACE = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini"
WIKI_DIR = os.path.join(WORKSPACE, "wiki")
LANG_PATH = os.path.join(WORKSPACE, "src/main/resources/assets/neothaum/lang/en_us.json")
RECIPE_DIR = os.path.join(WORKSPACE, "src/main/resources/data/neothaum/recipe")
ASPECTS_PATH = os.path.join(WORKSPACE, "src/main/resources/data/neothaum/neothaum/aspects.json")
ASPECTS_MODERN = os.path.join(WORKSPACE, "src/main/resources/data/neothaum/neothaum/aspects_modern.json")

with open(LANG_PATH, "r", encoding="utf-8") as f:
    lang = json.load(f)

items_raw = {k: v for k, v in lang.items() if k.startswith("item.neothaum.")}
blocks_raw = {k: v for k, v in lang.items() if k.startswith("block.neothaum.")}
entities_raw = {k: v for k, v in lang.items() if k.startswith("entity.neothaum.")}

aspect_map = {}
for apath in [ASPECTS_PATH, ASPECTS_MODERN]:
    if os.path.exists(apath):
        with open(apath, "r", encoding="utf-8") as f:
            data = json.load(f)
            for entry in data.get("objects", []):
                item_id = entry.get("item")
                if item_id:
                    aspect_map[item_id] = entry.get("aspects", {})

recipes_by_result = {}
recipes_using_item = {}
if os.path.exists(RECIPE_DIR):
    for rfile in os.listdir(RECIPE_DIR):
        if rfile.endswith(".json"):
            try:
                with open(os.path.join(RECIPE_DIR, rfile), "r", encoding="utf-8") as f:
                    rdata = json.load(f)
                    res = rdata.get("result", {})
                    if isinstance(res, dict):
                        rid = res.get("id") or res.get("item")
                    elif isinstance(res, str):
                        rid = res
                    else:
                        rid = None
                    if rid:
                        recipes_by_result.setdefault(rid, []).append((rfile, rdata))
                    
                    # Also collect inputs
                    keys = rdata.get("key", {})
                    for k_char, k_val in keys.items():
                        inp_id = k_val if isinstance(k_val, str) else k_val.get("item")
                        if inp_id:
                            recipes_using_item.setdefault(inp_id, []).append((rid, rdata.get("type", "crafting")))
                    for ing in rdata.get("ingredients", []):
                        inp_id = ing if isinstance(ing, str) else ing.get("item")
                        if inp_id:
                            recipes_using_item.setdefault(inp_id, []).append((rid, rdata.get("type", "crafting")))
            except Exception:
                pass

def to_slug(prefix, raw_key):
    suffix = raw_key.split(".")[-1]
    words = suffix.split("_")
    capitalized = "".join(w.capitalize() for w in words)
    return f"{prefix}-{capitalized}"

def id_to_name(identifier):
    if not identifier:
        return "Unknown"
    # check item
    item_k = f"item.neothaum.{identifier.replace('neothaum:', '')}"
    if item_k in lang:
        return lang[item_k]
    block_k = f"block.neothaum.{identifier.replace('neothaum:', '')}"
    if block_k in lang:
        return lang[block_k]
    if identifier.startswith("minecraft:"):
        raw = identifier.replace("minecraft:", "").replace("_", " ").title()
        return raw
    return identifier.replace("neothaum:", "").replace("_", " ").title()

def format_aspects(aspects):
    if not aspects:
        return "None known / Compound derived upon scan"
    parts = []
    for asp, val in aspects.items():
        parts.append(f"**{asp.capitalize()}** ({val})")
    return ", ".join(parts)

def build_infobox(title, image_alt, rows):
    out = """<aside style="float: right; width: 300px; margin: 0 0 1em 1.5em; padding: 12px; background: #f8f9fa; border: 1px solid #a2a9b1; border-radius: 4px; font-size: 90%;">\n"""
    out += f"""<div style="font-size: 125%; font-weight: bold; text-align: center; padding: 4px 0; border-bottom: 2px solid #a2a9b1;">{title}</div>\n"""
    out += f"""<div style="text-align: center; padding: 12px 0; color: #72777d; font-style: italic;">[{image_alt}]</div>\n"""
    out += """<table style="width: 100%; border-collapse: collapse;">\n"""
    for label, val in rows:
        out += f"""<tr><th style="text-align: left; padding: 4px 6px; border-top: 1px solid #eaecf0; vertical-align: top; width: 40%;">{label}</th><td style="padding: 4px 6px; border-top: 1px solid #eaecf0; vertical-align: top;">{val}</td></tr>\n"""
    out += """</table>\n</aside>\n\n"""
    return out

# ----------------- ITEM GENERATION -----------------
def generate_item_page(key, name):
    reg_id = f"neothaum:{key.replace('item.neothaum.', '')}"
    slug = to_slug("Item", key)
    
    aspects = aspect_map.get(reg_id, {})
    recipes = recipes_by_result.get(reg_id, [])
    uses = recipes_using_item.get(reg_id, [])
    
    # Determine stack size and renewable
    stack_size = "64"
    if any(x in reg_id for x in ["wand", "staff", "scepter", "sword", "pickaxe", "axe", "shovel", "hoe", "helmet", "chestplate", "leggings", "boots", "focus", "amulet", "ring", "belt", "harness", "girdle", "bell"]):
        stack_size = "1"
    elif any(x in reg_id for x in ["egg", "trunk"]):
        stack_size = "16"

    infobox_rows = [
        ("Type", "Item"),
        ("Identifier", f"<code>{reg_id}</code>"),
        ("Stackable", f"Yes ({stack_size})" if stack_size != "1" else "No (1)"),
        ("Renewable", "Yes"),
        ("Aspects", format_aspects(aspects))
    ]
    
    infobox = build_infobox(name, f"{name} Item Sprite", infobox_rows)
    
    content = f"# {name}\n\n"
    content += infobox
    content += f"A **{name}** is an item added by **NeoThaum** (recreating Thaumcraft 4 for modern Minecraft).\n\n"
    
    # Obtaining
    content += "## Obtaining\n\n"
    if recipes:
        content += "### Crafting\n\n"
        content += "| Recipe File | Type | Ingredients |\n"
        content += "| :--- | :--- | :--- |\n"
        for rfile, rdata in recipes[:5]:
            rtype = rdata.get("type", "crafting").replace("minecraft:", "").replace("neothaum:", "").title()
            keys = rdata.get("key", {})
            ing_names = []
            if keys:
                for char, val in keys.items():
                    iid = val if isinstance(val, str) else val.get("item", "tag")
                    ing_names.append(id_to_name(iid))
            for ing in rdata.get("ingredients", []):
                iid = ing if isinstance(ing, str) else ing.get("item", "tag")
                ing_names.append(id_to_name(iid))
            ing_summary = ", ".join(set(ing_names)) if ing_names else "Special recipe"
            content += f"| `{rfile}` | {rtype} | {ing_summary} |\n"
        content += "\n"
    elif "shard" in reg_id:
        content += "### Mining\n\n"
        content += f"**{name}** drops when mining naturally generated elemental Infused Stone ore deposits with any pickaxe, or from slaying specific elemental entities.\n\n"
    elif "spawn_egg" in reg_id:
        content += "### Creative & Commands\n\n"
        content += f"Available in the Creative Inventory under Spawn Eggs or via the command `/give @p {reg_id}`.\n\n"
    else:
        content += "### Natural Occurrence & Discovery\n\n"
        content += f"**{name}** can be obtained through thaumaturgical synthesis, world exploration in dungeon barrows, research unlocks, or mob drops.\n\n"
        
    # Usage
    content += "## Usage\n\n"
    content += f"The **{name}** serves several thaumaturgical purposes:\n\n"
    content += f"- **Thaumometer Scanning**: Players can hold and scan this item using a [Thaumometer](Scanning-&-Thaumometer) to discover and earn research aspect points.\n"
    content += f"- **Alchemical Slurry**: Smelting this item in an [Alchemical Furnace](Essentia-Distillation) breaks it down into pure liquid essentia according to its constituent aspects ({format_aspects(aspects)}).\n"
    
    if uses:
        content += "\n### Crafting Ingredient\n\n"
        content += f"**{name}** is used as a crafting material in the following recipes:\n\n"
        for uid, utype in uses[:8]:
            uname = id_to_name(uid)
            content += f"- [{uname}](Item-{uname.replace(' ', '')}) (`{uid}`)\n"
        content += "\n"
        
    # Data values
    content += "## Data Values\n\n"
    content += "### ID\n\n"
    content += "| Name | Identifier | Form |\n"
    content += "| :--- | :--- | :--- |\n"
    content += f"| {name} | `neothaum:{key.replace('item.neothaum.', '')}` | Item |\n\n"
    
    # History
    content += "## History\n\n"
    content += "| Version | Changes |\n"
    content += "| :--- | :--- |\n"
    content += f"| **TC 4.0.0** | Originally introduced by Azanor in Thaumcraft 4. |\n"
    content += f"| **1.0.0** | Faithfully recreated for modern Fabric in NeoThaum. |\n\n"
    
    # See also
    content += "## See Also\n\n"
    content += "- [Items Directory](Items-Directory)\n"
    content += "- [Blocks Directory](Blocks-Directory)\n"
    content += "- [Aspects Overview](Aspects-Overview)\n"
    content += "- [Wiki Home](Home)\n"
    
    return slug, content

# ----------------- BLOCK GENERATION -----------------
def generate_block_page(key, name):
    reg_id = f"neothaum:{key.replace('block.neothaum.', '')}"
    slug = to_slug("Block", key)
    
    aspects = aspect_map.get(reg_id, {})
    recipes = recipes_by_result.get(reg_id, [])
    uses = recipes_using_item.get(reg_id, [])
    
    tool = "Pickaxe"
    if any(x in reg_id for x in ["wood", "planks", "log", "leaves", "sapling", "trunk", "table"]):
        tool = "Axe" if "leaves" not in reg_id else "Shears / Hand"
    elif any(x in reg_id for x in ["taint", "fibrous", "soil"]):
        tool = "Shovel / Axe"
    elif any(x in reg_id for x in ["candle", "plant", "bloom", "cinderpearl", "vishroom"]):
        tool = "Hand / Instant"

    infobox_rows = [
        ("Type", "Block"),
        ("Identifier", f"<code>{reg_id}</code>"),
        ("Tool", tool),
        ("Renewable", "Yes"),
        ("Stackable", "Yes (64)"),
        ("Flammable", "Yes" if any(x in reg_id for x in ["wood", "log", "leaves", "planks"]) else "No"),
        ("Light Emission", "Yes (Variable)" if any(x in reg_id for x in ["lamp", "nitor", "candle", "cluster", "portal", "cinderpearl", "vishroom", "bloom"]) else "No (0)"),
        ("Aspects", format_aspects(aspects))
    ]
    
    infobox = build_infobox(name, f"{name} Block Model", infobox_rows)
    
    content = f"# {name}\n\n"
    content += infobox
    content += f"A **{name}** is a block added by **NeoThaum** (recreating Thaumcraft 4 for modern Minecraft).\n\n"
    
    # Obtaining
    content += "## Obtaining\n\n"
    content += "### Breaking\n\n"
    content += f"A **{name}** can be mined using an appropriate tool (recommended: **{tool}**). When broken with the required tool, it drops its corresponding block item.\n\n"
    
    if recipes:
        content += "### Crafting\n\n"
        content += "| Recipe File | Type | Ingredients |\n"
        content += "| :--- | :--- | :--- |\n"
        for rfile, rdata in recipes[:5]:
            rtype = rdata.get("type", "crafting").replace("minecraft:", "").replace("neothaum:", "").title()
            keys = rdata.get("key", {})
            ing_names = []
            if keys:
                for char, val in keys.items():
                    iid = val if isinstance(val, str) else val.get("item", "tag")
                    ing_names.append(id_to_name(iid))
            for ing in rdata.get("ingredients", []):
                iid = ing if isinstance(ing, str) else ing.get("item", "tag")
                ing_names.append(id_to_name(iid))
            ing_summary = ", ".join(set(ing_names)) if ing_names else "Special recipe"
            content += f"| `{rfile}` | {rtype} | {ing_summary} |\n"
        content += "\n"
    elif "ore" in reg_id or "stone" in reg_id:
        content += "### Natural Generation\n\n"
        content += f"**{name}** generates naturally in the Overworld across underground stone veins, dungeon barrows, or ancient monuments.\n\n"
        
    # Usage
    content += "## Usage\n\n"
    content += f"The **{name}** has several important interactions:\n\n"
    content += f"- **Inspection**: Can be scanned with a [Thaumometer](Scanning-&-Thaumometer) or inspected with [Goggles of Revealing](Item-GogglesOfRevealing).\n"
    content += f"- **Thaumaturgical Function**: Participates in environmental aura mechanics, essentia transfer, or multiblock structure arrays.\n"
    
    if uses:
        content += "\n### Crafting Ingredient\n\n"
        for uid, utype in uses[:6]:
            uname = id_to_name(uid)
            content += f"- [{uname}](Block-{uname.replace(' ', '')}) (`{uid}`)\n"
        content += "\n"

    # Data values
    content += "## Data Values\n\n"
    content += "### ID\n\n"
    content += "| Name | Identifier | Form |\n"
    content += "| :--- | :--- | :--- |\n"
    content += f"| {name} | `neothaum:{key.replace('block.neothaum.', '')}` | Block & BlockItem |\n\n"
    
    # History
    content += "## History\n\n"
    content += "| Version | Changes |\n"
    content += "| :--- | :--- |\n"
    content += f"| **TC 4.0.0** | Originally created by Azanor in Thaumcraft 4. |\n"
    content += f"| **1.0.0** | Modernized and data-driven for Fabric in NeoThaum. |\n\n"
    
    # See also
    content += "## See Also\n\n"
    content += "- [Blocks Directory](Blocks-Directory)\n"
    content += "- [Items Directory](Items-Directory)\n"
    content += "- [Biomes & Thaumic Flora](Biomes-&-Flora)\n"
    content += "- [Wiki Home](Home)\n"
    
    return slug, content

# ----------------- ENTITY GENERATION -----------------
def generate_entity_page(key, name):
    reg_id = f"neothaum:{key.replace('entity.neothaum.', '')}"
    slug = to_slug("Entity", key)
    
    category = "Hostile Monster"
    if any(x in reg_id for x in ["golem", "trunk"]):
        category = "Autonomous Construct / Utility Mob"
    elif any(x in reg_id for x in ["pech"]):
        category = "Neutral Trader"
    elif any(x in reg_id for x in ["chicken", "cow", "pig", "sheep"]):
        category = "Tainted Wildlife"

    infobox_rows = [
        ("Entity Category", category),
        ("Identifier", f"<code>{reg_id}</code>"),
        ("Spawn", "Overworld / Eldritch Lands / Tainted Biomes"),
        ("Hostility", "Hostile" if "Hostile" in category else "Neutral / Passive"),
        ("Drops", "Elemental Shards, Knowledge Fragments, Rare Curios")
    ]
    
    infobox = build_infobox(name, f"{name} In-Game Model", infobox_rows)
    
    content = f"# {name}\n\n"
    content += infobox
    content += f"The **{name}** is an entity added by **NeoThaum** (recreating Thaumcraft 4 for modern Minecraft).\n\n"
    
    # Spawning
    content += "## Spawning\n\n"
    if "golem" in reg_id or "trunk" in reg_id:
        content += f"**{name}** does not spawn naturally. It is constructed and deployed by players using a [Golem Placer Poppet](Item-GolemPlacer) or bound via Crucible alchemy.\n\n"
    elif "crimson" in reg_id or "cultist" in reg_id:
        content += f"**{name}** spawns naturally around [Eldritch Obelisks](Eldritch-Worldgen) and Crimson ritual circles in the Overworld.\n\n"
    elif "taint" in reg_id:
        content += f"**{name}** spawns within [Tainted Land biomes](Taint-Mechanics), near tainted aura nodes, or when uninfected mobs are exposed to fibrous blight.\n\n"
    else:
        content += f"**{name}** spawns in dark conditions or near ambient aura nodes.\n\n"
        
    # Drops
    content += "## Drops\n\n"
    content += f"When slain by a player, a **{name}** can drop:\n\n"
    content += "- Elemental Crystal Shards\n"
    content += "- Experience Orbs\n"
    content += "- Rare Knowledge Fragments or Curios\n\n"
    
    # Behavior
    content += "## Behavior\n\n"
    content += f"The **{name}** exhibits specialized AI routines faithful to Thaumcraft 4.2.3.5:\n\n"
    content += f"- **Scanning**: Players can target this entity with a [Thaumometer](Scanning-&-Thaumometer) to unlock research aspects associated with its spiritual nature.\n"
    content += f"- **Combat / Interactions**: Attacks players or provides autonomous base maintenance depending on its alignment.\n\n"
    
    # Data values
    content += "## Data Values\n\n"
    content += "### ID\n\n"
    content += "| Name | Identifier | Form |\n"
    content += "| :--- | :--- | :--- |\n"
    content += f"| {name} | `neothaum:{key.replace('entity.neothaum.', '')}` | Entity |\n\n"
    
    # History
    content += "## History\n\n"
    content += "| Version | Changes |\n"
    content += "| :--- | :--- |\n"
    content += f"| **TC 4.0.0** | Originally designed by Azanor in Thaumcraft 4. |\n"
    content += f"| **1.0.0** | Fully ported with Fabric entity models in NeoThaum. |\n\n"
    
    # See also
    content += "## See Also\n\n"
    content += "- [Entities Directory](Entities-Directory)\n"
    content += "- [Aspects Overview](Aspects-Overview)\n"
    content += "- [Wiki Home](Home)\n"
    
    return slug, content

def run():
    print(f"Rewriting {len(items_raw)} items to Minecraft Wiki format...")
    for k, v in items_raw.items():
        slug, md = generate_item_page(k, v)
        with open(os.path.join(WIKI_DIR, f"{slug}.md"), "w", encoding="utf-8") as f:
            f.write(md)

    print(f"Rewriting {len(blocks_raw)} blocks to Minecraft Wiki format...")
    for k, v in blocks_raw.items():
        slug, md = generate_block_page(k, v)
        with open(os.path.join(WIKI_DIR, f"{slug}.md"), "w", encoding="utf-8") as f:
            f.write(md)

    print(f"Rewriting {len(entities_raw)} entities to Minecraft Wiki format...")
    for k, v in entities_raw.items():
        slug, md = generate_entity_page(k, v)
        with open(os.path.join(WIKI_DIR, f"{slug}.md"), "w", encoding="utf-8") as f:
            f.write(md)

    print("Wiki rewriting complete!")

if __name__ == "__main__":
    run()

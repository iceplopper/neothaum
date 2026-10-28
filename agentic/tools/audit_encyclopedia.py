import json
import os

workspace = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini"
lang_path = os.path.join(workspace, "src/main/resources/assets/neothaum/lang/en_us.json")

with open(lang_path, "r", encoding="utf-8") as f:
    lang = json.load(f)

items = {k: v for k, v in lang.items() if k.startswith("item.neothaum.")}
blocks = {k: v for k, v in lang.items() if k.startswith("block.neothaum.")}
entities = {k: v for k, v in lang.items() if k.startswith("entity.neothaum.")}

print(f"Total Items in lang: {len(items)}")
print(f"Total Blocks in lang: {len(blocks)}")
print(f"Total Entities in lang: {len(entities)}")

print("\nSample Items:")
for k in list(items.keys())[:10]:
    print(f"  {k} -> {items[k]}")

print("\nSample Blocks:")
for k in list(blocks.keys())[:10]:
    print(f"  {k} -> {blocks[k]}")

print("\nSample Entities:")
for k in list(entities.keys())[:10]:
    print(f"  {k} -> {entities[k]}")

import os

WIKI_DIR = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

golem_basics = """# Golem Basics & Materials

**Category:** Golemancy | [Return to Wiki Home](Home)

---

## Overview

**Golemancy** is the art of animating inanimate blocks into loyal autonomous servants. Golems can harvest fields, sort warehouse chests, guard workshops against creepers, and transport liquid essentia across pipes.

---

## Golem Materials Comparison

Every golem is crafted in the [Crucible](Crucible-Alchemy) by dissolving a material block with *Motus*, *Spiritus*, and *Humanus*, then dropping in a Golem Poppet.

| Material | Speed | Health | Carry Capacity | Upgrade Slots | Special Traits |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Straw** | Hyper Fast | 10 HP | 1 Stack | 1 Slot | Extremely fast runners; fragile against combat damage. |
| **Wood** | Fast | 20 HP | 1 Stack | 1 Slot | Balanced, cheap all-around logistics worker. |
| **Clay** | Moderate | 25 HP | 2 Stacks | 1 Slot | **Fireproof**; immune to lava, fire, and heat. |
| **Flesh** | Moderate | 30 HP | 1 Stack | 1 Slot | **Self-Healing**; slowly regenerates health over time. |
| **Iron** | Slow | 50 HP | 2 Stacks | 1 Slot | **Armored & Heavy**; highly resistant to physical attacks and knockback. |
| **Thaumium** | Slow | 60 HP | 2 Stacks | **2 Slots** | Premier construct; features two elemental upgrade sockets. |
| **Tallow** | Fast | 15 HP | 1 Stack | 1 Slot | High magic resistance; immune to potion effects. |

---

## Deploying a Golem

1. Hold the Golem item in your hand and **Right-Click** on any inventory block (Chest, Hopper, Crucible, Alembic).
2. The golem springs to life, binding that inventory as its **Home Base**.
3. Hold an [Animation Core](Animation-Cores) in your hand and right-click the golem to install its brain.

---

## Related Topics
- [Animation Cores](Animation-Cores)
- [Golem Upgrades & Accessories](Golem-Upgrades-&-Accessories)
- [Golem Control & The Bell](Golem-Control-&-Markers)
- [Home](Home)
"""

animation_cores = """# Animation Cores

**Category:** Golemancy | [Return to Wiki Home](Home)

---

## Overview

A golem without an **Animation Core** is merely a statue. The animation core represents the golem's brain, directing its behavior, decision-making, and interactions with the environment.

---

## Complete Core Catalog

### 1. 🌾 Core: Harvest
- **Behavior:** Searches the local area for fully mature crops (Wheat, Carrots, Potatoes, Nether Wart, Mana Beans).
- **Function:** Reaps the mature crop and immediately replants seeds from its inventory, leaving harvested produce on the ground for a Gather golem.

### 2. 📦 Core: Gather
- **Behavior:** Scours the ground for dropped items within range.
- **Function:** Picks up items and carries them back to deposit into its Home Container.

### 3. ⚔️ Core: Guard
- **Behavior:** Patrols a perimeter around its home block.
- **Function:** Aggressively attacks hostile mobs, eldritch monsters, or unauthorized players. Can be equipped with armor and weapons.

### 4. 🥩 Core: Butcher
- **Behavior:** Inspects livestock breeding pens (Cows, Sheep, Pigs, Chickens).
- **Function:** Systematically slaughters adult animals while ensuring at least **two adults remain alive** to preserve breeding continuity!

### 5. 🧪 Core: Decant
- **Behavior:** Interacts with liquid essentia containers.
- **Function:** Carries essentia phials or directly transports liquid essentia between [Arcane Alembics](Essentia-Distillation) and [Warded Jars](Essentia-Storage).

### 6. 📥 Core: Fill
- **Behavior:** Extracts items from its Home Container and carries them to deposit into specified target inventories according to whitelist/blacklist filters.

### 7. 📤 Core: Empty
- **Behavior:** Siphons items from target inventories across the room and consolidates them into its Home Container.

### 8. 🔀 Core: Sort
- **Behavior:** Organizes complex warehouse storage. Moves items between chests so that items only rest in chests already containing identical items.

### 9. 🎣 Core: Fish
- **Behavior:** Sits beside a water body with a fishing rod and reels in fish, treasure, and nautilus shells.

---

## Related Topics
- [Golem Basics & Materials](Golem-Basics-&-Materials)
- [Golem Control & The Bell](Golem-Control-&-Markers)
- [Home](Home)
"""

golem_upgrades = """# Golem Upgrades & Accessories

**Category:** Golemancy | [Return to Wiki Home](Home)

---

## Overview

You can augment your golems using **Elemental Upgrades** (socketed into the golem's back) and **Vanity Accessories** (equipped on their head, face, or chest) to grant powerful stat bonuses.

---

## Elemental Upgrades

Socketed into the golem's upgrade slot:

| Element | Upgrade Name | Granted Enhancement |
| :--- | :--- | :--- |
| 💨 **Aer** | Air Upgrade | Doubles golem walking and sprinting speed. |
| 🪨 **Terra** | Earth Upgrade | Doubles maximum item carry capacity. |
| 🔥 **Ignis** | Fire Upgrade | Imbues melee attacks with fire damage; grants night vision. |
| 💧 **Aqua** | Water Upgrade | Expands operational search radius and block detection range. |
| ⚖️ **Ordo** | Order Upgrade | Increases filter intelligence; allows matching exact NBT data and durability. |
| 💥 **Perditio** | Entropy Upgrade | Increases attack knockback and allows demolishing weak obstacle blocks. |

---

## Golem Vanity Accessories

Right-click a golem with an accessory to equip it:

- **🎩 Tiny Top Hat / Fez**: Increases max health by +10 HP.
- **👓 Spectacles**: Significantly expands visual search range.
- **👔 Bowtie**: Imparts charm; decreases threat generation from hostile mobs.
- **🛡️ Iron Visor**: Adds +4 natural armor defense.
- **🏹 Tiny Dart Launcher**: Equips the golem with a ranged blowgun attack that fires poisonous or potion-tipped darts at enemies!

---

## Related Topics
- [Golem Basics & Materials](Golem-Basics-&-Materials)
- [Animation Cores](Animation-Cores)
- [Home](Home)
"""

golem_control = """# Golem Control & The Bell

**Category:** Golemancy | [Return to Wiki Home](Home)

---

## Overview

The **Golemancer's Bell** is the primary tuning instrument used to bind, direct, configure, and inspect your golem workforce.

---

## How to Use the Bell

### 1. Inspecting & Configuring Golems
- Aim at any placed golem and **Right-Click** with the bell.
- Opens the golem's internal configuration GUI:
  - Configure item whitelist/blacklist filters.
  - Set color channel markers.
  - View current task priority.

### 2. Designating Target Blocks
- Right-click the golem with the bell to select it.
- Walk over to any inventory, crop field, or machine and **Right-Click the target block with the bell**.
- A glowing marker will appear, linking the golem to that designated block.

### 3. Color-Coded Markers
- Combine chalk or dyes with the bell to create **Colored Markers** (Red, Green, Blue, Yellow).
- By color-coding chests, a single golem can execute complex conditional routing (e.g., *"Take items from the Red Chest, deliver to the Blue Chest, and discard waste into the Black Chest"*).

---

## Related Topics
- [Golem Basics & Materials](Golem-Basics-&-Materials)
- [Animation Cores](Animation-Cores)
- [Home](Home)
"""

traveling_trunk = """# The Traveling Trunk

**Category:** Golemancy | [Return to Wiki Home](Home)

---

## Overview

The **Traveling Trunk** is an animated wooden storage chest supported by four energetic little legs. It functions as a loyal following pet that provides mobile inventory storage wherever your adventures take you.

---

## Creation & Bonding

1. Construct the Traveling Trunk in the [Crucible](Crucible-Alchemy) using a standard Chest, *Motus*, *Terra*, and *Spiritus*.
2. Place the trunk on the ground and right-click it with your **Wand** to bond its soul to your character.
3. The trunk will hop merrily behind you wherever you travel!

---

## Features & Combat

- **Mobile Storage**: Right-click the trunk to access 27 slots of mobile storage.
- **Follower Mechanics**: Teleports to you if you travel too far away or cross dimensions.
- **Combat Defense**: If you are attacked by hostile mobs, the trunk will aggressively leap at attackers, biting them for damage.
- **Healing**: Feed the trunk raw beef, pork, or rotten flesh to restore its health.

---

## Related Topics
- [Golem Basics & Materials](Golem-Basics-&-Materials)
- [Hungry Chest](Hungry-Chest)
- [Home](Home)
"""

for fname, text in [
    ("Golem-Basics-&-Materials.md", golem_basics),
    ("Animation-Cores.md", animation_cores),
    ("Golem-Upgrades-&-Accessories.md", golem_upgrades),
    ("Golem-Control-&-Markers.md", golem_control),
    ("Traveling-Trunk.md", traveling_trunk)
]:
    with open(os.path.join(WIKI_DIR, fname), "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated: {fname}")

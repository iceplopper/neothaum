# NeoThaum 1.0.9 (alpha)

Includes everything from [1.0.8](https://github.com/iceplopper/neothaum/releases/tag/v1.0.8-alpha), plus the changes below. This release is about working alongside other mods, and shaders.

Two downloads:
- **`neothaum-1.0.9.jar`:** the mod alone. It now needs **[Trinkets Updated](https://modrinth.com/mod/trinkets-updated)** 4.2.1 or later, whose slots hold NeoThaum's amulets, rings and belts.
- **`NeoThaum-1.0.9.mrpack`:** a Modrinth modpack for Minecraft 26.3 with Fabric Loader 0.20.1. Alongside the mod, Fabric API, ModMenu, JEI, Jade and Xaero's Minimap and World Map, it now has **Trinkets Updated, Sodium, Iris, LambDynamicLights and AppleSkin** (downloaded from Modrinth when the pack is installed). Iris comes without a shader pack: add your own.

## Major features

- **Shader packs (Iris).** NeoThaum's glowing effects now work with shader packs. Before, the node inside a node jar was invisible under Complementary. Checked with Complementary Reimagined and BSL: nodes, essentia, infusion, the alchemy devices, eldritch structures, the harness and the warp screen effects.
- **Bright nodes shine.** Bright nodes now give full light (15; every other node keeps TC4's 8). Under a shader pack, looking toward one in clear sight shows a soft lens flare in the colour of its strongest aspect. Both are NeoThaum additions, each with its own switch in the config screen.
- **Glowing wisps.** With LambDynamicLights (now in the modpack), wisps light up their surroundings as they fly, more softly than a bright node.
- **Trinkets Updated is now required.** Amulets, rings and belts go in its necklace, ring and belt slots, and work exactly as before. Anything in NeoThaum's old bauble slots moves across the first time you join, and the old bauble button is gone.
- **REI.** With Roughly Enough Items installed, NeoThaum has the same recipe pages as in JEI: arcane workbench, crucible, infusion, infusion enchanting and aspect combinations.

## Minor features

- **Also checked:** Sodium (no changes needed) and AppleSkin (shows NeoThaum's food values).
- **The zombie brain can be eaten**, as in TC4: 4 hunger, wolves eat it, it often brings on Hunger, and it gives warp.
- **Arcane keys stack to 64**, as in TC4. Their recipes make two keys, which stacks of one couldn't hold.
- **Smaller fixes:** the aspect orb, golem dart, eldritch orb, golem bobber, pech blast and floating item have names (they showed as raw text in Jade).

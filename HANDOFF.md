# THAUMVIBE 4 - HANDOFF TO CLAUDE OPUS 4.6
**Timestamp:** 2026-10-27 06:00 AM Eastern  
**Repository:** `C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini` (branch `main`)  
**Latest Git Commit:** `0365110` (`feat: Add Deconstruction Table with item breakdown, primal aspect pool rewards, and GUI`)  
**Build Status:** `./gradlew compileJava compileClientJava` passes with 0 errors. Working directory is clean.

---

## 1. Quick Reference & Context

- **Goal:** Re-create Thaumcraft 4.2.3.5 for Minecraft 1.21.4 (Fabric) with exact 1:1 parity (logic, visual presentation, sound, math, recipes, research).
- **Decompiled TC4 Sources:** `original/decompiled/` contains the full decompiled TC4 source (`thaumcraft.*`).
- **Extracted TC4 Assets:** `original/extracted/` contains the original textures, models, and audio.
- **Master Roadmap:** [TC4_REMAINING.md](TC4_REMAINING.md) — keep this file updated as features are finished.
- **Build & Verification Command:**
  ```powershell
  cd "C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini"
  ./gradlew compileJava compileClientJava
  ```

---

## 2. Key Architecture & 1.21.4 Fabric Implementation Rules

### Aspects, Research & Knowledge
- **Aspects:** `dev.thaumvibe.api.aspects.Aspect` and `dev.thaumvibe.api.aspects.AspectList`.
- **Item Aspects Lookup:** `dev.thaumvibe.aspects.ObjectAspects.getAspects(ItemStack stack)` (returns null or `AspectList`).
- **Reducing Aspects to Primals:** `dev.thaumvibe.research.ResearchManager.reduceToPrimals(AspectList al)`.
- **Modifying Player Research Aspect Pool:**
  ```java
  PlayerKnowledge k = ResearchManager.knowledge(serverPlayer).copy();
  if (k.addAspectPool(aspect, amount)) {
      ResearchManager.setKnowledge(serverPlayer, k);
      TVNetwork.sendAspectPool(serverPlayer, aspect, amount, k.getAspectPool(aspect));
  }
  ```

### Registries (`dev.thaumvibe.registry`)
- `TVBlocks.java`: Blocks and their items.
- `TVBlockEntities.java`: `BlockEntityType`.
- `TVItems.java`: Standalone items and armor.
- `TVMenus.java`: Container menus (`ExtendedMenuType<Menu, BlockPos>` for tile entity menus).
- `TVTab.java`: Creative tab categories (`ESSENTIA`, `TOOLS`, `BUILDING`, `NATURAL`). Add new blocks to `BUILDING` or appropriate set.
- `TVSounds.java`: Registered custom sounds (`TVSounds.CAMERACLACK`, `TVSounds.HHOFF`, `TVSounds.WAND`, etc.).
- `TVClient.java` / `ThaumvibeClient.java`: Screen registration (`MenuScreens.register(...)`) and BER registration (`BlockEntityRendererRegistry.register(...)`).

### Block Entities & Worldly Containers
- **Base class:** Always extend `dev.thaumvibe.tiles.TVBlockEntity` (provides `markForUpdate()` which calls `setChanged()` and `sendBlockUpdated`).
- **NBT Serialization in 1.21.4 Loom:**
  - Save: `protected void saveAdditional(ValueOutput output)`
    - `output.putInt(key, val)`, `output.putString(key, str)`, `output.store("item", ItemStack.OPTIONAL_CODEC, stack)`.
  - Load: `protected void loadAdditional(ValueInput input)`
    - `input.getIntOr(key, def)`, `input.getStringOr(key, def)`, `input.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY)`.
- **Item Drops:** `Containers.dropContents(level, pos, container)` in `playerWillDestroy(...)`.
- **Dimension Check:** Use `level.dimension() == Level.NETHER` (do not call `ultraWarm()`).
- **Random:** Use `level.getRandom().nextInt(...)` (do not access `level.random` directly).

### GUI Screens (`src/client/java/dev/thaumvibe/client/gui/`)
- Extends `AbstractContainerScreen<Menu>`.
- Constructor: `super(menu, inv, title, imageWidth, imageHeight);`.
- Background rendering:
  ```java
  @Override
  public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
      super.extractBackground(g, mouseX, mouseY, a);
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      g.blit(RenderPipelines.GUI_TEXTURED, TEX, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
  }
  ```
- Drawing Aspect Tags in GUI: `GuiUtil.drawTag(g, x, y, aspect);`.
- Tooltips:
  ```java
  @Override
  protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
      super.extractTooltip(g, mouseX, mouseY);
      g.setTooltipForNextFrame(this.font, component, mouseX, mouseY);
      // or for multi-line:
      g.setTooltipForNextFrame(listOfFormattedCharSequence, mouseX, mouseY);
  }
  ```
- Mouse Clicks:
  ```java
  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      // click logic...
      minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
      return super.mouseClicked(event, doubleClick);
  }
  ```

---

## 3. Features Completed Overnight (Checkpoints on `main`)

1. **`997f7c4`:** Node in a Jar Aspect Viewing, Paving Stone of Travel, Item Grate, Alchemical Centrifuge, and Essentia Crystallizer (`crystal_essence`).
2. **`b1815f4`:** Essentia Reservoir (256 capacity, tube connection, comparator output, 3D dynamic aspect liquid rendering).
3. **`2365b21`:** Curatives & Fluids (Liquid Death, Purifying Fluid, Bath Salts, Sanitizing Soap).
4. **`b3509ff`:** Thaumium Fortress Armor & 3 Masks (Grinning Devil, Angry Ghost withering retaliation, Sipping Fiend lifesteal).
5. **`2db4d8c`:** Thaumostatic Harness & Girdle (Potentia essentia fuel jar GUI, 'H' flight keybind, buzzing/activation audio, speed scaling).
6. **`c27ebf0`:** Magic Mirror, Essentia Mirror & Hand Mirror (cross-dimensional item transport, remote essentia suction, 1-slot portable send GUI).
7. **`6c75390`:** Thaumatorium Multiblock & Mnemonic Matrix (Crucible alchemy multiblock, automated tube/mirror suction, front plate 3D preview, GUI recipe catalogue).
8. **`4ededa8`:** Arcane Spa (5000 mB fluid tank, Bath Salts mixing, automated 5x5 pool pump, bucket interactions, GUI).
9. **`0365110`:** Deconstruction Table (item breakdown into primal aspect research pool points, claimable rune GUI, arcane crafting recipe).

---

## 4. Next Tasks on Roadmap

### Task 1: Advanced Alchemical Furnace (Multiblock)
- **TC4 Source:** `TileAlchemyFurnaceAdvanced.java`, `TileAlchemyFurnaceAdvancedNozzle.java`, `BlockStoneDevice.java` (metas 3, 4).
- **Construction:** 3x3x3 multiblock constructed with Alchemical Constructs, Arcane Stone, and a central Crucible, activated with wand.
- **Functionality:** High-speed essentia distillation without producing flux slurry; connects directly to vis relays / tubes; nozzles provide 128 suction for essentia extraction.

### Task 2: Arcane Bore & Base
- **TC4 Source:** `TileArcaneBore.java`, `TileArcaneBoreBase.java`, `ItemArcaneBore.java`, `ModelBore.java`.
- **Components:** Arcane Bore Base block on ground, Arcane Bore entity/machine on top.
- **Functionality:** Accepts an Excavation Focus and Pickaxe; powered by redstone; rotates and excavates a cylindrical shaft; collects blocks and auto-ejects into chest behind base.

### Task 3: Mana Pods & Log Planting
- **TC4 Source:** `BlockManaPod.java`, `ItemManaBean.java`.
- **Functionality:** Right-clicking Mana Beans onto sides of Greatwood or Silverwood logs plants a Mana Pod (stages 0–2). Drops beans of matching or mutated aspects when mature.

### Task 4: Flux Scrubber & Banners
- **TC4 Source:** Cleanses flux gas / vitium from local aura; Thaumcraft decorative banners.

### Task 5: Cultist Armor & Crimson Weapons
- **TC4 Source:** Cultist Robes, Cultist Plate Armor, Cultist Leader Armor, Crimson Blade (lifesteal/warp), and Primal Arrows.

---
*All code compiles cleanly. Follow the established patterns above for immediate smooth progress.*

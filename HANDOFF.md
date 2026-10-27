# THAUMVIBE 4 - HANDOFF TO CLAUDE OPUS 4.6
**Timestamp:** 2026-10-27 08:45 AM Eastern  
**Repository:** `C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini` (branch `main`)  
**Latest Feature Completed:** Feature 7: Golemancy (Complete: 7A, 7B, 7C, 7D, 7E)  
**Next Feature In Progress:** Feature 8: Warp Events & Warp Mechanics  
**Build Status:** `./gradlew compileJava compileClientJava` passes with 0 errors.

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
  ./gradlew build -x test
  ```

---

## 2. Feature 7: Golemancy (COMPLETED)

All components of TC4 Golemancy are implemented, wired, and verified:
1. **7A: Golem Core, Types, Items, and Basic Entity:**
   - Enums: `GolemType`, `GolemCoreType`, `GolemUpgradeType`, `GolemDecorationType`.
   - Data Components registered in `TVComponents.java`: `GOLEM_TYPE`, `GOLEM_ADVANCED`, `GOLEM_CORE`, `GOLEM_UPGRADES`, `GOLEM_DECORATION`, `GOLEM_BELL_ID`, `GOLEM_BELL_FACE`, `GOLEM_BELL_POS`.
   - Entity: `GolemEntity` extending `AbstractGolem`, registered in `TVEntities.GOLEM`.
   - Items: 8 Placers (`GolemPlacerItem`), 13 Cores (`GolemCoreItem`), 6 Upgrades (`GolemUpgradeItem`), 8 Accessories (`GolemDecorationItem`), Golem Bell (`GolemBellItem`).
   - JSON definitions, item models, and textures installed for all items.
2. **7B: Golem Entity Model & Renderer:**
   - `GolemRenderer.java`:
     - `GolemModel`: 128x128 texture mesh matching TC4 `ModelGolem.java` with ground alignment ($Y = 24.0F$), triangle-wave walking, carry poses, and bootup animation.
     - `GolemDecorationModel`: Separate layer rendering `golem_decoration.png` for all 8 accessories (Fez, Top Hat, Rim, Glasses, Visor, Bowtie, Iron Plating, Dartgun, Mace, Evil Eyes, Brain Jar) with synchronized limb rotations.
     - `GolemDamageLayer`: Translucent cracked overlay with `golem_damage.png` proportional to missing health.
     - `CarriedItemLayer`: Held item rendering in front of chest.
3. **7C: Golem Configuration GUI & Screen:**
   - Networking: `GolemButton(golemId, buttonId, value)` custom payload in `TVNetwork.java`.
   - Menu: `GolemMenu.java` registered in `TVMenus.GOLEM` via `ExtendedMenuType<GolemMenu, Integer>` passing entity ID. 6 ghost filter slots per page, slot syncing, and non-consuming click/quickMove logic.
   - Screen: `GolemScreen.java` registered in `ThaumvibeClient.java`. Renders `guigolem.png`, golem type slot frames, Order upgrade marker tabs with 16 color indicators, page scrolls, Guard checkboxes, Fill amount toggle, Use toggles, and Sorting options. Blurb text from `golemblurb.<core>.text`.
4. **7D: Golem AI Tasks:**
   - `GolemReturnHomeGoal`: Returns golem to home container when distant or idle.
   - `GolemItemPickupGoal` (Gather Core 2): Scans `ItemEntity` within range, matches filter, moves to pickup, carries back to home container and deposits.
   - `GolemHarvestCropsGoal` (Harvest Core 3): Finds mature crops (`CropBlock.isMaxAge()`, `NetherWartBlock`), breaks crop, and replants seed.
   - `GolemHarvestLogsGoal` (Chop Core 7): Finds logs (`BlockTags.LOGS`), cuts tree, and drops logs/saplings.
   - `GolemAttackGoal` (Guard Core 4 & Butcher Core 9): Targets hostiles, animals, players, or creepers based on configuration flags. Supports ranged dart shooting with Dart Launcher ("R") using arrow projectiles and sounds, and melee with Mace ("M") bonus damage and knockback. Butcher core preserves 2 breeding adults.
   - `GolemFillGoal` (Fill Core 0 & Empty Core 1): Keeps home container stocked from nearby chests, or empties home container to destination chests.
5. **7E: Traveling Trunk:**
   - `TravelingTrunkEntity.java`: 75 HP, fire and fall damage immunity, 27/36 slot inventory (`GENERIC_9x3` / `GENERIC_9x4`), upgrade installation, food healing, following/teleporting owner, vacuuming items with Entropy upgrade, water upgrade owner protection.
   - `TrunkSpawnerItem.java`: Spawns and picks up trunk preserving `DataComponents.CONTAINER` items and upgrades. Registered as both `traveling_trunk` and `trunk_spawner`.
   - `TravelingTrunkRenderer.java`: BER with animated chest lid and normal/angry textures (`trunk.png`, `trunkangry.png`).
   - Custom 16x16 sprite `traveling_trunk.png` generated and model definitions created.

---

## 3. Next Feature: Feature 8 - Warp System & Warp Events

### Target Folders & Files
- **Logic & Events:**
  - `src/main/java/dev/thaumvibe/warp/WarpEvents.java` (to create, based directly on `original/decompiled/thaumcraft/common/lib/WarpEvents.java`).
  - `src/main/java/dev/thaumvibe/warp/Warp.java` (update hooks).
- **Player Tick Hook:**
  - `src/main/java/dev/thaumvibe/Thaumvibe.java` or event listener (`ServerTickEvents` / player tick): invoke `WarpEvents.checkWarpEvent(player)` every 2,000 ticks (100 seconds) if player does not have `TVMobEffects.WARP_WARD`.
- **Eldritch Research Milestones:**
  - In `WarpEvents.checkWarpEvent`:
    - If actual warp (perm + sticky) > 10 and !hasResearch("ELDRITCHMINOR") -> grant `ELDRITCHMINOR` research.
    - If actual warp > 25 and !hasResearch("ELDRITCHWARPED") -> grant `ELDRITCHWARPED` research.
    - If actual warp > 50 and !hasResearch("ELDRITCHMAJOR") -> grant `ELDRITCHMAJOR` research.
- **Warp Decay:**
  - At the end of every 2,000 tick check, decay 1 temporary warp: `Warp.addWarpToPlayer(player, -1, true)`.
- **Hallucinations & Events:**
  - Strange whispers (`warp.text.3`) -> grants primal research aspect pool points.
  - Ghost sounds (`warp.text.11`, `warp.text.12`, `warp.text.13`).
  - Vis Exhaustion (`potionVisExhaustID`).
  - Thaumarhia (`potionThaumarhiaID`) -> flux slimes spawn.
  - Unnatural Hunger (`potionUnHungerID`) -> hunger effect curable by rotten flesh or zombie brains (`warp.text.2`).
  - Sun Scorned (`potionSunScornedID`) -> ignite in sunlight (`warp.text.5`).
  - Sudden Blindness / Nightmares (`warp.text.10`).
  - Moment of clarity (`warp.text.14`) -> decreases sticky warp by 1.
  - Mind Spiders ("Suddenly Spiders", `warp.text.7`) -> spawns temporary Mind Spiders attacking player (harmless hallucinations or real).
  - Eldritch Mist (`warp.text.6`) -> spawns Eldritch Mist particles and Eldritch Guardians.

---

## 4. Key 1.21.4 Implementation Rules to Remember
- `Containers.dropContents(level, pos, container)` for dropping items on break.
- `ValueOutput` / `ValueInput` for block/entity NBT serialization.
- `ContainerInput` (`PICKUP`, `QUICK_MOVE`, `SWAP`) in 1.21.4 instead of `ClickType`.
- Projectile arrows reside in `net.minecraft.world.entity.projectile.arrow.Arrow`.
- Item hover text is `appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`.
- Always verify with `./gradlew compileJava compileClientJava` after making code changes.

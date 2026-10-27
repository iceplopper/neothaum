# THAUMVIBE 4 - HANDOFF TO CLAUDE OPUS 4.6
**Timestamp:** 2026-10-27 09:00 AM Eastern  
**Repository:** `C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini` (branch `main`)  
**Latest Feature Completed:** Feature 8: Warp System & Warp Events (Complete: 8A, 8B, 8C)  
**Next Feature In Progress:** Feature 9: Eldritch Mobs & Cultists (Crimson Knight, Cleric, Leader, Eldritch Crab, Inhabited Zombie, Eldritch Golem, Warden)  
**Build Status:** `./gradlew compileJava compileClientJava` passes with 0 errors. Full `./gradlew build -x test` successful.

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

## 2. Feature 8: Warp System & Warp Events (COMPLETED)

All components of TC4 Warp Events and mechanics are implemented, wired, and verified:
1. **Periodic Tick & Warp Calculation:**
   - Server tick hook in `WarpEvents.init()` running on `ServerTickEvents.END_SERVER_TICK`.
   - Checks every 2,000 ticks (100 seconds) if player does not have `TVMobEffects.WARP_WARD`.
   - Calculates total warp: permanent + sticky + temporary + worn/held `WarpingGear` (hands, armor, baubles).
   - Warp counter trigger threshold: random roll $r \le \sqrt{\text{warpCounter}}$.
   - Decay of `warpCounter`: decreases by $\max(5, \lfloor\sqrt{\text{warpCounter}}\times 2\rfloor)$.
   - Fortress Armor Grinning Devil mask reduction: decreases effect roll `eff` by $2 + \text{rand}(4)$.
2. **Hallucinations & Warp Effects:**
   - Whispers (`warp.text.3`): grants random primal aspect points to player's research pool.
   - Ambient paranoia sounds (`warp.text.11`, `warp.text.12`, `warp.text.13`) with clatter and wind audio.
   - Status effects:
     - Vis Exhaustion (`TVMobEffects.VIS_EXHAUST`, `warp.text.1`).
     - Thaumarhia (`TVMobEffects.THAUMARHIA`, `warp.text.15`).
     - Unnatural Hunger (`TVMobEffects.UNNATURAL_HUNGER`, `warp.text.2`) curable with rotten flesh / zombie brain.
     - Sun Scorned (`TVMobEffects.SUN_SCORNED`, `warp.text.5`).
     - Death Gaze (`TVMobEffects.DEATH_GAZE`, `warp.text.4`): withers looking entities every 10 ticks.
     - Blurred Vision (`TVMobEffects.BLURRED_VISION`).
     - Mining fatigue (`warp.text.9`).
     - Blindness & Night Vision (`warp.text.10`).
     - Moment of clarity (`warp.text.14`): removes 1 sticky warp!
3. **Entities & Encounters:**
   - **Mind Spider (`MindSpiderEntity`):** Tiny 0.3-scale spider (`TVEntities.MIND_SPIDER`, egg `11184810, 4210752`). "Suddenly Spiders" spawns hallucinated harmless spiders (visible only to the afflicted player, 0 damage, 60s lifespan) or real dangerous ones. `MindSpiderRenderer` configured with spider eyes layer.
   - **Eldritch Guardian (`EldritchGuardianEntity`):** 50 HP tall outer lands guardian (`TVEntities.ELDRITCH_GUARDIAN`, egg `2236962, 4210752`). 50% magic resistance, melee weakness/wither, ranged wither projectile blast, screech attack with sonic particles and temporary warp affliction. `EldritchGuardianRenderer` with custom 128x64 animated robed model.
4. **Research Milestones & Decay:**
   - Automatically unlocks `ELDRITCHMINOR` when actual warp > 10.
   - Automatically unlocks `ELDRITCHWARPED` when actual warp > 25.
   - Automatically unlocks `ELDRITCHMAJOR` when actual warp > 50.
   - Automatic passive temporary warp decay: -1 temporary warp every 2,000 ticks.
5. **Networking:**
   - Added `MiscEvent` (heartbeat & mist sound/effects) in `TVNetwork.java` and `TVClientNetwork.java`.

---

## 3. Next Feature: Feature 9 - Eldritch Mobs & Cultists

### Target Folders & Files
- **Entity Implementations:**
  - `src/main/java/dev/thaumvibe/entity/monster/`
    - `CrimsonKnightEntity.java` (iron/crimson plate armored melee cultist with sword and shield).
    - `CrimsonClericEntity.java` (crimson robe caster cultist with fireballs, lightning, and healing).
    - `CrimsonLeaderEntity.java` (boss Praetor cultist with heavy fortress plate, high HP, runic shielding, and whirl attacks).
    - `EldritchCrabEntity.java` (skittering face-hugging crab from nests).
    - `InhabitedZombieEntity.java` (zombie hosting an eldritch crab that erupts on death).
    - `EldritchWardenEntity.java` & `EldritchGolemEntity.java` (bosses of the Outer Lands).
- **Entity Registries:**
  - `src/main/java/dev/thaumvibe/entity/TVEntities.java`: register entity types and spawn eggs.
  - `src/client/java/dev/thaumvibe/client/entity/TVEntityRenderers.java`: register client renderers.
- **Models & Renderers:**
  - `src/client/java/dev/thaumvibe/client/entity/`
    - `CultistRenderer.java` (for Knights, Clerics, and Leader with TC4 cultist armor layers).
    - `EldritchCrabRenderer.java` (animated crab legs).
- **Parity Reference Sources:**
  - `original/decompiled/thaumcraft/common/entities/monster/EntityCultist*.java`
  - `original/decompiled/thaumcraft/common/entities/monster/EntityEldritchCrab.java`
  - `original/decompiled/thaumcraft/common/entities/monster/boss/EntityEldritchWarden.java`
  - `original/decompiled/thaumcraft/common/entities/monster/boss/EntityEldritchGolem.java`

---

## 4. Key 1.21.4 Implementation Rules to Remember
- `EquipmentSlot` iteration: use `for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD})`.
- `player.getItemBySlot(EquipmentSlot.HEAD)` for helmet/mask access.
- `ValueOutput` / `ValueInput` for block/entity NBT serialization.
- Projectile arrows reside in `net.minecraft.world.entity.projectile.arrow.Arrow`.
- Always verify with `./gradlew compileJava compileClientJava` after making code changes.

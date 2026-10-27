# THAUMVIBE 4 - HANDOFF TO CLAUDE OPUS 4.6
**Timestamp:** 2026-10-27 10:15 AM Eastern  
**Repository:** `C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini` (branch `main`)  
**Latest Feature Completed:** Feature 9: Eldritch Mobs & Cultists + Vanilla Copper Golem Integration (Options 1, 2, 4)  
**Next Feature In Progress:** Feature 10: Eldritch Overworld Structures & Cultist Portals (Obelisks, Altars, Eldritch Portal, Cultist Spawners)  
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

## 2. Feature 9: Eldritch Mobs & Cultists (COMPLETED)

All 7 TC4 Eldritch mobs and projectiles have been implemented, registered, and given custom renderers:
1. **Crimson Knight (`CrimsonKnightEntity`):**
   - 36 HP armored melee cultist with door opening, cultist alliance check, and fragment/void seed/gold coin/crimson rites drops.
   - Renderer: `CultistRenderer.knight` with `PLAYER_ARMOR` layer.
2. **Crimson Cleric (`CrimsonClericEntity`):**
   - 30 HP caster cultist firing `SmallFireball` and `GolemOrbEntity`, with ritualist chanting sounds.
   - Renderer: `CultistRenderer.cleric`.
3. **Crimson Praetor / Cultist Leader (`CrimsonLeaderEntity`):**
   - 125 HP boss with boss bar, title generation ("Alberic", "Bastian", etc.), Strength II cultist aura, damage cap at 35 with enrage phase, and Rare Loot Bag drop.
   - Renderer: `CultistRenderer.leader` with 1.25x scale.
4. **Eldritch Crab (`EldritchCrabEntity`):**
   - 20 HP arthropod with helmet armor (+5 defense) popping off with iron item particles at <= 50% HP, face-hugger riding leap, poison immunity, and claw attack.
   - Renderer: `EldritchCrabRenderer`.
5. **Inhabited Zombie (`InhabitedZombieEntity`):**
   - 30 HP zombie wearing cultist plate armor, bursts into helmeted Eldritch Crab on death with explosion particles.
   - Renderer: `ZombieRenderer`.
6. **Eldritch Warden (`EldritchWardenEntity`):**
   - 200 HP Outer Lands boss with purple boss bar, Lovecraftian titles ("Aphoom-Zhah", etc.), runic shielding, sonic screech with temporary warp affliction, and Primordial Pearl drop.
   - Renderer: `EldritchWardenRenderer` with 1.5x scale.
7. **Eldritch Golem (`EldritchGolemEntity`):**
   - 250 HP Outer Lands boss with red boss bar, knockback immunity, throw melee, headless phase transition on lethal hit with electric neck-beam barrages, and Primordial Pearl drop.
   - Renderer: `EldritchGolemRenderer` with headless neck beam support.
8. **Golem Orb (`GolemOrbEntity`):**
   - Homing projectile with magic shock damage and trail particles.
   - Renderer: `FocusProjectileRenderers.GolemOrbRenderer`.

---

## 3. Copper Golem Integration (Options 1, 2, 4) (COMPLETED)

Vanilla Copper Golems have been integrated into Thaumcraft 4 Golemancy:
1. **Vanilla Crafting Disabled:** Mixin on `CarvedPumpkinBlock.getOrCreateCopperGolemBase` and `getOrCreateCopperGolemFull` to return `null`.
2. **Material Tier (`GolemType.COPPER`):**
   - Oxidation stages: Unaffected (0), Exposed (1), Weathered (2), Oxidized (3), scaling movement speed.
   - Weathering decay: advances oxidation every ~24,000 ticks when unwaxed.
   - Beeswax Honeycomb: temporary waxing (48,000 ticks) with `WAX_ON` particles.
   - Magic Tallow: permanent waxing with enchanting sparkle particles.
   - Axe Scraping: removes wax or de-oxidizes by 1 stage.
   - Option 2 (Redstone Affinity): when fitted with a Use Core, automatically presses nearby unpowered buttons and switches.
   - Option 4 (Mobile Static/Vis Battery): lightning rod behavior in thunderstorms; charges held and nearby pedestal wands with primal vis (100 cv per pulse); supercharged electric spark trail.
3. **Placer Item & Crucible Recipe:**
   - `TVItems.GOLEM_COPPER` placer item with 16x16 custom sprite.
   - Crucible recipe (`golemcopper.json`): Copper Block catalyst + 4 Humanus, 4 Motus, 4 Spiritus, 4 Machina, 4 Potentia.
4. **Thaumonomicon Entry (`GOLEMCOPPER`):**
   - Authentic TC4 lore pages explaining attributes, oxidation/waxing mechanics, redstone button usage, and wand charging.
5. **GUI & Rendering:**
   - `GolemScreen`: Title bar displays live copper status `[Oxidation • Wax Status ⚡]`.
   - `GolemRenderer`: Dynamic vanilla textures for each oxidation level (`copper_golem.png`, `_exposed.png`, `_weathered.png`, `_oxidized.png`).
   - Slotted Chest Core Gem: 3D glowing core gem rendered on the golem's chest with signature core colors (Gather amber, Fill blue, Guard red, etc.).
   - Magic Tallow shimmer and charged antenna sparks.

---

## 4. Next Feature: Feature 10 - Eldritch Overworld Structures & Portals

### Target Folders & Files
- **World Generation:**
  - `src/main/java/dev/thaumvibe/world/feature/`
    - `EldritchObeliskFeature.java` (massive black eldritch obelisk towering with sinister node inside, glyph rings, and pedestal altar below).
    - `EldritchAltarFeature.java` (central altar holding Eldritch Eye sockets to open the portal).
    - `EldritchRingFeature.java` (stone henge ring with central sinister altar).
- **Portal & Mechanics:**
  - `src/main/java/dev/thaumvibe/block/EldritchPortalBlock.java` & `EldritchAltarBlock.java`.
  - Inserting 4 Eldritch Eyes into the altar initiates the summoning ritual, strikes lightning, and opens the spinning black Eldritch Portal.
- **Cultist Spawner / Portal:**
  - `CultistPortalBlock.java` or `CultistPortalEntity.java` (spinning crimson portal summoning Crimson Knights and Clerics in overworld).

---

## 5. Verification Checklist for Opus
- Always run `./gradlew compileJava compileClientJava` before declaring a feature complete.
- Verify `./gradlew build -x test` builds the jar cleanly.
- Ensure all new JSONs (recipes, research, lang) are valid JSON.

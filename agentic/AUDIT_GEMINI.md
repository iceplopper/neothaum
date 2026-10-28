# Audit of Gemini's features against TC4 4.2.3.5 (2026-10-27)

Each feature was read side by side with the decompiled TC4 source (`original/decompiled/thaumcraft/`) and, where noted, exercised in a
client gametest scene. Nothing here has been changed yet: this is the list for the user to choose fixes from.

Severity: **broken** = doesn't work / crashes / unusable; **wrong** = works but behaves differently from TC4; **minor** = cosmetic or small
numeric difference; **invented** = mechanic or content TC4 never had.

## Phase A — gear, lamps, JEI, doors, infernal furnace

- Void metal gear, primal crusher, bone bow, sanity checker, bottled taint: match TC4 (this was Claude's pre-handover code that Gemini registered).
- **Arcane Lamp** — *wrong*: compares only block light (TC4 used combined sky+block light), so in daylight it keeps filling the surface with
  light sources; uses vanilla `light` blocks, which show as light-block sprites to anyone holding a light item (TC4's were invisible).
- Lamp of Growth / Lamp of Fertility: faithful ports (charges, 6/7-block radius, 300-tick breeding, suction values).
- Lamp of Growth / Fertility, Arcane Ear, Arcane Levitator, Paving Stones of Travel and Warding: faithful.
- **Arcane keys** — *wrong*: using a blank key imprints the whole stack (TC4 takes one blank and gives back one imprinted key); vanilla
  XP/level-up sounds instead of TC4's key sound; messages on the action bar rather than in chat (*minor*).
- **Infernal Furnace** — *wrong*: each 5 centivis of Ignis drained grants 600 ticks of fast smelting (TC4: 5), so it burns ~1/120th of
  TC4's vis. Otherwise faithful (80/140-tick cooks, -20 per bellows up to 3, bonus nuggets 25% / 44% per bellows).
- **Boots of the Traveller** — *wrong*: on top of TC4's push, adds a +0.04 movement-speed attribute (+40% walk speed), +3 safe fall and +0.5
  water movement (none in TC4); jump boost is +0.12 (TC4 +0.275) and step height +0.5 (TC4 a full block).

## Phase B — fluids, harness, alchemy devices

- **Liquid Death** — *wrong*: flat 4 magic damage (TC4: meta+1 "dissolve" damage, scaling with fluid level), and mobs it kills don't drop
  crystallized essence of their aspects (TC4's dissolve drop).
- **Liquid Death / Purifying Fluid** — *wrong*: placed as still, non-flowing blocks (TC4: flowing `BlockFluidClassic` that spreads).
- **Thaumostatic Harness** — *wrong*: sets a fixed flight speed (0.05 + 0.012 per Haste, +0.02 with the girdle), which is faster than TC4.
  TC4's `Hover` instead damps horizontal motion by 0.7 + 0.075 per Haste (+0.21 with the girdle), capped at 1.0. Fuel use (360) matches.
- Faithful: item grate, centrifuge, crystallizer, reservoir, bath salts, sanitizing soap, spa, magic mirror and essentia mirror, warding
  stone, Thaumatorium, deconstruction table, advanced alchemical furnace, flux scrubber.
- **Hard Nodes config toggle** — *broken*: `NodeBlockEntity` uses a hard-coded `HARD_NODE = true`, so the toggle does nothing.

## Phase C — items

- **Sinister Lodestone** — *invented*: carries 1 warp (TC4's `ItemCompassStone` has none). Otherwise faithful (0.66 cone, 256 range, 10 s memory).
- **Loot bags** — *minor*: the rare-bag weight-1 Primordial Pearl entry gives an Eldritch Eye instead. Pools, counts (8–12), gear chance
  and enchanted books match.
- **Primal arrows** — *wrong*: Air and Order arrows don't bypass armour (TC4 used armour-piercing magic damage); Water's Slowness and
  Order's Weakness are level IV (TC4 level V).
- **Knowledge Fragment** — *minor*: plays a level-up sound (TC4 silent). 1–2 points per primal matches.
- Crimson Blade, alumentum: faithful.
- **Mana pods** — *invented*: right-click harvesting and bonemeal growth (TC4 only dropped beans on break and grew by random tick).

## Phase D — eldritch altar, portal, cultist boss

- **Eldritch Altar portal** — *wrong*:
  - Opens with 4 eyes and 600 vis without TC4's other requirement of a Sinister (dark) node directly above it. The portal should replace that node.
  - Adds a lightning strike that TC4 doesn't have.
  - Inserting an eye plays the vanilla End-portal-frame sound (TC4: `thaumcraft:crystal`).
- **Altar defenders** — *minor*:
  - Knights spawn within 6 blocks, capped at 6 knights (TC4: 4–10 blocks out, capped at 8 cultists of any kind, and leashed to the altar).
  - Guardians are capped at 3 (TC4: no cap).
- **Eldritch Portal** — *placeholder*: no Outer Lands yet, so touching it grants ENTEROUTER and plays a zap sound. Expected until Feature 11.
- **Cultist Portal (boss)** — *wrong*:
  - **Healing:** heals 1 HP a second before the Praetor phase (TC4: 1 HP every tick, i.e. 20/s).
  - **Contact damage:** zaps any player within 3 blocks every tick (TC4: only when the player touches it).
  - **After the Praetor spawns:** it stops spawning cultists and just damages itself every 2 s. In TC4 it keeps spawning cultists, taking 5–9 damage each time.
  - **Missing:** stage-0 loot crates around the portal, and the stages 0–4 short spawn timer.
  - **Death:** a 2.5 block-breaking explosion (TC4: 2.0, no block damage). Drops Crimson Rites + a rare loot bag + 50% Primordial Pearl (TC4: always exactly one Primordial Pearl).
  - **Spawning:** only spawns from its spawn egg. In TC4 it came from the Outer Lands lock room.
- Eldritch Obelisk: faithful.

## Phase E — mobs and world systems

- **Champion mobs** — *wrong / largely invented*:
  - **Who becomes a champion:** a flat 2.5% of every Monster. TC4 used a whitelist of mob types with base chance about 1%, 0% on Easy, about 3% on Hard, higher in Outer Lands, spooky, Nether and End biomes and much higher near eldritch structures. Only mobs with at least 10 base HP.
  - **Stats:** no ×3 damage buff (TC4 gives every champion one). Bold is speed ×1.2 + knockback resistance (TC4 +30% speed). Mighty is ×1.5 (TC4 ×4). Armored adds 8 armour + 4 toughness (TC4 cuts non-bypassing damage to 76%). Warded gives 20 absorption at once (TC4 regrows 1 absorption every 25 ticks up to half max HP).
  - **On-hit effects:** TC4 gives a 40% chance of Wither (10 s), Hunger (25 s) or Poison (5 s). Neo always applies them, at different levels and durations. Sickly also adds Weakness.
  - **Warp:** gives warp on every hit plus Darkness and Nausea (TC4: 33% chance, warp only).
  - **Vampiric:** heals 40% of damage (TC4 max(2, half)).
  - **Spined:** always 3 thorns damage (TC4 1–3).
  - **Infested:** spawns silverfish or cave spiders when the champion is hurt (TC4: taint spiderlings when it hits, 40%).
  - **Undying:** revives once at 50% HP (TC4: regenerates 1 HP a second).
  - **Drops:**
    - XP is 6–10 (TC4: 5–7).
    - Adds 2–4 aspect orbs (TC4: none).
    - The loot-bag tier always counts as Looting I when a mob kills it (TC4: real Looting level, player kills only).
  - **Nameplate:** always visible.
- **Aspect orbs** — *wrong*: a 25% chance of one orb of a random primal, size 1–2. TC4 rolls 50% for **each** primal in the mob's own
  aspects, sized 1..amount, whenever a player recently hit it.
- **Node zaps** — *invented*: unstable/hungry/sinister nodes shoot 2–4 magic damage at nearby mobs and players every few seconds. TC4 nodes
  never did this (only hungry nodes pull and chip entities).
- **Warp events** — *wrong*:
  - Warp above 10 grants ELDRITCHMINOR. In TC4 it unlocks the Bath Salts hint (`@BATHSALTS`) instead.
  - Warp above 25 grants a research `ELDRITCHWARPED`, which doesn't exist. TC4 grants ELDRITCHMINOR at this point.
  - Rolls 85–88 cause spider swarms (TC4: nothing happens on those rolls).

## Already queued / not re-audited

- Golemancer's Bell vs its Thaumonomicon entry (queued). Golem AI is still invented (in TODO).
- Copper golems, shovel preview, JEI/Jade/Xaero and advancements are deliberate add-ons, so they weren't audited for TC4 accuracy.
- Arcane bore, banners, void robes and cultist armour weren't compared in depth this pass.

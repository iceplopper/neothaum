# Aspect audit (2026-10-29)

Every item's aspects were dumped as the game computes them (`NEOTHAUM_SCENES=aspects` writes `aspect_table.json`: explicit entries from
`data/neothaum/neothaum/aspects.json` plus aspects worked out from recipes). They were compared with TC4's `ConfigAspects` (vanilla entries
mapped through MCP names) where TC4 had the thing, and judged on their own where it didn't.

**How recipe aspects work (TC4's rule, which NeoThaum copies exactly):** add up the ingredients' aspects, take 75%, divide by the number
of items made, round down. Most odd results below come from that rounding wiping out the one ingredient that defines the item.

## Fixed already

| What | Problem | Fix |
|---|---|---|
| Thaumic slime, small taintacle, taint swarmer | Their TC4 aspects were stored under IDs that don't exist (`thaum_slime`, `taintacle_tiny`, `taint_swarmer`), so they had none | Entries now use `thaumic_slime`, `taintacle_small`, `taint_spore_swarmer` |

## Items to change (suggestions)

Source: **modern** = not in TC4, judged on its own; **TC4 rule** = TC4 had the item and gave it the same value through its recipe rule
(faithful, but odd); **added** = a NeoThaum addition.

| Item | Now | Source | Problem | Suggested |
|---|---|---|---|---|
| Magma block | limus 3 | modern | Made from 4 magma cream, whose fire rounds away: a block of magma with no fire | ignis 3, terra 2, limus 1 |
| Magma cream | limus 1 | TC4 rule | Blaze powder's ignis 1 rounds to 0 | leave (TC4), or limus 1, ignis 1 |
| Lantern | metallum 6 | modern | The torch's light rounds away | metallum 4, lux 2 |
| Soul lantern | metallum 6 | modern | Same | metallum 4, lux 2, spiritus 1 |
| Campfire | arbor 11, potentia 1, ignis 1 | modern | Mostly wood; barely any fire | arbor 4, ignis 3, lux 1 |
| Soul campfire | arbor 11 | modern | No fire or soul at all | arbor 4, ignis 2, spiritus 2 |
| Dried ghast | aqua 6, exanimis 24, spiritus 24 | modern | Enormous; exanimis (undead) doesn't fit a dormant ghast | spiritus 4, aer 2, aqua 1, victus 1 |
| Packed mud | messis 1 | modern | The mud (terra) rounds away, only the wheat is left | terra 2, messis 1 |
| Target | potentia 6, machina 3, messis 9, fames 4 | modern | Mostly a hay bale; nothing of a target | sensus 2, machina 2, messis 2, potentia 1 |
| Budding amethyst | vitreus 2 | modern | Less than a plain amethyst block (vitreus 6, sensus 3), though it grows crystals | vitreus 6, sensus 3, victus 2 |
| Tinted glass | vitreus 1 | modern | Nothing of its darkness or the amethyst | vitreus 2, tenebrae 1, sensus 1 |
| Bone block | sensus 6 | modern | Bones with no death in them (bone meal is a dye in TC4, so sensus) | mortuus 4, sensus 2 |
| Creaking heart | arbor 10, limus 4 | modern | The heart of a haunted tree, reads as wood and resin | arbor 4, spiritus 3, tenebrae 2, limus 1 |
| Respawn anchor | terra 9, ignis 9, praecantatio 4, tenebrae 4, sensus 6, lux 22 | modern | Lux 22 from three glowstone dominates; nothing of respawning souls | terra 6, ignis 6, praecantatio 4, lux 8, spiritus 4 |
| Charged obsidian totem | none | added | New in 1.0.8; the plain totem has terra 4, tenebrae 2, alienis 2 | terra 4, tenebrae 2, alienis 2, auram 2 |
| Sponge | none | modern | Wet sponge has aqua 2, bestia 1, the dry one nothing | bestia 1, vacuos 2 |
| Glow item frame | arbor 4 | modern | The glow is lost | arbor 4, lux 1 |
| Golden dandelion | herba 1, victus 1, sensus 1 | modern | Nothing golden | add lucrum 1 |
| Jack o'lantern | messis 1 | TC4 rule | The torch's light rounds away | leave (TC4), or messis 2, lux 2 |
| Chiseled sandstone, cut sandstone | praecantatio 1 / ordo 1 | TC4 quirk | TC4 built them from sandstone's entry while it was still empty, so no stone in them | leave (TC4), or terra 2, perditio 2 plus the 1 |
| Frost focus | aqua 9, vitreus 9, … no gelum | TC4 rule | Its recipe (water shards, quartz, diamond) has no cold in it | leave (TC4), or add gelum 3 |

Also in `aspects.json`: podzol, mossy/cracked/chiseled stone bricks, chiseled/cut sandstone and the furnace each have two entries (the
second wins, and matches TC4). Harmless; worth tidying.

**Left alone on purpose:** horse armour without metallum, potions as aqua + vitreus, end stone as terra + tenebrae, dead coral without
aqua, and modern slabs, walls, glass panes and sugar with no aspects all match TC4 or follow its rounding the same way TC4's own slabs
did.

## Mobs

**Correction:** the first version of this audit said 58 newer vanilla mobs had no aspects. That was wrong: it only read `aspects.json`, and
`aspects_modern.json` (written with the 1.0.0 port for everything added after 1.7.10) already covers them. Of those, only the **copper golem**
had none; it now has metallum 3, machina 2, motus 1. Three of that file's values were also off and are fixed:

| Mob | Was | Now | Why |
|---|---|---|---|
| Breeze | alienis 4, aer 1 | aer 4, motus 2, praecantatio 1 | A wind creature, not an eldritch one |
| Skeleton horse | bestia 4, aer 1, terra 1 | bestia 3, exanimis 2, aer 1 | Copied from the living horse (a horse variant in TC4); undead now |
| Zombie horse | bestia 4, aer 1, terra 1 | bestia 3, exanimis 2, terra 1 | Same |

NeoThaum's own mobs that TC4 never gave aspects now have them: cultist leader (alienis 2, humanus 2, perditio 2, telum 2), cultist portal
(alienis 4, iter 4, tenebrae 2), eldritch crab (alienis 2, bestia 2, tutamen 2), eldritch golem (alienis 4, machina 4, tutamen 4), eldritch
warden (alienis 6, exanimis 4, tenebrae 4), inhabited zombie (exanimis 2, alienis 2, tutamen 2), giant taintacle (vitium 6, aqua 4). The wisp
still has none, as in TC4.

## Applied

Every item change above is in, including the optional TC4-faithful ones (magma cream, jack o'lantern, chiseled and cut sandstone, frost
focus). Fixes for things TC4 had are in `aspects.json`; fixes for modern content are in `aspects_modern.json`, which loads second and so
decides for anything in both. The duplicate entries are gone. Knock-on: the hellbat focus's ignis goes from 6 to 7, through its magma cream.

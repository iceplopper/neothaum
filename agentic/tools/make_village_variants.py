"""Biome versions of the TC4 village buildings (wizard tower, banker's houses), as StructureVillagePieces.func_151558_b re-skinned them:
desert in sandstone (TC4's own rule), savanna in acacia, taiga and snowy in spruce. Plains keeps the originals.
Run from the repository root; it rewrites data/neothaum/structure/village/<type>/*.nbt from the plains templates."""
import os
import sys
sys.path.insert(0, os.path.dirname(__file__))
import nbt_min as n

SRC = 'src/main/resources/data/neothaum/structure/village/'
BUILDINGS = ['wizard_tower', 'banker_home_1', 'banker_home_tall_2']


def wood(w):
    return {
        'minecraft:oak_log': 'minecraft:%s_log' % w,
        'minecraft:oak_planks': 'minecraft:%s_planks' % w,
        'minecraft:oak_door': 'minecraft:%s_door' % w,
        'minecraft:oak_trapdoor': 'minecraft:%s_trapdoor' % w,
        'minecraft:oak_stairs': 'minecraft:%s_stairs' % w,
        'minecraft:oak_fence': 'minecraft:%s_fence' % w,
        'minecraft:oak_slab': 'minecraft:%s_slab' % w,
    }


VARIANTS = {
    # func_151558_b for desert villages: logs and cobblestone to sandstone, planks to smooth (cut) sandstone, stone stairs to sandstone stairs
    'desert': {
        'minecraft:oak_log': 'minecraft:sandstone',
        'minecraft:cobblestone': 'minecraft:sandstone',
        'minecraft:oak_planks': 'minecraft:cut_sandstone',
        'minecraft:cobblestone_wall': 'minecraft:sandstone_wall',
        'minecraft:cobblestone_stairs': 'minecraft:sandstone_stairs',
        'minecraft:oak_stairs': 'minecraft:sandstone_stairs',
        'minecraft:dirt': 'minecraft:sand',
        'minecraft:gravel': 'minecraft:sandstone',
    },
    'savanna': wood('acacia'),
    'taiga': wood('spruce'),
    'snowy': wood('spruce'),
}
# a log turned to sandstone has no axis
DROP_PROPS = {'minecraft:sandstone': ('axis',)}


def make(kind, swaps):
    os.makedirs(SRC + kind, exist_ok=True)
    for b in BUILDINGS:
        name, r = n.load(SRC + b + '.nbt')
        for p in r['palette'][1][1]:
            old = p['id'][1]
            new = swaps.get(old)
            if new is None:
                continue
            p['id'] = (n.STRING, new)
            if 'properties' in p:
                for key in DROP_PROPS.get(new, ()):
                    p['properties'][1].pop(key, None)
                if not p['properties'][1]:
                    del p['properties']
        # the building's villager wears its village's clothes
        for e in r['entities'][1][1]:
            vd = e['nbt'][1].get('VillagerData')
            if vd:
                vd[1]['type'] = (n.STRING, 'minecraft:snow' if kind == 'snowy' else 'minecraft:' + kind)
        n.save(SRC + kind + '/' + b + '.nbt', name, r)
        print(kind, b)


if __name__ == '__main__':
    for kind, swaps in VARIANTS.items():
        make(kind, swaps)

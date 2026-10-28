package dev.neothaum.items;

import dev.neothaum.registry.TVComponents;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ItemCompassStone (Sinister Lodestone):
 * Attuned stone compass that vibrates and glows when facing towards a Sinister node within 256 blocks.
 */
public class SinisterLodestoneItem extends Item {
	public static final Map<GlobalPos, Long> SINISTER_NODES = new ConcurrentHashMap<>();

	public SinisterLodestoneItem(Properties props) {
		super(props);
	}

	public static void trackSinisterNode(Level level, BlockPos pos) {
		SINISTER_NODES.put(GlobalPos.of(level.dimension(), pos.immutable()), System.currentTimeMillis());
	}

	public static boolean isLookingAtSinisterNode(Level level, Entity entity) {
		long now = System.currentTimeMillis();
		SINISTER_NODES.entrySet().removeIf(e -> now - e.getValue() > 10000L);

		for (Map.Entry<GlobalPos, Long> entry : SINISTER_NODES.entrySet()) {
			if (entry.getKey().dimension().equals(level.dimension())) {
				BlockPos p = entry.getKey().pos();
				if (isVisibleTo(0.66F, entity, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 256.0F)) {
					return true;
				}
			}
		}
		return false;
	}

	/** TC4 EntityUtils.isVisibleTo cone test (0.66F aperture ≈ 38 degrees). */
	public static boolean isVisibleTo(float fov, Entity ent, double targetX, double targetY, double targetZ, float range) {
		double eyeX = ent.getX();
		double eyeY = ent.getEyeY();
		double eyeZ = ent.getZ();
		double dx = targetX - eyeX;
		double dy = targetY - eyeY;
		double dz = targetZ - eyeZ;
		double distSq = dx * dx + dy * dy + dz * dz;
		if (distSq > (double) (range * range) || distSq < 1.0E-4) {
			return false;
		}
		double dist = Math.sqrt(distSq);
		Vec3 look = ent.getViewVector(1.0F).normalize();
		double dot = (dx * look.x + dy * look.y + dz * look.z) / dist;
		return dot > Math.cos(fov / 2.0F);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		boolean looking = isLookingAtSinisterNode(level, owner);
		if (looking && !stack.has(TVComponents.ACTIVE)) {
			stack.set(TVComponents.ACTIVE, true);
		} else if (!looking && stack.has(TVComponents.ACTIVE)) {
			stack.remove(TVComponents.ACTIVE);
		}
	}
}

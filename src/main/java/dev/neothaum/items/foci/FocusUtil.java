package dev.neothaum.items.foci;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** BlockUtils.getTargetBlock and EntityUtils.getPointedEntity, as the foci use them. */
public final class FocusUtil {
	/** BlockUtils.getTargetBlock: the block ten blocks along the player's look, or null. */
	public static BlockHitResult targetBlock(Level level, Player p, double range) {
		Vec3 eye = p.getEyePosition();
		Vec3 end = eye.add(p.getViewVector(1.0F).scale(range));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
		return hit.getType() == HitResult.Type.BLOCK ? hit : null;
	}

	/**
	 * EntityUtils.getPointedEntity: of the pickable entities in clear sight, the nearest whose box (grown by at least 0.8) the look ray crosses.
	 * {@code exclude} skips one kind (the hellbat focus ignores firebats).
	 */
	public static Entity pointedEntity(Level level, Player p, double minRange, double range, float padding, Class<?> exclude) {
		Vec3 eye = p.getEyePosition();
		Vec3 look = p.getViewVector(1.0F);
		Vec3 end = eye.add(look.scale(range));
		Entity pointed = null;
		double d2 = 0.0;
		for (Entity e : level.getEntities(p, p.getBoundingBox().expandTowards(look.scale(range)).inflate(padding))) {
			if (e.distanceTo(p) < minRange || !e.isPickable() || exclude != null && exclude.isInstance(e)) {
				continue;
			}
			if (level.clip(new ClipContext(eye, e.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p)).getType()
				!= HitResult.Type.MISS) {
				continue;
			}
			float f2 = Math.max(0.8F, e.getPickRadius());
			AABB box = e.getBoundingBox().inflate(f2);
			var clip = box.clip(eye, end);
			if (box.contains(eye)) {
				pointed = e;
				d2 = 0.0;
			} else if (clip.isPresent()) {
				double d3 = eye.distanceTo(clip.get());
				if (d3 < d2 || d2 == 0.0) {
					pointed = e;
					d2 = d3;
				}
			}
		}
		return pointed;
	}

	private FocusUtil() {
	}
}

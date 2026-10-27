package dev.neothaum.items;

import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.fx.FX;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.research.ScanManager;
import dev.neothaum.research.ScanManager.ScanResult;
import dev.neothaum.tiles.NodeBlockEntity;
import dev.neothaum.wands.WandManager;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Port of ItemThaumometer: hold right-click for 25 ticks on a target to scan it. Scanning runs client side, then the server confirms. */
public class ThaumometerItem extends Item {
	private ScanResult startScan;

	public ThaumometerItem(Item.Properties props) {
		super(props);
	}

	/**
	 * NeoThaum: villagers, horses and other mobs with their own right-click action would otherwise swallow the click. Sneaking with
	 * the thaumometer starts a scan instead, on both sides so the item-use state stays in step.
	 */
	public static void registerSneakScan() {
		net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (player.isShiftKeyDown() && stack.getItem() instanceof ThaumometerItem item && !player.isSpectator()) {
				return item.use(level, player, hand);
			}
			return InteractionResult.PASS;
		});
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 25;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			ScanResult scan = doScan(level, player);
			if (scan != null) {
				this.startScan = scan;
			}
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
		if (!level.isClientSide() || !(entity instanceof Player p) || p != WandManager.clientPlayer()) {
			return;
		}
		ScanResult scan = doScan(level, p);
		if (scan != null && scan.sameAs(this.startScan)) {
			if (count <= 5) {
				this.startScan = null;
				p.stopUsingItem();
				var err = ScanManager.validScanError(ScanManager.getScanAspects(scan, level), p);
				if (err != null) {
					TVNetwork.clientNotify(err);
				} else {
					TVNetwork.sendScanToServer(scan);
				}
			}
			if (count % 2 == 0) {
				level.playLocalSound(p.getX(), p.getY(), p.getZ(), TVSounds.CAMERATICKS, net.minecraft.sounds.SoundSource.PLAYERS, 0.2F,
					0.45F + level.getRandom().nextFloat() * 0.1F, false);
			}
		} else {
			this.startScan = null;
		}
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		this.startScan = null;
		return false;
	}

	public static Entity getPointedEntity(Level level, Player p, double range) {
		Vec3 eye = p.getEyePosition();
		Vec3 look = p.getViewVector(1.0F);
		Vec3 end = eye.add(look.scale(range));
		AABB box = p.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(p, eye, end, box, e -> !e.isSpectator() && e.isPickable() || e instanceof net.minecraft.world.entity.item.ItemEntity, range * range);
		if (hit == null) {
			return null;
		}
		BlockHitResult block = level.clip(new ClipContext(eye, hit.getLocation(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
		return block.getType() == HitResult.Type.MISS ? hit.getEntity() : null;
	}

	/**
	 * getMovingObjectPositionFromPlayer(world, player, true): liquids count as targets so water and lava can be scanned, but with the
	 * eyes under a liquid the ray passes through it, as it would otherwise stop on the water the player is standing in.
	 */
	public static BlockHitResult pickBlock(Level level, Player p) {
		boolean submerged = !level.getFluidState(BlockPos.containing(p.getEyePosition())).isEmpty();
		HitResult hr = p.pick(5.0, 1.0F, !submerged);
		return hr instanceof BlockHitResult bhr && hr.getType() == HitResult.Type.BLOCK ? bhr : null;
	}

	/** ItemThaumometer.doScan. */
	public static ScanResult doScan(Level level, Player p) {
		Entity pointed = getPointedEntity(level, p, 10.0);
		if (pointed != null) {
			ScanResult sr = ScanResult.entity(pointed);
			if (ScanManager.isValidScanTarget(p, sr, "@")) {
				FX.sparkle(level, pointed.getX(), pointed.getY() + pointed.getEyeHeight() / 2.0F, pointed.getZ(), 0.5F, 5, 0.03F);
				return sr;
			}
			return null;
		}
		BlockHitResult mop = pickBlock(level, p);
		if (mop != null) {
			BlockPos pos = mop.getBlockPos();
			if (level.getBlockEntity(pos) instanceof NodeBlockEntity || level.getBlockEntity(pos) instanceof dev.neothaum.tiles.JarNodeBlockEntity) {
				ScanResult sr = ScanResult.node(pos);
				if (ScanManager.isValidScanTarget(p, sr, "@")) {
					FX.blockSparkle(level, pos, 0x9900CC);
					return sr;
				}
				return null;
			}
			BlockState bs = level.getBlockState(pos);
			if (!bs.isAir()) {
				// TC4 used Block.getPickBlock: blocks without an item of their own (tall seagrass, kelp stems) scan as what they pick
				ItemStack is = bs.getCloneItemStack(level, pos, false);
				ScanResult sr;
				if (!is.isEmpty()) {
					sr = ScanResult.item(is.getItem());
				} else if (ObjectAspects.getBlockAspects(bs.getBlock()) != null) {
					sr = ScanResult.block(bs.getBlock());
				} else {
					return null;
				}
				if (ScanManager.isValidScanTarget(p, sr, "@")) {
					FX.blockSparkle(level, pos, 0x9900CC);
					return sr;
				}
			}
		}
		return null;
	}
}

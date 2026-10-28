package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemMarker;
import java.util.List;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * AIUseItem: take what it carries to a marked block (or, with the toggle, a marked empty space) in one of the item's colours whose marked face
 * is open, and use it there as a player would: right click, or left click with the second toggle, sneaking with the third. Whatever the use
 * leaves in hand it keeps; anything else it gets is dropped. With no home container it works empty-handed.
 */
public class UseItemGoal extends GolemGoal {
	private BlockPos target = BlockPos.ZERO;
	private BlockState block = Blocks.AIR.defaultBlockState();
	private int count;
	private int color = -1;
	private int nextTick;

	public UseItemGoal(GolemEntity golem) {
		super(golem);
		this.nextTick = golem.tickCount + golem.getRandom().nextInt(6);
	}

	private boolean ignoreItem() {
		return GolemHelper.getHomeContainer(this.golem) == null;
	}

	@Override
	public boolean canUse() {
		int d = Math.max(1, 5 - this.golem.tickCount);
		if ((!this.golem.itemCarried.isEmpty() || this.ignoreItem()) && this.golem.tickCount >= this.nextTick && this.navDone()) {
			this.nextTick = this.golem.tickCount + d * 3;
			return this.findSomething();
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.golem.level().getBlockState(this.target) == this.block && this.count-- > 0 && !this.navDone();
	}

	@Override
	public void tick() {
		this.golem.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 30.0F, 30.0F);
		if (this.golem.distanceToSqr(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5) <= 4.0) {
			this.click();
		}
	}

	@Override
	public void stop() {
		this.count = 0;
		this.golem.getNavigation().stop();
	}

	@Override
	public void start() {
		this.count = 200;
		this.moveTo(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5);
	}

	private void click() {
		List<Integer> sides = GolemHelper.getMarkedSides(this.golem, this.target, (byte) this.color);
		if (sides.isEmpty()) {
			return;
		}
		if (this.golem.itemCarried.isEmpty() && !this.ignoreItem()) {
			this.stop();
			return;
		}
		Direction side = Direction.from3DDataValue(sides.get(0));
		BlockPos at = this.target;
		if (this.golem.level().getBlockState(this.target).isAir()) {
			at = this.target.relative(side.getOpposite());
		}
		ServerLevel level = (ServerLevel) this.golem.level();
		FakePlayer player = FakePlayer.get(level, BlockWorkGoal.FAKE_PROFILE);
		player.snapTo(this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.golem.getYRot(), this.golem.getXRot());
		player.getInventory().clearContent();
		player.setItemInHand(InteractionHand.MAIN_HAND, this.golem.itemCarried);
		player.setShiftKeyDown(this.golem.getToggle(2));
		if (this.golem.getToggle(1)) {
			this.golem.startLeftArmTimer();
			player.gameMode.handleBlockBreakAction(at, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, side, level.getMaxY(), 0);
			player.gameMode.handleBlockBreakAction(at, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, side, level.getMaxY(), 0);
		} else {
			Vec3 hit = Vec3.atCenterOf(at).add(side.getStepX() * 0.5, side.getStepY() * 0.5, side.getStepZ() * 0.5);
			var result = player.gameMode.useItemOn(player, level, this.golem.itemCarried, InteractionHand.MAIN_HAND, new BlockHitResult(hit, side, at, false));
			if (result.consumesAction()) {
				this.golem.startRightArmTimer();
			}
		}
		this.golem.itemCarried = player.getMainHandItem();
		if (this.golem.itemCarried.isEmpty()) {
			this.golem.itemCarried = ItemStack.EMPTY;
		}
		var inv = player.getInventory();
		for (int a = 0; a < inv.getContainerSize(); a++) {
			if (a == inv.getSelectedSlot()) {
				continue;
			}
			ItemStack s = inv.getItem(a);
			if (!s.isEmpty()) {
				if (this.golem.itemCarried.isEmpty()) {
					this.golem.itemCarried = s.copy();
				} else {
					player.drop(s.copy(), false, net.minecraft.util.Prediction.SERVER_ONLY);
				}
				inv.setItem(a, ItemStack.EMPTY);
			}
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setShiftKeyDown(false);
		this.golem.updateCarried();
		this.stop();
	}

	private boolean findSomething() {
		for (byte col : this.golem.getColorsMatching(this.golem.itemCarried)) {
			for (GolemMarker m : this.golem.getMarkers()) {
				boolean air = this.golem.level().getBlockState(m.pos()).isAir();
				if ((m.color() == col || col == -1) && this.golem.getToggle(0) == air
					&& this.golem.level().getBlockState(m.pos().relative(Direction.from3DDataValue(m.side()))).isAir()) {
					this.color = col;
					this.target = m.pos();
					this.block = this.golem.level().getBlockState(m.pos());
					return true;
				}
			}
		}
		return false;
	}
}

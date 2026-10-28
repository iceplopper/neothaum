package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.visnet.VisNetHandler;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.blocks.ArcaneBoreBaseBlock;
import dev.neothaum.blocks.ArcaneBoreBlock;
import dev.neothaum.items.equipment.ElementalPickaxeItem;
import dev.neothaum.items.foci.ExcavationFocusItem;
import dev.neothaum.menu.ArcaneBoreMenu;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import java.util.List;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TC4 TileArcaneBore:
 * Automated digging turret. Uses an Excavation Focus and Pickaxe to mine a tunnel in a spiral pattern.
 * Mined items are ejected from the attached Arcane Bore Base.
 */
public class ArcaneBoreBlockEntity extends TVBlockEntity implements WorldlyContainer, ExtendedMenuProvider<BlockPos> {
	private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

	public Direction orientation = Direction.NORTH;
	public Direction baseOrientation = Direction.DOWN;

	public int spiral = 0;
	public float currentRadius = 0.0F;
	public int maxRadius = 2;
	public float radInc = 0.0F;

	public int rotX = 0;
	public int rotZ = 0;
	public int tarX = 0;
	public int tarZ = 0;
	public int speedX = 0;
	public int speedZ = 0;
	public int topRotation = 0;

	public float speedyTime = 0.0F;
	public int fortune = 0;
	public int speed = 0;
	public int area = 0;
	public boolean hasFocus = false;
	public boolean hasPickaxe = false;

	public boolean toDig = false;
	public BlockPos digPos = BlockPos.ZERO;
	private int count = 0;
	private int lastX = 0, lastY = 0, lastZ = 0;
	private int paused = 100;
	private int maxPause = 100;

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> ArcaneBoreBlockEntity.this.speed;
				case 1 -> ArcaneBoreBlockEntity.this.area;
				case 2 -> ArcaneBoreBlockEntity.this.fortune;
				case 3 -> ArcaneBoreBlockEntity.this.hasSilkTouch() ? 1 : 0;
				case 4 -> ArcaneBoreBlockEntity.this.hasNativeClusters() ? 1 : 0;
				case 5 -> ArcaneBoreBlockEntity.this.isPickaxeNearBroken() ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int getCount() {
			return 6;
		}
	};

	public ArcaneBoreBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_BORE, pos, state);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return this.worldPosition;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("tile.blockWoodenDevice.5.name");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ArcaneBoreMenu(id, inv, this, this.worldPosition, this.data);
	}

	public boolean hasSilkTouch() {
		ItemStack pick = this.items.get(1);
		if (!pick.isEmpty() && this.level != null) {
			var silkEntry = this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.SILK_TOUCH);
			if (silkEntry.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(silkEntry.get(), pick) > 0) {
				return true;
			}
		}
		ItemStack focus = this.items.get(0);
		return !focus.isEmpty() && focus.getItem() instanceof ExcavationFocusItem ef && ef.isUpgradedWith(focus, FocusUpgradeType.silktouch);
	}

	public boolean hasNativeClusters() {
		ItemStack pick = this.items.get(1);
		if (!pick.isEmpty() && pick.getItem() instanceof ElementalPickaxeItem) {
			return true;
		}
		ItemStack focus = this.items.get(0);
		return !focus.isEmpty() && focus.getItem() instanceof ExcavationFocusItem ef && ef.isUpgradedWith(focus, ExcavationFocusItem.DOWSING);
	}

	public boolean isPickaxeNearBroken() {
		ItemStack pick = this.items.get(1);
		return !pick.isEmpty() && pick.isDamageableItem() && (pick.getDamageValue() + 1 >= pick.getMaxDamage());
	}

	public void updateStats() {
		this.fortune = 0;
		this.area = 0;
		this.speed = 0;

		ItemStack focus = this.items.get(0);
		if (!focus.isEmpty() && focus.getItem() instanceof ExcavationFocusItem ef) {
			this.fortune = ef.getUpgradeLevel(focus, FocusUpgradeType.treasure);
			this.area = ef.getUpgradeLevel(focus, FocusUpgradeType.enlarge);
			this.speed += ef.getUpgradeLevel(focus, FocusUpgradeType.potency);
			this.hasFocus = true;
		} else {
			this.hasFocus = false;
		}

		ItemStack pick = this.items.get(1);
		if (!pick.isEmpty() && pick.is(ItemTags.PICKAXES)) {
			this.hasPickaxe = true;
			if (this.level != null) {
				var fortEntry = this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE);
				if (fortEntry.isPresent()) {
					int f = EnchantmentHelper.getItemEnchantmentLevel(fortEntry.get(), pick);
					if (f > this.fortune) {
						this.fortune = f;
					}
				}
				var effEntry = this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.EFFICIENCY);
				if (effEntry.isPresent()) {
					this.speed += EnchantmentHelper.getItemEnchantmentLevel(effEntry.get(), pick);
				}
			}
		} else {
			this.hasPickaxe = false;
		}
	}

	public boolean isGettingPower() {
		if (this.level == null) return false;
		BlockPos basePos = this.worldPosition.relative(this.baseOrientation);
		return this.level.hasNeighborSignal(this.worldPosition) || this.level.hasNeighborSignal(basePos);
	}

	public boolean areItemsValid() {
		return this.hasFocus && this.hasPickaxe && !this.isPickaxeNearBroken();
	}

	public void setOrientation(Direction dir, boolean initial) {
		this.orientation = dir;
		this.lastX = 0;
		this.lastZ = 0;
		this.lastY = 0;
		switch (dir) {
			case DOWN -> { this.tarZ = 180; this.tarX = 0; }
			case UP -> { this.tarZ = 0; this.tarX = 0; }
			case NORTH -> { this.tarZ = 90; this.tarX = 270; }
			case SOUTH -> { this.tarZ = 90; this.tarX = 90; }
			case WEST -> { this.tarZ = 90; this.tarX = 0; }
			case EAST -> { this.tarZ = 90; this.tarX = 180; }
		}
		if (initial) {
			this.rotX = this.tarX;
			this.rotZ = this.tarZ;
		}
		this.toDig = false;
		this.radInc = 0.0F;
		this.paused = 100;
		this.markForUpdate();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
		bore.updateStats();

		// Draw speed-up power: Perditio centivis from grid or essentia from bore base
		if (bore.speedyTime < 20.0F) {
			bore.speedyTime += VisNetHandler.drainVis(level, pos, Aspect.ENTROPY, 100) / 5.0F;
			if (bore.speedyTime < 20.0F) {
				BlockPos basePos = pos.relative(bore.baseOrientation);
				if (level.getBlockEntity(basePos) instanceof ArcaneBoreBaseBlockEntity base && base.drawEssentia()) {
					bore.speedyTime += 20.0F;
				}
			}
		}

		// Rotation smoothing
		if (bore.rotX < bore.tarX) {
			bore.rotX += bore.speedX;
			if (bore.rotX < bore.tarX) bore.speedX++;
			else bore.speedX = (int) (bore.speedX / 3.0F);
		} else if (bore.rotX > bore.tarX) {
			bore.rotX += bore.speedX;
			if (bore.rotX > bore.tarX) bore.speedX--;
			else bore.speedX = (int) (bore.speedX / 3.0F);
		} else {
			bore.speedX = 0;
		}

		if (bore.rotZ < bore.tarZ) {
			bore.rotZ += bore.speedZ;
			if (bore.rotZ < bore.tarZ) bore.speedZ++;
			else bore.speedZ = (int) (bore.speedZ / 3.0F);
		} else if (bore.rotZ > bore.tarZ) {
			bore.rotZ += bore.speedZ;
			if (bore.rotZ > bore.tarZ) bore.speedZ--;
			else bore.speedZ = (int) (bore.speedZ / 3.0F);
		} else {
			bore.speedZ = 0;
		}

		if (bore.isGettingPower() && bore.areItemsValid()) {
			bore.digBlock(level, pos);
		}
	}

	private void digBlock(Level level, BlockPos pos) {
		if (this.rotX != this.tarX || this.rotZ != this.tarZ || !(level instanceof ServerLevel sl)) {
			return;
		}
		if (--this.count > 0) {
			return;
		}
		if (this.toDig) {
			this.toDig = false;
			BlockState targetState = level.getBlockState(this.digPos);
			if (!targetState.isAir()) {
				this.harvest(sl, pos, targetState);
			}
			// the bore's own pickaxe wears with every block
			ItemStack pick = this.items.get(1);
			if (!pick.isEmpty()) {
				pick.hurtAndBreak(1, sl, null, item -> this.items.set(1, ItemStack.EMPTY));
				this.setChanged();
			}
			level.removeBlock(this.digPos, false);
			this.lightTunnel(sl, pos);
			if (this.speedyTime > 0.0F) {
				this.speedyTime--;
			}
		}
		this.findNextBlockToDig(level, pos);
	}

	/**
	 * The dig: drops as the pickaxe (or the focus's treasure upgrade, whichever is luckier) would give, or the block itself with silk touch
	 * from either; any loose items about the spot are swept up too. An elemental pickaxe or a dowsing focus can turn ore into native clusters
	 * (20% plus 7.5% per fortune). Everything goes into the container the base faces, or is spat out beside it.
	 */
	private void harvest(ServerLevel sl, BlockPos pos, BlockState targetState) {
		var enchants = sl.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		var silkHolder = enchants.getOrThrow(Enchantments.SILK_TOUCH);
		var fortuneHolder = enchants.getOrThrow(Enchantments.FORTUNE);
		ItemStack pick = this.items.get(1);
		ItemStack focus = this.items.get(0);
		int tfortune = this.fortune;
		boolean canSilk = targetState.getBlock().asItem() != net.minecraft.world.item.Items.AIR;
		boolean silktouch = canSilk && (EnchantmentHelper.getItemEnchantmentLevel(silkHolder, pick) > 0
			|| focus.getItem() instanceof ExcavationFocusItem ef && ef.isUpgradedWith(focus, FocusUpgradeType.silktouch));
		if (silktouch) {
			tfortune = 0;
		}
		sl.playSound(null, this.digPos, targetState.getSoundType().getBreakSound(), SoundSource.BLOCKS, (targetState.getSoundType().getVolume() + 1.0F) / 2.0F,
			targetState.getSoundType().getPitch() * 0.8F);
		sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, targetState), this.digPos.getX() + 0.5, this.digPos.getY() + 0.5,
			this.digPos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
		List<ItemStack> items = new java.util.ArrayList<>();
		if (silktouch) {
			items.add(new ItemStack(targetState.getBlock()));
		} else {
			ItemStack tool = pick.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE) : pick.copy();
			final int f = tfortune;
			EnchantmentHelper.updateEnchantments(tool, m -> {
				m.set(silkHolder, 0);
				m.set(fortuneHolder, f);
			});
			items.addAll(Block.getDrops(targetState, sl, this.digPos, sl.getBlockEntity(this.digPos), null, tool));
		}
		for (var e : sl.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(this.digPos).inflate(1.0))) {
			items.add(e.getItem().copy());
			e.discard();
		}
		boolean special = !silktouch && (pick.getItem() instanceof ElementalPickaxeItem
			|| focus.getItem() instanceof ExcavationFocusItem ef && ef.isUpgradedWith(focus, ExcavationFocusItem.DOWSING));
		BlockPos basePos = pos.relative(this.baseOrientation);
		ArcaneBoreBaseBlockEntity base = sl.getBlockEntity(basePos) instanceof ArcaneBoreBaseBlockEntity b ? b : null;
		for (ItemStack is : items) {
			ItemStack dropped = special ? dev.neothaum.items.equipment.SpecialMining.find(is, 0.2F + tfortune * 0.075F, sl.getRandom()) : is.copy();
			if (base != null) {
				base.ejectItem(dropped);
			}
		}
	}

	/** With an arcane lamp beside the base, the bore lights its tunnel as it goes. */
	private void lightTunnel(ServerLevel sl, BlockPos pos) {
		BlockPos basePos = pos.relative(this.baseOrientation);
		for (Direction lbd : Direction.Plane.HORIZONTAL) {
			if (sl.getBlockEntity(basePos.relative(lbd)) instanceof ArcaneLampBlockEntity) {
				int d = sl.getRandom().nextInt(32) * 2;
				Direction o = this.orientation;
				int xx = pos.getX() + o.getStepX() + o.getStepX() * d;
				int yy = pos.getY() + o.getStepY() + o.getStepY() * d;
				int zz = pos.getZ() + o.getStepZ() + o.getStepZ() * d;
				int p = d / 2 % 4;
				int off = p == 0 ? 3 : (p != 1 && p != 3 ? -3 : 0);
				if (o.getStepX() != 0) {
					zz += off;
				} else {
					xx += off;
				}
				if (p == 3 && o.getStepY() == 0) {
					yy -= 2;
				}
				BlockPos lp = new BlockPos(xx, yy, zz);
				if (sl.getBlockState(lp).isAir() && !sl.getBlockState(lp).is(TVBlocks.LAMP_LIGHT) && sl.getMaxLocalRawBrightness(lp) < 15) {
					sl.setBlock(lp, TVBlocks.LAMP_LIGHT.defaultBlockState(), 3);
				}
				break;
			}
		}
	}

	private int digDelay(Level level, BlockPos at) {
		int c = Math.max(10 - this.speed, (int) (level.getBlockState(at).getDestroySpeed(level, at) * 2.0F) - this.speed * 2);
		return this.speedyTime < 1.0F ? c * 4 : c;
	}

	/** findNextBlockToDig: the next point along the widening and narrowing spiral, then the first solid block up to 64 deep along the bore's aim. */
	private void findNextBlockToDig(Level level, BlockPos pos) {
		if (this.radInc == 0.0F) {
			this.radInc = (this.maxRadius + this.area) / 360.0F;
		}
		int targetRadius = this.maxRadius + this.area;
		int x = this.lastX, y = this.lastY, z = this.lastZ;
		Direction o = this.orientation;
		while (x == this.lastX && z == this.lastZ && y == this.lastY) {
			this.spiral += 2;
			if (this.spiral >= 360) {
				this.spiral -= 360;
			}
			this.currentRadius += this.radInc;
			if (this.currentRadius > targetRadius || this.currentRadius < -targetRadius) {
				this.radInc *= -1.0F;
			}
			Vec3 vtar = new Vec3(0.0, this.currentRadius, 0.0).zRot(this.spiral / 180.0F * (float) Math.PI).yRot((float) (Math.PI / 2) * o.getStepX())
				.xRot((float) (Math.PI / 2) * o.getStepY());
			x = Mth.floor(pos.getX() + o.getStepX() + 0.5 + vtar.x);
			y = Mth.floor(pos.getY() + o.getStepY() + 0.5 + vtar.y);
			z = Mth.floor(pos.getZ() + o.getStepZ() + 0.5 + vtar.z);
		}
		this.lastX = x;
		this.lastY = y;
		this.lastZ = z;
		BlockPos.MutableBlockPos ray = new BlockPos.MutableBlockPos(x, y, z).move(o);
		for (int depth = 0; depth < 64; depth++) {
			ray.move(o);
			BlockState bs = level.getBlockState(ray);
			if (bs.getDestroySpeed(level, ray) < 0.0F) {
				break;
			}
			if (!bs.isAir() && !bs.getCollisionShape(level, ray).isEmpty()) {
				this.digPos = ray.immutable();
				this.count = this.digDelay(level, this.digPos);
				this.toDig = true;
				// anything solid in the way of the beam is dug first
				Vec3 from = new Vec3(pos.getX() + 0.5 + o.getStepX(), pos.getY() + 0.5 + o.getStepY(), pos.getZ() + 0.5 + o.getStepZ());
				var hit = level.clip(new net.minecraft.world.level.ClipContext(from, Vec3.atCenterOf(this.digPos), net.minecraft.world.level.ClipContext.Block.COLLIDER,
					net.minecraft.world.level.ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
				if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
					BlockPos hp = hit.getBlockPos();
					BlockState hs = level.getBlockState(hp);
					if (hs.getDestroySpeed(level, hp) > -1.0F && !hs.getCollisionShape(level, hp).isEmpty()) {
						this.count = this.digDelay(level, hp);
						this.digPos = hp.immutable();
					}
				}
				this.markForUpdate();
				break;
			}
		}
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack res = ContainerHelper.removeItem(this.items, slot, amount);
		if (!res.isEmpty()) this.setChanged();
		return res;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0, 1};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		if (slot == 0) return stack.getItem() instanceof ExcavationFocusItem;
		if (slot == 1) return stack.is(ItemTags.PICKAXES);
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items.clear();
		ContainerHelper.loadAllItems(input, this.items);
		this.orientation = Direction.from3DDataValue(input.getIntOr("orientation", Direction.NORTH.get3DDataValue()));
		this.baseOrientation = Direction.from3DDataValue(input.getIntOr("baseOrientation", Direction.DOWN.get3DDataValue()));
		this.speedyTime = input.getFloatOr("SpeedyTime", 0.0F);
		this.setOrientation(this.orientation, true);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putInt("orientation", this.orientation.get3DDataValue());
		output.putInt("baseOrientation", this.baseOrientation.get3DDataValue());
		output.putFloat("SpeedyTime", this.speedyTime);
	}
}

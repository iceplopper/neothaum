package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.nodes.NodeModifier;
import dev.neothaum.api.nodes.NodeType;
import dev.neothaum.blocks.TaintFibreBlock;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.monster.GiantBrainyZombie;
import dev.neothaum.items.WandItem;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.world.TVBiomes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Port of thaumcraft.common.tiles.TileNode. */
public class NodeBlockEntity extends TVBlockEntity {
	long lastActive = 0L;
	AspectList aspects = new AspectList();
	AspectList aspectsBase = new AspectList();
	private NodeType nodeType = NodeType.NORMAL;
	private NodeModifier nodeModifier = null;
	public int count = 0;
	int regeneration = -1;
	int wait = 0;
	byte nodeLock = 0;
	boolean catchUp = false;
	/** Client-side drain beam state. */
	public Entity drainEntity = null;
	public int drainColor = 0xFFFFFF;
	public int targetColor = 0xFFFFFF;
	public int color = 0xFFFFFF;
	private String drainerName = "";

	public NodeBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.NODE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, NodeBlockEntity node) {
		node.updateEntity();
		// nodes from before the bright-node addon, or after its setting changed, catch up now and then
		if (!level.isClientSide() && (level.getGameTime() + pos.asLong()) % 200 == 0) {
			dev.neothaum.addon.BrightNodes.sync(level, pos, node.getNodeModifier());
		}
	}

	public void updateEntity() {
		boolean change = false;
		change = this.handleHungryNodeFirst(change);
		this.count++;
		this.checkLock();
		if (this.nodeType == NodeType.DARK && this.count % 50 == 0) {
			dev.neothaum.items.SinisterLodestoneItem.trackSinisterNode(this.level, this.worldPosition);
		}
		if (!this.level.isClientSide()) {
			change = this.handleDischarge(change);
			change = this.handleRecharge(change);
			change = this.handleTaintNode(change);
			change = this.handleNodeStability(change);
			change = this.handleDarkNode(change);
			change = this.handlePureNode(change);
			change = this.handleHungryNodeSecond(change);
			if (change) {
				this.markForUpdate();
			}
		} else if (this.drainEntity == null && !this.drainerName.isEmpty()) {
			this.drainEntity = this.level.players().stream().filter(p -> p.getName().getString().equals(this.drainerName)).findFirst().orElse(null);
		}
	}

	public void nodeChange() {
		this.regeneration = -1;
		this.markForUpdate();
	}

	public double getDistanceTo(double x, double y, double z) {
		double dx = this.worldPosition.getX() + 0.5 - x;
		double dy = this.worldPosition.getY() + 0.5 - y;
		double dz = this.worldPosition.getZ() + 0.5 - z;
		return dx * dx + dy * dy + dz * dz;
	}

	public AspectList getAspects() {
		return this.aspects;
	}

	public AspectList getAspectsBase() {
		return this.aspectsBase;
	}

	public void setAspects(AspectList aspects) {
		this.aspects = aspects;
		this.aspectsBase = aspects.copy();
	}

	public int addToContainer(Aspect aspect, int amount) {
		int left = amount + this.aspects.getAmount(aspect) - this.aspectsBase.getAmount(aspect);
		left = Math.max(left, 0);
		this.aspects.add(aspect, amount - left);
		return left;
	}

	public boolean takeFromContainer(Aspect aspect, int amount) {
		return this.aspects.reduce(aspect, amount);
	}

	public Aspect takeRandomPrimalFromSource() {
		Aspect[] primals = this.aspects.getPrimalAspects();
		if (primals.length == 0) {
			return null;
		}
		Aspect asp = primals[this.level.getRandom().nextInt(primals.length)];
		return asp != null && this.aspects.reduce(asp, 1) ? asp : null;
	}

	public Aspect chooseRandomFilteredFromSource(AspectList filter, boolean preserve) {
		int min = preserve ? 1 : 0;
		List<Aspect> valid = new ArrayList<>();
		for (Aspect prim : this.aspects.getAspects()) {
			if (filter.getAmount(prim) > 0 && this.aspects.getAmount(prim) > min) {
				valid.add(prim);
			}
		}
		if (valid.isEmpty()) {
			return null;
		}
		Aspect asp = valid.get(this.level.getRandom().nextInt(valid.size()));
		return asp != null && this.aspects.getAmount(asp) > min ? asp : null;
	}

	public NodeType getNodeType() {
		return this.nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public void setNodeModifier(NodeModifier nodeModifier) {
		this.nodeModifier = nodeModifier;
		if (this.level != null && !this.level.isClientSide()) {
			dev.neothaum.addon.BrightNodes.sync(this.level, this.worldPosition, nodeModifier);
		}
	}

	public NodeModifier getNodeModifier() {
		return this.nodeModifier;
	}

	public int getNodeVisBase(Aspect aspect) {
		return this.aspectsBase.getAmount(aspect);
	}

	public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
		if (this.aspectsBase.getAmount(aspect) < nodeVisBase) {
			this.aspectsBase.merge(aspect, nodeVisBase);
		} else {
			this.aspectsBase.reduce(aspect, this.aspectsBase.getAmount(aspect) - nodeVisBase);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.lastActive = input.getLongOr("lastActive", 0L);
		this.aspectsBase = input.read("AspectsBase", AspectList.CODEC).orElseGet(AspectList::new);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		this.nodeType = NodeType.values()[input.getByteOr("type", (byte) 0)];
		byte mod = input.getByteOr("modifier", (byte) -1);
		this.nodeModifier = mod >= 0 ? NodeModifier.values()[mod] : null;
		this.drainerName = input.getStringOr("drainer", "");
		if (this.drainerName.isEmpty()) {
			this.drainEntity = null;
		}
		this.drainColor = input.getIntOr("draincolor", 0xFFFFFF);
		this.targetColor = this.drainColor;
		int regen = 600;
		if (this.nodeModifier != null) {
			regen = switch (this.nodeModifier) {
				case BRIGHT -> 400;
				case PALE -> 900;
				case FADING -> 0;
			};
		}
		long ct = System.currentTimeMillis();
		int inc = regen * 75;
		if (regen > 0 && this.lastActive > 0L && ct > this.lastActive + inc) {
			this.catchUp = true;
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("lastActive", this.lastActive);
		output.store("AspectsBase", AspectList.CODEC, this.aspectsBase);
		output.store("Aspects", AspectList.CODEC, this.aspects);
		output.putByte("type", (byte) this.nodeType.ordinal());
		output.putByte("modifier", this.nodeModifier == null ? -1 : (byte) this.nodeModifier.ordinal());
		if (this.drainEntity instanceof Player p) {
			output.putString("drainer", p.getName().getString());
		}
		output.putInt("draincolor", this.drainColor);
	}

	/** Called every tick while a player holds right-click on this node with a wand. */
	public void onUsingWandTick(ItemStack wandstack, Player player, int count) {
		boolean mfu = false;
		HitResult hit = player.pick(5.0, 1.0F, false);
		if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK || !bhr.getBlockPos().equals(this.worldPosition)) {
			player.stopUsingItem();
		}

		if (count % 5 == 0) {
			int tap = 1;
			if (ResearchManager.isResearchComplete(player, "NODETAPPER1")) {
				tap++;
			}
			if (ResearchManager.isResearchComplete(player, "NODETAPPER2")) {
				tap++;
			}
			boolean preserve = !player.isShiftKeyDown() && ResearchManager.isResearchComplete(player, "NODEPRESERVE")
				&& !WandItem.getRod(wandstack).getTag().equals("wood") && !WandItem.getCap(wandstack).getTag().equals("iron");
			boolean success = false;
			Aspect aspect;
			if ((aspect = this.chooseRandomFilteredFromSource(WandItem.getAspectsWithRoom(wandstack), preserve)) != null) {
				int amt = this.aspects.getAmount(aspect);
				if (tap > amt) {
					tap = amt;
				}
				if (preserve && tap == amt) {
					tap--;
				}
				if (tap > 0) {
					int rem = WandItem.addVis(wandstack, aspect, tap, !this.level.isClientSide());
					if (rem < tap) {
						this.drainColor = aspect.getColor();
						if (!this.level.isClientSide()) {
							this.takeFromContainer(aspect, tap - rem);
							mfu = true;
						}
						success = true;
					}
				}
			}

			if (success) {
				this.drainEntity = player;
				this.targetColor = this.drainColor;
			} else {
				this.drainEntity = null;
			}
			if (mfu) {
				this.markForUpdate();
			}
		}

		if (this.level.isClientSide()) {
			int r = (this.targetColor >> 16 & 255) + (this.color >> 16 & 255) * 4;
			int g = (this.targetColor >> 8 & 255) + (this.color >> 8 & 255) * 4;
			int b = (this.targetColor & 255) + (this.color & 255) * 4;
			this.color = (r / 5) << 16 | (g / 5) << 8 | b / 5;
		}
	}

	public void onWandStoppedUsing(ItemStack wandstack, Level level, Player player, int count) {
		this.drainEntity = null;
		if (!level.isClientSide()) {
			this.markForUpdate();
		}
	}

	private boolean handleHungryNodeFirst(boolean change) {
		if (this.nodeType == NodeType.HUNGRY) {
			if (this.level.isClientSide()) {
				var rand = this.level.getRandom();
				int tx = this.worldPosition.getX() + rand.nextInt(16) - rand.nextInt(16);
				int ty = this.worldPosition.getY() + rand.nextInt(16) - rand.nextInt(16);
				int tz = this.worldPosition.getZ() + rand.nextInt(16) - rand.nextInt(16);
				int h = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, tx, tz);
				if (ty > h) {
					ty = h;
				}
				BlockHitResult mop = rayTraceIgnoringSource(new Vec3(tx + 0.5, ty + 0.5, tz + 0.5));
				if (mop != null && mop.getType() == HitResult.Type.BLOCK && this.worldPosition.distSqr(mop.getBlockPos()) < 256.0) {
					BlockPos p = mop.getBlockPos();
					if (!this.level.getBlockState(p).isAir()) {
						dev.neothaum.fx.FX.wispTo(this.level, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, this.worldPosition.getX() + 0.5,
							this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 0.3F, 5, true, 0.0F);
					}
				}
			}
			// Config.hardNode: pull entities in and consume them
			List<Entity> ents = this.level.getEntitiesOfClass(Entity.class, new AABB(this.worldPosition).inflate(15.0));
			for (Entity eo : ents) {
				if (eo instanceof Player p && (p.isCreative() || p.isSpectator())) {
					continue;
				}
				if (eo.isAlive() && !this.level.isClientSide() && this.level instanceof ServerLevel sl) {
					double d = this.getDistanceTo(eo.getX(), eo.getY(), eo.getZ());
					if (d < 2.0) {
						eo.hurtServer(sl, sl.damageSources().fellOutOfWorld(), 1.0F);
					}
				}
				double var3 = (this.worldPosition.getX() + 0.5 - eo.getX()) / 15.0;
				double var5 = (this.worldPosition.getY() + 0.5 - eo.getY()) / 15.0;
				double var7 = (this.worldPosition.getZ() + 0.5 - eo.getZ()) / 15.0;
				double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
				double var11 = 1.0 - var9;
				if (var11 > 0.0 && var9 > 0.0) {
					var11 *= var11;
					eo.setDeltaMovement(eo.getDeltaMovement().add(var3 / var9 * var11 * 0.15, var5 / var9 * var11 * 0.25, var7 / var9 * var11 * 0.15));
				}
			}
		}
		return change;
	}

	private BlockHitResult rayTraceIgnoringSource(Vec3 to) {
		Vec3 from = Vec3.atCenterOf(this.worldPosition);
		BlockHitResult r = this.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
		if (r.getType() == HitResult.Type.BLOCK && r.getBlockPos().equals(this.worldPosition)) {
			return null;
		}
		return r;
	}

	private boolean handleDischarge(boolean change) {
		if (!this.getBlockState().is(TVBlocks.AURA_NODE) || this.nodeLock == 1 || this.nodeModifier == NodeModifier.FADING) {
			return change;
		}
		boolean shiny = this.nodeType == NodeType.HUNGRY || this.nodeModifier == NodeModifier.BRIGHT;
		int inc = this.nodeModifier == null ? 2 : (shiny ? 1 : (this.nodeModifier == NodeModifier.PALE ? 3 : 2));
		if (this.count % inc != 0) {
			return change;
		}
		var rand = this.level.getRandom();
		int x = rand.nextInt(5) - rand.nextInt(5);
		int y = rand.nextInt(5) - rand.nextInt(5);
		int z = rand.nextInt(5) - rand.nextInt(5);
		if (this.nodeModifier == NodeModifier.PALE && rand.nextBoolean()) {
			return change;
		}
		if (x != 0 || y != 0 || z != 0) {
			BlockPos op = this.worldPosition.offset(x, y, z);
			BlockEntity te = this.level.getBlockEntity(op);
			if (te instanceof NodeBlockEntity nd && this.level.getBlockState(op).is(TVBlocks.AURA_NODE)) {
				if (nd.nodeLock > 0) {
					return change;
				}
				int ndavg = (nd.aspects.visSize() + nd.aspectsBase.visSize()) / 2;
				int thisavg = (this.aspects.visSize() + this.aspectsBase.visSize()) / 2;
				if (ndavg < thisavg && nd.aspects.size() > 0) {
					Aspect a = nd.aspects.getAspects()[rand.nextInt(nd.aspects.size())];
					boolean u = false;
					if (this.aspects.getAmount(a) < this.getNodeVisBase(a) && nd.takeFromContainer(a, 1)) {
						this.addToContainer(a, 1);
						u = true;
					} else if (nd.takeFromContainer(a, 1)) {
						if (rand.nextInt(1 + (int) (this.getNodeVisBase(a) / (shiny ? 1.5 : 1.0))) == 0) {
							this.aspectsBase.add(a, 1);
							if (this.nodeModifier == NodeModifier.PALE && rand.nextInt(100) == 0) {
								this.nodeModifier = null;
								this.regeneration = -1;
							}
							if (rand.nextInt(3) == 0) {
								nd.setNodeVisBase(a, (short) (nd.getNodeVisBase(a) - 1));
							}
						}
						u = true;
					}
					if (u) {
						nd.wait = nd.regeneration / 2;
						nd.markForUpdate();
						change = true;
						dev.neothaum.network.TVNetwork.sendZap(this.level, op, this.worldPosition);
					}
				}
			}
		}
		return change;
	}

	private boolean handleRecharge(boolean change) {
		if (this.regeneration < 0) {
			this.regeneration = 600;
			if (this.nodeModifier != null) {
				this.regeneration = switch (this.nodeModifier) {
					case BRIGHT -> 400;
					case PALE -> 900;
					case FADING -> 0;
				};
			}
			if (this.nodeLock == 1) {
				this.regeneration *= 2;
			}
			if (this.nodeLock == 2) {
				this.regeneration *= 20;
			}
		}
		var rand = this.level.getRandom();
		if (this.catchUp) {
			this.catchUp = false;
			long ct = System.currentTimeMillis();
			int inc = this.regeneration * 75;
			int amt = inc > 0 ? (int) ((ct - this.lastActive) / inc) : 0;
			for (int a = 0; a < Math.min(amt, this.aspectsBase.visSize()); a++) {
				AspectList al = new AspectList();
				for (Aspect aspect : this.aspects.getAspects()) {
					if (this.aspects.getAmount(aspect) < this.getNodeVisBase(aspect)) {
						al.add(aspect, 1);
					}
				}
				if (al.size() > 0) {
					this.addToContainer(al.getAspects()[rand.nextInt(al.size())], 1);
				}
			}
		}

		if (this.count % 1200 == 0) {
			for (Aspect aspectx : this.aspects.getAspects()) {
				if (this.aspects.getAmount(aspectx) <= 0) {
					this.setNodeVisBase(aspectx, (short) (this.getNodeVisBase(aspectx) - 1));
					if (rand.nextInt(20) == 0 || this.getNodeVisBase(aspectx) <= 0) {
						this.aspects.remove(aspectx);
						if (rand.nextInt(5) == 0) {
							if (this.nodeModifier == NodeModifier.BRIGHT) {
								this.nodeModifier = null;
							} else if (this.nodeModifier == null) {
								this.nodeModifier = NodeModifier.PALE;
							}
							if (this.nodeModifier == NodeModifier.PALE && rand.nextInt(5) == 0) {
								this.nodeModifier = NodeModifier.FADING;
							}
						}
						this.nodeChange();
						break;
					}
					this.nodeChange();
				}
			}
			if (this.aspects.size() <= 0 && this.getBlockState().is(TVBlocks.AURA_NODE)) {
				this.level.removeBlock(this.worldPosition, false);
				return false;
			}
		}

		if (this.wait > 0) {
			this.wait--;
		}

		if (this.regeneration > 0 && this.wait == 0 && this.count % this.regeneration == 0) {
			this.lastActive = System.currentTimeMillis();
			AspectList al = new AspectList();
			for (Aspect a : this.aspects.getAspects()) {
				if (this.aspects.getAmount(a) < this.getNodeVisBase(a)) {
					al.add(a, 1);
				}
			}
			if (al.size() > 0) {
				this.addToContainer(al.getAspects()[rand.nextInt(al.size())], 1);
				change = true;
			}
		}
		return change;
	}

	private boolean handleNodeStability(boolean change) {
		if (this.count % 100 == 0) {
			var rand = this.level.getRandom();
			if (this.nodeType == NodeType.UNSTABLE && rand.nextBoolean()) {
				if (this.nodeLock == 0) {
					Aspect aspect = this.takeRandomPrimalFromSource();
					if (aspect != null) {
						if (!this.level.isClientSide()) {
							dev.neothaum.entity.AspectOrbEntity orb = new dev.neothaum.entity.AspectOrbEntity(
								this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, aspect, 1);
							this.level.addFreshEntity(orb);
						}
						change = true;
					}
				} else if (rand.nextInt(10000 / this.nodeLock) == 42) {
					this.nodeType = NodeType.NORMAL;
					change = true;
				}
			}
			if (this.nodeModifier == NodeModifier.FADING && this.nodeLock > 0 && rand.nextInt(12500 / this.nodeLock) == 69) {
				this.nodeModifier = NodeModifier.PALE;
				change = true;
			}
		}
		return change;
	}

	/** Config.hardNode (TC4's "hard_mode_nodes", on by default): tainted nodes also sprout fibres, and sinister nodes call up giant zombies. */
	private static boolean hardNode() {
		return dev.neothaum.config.NeoThaumConfig.current.hardNodes;
	}

	/** Nodes change the land only in the overworld and other non-Nether, non-End dimensions (TC4's dimension blacklist). */
	private boolean landChanges() {
		return this.level.dimension() != Level.NETHER && this.level.dimension() != Level.END;
	}

	/**
	 * TileNode.handleTaintNode: a tainted node taints the land around it (and, in hard mode, grows fibres); any other node but a pure one
	 * standing in the tainted land may itself become tainted.
	 */
	private boolean handleTaintNode(boolean change) {
		var rand = this.level.getRandom();
		ServerLevel sl = (ServerLevel) this.level;
		if (this.nodeType == NodeType.TAINTED && this.count % 50 == 0) {
			int x = this.worldPosition.getX() + rand.nextInt(8) - rand.nextInt(8);
			int z = this.worldPosition.getZ() + rand.nextInt(8) - rand.nextInt(8);
			if (!TVBiomes.is(sl, new BlockPos(x, this.worldPosition.getY(), z), TVBiomes.TAINTED_LAND)) {
				TVBiomes.setBiomeAt(sl, x, z, TVBiomes.TAINTED_LAND);
			}
			if (hardNode() && rand.nextBoolean()) {
				BlockPos p = this.worldPosition.offset(rand.nextInt(5) - rand.nextInt(5), rand.nextInt(5) - rand.nextInt(5),
					rand.nextInt(5) - rand.nextInt(5));
				TaintFibreBlock.spreadFibres(sl, p);
			}
		} else if (this.nodeType != NodeType.PURE && this.nodeType != NodeType.TAINTED && this.count % 100 == 0) {
			if (TVBiomes.is(sl, this.worldPosition, TVBiomes.TAINTED_LAND) && rand.nextInt(500) == 0) {
				this.nodeType = NodeType.TAINTED;
				this.nodeChange();
			}
		}
		return change;
	}

	/**
	 * TileNode.handlePureNode: a pure node cleanses the tainted land around it into magical forest; one grown inside a silverwood trunk turns
	 * any land around it into magical forest.
	 */
	private boolean handlePureNode(boolean change) {
		if (this.nodeType == NodeType.PURE && this.count % 50 == 0 && landChanges()) {
			var rand = this.level.getRandom();
			ServerLevel sl = (ServerLevel) this.level;
			int x = this.worldPosition.getX() + rand.nextInt(8) - rand.nextInt(8);
			int z = this.worldPosition.getZ() + rand.nextInt(8) - rand.nextInt(8);
			BlockPos p = new BlockPos(x, this.worldPosition.getY(), z);
			if (!TVBiomes.is(sl, p, TVBiomes.MAGICAL_FOREST) && (TVBiomes.is(sl, p, TVBiomes.TAINTED_LAND) || this.inSilverwood())) {
				TVBiomes.setBiomeAt(sl, x, z, TVBiomes.MAGICAL_FOREST);
			}
		}
		return change;
	}

	/**
	 * TC4 kept a silverwood's node inside a magical log block; here the node takes the trunk's centre and the four logs around it stay, so a
	 * node with silverwood logs on all four sides is the one in the trunk.
	 */
	private boolean inSilverwood() {
		for (Direction d : Direction.Plane.HORIZONTAL) {
			if (!this.level.getBlockState(this.worldPosition.relative(d)).is(TVBlocks.SILVERWOOD_LOG)) {
				return false;
			}
		}
		return true;
	}

	/** TileNode.handleDarkNode: a sinister node turns the land around it eerie and, in hard mode, calls up giant zombies near players. */
	private boolean handleDarkNode(boolean change) {
		if (this.nodeType != NodeType.DARK || this.count % 50 != 0 || !landChanges()) {
			return change;
		}
		var rand = this.level.getRandom();
		ServerLevel sl = (ServerLevel) this.level;
		int x = this.worldPosition.getX() + rand.nextInt(12) - rand.nextInt(12);
		int z = this.worldPosition.getZ() + rand.nextInt(12) - rand.nextInt(12);
		if (!TVBiomes.is(sl, new BlockPos(x, this.worldPosition.getY(), z), TVBiomes.EERIE)) {
			TVBiomes.setBiomeAt(sl, x, z, TVBiomes.EERIE);
		}
		double cx = this.worldPosition.getX() + 0.5, cy = this.worldPosition.getY() + 0.5, cz = this.worldPosition.getZ() + 0.5;
		if (hardNode() && rand.nextBoolean() && sl.getDifficulty() != Difficulty.PEACEFUL && sl.getNearestPlayer(cx, cy, cz, 24.0, false) != null) {
			int j = sl.getEntitiesOfClass(GiantBrainyZombie.class, new AABB(this.worldPosition).inflate(10.0, 6.0, 10.0)).size();
			if (j <= 3) {
				GiantBrainyZombie entity = TVEntities.GIANT_BRAINY_ZOMBIE.create(sl, EntitySpawnReason.EVENT);
				if (entity != null) {
					double d0 = this.worldPosition.getX() + (rand.nextDouble() - rand.nextDouble()) * 5.0;
					double d3 = this.worldPosition.getY() + rand.nextInt(3) - 1;
					double d4 = this.worldPosition.getZ() + (rand.nextDouble() - rand.nextDouble()) * 5.0;
					entity.snapTo(d0, d3, d4, rand.nextFloat() * 360.0F, 0.0F);
					// EntityMob.getCanSpawnHere: dark enough, and room to stand
					if (Monster.isDarkEnoughToSpawn(sl, entity.blockPosition(), rand) && entity.checkSpawnObstruction(sl)) {
						sl.addFreshEntity(entity);
						sl.levelEvent(2004, this.worldPosition, 0);
						entity.spawnAnim();
					}
				}
			}
		}
		return change;
	}

	private boolean handleHungryNodeSecond(boolean change) {
		if (this.nodeType == NodeType.HUNGRY && this.count % 50 == 0) {
			var rand = this.level.getRandom();
			int tx = this.worldPosition.getX() + rand.nextInt(16) - rand.nextInt(16);
			int ty = this.worldPosition.getY() + rand.nextInt(16) - rand.nextInt(16);
			int tz = this.worldPosition.getZ() + rand.nextInt(16) - rand.nextInt(16);
			int h = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, tx, tz);
			if (ty > h) {
				ty = h;
			}
			BlockHitResult mop = rayTraceIgnoringSource(new Vec3(tx + 0.5, ty + 0.5, tz + 0.5));
			if (mop != null && mop.getType() == HitResult.Type.BLOCK && this.worldPosition.distSqr(mop.getBlockPos()) < 256.0) {
				BlockPos p = mop.getBlockPos();
				BlockState bs = this.level.getBlockState(p);
				if (!bs.isAir()) {
					float hard = bs.getDestroySpeed(this.level, p);
					if (hard >= 0.0F && hard < 5.0F) {
						this.level.destroyBlock(p, true);
					}
				}
			}
		}
		return change;
	}

	public byte getLock() {
		return this.nodeLock;
	}

	/** TileNode.checkLock: an unpowered node stabilizer below locks the node (an advanced one more firmly). */
	public void checkLock() {
		if ((this.count <= 1 || this.count % 50 == 0) && this.getBlockState().is(TVBlocks.AURA_NODE)) {
			byte oldLock = this.nodeLock;
			this.nodeLock = 0;
			BlockPos below = this.worldPosition.below();
			if (!this.level.hasNeighborSignal(below)) {
				BlockState bs = this.level.getBlockState(below);
				if (bs.is(TVBlocks.NODE_STABILIZER)) {
					this.nodeLock = 1;
				} else if (bs.is(TVBlocks.ADVANCED_NODE_STABILIZER)) {
					this.nodeLock = 2;
				}
			}
			if (oldLock != this.nodeLock) {
				this.regeneration = -1;
			}
		}
	}
}

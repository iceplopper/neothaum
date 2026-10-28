package dev.neothaum.tiles;

import dev.neothaum.api.InfusionStabiliser;
import dev.neothaum.api.Wandable;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectContainer;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.aspects.EssentiaHandler;
import dev.neothaum.blocks.FluxFluidBlock;
import dev.neothaum.crafting.InfusionEnchantmentRecipe;
import dev.neothaum.crafting.InfusionRecipe;
import dev.neothaum.fx.FX;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVMobEffects;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.warp.Warp;
import dev.neothaum.wands.WandManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.Codec;

/**
 * TileInfusionMatrix: the runic matrix of an infusion altar. Wanded once it wakes; wanded again it reads the central and surrounding pedestals
 * for an infusion (or infusion enchantment) recipe, then every half second draws a point of essentia from jars within 12 blocks, and once
 * that is all in, takes the components off their pedestals one by one. Instability, from the recipe and from an unbalanced layout of
 * pedestals and stabilisers, makes things go wrong along the way.
 */
public class InfusionMatrixBlockEntity extends TVBlockEntity implements Wandable, AspectContainer {
	private final List<BlockPos> pedestals = new ArrayList<>();
	public boolean active;
	public boolean crafting;
	public boolean checkSurroundings = true;
	public int symmetry;
	public int instability;
	private AspectList recipeEssentia = new AspectList();
	/** The recipe being made, and which of its components are still on their pedestals. */
	private ResourceKey<Recipe<?>> recipeId;
	private List<Integer> remaining = new ArrayList<>();
	private List<Ingredient> recipeIngredients;
	private ItemStack recipeOutput = ItemStack.EMPTY;
	private Holder<Enchantment> recipeEnchantment;
	private String recipePlayer;
	private ItemStack recipeInput = ItemStack.EMPTY;
	private int recipeInstability;
	private int recipeXP;
	/** 0 = infusion, 1 = infusion enchantment, 2 = runic augment (its components depend on the central item). */
	private int recipeType;
	public int count;
	public int craftCount;
	public float startUp;
	private int countDelay = 10;
	private int itemCount;
	/** Client: what is flowing into the matrix (pedestal or player) and for how much longer. */
	private final Map<String, SourceFX> sourceFX = new HashMap<>();

	public static final class SourceFX {
		final BlockPos loc;
		int ticks;
		final int entity;

		SourceFX(BlockPos loc, int ticks, int entity) {
			this.loc = loc;
			this.ticks = ticks;
			this.entity = entity;
		}
	}

	public InfusionMatrixBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.INFUSION_MATRIX, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.active = input.getBooleanOr("active", false);
		this.crafting = input.getBooleanOr("crafting", false);
		this.instability = input.getShortOr("instability", (short) 0);
		this.recipeEssentia = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		this.recipeId = input.read("recipe", Identifier.CODEC).map(id -> ResourceKey.create(Registries.RECIPE, id)).orElse(null);
		this.remaining = new ArrayList<>(input.read("remaining", Codec.INT.listOf()).orElse(List.of()));
		this.recipeIngredients = null;
		this.recipeInput = input.read("recipeinput", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		this.recipeOutput = input.read("recipeout", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		this.recipeInstability = input.getIntOr("recipeinst", 0);
		this.recipeType = input.getIntOr("recipetype", 0);
		this.recipeXP = input.getIntOr("recipexp", 0);
		this.recipePlayer = input.getStringOr("recipeplayer", "");
		if (this.recipePlayer.isEmpty()) {
			this.recipePlayer = null;
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("active", this.active);
		output.putBoolean("crafting", this.crafting);
		output.putShort("instability", (short) this.instability);
		output.store("Aspects", AspectList.CODEC, this.recipeEssentia);
		if (this.recipeId != null) {
			output.store("recipe", Identifier.CODEC, this.recipeId.identifier());
		}
		output.store("remaining", Codec.INT.listOf(), this.remaining);
		output.store("recipeinput", ItemStack.OPTIONAL_CODEC, this.recipeInput);
		output.store("recipeout", ItemStack.OPTIONAL_CODEC, this.recipeOutput);
		output.putInt("recipeinst", this.recipeInstability);
		output.putInt("recipetype", this.recipeType);
		output.putInt("recipexp", this.recipeXP);
		output.putString("recipeplayer", this.recipePlayer == null ? "" : this.recipePlayer);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity m) {
		m.count++;
		if (m.checkSurroundings) {
			m.checkSurroundings = false;
			m.getSurroundings();
		}
		if (level.isClientSide()) {
			m.doEffects();
			return;
		}
		if (m.count % (m.crafting ? 20 : 100) == 0 && !m.validLocation()) {
			m.active = false;
			m.markForUpdate();
			return;
		}
		if (m.active && m.crafting && m.count % m.countDelay == 0) {
			m.craftCycle();
			m.setChanged();
		}
	}

	private BlockPos center() {
		return this.worldPosition.below(2);
	}

	public boolean validLocation() {
		if (!(this.level.getBlockEntity(this.center()) instanceof PedestalBlockEntity)) {
			return false;
		}
		for (int[] c : new int[][]{{1, 1}, {1, -1}, {-1, -1}, {-1, 1}}) {
			if (!(this.level.getBlockEntity(this.center().offset(c[0], 0, c[1])) instanceof InfusionPillarBlockEntity)) {
				return false;
			}
		}
		return true;
	}

	private PedestalBlockEntity centralPedestal() {
		return this.level.getBlockEntity(this.center()) instanceof PedestalBlockEntity p ? p : null;
	}

	// ------------------------------------------------------------------ starting

	public void craftingStart(Player player) {
		if (!this.validLocation()) {
			this.active = false;
			this.markForUpdate();
			return;
		}
		this.getSurroundings();
		PedestalBlockEntity central = this.centralPedestal();
		this.recipeInput = central == null || central.getItem().isEmpty() ? ItemStack.EMPTY : central.getItem().copy();
		if (this.recipeInput.isEmpty()) {
			return;
		}
		List<ItemStack> components = new ArrayList<>();
		for (BlockPos cc : this.pedestals) {
			if (this.level.getBlockEntity(cc) instanceof PedestalBlockEntity ped && !ped.getItem().isEmpty()) {
				components.add(ped.getItem().copy());
			}
		}
		if (components.isEmpty() || !(this.level instanceof ServerLevel sl)) {
			return;
		}
		Collection<RecipeHolder<?>> all = sl.getServer().getRecipeManager().getRecipes();
		for (RecipeHolder<?> h : all) {
			if (h.value() instanceof InfusionRecipe r && r.matches(components, this.recipeInput, player)) {
				this.recipeType = 0;
				this.setRecipe(h.id(), r.components());
				this.recipeOutput = r.output(this.recipeInput);
				this.recipeInstability = r.instability();
				this.recipeEssentia = r.aspects().copy();
				this.begin(player);
				return;
			}
		}
		if (dev.neothaum.crafting.RunicAugment.matches(components, this.recipeInput, player)) {
			this.recipeType = 2;
			this.recipeId = null;
			this.recipeIngredients = dev.neothaum.crafting.RunicAugment.components(this.recipeInput);
			this.remaining = new ArrayList<>();
			for (int i = 0; i < this.recipeIngredients.size(); i++) {
				this.remaining.add(i);
			}
			this.recipeOutput = dev.neothaum.crafting.RunicAugment.output(this.recipeInput);
			this.recipeInstability = dev.neothaum.crafting.RunicAugment.instability(this.recipeInput);
			this.recipeEssentia = dev.neothaum.crafting.RunicAugment.aspects(this.recipeInput);
			this.begin(player);
			return;
		}
		for (RecipeHolder<?> h : all) {
			if (h.value() instanceof InfusionEnchantmentRecipe r && r.matches(components, this.recipeInput, player)) {
				this.recipeType = 1;
				this.setRecipe(h.id(), r.components());
				this.recipeEnchantment = r.enchantment();
				this.recipeOutput = ItemStack.EMPTY;
				this.recipeInstability = r.calcInstability(this.recipeInput);
				AspectList esscost = r.aspects().copy();
				float essmod = r.getEssentiaMod(this.recipeInput);
				for (Aspect as : esscost.getAspects()) {
					esscost.add(as, (int) (esscost.getAmount(as) * essmod));
				}
				this.recipeEssentia = esscost;
				this.recipeXP = r.calcXP(this.recipeInput);
				this.begin(player);
				return;
			}
		}
	}

	private void setRecipe(ResourceKey<Recipe<?>> id, List<Ingredient> components) {
		this.recipeId = id;
		this.recipeIngredients = components;
		this.remaining = new ArrayList<>();
		for (int i = 0; i < components.size(); i++) {
			this.remaining.add(i);
		}
	}

	private void begin(Player player) {
		this.recipePlayer = player.getName().getString();
		this.instability = this.symmetry + this.recipeInstability;
		this.crafting = true;
		this.level.playSound(null, this.worldPosition, TVSounds.CRAFTSTART, SoundSource.BLOCKS, 0.5F, 1.0F);
		this.markForUpdate();
	}

	/** After a reload, look the recipe's components up again by its id. */
	private List<Ingredient> ingredients() {
		if (this.recipeIngredients == null && this.recipeType == 2) {
			this.recipeIngredients = dev.neothaum.crafting.RunicAugment.components(this.recipeInput);
		}
		if (this.recipeIngredients == null && this.recipeId != null && this.level instanceof ServerLevel sl) {
			Optional<RecipeHolder<?>> h = sl.getServer().getRecipeManager().byKey(this.recipeId);
			this.recipeIngredients = h.map(RecipeHolder::value).map(r -> r instanceof InfusionRecipe ir ? ir.components()
				: r instanceof InfusionEnchantmentRecipe er ? er.components() : null).orElse(List.of());
			if (h.isPresent() && h.get().value() instanceof InfusionEnchantmentRecipe er) {
				this.recipeEnchantment = er.enchantment();
			}
		}
		return this.recipeIngredients == null ? List.of() : this.recipeIngredients;
	}

	// ------------------------------------------------------------------ crafting

	public void craftCycle() {
		var rand = this.level.getRandom();
		PedestalBlockEntity central = this.centralPedestal();
		boolean valid = central != null && !central.getItem().isEmpty() && ItemStack.isSameItemSameComponents(central.getItem(), this.recipeInput);
		if (!valid || this.instability > 0 && rand.nextInt(500) <= this.instability) {
			switch (rand.nextInt(21)) {
				case 0, 2, 10, 13 -> this.inEvEjectItem(0);
				case 1, 11 -> this.inEvEjectItem(2);
				case 3, 8, 14 -> this.inEvZap(false);
				case 4, 15 -> this.inEvEjectItem(5);
				case 5, 16 -> this.inEvHarm(false);
				case 6, 17 -> this.inEvEjectItem(1);
				case 7 -> this.inEvEjectItem(4);
				case 9 -> this.level.explode(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
					1.5F + rand.nextFloat(), Level.ExplosionInteraction.NONE);
				case 12 -> this.inEvZap(true);
				case 18 -> this.inEvHarm(true);
				case 19 -> this.inEvEjectItem(3);
				case 20 -> this.inEvWarp();
				default -> {
				}
			}
			if (valid) {
				return;
			}
		}
		if (!valid) {
			this.instability = 0;
			this.crafting = false;
			this.recipeEssentia = new AspectList();
			this.level.playSound(null, this.worldPosition, TVSounds.CRAFTFAIL, SoundSource.BLOCKS, 1.0F, 0.6F);
			this.markForUpdate();
			return;
		}
		if (this.recipeType == 1 && this.recipeXP > 0) {
			this.drainExperience();
			return;
		}
		if (this.recipeType == 1 && this.recipeXP == 0) {
			this.countDelay = 10;
		}
		if (this.recipeEssentia.visSize() > 0) {
			for (Aspect aspect : this.recipeEssentia.getAspects()) {
				if (this.recipeEssentia.getAmount(aspect) > 0) {
					if (EssentiaHandler.drainEssentia(this, aspect, null, 12)) {
						this.recipeEssentia.reduce(aspect, 1);
						this.markForUpdate();
						return;
					}
					if (rand.nextInt(100 - this.recipeInstability * 3) == 0) {
						this.instability++;
					}
					this.instability = Math.min(this.instability, 25);
					this.markForUpdate();
				}
			}
			this.checkSurroundings = true;
		} else if (this.remaining.isEmpty()) {
			this.instability = 0;
			this.crafting = false;
			this.craftingFinish();
			this.recipeOutput = ItemStack.EMPTY;
			this.markForUpdate();
		} else {
			List<Ingredient> ings = this.ingredients();
			for (int a = 0; a < this.remaining.size(); a++) {
				Ingredient want = ings.get(this.remaining.get(a));
				for (BlockPos cc : this.pedestals) {
					if (this.level.getBlockEntity(cc) instanceof PedestalBlockEntity ped && !ped.getItem().isEmpty() && want.test(ped.getItem())) {
						if (this.itemCount == 0) {
							this.itemCount = 5;
							TVNetwork.sendInfusionSource(this.level, this.worldPosition, cc, 0);
						} else if (this.itemCount-- <= 1) {
							var rem = ped.getItem().getItem().getCraftingRemainder();
							ped.setItem(0, rem == null ? ItemStack.EMPTY : rem.create());
							this.remaining.remove(a);
						}
						return;
					}
				}
				// a component is missing: the essentia cost creeps back up
				Aspect[] ingEss = this.recipeEssentia.getAspects();
				if (ingEss.length > 0 && rand.nextInt(1 + a) == 0) {
					Aspect asx = ingEss[rand.nextInt(ingEss.length)];
					this.recipeEssentia.add(asx, 1);
					if (rand.nextInt(50 - this.recipeInstability * 2) == 0) {
						this.instability++;
					}
					this.instability = Math.min(this.instability, 25);
					this.markForUpdate();
				}
			}
		}
	}

	/** Infusion enchantment: a level of experience from each nearby player per cycle, and a little pain. */
	private void drainExperience() {
		var rand = this.level.getRandom();
		for (Player target : this.level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(10.0))) {
			if (target.experienceLevel > 0) {
				target.giveExperienceLevels(-1);
				this.recipeXP--;
				if (this.level instanceof ServerLevel sl) {
					target.hurtServer(sl, sl.damageSources().magic(), rand.nextInt(2));
				}
				TVNetwork.sendInfusionSource(this.level, this.worldPosition, this.worldPosition, target.getId());
				this.level.playSound(null, target.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 2.0F + rand.nextFloat() * 0.4F);
				this.countDelay = 20;
				return;
			}
		}
		Aspect[] ingEss = this.recipeEssentia.getAspects();
		if (ingEss.length > 0 && rand.nextInt(3) == 0) {
			this.recipeEssentia.add(ingEss[rand.nextInt(ingEss.length)], 1);
			if (rand.nextInt(50 - this.recipeInstability * 2) == 0) {
				this.instability++;
			}
			this.instability = Math.min(this.instability, 25);
			this.markForUpdate();
		}
	}

	private List<LivingEntity> nearby() {
		return this.level.getEntitiesOfClass(LivingEntity.class, new AABB(this.worldPosition).inflate(10.0));
	}

	private void inEvZap(boolean all) {
		for (LivingEntity target : this.nearby()) {
			TVNetwork.sendZap(this.level, this.worldPosition, target.blockPosition().above((int) (target.getBbHeight() / 2.0F)));
			if (this.level instanceof ServerLevel sl) {
				target.hurtServer(sl, sl.damageSources().magic(), 4 + this.level.getRandom().nextInt(4));
			}
			if (!all) {
				break;
			}
		}
	}

	private void inEvHarm(boolean all) {
		for (LivingEntity target : this.nearby()) {
			if (this.level.getRandom().nextBoolean()) {
				target.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 120, 0));
			} else {
				target.addEffect(new MobEffectInstance(TVMobEffects.VIS_EXHAUST, 2400, 0, true, true));
			}
			if (!all) {
				break;
			}
		}
	}

	private void inEvWarp() {
		List<Player> targets = this.level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(10.0));
		if (!targets.isEmpty()) {
			Player target = targets.get(this.level.getRandom().nextInt(targets.size()));
			if (this.level.getRandom().nextFloat() < 0.25F) {
				Warp.addStickyWarpToPlayer(target, 1);
			} else {
				Warp.addWarpToPlayer(target, 1 + this.level.getRandom().nextInt(5), true);
			}
		}
	}

	/** 0 drops, 1 drops in goo, 2 drops in gas, 3 destroys in goo, 4 destroys in gas, 5 drops with a bang. */
	private void inEvEjectItem(int type) {
		var rand = this.level.getRandom();
		for (int q = 0; q < 50 && !this.pedestals.isEmpty(); q++) {
			BlockPos cc = this.pedestals.get(rand.nextInt(this.pedestals.size()));
			if (!(this.level.getBlockEntity(cc) instanceof PedestalBlockEntity ped) || ped.getItem().isEmpty()) {
				continue;
			}
			if (type >= 3 && type != 5) {
				ped.setItem(0, ItemStack.EMPTY);
			} else {
				Containers.dropContents(this.level, cc, ped);
				ped.markForUpdate();
			}
			if (type == 1 || type == 3) {
				this.level.setBlockAndUpdate(cc.above(), ((FluxFluidBlock) TVBlocks.FLUX_GOO).withQuanta(this.level, cc.above(), 8));
				this.level.playSound(null, cc, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.3F, 1.0F);
			} else if (type == 2 || type == 4) {
				this.level.setBlockAndUpdate(cc.above(), ((FluxFluidBlock) TVBlocks.FLUX_GAS).withQuanta(this.level, cc.above(), 8));
				this.level.playSound(null, cc, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 1.0F);
			} else if (type == 5) {
				this.level.explode(null, cc.getX() + 0.5, cc.getY() + 0.5, cc.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
			}
			this.level.blockEvent(cc, this.level.getBlockState(cc).getBlock(), 11, 0);
			TVNetwork.sendZap(this.level, this.worldPosition, cc.above());
			return;
		}
	}

	public void craftingFinish() {
		PedestalBlockEntity ped = this.centralPedestal();
		if (ped == null) {
			return;
		}
		if (this.recipeType != 1 && !this.recipeOutput.isEmpty()) {
			ped.setItemFromInfusion(this.recipeOutput.copy());
		} else if (this.recipeType == 1) {
			this.ingredients();
			if (this.recipeEnchantment != null) {
				ItemStack temp = ped.getItem();
				int lvl = EnchantmentHelper.getItemEnchantmentLevel(this.recipeEnchantment, temp);
				EnchantmentHelper.updateEnchantments(temp, e -> e.set(this.recipeEnchantment, lvl + 1));
				ped.markForUpdate();
			}
		}
		if (this.recipePlayer != null && this.level instanceof ServerLevel sl) {
			Player p = sl.getServer().getPlayerList().getPlayerByName(this.recipePlayer);
			if (p != null) {
				ped.getItem().onCraftedBy(p, ped.getItem().getCount());
			}
		}
		this.recipeEssentia = new AspectList();
		this.recipeId = null;
		this.recipeIngredients = null;
		this.markForUpdate();
		this.level.blockEvent(this.center(), this.level.getBlockState(this.center()).getBlock(), 12, 0);
	}

	// ------------------------------------------------------------------ layout

	/** Stabilisers that are not InfusionStabiliser blocks (the vanilla candles addon adds lit candles). */
	public static final List<java.util.function.BiPredicate<Level, BlockPos>> EXTRA_STABILISERS = new ArrayList<>();

	private static boolean isStabiliser(Level level, BlockPos p) {
		BlockState s = level.getBlockState(p);
		return s.getBlock() instanceof AbstractSkullBlock || s.getBlock() instanceof InfusionStabiliser st && st.canStabaliseInfusion(level, p)
			|| EXTRA_STABILISERS.stream().anyMatch(e -> e.test(level, p));
	}

	/**
	 * Finds the pedestals (the first one in each column below the matrix, within 8 blocks) and the stabilisers (within 12), then scores the
	 * layout: every pedestal or stabiliser without a twin mirrored through the matrix adds instability.
	 */
	private void getSurroundings() {
		if (this.level == null) {
			return;
		}
		List<BlockPos> stuff = new ArrayList<>();
		this.pedestals.clear();
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int xx = -12; xx <= 12; xx++) {
			for (int zz = -12; zz <= 12; zz++) {
				boolean skip = false;
				for (int yy = -5; yy <= 10; yy++) {
					if (xx == 0 && zz == 0) {
						continue;
					}
					m.set(this.worldPosition.getX() + xx, this.worldPosition.getY() - yy, this.worldPosition.getZ() + zz);
					if (!skip && yy > 0 && Math.abs(xx) <= 8 && Math.abs(zz) <= 8 && this.level.getBlockEntity(m) instanceof PedestalBlockEntity) {
						this.pedestals.add(m.immutable());
						skip = true;
					} else if (isStabiliser(this.level, m)) {
						stuff.add(m.immutable());
					}
				}
			}
		}
		this.symmetry = 0;
		for (BlockPos cc : this.pedestals) {
			boolean items = false;
			if (this.level.getBlockEntity(cc) instanceof PedestalBlockEntity ped) {
				this.symmetry += 2;
				if (!ped.getItem().isEmpty()) {
					this.symmetry++;
					items = true;
				}
			}
			BlockPos mirror = new BlockPos(2 * this.worldPosition.getX() - cc.getX(), cc.getY(), 2 * this.worldPosition.getZ() - cc.getZ());
			if (this.level.getBlockEntity(mirror) instanceof PedestalBlockEntity ped) {
				this.symmetry -= 2;
				if (!ped.getItem().isEmpty() && items) {
					this.symmetry--;
				}
			}
		}
		float sym = 0.0F;
		for (BlockPos cc : stuff) {
			if (isStabiliser(this.level, cc)) {
				sym += 0.1F;
			}
			BlockPos mirror = new BlockPos(2 * this.worldPosition.getX() - cc.getX(), cc.getY(), 2 * this.worldPosition.getZ() - cc.getZ());
			// TC4 asked the mirrored block whether the original spot could stabilise; the same block answers the same
			Block bi = this.level.getBlockState(mirror).getBlock();
			if (bi instanceof AbstractSkullBlock || bi instanceof InfusionStabiliser st && st.canStabaliseInfusion(this.level, cc)
				|| EXTRA_STABILISERS.stream().anyMatch(e -> e.test(this.level, mirror))) {
				sym -= 0.2F;
			}
		}
		this.symmetry = (int) (this.symmetry + sym);
	}

	// ------------------------------------------------------------------ wand

	@Override
	public boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, BlockHitResult hit) {
		// the matrix's wand trigger builds the altar first, if the stone around it is ready
		if (!this.active && WandManager.createInfusionAltar(wand, player, level, pos)) {
			return true;
		}
		if (!level.isClientSide() && this.active && !this.crafting) {
			this.craftingStart(player);
			return true;
		}
		if (!level.isClientSide() && !this.active && this.validLocation()) {
			this.active = true;
			this.markForUpdate();
			return true;
		}
		return level.isClientSide();
	}

	// ------------------------------------------------------------------ client effects

	public void addSourceFX(BlockPos loc, int ticks, int entity) {
		String key = loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + entity;
		SourceFX sf = this.sourceFX.get(key);
		if (sf != null) {
			sf.ticks = ticks;
		} else {
			this.sourceFX.put(key, new SourceFX(loc, ticks, entity));
		}
	}

	private void doEffects() {
		var rand = this.level.getRandom();
		BlockPos p = this.worldPosition;
		if (this.crafting) {
			if (this.craftCount == 0) {
				this.level.playLocalSound(p, TVSounds.INFUSERSTART, SoundSource.BLOCKS, 0.5F, 1.0F, false);
			} else if (this.craftCount % 65 == 0) {
				this.level.playLocalSound(p, TVSounds.INFUSER, SoundSource.BLOCKS, 0.5F, 1.0F, false);
			}
			this.craftCount++;
			FX.blockRunes(this.level, p.getX(), p.getY() - 2, p.getZ(), 0.5F + rand.nextFloat() * 0.2F, 0.1F, 0.7F + rand.nextFloat() * 0.3F, 25, -0.03F);
		} else if (this.craftCount > 0) {
			this.craftCount = Math.min(50, Math.max(0, this.craftCount - 2));
		}
		if (this.active && this.startUp != 1.0F) {
			if (this.startUp < 1.0F) {
				this.startUp += Math.max(this.startUp / 10.0F, 0.001F);
			}
			if (this.startUp > 0.999) {
				this.startUp = 1.0F;
			}
		}
		if (!this.active && this.startUp > 0.0F) {
			this.startUp -= this.startUp / 10.0F;
			if (this.startUp < 0.001) {
				this.startUp = 0.0F;
			}
		}
		for (var it = this.sourceFX.entrySet().iterator(); it.hasNext(); ) {
			SourceFX fx = it.next().getValue();
			if (fx.ticks <= 0) {
				it.remove();
				continue;
			}
			if (fx.loc.equals(p)) {
				Entity player = this.level.getEntity(fx.entity);
				if (player != null) {
					for (int a = 0; a < 2; a++) {
						FX.infusionSparkle(this.level, player.getX() + (rand.nextFloat() - rand.nextFloat()) * player.getBbWidth(),
							player.getBoundingBox().minY + rand.nextFloat() * player.getBbHeight(),
							player.getZ() + (rand.nextFloat() - rand.nextFloat()) * player.getBbWidth(), p, 0.2F, 0.6F + rand.nextFloat() * 0.3F, 0.3F);
					}
				}
			} else if (this.level.getBlockEntity(fx.loc) instanceof PedestalBlockEntity ped) {
				ItemStack is = ped.getItem();
				if (!is.isEmpty()) {
					if (rand.nextInt(3) == 0) {
						FX.infusionSparkle(this.level, fx.loc.getX() + rand.nextFloat(), fx.loc.getY() + rand.nextFloat() + 1.0F,
							fx.loc.getZ() + rand.nextFloat(), p, 0.4F + rand.nextFloat() * 0.2F, 0.2F, 0.6F + rand.nextFloat() * 0.3F);
					} else if (is.getItem() instanceof BlockItem) {
						for (int a = 0; a < 2; a++) {
							FX.infusionItem(this.level, fx.loc.getX() + rand.nextFloat(), fx.loc.getY() + rand.nextFloat() + 1.0F,
								fx.loc.getZ() + rand.nextFloat(), p, is, false);
						}
					} else {
						for (int a = 0; a < 2; a++) {
							FX.infusionItem(this.level, fx.loc.getX() + 0.4F + rand.nextFloat() * 0.2F, fx.loc.getY() + 1.23F + rand.nextFloat() * 0.2F,
								fx.loc.getZ() + 0.4F + rand.nextFloat() * 0.2F, p, is, true);
						}
					}
				}
			} else {
				fx.ticks = 0;
			}
			fx.ticks--;
		}
		if (this.crafting && this.instability > 0 && rand.nextInt(200) <= this.instability) {
			// nodeBolt: a crackle of lightning off the matrix
			FX.wispTo(this.level, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, p.getX() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 2.0F,
				p.getY() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 2.0F, p.getZ() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 2.0F, 0.3F, 0,
				true, 0.0F);
		}
	}

	// ------------------------------------------------------------------ IAspectContainer (what the infusion still needs)

	@Override
	public AspectList getAspects() {
		return this.recipeEssentia;
	}

	@Override
	public void setAspects(AspectList aspects) {
	}

	@Override
	public int addToContainer(Aspect tag, int amount) {
		return 0;
	}

	@Override
	public boolean takeFromContainer(Aspect tag, int amount) {
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList ot) {
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tag, int amount) {
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		return false;
	}

	@Override
	public int containerContains(Aspect tag) {
		return 0;
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return true;
	}
}

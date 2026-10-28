package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemBobberEntity;
import dev.neothaum.golem.GolemEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;

/**
 * AIFish: cast a bobber onto open water near home and wait for a bite. Bites come sooner for strong golems and in big, deep, open water; the
 * catch is 1.7's fishing (junk, treasure or fish, with less junk and more treasure for entropy and order upgrades), sometimes two with air
 * upgrades, and cooked by fire upgrades.
 */
public class FishGoal extends GolemGoal {
	private float quality;
	private final float distance;
	private int count;
	private BlockPos target;
	private GolemBobberEntity bobber;

	private record Fishable(ItemStack stack, float damage, boolean enchant) {
		ItemStack make(ServerLevel level, RandomSource rand) {
			ItemStack s = this.stack.copy();
			if (this.damage > 0.0F && s.isDamageableItem()) {
				int max = (int) (s.getMaxDamage() * this.damage);
				s.setDamageValue(s.getMaxDamage() - rand.nextInt(rand.nextInt(Math.max(1, max)) + 1));
			}
			if (this.enchant) {
				var tag = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
				s = EnchantmentHelper.enchantItem(rand, s, 30, level.registryAccess(), tag.map(t -> t));
			}
			return s;
		}
	}

	private static Fishable f(ItemStack s) {
		return new Fishable(s, 0.0F, false);
	}

	private static final WeightedList<Fishable> LOOTCRAP = WeightedList.<Fishable>builder()
		.add(new Fishable(new ItemStack(Items.LEATHER_BOOTS), 0.9F, false), 10).add(f(new ItemStack(Items.LEATHER)), 10).add(f(new ItemStack(Items.BONE)), 10)
		.add(f(PotionContents.createItemStack(Items.POTION, Potions.WATER)), 10).add(f(new ItemStack(Items.STRING)), 5)
		.add(new Fishable(new ItemStack(Items.FISHING_ROD), 0.9F, false), 2).add(f(new ItemStack(Items.BOWL)), 10).add(f(new ItemStack(Items.STICK)), 5)
		.add(f(new ItemStack(Items.INK_SAC, 10)), 5).add(f(new ItemStack(Blocks.TRIPWIRE_HOOK)), 10).add(f(new ItemStack(Items.ROTTEN_FLESH)), 10).build();
	private static final WeightedList<Fishable> LOOTRARE = WeightedList.<Fishable>builder().add(f(new ItemStack(Blocks.LILY_PAD)), 1)
		.add(f(new ItemStack(Items.NAME_TAG)), 1).add(f(new ItemStack(Items.SADDLE)), 1).add(new Fishable(new ItemStack(Items.BOW), 0.25F, true), 1)
		.add(new Fishable(new ItemStack(Items.FISHING_ROD), 0.25F, true), 1).add(new Fishable(new ItemStack(Items.BOOK), 0.0F, true), 1).build();
	private static final WeightedList<Fishable> LOOTFISH = WeightedList.<Fishable>builder().add(f(new ItemStack(Items.COD)), 60)
		.add(f(new ItemStack(Items.SALMON)), 25).add(f(new ItemStack(Items.TROPICAL_FISH)), 2).add(f(new ItemStack(Items.PUFFERFISH)), 13).build();

	public FishGoal(GolemEntity golem) {
		super(golem);
		this.distance = (float) Math.ceil(golem.getRange() / 2.0F);
	}

	private boolean isWater(BlockPos p) {
		return this.golem.level().getFluidState(p).is(FluidTags.WATER);
	}

	@Override
	public boolean canUse() {
		if (this.target != null || this.count > 0 || !this.delayTick() || !this.navDone()) {
			return false;
		}
		if (this.bobber != null) {
			this.bobber.discard();
		}
		BlockPos vv = this.findWater();
		if (vv == null) {
			return false;
		}
		this.target = vv;
		this.quality = 0.0F;
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos p = vv.relative(dir);
			if (this.isWater(p) && this.golem.level().getBlockState(p.above()).isAir()) {
				this.quality += 3.0E-5F;
				if (this.golem.level().canSeeSky(p.above())) {
					this.quality += 3.0E-5F;
				}
				for (int depth = 1; depth <= 3; depth++) {
					if (this.isWater(p.below(depth))) {
						this.quality += 1.5E-5F;
					}
				}
			}
		}
		this.golem.playSound(SoundEvents.FISHING_BOBBER_THROW, 0.5F, 0.4F / (this.golem.getRandom().nextFloat() * 0.4F + 0.8F));
		this.bobber = new GolemBobberEntity(this.golem.level(), this.golem, vv);
		return this.golem.level().addFreshEntity(this.bobber);
	}

	@Override
	public boolean canContinueToUse() {
		return this.bobber != null && !this.bobber.isRemoved() && this.target != null && this.count-- > 0;
	}

	@Override
	public void tick() {
		if (this.target == null) {
			return;
		}
		ServerLevel level = (ServerLevel) this.golem.level();
		RandomSource rand = level.getRandom();
		this.golem.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 1.0, this.target.getZ() + 0.5, 30.0F, 30.0F);
		float chance = this.quality + this.golem.getGolemStrength() * 1.5E-4F;
		if (rand.nextFloat() >= chance) {
			return;
		}
		this.golem.startRightArmTimer();
		int qq = 1;
		if (this.golem.getUpgradeAmount(0) > 0 && rand.nextInt(10) < this.golem.getUpgradeAmount(0)) {
			qq++;
		}
		for (int a = 0; a < qq; a++) {
			ItemStack fs = this.getFishingResult(level, rand);
			if (this.golem.getUpgradeAmount(2) > 0) {
				var smelt = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(fs), level);
				if (smelt.isPresent()) {
					fs = smelt.get().value().assemble(new SingleRecipeInput(fs));
				}
			}
			ItemEntity item = new ItemEntity(level, this.target.getX() + 0.5, this.target.getY() + 1.0, this.target.getZ() + 0.5, fs);
			if (this.golem.getUpgradeAmount(2) > 0) {
				item.igniteForSeconds(2.0F);
			}
			item.setPickUpDelay(20);
			double d1 = this.golem.getX() + rand.nextFloat() - rand.nextFloat() - this.target.getX() + 0.5;
			double d3 = this.golem.getY() - this.target.getY() + 1.0;
			double d5 = this.golem.getZ() + rand.nextFloat() - rand.nextFloat() - this.target.getZ() + 0.5;
			double d7 = Math.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
			item.setDeltaMovement(d1 * 0.1, d3 * 0.1 + Math.sqrt(d7) * 0.08, d5 * 0.1);
			level.addFreshEntity(item);
		}
		if (this.bobber != null) {
			level.playSound(null, this.bobber.getX(), this.bobber.getY(), this.bobber.getZ(), SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.15F,
				1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
			level.sendParticles(ParticleTypes.SPLASH, this.bobber.getX(), this.bobber.getY() + 0.5, this.bobber.getZ(), 20 + rand.nextInt(20), 0.1, 0.0, 0.1,
				0.0);
			this.bobber.discard();
		}
		this.target = null;
	}

	@Override
	public void stop() {
		if (this.bobber != null) {
			this.bobber.discard();
		}
		this.target = null;
		this.count = -1;
	}

	@Override
	public void start() {
		this.count = 300 + this.golem.getRandom().nextInt(200);
		this.golem.startRightArmTimer();
	}

	private BlockPos findWater() {
		RandomSource rand = this.golem.getRandom();
		BlockPos h = this.golem.getHomePos();
		for (int i = 0; i < this.distance * 2.0F; i++) {
			int x = (int) (h.getX() + rand.nextInt((int) (1.0F + this.distance * 2.0F)) - this.distance);
			int y = (int) (h.getY() + rand.nextInt((int) (1.0F + this.distance)) - this.distance / 2.0F);
			int z = (int) (h.getZ() + rand.nextInt((int) (1.0F + this.distance * 2.0F)) - this.distance);
			BlockPos p = new BlockPos(x, y, z);
			if (this.isWater(p) && this.golem.level().getBlockState(p.above()).isAir()) {
				return p;
			}
		}
		return null;
	}

	private ItemStack getFishingResult(ServerLevel level, RandomSource rand) {
		float f = rand.nextFloat();
		float f1 = 0.1F - this.golem.getUpgradeAmount(5) * 0.025F;
		float f2 = 0.05F + this.golem.getUpgradeAmount(4) * 0.0125F;
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos p = this.target.relative(dir);
			if (this.isWater(p) && level.getBlockState(p.above()).isAir()) {
				f1 -= 0.005F;
				f2 += 0.00125F;
				if (level.canSeeSky(p.above())) {
					f1 -= 0.005F;
					f2 += 0.00125F;
				}
				for (int depth = 1; depth <= 3; depth++) {
					if (this.isWater(p.below(depth))) {
						f2 += 0.001F;
					}
				}
			}
		}
		f1 = Mth.clamp(f1, 0.0F, 1.0F);
		f2 = Mth.clamp(f2, 0.0F, 1.0F);
		WeightedList<Fishable> list;
		if (f < f1) {
			list = LOOTCRAP;
		} else if (f - f1 < f2) {
			list = LOOTRARE;
		} else {
			list = LOOTFISH;
		}
		Optional<Fishable> pick = list.getRandom(rand);
		return pick.map(p -> p.make(level, rand)).orElse(new ItemStack(Items.COD));
	}
}

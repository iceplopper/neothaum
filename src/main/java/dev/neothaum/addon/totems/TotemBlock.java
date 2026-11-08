package dev.neothaum.addon.totems;

import dev.neothaum.items.WandItem;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Traveller's Totem, or a Home Totem: a carved arcane stone pillar on a broad foot.
 * <ul>
 * <li>Traveller's Totem: use it to learn it, then to open the list of totems you know and travel to one.</li>
 * <li>Home Totem: strike it with a wand to make it your home; it can be travelled to, never from.</li>
 * <li>Either: a name tag renames it (for anyone who knows it); sneak and use a Wayfarer's Compass on one you know to save it.</li>
 * </ul>
 */
public class TotemBlock extends BaseEntityBlock {
	private static final VoxelShape SHAPE = Shapes.or(box(2, 0, 2, 14, 3, 14), box(4, 3, 4, 12, 15, 12), box(3, 15, 3, 13, 16, 13));
	public final boolean home;

	public TotemBlock(boolean home, Properties props) {
		super(props);
		this.home = home;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TotemBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : createTickerHelper(type, TravellersTotems.TOTEM_ENTITY, TotemBlockEntity::tick);
	}

	/** Only a thaumium pickaxe or better (the neothaum:breaks_totems tag) makes any headway; netherite does not. */
	@Override
	protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (!player.getMainHandItem().is(TravellersTotems.BREAKS_TOTEMS)) {
			return 0.0F;
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// a totem named at an anvil keeps its given name (else it names itself for the land round it, a moment after it is set down)
		if (stack.has(DataComponents.CUSTOM_NAME) && level instanceof ServerLevel sl && sl.getBlockEntity(pos) instanceof TotemBlockEntity totem) {
			String given = stack.getHoverName().getString().strip();
			if (given.length() > TravellersTotems.NAME_LENGTH) {
				given = given.substring(0, TravellersTotems.NAME_LENGTH);
			}
			if (!given.isEmpty()) {
				totem.name = given;
				TotemData.get(sl.getServer()).put(new TotemData.Totem(totem.globalPos(), given, this.home));
				totem.markForUpdate();
			}
		}
		// the first Home Totem a player sets down: how to claim one
		if (this.home && placer instanceof ServerPlayer sp && TravellersTotems.enabled()) {
			var known = sp.getAttachedOrCreate(TravellersTotems.KNOWN);
			if (!known.reminded()) {
				sp.sendSystemMessage(Component.translatable("neothaum.totem.home.remind").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
				sp.setAttached(TravellersTotems.KNOWN, known.withReminded());
			}
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
		// a totem taken up leaves everyone's list; set down elsewhere it is a new totem with a new name
		TotemData.get(level.getServer()).remove(GlobalPos.of(level.dimension(), pos));
		super.affectNeighborsAfterRemoval(state, level, pos, moved);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!TravellersTotems.enabled() || !(level.getBlockEntity(pos) instanceof TotemBlockEntity totem) || totem.name.isEmpty()) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		GlobalPos gp = totem.globalPos();
		boolean knows = player.getAttachedOrCreate(TravellersTotems.KNOWN).learned().contains(gp)
			|| player.getAttachedOrCreate(TravellersTotems.KNOWN).home().filter(gp::equals).isPresent();
		// a name tag renames it, for anyone who knows it
		if (stack.is(Items.NAME_TAG) && stack.has(DataComponents.CUSTOM_NAME)) {
			if (!knows) {
				return InteractionResult.FAIL;
			}
			if (player instanceof ServerPlayer sp) {
				totem.name = stack.getHoverName().getString();
				TotemData.get(sp.level().getServer()).put(new TotemData.Totem(gp, totem.name, this.home));
				totem.markForUpdate();
				stack.consume(1, player);
				sp.sendSystemMessage(Component.translatable("neothaum.totem.renamed", totem.name).withStyle(ChatFormatting.DARK_PURPLE));
			}
			return InteractionResult.SUCCESS;
		}
		// sneak with the compass: save this totem's bearings
		if (stack.is(TravellersTotems.compass) && player.isShiftKeyDown()) {
			if (player instanceof ServerPlayer sp) {
				saveToCompass(sp, stack, totem, knows);
			}
			return InteractionResult.SUCCESS;
		}
		// a wand on a Home Totem makes it yours
		if (this.home && stack.getItem() instanceof WandItem) {
			if (player instanceof ServerPlayer sp) {
				var known = sp.getAttachedOrCreate(TravellersTotems.KNOWN);
				if (known.home().filter(gp::equals).isPresent()) {
					sp.sendSystemMessage(Component.translatable("neothaum.totem.home.already", totem.name).withStyle(ChatFormatting.DARK_PURPLE));
				} else {
					String old = known.home().map(h -> TotemData.get(sp.level().getServer()).get(h)).map(TotemData.Totem::name).orElse(null);
					sp.setAttached(TravellersTotems.KNOWN, known.withHome(Optional.of(gp)));
					totem.attuned = true;
					totem.markForUpdate();
					sp.sendSystemMessage((old == null ? Component.translatable("neothaum.totem.home.set", totem.name)
						: Component.translatable("neothaum.totem.home.replaced", totem.name, old)).withStyle(ChatFormatting.DARK_PURPLE));
					level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!TravellersTotems.enabled() || !(level.getBlockEntity(pos) instanceof TotemBlockEntity totem) || totem.name.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!(player instanceof ServerPlayer sp)) {
			return InteractionResult.SUCCESS;
		}
		GlobalPos gp = totem.globalPos();
		var known = sp.getAttachedOrCreate(TravellersTotems.KNOWN);
		// sneaking: the naming screen, for anyone who knows it
		if (sp.isShiftKeyDown() && (known.learned().contains(gp) || known.home().filter(gp::equals).isPresent())) {
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, new TravellersTotems.OpenRename(gp, totem.name));
			return InteractionResult.SUCCESS;
		}
		if (this.home) {
			// a Home Totem says what it is, and how to claim it; it sends no one anywhere
			sp.sendSystemMessage(Component.translatable(known.home().filter(gp::equals).isPresent() ? "neothaum.totem.home.yours"
				: "neothaum.totem.home.info", totem.name).withStyle(ChatFormatting.DARK_PURPLE));
			return InteractionResult.SUCCESS;
		}
		if (!known.learned().contains(gp)) {
			sp.setAttached(TravellersTotems.KNOWN, known.learn(gp));
			totem.attuned = true;
			totem.markForUpdate();
			sp.sendSystemMessage(Component.translatable("neothaum.totem.learned", totem.name).withStyle(ChatFormatting.DARK_PURPLE));
			level.playSound(null, pos, dev.neothaum.registry.TVSounds.LEARN, SoundSource.PLAYERS, 0.75F, 1.0F);
			// the first totem found in the world teaches what they are
			if (totem.natural && !ResearchManager.isResearchComplete(sp, "TOTEMLORE") && ResearchManager.getResearch("TOTEMLORE") != null
				&& ResearchManager.completeResearch(sp, "TOTEMLORE")) {
				dev.neothaum.network.TVNetwork.sendResearchComplete(sp, "TOTEMLORE");
			}
			return InteractionResult.SUCCESS;
		}
		TotemTravel.openList(sp, Optional.of(gp));
		return InteractionResult.SUCCESS;
	}

	/** A name from the naming screen: for someone who knows the totem and stands near it. {@code restore} gives back its own Latin name. */
	public static void rename(ServerPlayer sp, GlobalPos gp, String name, boolean restore) {
		rename(sp, gp, name, restore, false);
	}

	/**
	 * Renames a totem for a player who knows it: to the name typed, to its own name ({@code restore}), or to another name drawn from its
	 * surroundings ({@code reroll}), after which the rename screen opens again on the new name, to keep or draw again.
	 */
	public static void rename(ServerPlayer sp, GlobalPos gp, String name, boolean restore, boolean reroll) {
		if (!TravellersTotems.enabled() || gp.dimension() != sp.level().dimension() || gp.pos().distToCenterSqr(sp.position()) > 64
			|| !(sp.level().getBlockEntity(gp.pos()) instanceof TotemBlockEntity totem)) {
			return;
		}
		var known = sp.getAttachedOrCreate(TravellersTotems.KNOWN);
		if (!known.learned().contains(gp) && known.home().filter(gp::equals).isEmpty()) {
			return;
		}
		TotemData data = TotemData.get(sp.level().getServer());
		String chosen;
		if (reroll) {
			// draws until one differs from the name it has
			chosen = totem.name;
			for (int i = 0; i < 12 && chosen.equals(totem.name); i++) {
				chosen = TotemNames.name((ServerLevel) sp.level(), gp.pos(), n -> !n.equals(totem.name) && data.nameTaken(n),
					sp.getRandom().nextLong() | 1L);
			}
		} else if (restore) {
			chosen = TotemNames.name((ServerLevel) sp.level(), gp.pos(), n -> !n.equals(totem.name) && data.nameTaken(n));
		} else {
			chosen = name.strip();
			if (chosen.length() > TravellersTotems.NAME_LENGTH) {
				chosen = chosen.substring(0, TravellersTotems.NAME_LENGTH);
			}
			if (chosen.isEmpty()) {
				return;
			}
		}
		totem.name = chosen;
		data.put(new TotemData.Totem(gp, chosen, totem.isHome()));
		totem.markForUpdate();
		sp.sendSystemMessage(Component.translatable("neothaum.totem.renamed", chosen).withStyle(ChatFormatting.DARK_PURPLE));
		if (reroll) {
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, new TravellersTotems.OpenRename(gp, chosen));
		}
	}

	private static void saveToCompass(ServerPlayer sp, ItemStack compass, TotemBlockEntity totem, boolean knows) {
		GlobalPos gp = totem.globalPos();
		List<GlobalPos> saved = new ArrayList<>(compass.getOrDefault(TravellersTotems.COMPASS_TOTEMS, List.of()));
		String key;
		if (!knows || totem.isHome()) {
			// a Home Totem is always on hand; any other must be learned first
			key = totem.isHome() ? "neothaum.compass.home" : "neothaum.compass.unknown";
		} else if (saved.contains(gp)) {
			key = "neothaum.compass.already";
		} else if (saved.size() >= TravellersTotems.COMPASS_SLOTS) {
			key = "neothaum.compass.full";
		} else {
			saved.add(gp);
			compass.set(TravellersTotems.COMPASS_TOTEMS, saved);
			key = "neothaum.compass.saved";
			sp.level().playSound(null, sp.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		sp.sendSystemMessage(Component.translatable(key, totem.name, saved.size(), TravellersTotems.COMPASS_SLOTS).withStyle(ChatFormatting.DARK_PURPLE));
	}
}

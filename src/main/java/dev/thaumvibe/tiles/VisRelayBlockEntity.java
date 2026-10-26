package dev.thaumvibe.tiles;

import dev.thaumvibe.api.Wandable;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.visnet.VisNetHandler;
import dev.thaumvibe.api.visnet.VisNodeBlockEntity;
import dev.thaumvibe.blocks.VisRelayBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVSounds;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * TileVisRelay: carries vis from an energized node to machines up to eight blocks away. A wand cycles its attunement through the six primals;
 * it flashes the colour of whatever vis passes through, and refills the vis amulets of players standing near it.
 */
public class VisRelayBlockEntity extends VisNodeBlockEntity implements Wandable {
	/** TileVisRelay.colors: the primal pulse colours. */
	public static final int[] COLORS = {16777086, 16727041, 37119, 40960, 15650047, 5592439};
	/** TileVisRelay.nearbyPlayers: the relay closest to each player, for vis amulets. */
	public static final Map<Integer, WeakReference<VisRelayBlockEntity>> NEARBY_PLAYERS = new ConcurrentHashMap<>();
	public byte color = -1;
	/** Client: where the parent is, relative to this relay. */
	public int px;
	public int py;
	public int pz;
	public int pulse;
	public float pRed = 0.5F;
	public float pGreen = 0.5F;
	public float pBlue = 0.5F;
	public float opacity = 0.3F;

	public VisRelayBlockEntity(BlockPos pos, BlockState state) {
		this(TVBlockEntities.VIS_RELAY, pos, state);
	}

	protected VisRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Direction orientation() {
		BlockState s = this.getBlockState();
		return s.hasProperty(VisRelayBlock.FACING) ? s.getValue(VisRelayBlock.FACING) : Direction.UP;
	}

	@Override
	public byte getAttunement() {
		return this.color;
	}

	@Override
	public int getRange() {
		return 8;
	}

	@Override
	public boolean isSource() {
		return false;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, VisRelayBlockEntity be) {
		be.relayTick();
	}

	protected void relayTick() {
		if (this.level.isClientSide()) {
			this.clientTick();
		}
		this.visTick();
		if (!this.level.isClientSide() && this.nodeCounter % 20 == 0) {
			for (Player player : this.level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(5.0))) {
				WeakReference<VisRelayBlockEntity> cur = NEARBY_PLAYERS.get(player.getId());
				if (cur == null || cur.get() == null
					|| cur.get().worldPosition.distToCenterSqr(player.position()) >= this.worldPosition.distToCenterSqr(player.position())) {
					NEARBY_PLAYERS.put(player.getId(), new WeakReference<>(this));
				}
			}
		}
		if (this.pulse > 0) {
			this.pulse--;
		}
	}

	/** TileVisRelay.drawEffect: the beam brightens on a pulse and fades back to grey. */
	private void clientTick() {
		this.pRed = Math.min(1.0F, this.pRed + 0.025F);
		this.pGreen = Math.min(1.0F, this.pGreen + 0.025F);
		this.pBlue = Math.min(1.0F, this.pBlue + 0.025F);
		if (this.opacity > 0.3F) {
			this.opacity = Math.max(0.3F, this.opacity - 0.025F);
		}
	}

	public boolean hasParent() {
		return this.px != 0 || this.py != 0 || this.pz != 0;
	}

	@Override
	public void triggerConsumeEffect(Aspect aspect) {
		int c = Aspect.getPrimalAspects().indexOf(aspect);
		if (c >= 0 && this.pulse == 0) {
			this.pulse = 5;
			this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, c);
		}
	}

	/** receiveClientEvent: the pulse runs back up the chain of relays toward the source. */
	@Override
	public boolean triggerEvent(int id, int param) {
		if (id != 0) {
			return super.triggerEvent(id, param);
		}
		if (this.level != null && this.level.isClientSide()) {
			int c = COLORS[Math.max(0, Math.min(5, param))];
			this.pulse(c);
			BlockPos p = this.worldPosition;
			VisRelayBlockEntity cur = this;
			for (int guard = 0; guard < 64 && cur.hasParent(); guard++) {
				p = p.offset(-cur.px, -cur.py, -cur.pz);
				if (!(this.level.getBlockEntity(p) instanceof VisRelayBlockEntity up) || up.pulse != 0) {
					break;
				}
				up.pulse(c);
				cur = up;
			}
		}
		return true;
	}

	private void pulse(int c) {
		this.pulse = 5;
		this.opacity = 0.8F;
		this.pRed = (c >> 16 & 255) / 255.0F;
		this.pGreen = (c >> 8 & 255) / 255.0F;
		this.pBlue = (c & 255) / 255.0F;
	}

	@Override
	public void parentChanged() {
		this.setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.color = input.getByteOr("color", (byte) -1);
		this.px = input.getByteOr("px", (byte) 0);
		this.py = input.getByteOr("py", (byte) 0);
		this.pz = input.getByteOr("pz", (byte) 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putByte("color", this.color);
		if (VisNetHandler.isNodeValid(this.getParent())) {
			BlockPos pp = this.getParent().get().getBlockPos();
			output.putByte("px", (byte) (this.worldPosition.getX() - pp.getX()));
			output.putByte("py", (byte) (this.worldPosition.getY() - pp.getY()));
			output.putByte("pz", (byte) (this.worldPosition.getZ() - pp.getZ()));
		} else {
			output.putByte("px", (byte) 0);
			output.putByte("py", (byte) 0);
			output.putByte("pz", (byte) 0);
		}
	}

	/** A wand click cycles the attunement: none, then air, fire, water, earth, order, entropy. */
	@Override
	public boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, BlockHitResult hit) {
		if (!level.isClientSide()) {
			this.color++;
			if (this.color > 5) {
				this.color = -1;
			}
			this.removeThisNode();
			this.nodeRefresh = true;
			this.markForUpdate();
			level.playSound(null, pos, TVSounds.CRYSTAL, SoundSource.BLOCKS, 0.2F, 1.0F);
		}
		return true;
	}
}

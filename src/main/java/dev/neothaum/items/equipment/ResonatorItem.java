package dev.neothaum.items.equipment;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.BufferBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** ItemResonator: knocked against anything that carries essentia, it reports what essentia is there and how hard that face pulls. */
public class ResonatorItem extends Item {
	public ResonatorItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		var level = ctx.getLevel();
		if (!(level.getBlockEntity(ctx.getClickedPos()) instanceof EssentiaTransport et)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		Player player = ctx.getPlayer();
		Direction face = ctx.getClickedFace();
		if (!(et instanceof BufferBlockEntity) && et.getEssentiaType(face) != null) {
			player.sendSystemMessage(Component.translatable("tc.resonator1", "" + et.getEssentiaAmount(face), et.getEssentiaType(face).getName()));
		} else if (et instanceof BufferBlockEntity buf && buf.getAspects().size() > 0) {
			for (Aspect aspect : buf.getAspects().getAspectsSorted()) {
				player.sendSystemMessage(Component.translatable("tc.resonator1", "" + buf.getAspects().getAmount(aspect), aspect.getName()));
			}
		}
		Component s = et.getSuctionType(face) != null ? Component.literal(et.getSuctionType(face).getName()) : Component.translatable("tc.resonator3");
		player.sendSystemMessage(Component.translatable("tc.resonator2", "" + et.getSuctionAmount(face), s));
		level.playSound(null, ctx.getClickedPos(), TVSounds.ALEMBICKNOCK, SoundSource.BLOCKS, 0.5F, 1.9F + level.getRandom().nextFloat() * 0.1F);
		return InteractionResult.SUCCESS;
	}
}

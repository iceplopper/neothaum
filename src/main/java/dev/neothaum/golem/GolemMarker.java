package dev.neothaum.golem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Marker: a block face a golemancer's bell has marked for a golem, with its colour (-1 for any). */
public record GolemMarker(BlockPos pos, ResourceKey<Level> dim, byte side, byte color) {
	public static final Codec<GolemMarker> CODEC = RecordCodecBuilder.create(i -> i.group(BlockPos.CODEC.fieldOf("pos").forGetter(GolemMarker::pos),
		ResourceKey.codec(Registries.DIMENSION).fieldOf("dim").forGetter(GolemMarker::dim), Codec.BYTE.fieldOf("side").forGetter(GolemMarker::side),
		Codec.BYTE.fieldOf("color").forGetter(GolemMarker::color)).apply(i, GolemMarker::new));
	public static final Codec<List<GolemMarker>> LIST_CODEC = CODEC.listOf();
	public static final StreamCodec<ByteBuf, GolemMarker> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, GolemMarker::pos,
		ResourceKey.streamCodec(Registries.DIMENSION), GolemMarker::dim, ByteBufCodecs.BYTE, GolemMarker::side, ByteBufCodecs.BYTE, GolemMarker::color,
		GolemMarker::new);
	public static final StreamCodec<ByteBuf, List<GolemMarker>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

	public GolemMarker withColor(int color) {
		return new GolemMarker(this.pos, this.dim, this.side, (byte) color);
	}
}

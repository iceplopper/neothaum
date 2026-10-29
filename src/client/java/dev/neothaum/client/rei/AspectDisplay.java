package dev.neothaum.client.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.neothaum.api.aspects.Aspect;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import net.minecraft.network.codec.ByteBufCodecs;

/** A compound aspect and the two it is made from, as JEI's aspect combination page shows them. Serialized as the aspect's tag. */
public class AspectDisplay extends BasicDisplay {
	public static final DisplaySerializer<AspectDisplay> SERIALIZER = DisplaySerializer.of(
		Codec.STRING.fieldOf("aspect").flatXmap(AspectDisplay::lookup, d -> DataResult.success(d.aspect.getTag())),
		ByteBufCodecs.STRING_UTF8.map(tag -> lookup(tag).getOrThrow(), d -> d.aspect.getTag()).cast(),
		false);

	public final Aspect aspect;
	public final Aspect component1;
	public final Aspect component2;

	public AspectDisplay(Aspect aspect) {
		super(List.of(), List.of(), Optional.empty());
		this.aspect = aspect;
		this.component1 = aspect.getComponents()[0];
		this.component2 = aspect.getComponents()[1];
	}

	private static DataResult<AspectDisplay> lookup(String tag) {
		Aspect a = Aspect.getAspect(tag);
		return a != null && a.getComponents() != null && a.getComponents().length >= 2 ? DataResult.success(new AspectDisplay(a))
			: DataResult.error(() -> "Not a compound aspect: " + tag);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return NeoThaumReiPlugin.ASPECT_DECOMP;
	}

	@Override
	public DisplaySerializer<? extends AspectDisplay> getSerializer() {
		return SERIALIZER;
	}
}

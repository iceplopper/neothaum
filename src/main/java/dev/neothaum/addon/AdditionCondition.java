package dev.neothaum.addon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.neothaum.NeoThaum;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.RegistryOps;

/**
 * A data file's load condition that holds while the named NeoThaum Addition is on:
 * {@code "fabric:load_conditions": [{"condition": "neothaum:addition", "addition": "wood_sets"}]}. Recipes for an addition use it, so they
 * are gone while it is off; the world needs a reload (or restart) to notice a change.
 */
public record AdditionCondition(String addition) implements ResourceCondition {
	public static final MapCodec<AdditionCondition> CODEC = Codec.STRING.fieldOf("addition").xmap(AdditionCondition::new, AdditionCondition::addition);
	public static final ResourceConditionType<AdditionCondition> TYPE = ResourceConditionType.create(NeoThaum.id("addition"), CODEC);

	public static void init() {
		ResourceConditions.register(TYPE);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
		return Additions.enabled(this.addition);
	}
}

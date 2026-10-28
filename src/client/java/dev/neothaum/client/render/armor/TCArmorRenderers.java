package dev.neothaum.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.client.entity.InhabitedZombieRenderer;
import dev.neothaum.items.equipment.FortressArmorItem;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.TransformCopyingModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Draws the armour TC4 gave its own models (getArmorModel): the crimson cultists' robes, plate and praetor's armour, the thaumium fortress
 * armour and the void robes. Helmets and leggings use the model built with f = 0.5, chestplates the one built with f = 1.0, as TC4 did.
 */
public final class TCArmorRenderers {
	private static final Identifier ROBE = NeoThaum.id("textures/models/cultist_robe_armor.png");
	private static final Identifier PLATE = NeoThaum.id("textures/models/cultist_plate_armor.png");
	private static final Identifier ZOMBIE_PLATE = NeoThaum.id("textures/models/zombie_plate_armor.png");
	private static final Identifier LEADER = NeoThaum.id("textures/models/cultist_leader_armor.png");
	private static final Identifier FORTRESS = NeoThaum.id("textures/models/fortress_armor.png");
	private static final Identifier VOID_DYED = NeoThaum.id("textures/models/void_robe_armor_overlay.png");
	private static final Identifier VOID = NeoThaum.id("textures/models/void_robe_armor.png");
	/** ItemVoidRobeArmor.getColor when undyed. */
	private static final int VOID_COLOR = 6961280;

	/** One model per slot, so the deferred draws of a helmet and a chestplate never share visibility flags. */
	private record Renderer(TCArmorModel.Kind kind, Supplier<LayerDefinition> small, Supplier<LayerDefinition> large, Identifier texture, boolean dyed)
		implements ArmorRenderer {
		private static final Map<TCArmorModel.Kind, Map<EquipmentSlot, TCArmorModel>> MODELS = new EnumMap<>(TCArmorModel.Kind.class);

		private TCArmorModel model(EquipmentSlot slot) {
			return MODELS.computeIfAbsent(this.kind, k -> new EnumMap<>(EquipmentSlot.class)).computeIfAbsent(slot, s -> new TCArmorModel(
				(s == EquipmentSlot.CHEST || s == EquipmentSlot.FEET ? this.large : this.small).get().bakeRoot(), this.kind, s));
		}

		@Override
		public void render(PoseStack ps, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state, EquipmentSlot slot, int light,
			HumanoidModel<HumanoidRenderState> context) {
			int set = 0;
			int mask = -1;
			boolean goggles = false;
			if (this.kind == TCArmorModel.Kind.FORTRESS) {
				for (ItemStack piece : new ItemStack[]{state.headEquipment, state.chestEquipment, state.legsEquipment}) {
					if (piece.getItem() instanceof FortressArmorItem) {
						set++;
					}
				}
				mask = state.headEquipment.getOrDefault(TVComponents.MASK, -1);
				goggles = state.headEquipment.getOrDefault(TVComponents.GOGGLES, false);
			}
			TCArmorModel model = this.model(slot);
			var copying = TransformCopyingModel.create(context, model, true);
			var args = Pair.of(state, new TCArmorModel.Args(state.walkAnimationPos, state.walkAnimationSpeed, set, mask, goggles));
			Identifier tex = this.texture == PLATE && state instanceof InhabitedZombieRenderer.State ? ZOMBIE_PLATE : this.texture;
			boolean foil = stack.hasFoil();
			if (this.dyed) {
				// ItemVoidRobeArmor: the dyed pass first, then the undyed one over it
				int color = ARGB.opaque(DyedItemColor.getOrDefault(stack, VOID_COLOR));
				collector.order(0).submitModel(copying, args, ps, foil ? RenderTypes.armorCutoutNoCullGlint(VOID_DYED) : RenderTypes.armorCutoutNoCull(VOID_DYED),
					light, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
				foil = false;
			}
			collector.order(1).submitModel(copying, args, ps, foil ? RenderTypes.armorCutoutNoCullGlint(tex) : RenderTypes.armorCutoutNoCull(tex), light,
				OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		}
	}

	public static void init() {
		ArmorRenderer.register(new Renderer(TCArmorModel.Kind.ROBE, TCArmorLayers::robeSmall, TCArmorLayers::robeLarge, ROBE, false),
			TVItems.CULTIST_ROBE_HELM, TVItems.CULTIST_ROBE_CHEST, TVItems.CULTIST_ROBE_LEGS);
		ArmorRenderer.register(new Renderer(TCArmorModel.Kind.KNIGHT, TCArmorLayers::knightSmall, TCArmorLayers::knightLarge, PLATE, false),
			TVItems.CULTIST_PLATE_HELM, TVItems.CULTIST_PLATE_CHEST, TVItems.CULTIST_PLATE_LEGS);
		ArmorRenderer.register(new Renderer(TCArmorModel.Kind.LEADER, TCArmorLayers::leaderSmall, TCArmorLayers::leaderLarge, LEADER, false),
			TVItems.CULTIST_LEADER_HELM, TVItems.CULTIST_LEADER_CHEST, TVItems.CULTIST_LEADER_LEGS);
		ArmorRenderer.register(new Renderer(TCArmorModel.Kind.FORTRESS, TCArmorLayers::fortressSmall, TCArmorLayers::fortressLarge, FORTRESS, false),
			TVItems.THAUMIUM_FORTRESS_HELM, TVItems.THAUMIUM_FORTRESS_CUIRASS, TVItems.THAUMIUM_FORTRESS_THIGH_GUARDS);
		ArmorRenderer.register(new Renderer(TCArmorModel.Kind.ROBE, TCArmorLayers::robeSmall, TCArmorLayers::robeLarge, VOID, true),
			TVItems.VOID_ROBE_HELM, TVItems.VOID_ROBE_CHEST, TVItems.VOID_ROBE_LEGS);
	}

	private TCArmorRenderers() {
	}
}

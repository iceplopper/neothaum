package dev.neothaum.client.render.armor;

import java.util.function.Function;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import org.jspecify.annotations.Nullable;

/**
 * One of TC4's custom armour models (ModelRobe, ModelKnightArmor, ModelLeaderArmor, ModelFortressArmor) for one armour slot. The wearer's pose
 * is copied onto its head, body, arm and leg parts first; setupAnim then shows only the parts the slot covers (as the items' getArmorModel did)
 * and swings the cloth with the walk.
 */
public class TCArmorModel extends Model<TCArmorModel.Args> {
	public enum Kind { ROBE, KNIGHT, LEADER, FORTRESS }

	/** walkPos and walkSpeed are the limb swing; set, mask and goggles only matter to the fortress armour. */
	public record Args(float walkPos, float walkSpeed, int set, int mask, boolean goggles) {
	}

	private final Kind kind;
	private final EquipmentSlot slot;
	private final Function<String, @Nullable ModelPart> parts;
	private final ModelPart head, hat, body, rightArm, leftArm, rightLeg, leftLeg;

	public TCArmorModel(ModelPart root, Kind kind, EquipmentSlot slot) {
		super(root, RenderTypes::armorCutoutNoCull);
		this.kind = kind;
		this.slot = slot;
		this.parts = root.createPartLookup();
		this.head = root.getChild("head");
		this.hat = root.getChild("hat");
		this.body = root.getChild("body");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	/** Runs after the wearer's pose has been copied on, so it leaves the main parts' transforms alone. */
	@Override
	public void setupAnim(Args a) {
		this.head.visible = this.hat.visible = this.slot == EquipmentSlot.HEAD;
		this.body.visible = this.slot == EquipmentSlot.CHEST || this.slot == EquipmentSlot.LEGS;
		this.rightArm.visible = this.leftArm.visible = this.slot == EquipmentSlot.CHEST;
		this.rightLeg.visible = this.leftLeg.visible = this.slot == EquipmentSlot.LEGS;
		// the helmet is drawn a hundredth larger, so the hood or helm never shows the head through it
		this.head.xScale = this.head.yScale = this.head.zScale = 1.01F;
		float a1 = Mth.cos(a.walkPos() * 0.6662F) * 1.4F * a.walkSpeed();
		float b1 = Mth.cos(a.walkPos() * 0.6662F + Mth.PI) * 1.4F * a.walkSpeed();
		float c = Math.min(a1, b1);
		switch (this.kind) {
			case ROBE -> {
				xRot(c - 0.1047198F, "frontcloth_r1", "frontcloth_l1");
				xRot(c - 0.3316126F, "frontcloth_r2", "frontcloth_l2");
				xRot(-c + 0.1047198F, "cloth_back_r1", "cloth_back_l1");
				xRot(-c + 0.2268928F, "cloth_back_r2", "cloth_back_l2", "cloth_back_r3", "cloth_back_l3");
			}
			case KNIGHT -> {
				xRot(c - 0.1047198F, "frontcloth1");
				xRot(c - 0.3316126F, "frontcloth2");
				cloak(c);
			}
			case LEADER -> {
				xRot(a1 - 0.1047198F, "leg_cloth_r");
				xRot(b1 - 0.1047198F, "leg_cloth_l");
				cloak(c);
			}
			case FORTRESS -> {
				// pieces of the set that show only once two or three parts of it are worn, and the helmet's mask or goggles
				show(a.goggles(), "goggles");
				for (int m = 0; m < 3; m++) {
					show(a.mask() == m, "mask" + m);
				}
				show(a.set() >= 3, "scroll", "ornament_l", "ornament_l2", "ornament_r", "ornament_r2", "gemornament", "gem", "shoulderplate_l2",
					"shoulderplate_l3", "shoulderplate_r2", "shoulderplate_r3", "sidepanel_r3", "sidepanel_l3");
				show(a.set() >= 2, "book", "flap_l", "flap_r", "shoulderplate_ltop", "shoulderplate_l1", "shoulderplate_rtop", "shoulderplate_r1",
					"sidepanel_r2", "sidepanel_l2");
			}
		}
	}

	private void cloak(float c) {
		xRot(-c / 2.0F + 0.1396263F, "cloak1");
		xRot(-c / 2.0F + 0.3069452F, "cloak2");
		xRot(-c / 2.0F + 0.4465716F, "cloak3");
	}

	private void xRot(float rot, String... names) {
		for (String n : names) {
			ModelPart p = this.parts.apply(n);
			if (p != null) {
				p.xRot = rot;
			}
		}
	}

	private void show(boolean visible, String... names) {
		for (String n : names) {
			ModelPart p = this.parts.apply(n);
			if (p != null) {
				p.visible = visible;
			}
		}
	}
}

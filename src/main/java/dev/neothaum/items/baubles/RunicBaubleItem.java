package dev.neothaum.items.baubles;

import dev.neothaum.api.RunicArmor;
import dev.neothaum.baubles.BaubleItem;
import dev.neothaum.baubles.BaubleType;
import dev.neothaum.runic.RunicShielding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ItemRingRunic, ItemAmuletRunic and ItemGirdleRunic: baubles that hold a runic shield charge. Some carry an extra trick when the shield
 * breaks (the charged ring recharges faster, the revitalizing ring heals, the kinetic girdle blasts attackers back, the emergency amulet
 * refills).
 */
public class RunicBaubleItem extends Item implements BaubleItem, RunicArmor {
	public enum Kind { NONE, CHARGED, KINETIC, HEALING, EMERGENCY }

	private final BaubleType type;
	private final int charge;
	public final Kind kind;

	public RunicBaubleItem(BaubleType type, int charge, Kind kind, Properties props) {
		super(props);
		this.type = type;
		this.charge = charge;
		this.kind = kind;
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return this.type;
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return this.charge;
	}

	@Override
	public void onEquipped(ItemStack stack, LivingEntity wearer) {
		RunicShielding.markDirty();
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity wearer) {
		RunicShielding.markDirty();
	}
}

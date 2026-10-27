package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.client.render.TVRenderTypes;
import dev.neothaum.menu.FocalManipulatorMenu;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.FocalManipulatorBlockEntity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * GuiFocalManipulator: the focus in the middle, its applied upgrades along the top, the upgrades its next rank offers below; pick one to see
 * its vis and experience price, then start it. While it runs, the price bar fills in the primals' colours as the vis arrives.
 */
public class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/gui_wandtable.png");
	private static final DecimalFormat FORMAT = new DecimalFormat("#######.#");
	private final Random rand = new Random();
	private int selected = -1;
	private int rank;
	private long nextSparkle;
	private final List<FocusUpgradeType> possible = new ArrayList<>();
	private final List<FocusUpgradeType> upgrades = new ArrayList<>();
	private AspectList aspects = new AspectList();
	private ItemStack lastFocus = ItemStack.EMPTY;
	private final Map<Long, Sparkle> sparkles = new LinkedHashMap<>();

	private static final class Sparkle {
		float x;
		float y;
		final float mx;
		final float my;
		final float r;
		final float g;
		final float b;
		int frame;
		long nextframe;

		Sparkle(float x, float y, float mx, float my, float r, float g, float b) {
			this.x = x;
			this.y = y;
			this.mx = mx;
			this.my = my;
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}

	public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 192, 233);
	}

	private ItemStack focus() {
		return this.menu.tile.getItem(0);
	}

	private int size() {
		return this.menu.data.get(6);
	}

	private AspectList tableAspects() {
		AspectList al = new AspectList();
		for (int i = 0; i < 6; i++) {
			int v = this.menu.data.get(i);
			if (v > 0) {
				al.add(Aspect.getPrimalAspects().get(i), v);
			}
		}
		return al;
	}

	private void gatherInfo() {
		this.possible.clear();
		this.upgrades.clear();
		this.aspects = new AspectList();
		ItemStack fs = this.focus();
		if (!(fs.getItem() instanceof FocusItem focus)) {
			return;
		}
		int[] s = FocusItem.getAppliedUpgrades(fs);
		this.rank = 1;
		int fu = 0;
		while (this.rank <= 5 && s[this.rank - 1] != -1) {
			FocusUpgradeType t = FocusUpgradeType.types[s[this.rank - 1]];
			if (t != null) {
				this.upgrades.add(t);
			}
			this.rank++;
			fu++;
		}
		if (fu == 5) {
			this.rank = -1;
		} else {
			FocusUpgradeType[] ut = focus.getPossibleUpgradesByRank(fs, this.rank);
			if (ut != null) {
				for (FocusUpgradeType t : ut) {
					if (focus.canApplyUpgrade(fs, Minecraft.getInstance().player, t, this.rank)) {
						this.possible.add(t);
					}
				}
			}
		}
		if (this.size() > 0) {
			this.selected = this.menu.data.get(7);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		long time = System.currentTimeMillis();
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		ItemStack fs = this.focus();
		if (fs.isEmpty() || !ItemStack.isSameItemSameComponents(fs, this.lastFocus)) {
			this.rank = 0;
			this.selected = -1;
			this.possible.clear();
			this.upgrades.clear();
			this.aspects = new AspectList();
			this.lastFocus = fs.copy();
		}
		if (this.rank == 0 && !fs.isEmpty()) {
			this.gatherInfo();
		}
		if (this.size() > 0 && this.selected < 0) {
			this.selected = this.menu.data.get(7);
		}
		if (this.rank > 0) {
			for (int i = 0; i < this.possible.size(); i++) {
				if (this.selected == this.possible.get(i).id) {
					g.blit(RenderPipelines.GUI_TEXTURED, TEX, k + 48 + i * 16, l + 104, 200, 0, 16, 16, 256, 256);
				}
			}
		}
		var player = Minecraft.getInstance().player;
		if (this.rank > 0 && this.selected >= 0 && !fs.isEmpty() && player != null) {
			int xp = this.rank * FocalManipulatorBlockEntity.XP_MULT;
			if (this.size() == 0 && (xp <= player.experienceLevel || player.isCreative())) {
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, k + 48, l + 88, 8, 240, 96, 8, 256, 256);
			}
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, k + 108, l + 59, 200, 16, 16, 16, 256, 256);
			AspectList ta = this.tableAspects();
			int start = 0;
			if (ta.size() > 0 && this.size() > 0) {
				for (Aspect aspect : ta.getAspectsSorted()) {
					int sz = (int) ((float) ta.getAmount(aspect) / this.size() * 96.0F);
					g.blit(RenderPipelines.GUI_TEXTURED, TEX, k + 48 + start, l + 88, 112 + start, 240, sz, 8, 256, 256,
						GuiUtil.color(aspect.getColor(), 0.9F));
					start += sz;
					if (this.rand.nextInt(66) == 0) {
						float x = 48 + start;
						float y = 92.0F;
						int c = aspect.getColor();
						this.sparkles.put(time + this.rand.nextInt(1000), new Sparkle(x, y, (46 + this.rank * 16 - x) / 9.0F, (38.0F - y) / 9.0F,
							(c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F));
					}
				}
			}
			g.text(this.font, String.valueOf(xp), k + 125, l + 64, xp > player.experienceLevel && !player.isCreative() ? 0xFFF67078 : 0xFF9BF68D, true);
			AspectList al = this.size() > 0 ? ta : this.aspects;
			int q = 0;
			for (Aspect ax : al.getAspectsSorted()) {
				g.pose().pushMatrix();
				g.pose().translate(k + 49, (float) (l + 68 - al.size() * 2.5));
				g.pose().scale(0.5F, 0.5F);
				g.text(this.font, ax.getName(), 0, q * 10, 0xFF000000 | ax.getColor(), true);
				g.text(this.font, FORMAT.format(al.getAmount(ax) / 100.0F), 48, q * 10, 0xFF000000 | ax.getColor(), true);
				g.pose().popMatrix();
				q++;
			}
		}
		if (this.rank > 0) {
			if (this.nextSparkle < time) {
				this.nextSparkle = time + (this.size() > 0 ? 10 : 500) + this.rand.nextInt(200);
				this.sparkles.put(time, new Sparkle(42 + this.rank * 16 + this.rand.nextInt(12), 34 + this.rand.nextInt(12), 0.0F, 0.0F,
					0.5F + this.rand.nextFloat() * 0.4F, 1.0F - this.rand.nextFloat() * 0.4F, 1.0F - this.rand.nextFloat() * 0.4F));
			}
			for (int i = 0; i < this.possible.size(); i++) {
				g.blit(RenderPipelines.GUI_TEXTURED, this.possible.get(i).icon, k + 48 + i * 16, l + 104, 0, 0, 16, 16, 16, 16);
			}
		}
		for (int i = 0; i < this.upgrades.size(); i++) {
			g.blit(RenderPipelines.GUI_TEXTURED, this.upgrades.get(i).icon, k + 56 + i * 16, l + 32, 0, 0, 16, 16, 16, 16);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		long time = System.currentTimeMillis();
		for (Iterator<Map.Entry<Long, Sparkle>> it = this.sparkles.entrySet().iterator(); it.hasNext(); ) {
			Sparkle s = it.next().getValue();
			float u0 = s.frame / 16.0F;
			g.pose().pushMatrix();
			g.pose().translate(s.x - 4, s.y - 4);
			g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, 0, 0, s.frame * 16.0F, 112.0F, 8, 8, 16, 16, 256, 256,
				GuiUtil.color(((int) (s.r * 255) << 16) | ((int) (s.g * 255) << 8) | (int) (s.b * 255), 0.9F));
			g.pose().popMatrix();
			if (s.nextframe < time) {
				s.frame++;
				s.nextframe = time + 50L;
				s.x += s.mx;
				s.y += s.my;
			}
			if (s.frame >= 9) {
				it.remove();
			}
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractRenderState(g, mouseX, mouseY, a);
		int baseX = (this.width - this.imageWidth) / 2;
		int baseY = (this.height - this.imageHeight) / 2;
		var player = Minecraft.getInstance().player;
		if (this.rank > 0) {
			for (int i = 0; i < this.possible.size(); i++) {
				if (in(mouseX, mouseY, baseX + 48 + i * 16, baseY + 104, 16, 16)) {
					this.upgradeTooltip(g, this.possible.get(i), baseX, baseY);
				}
			}
		}
		if (this.selected >= 0) {
			if (in(mouseX, mouseY, baseX + 48, baseY + 48, 36, 36)) {
				g.setTooltipForNextFrame(this.font, Component.translatable("wandtable.text1"), mouseX, mouseY);
			}
			if (in(mouseX, mouseY, baseX + 108, baseY + 58, 36, 16)) {
				g.setTooltipForNextFrame(this.font, Component.translatable("wandtable.text2"), mouseX, mouseY);
			}
			if (player != null && this.size() == 0 && (this.rank * 8 <= player.experienceLevel || player.isCreative())
				&& in(mouseX, mouseY, baseX + 48, baseY + 88, 96, 8)) {
				g.setTooltipForNextFrame(this.font, Component.translatable("wandtable.text3"), mouseX, mouseY);
			}
		}
		for (int i = 0; i < this.upgrades.size(); i++) {
			if (in(mouseX, mouseY, baseX + 56 + i * 16, baseY + 32, 16, 16)) {
				this.upgradeTooltip(g, this.upgrades.get(i), baseX, baseY);
			}
		}
	}

	/** drawHoveringTextFixed: the upgrade's name and description, wrapped to the right of the window. */
	private void upgradeTooltip(GuiGraphicsExtractor g, FocusUpgradeType u, int baseX, int baseY) {
		int w = Math.max(60, this.width - (baseX + this.imageWidth - 16));
		List<FormattedCharSequence> lines = new ArrayList<>();
		lines.add(u.getLocalizedName().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE).getVisualOrderText());
		lines.addAll(this.font.split(u.getLocalizedText(), w));
		g.setTooltipForNextFrame(lines, baseX + this.imageWidth - 36, baseY + 24);
	}

	private static boolean in(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && my >= y && mx < x + w && my < y + h;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean handled = super.mouseClicked(event, doubleClick);
		int gx = (this.width - this.imageWidth) / 2;
		int gy = (this.height - this.imageHeight) / 2;
		var mc = Minecraft.getInstance();
		if (this.size() == 0 && this.selected >= 0 && mc.player != null && (this.rank * 8 <= mc.player.experienceLevel || mc.player.isCreative())
			&& in(event.x(), event.y(), gx + 48, gy + 88, 96, 8)) {
			mc.gameMode.handleInventoryButtonClick(this.menu.containerId, this.selected);
			this.click();
			return true;
		}
		if (this.size() == 0) {
			for (int i = 0; i < this.possible.size(); i++) {
				FocusUpgradeType u = this.possible.get(i);
				if (in(event.x(), event.y(), gx + 48 + i * 16, gy + 104, 16, 16)) {
					this.aspects = new AspectList();
					if (this.selected == u.id) {
						this.selected = -1;
					} else {
						this.selected = u.id;
						this.aspects = FocalManipulatorBlockEntity.price(u.id, this.rank);
					}
					this.click();
					return true;
				}
			}
		}
		return handled;
	}

	private void click() {
		var p = Minecraft.getInstance().player;
		if (p != null) {
			p.playSound(TVSounds.CAMERACLACK, 0.4F, 1.0F);
		}
	}
}

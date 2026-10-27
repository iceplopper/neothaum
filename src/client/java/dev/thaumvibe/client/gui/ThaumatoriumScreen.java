package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.client.TVClientNetwork;
import dev.thaumvibe.crafting.CrucibleRecipe;
import dev.thaumvibe.menu.ThaumatoriumMenu;
import dev.thaumvibe.research.ResearchManager;
import dev.thaumvibe.tiles.ThaumatoriumBlockEntity;
import dev.thaumvibe.tiles.ThaumatoriumBlockEntity.CrucibleEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Port of GuiThaumatorium:
 * Slot 0 holds the catalyst; matching crucible recipes cycle on the top-right.
 * Allows toggling active recipes and displays required essentia progress bars.
 */
public class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {
	private static final Identifier TEX = Thaumvibe.id("textures/gui/gui_thaumatorium.png");

	private int index = 0;
	private int startAspect = 0;
	private int lastRecipeCount = 0;

	public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 175, 166);
	}

	private void blit(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h) {
		if (w > 0 && h > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, u, v, w, h, 256, 256);
		}
	}

	private void blitColored(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h, int argb) {
		if (w > 0 && h > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, u, v, w, h, w, h, 256, 256, argb);
		}
	}

	public List<CrucibleEntry> getRecipes() {
		List<CrucibleEntry> list = new ArrayList<>();
		ThaumatoriumBlockEntity tile = this.menu.thaumatorium;
		ItemStack catalyst = this.menu.tile.getItem(0);
		var player = Minecraft.getInstance().player;
		for (var entry : TVClientNetwork.RECIPES.entrySet()) {
			if (entry.getValue() instanceof CrucibleRecipe r) {
				Identifier id = entry.getKey();
				if (r.research().isEmpty() || ResearchManager.isResearchComplete(player, r.research())) {
					if (!catalyst.isEmpty() && r.catalyst().test(catalyst)) {
						list.add(new CrucibleEntry(id, r));
					} else if (tile != null && tile.hasSelectedRecipe(id)) {
						list.add(new CrucibleEntry(id, r));
					}
				}
			}
		}
		return list;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		this.blit(g, k, l, 0, 0, this.imageWidth, this.imageHeight);

		List<CrucibleEntry> recipes = getRecipes();
		if (recipes.size() != this.lastRecipeCount) {
			this.lastRecipeCount = recipes.size();
			if (this.index >= recipes.size()) {
				this.index = Math.max(0, recipes.size() - 1);
			}
		}

		if (this.index >= recipes.size()) {
			this.index = Math.max(0, recipes.size() - 1);
		}

		ThaumatoriumBlockEntity tile = this.menu.thaumatorium;

		if (!recipes.isEmpty()) {
			CrucibleEntry current = recipes.get(this.index);
			boolean isSelected = tile != null && tile.hasSelectedRecipe(current.id());

			// Cycle recipe up/down buttons
			if (recipes.size() > 1) {
				if (this.index > 0) {
					this.blit(g, k + 128, l + 16, 192, 16, 16, 8);
				} else {
					this.blit(g, k + 128, l + 16, 176, 16, 16, 8);
				}

				if (this.index < recipes.size() - 1) {
					this.blit(g, k + 128, l + 24, 192, 24, 16, 8);
				} else {
					this.blit(g, k + 128, l + 24, 176, 24, 16, 8);
				}
			}

			// Aspect scroll arrows if > 6 aspects
			Aspect[] aspects = current.recipe().aspects().getAspectsSorted();
			if (aspects.length > 6) {
				if (this.startAspect > 0) {
					this.blit(g, k + 32, l + 40, 192, 32, 8, 16);
				} else {
					this.blit(g, k + 32, l + 40, 176, 32, 8, 16);
				}

				if (this.startAspect < aspects.length - 6) {
					this.blit(g, k + 136, l + 40, 200, 32, 8, 16);
				} else {
					this.blit(g, k + 136, l + 40, 184, 32, 8, 16);
				}
			} else {
				this.startAspect = 0;
			}

			// Recipe output selection indicator / aura
			int mx = mouseX - (k + 112);
			int my = mouseY - (l + 16);
			if (isSelected || (mx >= 0 && my >= 0 && mx < 16 && my < 16)) {
				this.blit(g, k + 104, l + 8, 176, 96, 48, 48);
			}

			if (isSelected) {
				var player = Minecraft.getInstance().player;
				float alpha = 0.6F + Mth.sin((player != null ? player.tickCount : 0) / 5.0F) * 0.4F;
				this.blitColored(g, k + 88, l + 16, 176, 56, 24, 24, ARGB.color((int) (alpha * 255), 0xFFFFFF));
			}

			// Draw aspects & progress
			this.drawAspects(g, k, l, current.recipe(), isSelected);

			// Draw output item
			ItemStack out = current.recipe().result().create();
			g.item(out, k + 112, l + 16);
			g.itemDecorations(this.font, out, k + 112, l + 16);

			// Recipe capacity string
			if (tile != null && tile.maxRecipes > 1) {
				String text = tile.selectedRecipes.size() + "/" + tile.maxRecipes;
				int width = this.font.width(text);
				g.pose().pushMatrix();
				g.pose().translate(k + 136, l + 33);
				g.pose().scale(0.5F, 0.5F);
				g.text(this.font, text, -width / 2, 0, 0xFFFFFFFF, false);
				g.pose().popMatrix();
			}
		}
	}

	private void drawAspects(GuiGraphicsExtractor g, int k, int l, CrucibleRecipe recipe, boolean isSelected) {
		ThaumatoriumBlockEntity tile = this.menu.thaumatorium;
		Aspect[] aspects = recipe.aspects().getAspectsSorted();
		int count = 0;
		int pos = 0;

		// Draw progress bars if selected
		if (isSelected && tile != null) {
			for (Aspect aspect : aspects) {
				if (count >= this.startAspect) {
					// Slot frame
					this.blit(g, k + 41 + 16 * pos, l + 57, 176, 8, 14, 6);
					int req = recipe.aspects().getAmount(aspect);
					int stored = tile.essentia.getAmount(aspect);
					int fill = req > 0 ? (int) ((float) stored / req * 12.0F) : 0;
					if (fill > 12) fill = 12;
					if (fill > 0) {
						int argb = ARGB.color(255, aspect.getColor());
						this.blitColored(g, k + 42 + 16 * pos, l + 58, 176, 0, fill, 4, argb);
					}
					pos++;
				}
				if (++count >= 6 + this.startAspect) {
					break;
				}
			}
		}

		// Draw aspect icons
		count = 0;
		pos = 0;
		for (Aspect aspect : aspects) {
			if (count >= this.startAspect) {
				GuiUtil.drawTag(g, k + 40 + 16 * pos, l + 40, aspect, recipe.aspects().getAmount(aspect), 0, 771, 1.0F, false);
				pos++;
			}
			if (++count >= 6 + this.startAspect) {
				break;
			}
		}
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		super.extractTooltip(g, mouseX, mouseY);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		List<CrucibleEntry> recipes = getRecipes();
		if (!recipes.isEmpty() && this.index >= 0 && this.index < recipes.size()) {
			CrucibleEntry current = recipes.get(this.index);

			// Output item tooltip
			if (mouseX >= k + 112 && mouseX < k + 128 && mouseY >= l + 16 && mouseY < l + 32) {
				ItemStack out = current.recipe().result().create();
				var mc = Minecraft.getInstance();
				List<FormattedCharSequence> lines = out.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL)
					.stream().map(Component::getVisualOrderText).toList();
				g.setTooltipForNextFrame(lines, mouseX, mouseY);
			}

			// Aspect tooltip
			Aspect[] aspects = current.recipe().aspects().getAspectsSorted();
			int count = 0;
			int pos = 0;
			for (Aspect aspect : aspects) {
				if (count >= this.startAspect) {
					int ax = k + 40 + 16 * pos;
					int ay = l + 40;
					if (mouseX >= ax && mouseX < ax + 16 && mouseY >= ay && mouseY < ay + 16) {
						g.setTooltipForNextFrame(this.font, Component.literal(aspect.getName() + " (" + current.recipe().aspects().getAmount(aspect) + ")"), mouseX, mouseY);
						break;
					}
					pos++;
				}
				if (++count >= 6 + this.startAspect) {
					break;
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean handled = super.mouseClicked(event, doubleClick);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		double mouseX = event.x();
		double mouseY = event.y();
		List<CrucibleEntry> recipes = getRecipes();

		if (!recipes.isEmpty() && this.index >= 0 && this.index < recipes.size()) {
			// Click on output recipe slot to toggle active state
			if (mouseX >= k + 112 && mouseX < k + 128 && mouseY >= l + 16 && mouseY < l + 32) {
				ThaumatoriumBlockEntity tile = this.menu.thaumatorium;
				CrucibleEntry current = recipes.get(this.index);
				if (tile != null && (tile.selectedRecipes.size() < tile.maxRecipes || tile.hasSelectedRecipe(current.id()))) {
					if (this.minecraft != null && this.minecraft.gameMode != null) {
						this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.index);
						if (this.minecraft.player != null) {
							this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
						}
					}
					return true;
				}
			}

			// Recipe up/down arrows
			if (recipes.size() > 1) {
				if (this.index > 0 && mouseX >= k + 128 && mouseX < k + 144 && mouseY >= l + 16 && mouseY < l + 24) {
					this.index--;
					playClick();
					return true;
				}
				if (this.index < recipes.size() - 1 && mouseX >= k + 128 && mouseX < k + 144 && mouseY >= l + 24 && mouseY < l + 32) {
					this.index++;
					playClick();
					return true;
				}
			}

			// Aspect scroll arrows
			Aspect[] aspects = recipes.get(this.index).recipe().aspects().getAspectsSorted();
			if (aspects.length > 6) {
				if (this.startAspect > 0 && mouseX >= k + 32 && mouseX < k + 40 && mouseY >= l + 40 && mouseY < l + 56) {
					this.startAspect--;
					playClick();
					return true;
				}
				if (this.startAspect < aspects.length - 6 && mouseX >= k + 136 && mouseX < k + 144 && mouseY >= l + 40 && mouseY < l + 56) {
					this.startAspect++;
					playClick();
					return true;
				}
			}
		}

		return handled;
	}

	private void playClick() {
		if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.3F, 1.2F);
		}
	}
}

package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.client.render.TVRenderTypes;
import dev.neothaum.menu.ResearchTableMenu;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.research.HexUtils;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.research.ResearchNoteData;
import dev.neothaum.tiles.ResearchTableBlockEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Port of GuiResearchTable. */
public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/guiresearchtable2.png");
	private static final Identifier PARCHMENT = NeoThaum.id("textures/misc/parchment3.png");
	private static final Identifier HEX1 = NeoThaum.id("textures/gui/hex1.png");
	private static final Identifier HEX2 = NeoThaum.id("textures/gui/hex2.png");
	private static final Identifier SCRIPT = NeoThaum.id("textures/misc/script.png");
	private static final Identifier UNKNOWN = NeoThaum.id("textures/aspects/_unknown.png");
	private static final Identifier BACK = NeoThaum.id("textures/aspects/_back.png");

	private record Rune(int q, int r, long start, long decay, int rune) {
	}

	private boolean researcher1;
	private boolean researcher2;
	private boolean researchDupe;
	private long butcount2 = 0L;
	private int page = 0;
	private int lastPage = 0;
	private boolean mouseDown;
	private int buttonState;
	public Aspect select1 = null;
	public Aspect select2 = null;
	private final Map<String, Rune> runes = new HashMap<>();
	private Aspect draggedAspect;
	public ResearchNoteData note = null;
	long lastRuneCheck = 0L;
	private final Map<String, HexUtils.Hex[]> lines = new HashMap<>();
	private final List<String> checked = new ArrayList<>();
	private final List<String> highlight = new ArrayList<>();

	public ResearchTableScreen(ResearchTableMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 255, 255);
	}

	@Override
	protected void init() {
		super.init();
		var p = this.minecraft.player;
		this.researcher1 = ResearchManager.isResearchComplete(p, "RESEARCHER1");
		this.researcher2 = ResearchManager.isResearchComplete(p, "RESEARCHER2");
		this.researchDupe = ResearchManager.isResearchComplete(p, "RESEARCHDUPE");
	}

	private PlayerKnowledge knowledge() {
		return ResearchManager.knowledge(this.minecraft.player);
	}

	private AspectList bonus() {
		ResearchTableBlockEntity t = this.menu.table();
		return t == null ? new AspectList() : t.bonusAspects;
	}

	private void sound(SoundEvent s, float vol, float pitch) {
		this.minecraft.player.playSound(s, vol, pitch);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor g, int mx, int my, float a) {
		super.extractContents(g, mx, my, a);
		int gx = this.leftPos;
		int gy = this.topPos;
		if (this.note != null && this.researchDupe && this.note.complete) {
			int v7 = mx - (gx + 37);
			int v8 = my - (gy + 5);
			if (v7 >= 0 && v8 >= 0 && v7 < 24 && v8 < 24) {
				ResearchItem rr = ResearchManager.getResearch(this.note.key);
				if (rr != null) {
					g.blit(RenderPipelines.GUI_TEXTURED, TEX, gx + 100, gy + 21, 184, 224, 48, 16, 256, 256);
					AspectList al = rr.tags.copy();
					for (Aspect aspect : al.getAspects()) {
						al.add(aspect, this.note.copies);
					}
					int count = 0;
					for (Aspect aspect : al.getAspectsSorted()) {
						GuiUtil.drawTag(g, gx + 100 + 48 + count * 16, gy + 21, aspect, al.getAmount(aspect), 0, 771, 1.0F, false);
						count++;
					}
					g.text(this.font, Component.translatable("tc.research.copy"), gx + 100, gy + 12, 0xFFFFFFFF, true);
				}
			}
		}
		if (this.mouseDown) {
			int sx = gx + 10;
			int sy = gy + 40;
			if (this.buttonState == 0 && mx >= sx && mx < sx + 80 && my >= sy && my < sy + 80) {
				Aspect aspect = getClickedAspect(mx, my, gx, gy, false);
				if (aspect != null) {
					sound(TVSounds.HHOFF, 0.2F, 1.0F + this.minecraft.level.getRandom().nextFloat() * 0.1F);
					this.buttonState = 1;
					this.draggedAspect = aspect;
				} else {
					this.buttonState = 2;
				}
			} else if (this.buttonState == 1 && this.draggedAspect != null) {
				drawOrb(g, mx - 8, my - 8, this.draggedAspect.getColor());
			}
		}
		drawAspectText(g, gx + 10, gy + 40, mx, my);
		ItemStack ink = this.menu.tile.getItem(0);
		if (this.note != null && (ink.isEmpty() || ink.getDamageValue() >= ink.getMaxDamage())) {
			g.setComponentTooltipForNextFrame(this.font,
				List.of(Component.translatable("tile.researchtable.noink.0"), Component.translatable("tile.researchtable.noink.1")), gx + 117, gy + 84);
		}
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
			int mx = (int) event.x(), my = (int) event.y();
			int gx = this.leftPos, gy = this.topPos;
			if (this.buttonState == 1 && this.draggedAspect != null) {
				if (this.note != null) {
					HexUtils.Hex hp = HexUtils.Hex.fromPixel(mx - (gx + 169), my - (gy + 83), 9);
					ResearchNoteData.HexEntry he = this.note.hexEntries.get(hp.toString());
					if (he != null && he.type() == 0) {
						sound(TVSounds.HHON, 0.3F, 1.0F);
						sound(TVSounds.WRITE, 0.2F, 1.0F);
						TVNetwork.clientSender.accept(new TVNetwork.PlaceAspect(this.menu.pos, hp.q(), hp.r(), Optional.of(this.draggedAspect)));
						this.draggedAspect = null;
					}
				}
				if (this.draggedAspect != null) {
					boolean skip = false;
					int mouseX = mx - (gx + 20);
					int mouseY = my - (gy + 146);
					if (mouseX >= -16 && mouseY >= -16 && mouseX < 16 && mouseY < 16) {
						sound(TVSounds.HHOFF, 0.2F, 1.0F);
						this.select1 = this.draggedAspect;
						skip = true;
					}
					mouseX = mx - (gx + 79);
					if (!skip && mouseX >= -16 && mouseY >= -16 && mouseX < 16 && mouseY < 16) {
						sound(TVSounds.HHOFF, 0.2F, 1.0F);
						this.select2 = this.draggedAspect;
						skip = true;
					}
					if (!skip) {
						Aspect aspect = getClickedAspect(mx, my, gx, gy, false);
						if (aspect == this.draggedAspect) {
							if (this.select1 == null) {
								this.select1 = this.draggedAspect;
							} else if (this.select2 == null) {
								this.select2 = this.draggedAspect;
							}
						}
					}
				}
			}
			this.mouseDown = false;
			this.buttonState = 0;
			this.draggedAspect = null;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float a) {
		super.extractBackground(g, mx, my, a);
		int x = this.leftPos;
		int y = this.topPos;
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, 0, 0, 255, 167, 256, 256);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 40, y + 167, 0, 166, 184, 88, 256, 256);
		if (this.page < this.lastPage) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 51, y + 121, 208, 208, 24, 8, 256, 256);
		}
		if (this.page > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 27, y + 121, 184, 208, 24, 8, 256, 256);
		}
		if (this.select1 != null && this.select2 != null) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 35, y + 139, 184, 184, 32, 16, 256, 256);
			if (this.butcount2 < System.nanoTime()) {
				drawOrb(g, x + 43, y + 139);
			} else {
				g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 35, y + 139, 184, 168, 32, 16, 256, 256);
			}
		}
		if (this.researchDupe && this.note != null && this.note.complete) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 37, y + 5, 232, 200, 24, 24, 256, 256);
		}
		drawAspects(g, x + 10, y + 40);
		drawSheet(g, x, y, mx, my);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int xm, int ym) {
	}

	private void drawAspects(GuiGraphicsExtractor g, int x, int y) {
		PlayerKnowledge k = knowledge();
		AspectList aspects = k.aspects;
		AspectList bonus = bonus();
		this.lastPage = Math.max(0, (aspects.size() - 20) / 5);
		int count = 0;
		int drawn = 0;
		for (Aspect aspect : aspects.getAspectsSorted()) {
			if (++count - 1 >= this.page * 5 && drawn < 25) {
				boolean faded = aspects.getAmount(aspect) <= 0 && bonus.getAmount(aspect) <= 0;
				GuiUtil.drawTag(g, x + drawn / 5 * 16, y + drawn % 5 * 16, aspect, aspects.getAmount(aspect), bonus.getAmount(aspect), 771, faded ? 0.33F : 1.0F,
					false);
				drawn++;
			}
		}
		if (this.select1 != null && k.getAspectPool(this.select1) <= 0 && bonus.getAmount(this.select1) <= 0) {
			this.select1 = null;
		}
		if (this.select2 != null && k.getAspectPool(this.select2) <= 0 && bonus.getAmount(this.select2) <= 0) {
			this.select2 = null;
		}
		if (this.select1 != null) {
			GuiUtil.drawTag(g, x + 3, y + 99, this.select1);
		}
		if (this.select2 != null) {
			GuiUtil.drawTag(g, x + 61, y + 99, this.select2);
		}
	}

	private void drawAspectText(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
		AspectList aspects = knowledge().aspects;
		int count = 0;
		int drawn = 0;
		for (Aspect aspect : aspects.getAspectsSorted()) {
			if (++count - 1 >= this.page * 5 && drawn < 25) {
				int v7 = mx - (x + drawn / 5 * 16);
				int v8 = my - (y + drawn % 5 * 16);
				if (v7 >= 0 && v8 >= 0 && v7 < 16 && v8 < 16) {
					g.setComponentTooltipForNextFrame(this.font, List.of(Component.literal(aspect.getName()), aspect.getLocalizedDescription()), mx, my - 8);
					if (this.researcher1 && !aspect.isPrimal()) {
						GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, BACK, mx + 6, my + 6, 20, 20, 0xFFFFFFFF);
						GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, BACK, mx + 24, my + 6, 20, 20, 0xFFFFFFFF);
						GuiUtil.drawTag(g, mx + 26, my + 8, aspect.getComponents()[1]);
						GuiUtil.drawTag(g, mx + 8, my + 8, aspect.getComponents()[0]);
					}
					return;
				}
				drawn++;
			}
		}
		if (this.select1 != null && mx - (x + 3) >= 0 && my - (y + 99) >= 0 && mx - (x + 3) < 16 && my - (y + 99) < 16) {
			g.setComponentTooltipForNextFrame(this.font, List.of(Component.literal(this.select1.getName()), this.select1.getLocalizedDescription()), mx, my - 8);
			return;
		}
		if (this.select2 != null && mx - (x + 61) >= 0 && my - (y + 99) >= 0 && mx - (x + 61) < 16 && my - (y + 99) < 16) {
			g.setComponentTooltipForNextFrame(this.font, List.of(Component.literal(this.select2.getName()), this.select2.getLocalizedDescription()), mx, my - 8);
		}
	}

	private void drawSheet(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
		ItemStack notes = this.menu.tile.getItem(1);
		this.note = notes.is(TVItems.RESEARCH_NOTES) ? notes.get(TVComponents.RESEARCH_NOTE) : null;
		if (this.note == null || this.note.key == null || this.note.key.isEmpty()) {
			this.runes.clear();
			return;
		}
		PlayerKnowledge k = knowledge();
		g.blit(RenderPipelines.GUI_TEXTURED, PARCHMENT, x + 94, y + 8, 0, 0, 150, 150, 256, 256);
		long time = System.currentTimeMillis();
		var rand = this.minecraft.level.getRandom();
		if (this.lastRuneCheck < time) {
			this.lastRuneCheck = time + 250L;
			HexUtils.Hex hp = HexUtils.Hex.fromPixel(rand.nextInt(120) - 60, rand.nextInt(120) - 60, 9);
			if (!this.runes.containsKey(hp.toString()) && !this.note.hexes.containsKey(hp.toString())) {
				this.runes.put(hp.toString(), new Rune(hp.q(), hp.r(), time, this.lastRuneCheck + 15000L + rand.nextInt(10000), rand.nextInt(16)));
			}
		}
		for (Rune rune : new ArrayList<>(this.runes.values())) {
			if (rune.decay() < time) {
				this.runes.remove(rune.q() + ":" + rune.r());
				continue;
			}
			double[] pix = new HexUtils.Hex(rune.q(), rune.r()).toPixel(9);
			float progress = (float) (time - rune.start()) / (float) (rune.decay() - rune.start());
			float alpha = 0.5F;
			if (progress < 0.25F) {
				alpha = progress * 2.0F;
			} else if (progress > 0.5F) {
				alpha = 1.0F - progress;
			}
			drawRune(g, x + 169 + pix[0], y + 83 + pix[1], rune.rune(), alpha * 0.66F);
		}
		HexUtils.Hex hp = HexUtils.Hex.fromPixel(mx - (x + 169), my - (y + 83), 9);
		this.lines.clear();
		this.checked.clear();
		this.highlight.clear();
		for (HexUtils.Hex hex : this.note.hexes.values()) {
			ResearchNoteData.HexEntry e = this.note.hexEntries.get(hex.toString());
			if (e.type() == 1 && k.hasDiscoveredAspect(e.aspect())) {
				checkConnections(hex, k);
			}
		}
		for (HexUtils.Hex[] con : this.lines.values()) {
			double[] p1 = con[0].toPixel(9);
			double[] p2 = con[1].toPixel(9);
			drawLine(g, x + 169 + p1[0], y + 83 + p1[1], x + 169 + p2[0], y + 83 + p2[1]);
		}
		if (!this.note.complete) {
			for (HexUtils.Hex hex : this.note.hexes.values()) {
				double[] pix = hex.toPixel(9);
				if (this.note.hexEntries.get(hex.toString()).type() != 1) {
					if (hex.equals(hp)) {
						GuiUtil.blitFull(g, GuiUtil.GUI_ADDITIVE, HEX2, (float) (x + 169 + pix[0] - 8), (float) (y + 83 + pix[1] - 8), 16, 16, 0xFFFFFFFF);
					}
					GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, HEX1, (float) (x + 169 + pix[0] - 8), (float) (y + 83 + pix[1] - 8), 16, 16, 0x40FFFFFF);
				} else {
					drawOrb(g, x + 161 + pix[0], y + 75 + pix[1]);
				}
			}
		}
		for (HexUtils.Hex hex : this.note.hexes.values()) {
			ResearchNoteData.HexEntry e = this.note.hexEntries.get(hex.toString());
			double[] pix = hex.toPixel(9);
			if (e.aspect() != null && !k.hasDiscoveredAspect(e.aspect())) {
				GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, UNKNOWN, (float) (x + 161 + pix[0]), (float) (y + 75 + pix[1]), 16, 16, 0x80000000);
			} else if (e.type() == 1 || this.highlight.contains(hex.toString())) {
				GuiUtil.drawTag(g, x + 161 + pix[0], y + 75 + pix[1], e.aspect(), 0.0F, 0, 771, 1.0F, false);
			} else if (e.type() == 2) {
				GuiUtil.drawTag(g, x + 161 + pix[0], y + 75 + pix[1], e.aspect(), 0.0F, 0, 771, 0.66F, true);
			}
		}
	}

	private void checkConnections(HexUtils.Hex hex, PlayerKnowledge k) {
		this.checked.add(hex.toString());
		for (int a = 0; a < 6; a++) {
			HexUtils.Hex target = hex.getNeighbour(a);
			ResearchNoteData.HexEntry te = this.note.hexEntries.get(target.toString());
			if (!this.checked.contains(target.toString()) && te != null && te.type() >= 1) {
				Aspect a1 = this.note.hexEntries.get(hex.toString()).aspect();
				Aspect a2 = te.aspect();
				if (a1 != null && a2 != null && k.hasDiscoveredAspect(a1) && k.hasDiscoveredAspect(a2)
					&& (!a1.isPrimal() && (a1.getComponents()[0] == a2 || a1.getComponents()[1] == a2)
					|| !a2.isPrimal() && (a2.getComponents()[0] == a1 || a2.getComponents()[1] == a1))) {
					String k1 = hex + ":" + target;
					String k2 = target + ":" + hex;
					if (!this.lines.containsKey(k1) && !this.lines.containsKey(k2)) {
						this.lines.put(k1, new HexUtils.Hex[]{hex, target});
						this.highlight.add(target.toString());
					}
					checkConnections(target, k);
				}
			}
		}
	}

	private void drawLine(GuiGraphicsExtractor g, double x, double y, double x2, double y2) {
		int count = this.minecraft.player.tickCount;
		float alpha = 0.3F + Mth.sin((float) (count + x)) * 0.3F + 0.3F;
		float len = (float) Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
		g.pose().pushMatrix();
		g.pose().translate((float) x, (float) y);
		g.pose().rotate((float) Math.atan2(y2 - y, x2 - x));
		g.fill(0, -1, Mth.ceil(len), 1, GuiUtil.color(0x0099CC, alpha));
		g.pose().popMatrix();
	}

	private void drawRune(GuiGraphicsExtractor g, double x, double y, int rune, float alpha) {
		g.pose().pushMatrix();
		g.pose().translate((float) x, (float) y);
		if (rune < 16) {
			g.pose().rotate((float) (-Math.PI / 2));
		}
		g.blit(RenderPipelines.GUI_TEXTURED, SCRIPT, -5, -5, rune * 16, 0, 10, 10, 16, 16, 256, 16, GuiUtil.color(0, alpha));
		g.pose().popMatrix();
	}

	private void drawOrb(GuiGraphicsExtractor g, double x, double y) {
		int count = this.minecraft.player.tickCount;
		float red = 0.7F + Mth.sin((float) ((count + x) / 10.0)) * 0.15F + 0.15F;
		float green = 0.7F + Mth.sin((float) ((count + x + y) / 11.0)) * 0.15F + 0.15F;
		float blue = 0.7F + Mth.sin((float) ((count + y) / 12.0)) * 0.15F + 0.15F;
		drawOrb(g, x, y, (int) (Math.min(1, red) * 255) << 16 | (int) (Math.min(1, green) * 255) << 8 | (int) (Math.min(1, blue) * 255));
	}

	private void drawOrb(GuiGraphicsExtractor g, double x, double y, int color) {
		int part = this.minecraft.player.tickCount % 8;
		g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, (int) x, (int) y, 128 + part * 32, 128, 16, 16, 16, 16, 256, 256, 0xFF000000 | color);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean r = super.mouseClicked(event, doubleClick);
		if (event.button() != com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
			return r;
		}
		this.mouseDown = true;
		int mx = (int) event.x(), my = (int) event.y();
		int gx = this.leftPos, gy = this.topPos;
		if (this.butcount2 > System.nanoTime()) {
			return r;
		}
		int v7 = mx - (gx + 35);
		int v8 = my - (gy + 139);
		AspectList bonus = bonus();
		if (v7 >= 0 && v8 >= 0 && v7 < 32 && v8 < 16 && this.select1 != null && this.select2 != null) {
			this.butcount2 = System.nanoTime() + 200000000L;
			sound(TVSounds.CAMERACLACK, 0.4F, 1.0F);
			sound(TVSounds.HHON, 0.3F, 1.0F);
			TVNetwork.clientSender.accept(new TVNetwork.CombineAspects(this.menu.pos, this.select1, this.select2, bonus.getAmount(this.select1) > 0,
				bonus.getAmount(this.select2) > 0));
			return true;
		}
		if (this.page > 0 && mx - (gx + 27) >= 0 && my - (gy + 121) >= 0 && mx - (gx + 27) < 24 && my - (gy + 121) < 8) {
			this.page--;
			sound(TVSounds.KEY, 0.3F, 1.0F);
			return true;
		}
		if (this.page < this.lastPage && mx - (gx + 51) >= 0 && my - (gy + 121) >= 0 && mx - (gx + 51) < 24 && my - (gy + 121) < 8) {
			this.page++;
			sound(TVSounds.KEY, 0.3F, 1.0F);
			return true;
		}
		if (this.select1 != null && mx - (gx + 11) >= 0 && my - (gy + 137) >= 0 && mx - (gx + 11) < 16 && my - (gy + 137) < 16) {
			this.select1 = null;
			sound(TVSounds.HHOFF, 0.2F, 1.0F);
			return true;
		}
		if (this.select2 != null && mx - (gx + 71) >= 0 && my - (gy + 137) >= 0 && mx - (gx + 71) < 16 && my - (gy + 137) < 16) {
			this.select2 = null;
			sound(TVSounds.HHOFF, 0.2F, 1.0F);
			return true;
		}
		if (this.note != null) {
			HexUtils.Hex hp = HexUtils.Hex.fromPixel(mx - (gx + 169), my - (gy + 83), 9);
			ResearchNoteData.HexEntry he = this.note.hexEntries.get(hp.toString());
			if (he != null && he.type() == 2) {
				sound(TVSounds.HHON, 0.3F, 1.0F);
				sound(TVSounds.ERASE, 0.2F, 1.0F);
				TVNetwork.clientSender.accept(new TVNetwork.PlaceAspect(this.menu.pos, hp.q(), hp.r(), Optional.empty()));
			}
			if (this.researchDupe && this.note.complete && mx - (gx + 37) >= 0 && my - (gy + 5) >= 0 && mx - (gx + 37) < 24 && my - (gy + 5) < 24) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 5);
				sound(TVSounds.CAMERACLACK, 0.4F, 1.0F);
				return true;
			}
		}
		if (event.hasShiftDown() && this.researcher2) {
			Aspect aspect = getClickedAspect(mx, my, gx, gy, true);
			if (aspect != null && !aspect.isPrimal()) {
				AspectList aspects = knowledge().aspects;
				Aspect c0 = aspect.getComponents()[0], c1 = aspect.getComponents()[1];
				if ((aspects.getAmount(c0) > 0 || bonus.getAmount(c0) > 0) && (aspects.getAmount(c1) > 0 || bonus.getAmount(c1) > 0)) {
					this.draggedAspect = null;
					sound(TVSounds.HHON, 0.3F, 1.0F);
					TVNetwork.clientSender.accept(new TVNetwork.CombineAspects(this.menu.pos, c0, c1, bonus.getAmount(c0) > 0, bonus.getAmount(c1) > 0));
				}
			}
		}
		return r;
	}

	private Aspect getClickedAspect(int mx, int my, int gx, int gy, boolean ignoreZero) {
		AspectList aspects = knowledge().aspects;
		AspectList bonus = bonus();
		int count = 0;
		int drawn = 0;
		for (Aspect aspect : aspects.getAspectsSorted()) {
			if (++count - 1 >= this.page * 5 && drawn < 25) {
				int v7 = mx - (gx + drawn / 5 * 16 + 10);
				int v8 = my - (gy + drawn % 5 * 16 + 40);
				if ((ignoreZero || aspects.getAmount(aspect) > 0 || bonus.getAmount(aspect) > 0) && v7 >= 0 && v8 >= 0 && v7 < 16 && v8 < 16) {
					return aspect;
				}
				drawn++;
			}
		}
		return null;
	}
}

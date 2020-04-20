package com.teamwizardry.wizardry.client.gui.worktable;

import com.teamwizardry.librarianlib.core.LibrarianLib;
import com.teamwizardry.librarianlib.core.client.ClientTickHandler;
import com.teamwizardry.librarianlib.features.animator.Easing;
import com.teamwizardry.librarianlib.features.animator.animations.BasicAnimation;
import com.teamwizardry.librarianlib.features.eventbus.Event;
import com.teamwizardry.librarianlib.features.gui.EnumMouseButton;
import com.teamwizardry.librarianlib.features.gui.component.GuiComponent;
import com.teamwizardry.librarianlib.features.gui.component.GuiComponentEvents;
import com.teamwizardry.librarianlib.features.gui.components.ComponentVoid;
import com.teamwizardry.librarianlib.features.gui.mixin.DragMixin;
import com.teamwizardry.librarianlib.features.math.Vec2d;
import com.teamwizardry.librarianlib.features.math.interpolate.position.InterpBezier2D;
import com.teamwizardry.librarianlib.features.sprite.Sprite;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.spell.attribute.AttributeRegistry;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstance;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstanceModifier;
import com.teamwizardry.wizardry.api.spell.module.ModuleRegistry;
import com.teamwizardry.wizardry.api.spell.module.ModuleType;
import com.teamwizardry.wizardry.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.teamwizardry.wizardry.client.gui.worktable.WorktableGui.*;

public class TableModule extends GuiComponent {

	private static final Color effectColor = new Color(255, 102, 79);

	private boolean errored = false;

	@Nonnull
	private final WorktableGui worktable;
	@Nonnull
	private final ModuleInstance module;
	private final boolean draggable;
	private final Sprite icon;
	private final boolean benign;
	public float radius = 16, textRadius = 10;
	@Nullable
	private TableModule linksTo = null;
	private boolean enableTooltip;
	/**
	 * ALWAYS from the context of null. Never to any other component.
	 */
	private Vec2d initialPos;

	public TableModule(@Nonnull WorktableGui worktable, @Nonnull ModuleInstance module, boolean draggable, boolean benign) {
		super(0, 0, 16, 16);
		this.worktable = worktable;
		this.module = module;
		this.draggable = draggable;
		icon = new Sprite(module.getIconLocation());
		this.benign = enableTooltip = benign;

		initialPos = thisPosToOtherContext(null);

		ComponentVoid paper = worktable.paper;

		if (draggable) getTransform().setTranslateZ(30);

		if (!benign && draggable) {
			setData(UUID.class, "uuid", UUID.randomUUID());
		}

		if (!benign && !draggable)
			BUS.hook(GuiComponentEvents.MouseDownEvent.class, (event) -> {
				if (worktable.animationPlaying) return;
				if (event.getButton() == EnumMouseButton.LEFT && getMouseOver()) {
					Minecraft.getMinecraft().player.playSound(ModSounds.BUTTON_CLICK_IN, 1f, 1f);
					TableModule item = new TableModule(this.worktable, this.module, true, false);
					item.setPos(paper.otherPosToThisContext(event.component, event.getMousePos()));
					DragMixin drag = new DragMixin(item, vec2d -> vec2d);
					drag.setDragOffset(new Vec2d(6, 6));
					drag.setMouseDown(event.getButton());
					paper.add(item);

					event.cancel();
				}
			});

		if (!benign)
			BUS.hook(DragMixin.DragPickupEvent.class, (event) -> {
				if (worktable.animationPlaying) return;
				if (!getMouseOver()) return;

				if (isErrored()) {
					setErrored(false);
					deselect(this);
				}
				initialPos = event.component.thisPosToOtherContext(null);
				if (event.getButton() == EnumMouseButton.RIGHT) {
					event.component.addTag("connecting");
					Minecraft.getMinecraft().player.playSound(ModSounds.POP, 1f, 1f);
				}
			});

		if (!benign)
			BUS.hook(DragMixin.DragMoveEvent.class, (event) -> {
				if (isErrored()) {
					setErrored(false);
					deselect(this);
				}
				if (worktable.animationPlaying || event.getButton() == EnumMouseButton.RIGHT) {
					// event.getPos returns the before-moving position. Setting it back to it's place.
					// This allows the component to stay where it is while also allowing us to draw a line
					// outside of it's box
					event.setNewPos(event.getPos());
				}
			});

		if (!benign)
			BUS.hook(DragMixin.DragDropEvent.class, (event) -> {
				if (worktable.animationPlaying) return;

				if (!event.component.hasTag("placed")) event.component.addTag("placed");

				if (isErrored()) {
					setErrored(false);
					deselect(this);
				}

				Vec2d currentPos = event.component.thisPosToOtherContext(null);
				if (event.getButton() == EnumMouseButton.LEFT && initialPos.squareDist(currentPos) < 0.1) {

					if (worktable.selectedModule == this) {
						Minecraft.getMinecraft().player.playSound(ModSounds.BUTTON_CLICK_OUT, 1f, 1f);

						worktable.selectedModule = null;
						deselect(this);

					} else {
						Minecraft.getMinecraft().player.playSound(ModSounds.BUTTON_CLICK_IN, 1f, 1f);
						if (worktable.selectedModule != null) {
							unhoverOver(worktable.selectedModule);
						}

						worktable.selectedModule = this;
						select(this);
					}

					worktable.modifiers.refresh();
					event.component.removeTag("connecting");
					return;
				}

				Vec2d plateSize = paper.getSize();
				Vec2d platePos = event.component.getPos();
				boolean isInsidePaper = platePos.getX() >= 0 && platePos.getX() <= plateSize.getX() && platePos.getY() >= 0 && platePos.getY() <= plateSize.getY();

				if (!isInsidePaper) {
					if (!event.component.hasTag("connecting")) {

						for (GuiComponent paperComponent : paper.getChildren()) {
							if (paperComponent == event.component) continue;

							if (!(paperComponent instanceof TableModule)) continue;
							TableModule linkTo = (TableModule) paperComponent;

							if (linkTo.getLinksTo() == this) {
								linkTo.setLinksTo(null);
							}
						}

						if (worktable.selectedModule == this) worktable.selectedModule = null;

						Minecraft.getMinecraft().player.playSound(ModSounds.ZOOM, 1f, 1f);
						event.component.invalidate();

						if (event.component.hasTag("placed"))
							worktable.setToastMessage("", Color.GREEN);

						worktable.modifiers.refresh();
					}
					event.component.removeTag("connecting");
					worktable.paper.BUS.fire(new ModuleUpdateEvent());
					worktable.syncToServer();
					return;
				}

				if (event.component.hasTag("connecting")) {
					for (GuiComponent paperComponent : paper.getChildren()) {
						if (paperComponent == event.component) continue;
						if (!paperComponent.geometry.getMouseOverNoOcclusion()) continue;

						if (!(paperComponent instanceof TableModule)) continue;
						TableModule linkTo = (TableModule) paperComponent;
						if (!linkTo.draggable) continue;
						if (linkTo == this) continue;

						if (getLinksTo() == linkTo) {
							event.component.removeTag("connecting");
							setLinksTo(null);
							worktable.setToastMessage("", Color.GREEN);
							worktable.paper.BUS.fire(new ModuleUpdateEvent());
							worktable.syncToServer();
							return;
						} else {
							if (linkTo.getLinksTo() == this) {
								setLinksTo(null);
								linkTo.setLinksTo(null);
							} else {
								boolean linkedFrom = false;
								for (GuiComponent component : paper.getChildren()) {
									if (!(component instanceof TableModule)) continue;
									TableModule child = (TableModule) component;

									if (child.getLinksTo() == linkTo) {
										child.setLinksTo(null);
										linkedFrom = true;

										if (child.isErrored()) {
											child.setErrored(false);
											deselect(child);
										}
									}
								}
								if (linkTo.getLinksTo() != null && linkedFrom) {
									linkTo.setLinksTo(null);
								}
								setLinksTo(linkTo);

								if (linkTo.isErrored()) {
									linkTo.setErrored(false);
									deselect(linkTo);

								}
								if (isErrored()) {
									setErrored(false);
									deselect(this);
								}
							}

							Minecraft.getMinecraft().player.playSound(ModSounds.BELL_TING, 1f, 1f);
							worktable.setToastMessage("", Color.GREEN);
							worktable.paper.BUS.fire(new ModuleUpdateEvent());
							worktable.syncToServer();
						}

						event.component.removeTag("connecting");
						return;
					}
				}

				event.component.removeTag("connecting");
				worktable.paper.BUS.fire(new ModuleUpdateEvent());
				worktable.syncToServer();
			});

		if (!benign || enableTooltip)
			render.getTooltip().func((Function<GuiComponent, List<String>>) t -> {
				List<String> txt = new ArrayList<>();

				if (worktable.animationPlaying) return txt;
				if (t.hasTag("connecting")) return txt;

				txt.add(TextFormatting.GOLD + module.getReadableName());
				if (GuiScreen.isShiftKeyDown()) {
					txt.add(TextFormatting.GRAY + module.getDescription());
					if (module.getAttributeRanges().keySet().stream().anyMatch(AttributeRegistry.Attribute::hasDetailedText))
						if (GuiScreen.isCtrlKeyDown())
							module.getDetailedInfo().forEach(info -> txt.add(TextFormatting.GRAY + info));
						else txt.add(TextFormatting.GRAY + LibrarianLib.PROXY.translate("wizardry.misc.ctrl"));
				} else txt.add(TextFormatting.GRAY + LibrarianLib.PROXY.translate("wizardry.misc.sneak"));
				return txt;
			});

		if (!benign)
			BUS.hook(GuiComponentEvents.MouseInEvent.class, event -> {
				if (worktable.animationPlaying) return;
				if (isErrored() || worktable.selectedModule == this) return;

				hoverOver(this);
			});

		if (!benign)
			BUS.hook(GuiComponentEvents.MouseOutEvent.class, event -> {
				if (worktable.animationPlaying) return;
				if (isErrored() || worktable.selectedModule == this) return;

				unhoverOver(this);

			});
	}

	public static void select(TableModule module) {
		Vec2d toSize = new Vec2d(24, 24);
		BasicAnimation<TableModule> animSize = new BasicAnimation<>(module, "size");
		animSize.setDuration(5);
		animSize.setEasing(Easing.easeOutCubic);
		animSize.setTo(toSize);
		module.add(animSize);

		BasicAnimation<TableModule> animPos = new BasicAnimation<>(module, "pos");
		animPos.setDuration(5);
		animPos.setEasing(Easing.easeOutCubic);
		animPos.setTo(module.getPos().add((module.getSize().sub(toSize)).mul(0.5f)));
		module.add(animPos);

		BasicAnimation<TableModule> animRadius = new BasicAnimation<>(module, "radius");
		animRadius.setDuration(20);
		animRadius.setEasing(Easing.easeOutCubic);
		animRadius.setTo(24);
		module.add(animRadius);

		BasicAnimation<TableModule> animText = new BasicAnimation<>(module, "textRadius");
		animText.setDuration(40);
		animText.setEasing(Easing.easeOutCubic);
		animText.setTo(40);
		module.add(animText);
	}

	public static void drawWire(Vec2d start, Vec2d end, Color primary, Color secondary) {
		drawWire(start, end, primary, secondary, 1f);
	}

	public static void drawWire(Vec2d start, Vec2d end, Color primary, Color secondary, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableCull();

		GlStateManager.translate(0, 0, -10);
		STREAK.bind();
		InterpBezier2D bezier = new InterpBezier2D(start, end);
		List<Vec2d> list = bezier.list(50);

		float p = 0;
		for (int i = 0; i < list.size() - 1; i++) {
			float x = (float) (start.length() + ClientTickHandler.getTicks() + ClientTickHandler.getPartialTicks()) / 30f;
			if (i == (int) ((x - Math.floor(x)) * 50f)) {
				p = i / (list.size() - 1.0f);
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vb = tessellator.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		Vec2d lastPoint = null;

		for (int i = 0; i < list.size() - 1; i++) {
			Vec2d point = list.get(i);
			if (lastPoint == null) {
				lastPoint = point;
				continue;
			}

			float dist = (i / (list.size() - 1.0f));

			float wire;
			if (dist < p) {
				float z = Math.abs(dist - p);
				wire = 256.0f / 27.0f * (z * z * z - z * z * z * z);
			} else {
				float z = Math.abs(dist - (p + 1f));
				wire = 256.0f / 27.0f * (z * z * z - z * z * z * z);
			}

//			float r = lerp(primary.getRed(), secondary.getRed(), wire) / 255f;
//			float g = lerp(primary.getGreen(), secondary.getGreen(), wire) / 255f;
//			float b = lerp(primary.getBlue(), secondary.getBlue(), wire) / 255f;

			float[] primaryHsv = Color.RGBtoHSB(primary.getRed(), primary.getBlue(), primary.getGreen(), null);
			float[] secondaryHsv = Color.RGBtoHSB(secondary.getRed(), secondary.getBlue(), secondary.getGreen(), null);

			int rgb = Color.HSBtoRGB(lerp(primaryHsv[0], secondaryHsv[0], wire), primaryHsv[1], primaryHsv[2]);

			float r = ((rgb >> 16) & 0xFF) / 255f;
			float g = ((rgb >> 8) & 0xFF) / 255f;
			float b = (rgb & 0xFF) / 255f;

			Vec2d normal = point.sub(lastPoint);
			Vec2d perp = new Vec2d(-normal.getYf(), normal.getXf()).mul(1.0f - 2.0f * Math.abs(dist - 0.5f) + 0.3f);
			Vec2d point1 = lastPoint.sub(normal.mul(0.5)).add(perp);
			Vec2d point2 = point.add(normal.mul(0.5)).add(perp);
			Vec2d point3 = point.add(normal.mul(0.5)).sub(perp);
			Vec2d point4 = lastPoint.sub(normal.mul(0.5)).sub(perp);

			vb.pos(point1.getXf(), point1.getYf(), 0).tex(0, 0).color(r, g, b, alpha).endVertex();
			vb.pos(point2.getXf(), point2.getYf(), 0).tex(0, 1).color(r, g, b, alpha).endVertex();
			vb.pos(point3.getXf(), point3.getYf(), 0).tex(1, 0).color(r, g, b, alpha).endVertex();
			vb.pos(point4.getXf(), point4.getYf(), 0).tex(1, 1).color(r, g, b, alpha).endVertex();

			lastPoint = point;
		}

		tessellator.draw();

		GlStateManager.translate(0, 0, 10);
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

	private static float lerp(float a, float b, float f) {
		return a + f * (b - a);
	}

	public static Color getColorForModule(ModuleType type) {
		switch (type) {
			case EVENT:
				return Color.PINK;
			case SHAPE:
				return Color.CYAN;
			case EFFECT:
				return effectColor;
			case MODIFIER:
				return Color.GREEN;
			default:
				return Color.BLACK;
		}
	}

	public static void deselect(TableModule module) {
		Vec2d toSize = new Vec2d(20, 20);
		BasicAnimation<TableModule> animSize = new BasicAnimation<>(module, "size");
		animSize.setDuration(5);
		animSize.setEasing(Easing.easeOutCubic);
		animSize.setTo(toSize);
		module.add(animSize);

		BasicAnimation<TableModule> animPos = new BasicAnimation<>(module, "pos");
		animPos.setDuration(5);
		animPos.setEasing(Easing.easeOutCubic);
		animPos.setTo(module.getPos().add((module.getSize().sub(toSize)).mul(0.5f)));
		module.add(animPos);

		BasicAnimation<TableModule> animRadius = new BasicAnimation<>(module, "radius");
		animRadius.setDuration(20);
		animRadius.setEasing(Easing.easeOutCubic);
		animRadius.setTo(16);
		module.add(animRadius);

		BasicAnimation<TableModule> animText = new BasicAnimation<>(module, "textRadius");
		animText.setDuration(40);
		animText.setEasing(Easing.easeOutCubic);
		animText.setTo(30);
		module.add(animText);
	}

	@Override
	public void drawComponent(@NotNull Vec2d mousePos, float partialTicks) {
		super.drawComponent(mousePos, partialTicks);
		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();

		Sprite plate;
		plate = isErrored() ? PLATE_HIGHLIGHTED_ERROR : (worktable.selectedModule == this ? PLATE_HIGHLIGHTED : PLATE);
		Vec2d pos = Vec2d.ZERO;

		GlStateManager.translate(0, 0, -20);
		if (hasTag("connecting")) {
			drawWire(pos.add(getSize().getX() / 2.0, getSize().getY() / 2.0), mousePos, getColorForModule(module.getModuleType()), Color.WHITE);
		}
		if (linksTo != null) {
			Vec2d posTo = linksTo.thisPosToOtherContext(this);
			drawWire(pos.add(getSize().getX() / 2.0, getSize().getY() / 2.0), posTo.add(getSize().getX() / 2.0, getSize().getY() / 2.0), getColorForModule(module.getModuleType()), getColorForModule(linksTo.getModule().getModuleType()));
		}

		GlStateManager.translate(0, 0, 20);

		if (isErrored() || worktable.selectedModule == this || (!benign && !worktable.animationPlaying && getMouseOver() && !hasTag("connecting"))) {
			GlStateManager.translate(0, 0, 80);
		}

		plate.bind();
		plate.draw(0, 0, 0, getSize().getXf(), getSize().getYf());

		float shrink = 4;

		icon.bind();
		icon.draw(0, shrink / 2.0f, shrink / 2.0f, getSize().getXf() - shrink, getSize().getYf() - shrink);

		HashMap<ModuleInstanceModifier, Integer> modifiers = new HashMap<>();
		List<ModuleInstanceModifier> modifierList = new ArrayList<>();
		for (ModuleInstance module : ModuleRegistry.INSTANCE.getModules(ModuleType.MODIFIER)) {
			if (!(module instanceof ModuleInstanceModifier)) continue;
			if (!hasData(Integer.class, module.getNBTKey())) continue;

			modifiers.put((ModuleInstanceModifier) module, getData(Integer.class, module.getNBTKey()));
			modifierList.add((ModuleInstanceModifier) module);
		}

		int count = modifierList.size();
		for (int i = 0; i < count; i++) {

			ModuleInstanceModifier modifier = modifierList.get(i);

			Vec2d modSize = getSize().mul(0.75f);

			float angle = (float) (i * Math.PI * 2.0 / count);

			// RENDER PLATE
			{
				float x = (getSize().getXf() / 2f - modSize.getXf() / 2f) + MathHelper.cos(angle) * radius;
				float y = (getSize().getYf() / 2f - modSize.getYf() / 2f) + MathHelper.sin(angle) * radius;

				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y, -10);

				plate.bind();
				plate.draw(0, 0, 0, modSize.getXf(), modSize.getYf());

				float modShrink = 4;

				Sprite modICon = new Sprite(new ResourceLocation(Wizardry.MODID, "textures/gui/worktable/icons/" + modifier.getNBTKey() + ".png"));
				modICon.bind();
				modICon.draw(0, modShrink / 2.0f, modShrink / 2.0f, modSize.getXf() - modShrink, modSize.getYf() - modShrink);

				GlStateManager.translate(-x, -y, 10);
				GlStateManager.popMatrix();
			}

			// RENDER TEXT
			{
				FontRenderer font = Minecraft.getMinecraft().fontRenderer;
				String txt = "x" + modifiers.get(modifier);
				float txtWidth = font.getStringWidth(txt);
				float txtHeight = font.FONT_HEIGHT;

				float x = (getSize().getXf() / 2f - txtWidth / 2f) + MathHelper.cos(angle) * textRadius;
				float y = (getSize().getYf() / 2f - txtHeight / 2f) + MathHelper.sin(angle) * textRadius;

				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y, -15);

				font.drawString(txt, 0, 0, 0x000000);
				GlStateManager.color(1f, 1f, 1f, 1f);

				GlStateManager.translate(-x, -y, 15);
				GlStateManager.popMatrix();
			}
		}

		if (isErrored() || worktable.selectedModule == this || (!benign && !worktable.animationPlaying && getMouseOver() && !hasTag("connecting"))) {
			GlStateManager.translate(0, 0, -80);
		}
	}

	public boolean isErrored() {
		return errored;
	}

	@Nullable
	public TableModule getLinksTo() {
		return linksTo;
	}

	public void setLinksTo(@Nullable TableModule linksTo) {
		this.linksTo = linksTo;
	}

	@Nonnull
	public WorktableGui getWorktable() {
		return worktable;
	}

	public boolean isDraggable() {
		return draggable;
	}

	@Nonnull
	public ModuleInstance getModule() {
		return module;
	}

	public Sprite getIcon() {
		return icon;
	}

	public boolean isEnableTooltip() {
		return enableTooltip;
	}

	public void setEnableTooltip(boolean enableTooltip) {
		this.enableTooltip = enableTooltip;
	}

	public static void unhoverOver(TableModule module) {
		Vec2d toSize = new Vec2d(16, 16);
		BasicAnimation<TableModule> animSize = new BasicAnimation<>(module, "size");
		animSize.setDuration(5);
		animSize.setEasing(Easing.easeOutCubic);
		animSize.setTo(toSize);
		module.add(animSize);

		BasicAnimation<TableModule> animPos = new BasicAnimation<>(module, "pos");
		animPos.setDuration(5);
		animPos.setEasing(Easing.easeOutCubic);
		animPos.setTo(module.getPos().add((module.getSize().sub(toSize)).mul(0.5f)));
		module.add(animPos);

		BasicAnimation<TableModule> animRadius = new BasicAnimation<>(module, "radius");
		animRadius.setDuration(20);
		animRadius.setEasing(Easing.easeOutCubic);
		animRadius.setTo(10);
		module.add(animRadius);

		BasicAnimation<TableModule> animText = new BasicAnimation<>(module, "textRadius");
		animText.setDuration(40);
		animText.setEasing(Easing.easeOutCubic);
		animText.setTo(0);
		module.add(animText);
	}

	public static void hoverOver(TableModule module) {
		Vec2d toSize = new Vec2d(20, 20);
		BasicAnimation<TableModule> animSize = new BasicAnimation<>(module, "size");
		animSize.setDuration(5);
		animSize.setEasing(Easing.easeOutCubic);
		animSize.setTo(toSize);
		module.add(animSize);

		BasicAnimation<TableModule> animPos = new BasicAnimation<>(module, "pos");
		animPos.setDuration(5);
		animPos.setEasing(Easing.easeOutCubic);
		animPos.setTo(module.getPos().add((module.getSize().sub(toSize)).mul(0.5f)));
		module.add(animPos);

		BasicAnimation<TableModule> animRadius = new BasicAnimation<>(module, "radius");
		animRadius.setDuration(20);
		animRadius.setEasing(Easing.easeOutCubic);
		animRadius.setTo(16);
		module.add(animRadius);

		BasicAnimation<TableModule> animText = new BasicAnimation<>(module, "textRadius");
		animText.setDuration(40);
		animText.setEasing(Easing.easeOutCubic);
		animText.setTo(30);
		module.add(animText);
	}

	public void setErrored(boolean errored) {
		this.errored = errored;
	}

	public static class ModuleUpdateEvent extends Event {
	}
}

package XiGyoku.furyborn.client.screen;

import XiGyoku.furyborn.blockentity.HaloProjectorBlockEntity;
import XiGyoku.furyborn.client.util.HaloProjectorRenderer;
import XiGyoku.furyborn.network.FuryBornNetwork;
import XiGyoku.furyborn.network.PacketUpdateHaloProjector;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class HaloProjectorScreen extends Screen {

    private final HaloProjectorBlockEntity blockEntity;
    private float offsetX, offsetY, offsetZ;
    private float rotX, rotY, rotZ;
    private float scale, sunScale;
    private boolean individualRotation;
    private int orbitCount;
    private float orbitSpacing;
    private boolean unifyPlanetColor;
    private int unifiedPlanetColor;
    private float rotationSpeedMultiplier;
    private float hazeScale;
    private int redstoneMode;

    private double scrollAmount = 0;
    private double maxScroll = 0;
    private boolean isDraggingScrollbar = false;
    private final List<ScrollableEntry> scrollableWidgets = new ArrayList<>();
    private final List<AbstractWidget> fixedWidgets = new ArrayList<>();

    private static class ScrollableEntry {
        AbstractWidget widget;
        int initialY;
        ScrollableEntry(AbstractWidget w, int y) {
            this.widget = w;
            this.initialY = y;
        }
    }

    public HaloProjectorScreen(HaloProjectorBlockEntity blockEntity) {
        super(Component.literal("Halo Projector"));
        this.blockEntity = blockEntity;
        this.offsetX = blockEntity.offsetX;
        this.offsetY = blockEntity.offsetY;
        this.offsetZ = blockEntity.offsetZ;
        this.rotX = blockEntity.rotX;
        this.rotY = blockEntity.rotY;
        this.rotZ = blockEntity.rotZ;
        this.scale = blockEntity.scale;
        this.sunScale = blockEntity.sunScale;
        this.individualRotation = blockEntity.individualRotation;
        this.orbitCount = blockEntity.orbitCount;
        this.orbitSpacing = blockEntity.orbitSpacing;
        this.unifyPlanetColor = blockEntity.unifyPlanetColor;
        this.unifiedPlanetColor = blockEntity.unifiedPlanetColor;
        this.rotationSpeedMultiplier = blockEntity.rotationSpeedMultiplier;
        this.hazeScale = blockEntity.hazeScale;
        this.redstoneMode = blockEntity.redstoneMode;
    }

    @Override
    protected void init() {
        super.init();
        this.scrollableWidgets.clear();
        this.fixedWidgets.clear();
        this.clearWidgets();

        int x = 20;
        int currentY = 0;

        addConfigStep(x, currentY, "Offset X", -100.0f, 100.0f, this.offsetX, 0.0f, v -> offsetX = v); currentY += 24;
        addConfigStep(x, currentY, "Offset Y", -100.0f, 100.0f, this.offsetY, 1.0f, v -> offsetY = v); currentY += 24;
        addConfigStep(x, currentY, "Offset Z", -100.0f, 100.0f, this.offsetZ, 0.0f, v -> offsetZ = v); currentY += 24;
        addConfigStep(x, currentY, "Rot X", -360.0f, 360.0f, this.rotX, 180.0f, v -> rotX = v); currentY += 24;
        addConfigStep(x, currentY, "Rot Y", -360.0f, 360.0f, this.rotY, 0.0f, v -> rotY = v); currentY += 24;
        addConfigStep(x, currentY, "Rot Z", -360.0f, 360.0f, this.rotZ, 0.0f, v -> rotZ = v); currentY += 24;
        addConfigStep(x, currentY, "Scale", 0.1f, 10.0f, this.scale, 1.0f, v -> scale = v); currentY += 24;
        addConfigStep(x, currentY, "Sun Scale", 0.1f, 10.0f, this.sunScale, 0.2f, v -> sunScale = v); currentY += 24;
        addConfigStep(x, currentY, "Haze Scale", 0.0f, 10.0f, this.hazeScale, 0.1f, v -> hazeScale = v); currentY += 24;
        addConfigStep(x, currentY, "Orbit Count", 1.0f, 20.0f, (float)this.orbitCount, 5.0f, v -> orbitCount = v.intValue()); currentY += 24;
        addConfigStep(x, currentY, "Orbit Spacing", 0.0f, 5.0f, this.orbitSpacing, 0.5f, v -> orbitSpacing = v); currentY += 24;
        addConfigStep(x, currentY, "Rot Speed", 0.0f, 10.0f, this.rotationSpeedMultiplier, 0.5f, v -> rotationSpeedMultiplier = v); currentY += 24;
        addConfigStep(x, currentY, "Color R", 0.0f, 255.0f, (float)((this.unifiedPlanetColor >> 16) & 0xFF), 255.0f, v -> unifiedPlanetColor = (v.intValue() << 16) | (unifiedPlanetColor & 0x00FFFF)); currentY += 24;
        addConfigStep(x, currentY, "Color G", 0.0f, 255.0f, (float)((this.unifiedPlanetColor >> 8) & 0xFF), 255.0f, v -> unifiedPlanetColor = (unifiedPlanetColor & 0xFF00FF) | (v.intValue() << 8)); currentY += 24;
        addConfigStep(x, currentY, "Color B", 0.0f, 255.0f, (float)(this.unifiedPlanetColor & 0xFF), 255.0f, v -> unifiedPlanetColor = (unifiedPlanetColor & 0xFFFF00) | v.intValue()); currentY += 24;

        addScrollable(CycleButton.booleanBuilder(Component.literal("Yes"), Component.literal("No"))
                .withInitialValue(this.individualRotation)
                .create(x, 0, 150, 20, Component.literal("Indiv. Rot"), (b, v) -> this.individualRotation = v), currentY);
        currentY += 24;

        addScrollable(CycleButton.booleanBuilder(Component.literal("Yes"), Component.literal("No"))
                .withInitialValue(this.unifyPlanetColor)
                .create(x, 0, 150, 20, Component.literal("Unify Color"), (b, v) -> this.unifyPlanetColor = v), currentY);
        currentY += 24;

        this.maxScroll = Math.max(0, currentY - (this.height - 40));

        int rightX = this.width - 170;
        String[] redstoneModes = {"Always On", "RS High", "RS Low"};

        addFixed(Button.builder(Component.literal(redstoneModes[this.redstoneMode]), button -> {
            this.redstoneMode = (this.redstoneMode + 1) % 3;
            button.setMessage(Component.literal(redstoneModes[this.redstoneMode]));
        }).bounds(rightX, this.height - 50, 150, 20).build());

        addFixed(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(rightX, this.height - 26, 150, 20).build());

        updateWidgetPositions();
    }

    private void addConfigStep(int x, int y, String label, float min, float max, float current, float defaultValue, java.util.function.Consumer<Float> applier) {
        CustomSlider slider = new CustomSlider(x, 0, 110, label, min, max, current) {
            @Override
            protected void applyValue() {
                if (this.isFocused() || this.isHovered()) {
                    applier.accept((float)getValue());
                }
            }
        };

        EditBox editBox = new EditBox(this.font, x + 115, 0, 40, 20, Component.empty());
        editBox.setValue(String.format("%.2f", current));
        editBox.setResponder(s -> {
            if (!editBox.isFocused()) return;
            try {
                float val = s.isEmpty() ? defaultValue : Float.parseFloat(s);
                float clamped = Mth.clamp(val, min, max);
                slider.setRawValueInternal(clamped);
                applier.accept(clamped);
            } catch (NumberFormatException ignored) {}
        });

        slider.setResponderBox(editBox);
        addScrollable(slider, y);
        addScrollable(editBox, y);
    }

    private void addScrollable(AbstractWidget widget, int y) {
        this.scrollableWidgets.add(new ScrollableEntry(widget, y));
        this.addRenderableWidget(widget);
    }

    private void addFixed(AbstractWidget widget) {
        this.fixedWidgets.add(widget);
        this.addRenderableWidget(widget);
    }

    private void updateWidgetPositions() {
        for (ScrollableEntry entry : scrollableWidgets) {
            int newY = entry.initialY - (int) this.scrollAmount + 20;
            entry.widget.setY(newY);
            entry.widget.visible = (newY >= 10 && newY <= this.height - 30);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scrollAmount = Mth.clamp(this.scrollAmount - delta * 20.0, 0, maxScroll);
        updateWidgetPositions();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= 180 && mouseX <= 186 && mouseY >= 20 && mouseY <= this.height - 20) {
            this.isDraggingScrollbar = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDraggingScrollbar && maxScroll > 0) {
            double barHeight = this.height - 40;
            this.scrollAmount = Mth.clamp(this.scrollAmount + (dragY / barHeight) * maxScroll, 0, maxScroll);
            updateWidgetPositions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.fill(15, 15, 190, this.height - 15, 0x88000000);

        int barX = 180, barY = 20, barH = this.height - 40;
        guiGraphics.fill(barX, barY, barX + 6, barY + barH, 0xFF000000);
        if (maxScroll > 0) {
            int handleH = Math.max(20, (int) (barH * barH / (maxScroll + barH)));
            int handleY = barY + (int) ((barH - handleH) * (scrollAmount / maxScroll));
            guiGraphics.fill(barX, handleY, barX + 6, handleY + handleH, 0xFF888888);
        }

        guiGraphics.enableScissor(0, 20, 190, this.height - 20);
        for (ScrollableEntry entry : scrollableWidgets) {
            entry.widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        guiGraphics.disableScissor();

        for (AbstractWidget w : fixedWidgets) {
            w.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.pose().pushPose();
        RenderSystem.disableCull();
        guiGraphics.pose().translate(190 + (this.width - 190) / 2, this.height / 2 - 20, 200);
        float previewScale = 25.0f * this.scale;
        guiGraphics.pose().scale(previewScale, -previewScale, previewScale);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(this.rotZ));
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(this.rotY));
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(this.rotX));

        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        HaloProjectorRenderer.renderHalo(guiGraphics.pose(), bufferSource, 15728880,
                this.sunScale, this.hazeScale, this.orbitCount, this.orbitSpacing,
                this.rotationSpeedMultiplier, this.individualRotation, this.unifyPlanetColor, this.unifiedPlanetColor, (System.currentTimeMillis() % 24000L) / 50.0F);
        bufferSource.endBatch();
        RenderSystem.enableCull();
        guiGraphics.pose().popPose();
    }

    @Override
    public void onClose() {
        FuryBornNetwork.CHANNEL.sendToServer(new PacketUpdateHaloProjector(
                this.blockEntity.getBlockPos(), offsetX, offsetY, offsetZ,
                rotX, rotY, rotZ, scale, sunScale, individualRotation,
                orbitCount, orbitSpacing, unifyPlanetColor, unifiedPlanetColor,
                rotationSpeedMultiplier, hazeScale, redstoneMode
        ));
        super.onClose();
    }

    private abstract static class CustomSlider extends AbstractSliderButton {
        private final float min, max;
        private final String prefix;
        private EditBox responderBox;

        public CustomSlider(int x, int y, int width, String prefix, float min, float max, float current) {
            super(x, y, width, 20, Component.empty(), (current - min) / (max - min));
            this.prefix = prefix; this.min = min; this.max = max;
            updateMessage();
        }

        public void setResponderBox(EditBox box) { this.responderBox = box; }

        public void setRawValueInternal(float val) {
            this.value = (Mth.clamp(val, min, max) - min) / (max - min);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(this.prefix));
            if (responderBox != null && !responderBox.isFocused()) {
                responderBox.setValue(String.format("%.2f", getValue()));
            }
        }

        public double getValue() { return this.min + (this.max - this.min) * this.value; }

        @Override
        public void onClick(double mouseX, double mouseY) {
            super.onClick(mouseX, mouseY);
            if (responderBox != null) responderBox.setValue(String.format("%.2f", getValue()));
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            super.onDrag(mouseX, mouseY, dragX, dragY);
            if (responderBox != null) responderBox.setValue(String.format("%.2f", getValue()));
        }

        @Override protected abstract void applyValue();
    }
}
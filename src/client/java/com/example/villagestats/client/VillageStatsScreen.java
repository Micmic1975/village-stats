package com.example.villagestats.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class VillageStatsScreen extends Screen {

    public VillageStatsScreen() {
        super(Component.translatable("screen.village_stats.title"));
    }

    @Override
    protected void init() {
        super.init();

        int panelX = this.width / 2 - 170;
        int panelY = this.height / 2 - 110;
        int panelWidth = 340;
        int panelHeight = 220;

        int buttonWidth = 90;
        int buttonHeight = 20;
        int buttonX = panelX + (panelWidth - buttonWidth) / 2;
        int buttonY = panelY + panelHeight - 28;

        this.addRenderableWidget(
                Button.builder(
                        Component.translatable("screen.village_stats.button.close"),
                        button -> this.onClose()
                ).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int panelX = this.width / 2 - 170;
        int panelY = this.height / 2 - 110;
        int panelWidth = 340;
        int panelHeight = 220;

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC000000);

        String titleText = this.title.getString();
        int titleX = panelX + (panelWidth - this.font.width(titleText)) / 2;
        graphics.text(this.font, titleText, titleX, panelY + 10, 0xFFFFFFFF, true);

        if (!ClientVillageStatsState.villageFound) {
            String notFoundText = tr("screen.village_stats.message.not_found");
            int textX = panelX + (panelWidth - this.font.width(notFoundText)) / 2;
            int textY = panelY + panelHeight / 2 - 16;

            graphics.text(this.font, notFoundText, textX, textY, 0xFFFFFFFF, true);
            return;
        }

        int leftX = panelX + 10;
        int middleX = panelX + 120;
        int rightX = panelX + 230;
        int startY = panelY + 35;
        int lineHeight = 12;

        drawVillagersBlock(graphics, leftX, startY, lineHeight);
        drawWorkstationsBlock(graphics, middleX, startY, lineHeight);
        drawBedsBlock(graphics, rightX, startY, lineHeight);
    }

    private void drawVillagersBlock(GuiGraphicsExtractor graphics, int x, int y, int lineHeight) {
        graphics.text(
                this.font,
                tr("screen.village_stats.section.villagers"),
                x,
                y,
                0xFFFFFFAA,
                true
        );
        y += lineHeight;

        graphics.text(
                this.font,
                tr("screen.village_stats.label.total") + ": " + ClientVillageStatsState.villagers,
                x,
                y,
                0xFFFFFFFF,
                false
        );
        y += lineHeight;

        graphics.text(
                this.font,
                tr("screen.village_stats.label.professions") + ":",
                x,
                y,
                0xFFFFFFFF,
                false
        );
        y += lineHeight;

        for (Map.Entry<String, Integer> entry : ClientVillageStatsState.professions.entrySet()) {
            String professionName = localizeProfessionId(entry.getKey());

            graphics.text(
                    this.font,
                    professionName + ": " + entry.getValue(),
                    x,
                    y,
                    0xFFFFFFFF,
                    false
            );
            y += lineHeight;
        }
    }

    private void drawWorkstationsBlock(GuiGraphicsExtractor graphics, int x, int y, int lineHeight) {
        graphics.text(
                this.font,
                tr("screen.village_stats.section.job_sites"),
                x,
                y,
                0xFFFFFFAA,
                true
        );
        y += lineHeight;

        if (ClientVillageStatsState.jobSites.isEmpty()) {
            graphics.text(
                    this.font,
                    tr("screen.village_stats.label.none"),
                    x,
                    y,
                    0xFFFFFFFF,
                    false
            );
            return;
        }

        for (Map.Entry<String, Integer> entry : ClientVillageStatsState.jobSites.entrySet()) {
            String poiName = localizeJobSiteId(entry.getKey());

            graphics.text(
                    this.font,
                    poiName + ": " + entry.getValue(),
                    x,
                    y,
                    0xFFFFFFFF,
                    false
            );
            y += lineHeight;
        }
    }

    private void drawBedsBlock(GuiGraphicsExtractor graphics, int x, int y, int lineHeight) {
        graphics.text(
                this.font,
                tr("screen.village_stats.section.beds"),
                x,
                y,
                0xFFFFFFAA,
                true
        );
        y += lineHeight;

        graphics.text(
                this.font,
                tr("screen.village_stats.label.total") + ": " + ClientVillageStatsState.beds,
                x,
                y,
                0xFFFFFFFF,
                false
        );
        y += lineHeight;

        graphics.text(
                this.font,
                tr("screen.village_stats.label.free") + ": " + ClientVillageStatsState.freeBeds,
                x,
                y,
                0xFFFFFFFF,
                false
        );
    }

    private String localizeProfessionId(String professionId) {
        Identifier id = parseId(professionId);
        if (id == null) {
            return tr("screen.village_stats.label.unknown");
        }

        if ("minecraft".equals(id.getNamespace())) {
            return Component.translatable("entity.minecraft.villager." + id.getPath()).getString();
        }

        return professionId;
    }

    private String localizeJobSiteId(String jobSiteId) {
        Identifier id = parseId(jobSiteId);
        if (id == null) {
            return tr("screen.village_stats.label.unknown");
        }

        if ("minecraft".equals(id.getNamespace())) {
            return Component.translatable("block." + id.getNamespace() + "." + id.getPath()).getString();
        }

        return jobSiteId;
    }

    private Identifier parseId(String raw) {
        try {
            return Identifier.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private String tr(String key) {
        return Component.translatable(key).getString();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
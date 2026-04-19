package com.example.villagestats.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class VillageStatsScreen extends Screen {

    private static final Map<String, String> PET_ANIMALS = createPetAnimals();

    public VillageStatsScreen() {
        super(Component.translatable("screen.village_stats.title"));
    }

    @Override
    protected void init() {
        super.init();

        int panelX = this.width / 2 - 230;
        int panelY = this.height / 2 - 125;
        int panelWidth = 460;
        int panelHeight = 250;

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

        int panelX = this.width / 2 - 230;
        int panelY = this.height / 2 - 125;
        int panelWidth = 460;
        int panelHeight = 250;

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

        int column1X = panelX + 10;
        int column2X = panelX + 120;
        int column3X = panelX + 230;
        int column4X = panelX + 340;
        int startY = panelY + 35;
        int lineHeight = 12;

        drawVillagersBlock(graphics, column1X, startY, lineHeight);
        drawWorkstationsBlock(graphics, column2X, startY, lineHeight);
        drawBedsBlock(graphics, column3X, startY, lineHeight);
        drawAnimalsBlock(graphics, column4X, startY, lineHeight);
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

        for (Map.Entry<String, Integer> entry : new TreeMap<>(ClientVillageStatsState.professions).entrySet()) {
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

        for (Map.Entry<String, Integer> entry : new TreeMap<>(ClientVillageStatsState.jobSites).entrySet()) {
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

    private void drawAnimalsBlock(GuiGraphicsExtractor graphics, int x, int y, int lineHeight) {
        graphics.text(
                this.font,
                tr("screen.village_stats.section.animals"),
                x,
                y,
                0xFFFFFFAA,
                true
        );
        y += lineHeight;

        if (ClientVillageStatsState.animals.isEmpty()) {
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

        Map<String, Integer> pets = new LinkedHashMap<>();
        Map<String, Integer> others = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : new TreeMap<>(ClientVillageStatsState.animals).entrySet()) {
            if (PET_ANIMALS.containsKey(entry.getKey())) {
                pets.put(entry.getKey(), entry.getValue());
            } else {
                others.put(entry.getKey(), entry.getValue());
            }
        }

        if (!pets.isEmpty()) {
            graphics.text(
                    this.font,
                    tr("screen.village_stats.section.pets"),
                    x,
                    y,
                    0xFFAAFFAA,
                    false
            );
            y += lineHeight;

            for (Map.Entry<String, Integer> entry : pets.entrySet()) {
                String animalName = localizeAnimalId(entry.getKey());

                graphics.text(
                        this.font,
                        animalName + ": " + entry.getValue(),
                        x,
                        y,
                        0xFFFFFFFF,
                        false
                );
                y += lineHeight;
            }
        }

        if (!pets.isEmpty() && !others.isEmpty()) {
            y += 4;
        }

        if (!others.isEmpty()) {
            graphics.text(
                    this.font,
                    tr("screen.village_stats.section.other_animals"),
                    x,
                    y,
                    0xFFFFFFAA,
                    false
            );
            y += lineHeight;

            for (Map.Entry<String, Integer> entry : others.entrySet()) {
                String animalName = localizeAnimalId(entry.getKey());

                graphics.text(
                        this.font,
                        animalName + ": " + entry.getValue(),
                        x,
                        y,
                        0xFFFFFFFF,
                        false
                );
                y += lineHeight;
            }
        }
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

    private String localizeAnimalId(String animalId) {
        Identifier id = parseId(animalId);
        if (id == null) {
            return tr("screen.village_stats.label.unknown");
        }

        if ("minecraft".equals(id.getNamespace())) {
            return Component.translatable("entity.minecraft." + id.getPath()).getString();
        }

        return animalId;
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

    private static Map<String, String> createPetAnimals() {
        Map<String, String> pets = new LinkedHashMap<>();
        pets.put("minecraft:cat", "cat");
        pets.put("minecraft:wolf", "wolf");
        pets.put("minecraft:parrot", "parrot");
        pets.put("minecraft:horse", "horse");
        pets.put("minecraft:donkey", "donkey");
        pets.put("minecraft:mule", "mule");
        pets.put("minecraft:llama", "llama");
        pets.put("minecraft:trader_llama", "trader_llama");
        pets.put("minecraft:camel", "camel");
        return pets;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
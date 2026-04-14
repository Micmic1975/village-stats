package com.example.villagestats.client;

import com.example.villagestats.VillageStats;
import com.example.villagestats.network.VillageStatsPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class VillageStatsClient implements ClientModInitializer {

	private static KeyMapping OPEN_SCREEN_KEY;
	private static KeyMapping.Category KEY_CATEGORY;

	@Override
	public void onInitializeClient() {
		KEY_CATEGORY = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(VillageStats.MOD_ID, "category")
		);

		OPEN_SCREEN_KEY = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.village_stats.open",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_V,
						KEY_CATEGORY
				)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_SCREEN_KEY.consumeClick()) {
				client.setScreen(new VillageStatsScreen());
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(
				VillageStatsPayload.TYPE,
				(payload, context) -> context.client().execute(() -> {
					ClientVillageStatsState.villageFound = payload.villageFound();
					ClientVillageStatsState.villagers = payload.villagers();
					ClientVillageStatsState.beds = payload.beds();
					ClientVillageStatsState.freeBeds = payload.freeBeds();
					ClientVillageStatsState.professions = payload.professions();
					ClientVillageStatsState.jobSites = payload.jobSites();
				})
		);
	}
}
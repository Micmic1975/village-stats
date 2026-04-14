package com.example.villagestats;

import com.example.villagestats.network.VillageStatsPayload;
import com.example.villagestats.server.VillageStatsServerSync;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class VillageStats implements ModInitializer {

	public static final String MOD_ID = "village-stats";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(VillageStatsPayload.TYPE, VillageStatsPayload.CODEC);
		VillageStatsServerSync.init();
	}
}
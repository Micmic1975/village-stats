package com.example.villagestats.server;

import com.example.villagestats.network.VillageStatsPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VillageStatsServerSync {

    private static final int VILLAGE_SCAN_RADIUS = 64;
    private static final int UPDATE_INTERVAL_TICKS = 20;

    private static final Map<UUID, BlockPos> LAST_PLAYER_POSITIONS = new HashMap<>();
    private static final Map<UUID, BlockPos> LAST_MEETING_POINTS = new HashMap<>();
    private static final Map<UUID, VillageStatsPayload> LAST_SENT_PAYLOADS = new HashMap<>();

    private static int tickCounter = 0;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            if (tickCounter < UPDATE_INTERVAL_TICKS) {
                return;
            }

            tickCounter = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                BlockPos currentPos = player.blockPosition();
                BlockPos lastPos = LAST_PLAYER_POSITIONS.get(playerId);

                boolean moved = lastPos == null || !lastPos.equals(currentPos);
                LAST_PLAYER_POSITIONS.put(playerId, currentPos);

                BlockPos villageCenter = findNearestMeetingPoint(player);
                BlockPos lastMeetingPoint = LAST_MEETING_POINTS.get(playerId);

                boolean meetingChanged =
                        (lastMeetingPoint == null && villageCenter != null)
                                || (lastMeetingPoint != null && villageCenter == null)
                                || (lastMeetingPoint != null && !lastMeetingPoint.equals(villageCenter));

                if (!moved && !meetingChanged) {
                    continue;
                }

                LAST_MEETING_POINTS.put(playerId, villageCenter);

                if (villageCenter == null) {
                    VillageStatsPayload payload = new VillageStatsPayload(
                            false,
                            0,
                            0,
                            0,
                            new HashMap<>(),
                            new HashMap<>()
                    );

                    VillageStatsPayload lastPayload = LAST_SENT_PAYLOADS.get(playerId);
                    if (!isSamePayload(lastPayload, payload)) {
                        LAST_SENT_PAYLOADS.put(playerId, payload);
                        ServerPlayNetworking.send(player, payload);
                    }

                    continue;
                }

                List<Villager> villagers = countVillagersAroundCenter(player, villageCenter);

                Map<String, Integer> professions = new HashMap<>();
                for (Villager villager : villagers) {
                    String id = villager.getVillagerData()
                            .profession()
                            .unwrapKey()
                            .map(key -> key.identifier().toString())
                            .orElse("village-stats:unknown");

                    professions.put(id, professions.getOrDefault(id, 0) + 1);
                }

                int beds = countBedsAroundCenter(player, villageCenter);
                int freeBeds = Math.max(beds - villagers.size(), 0);
                Map<String, Integer> jobSites = countJobSitesAroundCenter(player, villageCenter);

                VillageStatsPayload payload = new VillageStatsPayload(
                        true,
                        villagers.size(),
                        beds,
                        freeBeds,
                        professions,
                        jobSites
                );

                VillageStatsPayload lastPayload = LAST_SENT_PAYLOADS.get(playerId);
                if (isSamePayload(lastPayload, payload)) {
                    continue;
                }

                LAST_SENT_PAYLOADS.put(playerId, payload);
                ServerPlayNetworking.send(player, payload);
            }
        });
    }

    private static BlockPos findNearestMeetingPoint(ServerPlayer player) {
        PoiManager poiManager = player.level().getPoiManager();
        BlockPos center = player.blockPosition();

        return poiManager.getInRange(
                        holder -> holder.is(PoiTypes.MEETING),
                        center,
                        VILLAGE_SCAN_RADIUS,
                        PoiManager.Occupancy.ANY
                ).map(record -> record.getPos())
                .min((a, b) -> Double.compare(a.distSqr(center), b.distSqr(center)))
                .orElse(null);
    }

    private static List<Villager> countVillagersAroundCenter(ServerPlayer player, BlockPos center) {
        AABB box = new AABB(center).inflate(VILLAGE_SCAN_RADIUS);

        return player.level().getEntitiesOfClass(
                Villager.class,
                box
        );
    }

    private static int countBedsAroundCenter(ServerPlayer player, BlockPos center) {
        PoiManager poiManager = player.level().getPoiManager();

        return (int) poiManager.getInRange(
                holder -> holder.is(PoiTypes.HOME),
                center,
                VILLAGE_SCAN_RADIUS,
                PoiManager.Occupancy.ANY
        ).count();
    }

    private static Map<String, Integer> countJobSitesAroundCenter(ServerPlayer player, BlockPos center) {
        PoiManager poiManager = player.level().getPoiManager();

        Map<String, Integer> result = new HashMap<>();

        poiManager.getInRange(
                VillageStatsServerSync::isWorkstationPoi,
                center,
                VILLAGE_SCAN_RADIUS,
                PoiManager.Occupancy.ANY
        ).forEach(record -> {
            String id = getJobSiteId(record.getPoiType());
            result.put(id, result.getOrDefault(id, 0) + 1);
        });

        return result;
    }

    private static boolean isWorkstationPoi(Holder<PoiType> holder) {
        return holder.is(PoiTypes.ARMORER)
                || holder.is(PoiTypes.BUTCHER)
                || holder.is(PoiTypes.CARTOGRAPHER)
                || holder.is(PoiTypes.CLERIC)
                || holder.is(PoiTypes.FARMER)
                || holder.is(PoiTypes.FISHERMAN)
                || holder.is(PoiTypes.FLETCHER)
                || holder.is(PoiTypes.LEATHERWORKER)
                || holder.is(PoiTypes.LIBRARIAN)
                || holder.is(PoiTypes.MASON)
                || holder.is(PoiTypes.SHEPHERD)
                || holder.is(PoiTypes.TOOLSMITH)
                || holder.is(PoiTypes.WEAPONSMITH);
    }

    private static String getJobSiteId(Holder<PoiType> holder) {
        if (holder.is(PoiTypes.FARMER)) return "minecraft:composter";
        if (holder.is(PoiTypes.ARMORER)) return "minecraft:blast_furnace";
        if (holder.is(PoiTypes.BUTCHER)) return "minecraft:smoker";
        if (holder.is(PoiTypes.LIBRARIAN)) return "minecraft:lectern";
        if (holder.is(PoiTypes.CARTOGRAPHER)) return "minecraft:cartography_table";
        if (holder.is(PoiTypes.CLERIC)) return "minecraft:brewing_stand";
        if (holder.is(PoiTypes.FISHERMAN)) return "minecraft:barrel";
        if (holder.is(PoiTypes.FLETCHER)) return "minecraft:fletching_table";
        if (holder.is(PoiTypes.LEATHERWORKER)) return "minecraft:cauldron";
        if (holder.is(PoiTypes.MASON)) return "minecraft:stonecutter";
        if (holder.is(PoiTypes.SHEPHERD)) return "minecraft:loom";
        if (holder.is(PoiTypes.TOOLSMITH)) return "minecraft:smithing_table";
        if (holder.is(PoiTypes.WEAPONSMITH)) return "minecraft:grindstone";

        return "village-stats:unknown";
    }

    private static boolean isSamePayload(VillageStatsPayload a, VillageStatsPayload b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.villagers() != b.villagers()) {
            return false;
        }

        if (a.beds() != b.beds()) {
            return false;
        }

        if (a.freeBeds() != b.freeBeds()) {
            return false;
        }

        if (!a.professions().equals(b.professions())) {
            return false;
        }

        return a.jobSites().equals(b.jobSites());
    }
}
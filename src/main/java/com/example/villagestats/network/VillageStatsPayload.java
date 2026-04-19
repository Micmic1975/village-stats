package com.example.villagestats.network;

import com.example.villagestats.VillageStats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public record VillageStatsPayload(
        boolean villageFound,
        int villagers,
        int beds,
        int freeBeds,
        Map<String, Integer> professions,
        Map<String, Integer> jobSites,
        Map<String, Integer> animals
) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID =
            Identifier.fromNamespaceAndPath(VillageStats.MOD_ID, "stats");

    public static final CustomPacketPayload.Type<VillageStatsPayload> TYPE =
            new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, VillageStatsPayload> CODEC =
            StreamCodec.of(
                    VillageStatsPayload::write,
                    VillageStatsPayload::read
            );

    private static void write(RegistryFriendlyByteBuf buf, VillageStatsPayload payload) {
        buf.writeBoolean(payload.villageFound);
        buf.writeInt(payload.villagers);
        buf.writeInt(payload.beds);
        buf.writeInt(payload.freeBeds);

        writeStringIntMap(buf, payload.professions);
        writeStringIntMap(buf, payload.jobSites);
        writeStringIntMap(buf, payload.animals);
    }

    private static VillageStatsPayload read(RegistryFriendlyByteBuf buf) {
        boolean villageFound = buf.readBoolean();
        int villagers = buf.readInt();
        int beds = buf.readInt();
        int freeBeds = buf.readInt();

        Map<String, Integer> professions = readStringIntMap(buf);
        Map<String, Integer> jobSites = readStringIntMap(buf);
        Map<String, Integer> animals = readStringIntMap(buf);

        return new VillageStatsPayload(
                villageFound,
                villagers,
                beds,
                freeBeds,
                professions,
                jobSites,
                animals
        );
    }

    private static void writeStringIntMap(RegistryFriendlyByteBuf buf, Map<String, Integer> map) {
        buf.writeInt(map.size());

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    private static Map<String, Integer> readStringIntMap(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            int value = buf.readInt();
            map.put(key, value);
        }

        return map;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
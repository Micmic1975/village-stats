package com.example.villagestats.client;

import java.util.HashMap;
import java.util.Map;

public class ClientVillageStatsState {

    public static boolean villageFound = false;

    public static int villagers = 0;
    public static int beds = 0;
    public static int freeBeds = 0;

    public static Map<String, Integer> professions = new HashMap<>();
    public static Map<String, Integer> jobSites = new HashMap<>();
    public static Map<String, Integer> animals = new HashMap<>();
}
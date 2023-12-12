package cordori.dpstamina.utils;

import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.data.MapCount;
import cordori.dpstamina.data.PlayerData;

import java.util.HashMap;
import java.util.UUID;

public class CountProcess {

    public static String countToStr(UUID uuid, String s) {
        if(!ConfigManager.dataMap.containsKey(uuid)) return s;
        PlayerData playerData = ConfigManager.dataMap.get(uuid);
        HashMap<String, MapCount> mapCountMap = playerData.getMapCountMap();

        if(mapCountMap.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for(String key : mapCountMap.keySet()) {
            MapCount mapCount = mapCountMap.get(key);

            switch (s) {
                case "day":
                    mapCount.setDayCount(0);
                case "week":
                    mapCount.setWeekCount(0);
                case "month":
                    mapCount.setMonthCount(0);
            }

            int dayCount = mapCount.getDayCount();
            int weeCount = mapCount.getWeekCount();
            int monthCount = mapCount.getMonthCount();
            String str = key + "," + dayCount + "," + weeCount + "," + monthCount + ";";
            sb.append(str);
        }
        sb.deleteCharAt(sb.length()-1);
        LogInfo.debug("count转为字符串: " + sb);
        return sb.toString();
    }

    public static void strToCount(UUID uuid, String str) {
        if(!ConfigManager.dataMap.containsKey(uuid)) return;
        PlayerData playerData = ConfigManager.dataMap.get(uuid);
        HashMap<String, MapCount> mapCountMap = playerData.getMapCountMap();
        String[] strings = str.split(";");

        LogInfo.debug("mapCountMap转成字符串" + str);

        for (String string : strings) {
            String[] counts = string.split(",");
            String key = counts[0];
            if (!mapCountMap.containsKey(key)) {
                MapCount mapCount = new MapCount();
                mapCount.setDayCount(0);
                mapCount.setWeekCount(0);
                mapCount.setMonthCount(0);
                playerData.getMapCountMap().put(key, mapCount);
                continue;
            }

            int dayCount = Integer.parseInt(counts[1]);
            int weekCount = Integer.parseInt(counts[2]);
            int monthCount = Integer.parseInt(counts[3]);

            MapCount mapCount = mapCountMap.get(key);
            mapCount.setDayCount(dayCount);
            mapCount.setWeekCount(weekCount);
            mapCount.setMonthCount(monthCount);

            playerData.setMapCountMap(mapCountMap);

            LogInfo.debug("key为: " + key);
            LogInfo.debug("dayCount为: " + dayCount);
            LogInfo.debug("weekCount为: " + weekCount);
            LogInfo.debug("monthCount为: " + monthCount);
        }
    }
}

package cordori.dpstamina.manager;

import cordori.dpstamina.Main;
import cordori.dpstamina.data.GroupData;
import cordori.dpstamina.data.MapOption;
import cordori.dpstamina.data.PlayerData;
import cordori.dpstamina.data.RegionData;
import cordori.dpstamina.task.PermScheduler;
import cordori.dpstamina.task.RefreshScheduler;
import cordori.dpstamina.task.SQLScheduler;
import cordori.dpstamina.task.StaminaScheduler;
import cordori.dpstamina.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {
    public static boolean debug;
    public static int saveTime;
    public static int loadDelay;
    public static String universalTicket;
    public static boolean offline;
    public static boolean regionRecover;
    public static boolean refresh;
    public static String refreshTime;
    public static int refreshWeek;
    public static int refreshMonth;
    public static int minutes;

    public static HashMap<String, GroupData> groupMap = new HashMap<>();
    public static LinkedHashMap<String, String> permMap = new LinkedHashMap<>();
    public static HashMap<String, String> msgMap = new HashMap<>();
    public static ConcurrentHashMap<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();
    public static HashMap<String, MapOption> mapMap = new HashMap<>();

    public static void reloadMyConfig() {
        Main.inst.reloadConfig();
        FileConfiguration config = Main.inst.getConfig();

        debug = config.getBoolean("Debug");
        saveTime = config.getInt("SaveTime");
        loadDelay = config.getInt("LoadDelay");
        universalTicket= config.getString("UniversalTicket").replaceAll("&", "§");
        offline = config.getBoolean("Offline");
        regionRecover = config.getBoolean("RegionRecover");
        refresh = config.getBoolean("Refresh");
        refreshTime = config.getString("RefreshTime");
        refreshWeek = config.getInt("RefreshWeek");
        refreshMonth = config.getInt("RefreshMonth");
        minutes = config.getInt("Minutes");

        loadGroup(config);
        loadMessages(config);
        loadRegions(config);

        mapMap.clear();
        File folder = new File(Main.inst.getDataFolder() + "/mapOptions");
        findAllYmlFiles(folder);

    }

    /**
     * 加载体力组配置
     * @param config 传入配置文件
     */
    public static void loadGroup(FileConfiguration config) {
        groupMap.clear();
        permMap.clear();

        Set<String> group = config.getConfigurationSection("Group").getKeys(false);
        LogInfo.debug("§b==========================");
        for(String key : group) {
            String perm = config.getString("Group." + key + ".perm", "dpstamina.default");
            double limit = config.getDouble("Group." + key + ".limit");
            String recover = config.getString("Group." + key + ".recover");

            groupMap.put(key, new GroupData(key, perm, limit, recover));
            permMap.put(perm, key);

            LogInfo.debug("§c--------------------------");
            LogInfo.debug("key: " + key);
            LogInfo.debug("perm: " + perm);
            LogInfo.debug("limit: " + limit);
            LogInfo.debug("recover: " + recover);
        }
        LogInfo.debug("§b==========================");
    }

    /**
     * 加载消息
     * @param config 传入配置文件
     */
    public static void loadMessages(FileConfiguration config) {
        msgMap.clear();
        Set<String> messages = config.getConfigurationSection("Messages").getKeys(false);
        LogInfo.debug("§d==========================");
        for(String key : messages) {
            String msg = config.getString("Messages." + key).replaceAll("&", "§");
            if(msg.equals("")) continue;
            msgMap.put(key, msg);
            LogInfo.debug(key + ": " + msg);
        }
        LogInfo.debug("§d==========================");
    }

    /**
     * 加载配置文件中的区域
     * @param config 传入配置文件
     */
    public static void loadRegions(FileConfiguration config) {
        RegionData.regionsMap.clear();
        List<String> regions = config.getStringList("regions");
        for(String region : regions) {
            String[] reg = region.split(";");
            // 获取区域名称和两个坐标点的坐标值
            String worldName = reg[0];
            String[] pos1 = reg[1].split(",");
            String[] pos2 = reg[2].split(",");
            RegionData.regionsMap.put(worldName, new RegionData(pos1, pos2));
        }
    }

    public static void startTasks() {
        Bukkit.getScheduler().cancelTasks(Main.inst);
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.inst, new StaminaScheduler(), 0L, 20L * 60L * ConfigManager.minutes);
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.inst, new SQLScheduler(), 0L, 20L * 60L * saveTime);
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.inst, new RefreshScheduler(), 0L, 20L * 60L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.inst, new PermScheduler(), 0L, 50L);
    }

    /**
     * 用于递归加载yml文件
     * @param folder 一个文件夹对象
     */
    private static void findAllYmlFiles(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是文件夹则递归查找
                    findAllYmlFiles(file);
                } else if (file.getName().endsWith(".yml")) {
                    // 如果是YML文件则加入结果列表
                    File potionfile = new File(folder + "/" + file.getName());
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(potionfile);
                    loadMapOptions(config);
                }
            }
        }
    }

    /**
     * 用一个mapOption对象将副本所有配置保存起来
     * @param config 对应的配置文件
     */
    private static void loadMapOptions(YamlConfiguration config) {
        Set<String> set = config.getKeys(false);
        LogInfo.debug("§e==========================");
        for(String key : set) {
            String mapName = config.getString(key + ".mapName").replaceAll("&", "§");
            double cost = config.getDouble(key + ".cost", 0);
            String ticket = config.getString(key + ".ticket", "").replaceAll("&", "§");
            boolean allowUniversal = config.getBoolean(key + ".allowUniversal", true);
            int dayLimit = config.getInt(key + ".dayLimit", -1);
            int weekLimit = config.getInt(key + ".weekLimit", -1);
            int monthLimit = config.getInt(key + ".monthLimit", -1);
            MapOption mo = new MapOption(mapName, cost, ticket, allowUniversal, dayLimit, weekLimit, monthLimit);
            mapMap.put(key, mo);
            LogInfo.debug("---------------------------");
            LogInfo.debug("地图: " + key);
            LogInfo.debug("地图名: " + mapName);
            LogInfo.debug("消耗体力: " + cost);
            LogInfo.debug("门票: " + ticket);
            LogInfo.debug("是否允许通票: " + allowUniversal);
            LogInfo.debug("每日挑战限制: " + dayLimit);
            LogInfo.debug("每周挑战限制: " + weekLimit);
            LogInfo.debug("每月挑战限制: " + monthLimit);
        }
        LogInfo.debug("§e==========================");
    }

    /**
     *
     * @param player 玩家
     * @return 返回玩家的体力组
     */
    public static String getGroup(Player player) {
        for (String perm : permMap.keySet()) {
            if(perm.equals("dpstamina.default")) return "default";
            else if(player.hasPermission(perm)) {
                return permMap.get(perm);
            }
        }
        return "default";
    }
}

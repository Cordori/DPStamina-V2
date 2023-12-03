package cordori.dpstamina;

import cordori.dpstamina.commands.MainCommand;
import cordori.dpstamina.dataManager.ConfigManager;
import cordori.dpstamina.dataManager.SQLManager;
import cordori.dpstamina.hook.PAPIHook;
import cordori.dpstamina.listeners.DPListener;
import cordori.dpstamina.listeners.JoinQuitListener;
import cordori.dpstamina.objectManager.PlayerData;
import cordori.dpstamina.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public static Main inst;
    public static boolean MySQL = false;

    @Override
    public void onEnable() {
        inst = this;
        checkPlugins();
        try {
            createFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigManager.reloadMyConfig();

        if(!getConfig().getString("Version").equalsIgnoreCase(getDescription().getVersion())) {
            getLogger().warning("§b[DPStamina]§e插件版本为:§a " + getDescription().getVersion());
            getLogger().warning("§b[DPStamina]§e当前配置版本为:§7 " + getConfig().getString("Version"));
            getLogger().warning("§b[DPStamina]§e请更新插件！");
        }

        if(getConfig().getString("Storage").equalsIgnoreCase("MySQL")) MySQL = true;
        loadSQL();

        Bukkit.getPluginCommand("DPStamina").setExecutor(new MainCommand());
        Bukkit.getPluginManager().registerEvents(new DPListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
        Metrics metrics = new Metrics(this, 20424);
        getLogger().info("§b[DPStamina]§b插件加载完成！");
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if(!ConfigManager.dataMap.containsKey(uuid)) continue;
            PlayerData playerData = ConfigManager.dataMap.get(uuid);
            SQLManager.sql.saveData(uuid, playerData);
        }
        Bukkit.getScheduler().cancelTasks(this);
        if(SQLManager.sql != null) SQLManager.sql.disconnect();
        getLogger().info("§b[DPStamina]§c插件插件已卸载！");
    }

    private void checkPlugins() {
        if(Bukkit.getPluginManager().getPlugin("DungeonPlus") != null) {
            getLogger().info("§e[地牢体力]§b已找到DungeonPlus插件，插件正在加载......");
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
            getLogger().severe("[地牢体力]未找到DungeonPlus插件，插件加载失败！");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIHook().register();
            getLogger().info("§e[地牢体力]§b已找到PlaceholderAPI插件，可使用变量！");
        } else {
            getLogger().warning("[地牢体力]未找到PlaceholderAPI插件，无法使用变量！");
        }
    }

    private void createFile() throws IOException {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();
        File Folder = new File(getDataFolder(), "mapOptions");
        if(!Folder.exists()) Folder.mkdirs();
        File File = new File(Folder,"example.yml");
        if(!File.exists()) {
            File.createNewFile();
            try (InputStream inputStream = getResource("mapOptions/example.yml");
                 OutputStream outputStream = Files.newOutputStream(File.toPath())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadSQL() {
        String host = getConfig().getString("MySQL.host");
        String port = getConfig().getString("MySQL.port");
        String username = getConfig().getString("MySQL.username");
        String password = getConfig().getString("MySQL.password");
        String fileName = getConfig().getString("MySQL.fileName");
        String tableName = getConfig().getString("MySQL.tableName").toLowerCase();
        String driver;
        driver = getConfig().getString("MySQL.driver");
        String jdbc = getConfig().getString("MySQL.jdbc");
        String sqlString;
        if(MySQL) {
            sqlString = "jdbc:mysql://" + host + ":" + port + "/" + fileName + jdbc;
        } else {
            driver = "org.sqlite.JDBC";
            sqlString = "jdbc:sqlite:" + getDataFolder().toPath().resolve("database.db");
        }
        SQLManager.sql = new SQLManager(sqlString, username, password, driver, tableName);
        SQLManager.sql.createTable();
        ConfigManager.startTasks();
    }
}

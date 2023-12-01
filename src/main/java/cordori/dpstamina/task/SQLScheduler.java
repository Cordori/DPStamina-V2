package cordori.dpstamina.task;

import cordori.dpstamina.dataManager.ConfigManager;
import cordori.dpstamina.dataManager.SQLManager;
import cordori.dpstamina.objectManager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SQLScheduler implements Runnable {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if(!ConfigManager.dataMap.containsKey(uuid)) continue;
            PlayerData playerData = ConfigManager.dataMap.get(uuid);
            SQLManager.sql.saveData(uuid, playerData);
        }
    }
}

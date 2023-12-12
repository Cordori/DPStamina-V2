package cordori.dpstamina.task;

import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermScheduler implements Runnable {

    public void run() {
        for(Player player :Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if(!ConfigManager.dataMap.containsKey(uuid)) continue;
            PlayerData playerData = ConfigManager.dataMap.get(uuid);
            double stamina = playerData.getStamina();
            String group = ConfigManager.getGroup(player);
            double limit = ConfigManager.groupMap.get(group).getLimit();
            if(stamina > limit) stamina = limit;
            playerData.setStamina(stamina);
        }
    }
}

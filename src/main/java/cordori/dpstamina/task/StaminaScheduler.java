package cordori.dpstamina.task;


import cordori.dpstamina.dataManager.ConfigManager;
import cordori.dpstamina.hook.PAPIHook;
import cordori.dpstamina.objectManager.GroupData;
import cordori.dpstamina.objectManager.PlayerData;
import cordori.dpstamina.objectManager.RegionData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StaminaScheduler implements Runnable {

    @Override
    public void run() {

        if(ConfigManager.regionRecover && !RegionData.regionsMap.isEmpty()) {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if(!RegionData.isPlayerInRegion(player)) continue;
                staminaRecover(player);
            }

        } else {

            for (Player player : Bukkit.getOnlinePlayers()) {
                staminaRecover(player);
            }
        }
    }

    private void staminaRecover(Player player) {

        UUID uuid = player.getUniqueId();
        if(!ConfigManager.dataMap.containsKey(uuid)) return;

        PlayerData playerData = ConfigManager.dataMap.get(uuid);
        double stamina = playerData.getStamina();
        String group = ConfigManager.getGroup(player);

        GroupData groupData = ConfigManager.groupMap.get(group);
        double limit = groupData.getLimit();

        // 如果体力已经满了则不需要恢复
        if (stamina >= limit) {
            return;
        }

        // 计算体力恢复量
        double recover = Double.parseDouble(PAPIHook.onPAPIProcess(player, groupData.getRecover()));
        double recoveredStamina = stamina + recover;
        if (recoveredStamina > limit) {
            recoveredStamina = limit;
        }

        playerData.setStamina(recoveredStamina);
    }
}


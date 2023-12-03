package cordori.dpstamina.listeners;

import cordori.dpstamina.Main;
import cordori.dpstamina.dataManager.ConfigManager;
import cordori.dpstamina.dataManager.SQLManager;
import cordori.dpstamina.hook.PAPIHook;
import cordori.dpstamina.objectManager.GroupData;
import cordori.dpstamina.objectManager.PlayerData;
import cordori.dpstamina.utils.CountProcess;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.inst, () -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            // 从数据库拉取数据
            List<Object> objectList = SQLManager.sql.getData(uuid);
            if(objectList == null) {
                SQLManager.sql.insertNewData(uuid);
                return;
            }
            Double newStamina;
            Double stamina = (Double) objectList.get(0);
            int dayRecord = (int) objectList.get(2);
            int weekRecord = (int) objectList.get(3);
            int monthRecord = (int) objectList.get(4);
            String mapCountStr = (String) objectList.get(5);
            newStamina = stamina;

            // 如果开启了离线回复，计算回复量后存入PlayerData
            if(ConfigManager.offline) {
                long lastTime = (long) objectList.get(1);
                long currentTime = System.currentTimeMillis();
                long timeDiffMinutes = (currentTime - lastTime) / (1000L * 60L * ConfigManager.minutes);
                if(timeDiffMinutes >= 1) {
                    String group = ConfigManager.getGroup(player);
                    GroupData groupData = ConfigManager.groupMap.get(group);
                    double limit = groupData.getLimit();
                    double recover = Double.parseDouble(PAPIHook.onPAPIProcess(player, groupData.getRecover()));
                    double timeRecover = timeDiffMinutes * recover;
                    newStamina = stamina + timeRecover;
                    if (newStamina > limit) {
                        newStamina = limit;
                        timeRecover = Math.max(limit - stamina, 0);
                    }

                    if(ConfigManager.messagesMap.containsKey("join")) {
                        player.sendMessage(ConfigManager.messagesMap.get("join")
                                .replaceAll("%min%", String.valueOf(timeDiffMinutes))
                                .replaceAll("%num%", String.valueOf(timeRecover))
                                .replaceAll("%stamina%", String.valueOf(newStamina))
                        );
                    }
                }
            }
            ConfigManager.dataMap.put(uuid, new PlayerData(newStamina, dayRecord, weekRecord, monthRecord, new HashMap<>()));
            CountProcess.strToCount(uuid, mapCountStr);

        }, ConfigManager.loadDelay);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.inst, () -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            if(!ConfigManager.dataMap.containsKey(uuid)) return;
            PlayerData playerData = ConfigManager.dataMap.get(uuid);
            SQLManager.sql.saveData(uuid, playerData);
            ConfigManager.dataMap.remove(uuid);
        });
    }

}

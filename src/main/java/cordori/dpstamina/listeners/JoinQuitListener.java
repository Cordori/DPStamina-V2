package cordori.dpstamina.listeners;

import cordori.dpstamina.Main;
import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.manager.SQLManager;
import cordori.dpstamina.hook.PAPIHook;
import cordori.dpstamina.data.GroupData;
import cordori.dpstamina.data.PlayerData;
import cordori.dpstamina.task.RefreshScheduler;
import cordori.dpstamina.utils.CountProcess;

import cordori.dpstamina.utils.LogInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.inst, () -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            CountProcess.loadData(uuid);

            PlayerData playerData = ConfigManager.dataMap.get(uuid);

            // 如果开启了离线回复，计算回复量后存入PlayerData
            if(ConfigManager.offline) {
                Double newStamina;
                Double stamina = playerData.getStamina();
                long lastTime = playerData.getOfflineTime();
                long currentTime = System.currentTimeMillis();
                long offlineTime = (currentTime - lastTime) / (1000L * 60L);
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

                    if(ConfigManager.msgMap.containsKey("join") && timeRecover > 0) {
                        player.sendMessage(ConfigManager.msgMap.get("join")
                                .replaceAll("%min%", String.valueOf(offlineTime))
                                .replaceAll("%num%", String.valueOf(timeRecover))
                                .replaceAll("%stamina%", String.valueOf(newStamina))
                        );
                    }

                    playerData.setStamina(newStamina);
                }
            }

            // 判断一下是否刷新
            RefreshScheduler.refresh(player);

            LogInfo.debug("刷新后的stamina: " + playerData.getStamina());
            LogInfo.debug("dataMap是否有玩家数据: " + ConfigManager.dataMap.containsKey(uuid));

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

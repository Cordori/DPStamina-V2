package cordori.dpstamina.task;

import cordori.dpstamina.dataManager.ConfigManager;
import cordori.dpstamina.dataManager.SQLManager;
import cordori.dpstamina.objectManager.GroupData;
import cordori.dpstamina.objectManager.PlayerData;
import cordori.dpstamina.utils.CountProcess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RefreshScheduler implements Runnable {
    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if(!ConfigManager.dataMap.containsKey(uuid)) continue;
            PlayerData playerData = ConfigManager.dataMap.get(uuid);

            //刷新每日体力判断
            int dayRecord = playerData.getDayRecord();
            // 获取当前日期
            Calendar calendar = Calendar.getInstance();
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            if (dayOfMonth != dayRecord) {
                LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
                LocalTime refreshTime = LocalTime.parse(ConfigManager.refreshTime);

                // 如果当前时间超过刷新时间
                if (currentTime.isAfter(refreshTime)) {
                    // 如果开启每日刷新体力
                    if(ConfigManager.refresh) {
                        double stamina = playerData.getStamina();
                        String group = ConfigManager.getGroup(player);

                        GroupData groupData = ConfigManager.groupMap.get(group);
                        double limit = groupData.getLimit();

                        if (stamina < limit) {
                            playerData.setStamina(limit);
                            if (ConfigManager.messagesMap.containsKey("refresh")) {
                                player.sendMessage(ConfigManager.messagesMap.get("refresh"));
                            }
                        }
                    }
                    // 刷新每日次数
                    CountProcess.countToStr(uuid, "day");
                    SQLManager.sql.refreshData(uuid, "dayRecord", dayRecord);

                    // 刷新每周次数
                    int weekRecord = playerData.getWeekRecord();
                    int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    if(weekOfMonth != weekRecord && dayOfWeek >= ConfigManager.refreshWeek) {
                        CountProcess.countToStr(uuid, "week");
                        SQLManager.sql.refreshData(uuid, "weekRecord", weekOfMonth);
                    }

                    // 刷新每月次数
                    int currentMonth = LocalDate.now().getMonthValue();
                    int monthRecord = playerData.getMonthRecord();
                    int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    if(monthRecord != currentMonth && currentDayOfMonth >= ConfigManager.refreshMonth) {
                        CountProcess.countToStr(uuid, "month");
                        SQLManager.sql.refreshData(uuid, "monthRecord", currentMonth);
                    }
                }
            }
        }
    }
}
package cordori.dpstamina.task;

import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.manager.SQLManager;
import cordori.dpstamina.data.GroupData;
import cordori.dpstamina.data.PlayerData;
import cordori.dpstamina.utils.CountProcess;
import cordori.dpstamina.utils.LogInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RefreshScheduler implements Runnable {

    public static void refresh(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = ConfigManager.dataMap.get(uuid);
        //刷新每日体力判断
        int dayRecord = playerData.getDayRecord();
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        LogInfo.debug("dayRecord: " + dayRecord);
        LogInfo.debug("dayOfMonth: " + dayOfMonth);

        if (dayOfMonth != dayRecord) {
            LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
            LocalTime refreshTime = LocalTime.parse(ConfigManager.refreshTime);

            LogInfo.debug("currentTime: " + currentTime);
            LogInfo.debug("refreshTime: " + refreshTime);

            // 如果当前时间超过刷新时间
            if (currentTime.equals(refreshTime) || currentTime.isAfter(refreshTime)) {
                // 如果开启每日刷新体力
                if(ConfigManager.refresh) {
                    double stamina = playerData.getStamina();
                    String group = ConfigManager.getGroup(player);

                    GroupData groupData = ConfigManager.groupMap.get(group);
                    double limit = groupData.getLimit();

                    if (stamina < limit) {
                        playerData.setStamina(limit);
                        if (ConfigManager.msgMap.containsKey("refresh")) {
                            player.sendMessage(ConfigManager.msgMap.get("refresh"));
                        }
                    }
                }
                // 刷新每日次数
                playerData.setDayRecord(dayOfMonth);
                CountProcess.countToStr(uuid, "day");
                SQLManager.sql.refreshData(uuid, "dayRecord", dayOfMonth);

                // 刷新每周次数
                int weekRecord = playerData.getWeekRecord();
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                if(weekOfMonth != weekRecord && dayOfWeek >= ConfigManager.refreshWeek) {
                    playerData.setWeekRecord(dayOfWeek);
                    CountProcess.countToStr(uuid, "week");
                    SQLManager.sql.refreshData(uuid, "weekRecord", weekOfMonth);
                }

                // 刷新每月次数
                int currentMonth = LocalDate.now().getMonthValue();
                int monthRecord = playerData.getMonthRecord();
                int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                if(monthRecord != currentMonth && currentDayOfMonth >= ConfigManager.refreshMonth) {
                    playerData.setMonthRecord(currentMonth);
                    CountProcess.countToStr(uuid, "month");
                    SQLManager.sql.refreshData(uuid, "monthRecord", currentMonth);
                }
            }
        }
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if(!ConfigManager.dataMap.containsKey(uuid)) continue;
            refresh(player);
        }
    }
}

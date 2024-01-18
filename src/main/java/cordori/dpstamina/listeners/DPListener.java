package cordori.dpstamina.listeners;

import cordori.dpstamina.Main;
import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.data.MapCount;
import cordori.dpstamina.data.MapOption;
import cordori.dpstamina.data.PlayerData;
import cordori.dpstamina.utils.CountProcess;
import cordori.dpstamina.utils.LogInfo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.serverct.ersha.dungeon.common.api.event.DungeonEvent;
import org.serverct.ersha.dungeon.common.api.event.dungeon.DungeonStartEvent;
import org.serverct.ersha.dungeon.common.team.Team;
import org.serverct.ersha.dungeon.common.team.type.PlayerStateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DPListener implements Listener {
    public static HashMap<UUID, List<UUID>> takeStaminaMap = new HashMap<>();
    public static HashMap<UUID, HashMap<UUID, ItemStack>> takeTicketMap = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnterDP(DungeonEvent event) {
        if(event.isCancelled()) return;
        // 没有该地图的配置就不处理
        String dungeonName = event.getDungeon().getDungeonName();
        LogInfo.debug("当前副本节点名: " + dungeonName);
        double cost;
        cost = ConfigManager.defaultCost;
        Team team = event.getDungeon().getTeam();
        if(team.leader == null) {
            team.sendTeamMessage(ConfigManager.msgMap.get("noLeader"));
            event.setCancelled(true);
            return;
        }
        UUID leaderUUID = team.leader;
        List<Player> playerList = team.getPlayers(PlayerStateType.ALL);

        // 对于没有配置的单独检测
        if (!ConfigManager.mapMap.containsKey(dungeonName)) {
            if(cost <= 0) return;
            if (event.getEvent() instanceof DungeonStartEvent.Before) {
                for(Player player : playerList) {
                    UUID uuid = player.getUniqueId();
                    String playerName = player.getName();
                    // 如果没有玩家数据，取消事件
                    if(!ConfigManager.dataMap.containsKey(uuid)) {
                        Bukkit.getScheduler().runTaskAsynchronously(Main.inst, () -> {
                            CountProcess.loadData(uuid);
                            team.sendTeamMessage(ConfigManager.msgMap.get("noData").replace("%player%", playerName));
                        });
                        event.setCancelled(true);
                        return;
                    }
                    PlayerData playerData = ConfigManager.dataMap.get(uuid);
                    double stamina = playerData.getStamina();
                    if(stamina < cost) {
                        team.sendTeamMessage(ConfigManager.msgMap.get("noStamina")
                                .replace("%player%", playerName));
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (event.getEvent() instanceof DungeonStartEvent.After) {
                for(Player player : playerList) {
                    UUID uuid = player.getUniqueId();
                    PlayerData playerData = ConfigManager.dataMap.get(uuid);
                    double newStamina = playerData.getStamina() - cost;
                    playerData.setStamina(newStamina);
                    player.sendMessage(ConfigManager.msgMap.get("cost")
                            .replace("%cost%", String.valueOf(cost))
                            .replace("%dungeon%", dungeonName)
                            .replace("%stamina%", String.valueOf(newStamina))
                    );
                }
            }
            return;
        }

        MapOption mapOption = ConfigManager.mapMap.get(dungeonName);
        cost = mapOption.getCost();
        String customName = mapOption.getMapName();
        int dayLimit = mapOption.getDayLimit();
        int weekLimit = mapOption.getWeekLimit();
        int monthLimit = mapOption.getMonthLimit();
        LogInfo.debug("当前副本的dayLimit:" + dayLimit);
        LogInfo.debug("当前副本的weekLimit:" + weekLimit);
        LogInfo.debug("当前副本的monthLimit:" + monthLimit);


        // 副本开始之前的检测
        if (event.getEvent() instanceof DungeonStartEvent.Before) {
            long startTime = System.currentTimeMillis();

            boolean isNullTicket = (mapOption.getTicket().isEmpty());
            boolean allowUniversal = mapOption.isAllowUniversal();
            String ticket = mapOption.getTicket();

            List<UUID> staminaList = new ArrayList<>();
            HashMap<UUID, ItemStack> ticketMap = new HashMap<>();

            // 遍历队伍成员
            for(Player player : playerList) {
                String playerName = player.getName();
                LogInfo.debug("--------------------------");
                LogInfo.debug("当前检测玩家为: " + playerName);
                UUID uuid = player.getUniqueId();

                // 如果没有玩家数据，取消事件
                if(!ConfigManager.dataMap.containsKey(uuid)) {
                    Bukkit.getScheduler().runTaskAsynchronously(Main.inst, () -> {
                        CountProcess.loadData(uuid);
                        team.sendTeamMessage(ConfigManager.msgMap.get("noData").replace("%player%", playerName));
                    });
                    event.setCancelled(true);
                    return;
                }
                PlayerData playerData = ConfigManager.dataMap.get(uuid);
                HashMap<String, MapCount> mapCountMap = playerData.getMapCountMap();

                // 先判断次数
                if(!mapCountMap.containsKey(dungeonName)) {
                    mapCountMap.put(dungeonName,  new MapCount(0, 0, 0));
                }
                MapCount mapCount = mapCountMap.get(dungeonName);

                int dayCount = mapCount.getDayCount();
                int weekCount = mapCount.getWeekCount();
                int monthCount = mapCount.getMonthCount();
                LogInfo.debug("玩家当前副本的dayCount:" + dayCount);
                LogInfo.debug("玩家当前副本的weekCount:" + weekCount);
                LogInfo.debug("玩家当前副本的monthCount:" + monthCount);

                if(dayLimit != -1 && dayCount >= dayLimit) {
                    team.sendTeamMessage(ConfigManager.msgMap.get("noDayCount")
                            .replace("%player%", playerName)
                            .replace("%dungeon%", customName)
                    );
                    event.setCancelled(true);
                    return;
                }

                if(weekLimit != -1 && weekCount >= weekLimit) {
                    team.sendTeamMessage(ConfigManager.msgMap.get("noWeekCount")
                            .replace("%player%", playerName)
                            .replace("%dungeon%", customName)
                    );
                    event.setCancelled(true);
                    return;
                }

                if(monthLimit != -1 && monthCount >= monthLimit) {
                    team.sendTeamMessage(ConfigManager.msgMap.get("noMonthCount")
                            .replace("%player%", playerName)
                            .replace("%dungeon%", customName)
                    );
                    event.setCancelled(true);
                    return;
                }

                double currentStamina = playerData.getStamina();
                LogInfo.debug("副本消耗体力: " + cost);
                LogInfo.debug("玩家当前体力: " + currentStamina);

                // 如果没写门票配置，不用检查门票，判断体力就行了
                if(isNullTicket) {
                    // 如果体力消耗为-1，不判断体力，直接不让进了
                    if(cost <= -1) {
                        team.sendTeamMessage(ConfigManager.msgMap.get("allNull")
                                .replace("%dungeon%", customName));
                        event.setCancelled(true);
                        return;
                    }

                    if(cost > 0) {
                        if(currentStamina >= cost) {
                            staminaList.add(uuid);
                        } else {
                            team.sendTeamMessage(ConfigManager.msgMap.get("noStamina")
                                    .replace("%player%", playerName));
                            event.setCancelled(true);
                            return;
                        }
                    }

                } else {

                    // 遍历物品栏检查门票
                    for(ItemStack item : player.getInventory().getContents()) {
                        // 该物品没有自定义名称就跳过
                        if(item == null || item.getType() == Material.AIR ||
                                !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) continue;
                        String name = item.getItemMeta().getDisplayName();

                        // 如果允许使用通票
                        if (allowUniversal) {
                            // 如果有专票，那添加的物品就是专票
                            if (name.equals(ticket)) {
                                ticketMap.put(uuid, item);
                                LogInfo.debug("这是允许通票的专票");
                                break;
                            }
                            // 如果有通票
                            else if (name.equals(ConfigManager.universalTicket)) {
                                ticketMap.put(uuid, item);
                                LogInfo.debug("这是通票");
                            }

                        } else if (name.equals(ticket)) {
                            ticketMap.put(uuid, item);
                            LogInfo.debug("这是专票");
                            break;
                        }
                    }

                    // 如果玩家没有门票，判断是否有体力
                    if(!ticketMap.containsKey(uuid)) {
                        // 如果体力消耗为-1，直接终止
                        if(cost <= -1) {
                            team.sendTeamMessage(ConfigManager.msgMap.get("noTicket")
                                    .replace("%player%", playerName));
                            event.setCancelled(true);
                            return;
                        }

                        // 如果当前体力大于等于副本花费体力，加到扣除体力列表里
                        if(cost > 0) {
                            if(currentStamina >= cost) {
                                staminaList.add(uuid);
                            } else {
                                team.sendTeamMessage(ConfigManager.msgMap.get("noTicket")
                                        .replace("%player%", playerName));
                                team.sendTeamMessage(ConfigManager.msgMap.get("noStamina")
                                        .replace("%player%", playerName));
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
            LogInfo.debug("扣体力列表: " + staminaList);
            LogInfo.debug("扣门票列表: " + ticketMap);
            takeStaminaMap.put(leaderUUID, staminaList);
            takeTicketMap.put(leaderUUID, ticketMap);

            long finishTime = System.currentTimeMillis();
            long time = finishTime - startTime;
            LogInfo.debug("加入前的判断耗时: " + time + "ms");
        }



        if(event.getEvent() instanceof DungeonStartEvent.After) {
            long startTime = System.currentTimeMillis();
            List<UUID> staminaList = takeStaminaMap.get(leaderUUID);
            HashMap<UUID, ItemStack> ticketMap = takeTicketMap.get(leaderUUID);

            for(Player player : playerList) {
                String playerName = player.getName();
                LogInfo.debug("--------------------------");
                LogInfo.debug("当前检测玩家为: " + playerName);
                UUID uuid = player.getUniqueId();
                PlayerData playerData = ConfigManager.dataMap.get(uuid);

                if(cost > 0 && staminaList.contains(uuid)) {
                    double newStamina = playerData.getStamina() - cost;
                    player.sendMessage(ConfigManager.msgMap.get("cost")
                            .replace("%cost%", String.valueOf(cost))
                            .replace("%dungeon%", customName)
                            .replace("%stamina%", String.valueOf(newStamina))
                    );
                    playerData.setStamina(newStamina);
                }

                // 扣除门票
                if(ticketMap.containsKey(uuid)) {
                    ItemStack ticketItem = ticketMap.get(uuid);
                    for(ItemStack item : player.getInventory().getContents()) {
                        if(item.equals(ticketItem)) {
                            player.sendMessage(ConfigManager.msgMap.get("consume")
                                    .replace("%ticket%", item.getItemMeta().getDisplayName())
                                    .replace("%dungeon%", customName)
                                    );
                            item.setAmount(item.getAmount()-1);
                            break;
                        }
                    }
                }

                // 消耗挑战次数
                MapCount mapCount = playerData.getMapCountMap().get(dungeonName);
                if(dayLimit > 0) mapCount.setDayCount(mapCount.getDayCount()+1);
                if(weekLimit > 0) mapCount.setWeekCount(mapCount.getWeekCount()+1);
                if(monthLimit > 0) mapCount.setMonthCount(mapCount.getMonthCount()+1);
            }

            takeStaminaMap.remove(leaderUUID);
            takeTicketMap.remove(leaderUUID);

            long finishTime = System.currentTimeMillis();
            long time = finishTime - startTime;
            LogInfo.debug("加入后的扣除耗时: " + time + "ms");
        }
    }
}


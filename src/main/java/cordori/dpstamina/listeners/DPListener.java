package cordori.dpstamina.listeners;

import cordori.dpstamina.dataManager.ConfigManager;
import cordori.dpstamina.objectManager.MapCount;
import cordori.dpstamina.objectManager.MapOption;
import cordori.dpstamina.objectManager.PlayerData;
import cordori.dpstamina.utils.LogInfo;

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnterDP(DungeonEvent event) {
        // 没有该地图的配置就不处理
        String dungeonName = event.getDungeon().getDungeonName();

        if (!ConfigManager.mapMap.containsKey(dungeonName)) return;

        Player leader = event.getDungeon().getTeam().getLeaderPlayer();
        MapOption mapOption = ConfigManager.mapMap.get(dungeonName);
        String customName = mapOption.getMapName();
        Team team = event.getDungeon().getTeam();
        List<Player> playerList = team.getPlayers(PlayerStateType.ALL);
        List<UUID> takeStaminaList = new ArrayList<>();
        HashMap<UUID, ItemStack> takeTicketMap = new HashMap<>();

        // 如果无体力设置和门票设置就不用判断
        boolean isNullTicket = (mapOption.getTicket().equals(""));
        double cost = mapOption.getCost();
        if(cost == 0 && isNullTicket) return;

        // 副本开始之前的检测
        if (event.getEvent() instanceof DungeonStartEvent.Before) {
            // 如果没有队长就禁止开始副本
            if(leader == null) {
                team.sendTeamMessage(ConfigManager.messagesMap.get("noLeader"));
                event.setCancelled(true);
                return;
            }

            boolean allowUniversal = mapOption.isAllowUniversal();

            String ticket = mapOption.getTicket();

            int dayLimit = mapOption.getDayLimit();
            int weekLimit = mapOption.getWeekLimit();
            int monthLimit = mapOption.getMonthLimit();

            // 遍历队伍成员
            for(Player player : playerList) {
                String playerName = player.getName();
                UUID uuid = player.getUniqueId();

                // 如果没有玩家数据，取消事件
                if(!ConfigManager.dataMap.containsKey(uuid)) {
                    team.sendTeamMessage(ConfigManager.messagesMap.get("noData").replace("%player%", playerName));
                    event.setCancelled(true);
                    return;
                }
                PlayerData playerData = ConfigManager.dataMap.get(uuid);

                // 先判断次数
                MapCount mapCount = playerData.getMapCountMap().get(dungeonName);
                int dayCount = mapCount.getDayCount();
                int weekCount = mapCount.getWeekCount();
                int monthCount = mapCount.getMonthCount();

                if(dayCount >= dayLimit || weekCount >= weekLimit || monthCount >= monthLimit) {
                    if(dayCount >= dayLimit) {
                        team.sendTeamMessage(ConfigManager.messagesMap.get("noDayCount")
                                .replace("%player%", playerName)
                                .replace("%dungeon%", customName)
                        );
                    }
                    if(weekCount >= weekLimit) {
                        team.sendTeamMessage(ConfigManager.messagesMap.get("noWeekCount")
                                .replace("%player%", playerName)
                                .replace("%dungeon%", customName)
                        );
                    }
                    if(monthCount >= monthLimit) {
                        team.sendTeamMessage(ConfigManager.messagesMap.get("noMonthCount")
                                .replace("%player%", playerName)
                                .replace("%dungeon%", customName)
                        );
                    }
                    event.setCancelled(true);
                    return;
                }

                // 如果没写门票配置，不用检查门票，判断体力就行了
                if(isNullTicket) {
                    // 如果体力消耗为-1，不判断体力，直接不让进了
                    if(cost == -1) {
                        team.sendTeamMessage(ConfigManager.messagesMap.get("allNull")
                                .replace("%dungeon%", customName));
                        event.setCancelled(true);
                        return;
                    };

                    double currentStamina = playerData.getStamina();
                    // 如果当前体力大于等于副本花费体力，加到扣除体力列表里
                    if(currentStamina >= cost) {
                        takeStaminaList.add(uuid);
                    } else {
                        team.sendTeamMessage(ConfigManager.messagesMap.get("noStamina")
                                .replace("%player%", playerName));
                        event.setCancelled(true);
                        return;
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
                                takeTicketMap.put(uuid, item);
                                LogInfo.debug("这是允许通票的专票");
                                break;
                            }
                            // 如果有通票
                            else if (name.equals(ConfigManager.universalTicket)) {
                                takeTicketMap.put(uuid, item);
                                LogInfo.debug("这是通票");
                            }

                        } else if (name.equals(ticket)) {
                            takeTicketMap.put(uuid, item);
                            LogInfo.debug("这是专票");
                            break;
                        }
                    }

                    // 如果玩家没有门票，判断是否有体力
                    if(!takeTicketMap.containsKey(uuid)) {
                        // 如果体力消耗为-1，直接终止
                        if(cost == -1) {
                            team.sendTeamMessage(ConfigManager.messagesMap.get("noTicket")
                                    .replace("%player%", playerName));
                            event.setCancelled(true);
                            return;
                        }
                        double currentStamina = playerData.getStamina();
                        // 如果当前体力大于等于副本花费体力，加到扣除体力列表里
                        if(currentStamina >= cost) {
                            takeStaminaList.add(uuid);
                        } else {
                            team.sendTeamMessage(ConfigManager.messagesMap.get("noTicket")
                                    .replace("%player%", playerName));
                            team.sendTeamMessage(ConfigManager.messagesMap.get("noStamina")
                                    .replace("%player%", playerName));
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        if(event.getEvent() instanceof DungeonStartEvent.Before) {
            for(Player player : playerList) {
                UUID uuid = player.getUniqueId();
                PlayerData playerData = ConfigManager.dataMap.get(uuid);
                // 扣除体力
                if(cost > 0 && takeStaminaList.contains(uuid)) {
                    double newStamina = playerData.getStamina() - cost;
                    playerData.setStamina(newStamina);
                    player.sendMessage(ConfigManager.messagesMap.get("cost")
                            .replace("%cost%", String.valueOf(cost))
                            .replace("%dungeon%", customName)
                            .replace("%stamina%", String.valueOf(newStamina))
                    );
                }

                // 扣除门票
                if(takeTicketMap.containsKey(uuid)) {
                    ItemStack ticketItem = takeTicketMap.get(uuid);
                    for(ItemStack item : player.getInventory().getContents()) {
                        if(item.equals(ticketItem)) {
                            item.setAmount(item.getAmount()-1);
                            player.sendMessage(ConfigManager.messagesMap.get("consume")
                                    .replace("%ticket%", item.getItemMeta().getDisplayName())
                                    .replace("%dungeon%", customName)
                                    );
                            break;
                        }
                    }
                }

                // 消耗挑战次数
                MapCount mapCount = playerData.getMapCountMap().get(dungeonName);
                mapCount.setDayCount(mapCount.getDayCount()+1);
                mapCount.setWeekCount(mapCount.getWeekCount()+1);
                mapCount.setMonthCount(mapCount.getMonthCount()+1);
                playerData.getMapCountMap().replace(dungeonName, mapCount);
            }
        }
    }
}


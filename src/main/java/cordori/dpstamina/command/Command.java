package cordori.dpstamina.command;

import cordori.dpstamina.Main;
import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.data.PlayerData;

import cordori.dpstamina.utils.CountProcess;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Command implements CommandExecutor, TabCompleter {

    private static final Main dps = Main.inst;

    private List<String> filter(List<String> list, String latest) {
        if (list.isEmpty() || latest == null)
            return list;
        String ll = latest.toLowerCase();
        list.removeIf(k -> !k.toLowerCase().startsWith(ll));
        return list;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        String latest = null;
        List<String> list = new ArrayList<>();
        if (args.length != 0) {
            latest = args[args.length - 1];
        }
        if (args.length == 1) {
            list.add("reload");
            list.add("help");
            list.add("give");
            list.add("take");
            list.add("set");
            list.add("task");
        } else if (args.length == 2) {
            String playerName = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(playerName)) {
                    list.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            list.add("dayCount");
            list.add("weekCount");
            list.add("monthCount");
        } else if (args.length == 4) {
            list.addAll(ConfigManager.mapMap.keySet());
        }
        return filter(list, latest);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(dps, () -> {
            if (args.length == 0) {
                sender.sendMessage("§e[副本体力]§6==============================");
                sender.sendMessage("§e[副本体力]§b/dps help - 查看指令与描述");
                sender.sendMessage("§e[副本体力]§b/dps reload - 重载插件配置");
                sender.sendMessage("§e[副本体力]§b/dps task - 重置定时任务");

                sender.sendMessage("§e[副本体力]§b/dps give 玩家名称 数值 - 给予玩家体力值");
                sender.sendMessage("§e[副本体力]§b/dps take 玩家名称 数值 - 扣除玩家体力值");
                sender.sendMessage("§e[副本体力]§b/dps set 玩家名称 数值 - 设置玩家体力值");

                sender.sendMessage("§e[副本体力]§b/dps give 玩家名称 每日/每周/每月次数 副本节点名 数值");
                sender.sendMessage("§e[副本体力]  §f- 增加玩家挑战次数，无法超过上限");
                sender.sendMessage("§e[副本体力]§b/dps take 玩家名称 每日/每周/每月次数 副本节点名 数值");
                sender.sendMessage("§e[副本体力]  §f- 减少玩家挑战次数，无法低于0");
                sender.sendMessage("§e[副本体力]§b/dps set 玩家名称 每日/每周/每月次数 副本节点名 数值");
                sender.sendMessage("§e[副本体力]  §f- 设置玩家对应类型次数");
                sender.sendMessage("§e[副本体力]§6==============================");
            }

            else if (args[0].equalsIgnoreCase("reload")) {
                ConfigManager.reloadMyConfig();
                CountProcess.reloadAllCount();
                sender.sendMessage(ConfigManager.msgMap.get("reload"));
            }

            else if (args[0].equalsIgnoreCase("task")) {
                ConfigManager.startTasks();
                sender.sendMessage(ConfigManager.msgMap.get("task"));
            }

            else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§e[副本体力]§6==============================");
                sender.sendMessage("§e[副本体力]§b/dps help - 查看指令与描述");
                sender.sendMessage("§e[副本体力]§b/dps reload - 重载插件配置");
                sender.sendMessage("§e[副本体力]§b/dps task - 重置定时任务");

                sender.sendMessage("§e[副本体力]§b/dps give 玩家名称 数值 - 给予玩家体力值");
                sender.sendMessage("§e[副本体力]§b/dps take 玩家名称 数值 - 扣除玩家体力值");
                sender.sendMessage("§e[副本体力]§b/dps set 玩家名称 数值 - 设置玩家体力值");

                sender.sendMessage("§e[副本体力]§b/dps give 玩家名称 每日/每周/每月次数 副本节点名 数值");
                sender.sendMessage("§e[副本体力]  §f- 增加玩家挑战次数，无法超过上限");
                sender.sendMessage("§e[副本体力]§b/dps take 玩家名称 每日/每周/每月次数 副本节点名 数值");
                sender.sendMessage("§e[副本体力]  §f- 减少玩家挑战次数，无法低于0");
                sender.sendMessage("§e[副本体力]§b/dps set 玩家名称 每日/每周/每月次数 副本节点名 数值");
                sender.sendMessage("§e[副本体力]  §f- 设置玩家对应类型次数");
                sender.sendMessage("§e[副本体力]§6==============================");
            }

            else if (args[0].equalsIgnoreCase("set") ||
                    args[0].equalsIgnoreCase("give") ||
                    args[0].equalsIgnoreCase("take")) {

                if (args.length <= 2) {
                    sender.sendMessage("§e[副本体力]§c参数不足捏~");
                    return;
                }

                String playerName = args[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player == null) {
                    sender.sendMessage("§e[副本体力]§c该玩家不在线！");
                    return;
                }

                double number = 0;
                boolean isNumber;
                try {
                    number = Double.parseDouble(args[2]);
                    isNumber = true;
                } catch (NumberFormatException e) {
                    isNumber = false;
                }
                UUID uuid = player.getUniqueId();
                if(!ConfigManager.dataMap.containsKey(uuid)) {
                    sender.sendMessage(ConfigManager.msgMap.get("noData").replace("%player%", playerName));
                    return;
                }
                PlayerData playerData = ConfigManager.dataMap.get(uuid);
                double stamina = playerData.getStamina();
                double limit = ConfigManager.groupMap.get(ConfigManager.getGroup(player)).getLimit();

                if(isNumber) {
                    switch (args[0].toLowerCase()) {
                        case "set":
                            double num = Math.min(number, limit);
                            if(number < 0) num = 0;
                            String message1 = ConfigManager.msgMap.get("set")
                                    .replaceAll("%player%", playerName)
                                    .replaceAll("%num%", String.valueOf(num)
                                    );
                            modifyStamina(sender, uuid, num, message1);
                            break;

                        case "give":
                            if(number < 0) number = 0;
                            double newStamina = Math.min(stamina + number, limit);
                            double ns;
                            if(stamina + number > limit) {
                                ns = limit - stamina;
                            } else {
                                ns = number;
                            }
                            String message2 = ConfigManager.msgMap.get("give")
                                    .replaceAll("%player%", playerName)
                                    .replaceAll("%num%", String.valueOf(ns))
                                    .replaceAll("%stamina%", String.valueOf(newStamina)
                                    );
                            modifyStamina(sender, uuid, newStamina, message2);
                            break;

                        case "take":
                            if(number < 0) number = 0;
                            double reducedStamina = Math.max(stamina - number, 0);
                            double rs;
                            if(stamina - number < 0) {
                                rs = stamina;
                            } else {
                                rs = number;
                            }
                            String message3 = ConfigManager.msgMap.get("take")
                                    .replaceAll("%player%", playerName)
                                    .replaceAll("%num%", String.valueOf(rs))
                                    .replaceAll("%stamina%", String.valueOf(reducedStamina)
                                    );
                            modifyStamina(sender, uuid, reducedStamina, message3);
                            break;

                        default:
                            sender.sendMessage("§e[副本体力]§c无效的参数！");
                    }
                } else {
                    if(args.length <= 4) {
                        sender.sendMessage("§e[副本体力]§c参数不足捏~");
                        return;
                    }

                    if(args[2].equalsIgnoreCase("dayCount") ||
                            args[2].equalsIgnoreCase("weekCount") ||
                            args[2].equalsIgnoreCase("monthCount")) {

                        if(ConfigManager.mapMap.containsKey(args[3])) {
                            int value;
                            try {
                                value = Integer.parseInt(args[4]);
                                String customName = ConfigManager.mapMap.get(args[3]).getMapName();
                                modifyCount(sender, args[0], args[2], args[3], value, playerData, playerName, customName);
                            } catch (NumberFormatException e) {
                                sender.sendMessage("§e[副本体力]§c请输入数字~");
                            }
                        }
                    } else {
                        sender.sendMessage("§e[副本体力]§c无效的参数~");
                    }
                }

            }

            else {
                sender.sendMessage("§e[副本体力]§c无效的指令！");
            }
        });
        return false;
    }

    private void modifyStamina(CommandSender sender, UUID uuid, double newStamina, String message) {
        ConfigManager.dataMap.get(uuid).setStamina(newStamina);
        sender.sendMessage(message);
    }

    private void modifyCount(CommandSender sender, String type, String action, String key, int value,
                             PlayerData playerData, String playerName, String customName) {

        if(value < 0) {
            sender.sendMessage("§e[副本体力]§c请输入大于等于0的数字！");
            return;
        }

        if(type.equalsIgnoreCase("give")) {
            if(action.equalsIgnoreCase("dayCount")) {
                int count = playerData.getMapCountMap().get(key).getDayCount();
                int newCount = count - value;
                if(newCount < 0) newCount = 0;
                int limit = ConfigManager.mapMap.get(key).getDayLimit();
                if(value > limit) value = limit;
                playerData.getMapCountMap().get(key).setDayCount(newCount);
                sender.sendMessage(ConfigManager.msgMap.get("giveDayCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(value))
                );
            }

            else if(action.equalsIgnoreCase("weekCount")) {
                int count = playerData.getMapCountMap().get(key).getWeekCount();
                int newCount = count - value;
                if(newCount < 0) newCount = 0;
                int limit = ConfigManager.mapMap.get(key).getWeekLimit();
                if(value > limit) value = limit;
                playerData.getMapCountMap().get(key).setWeekCount(newCount);
                sender.sendMessage(ConfigManager.msgMap.get("giveWeekCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(value))
                );
            }

            else if(action.equalsIgnoreCase("monthCount")) {
                int count = playerData.getMapCountMap().get(key).getMonthCount();
                int newCount = count - value;
                if(newCount < 0) newCount = 0;
                int limit = ConfigManager.mapMap.get(key).getMonthLimit();
                if(value > limit) value = limit;
                playerData.getMapCountMap().get(key).setMonthCount(newCount);
                sender.sendMessage(ConfigManager.msgMap.get("giveMonthCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(value))
                );
            }

        }

        if(type.equalsIgnoreCase("take")) {

            if(action.equalsIgnoreCase("dayCount")) {
                int limit = ConfigManager.mapMap.get(key).getDayLimit();
                int count = playerData.getMapCountMap().get(key).getDayCount();
                int newCount = count + value;
                if(newCount > limit) newCount = limit;
                playerData.getMapCountMap().get(key).setDayCount(newCount);
                sender.sendMessage(ConfigManager.msgMap.get("takeDayCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(newCount))
                );
            }

            else if(action.equalsIgnoreCase("weekCount")) {
                int limit = ConfigManager.mapMap.get(key).getWeekLimit();
                int count = playerData.getMapCountMap().get(key).getWeekCount();
                int newCount = count + value;
                if(newCount > limit) newCount = limit;
                playerData.getMapCountMap().get(key).setWeekCount(newCount);
                sender.sendMessage(ConfigManager.msgMap.get("takeWeekCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(newCount))
                );
            }

            else if(action.equalsIgnoreCase("monthCount")) {
                int limit = ConfigManager.mapMap.get(key).getMonthLimit();
                int count = playerData.getMapCountMap().get(key).getMonthCount();
                int newCount = count + value;
                if(newCount > limit) newCount = limit;
                playerData.getMapCountMap().get(key).setMonthCount(newCount);
                sender.sendMessage(ConfigManager.msgMap.get("takeMonthCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(newCount))
                );
            }
        }

        if(type.equalsIgnoreCase("set")) {

            if(action.equalsIgnoreCase("dayCount")) {
                int limit = ConfigManager.mapMap.get(key).getDayLimit();
                if(value > limit) value = limit;
                if(value < 0) value = 0;
                playerData.getMapCountMap().get(key).setDayCount(value);
                sender.sendMessage(ConfigManager.msgMap.get("setDayCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(value))
                );
            }

            else if(action.equalsIgnoreCase("weekCount")) {
                int limit = ConfigManager.mapMap.get(key).getWeekLimit();
                if(value > limit) value = limit;
                if(value < 0) value = 0;
                playerData.getMapCountMap().get(key).setWeekCount(value);
                sender.sendMessage(ConfigManager.msgMap.get("setWeekCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(value))
                );
            }

            else if(action.equalsIgnoreCase("monthCount")) {
                int limit = ConfigManager.mapMap.get(key).getMonthLimit();
                if(value > limit) value = limit;
                if(value < 0) value = 0;
                playerData.getMapCountMap().get(key).setMonthCount(value);
                sender.sendMessage(ConfigManager.msgMap.get("setMonthCount")
                        .replace("%player%", playerName)
                        .replace("%dungeon%", customName)
                        .replace("%value%", String.valueOf(value))
                );
            }
        }

    }
}

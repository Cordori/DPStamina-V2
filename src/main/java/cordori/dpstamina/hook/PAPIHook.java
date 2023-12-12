package cordori.dpstamina.hook;


import cordori.dpstamina.Main;
import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.data.PlayerData;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PAPIHook extends PlaceholderExpansion {

    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public boolean canRegister() {
        return true;
    }
    @Override
    public String getAuthor() {
        return "Cordori";
    }
    @Override
    public String getVersion() { return Main.inst.getDescription().getVersion(); }
    @Override
    public String getIdentifier() {
        return "dps";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (player == null || identifier == null || identifier.isEmpty()) return null;
        UUID uuid = player.getUniqueId();
        if(!ConfigManager.dataMap.containsKey(uuid)) return null;
        PlayerData playerData = ConfigManager.dataMap.get(uuid);

        if(identifier.equalsIgnoreCase("stamina")) {
            double stamina = playerData.getStamina();
            return String.valueOf(stamina);
        }

        else if(identifier.equalsIgnoreCase("limit")) {
            String group = ConfigManager.getGroup(player);
            double limit = ConfigManager.groupMap.get(group).getLimit();
            return String.valueOf(limit);
        }

        else if(identifier.equalsIgnoreCase("recover")) {
            String group = ConfigManager.getGroup(player);
            String recover = ConfigManager.groupMap.get(group).getRecover();
            return PAPIHook.onPAPIProcess(player, recover);
        }

        else if(identifier.equalsIgnoreCase("group")) {
            return ConfigManager.getGroup(player);
        }

        else if(identifier.equalsIgnoreCase("universalTicket")) {
            return ConfigManager.universalTicket;
        }



        String[] strings = identifier.split("_");
        if(strings.length < 2) return null;
        if(ConfigManager.mapMap.containsKey(strings[0])) {
            String key = strings[0];
            String type = strings[1];
            if(type.equalsIgnoreCase("useDayCount")) {
                int count = playerData.getMapCountMap().get(key).getDayCount();
                return String.valueOf(count);
            }

            else if(type.equalsIgnoreCase("useWeekCount")) {
                int count = playerData.getMapCountMap().get(key).getWeekCount();
                return String.valueOf(count);
            }

            else if(type.equalsIgnoreCase("useMonthCount")) {
                int count = playerData.getMapCountMap().get(key).getMonthCount();
                return String.valueOf(count);
            }

            else if(type.equalsIgnoreCase("remainDayCount")) {
                int count = playerData.getMapCountMap().get(key).getDayCount();
                int limit = ConfigManager.mapMap.get(key).getDayLimit();
                int value = limit - count;
                return String.valueOf(value);
            }

            else if(strings[1].equalsIgnoreCase("remainWeekCount")) {
                int count = playerData.getMapCountMap().get(key).getWeekCount();
                int limit = ConfigManager.mapMap.get(key).getWeekLimit();
                int value = limit - count;
                return String.valueOf(value);
            }

            else if(strings[1].equalsIgnoreCase("remainMonthCount")) {
                int count = playerData.getMapCountMap().get(key).getMonthCount();
                int limit = ConfigManager.mapMap.get(key).getMonthLimit();
                int value = limit - count;
                return String.valueOf(value);
            }

            else if(type.equalsIgnoreCase("dayLimit")) {
                int limit = ConfigManager.mapMap.get(key).getDayLimit();
                return String.valueOf(limit);
            }

            else if(strings[1].equalsIgnoreCase("weekLimit")) {
                int limit = ConfigManager.mapMap.get(key).getWeekLimit();
                return String.valueOf(limit);
            }

            else if(strings[1].equalsIgnoreCase("monthLimit")) {
                int limit = ConfigManager.mapMap.get(key).getMonthLimit();
                return String.valueOf(limit);
            }

            else if(type.equalsIgnoreCase("cost")) {
                double cost = ConfigManager.mapMap.get(key).getCost();
                return String.valueOf(cost);
            }

            else if(strings[1].equalsIgnoreCase("mapName")) {
                return ConfigManager.mapMap.get(key).getMapName();
            }

            else if(strings[1].equalsIgnoreCase("ticket")) {
                return ConfigManager.mapMap.get(key).getTicket();
            }

            else if(strings[1].equalsIgnoreCase("allowUniversal")) {
                return String.valueOf(ConfigManager.mapMap.get(key).isAllowUniversal());
            }
        }

        return null;
    }

    public static String onPAPIProcess(Player player, String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }

}

package cordori.dpstamina.api;

import cordori.dpstamina.data.PlayerData;
import cordori.dpstamina.manager.ConfigManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StaminaAPI {

    /**
     *
     * @param uuid 玩家uuid
     * @return 玩家数据，存储着体力、挑战次数、离线时间
     */
    public PlayerData getData(UUID uuid) {
        return ConfigManager.dataMap.getOrDefault(uuid, null);
    }

    public boolean setStamina(Player player, double value) {
        UUID uuid = player.getUniqueId();
        if(ConfigManager.dataMap.containsKey(uuid)) {
            PlayerData data = ConfigManager.dataMap.get(uuid);
            String group = ConfigManager.getGroup(player);
            double limit = ConfigManager.groupMap.get(group).getLimit();
            double newStamina = Math.min(value, limit);
            if(value < 0) newStamina = 0;
            data.setStamina(newStamina);
            return true;
        }
        return false;
    }

    public boolean giveStamina(Player player, double number) {
        UUID uuid = player.getUniqueId();
        if(ConfigManager.dataMap.containsKey(uuid)) {
            PlayerData data = ConfigManager.dataMap.get(uuid);
            String group = ConfigManager.getGroup(player);
            double limit = ConfigManager.groupMap.get(group).getLimit();
            double stamina = data.getStamina();
            if(number < 0) number = 0;
            double newStamina = Math.min(stamina + number, limit);
            data.setStamina(newStamina);
            return true;
        }
        return false;
    }

    public boolean takeStamina(Player player, double number) {
        UUID uuid = player.getUniqueId();
        if(ConfigManager.dataMap.containsKey(uuid)) {
            PlayerData data = ConfigManager.dataMap.get(uuid);
            double stamina = data.getStamina();
            if(number < 0) number = 0;
            double newStamina = Math.max(stamina - number, 0);
            data.setStamina(newStamina);
            return true;
        }
        return false;
    }

}

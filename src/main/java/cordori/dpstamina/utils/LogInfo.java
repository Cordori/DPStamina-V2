package cordori.dpstamina.utils;


import cordori.dpstamina.Main;
import cordori.dpstamina.manager.ConfigManager;

public class LogInfo {

    public static void debug(String str) {
        if(ConfigManager.debug) {
            Main.inst.getLogger().info(str);
        }

    }


}

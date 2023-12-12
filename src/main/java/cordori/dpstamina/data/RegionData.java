package cordori.dpstamina.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Getter @RequiredArgsConstructor
public class RegionData {

    private final String[] pos1;
    private final String[] pos2;

    public static HashMap<String, RegionData> regionsMap = new HashMap<>();

    public static boolean isPlayerInRegion(Player player) {

        // 获取玩家当前的位置坐标
        Location playerLoc = player.getLocation();
        String world = playerLoc.getWorld().getName();

        if(!regionsMap.containsKey(world)) return false;

        double playerX = playerLoc.getX();
        double playerY = playerLoc.getY();
        double playerZ = playerLoc.getZ();

        for (String worldName : regionsMap.keySet()) {

            if(!worldName.equals(world)) continue;

            String[] pos1 = regionsMap.get(worldName).pos1;
            String[] pos2 = regionsMap.get(worldName).pos2;

            // 比较玩家的坐标是否在区域内
            double x1 = Double.parseDouble(pos1[0]);
            double y1 = Double.parseDouble(pos1[1]);
            double z1 = Double.parseDouble(pos1[2]);
            double x2 = Double.parseDouble(pos2[0]);
            double y2 = Double.parseDouble(pos2[1]);
            double z2 = Double.parseDouble(pos2[2]);

            double minX = Math.min(x1, x2);
            double minY = Math.min(y1, y2);
            double minZ = Math.min(z1, z2);
            double maxX = Math.max(x1, x2);
            double maxY = Math.max(y1, y2);
            double maxZ = Math.max(z1, z2);

            return playerX >= minX && playerX <= maxX &&
                    playerY >= minY && playerY <= maxY &&
                    playerZ >= minZ && playerZ <= maxZ;
        }

        return false;
    }
}

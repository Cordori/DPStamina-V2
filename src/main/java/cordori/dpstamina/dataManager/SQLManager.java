package cordori.dpstamina.dataManager;

import cordori.dpstamina.Main;
import cordori.dpstamina.objectManager.PlayerData;
import cordori.dpstamina.utils.CountProcess;

import cordori.dpstamina.utils.LogInfo;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;

import java.sql.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class SQLManager {
    private final String tableName;
    public static SQLManager sql;
    @Getter
    private final BasicDataSource dataSource = new BasicDataSource();

    @SneakyThrows
    public SQLManager(String url, String username, String password, String driver, String tableName) {
        this.tableName = tableName;
        Class.forName(driver);
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);

        if(Main.MySQL) {
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        }
    }

    @SneakyThrows
    public Connection getConnection() {
        return dataSource.getConnection();
    }

    @SneakyThrows
    public void disconnect() {
        getConnection().close();
    }

    @SneakyThrows
    public void createTable() {
        @Cleanup Connection connection = getConnection();
        @Cleanup Statement statement = connection.createStatement();
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "uuid VARCHAR(255) PRIMARY KEY, "
                + "stamina DOUBLE, "
                + "offlineTime BIGINT, "
                + "dayRecord DATE, "
                + "weekRecord INT, "
                + "monthRecord INT, "
                + "mapCount VARCHAR)";
        statement.executeUpdate(query);

    }

    @SneakyThrows
    public List<Object> getData(UUID uuid) {
        List<Object> objectList = new ArrayList<>();
        @Cleanup Connection conn = getConnection();
        String selectQuery = "SELECT stamina, offlineTime, dayRecord, weekRecord, monthRecord, mapCount FROM " + tableName + " WHERE uuid = ?";
        @Cleanup PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
        selectStmt.setString(1, uuid.toString());
        @Cleanup ResultSet rs = selectStmt.executeQuery();
        if (rs.next()) {
            double stamina = rs.getDouble("stamina");
            long offlineTime = rs.getLong("offlineTime");
            int dayRecord = rs.getInt("dayRecord");
            int weekRecord = rs.getInt("weekRecord");
            int monthRecord = rs.getInt("monthRecord");
            String mapCount = rs.getString("mapCount");
            objectList.add(stamina);
            objectList.add(offlineTime);
            objectList.add(dayRecord);
            objectList.add(weekRecord);
            objectList.add(monthRecord);
            objectList.add(mapCount);
        } else {
            objectList = null;
        }
        return objectList;
    }

    @SneakyThrows
    public void insertNewData(UUID uuid) {
        String group = ConfigManager.getGroup(Bukkit.getPlayer(uuid));
        double limit = ConfigManager.groupMap.get(group).getLimit();
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalTime refreshTime = LocalTime.parse(ConfigManager.refreshTime);

        int dayRecord = 0;
        if (currentTime.isAfter(refreshTime)) dayRecord = dayOfMonth;

        int weekRecord = 0;
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek >= ConfigManager.refreshWeek) weekRecord = dayOfWeek;

        int monthRecord = 0;
        int currentMonth = LocalDate.now().getMonthValue();
        if(dayOfMonth >= ConfigManager.refreshMonth) monthRecord = currentMonth;

        StringBuilder sb = new StringBuilder();
        LogInfo.debug("【首次加载数据mapMap】" + ConfigManager.mapMap);
        for(String key : ConfigManager.mapMap.keySet()) {
            int dayCount = 0;
            int weeCount = 0;
            int monthCount = 0;
            String str = key + "," + dayCount + "," + weeCount + "," + monthCount + ";";
            sb.append(str);
        }
        LogInfo.debug("【首次加载数据mapCount】" + sb);

        @Cleanup Connection conn = getConnection();
        String insertQuery = "INSERT INTO " + tableName + " (uuid, stamina, offlineTime, dayRecord, weekRecord, monthRecord, mapCount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.setString(1, uuid.toString());
        insertStmt.setDouble(2, limit);
        insertStmt.setLong(3, System.currentTimeMillis());
        insertStmt.setInt(4, dayRecord);
        insertStmt.setInt(5, weekRecord);
        insertStmt.setInt(6, monthRecord);
        insertStmt.setString(7, sb.toString());
        insertStmt.executeUpdate();

        ConfigManager.dataMap.put(uuid, new PlayerData(limit, dayRecord, weekRecord, monthRecord, new HashMap<>()));
        CountProcess.strToCount(uuid, sb.toString());
    }

    @SneakyThrows
    public void saveData(UUID uuid, PlayerData playerData) {
        double stamina = playerData.getStamina();
        long offlineTime = System.currentTimeMillis();
        int dayRecord = playerData.getDayRecord();
        int weekRecord = playerData.getWeekRecord();
        int monthRecord = playerData.getMonthRecord();
        String mapCount = CountProcess.countToStr(uuid, "");
        LogInfo.debug("[退服保存数据mapCount]" + mapCount);
        @Cleanup Connection conn = getConnection();
        String updateQuery = "UPDATE " + tableName + " SET stamina = ?, offlineTime = ?, dayRecord = ?," +
                " weekRecord = ?, monthRecord = ?, mapCount = ? WHERE uuid = ?";
        @Cleanup PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
        updateStmt.setDouble(1, stamina);
        updateStmt.setLong(2, offlineTime);
        updateStmt.setInt(3, dayRecord);
        updateStmt.setInt(4, weekRecord);
        updateStmt.setInt(5, monthRecord);
        updateStmt.setString(6, mapCount);
        updateStmt.setString(7, uuid.toString());
        updateStmt.executeUpdate();
    }

    @SneakyThrows
    public void refreshData(UUID uuid, String data, int record) {
        @Cleanup Connection conn = getConnection();
        String updateQuery = "UPDATE " + tableName + " SET " + data + " = ? WHERE uuid = ?";
        @Cleanup PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
        updateStmt.setInt(1, record);
        updateStmt.setString(2, uuid.toString());
        updateStmt.executeUpdate();
    }
}

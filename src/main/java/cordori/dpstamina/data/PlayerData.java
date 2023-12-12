package cordori.dpstamina.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter @Setter @RequiredArgsConstructor
public class PlayerData {
    private Double stamina;
    private long offlineTime;
    private int dayRecord;
    private int weekRecord;
    private int monthRecord;
    private HashMap<String, MapCount> mapCountMap;

    public PlayerData(Double stamina, long offlineTime, int dayRecord, int weekRecord, int monthRecord, HashMap<String, MapCount> mapCountMap) {
        this.stamina = stamina;
        this.offlineTime = offlineTime;
        this.dayRecord = dayRecord;
        this.weekRecord = weekRecord;
        this.monthRecord = monthRecord;
        this.mapCountMap = mapCountMap;
    }

}

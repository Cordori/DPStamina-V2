package cordori.dpstamina.objectManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter @Setter @RequiredArgsConstructor
public class PlayerData {
    private Double stamina;
    private int dayRecord;
    private int weekRecord;
    private int monthRecord;
    private HashMap<String, MapCount> mapCountMap;

    public PlayerData(Double stamina, int dayRecord, int weekRecord, int monthRecord, HashMap<String, MapCount> mapCountMap) {
        this.stamina = stamina;
        this.dayRecord = dayRecord;
        this.weekRecord = weekRecord;
        this.monthRecord = monthRecord;
        this.mapCountMap = mapCountMap;
    }

}

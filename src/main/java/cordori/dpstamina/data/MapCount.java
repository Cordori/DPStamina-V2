package cordori.dpstamina.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MapCount {
    private int dayCount;
    private int weekCount;
    private int monthCount;

    public MapCount(int dayCount, int weekCount, int monthCount) {
        this.dayCount = dayCount;
        this.weekCount = weekCount;
        this.monthCount = monthCount;
    }
}

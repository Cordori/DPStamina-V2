package cordori.dpstamina.objectManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public class MapOption {
    private final String mapName;
    private final double cost;
    private final String ticket;
    private final boolean allowUniversal;
    private final int dayLimit;
    private final int weekLimit;
    private final int monthLimit;
}

package cordori.dpstamina.objectManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public class GroupData {
    private final String groupName;
    private final String perm;
    private final Double limit;
    private final String recover;
}

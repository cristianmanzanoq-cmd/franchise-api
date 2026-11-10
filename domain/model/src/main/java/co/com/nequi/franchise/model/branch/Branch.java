package co.com.nequi.franchise.model.branch;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Branch {
    private final String id;
    private final String franchiseId;
    private final String name;
}

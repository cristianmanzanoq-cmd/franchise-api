package co.com.nequi.franchise.model.franchise;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Franchise {
    private final String id;
    private final String name;
}

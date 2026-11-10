package co.com.nequi.franchise.model.product;

import co.com.nequi.franchise.model.branch.Branch;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class TopStockProduct {
    private final Branch branch;
    private final Product product;
}

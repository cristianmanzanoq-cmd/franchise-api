package co.com.nequi.franchise.model.product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Product {
    private final String id;
    private final String branchId;
    private final String name;
    private final Integer stock;
}

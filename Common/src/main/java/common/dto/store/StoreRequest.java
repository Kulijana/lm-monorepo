package common.dto.store;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StoreRequest {
    private String tid;
    private String customerId;
    private String productId;
    private int amount;

//    TODO remove this property, its only here for testing purposes
    private int timeToProcess;
}

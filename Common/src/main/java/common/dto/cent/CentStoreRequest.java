package common.dto.cent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CentStoreRequest {
    private String customerId;
//    private String productId;
    private int amount;
    ArrayList<String> productsToBuy;
    private int timeout;
}

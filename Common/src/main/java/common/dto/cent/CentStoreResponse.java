package common.dto.cent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentStoreResponse {
    private boolean allowed;
    private ArrayList<Integer> amount;
}

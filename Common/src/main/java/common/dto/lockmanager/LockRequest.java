package common.dto.lockmanager;

import common.LockType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LockRequest {
    private String tid;
    private String dbid;
    private LockType type;
}

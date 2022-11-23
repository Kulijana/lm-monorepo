package common.dto;

public class LockRequest {
    public String TID;
    public String DBID;
    public LockType type;

    public LockRequest(){ }

    public LockRequest(String TID, String DBID, LockType type){
        this.TID = TID;
        this.DBID = DBID;
        this.type = type;
    }
}

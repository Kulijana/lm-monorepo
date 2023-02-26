package com.master.lockerroom;

import common.dto.LockType;

public class WriteLock extends Lock{
    public String TID;

    public WriteLock(String TID){
        this.type = LockType.EXCLUSIVE;
        this.TID = TID;
    }
}

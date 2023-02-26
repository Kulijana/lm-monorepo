package com.master.lockerroom;

import common.dto.LockType;

import java.util.ArrayList;

public class ReadLock extends Lock{
    public ArrayList<String> TIDs;

    public ReadLock(String TID){
        this.type = LockType.SHARED;
        this.TIDs = new ArrayList<>();
        this.TIDs.add(TID);
    }
}

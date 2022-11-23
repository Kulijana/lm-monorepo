package com.master.lockerroom;

import common.dto.LockRequest;
import common.dto.LockType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class LockerRoom {
//    first parameter is DBID
    private HashMap<String, Lock> locks;
//    first parameter is TID, second is list of DBIDs
    private HashMap<String, ArrayList<String>> T2DBMap;


    public LockerRoom(){
        this.locks = new HashMap<>();
        this.T2DBMap = new HashMap<>();
    }
    public boolean lockable(LockRequest request) {
        if (!locks.containsKey(request.DBID)) {
            return true;
        }
        Lock currentLock = locks.get(request.DBID);
        if (currentLock.type == LockType.READ && request.type == LockType.READ) {
            return true;
//            checking for upgradeability
//            TODO fix code repetition that occurs in locking compared to this part
        } else if(currentLock.type == LockType.READ && request.type == LockType.WRITE){
            ReadLock readLock = (ReadLock) currentLock;
            if(readLock.TIDs.size() == 1 && readLock.TIDs.get(0).equals(request.TID))
                return true;
            else
                return false;

        } else {
            return false;
        }
    }

    public boolean unlockable(LockRequest request){
        return T2DBMap.containsKey(request.TID);
    }

    public synchronized boolean lock(LockRequest request){


        boolean lockable = lockable(request);
        if(lockable){
            if(!locks.containsKey(request.DBID)){
                if(request.type == LockType.READ){
                    ReadLock lock= new ReadLock(request.TID);
                    locks.put(request.DBID, lock);
                    addToT2DBMap(request);
                }else{
                    WriteLock lock = new WriteLock(request.TID);
                    locks.put(request.DBID, lock);
                    addToT2DBMap(request);
                }
            }else{
                Lock currentLock = locks.get(request.DBID);
                if(currentLock.type == LockType.READ && request.type == LockType.READ){
                    ReadLock readLock = (ReadLock) currentLock;
                    readLock.TIDs.add(request.TID);
                    addToT2DBMap(request);
//                    Following part is about lock upgrades, developer needs to take care
//                    As his code needs to be responsible for knowing about the upgrade, for now
//                    no adding to T2DBMap because the item is already related to the transaction
                }else if(currentLock.type == LockType.READ && request.type == LockType.WRITE){
                    ReadLock readLock = (ReadLock) currentLock;
                    if(readLock.TIDs.size() == 1 && readLock.TIDs.get(0) == request.TID){
                        locks.replace(request.DBID, currentLock, new WriteLock(request.TID));
                    }
                }
            }
        }
        return lockable;
    }

    public boolean unlock(LockRequest request){
        boolean unlockable = unlockable(request);
        if(unlockable) {
            ArrayList<String> transactionLocks = T2DBMap.get(request.TID);
            for (String DBID : transactionLocks) {
                Lock currentLock = locks.get(DBID);
                if (currentLock.type == LockType.WRITE) {
                    locks.remove(DBID);
                } else {
                    ReadLock readLock = (ReadLock) currentLock;
                    if (readLock.TIDs.size() == 1) {
                        locks.remove(DBID);
                    } else {
                        readLock.TIDs.remove(request.TID);
                    }
                }
            }
//            No real need for second parameter, more for code readability
            T2DBMap.remove(request.TID, transactionLocks);
        }
        return unlockable;
    }

    private void addToT2DBMap(LockRequest request){
        if(!T2DBMap.containsKey(request.TID)) {
            ArrayList<String> DBIDs = new ArrayList<>();
            DBIDs.add(request.DBID);
            T2DBMap.put(request.TID, DBIDs);
        }else{
            T2DBMap.get(request.TID).add(request.DBID);
        }
    }
}

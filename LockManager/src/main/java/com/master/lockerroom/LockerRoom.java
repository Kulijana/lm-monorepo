package com.master.lockerroom;

import common.dto.LockRequest;
import common.dto.LockType;

import java.util.ArrayList;
import java.util.HashMap;

public class LockerRoom implements LockManager{
//    first parameter is DBID
    private HashMap<String, Lock> locks;
//    first parameter is TID, second is list of DBIDs
    private HashMap<String, ArrayList<String>> T2DBMap;

//    TODO make a tree that figures out how much stuff is locked, split by / preferably

    public LockerRoom(){
        this.locks = new HashMap<>();
        this.T2DBMap = new HashMap<>();
    }
    private boolean lockable(LockRequest request) {
        if (!locks.containsKey(request.getDbid())) {
            return true;
        }
        Lock currentLock = locks.get(request.getDbid());
        if (currentLock.type == LockType.SHARED && request.getType() == LockType.SHARED) {
            return true;
//            checking for upgradeability
//            TODO fix code repetition that occurs in locking compared to this part
        } else if(currentLock.type == LockType.SHARED && request.getType() == LockType.EXCLUSIVE){
            ReadLock readLock = (ReadLock) currentLock;
            if(readLock.TIDs.size() == 1 && readLock.TIDs.get(0).equals(request.getTid()))
                return true;
            else
                return false;

        } else {
            return false;
        }
    }

    private boolean unlockable(LockRequest request){
        return T2DBMap.containsKey(request.getTid());
    }

    public synchronized boolean lock(LockRequest request){
        boolean lockable = lockable(request);
        if(lockable){
            if(!locks.containsKey(request.getDbid())){
                if(request.getType() == LockType.SHARED){
                    ReadLock lock= new ReadLock(request.getTid());
                    locks.put(request.getDbid(), lock);
                    addToT2DBMap(request);
                }else{
                    WriteLock lock = new WriteLock(request.getTid());
                    locks.put(request.getDbid(), lock);
                    addToT2DBMap(request);
                }
            }else{
                Lock currentLock = locks.get(request.getDbid());
                if(currentLock.type == LockType.SHARED && request.getType() == LockType.SHARED){
                    ReadLock readLock = (ReadLock) currentLock;
                    readLock.TIDs.add(request.getTid());
                    addToT2DBMap(request);
//                    Following part is about lock upgrades, developer needs to take care
//                    As his code needs to be responsible for knowing about the upgrade, for now
//                    no adding to T2DBMap because the item is already related to the transaction
                }else if(currentLock.type == LockType.SHARED && request.getType() == LockType.EXCLUSIVE){
                    ReadLock readLock = (ReadLock) currentLock;
                    if(readLock.TIDs.size() == 1 && readLock.TIDs.get(0) == request.getTid()){
                        locks.replace(request.getDbid(), currentLock, new WriteLock(request.getTid()));
                    }
                }
            }
        }
        return lockable;
    }

    public boolean unlock(LockRequest request){
        boolean unlockable = unlockable(request);
        if(unlockable) {
            ArrayList<String> transactionLocks = T2DBMap.get(request.getTid());
            for (String DBID : transactionLocks) {
                Lock currentLock = locks.get(DBID);
                if (currentLock.type == LockType.EXCLUSIVE) {
                    locks.remove(DBID);
                } else {
                    ReadLock readLock = (ReadLock) currentLock;
                    if (readLock.TIDs.size() == 1) {
                        locks.remove(DBID);
                    } else {
                        readLock.TIDs.remove(request.getTid());
                    }
                }
            }
//            No real need for second parameter, more for code readability
            T2DBMap.remove(request.getTid(), transactionLocks);
        }
        return unlockable;
    }

    private void addToT2DBMap(LockRequest request){
        if(!T2DBMap.containsKey(request.getTid())) {
            ArrayList<String> DBIDs = new ArrayList<>();
            DBIDs.add(request.getDbid());
            T2DBMap.put(request.getTid(), DBIDs);
        }else{
            T2DBMap.get(request.getTid()).add(request.getDbid());
        }
    }
}

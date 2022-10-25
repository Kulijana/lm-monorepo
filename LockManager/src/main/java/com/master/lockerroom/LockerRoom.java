package com.master.lockerroom;

import common.dto.LockRequest;
import common.dto.LockType;

import java.util.ArrayList;
import java.util.HashMap;

public class LockerRoom {
//    first parameter is DBID
    private HashMap<String, Lock> locks;
//    first parameter is TID, second is list of DBIDs
    private HashMap<String, ArrayList<String>> lockGraphs;

    public LockerRoom(){
        this.locks = new HashMap<>();
        this.lockGraphs = new HashMap<>();
    }
    public boolean lockable(LockRequest request) {
        if (!locks.containsKey(request.DBID)) {
            return true;
        }
        Lock currentLock = locks.get(request.DBID);
        if (currentLock.type == LockType.READ && request.type == LockType.READ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean unlockable(LockRequest request){
        return lockGraphs.containsKey(request.TID);
    }

    public synchronized boolean lock(LockRequest request){
        boolean lockable = lockable(request);
        if(lockable){
            if(!locks.containsKey(request.DBID)){
                if(request.type == LockType.READ){
                    ReadLock lock= new ReadLock();
                    lock.type = LockType.READ;
                    lock.TIDs = new ArrayList<>();
                    lock.TIDs.add(request.TID);
                    locks.put(request.DBID, lock);
                    ArrayList<String> DBIDs = new ArrayList<>();
                    DBIDs.add(request.DBID);
                    lockGraphs.put(request.TID, DBIDs);
                }else{
                    WriteLock lock = new WriteLock();
                    lock.type = LockType.WRITE;
                    lock.TID = request.TID;
                    locks.put(request.DBID, lock);
                    ArrayList<String> DBIDs = new ArrayList<>();
                    DBIDs.add(request.DBID);
                    lockGraphs.put(request.TID, DBIDs);
                }
            }else{
                Lock currentLock = locks.get(request.DBID);
                if(currentLock.type == LockType.READ && request.type == LockType.READ){
                    ReadLock readLock = (ReadLock) currentLock;
                    readLock.TIDs.add(request.TID);
                    lockGraphs.get(request.TID).add(request.DBID);
                }
            }
        }
        return lockable;
    }

    public boolean unlock(LockRequest request){
        boolean unlockable = unlockable(request);
        if(unlockable) {
            ArrayList<String> transactionLocks = lockGraphs.get(request.TID);
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
        }
        return unlockable;
    }
}

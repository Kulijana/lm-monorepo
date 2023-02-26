package common.dto;

import java.util.Comparator;

public enum LockType{
    EXCLUSIVE, SHARED, INTENT_EXCLUSIVE, INTENT_SHARED, UNLOCK, INVALID;


    public static LockType applyNewLock(LockType newLock, LockType currentLock){
        if(currentLock==null || currentLock==UNLOCK){
            return newLock;
        }
        if(currentLock == EXCLUSIVE){
            return INVALID;
        }
        if(currentLock==INTENT_EXCLUSIVE){
            if(newLock==INTENT_EXCLUSIVE)
                return INTENT_EXCLUSIVE;
            if(newLock==INTENT_SHARED)
                return INTENT_EXCLUSIVE;
            return INVALID;
        }
        if(currentLock==INTENT_SHARED){
            if(newLock==INTENT_EXCLUSIVE)
                return INTENT_EXCLUSIVE;
            if(newLock==INTENT_SHARED)
                return INTENT_SHARED;
            if(newLock==SHARED){
                return SHARED;
            }
            return INVALID;
        }
        if(currentLock==SHARED){
            if(newLock==INTENT_SHARED)
                return SHARED;
            if(newLock==SHARED)
                return SHARED;
            return INVALID;
        }
        return INVALID;
    }

    public static LockType strongerLock(LockType lock1, LockType lock2){
        if(lock1==EXCLUSIVE || lock2==EXCLUSIVE)
            return EXCLUSIVE;
        if(lock1==INTENT_EXCLUSIVE || lock2==INTENT_EXCLUSIVE)
            return INTENT_EXCLUSIVE;
        if(lock1==SHARED || lock2==SHARED)
            return SHARED;
        if(lock1==INTENT_SHARED || lock2==INTENT_SHARED)
            return INTENT_SHARED;
        return INVALID;
    }

}

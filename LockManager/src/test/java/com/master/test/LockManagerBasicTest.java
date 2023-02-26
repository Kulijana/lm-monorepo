package com.master.test;

import com.master.controller.LockController;
import common.dto.LockRequest;
import common.dto.LockType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LockManagerBasicTest {
    @Autowired
    private LockController lockController;

    @Test
    void singleReadLockTest(){
        LockRequest lockRequest = new LockRequest("TID", "DBID", LockType.SHARED);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        assertTrue(lockController.lock(lockRequest).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
    }

    @Test
    void singleWriteLockTest(){
        LockRequest request = new LockRequest("TID", "DBID", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
    }

    @Test
    void multipleReadLocksTest(){
        LockRequest request = new LockRequest("TID", "DBID", LockType.SHARED);
        LockRequest request2 = new LockRequest("TID2", "DBID", LockType.SHARED);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "DBID", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertTrue(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertTrue(lockController.unlock(unlockRequest2).allowed);
    }

    @Test
    void multipleWriteLockTest(){
        LockRequest request = new LockRequest("TID", "DBID", LockType.EXCLUSIVE);
        LockRequest request2 = new LockRequest("TID2", "DBID", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "DBID", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertFalse(lockController.unlock(unlockRequest2).allowed);
    }

    @Test
    void readWriteLockTest(){
        LockRequest request = new LockRequest("TID", "DBID", LockType.SHARED);
        LockRequest request2 = new LockRequest("TID2", "DBID", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "DBID", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertTrue(lockController.unlock(unlockRequest2).allowed);
    }

    @Test
    void WriteReadLockTest(){
        LockRequest request = new LockRequest("TID", "DBID", LockType.SHARED);
        LockRequest request2 = new LockRequest("TID2", "DBID", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "DBID", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertFalse(lockController.unlock(unlockRequest2).allowed);
    }

}

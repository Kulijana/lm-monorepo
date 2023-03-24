package com.master.test;

import com.master.controller.LockController;
import common.dto.lockmanager.LockRequest;
import common.LockType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class LockManagerGranularTest {

    @Autowired
    LockController lockController;

    @Test
    void readLockTest() {
        LockRequest request = new LockRequest("TID", "app1/db1/table1/row1", LockType.SHARED);
        LockRequest request2 = new LockRequest("TID2", "app1/db1/table1", LockType.SHARED);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1/row1", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertTrue(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertTrue(lockController.unlock(unlockRequest2).allowed);
    }


    @Test
    void readWrite1Test() {
        LockRequest request = new LockRequest("TID", "app1/db1/table1", LockType.SHARED);
        LockRequest request2 = new LockRequest("TID2", "app1/db1/table1/row1", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1/row1", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertFalse(lockController.unlock(unlockRequest2).allowed);
    }

    @Test
    void readWrite2Test() {
        LockRequest request = new LockRequest("TID", "app1/db1/table1", LockType.EXCLUSIVE);
        LockRequest request2 = new LockRequest("TID2", "app1/db1/table1/row1", LockType.SHARED);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1/row1", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertFalse(lockController.unlock(unlockRequest2).allowed);
    }

    @Test
    void upgradeTest() {
        LockRequest request = new LockRequest("TID", "app1/db1/table1", LockType.SHARED);
        LockRequest request2 = new LockRequest("TID", "app1/db1/table1", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertTrue(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
    }

    @Test
    void granularMultipleWrite1Test() {
        LockRequest request = new LockRequest("TID", "app1/db1/table1/row1", LockType.EXCLUSIVE);
        LockRequest request2 = new LockRequest("TID", "app1/db1/table1", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1/row1", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertFalse(lockController.unlock(unlockRequest2).allowed);
    }

    @Test
    void granularMultipleWrite2Test() {
        LockRequest request = new LockRequest("TID", "app1/db1/table1", LockType.EXCLUSIVE);
        LockRequest request2 = new LockRequest("TID", "app1/db1/table1/row1", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1/row1", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertFalse(lockController.unlock(unlockRequest2).allowed);
    }

    @Test
    public void lockUnlockLockTest() {
        LockRequest request = new LockRequest("TID", "app1/db1/table1", LockType.EXCLUSIVE);
        LockRequest request2 = new LockRequest("TID2", "app1/db1/table1/row1", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1/row1", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertTrue(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest2).allowed);

    }
}

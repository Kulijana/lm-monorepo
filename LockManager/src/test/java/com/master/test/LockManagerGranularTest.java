package com.master.test;

import com.master.controller.LockController;
import common.dto.LockRequest;
import common.dto.LockType;
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
    void readLockTest(){
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
    void readWriteTest(){
        LockRequest request = new LockRequest("TID", "app1/db1/table1", LockType.SHARED);
        LockRequest request2 = new LockRequest("TID2", "app1/db1/table1/row1", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "app1/db1/table1/row1", LockType.UNLOCK);
        LockRequest unlockRequest2 = new LockRequest("TID2", "app1/db1/table1", LockType.UNLOCK);
        assertTrue(lockController.lock(request).allowed);
        assertFalse(lockController.lock(request2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
        assertFalse(lockController.unlock(unlockRequest2).allowed);
    }
}

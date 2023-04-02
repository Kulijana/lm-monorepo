package com.master.test;

import com.master.controller.LockController;
import common.dto.lockmanager.LockRequest;
import common.LockType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LockManagerSpecialTest {

    @Autowired
    LockController lockController;

    @Test
    void upgradeLockTest(){
        LockRequest lockRequest = new LockRequest("TID", "DBID", LockType.SHARED);
        LockRequest lockRequest2 = new LockRequest("TID", "DBID", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        assertTrue(lockController.lock(lockRequest).allowed);
        assertTrue(lockController.lock(lockRequest2).allowed);
        assertTrue(lockController.unlock(unlockRequest).allowed);
    }

}

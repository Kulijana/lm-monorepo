package com.master.test;

import com.master.controller.LockController;
import common.dto.LockRequest;
import common.dto.LockType;
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

    @Test
    void deadLockPreventionTest(){
        LockRequest lockRequest = new LockRequest("TID", "DBID", LockType.SHARED);
        LockRequest lockRequest2 = new LockRequest("TID2", "DBID2", LockType.EXCLUSIVE);
        LockRequest unlockRequest = new LockRequest("TID", "DBID", LockType.UNLOCK);
        lockController.lock(lockRequest);
        lockController.lock(lockRequest2);
//        TODO make 2 threads, make them try until they succed or until some arbitrary amount of time passes
//        assert that either one or 2 of them got the actual result/that they didnt both time out
    }
}

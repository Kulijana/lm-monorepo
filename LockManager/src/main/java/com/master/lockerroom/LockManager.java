package com.master.lockerroom;

import common.dto.lockmanager.LockRequest;

public interface LockManager {
    boolean lock(LockRequest request);
    boolean unlock(LockRequest request);
}

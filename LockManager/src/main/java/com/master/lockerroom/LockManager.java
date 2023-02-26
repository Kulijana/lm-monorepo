package com.master.lockerroom;

import common.dto.LockRequest;

public interface LockManager {
    boolean lock(LockRequest request);
    boolean unlock(LockRequest request);
}

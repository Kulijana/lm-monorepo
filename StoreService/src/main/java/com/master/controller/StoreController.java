package com.master.controller;

import common.dto.LockRequest;
import common.dto.LockType;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import common.function.LockMessenger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController()
@RequestMapping("distributed")
public class StoreController {


    private int storage = 100;
    private final String storageDBID = "storageDBID";

    private LockMessenger messenger = new LockMessenger();

    @PostMapping(path = "/buy", consumes = MediaType.ALL_VALUE)
    public StoreResponse buy(@RequestBody StoreRequest request){
        try {
            LockRequest lockRequest = new LockRequest(request.getTid(), storageDBID, LockType.EXCLUSIVE);
            if(messenger.multipleAttemptLock(lockRequest, 3, 1000).blockingGet(false)){
                Thread.sleep(request.getTimeToProcess());
                this.storage -= request.getAmount();
                return new StoreResponse(true, this.storage);
            }else{
                return new StoreResponse(false, -1);
            }
        } catch (Exception e) {
            return new StoreResponse(false, -1);
        }
    }

    @PostMapping(path = "/status", consumes = MediaType.ALL_VALUE)
    public StoreResponse status(@RequestBody StoreRequest request){
        try {
            LockRequest lockRequest = new LockRequest(request.getTid(), storageDBID, LockType.SHARED);
            if(messenger.multipleAttemptLock(lockRequest, 3, 1000).blockingGet(false)){
                Thread.sleep(request.getTimeToProcess());
                this.storage -= request.getAmount();
                return new StoreResponse(true, this.storage);
            }else{
                return new StoreResponse(false, -1);
            }
        } catch (Exception e) {
            return new StoreResponse(false, -1);
        }
    }

}

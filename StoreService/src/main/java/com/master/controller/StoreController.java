package com.master.controller;

import common.dto.LockType;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import common.function.LockMessenger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class StoreController {

    private int storage = 100;
    private final String storageDBID = "storageDBID";

    private LockMessenger messenger = new LockMessenger();

    @PostMapping(path = "/buy", consumes = MediaType.ALL_VALUE)
    public StoreResponse buy(@RequestBody StoreRequest request){
        try {
            if(messenger.multipleAttemptLock(storageDBID, request.TID, LockType.WRITE, 3, 1000)){
                Thread.sleep(request.timeToProcess);
                this.storage -= request.amount;
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
            if(messenger.multipleAttemptLock(storageDBID, request.TID, LockType.READ, 3, 1000)){
                Thread.sleep(request.timeToProcess);
                this.storage -= request.amount;
                return new StoreResponse(true, this.storage);
            }else{
                return new StoreResponse(false, -1);
            }
        } catch (Exception e) {
            return new StoreResponse(false, -1);
        }
    }

}

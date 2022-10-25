package com.master.controller;

import common.dto.LockType;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import common.function.LockMessenger;
import org.springframework.http.MediaType;
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
            if(messenger.requestLock(storageDBID, request.TID, LockType.WRITE)){
                this.storage -= request.amount;
                return new StoreResponse(true);
            }else{
                return new StoreResponse(false);
            }
        } catch (Exception e) {
            return new StoreResponse(false);
        }
    }

}

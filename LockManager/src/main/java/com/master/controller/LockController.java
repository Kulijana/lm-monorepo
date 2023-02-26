package com.master.controller;


import com.master.lockerroom.GranularityTree;
import com.master.lockerroom.LockManager;
import com.master.lockerroom.LockerRoom;
import common.dto.LockRequest;
import common.dto.LockResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LockController {

//    TODO make it synchronous
//    LockManager lockManager = new LockerRoom();
    LockManager lockManager = new GranularityTree();
    @PostMapping(path = "/locks", consumes = MediaType.ALL_VALUE)
    public LockResponse lock(@RequestBody LockRequest request) {
        System.out.println("Lock request received:");
        System.out.println("TID: " + request.getTid());
        System.out.println("DBID: " + request.getDbid());
        System.out.println("Request type: " + request.getType().toString());
        LockResponse response = new LockResponse();
        response.allowed = lockManager.lock(request);
        System.out.println("Response: " + response.allowed);
        return response;
    }

    @PostMapping(path="/unlocks", consumes = MediaType.ALL_VALUE)
    public LockResponse unlock(@RequestBody LockRequest request){
        System.out.println("Unlock request received:");
        System.out.println("TID: " + request.getTid());
        System.out.println("DBID: " + request.getDbid());
        LockResponse response = new LockResponse();
        var res = lockManager.unlock(request);
        System.out.println("Response: " + res);
        response.allowed = res;
        return response;
    }

}
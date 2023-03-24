package com.master.controller;


import com.master.lockerroom.GranularityTree;
import com.master.lockerroom.LockManager;
import common.dto.lockmanager.LockRequest;
import common.dto.lockmanager.LockResponse;
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

        LockResponse response = new LockResponse();
        response.allowed = lockManager.lock(request);
        if (response.allowed) {
            System.out.println("Lock request received:");
            System.out.println("TID: " + request.getTid());
            System.out.println("DBID: " + request.getDbid());
            System.out.println("Request type: " + request.getType().toString());
            System.out.println("Response: " + response.allowed);
        } else {
            System.err.println("Lock request received:");
            System.err.println("TID: " + request.getTid());
            System.err.println("DBID: " + request.getDbid());
            System.err.println("Request type: " + request.getType().toString());
            System.err.println("Response: " + response.allowed);
        }
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
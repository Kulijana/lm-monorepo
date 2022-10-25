package com.master.controller;


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
    LockerRoom lockerRoom = new LockerRoom();
    @PostMapping(path = "/locks", consumes = MediaType.ALL_VALUE)
    public LockResponse lock(@RequestBody LockRequest request) {
        System.out.println("Lock request received:");
        System.out.println("TID: " + request.TID);
        System.out.println("DBID: " + request.DBID);
        System.out.println("Request type: " + request.type.toString());
        LockResponse response = new LockResponse();
        response.allowed = lockerRoom.lock(request);
        return response;
    }

    @PostMapping(path="/unlocks", consumes = MediaType.ALL_VALUE)
    public LockResponse unlock(@RequestBody LockRequest request){
        System.out.println("Unlock request received:");
        System.out.println("TID: " + request.TID);
        System.out.println("DBID: " + request.DBID);
        LockResponse response = new LockResponse();
        lockerRoom.unlock(request);
        response.allowed = true;
        return response;
    }

}
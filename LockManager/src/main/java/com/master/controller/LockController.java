package com.master.controller;


import com.master.lockerroom.GranularityTree;
import com.master.lockerroom.LockManager;
import common.dto.lockmanager.LockRequest;
import common.dto.lockmanager.LockResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.stream.LongStream;

@RestController
public class LockController {

    LockManager lockManager = new GranularityTree();
    ArrayList<Long> timers = new ArrayList<>();
    @PostMapping(path = "/locks", consumes = MediaType.ALL_VALUE)
    public LockResponse lock(@RequestBody LockRequest request) {

        LockResponse response = new LockResponse();
        long start = System.nanoTime();
        response.allowed = lockManager.lock(request);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        timers.add(timeElapsed);
//        if (response.allowed) {
//            System.out.println("Lock request received:");
//            System.out.println("TID: " + request.getTid());
//            System.out.println("DBID: " + request.getDbid());
//            System.out.println("Request type: " + request.getType().toString());
//            System.out.println("Response: " + response.allowed);
//        } else {
//            System.err.println("Lock request received:");
//            System.err.println("TID: " + request.getTid());
//            System.err.println("DBID: " + request.getDbid());
//            System.err.println("Request type: " + request.getType().toString());
//            System.err.println("Response: " + response.allowed);
//        }
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

    @GetMapping(path="/results", consumes = MediaType.ALL_VALUE)
    public void results(){
        System.out.println("Max: " + timers.stream().max(Long::compareTo));
        System.out.println("Min: " + timers.stream().min(Long::compareTo));
        System.out.println("Average: " + LongStream.of(timers.stream().mapToLong(Long::longValue).toArray()).average().orElse(Double.NaN));
    }

}
package com.master.controller;

import com.master.model.Product;
import com.master.repository.StoreRepository;
import common.dto.LockType;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import common.function.LockMessenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

@RestController
@RequestMapping("local")
public class LocalStoreController {

    @Autowired
    StoreRepository storeRepository;

    private LockMessenger messenger = new LockMessenger();

    @PostMapping(path = "/buy", consumes = MediaType.ALL_VALUE)
    @Transactional
    public StoreResponse buy(@RequestBody StoreRequest request){

        Product p = storeRepository.findById(Long.parseLong(request.getProductId())).get();
        int amount = p.getAmount();
        if(amount < request.getAmount()){
            return new StoreResponse(false, amount);
        }
        p.setAmount(p.getAmount() - request.getAmount());
        storeRepository.save(p);
        return new StoreResponse(true, amount);
    }

    @PostMapping(path = "/status", consumes = MediaType.ALL_VALUE)
    @Transactional
    public StoreResponse status(@RequestBody StoreRequest request){
        Product p = storeRepository.findById(Long.parseLong(request.getProductId())).get();
        return new StoreResponse(true, p.getAmount());
    }

}

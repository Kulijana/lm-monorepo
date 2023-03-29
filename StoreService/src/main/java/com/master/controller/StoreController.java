package com.master.controller;

import com.master.model.Product;
import com.master.repository.StoreRepository;
import common.dto.lockmanager.ConfigRequest;
import common.dto.lockmanager.LockRequest;
import common.LockType;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import common.function.LockMessenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController()
@RequestMapping("distributed")
public class StoreController {

    @Autowired
    StoreRepository storeRepository;
    private final String storeDBID = "store/lm_serialized/product/";

    private LockMessenger messenger = new LockMessenger();

    private int timeout = 0;
    private int attempts = 1;

    @PostMapping(path = "/buy", consumes = MediaType.ALL_VALUE)
    public StoreResponse buy(@RequestBody StoreRequest request){
        try {
            System.out.println("Buy request");
            LockRequest lockRequest = new LockRequest(request.getTid(), getStorageDBID(request.getProductId()), LockType.EXCLUSIVE);
            if(messenger.multipleAttemptLock(lockRequest).blockingGet(false)){
                Thread.sleep(request.getTimeToProcess());
//                this should be fine, no need to check that storage is sufficient
//                if lm works correctly
                var product = storeRepository.findById(Long.parseLong(request.getProductId())).get();
                product.setAmount(product.getAmount()- request.getAmount());
                product.setIncome(product.getIncome() + request.getAmount());
                storeRepository.save(product);
                return new StoreResponse(true, product.getAmount());
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
            System.out.println("Status request");
            LockRequest lockRequest = new LockRequest(request.getTid(), getStorageDBID(request.getProductId()), LockType.SHARED);
            if(messenger.multipleAttemptLock(lockRequest).blockingGet(false)){
//                TODO throw this out
                Thread.sleep(request.getTimeToProcess());
                var product = storeRepository.findById(Long.parseLong(request.getProductId())).get();
                return new StoreResponse(true, product.getAmount());
            }else{
                return new StoreResponse(false, -1);
            }
        } catch (Exception e) {
            return new StoreResponse(false, -1);
        }
    }

    @PostMapping(path="/rollback", consumes = MediaType.ALL_VALUE)
    public StoreResponse rollback(@RequestBody StoreRequest request){
        System.err.println("Rollback for: {" + request.getTid() + ", " + request.getProductId() + "}");
        var product = storeRepository.findById(Long.parseLong(request.getProductId())).get();
        product.setAmount(product.getAmount() + request.getAmount());
        product.setIncome(product.getIncome() - request.getAmount());
        storeRepository.save(product);
        return new StoreResponse(true, product.getAmount());
    }


    @PostMapping(path="/scenario", consumes = MediaType.ALL_VALUE)
    public StoreResponse createScenario(@RequestBody StoreRequest storeRequest){
//        hacked together
        int productCount = Integer.parseInt(storeRequest.getProductId());
        int productAmount = storeRequest.getAmount();
        storeRepository.truncateStore();
        List<Product> products = new ArrayList<>();
        for(int i=0;i<productCount;i++){
            products.add(new Product(0, productAmount));
        }
        storeRepository.saveAll(products);
        return new StoreResponse(true, 0);
    }

    @GetMapping(path="/config", consumes = MediaType.ALL_VALUE)
    public void getAttempts(@RequestBody ConfigRequest request) {
        this.attempts = request.getAttempts();
        this.timeout = request.getTimeout();
    }

    private String getStorageDBID(String productId){
        return storeDBID + productId;
    }

}

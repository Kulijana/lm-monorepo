package com.master.controller;

import com.master.model.CentCustomer;
import com.master.model.Product;
import com.master.repository.CentCustomerRepository;
import com.master.repository.StoreRepository;
import common.dto.cent.CentStoreRequest;
import common.dto.cent.CentStoreResponse;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import common.function.LockMessenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("cent")
public class CentralizedStoreController {

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    CentCustomerRepository centCustomerRepository;


    private LockMessenger messenger = new LockMessenger();

    @PostMapping(path = "/buy", consumes = MediaType.ALL_VALUE)
    @Transactional
    public CentStoreResponse buy(@RequestBody CentStoreRequest request) throws InterruptedException {

        ArrayList<Integer> itemAmount = new ArrayList<>();

        var products = request.getProductsToBuy();
        var customer = centCustomerRepository.findById(Long.parseLong(request.getCustomerId())).get();

        for(var pId : products){
            Product p = storeRepository.findById(Long.parseLong(pId)).get();
            var balance = customer.getBalance();
            var amount = p.getAmount();
            if(balance > 0 && amount >0){
                customer.setBalance(balance-1);
                customer.setInventory(customer.getInventory() + 1);
                p.setAmount(amount-1);
                p.setIncome(p.getIncome()+1);
                itemAmount.add(p.getAmount());
                Thread.sleep(request.getTimeout());
            }
            storeRepository.save(p);
            centCustomerRepository.save(customer);
        }
        return new CentStoreResponse(true, itemAmount);
    }

    @PostMapping(path = "/status", consumes = MediaType.ALL_VALUE)
    @Transactional
    public CentStoreResponse status(@RequestBody CentStoreRequest request) throws InterruptedException {

        ArrayList<Integer> itemAmount = new ArrayList<>();

        for(var pId: request.getProductsToBuy()){
            Product p = storeRepository.findById(Long.parseLong(pId)).get();
            itemAmount.add(p.getAmount());
            Thread.sleep(request.getTimeout());
        }
        return new CentStoreResponse(true, itemAmount);
    }


    @Transactional
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
        try {
            storeRepository.saveAll(products);
        }catch (Exception e){

        }
        return new StoreResponse(true, 0);
    }

    @GetMapping(path="/test", consumes=MediaType.ALL_VALUE)
    public boolean requestTester(){
        return true;
    }

    @GetMapping(path = "/reset", consumes = MediaType.ALL_VALUE)
    @Transactional
    public void reset(){
        storeRepository.truncateStore();
    }

}

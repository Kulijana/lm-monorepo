package com.master.service;

import com.master.model.CentCustomer;
import com.master.model.Customer;
import com.master.repository.CustomerRepository;
import common.dto.lockmanager.ConfigRequest;
import common.dto.lockmanager.LockRequest;
import common.LockType;

import common.dto.store.StoreRequest;
import common.function.LockMessenger;
import common.function.StoreMessenger;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class DistributedClientService implements ClientService {

    // TODO: 09/03/2023 add creation of new client?

    CustomerRepository customerRepository;

    private LockMessenger lockMessenger;
    private StoreMessenger storeMessenger;
    private final String clientDBID = "client/lm_serialized/customer/";
    private Long clientId;
    private String tid;
    private boolean transactionActive;

    private boolean transactionFailed;

    private ArrayList<StoreRequest> logs;



    @Autowired
    public DistributedClientService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    public DistributedClientService(CustomerRepository customerRepository, Long clientId, int attempts, int timeout){
        this.customerRepository = customerRepository;
        lockMessenger = new LockMessenger(attempts, timeout);
        storeMessenger = new StoreMessenger("distributed");
        this.clientId = clientId;
        transactionActive = false;
    }

    public Maybe<Integer> getProductStorage(StoreRequest request){
        return storeMessenger.requestStatus(request);
    }

    public Maybe<Integer> getBalance(){
        try {
            LockRequest request = new LockRequest();
            request.setDbid(getDBID());
            request.setTid(tid);
            request.setType(LockType.SHARED);
            if (lockMessenger.multipleAttemptLock(request).blockingGet(false)) {
                Customer customer = customerRepository.findById(clientId).get();
                return Maybe.just(customer.getBalance());
            } else {
                transactionFail("Client lock timeout");
                return Maybe.empty();
            }
        }catch (Exception ex){
            transactionFail(ex.getMessage());
            return Maybe.error(ex);
        }
    }

//    public Maybe<Boolean> buyFromStore(StoreRequest storeRequest){
//        return buyFromStore(storeRequest, 1, 200);
//    }

    public Maybe<Boolean> buyFromStore(StoreRequest storeRequest){
        try {
//            locking our resources preemptively
            LockRequest request = new LockRequest(tid, getDBID(), LockType.EXCLUSIVE);
            if (lockMessenger.multipleAttemptLock(request).blockingGet(false)) {
                storeRequest.setTid(this.tid);
                var storeResponse =  storeMessenger.requestBuy(storeRequest).blockingGet();
                if(storeResponse){
                    var customer = customerRepository.findById(clientId).get();
                    customer.setBalance(customer.getBalance() - storeRequest.getAmount());
                    customer.setInventory(customer.getInventory() + storeRequest.getAmount());
                    customerRepository.save(customer);
                    logs.add(storeRequest);
                }else{
                    transactionFail("Store rejected request");
                }
                return Maybe.just(storeResponse);
            } else {
                transactionFail("Client lock timeout");
                return Maybe.just(false);
            }
        }catch (Exception ex){
            transactionFail(ex.getMessage());
            return Maybe.error(ex);
        }
    }

    public boolean startTransaction(){
        if(transactionActive){
            return false;
        }
        this.tid = UUID.randomUUID().toString();
        this.logs = new ArrayList<>();
        transactionActive = true;
        transactionFailed = false;
        return true;
    }
    public void endTransaction(){
        try {
            if(transactionActive) {
                lockMessenger.unlock(tid);
                this.transactionActive = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void transactionFail(String failReason){
        if(!transactionFailed) {
            transactionFailed = true;
            System.err.println("Transaction failed: " + tid);
            System.err.println(failReason);
            rollback();
            endTransaction();
        }
    }

    private String getDBID(){
        return clientDBID + clientId;
    }

    private void rollback(){
        System.out.println("Rolling back for: " + tid);
        var customer = customerRepository.findById(clientId).get();
        for(int i=logs.size()-1; i>=0; i--){
            var log = logs.get(i);
            var amount = log.getAmount();
            customer.setInventory(customer.getInventory() - amount);
            customer.setBalance(customer.getBalance() + amount);
            storeMessenger.rollback(log);
        }
        customerRepository.save(customer);
    }



    static int transactionsSucceded = 0;

    // TODO: 10/03/2023 add transaction timeout to improve throughput
    public Thread buyThread(ArrayList<String> productsToBuy, int timeToProcess, int store_attempts, int store_timeout){
        storeMessenger.config(new ConfigRequest(store_attempts, store_timeout));
        System.out.println("Buy single transaction:");
        System.out.println("ClientId: " + clientId + " products to buy: " + productsToBuy);
        Thread thread = new Thread(() -> {
            try {
                do {
                    transactionFailed = false;
                    startTransaction();
                    System.out.println("tranasction starting: " + this.tid);

                    for (int i = 0; i < productsToBuy.size(); i++) {
                        StoreRequest storeRequest = new StoreRequest();
                        storeRequest.setTid(tid);
                        storeRequest.setProductId(productsToBuy.get(i));
                        storeRequest.setCustomerId(clientId.toString());
                        storeRequest.setAmount(1);
                        storeRequest.setTimeToProcess(timeToProcess);
                        if (!buyFromStore(storeRequest).blockingGet()) {
                            break;
                        }
                    }
                    if(!transactionFailed){
                        System.out.println("Transaction succeded: " + this.tid);
                        transactionsSucceded++;
                        System.out.println("Tranasctions finished: " + transactionsSucceded);
                    }else{
                        Thread.sleep(5000);
                    }
                }while(transactionFailed);
                endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }


        });
        return thread;
    }

    public Thread getStorageThread(ArrayList<String> productsToRead, int timeToProcess, int attempts, int lockTimeout){
        System.out.println("Get storage single transaction:");
        System.out.println("ClientId: " + clientId + " products to read: " + productsToRead);
        Thread thread = new Thread(() -> {
            try {
                do {
                    transactionFailed = false;
                    startTransaction();
                    System.out.println("tranasction starting: " + this.tid);

                    for (int i = 0; i < productsToRead.size(); i++) {
                        StoreRequest storeRequest = new StoreRequest();
                        storeRequest.setTid(tid);
                        storeRequest.setProductId(productsToRead.get(i));
                        storeRequest.setCustomerId(clientId.toString());
                        storeRequest.setTimeToProcess(timeToProcess);
                        if (getProductStorage(storeRequest).isEmpty().blockingGet()) {
                            break;
                        }
                    }
                    if(!transactionFailed){
                        System.out.println("Transaction succeded: " + this.tid);
                    }else{
                        Thread.sleep(10000);
                    }
                }while(transactionFailed);
                endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }


        });
        return thread;
    }



//    TODO allow a transaction to change the read lock to a write lock!
//    TODO add rollback to stuff
}

package com.master;

import com.master.repository.CustomerRepository;
import common.dto.LockRequest;
import common.dto.LockType;

import common.dto.store.StoreRequest;
import common.function.LockMessenger;
import common.function.StoreMessenger;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.UUID;

@Service
public class DistributedClientService implements ClientService{


    CustomerRepository customerRepository;

    private LockMessenger lockMessenger;

    private StoreMessenger storeMessenger;

    private int balance;

    private String clientDBID;

    private int productAmount;

    private String tid;
    private boolean transactionActive;

    @Autowired
    public DistributedClientService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    //    TODO perhaps change it so that we fetch things from a database which contains all the user info, like a bank
//    TODO basically turn users into more of a bank, useful for adding new users etc
    public Maybe<Integer> getProductStorage(StoreRequest request){
        return storeMessenger.requestStatus(request);
    }


    public Maybe<Integer> getBalance(){
        try {
            LockRequest request = new LockRequest();
            request.setDbid(clientDBID);
            request.setTid(tid);
            request.setType(LockType.READ);
            if (lockMessenger.multipleAttemptLock(request, 3, 1000).blockingGet(false)) {
                return Maybe.just(balance);
            } else {
                return Maybe.empty();
            }
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }


    public Maybe<Boolean> buyFromStore(StoreRequest storeRequest){
        try {
            LockRequest request = new LockRequest(tid, clientDBID, LockType.WRITE);
            if (lockMessenger.multipleAttemptLock(request, 3, 1000).blockingGet(false)) {
                storeRequest.setTid(this.tid);
                return storeMessenger.requestBuy(storeRequest);
            } else {
                return Maybe.just(false);
            }
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }

    public boolean startTransaction(){
        if(transactionActive){
            return false;
        }
        this.tid = UUID.randomUUID().toString();
        transactionActive = true;
        return true;
    }
    public void endTransaction(){
        try {
            lockMessenger.unlock(tid);
            this.transactionActive = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public DistributedClientService(){
        lockMessenger = new LockMessenger();
        storeMessenger = new StoreMessenger("distributed");
        transactionActive = false;
    }

    public DistributedClientService(CustomerRepository customerRepository, String clientDBID, int balance){
        this.customerRepository = customerRepository;
        lockMessenger = new LockMessenger();
        storeMessenger = new StoreMessenger("distributed");
        this.clientDBID = clientDBID;
        this.balance = balance;
        transactionActive = false;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        DistributedClientService service = new DistributedClientService("client1", 200);
//        service.case1();
//        service.jpaExample();
    }

    public void jpaExample(){
        customerRepository.findAll().forEach(x -> {
            System.out.println(x.getIdcustomer());
                    System.out.println(x.getBalance());
                    System.out.println(x.getSpent());
        }
        );
    }

    public void case1() throws InterruptedException {
        System.out.println("Case 1: Customer checking amount and buying something");
        Thread customer1 = new Thread(() -> {
            try {
            if(startTransaction()){
                System.out.println("our balance:" + getBalance().blockingGet());
                endTransaction();
            }
            if(startTransaction()){
                StoreRequest storeRequest = new StoreRequest();
                storeRequest.setTid(tid);
                storeRequest.setCustomerId("Nonrelevant");
                storeRequest.setAmount(10);
                storeRequest.setTimeToProcess(500);
                buyFromStore(storeRequest);
                endTransaction();
            }
            if(startTransaction()){
                System.out.println("our balance:" + getBalance().blockingGet());
                endTransaction();
            }
            } catch (Exception e) {
                e.printStackTrace();
            }


        });

        customer1.start();
        customer1.join();
    }

//    TODO allow a transaction to change the read lock to a write lock!
}

package com.master.service;

import com.master.repository.CustomerRepository;
import com.master.repository.StoreRepository;
import common.dto.cent.CentStoreRequest;
import common.dto.store.StoreRequest;
import common.function.StoreMessenger;
import io.reactivex.rxjava3.core.Maybe;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Transactional
public class CentClientService implements ClientService {

    private StoreMessenger storeMessenger;


    private long clientId;

    private long timeout;

    public CentClientService(long clientId){
        storeMessenger = new StoreMessenger("cent");
        this.clientId = clientId;
    }

//    @Override
//    public Maybe<Integer> getProductStorage(CentStoreRequest request) {
//        try{
//            var productId = Long.parseLong(request.getProductId());
//            var product = storeRepository.findById(productId).get();
//            return Maybe.just(product.getAmount());
//        } catch (Exception ex){
//            return Maybe.error(ex);
//        }
//    }
//
//    @Override
//    public Maybe<Boolean> buyFromStore(StoreRequest request) {
//        try {
//            var customer = customerRepository.findById(clientId).get();
//            var productId = Long.parseLong(request.getProductId());
//            var product = storeRepository.findById(productId).get();
//            var amountToBuy = request.getAmount();
//            var amountInStore = product.getAmount();
//            var price = product.getIncome();
//            int balance = customer.getBalance();
//            if (balance >= price * amountToBuy && amountToBuy <= amountInStore) {
//                product.setAmount(amountInStore - amountToBuy);
//                customer.setBalance(balance - price * amountToBuy);
//                customer.setInventory(customer.getInventory() + price * amountToBuy);
//                customerRepository.save(customer);
//                storeRepository.save(product);
//                return Maybe.just(true);
//            }
//            return Maybe.just(false);
//        }catch (Exception ex){
//            return Maybe.error(ex);
//        }
//    }
//
//    @Override
//    public Maybe<Integer> getBalance() {
//        try {
//            var customer = customerRepository.findById(clientId).get();
//            return Maybe.just(customer.getBalance());
//        }catch (Exception ex){
//            return Maybe.error(ex);
//        }
//    }

    @Override
    public Maybe<Integer> getProductStorage(StoreRequest request) {
        return null;
    }

    @Override
    public Maybe<Boolean> buyFromStore(StoreRequest request) {
        return null;
    }

    @Override
    public Maybe<Integer> getBalance() {
        return null;
    }

    @Override
    public boolean startTransaction() {
        return false;
    }

    @Override
    public void endTransaction() {

    }

    public Thread buyThread(ArrayList<String> productsToBuy){
        System.out.println("Buy single transaction:");
        System.out.println("ClientId: " + clientId + " products to buy: " + productsToBuy);
        Thread thread = new Thread(() -> {
            try {
                boolean repeat = false;
                Maybe<Boolean> allowed;
                do {
                    startTransaction();
                    System.out.println("tranasction starting: " + this.clientId);
                    var timeout = storeMessenger.requestTimeTest().blockingGet();

                    CentStoreRequest storeRequest = new CentStoreRequest();
                    storeRequest.setProductsToBuy(productsToBuy);
                    storeRequest.setCustomerId(Long.toString(clientId));
                    storeRequest.setTimeout((int) timeout.longValue());

                    allowed = storeMessenger.centRequestBuy(storeRequest);

                    try {
                        repeat = !allowed.blockingGet();
                    }catch (Exception e){
                        repeat = true;
                    }
                    if(!repeat) {
                        System.out.println("Transaction " + clientId + " allowed:" + !repeat);
                    }else{
                        System.err.println("Transaction failed: " + clientId);
                    }
                    endTransaction();
                }while(repeat);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        return thread;
    }

    public Thread getProductStorageThread(ArrayList<String> productsToBuy){
        System.out.println("Buy single transaction:");
        System.out.println("ClientId: " + clientId + " products to get storage: " + productsToBuy);
        Thread thread = new Thread(() -> {
            try {
                startTransaction();
                System.out.println("tranasction starting: " + this.clientId);
                var timeout = storeMessenger.requestTimeTest().blockingGet();

                CentStoreRequest storeRequest = new CentStoreRequest();
                storeRequest.setProductsToBuy(productsToBuy);
                storeRequest.setCustomerId(Long.toString(clientId));
                storeRequest.setTimeout((int) timeout.longValue());

                var storages = storeMessenger.centRequestStatus(storeRequest).blockingGet();
                System.out.println("Transaction " + clientId + " storages:" + storages);
                endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        return thread;
    }


}

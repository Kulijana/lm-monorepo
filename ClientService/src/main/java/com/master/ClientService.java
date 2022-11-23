package com.master;

import common.dto.LockType;

import common.function.LockMessenger;
import common.function.StoreMessenger;


import java.io.IOException;



public class ClientService {

    private LockMessenger lockMessenger;

    private StoreMessenger storeMessenger;

    private int balance;

    private String clientDBID;

    //            TODO here should go a call to store service, its just simulation for now
//    TODO return a Maybe<int> depending on the store response
//    TODO lock not needed?
    public int getStorage(String DBID, String TID, int timeToProcess) throws IOException, InterruptedException {
        if(lockMessenger.multipleAttemptLock(DBID, TID, LockType.READ, 3, 1000)){
            return storeMessenger.requestStatus(TID, timeToProcess);
        }
        else{
            return -1;
        }
    }

    public int getBalance(String TID) throws IOException, InterruptedException {
        if(lockMessenger.multipleAttemptLock(this.clientDBID, TID, LockType.READ, 3, 1000)){
            return balance;
        }
        else{
            return -1;
        }
    }

    public boolean buyFromStore(String TID, int timeToProcess) throws IOException, InterruptedException {
        if(lockMessenger.multipleAttemptLock(this.clientDBID, TID, LockType.WRITE, 3, 1000)){
            return storeMessenger.requestBuy(TID, timeToProcess);
        }else{
            return false;
        }
    }

    public void endTransaction(String TID){
        try {
            lockMessenger.unlock(TID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientService(){
        lockMessenger = new LockMessenger();
        storeMessenger = new StoreMessenger();
    }

    public ClientService(String clientDBID, int balance){
        lockMessenger = new LockMessenger();
        storeMessenger = new StoreMessenger();
        this.clientDBID = clientDBID;
        this.balance = balance;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ClientService service = new ClientService();
        service.case1();
    }

    public void case1(){
        System.out.println("Case 1: Customer checking amount and buying something");
        Thread customer1 = new Thread(() -> {
            try {
            String TID = "case1";
            String DBID = "customer1";
            getStorage(DBID, TID, 500);
//            endTransaction(TID);
            buyFromStore(TID, 2000);
            endTransaction(TID);

            } catch (Exception e) {
                e.printStackTrace();
            }


        });

        customer1.start();
    }

//    TODO allow a transaction to change the read lock to a write lock!
}

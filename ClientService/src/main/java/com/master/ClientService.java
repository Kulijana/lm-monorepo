package com.master;

import common.dto.LockType;

import common.function.LockMessenger;
import common.function.StoreMessenger;


import java.io.IOException;



public class ClientService {

    private LockMessenger lockMessenger;

    private StoreMessenger storeMessenger;

    //            TODO here should go a call to store service, its just simulation for now
    public void getStorage(String DBID, String TID, int timeToProcess) throws IOException, InterruptedException {
        if(lockMessenger.requestLock(DBID, TID, LockType.READ)){
            System.out.println("Store storage amount: " + storeMessenger.requestStatus(TID, timeToProcess));
        }
        else{
            System.out.println("Status not read for DBID: " + DBID);
        }
    }

    public void buyFromStore(String DBID, String TID, int timeToProcess) throws IOException, InterruptedException {
        if(lockMessenger.requestLock(DBID, TID, LockType.WRITE)){
            System.out.println("Store purchase response: " + storeMessenger.requestBuy(TID, timeToProcess));
        }else{
            System.out.println("Failed write for DBID: " + DBID);
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

    public static void main(String[] args) throws IOException, InterruptedException {
        ClientService service = new ClientService();
        service.case1();
    }

    public void case1(){
        System.out.println("Customer checking amount and buying something");
        Thread customer1 = new Thread(() -> {
            try {
            String TID = "case1";
            String DBID = "customer1";
            getStorage(DBID, TID, 500);
            endTransaction(TID);
//            buyFromStore(DBID, TID, 2000);
//            endTransaction(TID);

            } catch (Exception e) {
                e.printStackTrace();
            }


        });

        customer1.start();
    }

//    TODO allow a transaction to change the read lock to a write lock!
}

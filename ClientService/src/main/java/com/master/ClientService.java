package com.master;

import common.dto.LockType;

import common.function.LockMessenger;


import java.io.IOException;



public class ClientService {

    private LockMessenger messenger;

    //            TODO here should go a call to store service, its just simulation for now
    public void getStatus(String DBID, String TID) throws IOException, InterruptedException {
        if(messenger.requestLock(DBID, TID, LockType.READ)){
            System.out.println("Status read successfully for DBID: " + DBID);
        }
        else{
            System.out.println("Status not read for DBID: " + DBID);
        }
    }

    public void updateStatus(String DBID, String TID) throws IOException, InterruptedException {
        if(messenger.requestLock(DBID, TID, LockType.WRITE)){
            System.out.println("Successful write for DBID: " + DBID);
        }else{
            System.out.println("Failed write for DBID: " + DBID);
        }
    }

    public void releaseLocks(String TID){
        try {
            messenger.unlock(TID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientService(){
        messenger = new LockMessenger();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ClientService service = new ClientService();
        service.getStatus("item1", "tid1");
        service.getStatus("item1", "tid1");
        service.updateStatus("item1", "tid1");
        service.releaseLocks("tid1");
        service.updateStatus("item1", "tid2");
    }
}

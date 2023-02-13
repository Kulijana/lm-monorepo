package com.master;

import com.master.repository.CustomerRepository;
import com.master.repository.StoreRepository;
import common.dto.store.StoreRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientServiceApplication
        implements CommandLineRunner {

    @Autowired
    CustomerRepository customerRepository;
//    private static Logger LOG = LoggerFactory
//            .getLogger(ClientServiceApplication.class);
    @Autowired
    StoreRepository storeRepository;

    public static void main(String[] args) {
//        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(ClientServiceApplication.class, args);
//        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws InterruptedException {
//        LOG.info("EXECUTING : command line runner");
        LocalClientService service = new LocalClientService(1, customerRepository, storeRepository);
        var balance = service.getBalance().blockingGet();
        System.out.println(balance);
        StoreRequest request = new StoreRequest();
        request.setCustomerId("1");
        request.setAmount(3);
        request.setProductId("1");
        var result = service.buyFromStore(request).blockingGet();
        System.out.println("buy was sucess?: " + result);
        var balance2 = service.getBalance().blockingGet();
        System.out.println("new balance: "+ balance2);


//        DistributedClientService service = new DistributedClientService(repository,"client1", 200);
////        service.jpaExample();
//        service.case1();

    }

}

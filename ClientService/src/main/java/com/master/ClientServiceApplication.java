package com.master;

import com.master.model.Customer;
import com.master.repository.CustomerRepository;
import com.master.repository.StoreRepository;
import common.dto.store.StoreRequest;
import common.function.StoreMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
public class ClientServiceApplication
        implements CommandLineRunner {

    @Autowired
    CustomerRepository customerRepository;
//    private static Logger LOG = LoggerFactory
//            .getLogger(ClientServiceApplication.class);

//    won't be needed
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
        buildScenario(20, 10);
        StoreMessenger storeMessenger = new StoreMessenger("distributed");
        storeMessenger.createScenario(10, 10);

        DistributedClientService service1 = new DistributedClientService(customerRepository, 1L);
        DistributedClientService service2 = new DistributedClientService(customerRepository, 2L);
        DistributedClientService service3 = new DistributedClientService(customerRepository, 3L);

        ArrayList<String> productsToBuy = new ArrayList<>(Arrays.asList("1", "2" , "3",  "4"));
        ArrayList<Integer> amountToBuy = new ArrayList<>(Arrays.asList(2, 2 , 2,  2));

        Thread thread1 = service1.buyThread(productsToBuy, amountToBuy, 100);
        Thread thread2 = service2.buyThread(productsToBuy, amountToBuy, 100);
        Thread thread3 = service3.buyThread(productsToBuy, amountToBuy, 100);

        thread1.start();
        thread1.join();

//        thread2.start();
//        thread2.join();
//
//        thread3.start();
//        thread3.join();

    }

    private void buildScenario(int customerCount, int customerBalance){
        customerRepository.truncateCustomers();
        ArrayList<Customer> customers = new ArrayList<>();
        for(int i=0;i<customerCount;i++){
            Customer customer = new Customer();
            customer.setInventory(0);
            customer.setBalance(customerBalance);
            customers.add(customer);
        }
        customerRepository.saveAll(customers);
    }

}

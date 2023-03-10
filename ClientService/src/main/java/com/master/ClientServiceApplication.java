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
import java.util.Random;

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

        int serviceCount= 10;
        int customerBalance = 100;

        int productCount = 10;
        int productsToBuyCount = 2;
        int productAmount = 100;

        buildScenario(serviceCount, customerBalance);
        StoreMessenger storeMessenger = new StoreMessenger("distributed");
        var resp = storeMessenger.createScenario(productCount, productAmount).blockingGet();

        ArrayList<DistributedClientService> services = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ArrayList<String>> productsToBuy = new ArrayList<>();
        ArrayList<Integer> amountToBuy = new ArrayList<>();

        Random r = new Random();

        for(int i=0;i<serviceCount;i++) {

            ArrayList<String> productsForService = new ArrayList<>();
            for (int j = 0; j < productsToBuyCount; j++) {
                int index = r.nextInt(productCount);
                productsForService.add(String.valueOf(index + 1));
            }
            productsToBuy.add(productsForService);
        }

        for (int j = 0; j < productsToBuyCount; j++) {
            amountToBuy.add(2);
        }


        for(long i=1;i<=serviceCount;i++){
            var service = new DistributedClientService(customerRepository, i);
            services.add(service);
            var thread = service.buyThread(productsToBuy.get((int)(i-1)), amountToBuy, 0, r.nextInt(7), r.nextInt(2000));
            threads.add(thread);
//            thread.start();
//            thread.join();
//            threads.add(service.buyThread(productsToBuy, amountToBuy, 0));
        }
        threads.forEach(thread -> thread.start());
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

//        DistributedClientService service1 = new DistributedClientService(customerRepository, 1L);
//        DistributedClientService service2 = new DistributedClientService(customerRepository, 2L);
//        DistributedClientService service3 = new DistributedClientService(customerRepository, 3L);
//        DistributedClientService service4 = new DistributedClientService(customerRepository, 4L);

//        ArrayList<String> productsToBuy = new ArrayList<>(Arrays.asList("1", "2" , "3",  "4"));
//        ArrayList<Integer> amountToBuy = new ArrayList<>(Arrays.asList(2, 2 , 2,  2));
//
//        Thread thread1 = service1.buyThread(productsToBuy, amountToBuy, 0);
//        Thread thread2 = service2.buyThread(productsToBuy, amountToBuy, 0);
//        Thread thread3 = service3.buyThread(productsToBuy, amountToBuy, 0);
//        Thread thread4 = service4.buyThread(productsToBuy, amountToBuy, 0);
//
//        thread1.start();
//        thread1.join();
//
//        thread2.start();
//        thread2.join();
//
//        thread3.start();
//        thread3.join();
//
//        thread4.start();
//        thread4.join();

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

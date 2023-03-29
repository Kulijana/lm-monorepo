package com.master;

import com.master.model.Customer;
import com.master.repository.CustomerRepository;
import com.master.service.CentClientService;
import com.master.service.DistributedClientService;
import common.function.StoreMessenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Random;

@SpringBootApplication
public class ClientServiceApplication
        implements CommandLineRunner {

    @Autowired
    CustomerRepository customerRepository;
//    private static Logger LOG = LoggerFactory
//            .getLogger(ClientServiceApplication.class);

//    won't be needed


    public static void main(String[] args) {
//        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(ClientServiceApplication.class, args);
//        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws InterruptedException {
//        LOG.info("EXECUTING : command line runner");
//        distributedScenario();

        var start = System.currentTimeMillis();
//        centralizedScenario();
        distributedScenario();
        var end = System.currentTimeMillis();
        System.out.println("Time taken in milliseconds: " + (end - start));
    }



    private void distributedScenario(){
        int serviceCount= 20;
        int customerBalance = 1000;

        int productCount = 500;
        int productsToBuyCount = 20;
        int productAmount = 200;

        int lm_attempts = 4;
        int lm_timeout = 1000;

        int store_attempts = 10;
        int store_timeout = 500;

        buildScenario(serviceCount, customerBalance);
        StoreMessenger storeMessenger = new StoreMessenger("distributed");
        var resp = storeMessenger.createScenario(productCount, productAmount).blockingGet();

        ArrayList<DistributedClientService> services = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ArrayList<String>> productsToBuy = new ArrayList<>();

        Random r = new Random();

        for(int i=0;i<serviceCount;i++) {

            ArrayList<String> productsForService = new ArrayList<>();
            for (int j = 0; j < productsToBuyCount; j++) {
                int index = r.nextInt(productCount);
                productsForService.add(String.valueOf(index + 1));
            }
            productsToBuy.add(productsForService);
        }

        for(long i=1;i<=serviceCount;i++){
            var service = new DistributedClientService(customerRepository, i, lm_attempts, lm_timeout);
            services.add(service);
//            var thread = service.buyThread(productsToBuy.get((int)(i-1)), 0, r.nextInt(store_attempts), r.nextInt(store_timeout));
            var thread = service.buyThread(productsToBuy.get((int)(i-1)), 0,store_attempts, store_timeout);
            threads.add(thread);
        }
        threads.forEach(thread -> thread.start());
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void centralizedScenario(){
        int serviceCount= 40;
        int customerBalance = 1000;

        int productCount = 1000;
        int productsToBuyCount = 20;
        int productAmount = 1000;

        buildScenario(serviceCount, customerBalance);
        StoreMessenger storeMessenger = new StoreMessenger("cent");
        var resp = storeMessenger.createScenario(productCount, productAmount).blockingGet();

        ArrayList<CentClientService> services = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ArrayList<String>> productsToBuy = new ArrayList<>();

        Random r = new Random();

        for(int i=0;i<serviceCount;i++) {

            ArrayList<String> productsForService = new ArrayList<>();
            for (int j = 0; j < productsToBuyCount; j++) {
                int index = r.nextInt(productCount);
                productsForService.add(String.valueOf(index + 1));
            }
            productsToBuy.add(productsForService);
        }

        for(long i=1;i<=serviceCount;i++){
            var service = new CentClientService(i);
            services.add(service);
            var thread = service.buyThread(productsToBuy.get((int)(i-1)));
            threads.add(thread);
        }
        threads.forEach(thread -> thread.start());
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
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

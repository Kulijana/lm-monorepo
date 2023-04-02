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

        int serviceCount = 5;
        int productCount = 100;
        int productsToBuyCount = 5;
        int productAmount = 200;
        int customerBalance = 5000;
        if(args.length > 0){
            serviceCount = Integer.parseInt(args[0]);
            productCount = Integer.parseInt(args[1]);
            productsToBuyCount = Integer.parseInt(args[2]);
        }
        if(args.length > 3){
            productAmount = Integer.parseInt(args[3]);
            customerBalance = Integer.parseInt(args[4]);
        }

        ScenarioResult r = distributedScenario(serviceCount, productCount, productsToBuyCount, productAmount, customerBalance);
        r.print();


    }



    private ScenarioResult distributedScenario(int serviceCount,
                                               int productCount,
                                               int productsToBuyCount,
                                               int productAmount,
                                               int customerBalance){

        ScenarioResult result = new ScenarioResult();
        result.serviceCount = serviceCount;
        result.productsCount = productCount;
        result.productsToBuyCount = productsToBuyCount;
        double loadFactor = loadFactor(serviceCount, productCount, productsToBuyCount);
        result.loadFactor = loadFactor;
        result.load = loadFactor*serviceCount;


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
                String index;
                do {
                    index = String.valueOf(r.nextInt(productCount)+1);
                }
                while(productsForService.contains(index));
                productsForService.add(index);
            }
            productsToBuy.add(productsForService);
        }

        for(long i=1;i<=serviceCount;i++){
            var service = new DistributedClientService(customerRepository, i, lm_attempts, lm_timeout);
            services.add(service);
//            var thread = service.buyThread(productsToBuy.get((int)(i-1)), 0, r.nextInt(store_attempts), r.nextInt(store_timeout));
            var thread = service.buyThread(productsToBuy.get((int)(i-1)), 0,store_attempts, store_timeout, (int)result.load+1);
            threads.add(thread);
        }
        long start = System.currentTimeMillis();
        threads.forEach(thread -> thread.start());
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        long end = System.currentTimeMillis();
        result.time = end - start;
        return result;
    }

    private void centralizedScenario(int serviceCount, int productCount, int productsToBuyCount){

        int customerBalance = 1000;
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



    private double loadFactor(int a, int t, int i){
        double k = ((double)i)/t;
        double p0 = Math.pow(1-k,a);
        double p1 = a*k*Math.pow(1-k, a-1);
//        probability that an item is bad is p
        double p = 1 - p0 - p1;

        return 1 - Math.pow(1 - p, i);
    }

    private class ScenarioResult{
        int serviceCount;
        int productsCount;
        int productsToBuyCount;
        double loadFactor;
        double load;
        long time;

        void print(){
            System.out.println("A | T | I | LF | L | time");
            System.out.println(serviceCount + " | " + productsCount + " | " + productsToBuyCount + " | " + loadFactor + " | " + load + " | " + time);
        }
    }


    private int fact(int k){
        if(k==0) return 1;
        return k*fact(k-1);
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

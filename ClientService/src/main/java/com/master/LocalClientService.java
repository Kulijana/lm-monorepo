package com.master;

import com.master.repository.CustomerRepository;
import com.master.repository.StoreRepository;
import common.dto.store.StoreRequest;
import common.function.StoreMessenger;
import io.reactivex.rxjava3.core.Maybe;

import javax.transaction.Transactional;

@Transactional
public class LocalClientService implements ClientService{

    private StoreMessenger storeMessenger;

    CustomerRepository customerRepository;
    StoreRepository storeRepository;

    private long customerId;

    public LocalClientService(long customerId, CustomerRepository customerRepository, StoreRepository storeRepository){
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.customerId = customerId;
    }

    @Override
    public Maybe<Integer> getProductStorage(StoreRequest request) {
        try{
            var productId = Long.parseLong(request.getProductId());
            var product = storeRepository.findById(productId).get();
            return Maybe.just(product.getAmount());
        } catch (Exception ex){
            return Maybe.error(ex);
        }
    }

    @Override
    public Maybe<Boolean> buyFromStore(StoreRequest request) {
        try {
            var customer = customerRepository.findById(customerId).get();
            var productId = Long.parseLong(request.getProductId());
            var product = storeRepository.findById(productId).get();
            var amountToBuy = request.getAmount();
            var amountInStore = product.getAmount();
            var price = product.getIncome();
            int balance = customer.getBalance();
            if (balance >= price * amountToBuy && amountToBuy <= amountInStore) {
                product.setAmount(amountInStore - amountToBuy);
                customer.setBalance(balance - price * amountToBuy);
                customer.setInventory(customer.getInventory() + price * amountToBuy);
                customerRepository.save(customer);
                storeRepository.save(product);
                return Maybe.just(true);
            }
            return Maybe.just(false);
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }

    @Override
    public Maybe<Integer> getBalance() {
        try {
            var customer = customerRepository.findById(customerId).get();
            return Maybe.just(customer.getBalance());
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }
}

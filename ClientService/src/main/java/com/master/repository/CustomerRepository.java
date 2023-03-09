package com.master.repository;

import com.master.model.Customer;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    @Modifying
    @Transactional
    @Query(value = "truncate table customer", nativeQuery = true)
    void truncateCustomers();
}

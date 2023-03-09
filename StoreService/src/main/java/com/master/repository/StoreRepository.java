package com.master.repository;


import com.master.model.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface StoreRepository extends CrudRepository <Product, Long>{

    @Modifying
    @Transactional
    @Query(value = "truncate table product", nativeQuery = true)
    void truncateStore();
}

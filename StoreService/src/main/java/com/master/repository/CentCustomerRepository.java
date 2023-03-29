package com.master.repository;

import com.master.model.CentCustomer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentCustomerRepository extends CrudRepository<CentCustomer, Long>{}

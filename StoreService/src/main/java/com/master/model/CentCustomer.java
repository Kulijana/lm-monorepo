package com.master.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name= "customer")
public class CentCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idcustomer;
    private int balance;
    private int inventory;
}

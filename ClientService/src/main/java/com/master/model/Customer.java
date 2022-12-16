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
@Table(name= "message")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idCustomer;
    private int balance;
    private int spent;
}

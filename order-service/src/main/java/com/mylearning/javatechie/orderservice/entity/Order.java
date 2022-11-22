package com.mylearning.javatechie.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "orderTb")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private int qty;
    private double price;

    @Transient
    private String port;
}

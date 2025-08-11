package com.stou.demo5_jpaproject.weather.catfood;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor

public class CatFood {
    @Id
    private String code;
    private String name;
    private double price;
}



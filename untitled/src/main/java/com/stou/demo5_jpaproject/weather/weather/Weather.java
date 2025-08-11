package com.stou.demo5_jpaproject.weather.weather;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor

public class Weather {
    @Id
    private String date;
    private String temperature;
    private String pm25;
    private int weathercondition;
}



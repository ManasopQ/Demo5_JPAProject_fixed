package com.stou.demo5_jpaproject.weather.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherService {
    @Autowired
    private WeatherRepository weatherRepository;

    public List<Weather> getWeather() {
        return weatherRepository.findAll();
    }

    public void addWeather(Weather weather){
        weatherRepository.save(weather);
    }
}

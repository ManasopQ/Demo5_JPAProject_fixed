package com.stou.demo5_jpaproject.weather.catfood;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatFoodService {
    @Autowired
    private CatFoodRepository catFoodRepository;

    public List<CatFood> getProducts() {
        return catFoodRepository.findAll();
    }

    public CatFood getProduct(String code) {
        return catFoodRepository.findById(code).orElse(null);
    }

    public void saveProduct(CatFood product){
        catFoodRepository.save(product);
    }

    public void deleteProduct(String code) {
        catFoodRepository.deleteById(code);
    }
}

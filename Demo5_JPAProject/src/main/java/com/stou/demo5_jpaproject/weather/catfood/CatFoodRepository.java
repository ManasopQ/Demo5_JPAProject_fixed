package com.stou.demo5_jpaproject.weather.catfood;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatFoodRepository extends JpaRepository<CatFood, String> {

}

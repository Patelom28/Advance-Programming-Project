package com.example.foodrescue.repository;

import com.example.foodrescue.model.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    List<Fridge> findAllByOrderByNameAsc();

    List<Fridge> findByActiveTrueOrderByNameAsc();
}

package com.example.foodrescue.repository;

import com.example.foodrescue.model.FoodItem;
import com.example.foodrescue.model.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Database access for food items, plus aggregation queries for the dashboard statistics
@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByFridgeIdOrderByExpiryDateAsc(Long fridgeId);

    List<FoodItem> findByStatusOrderByExpiryDateAsc(ItemStatus status);

    long countByStatus(ItemStatus status);

    long countByFridgeIdAndStatus(Long fridgeId, ItemStatus status);

    // Items still offered although their best-before date has passed
    List<FoodItem> findByStatusAndExpiryDateBefore(ItemStatus status, LocalDate date);

    // Available items whose best-before date is today or in the next days
    List<FoodItem> findByStatusAndExpiryDateLessThanEqualOrderByExpiryDateAsc(
            ItemStatus status, LocalDate limit);

    // Total weight (kg) of all items with the given status, null when there are none
    @Query("select sum(i.quantityKg) from FoodItem i where i.status = :status")
    Double sumWeightByStatus(@Param("status") ItemStatus status);

    // Total weight (kg) inside one fridge for the given status, null when there are none
    @Query("select sum(i.quantityKg) from FoodItem i "
            + "where i.fridge.id = :fridgeId and i.status = :status")
    Double sumWeightByFridgeAndStatus(@Param("fridgeId") Long fridgeId,
                                      @Param("status") ItemStatus status);

    // Weight per food category: each row is [0] = FoodCategory, [1] = Double weight
    @Query("select i.category, sum(i.quantityKg) from FoodItem i "
            + "where i.status = :status group by i.category order by sum(i.quantityKg) desc")
    List<Object[]> sumWeightGroupedByCategory(@Param("status") ItemStatus status);
}

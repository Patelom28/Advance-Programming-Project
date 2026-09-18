package com.example.foodrescue.service;

import com.example.foodrescue.dto.CategoryStat;
import com.example.foodrescue.dto.NetworkStats;
import com.example.foodrescue.model.FoodCategory;
import com.example.foodrescue.model.ItemStatus;
import com.example.foodrescue.repository.FoodItemRepository;
import com.example.foodrescue.repository.FridgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Builds the numbers of the dashboard out of a few aggregation queries
@Service
@Transactional(readOnly = true)
public class StatsService {

    private final FridgeRepository fridgeRepository;
    private final FoodItemRepository itemRepository;

    public StatsService(FridgeRepository fridgeRepository, FoodItemRepository itemRepository) {
        this.fridgeRepository = fridgeRepository;
        this.itemRepository = itemRepository;
    }

    public NetworkStats buildStats() {
        long totalFridges = fridgeRepository.count();
        long activeFridges = fridgeRepository.findByActiveTrueOrderByNameAsc().size();

        long available = itemRepository.countByStatus(ItemStatus.AVAILABLE);
        long claimed = itemRepository.countByStatus(ItemStatus.CLAIMED);
        long expired = itemRepository.countByStatus(ItemStatus.EXPIRED);

        double kgRescued = round(nullToZero(itemRepository.sumWeightByStatus(ItemStatus.CLAIMED)));
        double kgLost = round(nullToZero(itemRepository.sumWeightByStatus(ItemStatus.EXPIRED)));

        return new NetworkStats(totalFridges, activeFridges, available, claimed,
                expired, kgRescued, kgLost, rescuedByCategory(kgRescued));
    }

    private List<CategoryStat> rescuedByCategory(double totalKg) {
        List<CategoryStat> result = new ArrayList<>();
        for (Object[] row : itemRepository.sumWeightGroupedByCategory(ItemStatus.CLAIMED)) {
            FoodCategory category = (FoodCategory) row[0];
            double kg = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            int share = totalKg > 0 ? (int) Math.round(kg * 100.0 / totalKg) : 0;
            result.add(new CategoryStat(category, round(kg), share));
        }
        return result;
    }

    private double nullToZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

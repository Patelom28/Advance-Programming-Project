package com.example.foodrescue.service;

import com.example.foodrescue.dto.FridgeSummary;
import com.example.foodrescue.exception.BusinessRuleException;
import com.example.foodrescue.exception.ResourceNotFoundException;
import com.example.foodrescue.model.Fridge;
import com.example.foodrescue.model.ItemStatus;
import com.example.foodrescue.repository.FoodItemRepository;
import com.example.foodrescue.repository.FridgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final FoodItemRepository itemRepository;

    public FridgeService(FridgeRepository fridgeRepository, FoodItemRepository itemRepository) {
        this.fridgeRepository = fridgeRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Fridge> findAll() {
        return fridgeRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Fridge> findActive() {
        return fridgeRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Fridge findById(Long id) {
        return fridgeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Fridge", id));
    }

    @Transactional(readOnly = true)
    public FridgeSummary summarise(Fridge fridge) {
        long itemsInside = itemRepository.countByFridgeIdAndStatus(fridge.getId(), ItemStatus.AVAILABLE);
        Double kgInside = itemRepository.sumWeightByFridgeAndStatus(fridge.getId(), ItemStatus.AVAILABLE);
        return new FridgeSummary(fridge.getId(), fridge.getName(), fridge.getAddress(),
                fridge.getDistrict(), fridge.getCapacity(), fridge.isActive(),
                itemsInside, kgInside == null ? 0.0 : kgInside);
    }

    public Fridge create(Fridge fridge) {
        fridge.setId(null);
        return fridgeRepository.save(fridge);
    }

    public Fridge update(Long id, Fridge changes) {
        Fridge fridge = findById(id);
        fridge.setName(changes.getName());
        fridge.setAddress(changes.getAddress());
        fridge.setDistrict(changes.getDistrict());
        fridge.setCapacity(changes.getCapacity());
        fridge.setActive(changes.isActive());
        return fridgeRepository.save(fridge);
    }

    // A fridge may only be removed once it holds no available food
    public void delete(Long id) {
        Fridge fridge = findById(id);
        long available = itemRepository.countByFridgeIdAndStatus(id, ItemStatus.AVAILABLE);
        if (available > 0) {
            throw new BusinessRuleException("Fridge '" + fridge.getName()
                    + "' still holds " + available + " available item(s) and cannot be deleted.");
        }
        fridgeRepository.delete(fridge);
    }
}

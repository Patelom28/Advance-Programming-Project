package com.example.foodrescue.service;

import com.example.foodrescue.dto.FoodItemRequest;
import com.example.foodrescue.exception.BusinessRuleException;
import com.example.foodrescue.exception.ResourceNotFoundException;
import com.example.foodrescue.model.FoodItem;
import com.example.foodrescue.model.Fridge;
import com.example.foodrescue.model.ItemStatus;
import com.example.foodrescue.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// All operations on donated food items - see donate() and claim() for the domain rules
@Service
@Transactional
public class FoodItemService {

    private final FoodItemRepository itemRepository;
    private final FridgeService fridgeService;

    public FoodItemService(FoodItemRepository itemRepository, FridgeService fridgeService) {
        this.itemRepository = itemRepository;
        this.fridgeService = fridgeService;
    }

    @Transactional(readOnly = true)
    public FoodItem findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Food item", id));
    }

    @Transactional(readOnly = true)
    public List<FoodItem> findByFridge(Long fridgeId) {
        fridgeService.findById(fridgeId); // makes sure the fridge exists (404 otherwise)
        return itemRepository.findByFridgeIdOrderByExpiryDateAsc(fridgeId);
    }

    @Transactional(readOnly = true)
    public List<FoodItem> findByStatus(ItemStatus status) {
        return itemRepository.findByStatusOrderByExpiryDateAsc(status);
    }

    // Available items that must be taken within the given number of days
    @Transactional(readOnly = true)
    public List<FoodItem> findExpiringSoon(int days) {
        return itemRepository.findByStatusAndExpiryDateLessThanEqualOrderByExpiryDateAsc(
                ItemStatus.AVAILABLE, LocalDate.now().plusDays(days));
    }

    // Rule 1 and 2: donate a new item into a fridge
    public FoodItem donate(Long fridgeId, FoodItemRequest request) {
        Fridge fridge = fridgeService.findById(fridgeId);

        if (!fridge.isActive()) {
            throw new BusinessRuleException("Fridge '" + fridge.getName()
                    + "' is currently out of service and cannot accept donations.");
        }
        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException(
                    "The best-before date has already passed, this food cannot be donated.");
        }
        long available = itemRepository.countByFridgeIdAndStatus(fridgeId, ItemStatus.AVAILABLE);
        if (available >= fridge.getCapacity()) {
            throw new BusinessRuleException("Fridge '" + fridge.getName() + "' is full ("
                    + available + "/" + fridge.getCapacity()
                    + "). Please use another fridge nearby.");
        }

        FoodItem item = new FoodItem(
                request.getName().trim(),
                request.getCategory(),
                request.getQuantityKg(),
                request.getExpiryDate(),
                emptyToNull(request.getDonorName()),
                fridge);
        return itemRepository.save(item);
    }

    // Edit an item that is still available
    public FoodItem update(Long itemId, FoodItemRequest request) {
        FoodItem item = findById(itemId);
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new BusinessRuleException("Only available items can be edited. This item is "
                    + item.getStatus().getLabel().toLowerCase() + ".");
        }
        item.setName(request.getName().trim());
        item.setCategory(request.getCategory());
        item.setQuantityKg(request.getQuantityKg());
        item.setExpiryDate(request.getExpiryDate());
        item.setDonorName(emptyToNull(request.getDonorName()));
        return itemRepository.save(item);
    }

    // Rule 3: somebody takes the food home
    public FoodItem claim(Long itemId, String claimedBy) {
        FoodItem item = findById(itemId);

        if (item.getStatus() == ItemStatus.CLAIMED) {
            throw new BusinessRuleException("This item was already claimed by "
                    + item.getClaimedBy() + ".");
        }
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new BusinessRuleException("This item is no longer available ("
                    + item.getStatus().getLabel().toLowerCase() + ").");
        }
        if (item.isExpired()) {
            // mark it expired before refusing the claim
            item.setStatus(ItemStatus.EXPIRED);
            itemRepository.save(item);
            throw new BusinessRuleException(
                    "This item passed its best-before date and was removed from the offer.");
        }

        item.setStatus(ItemStatus.CLAIMED);
        item.setClaimedBy(claimedBy.trim());
        item.setClaimedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }

    // A volunteer takes an item out, e.g. damaged packaging
    public FoodItem removeFromFridge(Long itemId) {
        FoodItem item = findById(itemId);
        if (item.getStatus() == ItemStatus.CLAIMED) {
            throw new BusinessRuleException("A claimed item cannot be removed any more.");
        }
        item.setStatus(ItemStatus.REMOVED);
        return itemRepository.save(item);
    }

    // Hard delete, used by DELETE /api/items/{id}
    public void delete(Long itemId) {
        FoodItem item = findById(itemId);
        itemRepository.delete(item);
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

package com.example.foodrescue;

import com.example.foodrescue.dto.FoodItemRequest;
import com.example.foodrescue.exception.BusinessRuleException;
import com.example.foodrescue.model.FoodCategory;
import com.example.foodrescue.model.FoodItem;
import com.example.foodrescue.model.Fridge;
import com.example.foodrescue.model.ItemStatus;
import com.example.foodrescue.service.FoodItemService;
import com.example.foodrescue.service.FridgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tests for the three domain rules; each test rolls back its own transaction afterwards
@SpringBootTest
@Transactional
class FoodItemServiceTest {

    @Autowired
    private FridgeService fridgeService;

    @Autowired
    private FoodItemService itemService;

    private Fridge newFridge(String name, int capacity) {
        return fridgeService.create(new Fridge(name, "Teststrasse 1", "Testbezirk", capacity));
    }

    private FoodItemRequest request(String name, LocalDate expiry) {
        FoodItemRequest request = new FoodItemRequest();
        request.setName(name);
        request.setCategory(FoodCategory.BAKERY);
        request.setQuantityKg(1.0);
        request.setExpiryDate(expiry);
        request.setDonorName("Tester");
        return request;
    }

    @Test
    void donatedItemIsStoredAsAvailable() {
        Fridge fridge = newFridge("Unit test fridge A", 5);

        FoodItem item = itemService.donate(fridge.getId(),
                request("Bread", LocalDate.now().plusDays(3)));

        assertEquals(ItemStatus.AVAILABLE, item.getStatus());
        assertEquals(fridge.getId(), item.getFridge().getId());
    }

    @Test
    void fridgeCannotBeFilledBeyondItsCapacity() {
        Fridge fridge = newFridge("Unit test fridge B", 2);
        itemService.donate(fridge.getId(), request("Item 1", LocalDate.now().plusDays(2)));
        itemService.donate(fridge.getId(), request("Item 2", LocalDate.now().plusDays(2)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> itemService.donate(fridge.getId(), request("Item 3", LocalDate.now().plusDays(2))));

        assertTrue(ex.getMessage().contains("full"));
    }

    @Test
    void foodPastItsBestBeforeDateCannotBeDonated() {
        Fridge fridge = newFridge("Unit test fridge C", 5);

        assertThrows(BusinessRuleException.class,
                () -> itemService.donate(fridge.getId(), request("Old milk", LocalDate.now().minusDays(1))));
    }

    @Test
    void anItemCanOnlyBeClaimedOnce() {
        Fridge fridge = newFridge("Unit test fridge D", 5);
        FoodItem item = itemService.donate(fridge.getId(),
                request("Apples", LocalDate.now().plusDays(2)));

        FoodItem claimed = itemService.claim(item.getId(), "Maria");
        assertEquals(ItemStatus.CLAIMED, claimed.getStatus());
        assertEquals("Maria", claimed.getClaimedBy());

        assertThrows(BusinessRuleException.class, () -> itemService.claim(item.getId(), "Paul"));
    }

    @Test
    void anInactiveFridgeRefusesDonations() {
        Fridge fridge = newFridge("Unit test fridge E", 5);
        fridge.setActive(false);
        fridgeService.update(fridge.getId(), fridge);

        assertThrows(BusinessRuleException.class,
                () -> itemService.donate(fridge.getId(), request("Bread", LocalDate.now().plusDays(2))));
    }
}

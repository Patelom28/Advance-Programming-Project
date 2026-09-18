package com.example.foodrescue.controller;

import com.example.foodrescue.dto.ClaimRequest;
import com.example.foodrescue.dto.FoodItemRequest;
import com.example.foodrescue.model.FoodItem;
import com.example.foodrescue.model.ItemStatus;
import com.example.foodrescue.service.FoodItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// REST endpoints for donated food items
@RestController
@RequestMapping("/api/items")
public class FoodItemController {

    private final FoodItemService itemService;

    public FoodItemController(FoodItemService itemService) {
        this.itemService = itemService;
    }

    // GET /api/items?status=AVAILABLE
    @GetMapping
    public List<FoodItem> list(@RequestParam(defaultValue = "AVAILABLE") ItemStatus status) {
        return itemService.findByStatus(status);
    }

    // GET /api/items/expiring?days=2
    @GetMapping("/expiring")
    public List<FoodItem> expiringSoon(@RequestParam(defaultValue = "2") int days) {
        return itemService.findExpiringSoon(days);
    }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public FoodItem getOne(@PathVariable Long id) {
        return itemService.findById(id);
    }

    // PUT /api/items/{id}
    @PutMapping("/{id}")
    public FoodItem update(@PathVariable Long id, @Valid @RequestBody FoodItemRequest request) {
        return itemService.update(id, request);
    }

    // POST /api/items/{id}/claim - take the food home
    @PostMapping("/{id}/claim")
    public FoodItem claim(@PathVariable Long id, @Valid @RequestBody ClaimRequest request) {
        return itemService.claim(id, request.getClaimedBy());
    }

    // POST /api/items/{id}/remove - a volunteer takes the item out
    @PostMapping("/{id}/remove")
    public FoodItem remove(@PathVariable Long id) {
        return itemService.removeFromFridge(id);
    }

    // DELETE /api/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

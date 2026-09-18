package com.example.foodrescue.controller;

import com.example.foodrescue.dto.FoodItemRequest;
import com.example.foodrescue.dto.FridgeSummary;
import com.example.foodrescue.model.FoodItem;
import com.example.foodrescue.model.Fridge;
import com.example.foodrescue.service.FoodItemService;
import com.example.foodrescue.service.FridgeService;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

// REST endpoints for fridges, consumed by the frontend under src/main/resources/static
@RestController
@RequestMapping("/api/fridges")
public class FridgeController {

    private final FridgeService fridgeService;
    private final FoodItemService itemService;

    public FridgeController(FridgeService fridgeService, FoodItemService itemService) {
        this.fridgeService = fridgeService;
        this.itemService = itemService;
    }

    // GET /api/fridges?activeOnly=true
    @GetMapping
    public List<Fridge> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return activeOnly ? fridgeService.findActive() : fridgeService.findAll();
    }

    // GET /api/fridges/{id}
    @GetMapping("/{id}")
    public Fridge getOne(@PathVariable Long id) {
        return fridgeService.findById(id);
    }

    // GET /api/fridges/{id}/summary - fridge with its filling level
    @GetMapping("/{id}/summary")
    public FridgeSummary summary(@PathVariable Long id) {
        return fridgeService.summarise(fridgeService.findById(id));
    }

    // POST /api/fridges
    @PostMapping
    public ResponseEntity<Fridge> create(@Valid @RequestBody Fridge fridge,
                                         UriComponentsBuilder uriBuilder) {
        Fridge saved = fridgeService.create(fridge);
        URI location = uriBuilder.path("/api/fridges/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/fridges/{id}
    @PutMapping("/{id}")
    public Fridge update(@PathVariable Long id, @Valid @RequestBody Fridge fridge) {
        return fridgeService.update(id, fridge);
    }

    // DELETE /api/fridges/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fridgeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/fridges/{id}/items
    @GetMapping("/{id}/items")
    public List<FoodItem> items(@PathVariable Long id) {
        return itemService.findByFridge(id);
    }

    // POST /api/fridges/{id}/items - donate food into this fridge
    @PostMapping("/{id}/items")
    public ResponseEntity<FoodItem> donate(@PathVariable Long id,
                                           @Valid @RequestBody FoodItemRequest request,
                                           UriComponentsBuilder uriBuilder) {
        FoodItem saved = itemService.donate(id, request);
        URI location = uriBuilder.path("/api/items/{itemId}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }
}

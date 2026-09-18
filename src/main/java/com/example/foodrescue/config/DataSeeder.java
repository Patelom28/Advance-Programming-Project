package com.example.foodrescue.config;

import com.example.foodrescue.model.FoodCategory;
import com.example.foodrescue.model.FoodItem;
import com.example.foodrescue.model.Fridge;
import com.example.foodrescue.model.ItemStatus;
import com.example.foodrescue.repository.FoodItemRepository;
import com.example.foodrescue.repository.FridgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Fills the database with example fridges and food on start-up, only when it's still empty
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final FridgeRepository fridgeRepository;
    private final FoodItemRepository itemRepository;

    public DataSeeder(FridgeRepository fridgeRepository, FoodItemRepository itemRepository) {
        this.fridgeRepository = fridgeRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) {
        if (fridgeRepository.count() > 0) {
            return;
        }

        Fridge kreuzberg = fridgeRepository.save(
                new Fridge("Kiezkuehlschrank Wrangelstrasse", "Wrangelstrasse 12", "Kreuzberg", 12));
        Fridge neukoelln = fridgeRepository.save(
                new Fridge("Nachbarschaftshaus Neukoelln", "Richardplatz 5", "Neukoelln", 8));
        Fridge mitte = fridgeRepository.save(
                new Fridge("Campus Fridge Gisma", "Hardenbergstrasse 32", "Mitte", 10));
        Fridge pankow = fridgeRepository.save(
                new Fridge("Buergerhaus Pankow", "Breite Strasse 24a", "Pankow", 6));
        pankow.setActive(false); // out of service: good example for the error handling
        fridgeRepository.save(pankow);

        LocalDate today = LocalDate.now();
        List<FoodItem> items = new ArrayList<>();

        items.add(available("Rye bread, 2 loaves", FoodCategory.BAKERY, 1.4,
                today.plusDays(1), "Baeckerei Otto", kreuzberg));
        items.add(available("Carrots from the market", FoodCategory.FRUIT_VEG, 3.0,
                today.plusDays(4), "Marktstand Erdmann", kreuzberg));
        items.add(available("Natural yoghurt, 6 cups", FoodCategory.DAIRY, 0.9,
                today.plusDays(2), "Sara", kreuzberg));
        items.add(available("Lentil soup, cooked today", FoodCategory.COOKED_MEAL, 2.2,
                today, "Volkskueche", kreuzberg));

        items.add(available("Bananas, slightly spotted", FoodCategory.FRUIT_VEG, 2.5,
                today.plusDays(1), "Biomarkt Richardplatz", neukoelln));
        items.add(available("Rice, unopened pack", FoodCategory.PANTRY, 1.0,
                today.plusMonths(8), "Leon", neukoelln));

        items.add(available("Sandwiches from the canteen", FoodCategory.COOKED_MEAL, 1.8,
                today, "Gisma Mensa", mitte));
        items.add(available("Apples", FoodCategory.FRUIT_VEG, 2.0,
                today.plusDays(6), "Gisma Mensa", mitte));
        items.add(available("Oat milk, 4 cartons", FoodCategory.DAIRY, 4.0,
                today.plusMonths(3), "Supermarkt Hardenberg", mitte));

        // Food that was already rescued in the past days.
        items.add(claimed("Whole grain rolls", FoodCategory.BAKERY, 1.2,
                today.minusDays(1), "Baeckerei Otto", kreuzberg, "Nina", 1));
        items.add(claimed("Tomatoes", FoodCategory.FRUIT_VEG, 2.4,
                today.plusDays(1), "Marktstand Erdmann", kreuzberg, "Jonas", 2));
        items.add(claimed("Pasta bake", FoodCategory.COOKED_MEAL, 2.6,
                today.minusDays(2), "Volkskueche", neukoelln, "Familie Weber", 3));
        items.add(claimed("Cheese, 500 g", FoodCategory.DAIRY, 0.5,
                today.plusDays(3), "Supermarkt Hardenberg", mitte, "Ahmed", 1));

        // Food that nobody took in time.
        items.add(expired("Strawberries", FoodCategory.FRUIT_VEG, 0.8,
                today.minusDays(2), "Marktstand Erdmann", neukoelln));
        items.add(expired("Cream, 2 cups", FoodCategory.DAIRY, 0.4,
                today.minusDays(3), "Supermarkt Hardenberg", mitte));

        itemRepository.saveAll(items);
        log.info("Demo data created: {} fridges and {} food items",
                fridgeRepository.count(), itemRepository.count());
    }

    private FoodItem available(String name, FoodCategory category, double kg,
                               LocalDate expiry, String donor, Fridge fridge) {
        return new FoodItem(name, category, kg, expiry, donor, fridge);
    }

    private FoodItem claimed(String name, FoodCategory category, double kg, LocalDate expiry,
                             String donor, Fridge fridge, String claimedBy, int daysAgo) {
        FoodItem item = new FoodItem(name, category, kg, expiry, donor, fridge);
        item.setStatus(ItemStatus.CLAIMED);
        item.setClaimedBy(claimedBy);
        item.setClaimedAt(LocalDateTime.now().minusDays(daysAgo));
        return item;
    }

    private FoodItem expired(String name, FoodCategory category, double kg, LocalDate expiry,
                             String donor, Fridge fridge) {
        FoodItem item = new FoodItem(name, category, kg, expiry, donor, fridge);
        item.setStatus(ItemStatus.EXPIRED);
        return item;
    }
}

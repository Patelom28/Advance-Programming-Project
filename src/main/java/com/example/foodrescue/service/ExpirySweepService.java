package com.example.foodrescue.service;

import com.example.foodrescue.model.FoodItem;
import com.example.foodrescue.model.ItemStatus;
import com.example.foodrescue.repository.FoodItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Background job: moves items past their best-before date from AVAILABLE to EXPIRED
@Service
public class ExpirySweepService {

    private static final Logger log = LoggerFactory.getLogger(ExpirySweepService.class);

    private final FoodItemRepository itemRepository;

    public ExpirySweepService(FoodItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Scheduled(fixedRateString = "${app.expiry-sweep-rate-ms:60000}")
    public void scheduledSweep() {
        try {
            int updated = sweep();
            if (updated > 0) {
                log.info("Expiry sweep: {} item(s) marked as expired", updated);
            }
        } catch (Exception ex) {
            // never let a background failure stop the application
            log.error("Expiry sweep failed, will try again on the next run", ex);
        }
    }

    // Returns how many items were marked as expired
    @Transactional
    public int sweep() {
        List<FoodItem> stale = itemRepository.findByStatusAndExpiryDateBefore(
                ItemStatus.AVAILABLE, LocalDate.now());
        for (FoodItem item : stale) {
            item.setStatus(ItemStatus.EXPIRED);
        }
        itemRepository.saveAll(stale);
        return stale.size();
    }
}

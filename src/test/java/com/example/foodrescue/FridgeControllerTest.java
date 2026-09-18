package com.example.foodrescue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests the REST layer end to end, including the JSON error shapes from GlobalExceptionHandler
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FridgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unknownFridgeReturns404WithCleanMessage() throws Exception {
        mockMvc.perform(get("/api/fridges/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void creatingAFridgeWithoutANameReturns400WithFieldErrors() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "",
                "address", "",
                "district", "",
                "capacity", 0,
                "active", true);

        mockMvc.perform(post("/api/fridges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void creatingAndFetchingAFridgeWorks() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Test fridge",
                "address", "Teststrasse 1",
                "district", "Testbezirk",
                "capacity", 5,
                "active", true);

        mockMvc.perform(post("/api/fridges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test fridge"));

        mockMvc.perform(get("/api/fridges"))
                .andExpect(status().isOk());
    }
}

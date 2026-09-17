package com.xyz.products;

import com.xyz.products.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void beforeEach() {
        repository.deleteAll();
    }

    private String body(Object o) throws Exception {
        return json.writeValueAsString(o);
    }

    @Test
    void givenValidPayload_whenCreate_thenReturns201WithLocationAndBody() throws Exception {
        String payload = body(Map.of("name", "Widget", "price", 9.99, "category", "tools", "quantity", 5));

        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Widget")))
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    void givenInvalidFields_whenCreate_thenReturns400WithFieldErrors() throws Exception {
        String payload = body(Map.of("name", "", "price", -1, "category", "tools", "quantity", -3));

        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    void givenUnknownId_whenGet_thenReturns404() throws Exception {
        mvc.perform(get("/api/v1/products/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenNoParams_whenList_thenReturnsEverythingInOneEnvelope() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                    .content(body(Map.of("name", "P" + i, "price", 1.00, "category", "c", "quantity", 1))));
        }

        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(3)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void givenPageParam_whenList_thenReturnsPagedEnvelope() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                    .content(body(Map.of("name", "P" + i, "price", 1.00, "category", "c", "quantity", 1))));
        }

        mvc.perform(get("/api/v1/products").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.last", is(false)));
    }

    @Test
    void givenNameAndDescriptionParams_whenList_thenFiltersIndependentlyCaseInsensitively() throws Exception {
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name", "Steel Hammer", "price", 9.99, "category", "tools", "quantity", 1,
                        "description", "heavy duty"))));
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name", "Claw Hammer", "price", 7.00, "category", "tools", "quantity", 1,
                        "description", "lightweight"))));
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("name", "Bolt", "price", 0.10, "category", "parts", "quantity", 1))));

        // name filter alone: both hammers, case-insensitive
        mvc.perform(get("/api/v1/products").param("name", "HAMMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)));

        // name AND description combine: only the "heavy duty" hammer
        mvc.perform(get("/api/v1/products").param("name", "hammer").param("description", "heavy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Steel Hammer")));
    }

    @Test
    void givenProduct_whenCreateGetUpdateDelete_thenLifecycleSucceeds() throws Exception {
        String created = mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name", "Gadget", "price", 5.00, "category", "misc", "quantity", 2))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Gadget")));

        mvc.perform(put("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name", "Gadget v2", "price", 6.50, "category", "misc", "quantity", 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Gadget v2")))
                .andExpect(jsonPath("$.quantity", is(10)));

        mvc.perform(delete("/api/v1/products/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isNotFound());
    }
}

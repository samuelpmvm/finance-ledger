package com.fintech.finance.ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.entity.Tenant;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.CategoryRepository;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.fintech.finance.ledger.repository.UserRepository;
import com.model.category.CategoryDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CategoryControllerTest extends BaseIntegrationTest {

    private static final String API_V1_CATEGORIES = "/api/v1/categories";
    private static final String TEST_CATEGORY_1 = "Food & Dining";
    private static final String TEST_CATEGORY_2 = "Transportation";
    private static final String AUTH_PROVIDER_ID = "kc-user-id";
    private static final String TEST_EMAIL = "test@test.com";
    private static final MediaType CATEGORY_REQUEST_JSON = MediaType.parseMediaType("application/category-request+json");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Test Tenant");
        tenantRepository.save(tenant);

        User user = userRepository.save(new User(AUTH_PROVIDER_ID, tenantId, "testuser", TEST_EMAIL));
        userId = user.getId();

        UserContext.setUserContextData(new UserContextData(userId, tenantId, AUTH_PROVIDER_ID));
    }

    @AfterEach
    void cleanup() {
        categoryRepository.deleteAllByTenantId(tenantId);
        userRepository.deleteById(userId);
        tenantRepository.deleteById(tenantId);
        UserContext.clear();
    }

    @Test
    void shouldCreateCategory() throws Exception {
        CategoryDto categoryDto = createCategoryDto(TEST_CATEGORY_1);

        mockMvc.perform(post(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(CATEGORY_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(TEST_CATEGORY_1))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturnPagedCategories() throws Exception {
        createTestCategory(TEST_CATEGORY_1);
        createTestCategory(TEST_CATEGORY_2);

        mockMvc.perform(get(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        UUID categoryId = createTestCategory(TEST_CATEGORY_1);

        mockMvc.perform(get(API_V1_CATEGORIES + "/{categoryId}", categoryId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value(TEST_CATEGORY_1));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCategory() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get(API_V1_CATEGORIES + "/{categoryId}", nonExistentId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCategoryById() throws Exception {
        UUID categoryId = createTestCategory(TEST_CATEGORY_1);

        mockMvc.perform(delete(API_V1_CATEGORIES + "/{categoryId}", categoryId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_V1_CATEGORIES + "/{categoryId}", categoryId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllCategories() throws Exception {
        createTestCategory(TEST_CATEGORY_1);
        createTestCategory(TEST_CATEGORY_2);

        mockMvc.perform(delete(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        UUID categoryId = createTestCategory(TEST_CATEGORY_1);

        CategoryDto updateDto = createCategoryDto("Updated Category Name");
        updateDto.setId(categoryId);

        mockMvc.perform(put(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(CATEGORY_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category Name"));
    }

    @Test
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get(API_V1_CATEGORIES)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteCategoryWithChildCategories() throws Exception {
        UUID parentCategoryId = createTestCategory(TEST_CATEGORY_1);
        createTestCategoryWithParent(TEST_CATEGORY_2, parentCategoryId);

        mockMvc.perform(delete(API_V1_CATEGORIES + "/{categoryId}", parentCategoryId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isImUsed())
                .andExpect(jsonPath("$.message").value("Category with ID: " + parentCategoryId + " cannot be deleted as it has associated child categories."));
    }

    @Test
    void shouldNotDeleteAllCategoriesWhenAnyHasChildCategories() throws Exception {
        UUID parentCategoryId = createTestCategory(TEST_CATEGORY_1);
        createTestCategoryWithParent(TEST_CATEGORY_2, parentCategoryId);

        mockMvc.perform(delete(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isImUsed())
                .andExpect(jsonPath("$.message").value("Category with ID: " + parentCategoryId + " cannot be deleted as it has associated child categories."));

        mockMvc.perform(get(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    private static CategoryDto createCategoryDto(String name) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(name);
        return categoryDto;
    }

    private UUID createTestCategory(String name) throws Exception {
        CategoryDto categoryDto = createCategoryDto(name);

        String response = mockMvc.perform(post(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(CATEGORY_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, CategoryDto.class).getId();
    }

    private void createTestCategoryWithParent(String name, UUID parentId) throws Exception {
        CategoryDto categoryDto = createCategoryDto(name);
        categoryDto.setParentId(parentId);

        String response = mockMvc.perform(post(API_V1_CATEGORIES)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(CATEGORY_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        objectMapper.readValue(response, CategoryDto.class);
    }
}


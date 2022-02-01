package com.tul.shoppingcart

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProductControllerIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    companion object {
        @Container
        private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:12.2").apply {
            withDatabaseName("shop")
            withUsername("tul")
            withPassword("tul")
        }


        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        }
    }

    /**
     * Test add products Post /products
     */
    @Test
    fun addProducts() {
        webTestClient.post()
            .uri("api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                    {
                        "name":"Pepsi",
                        "sku": "some sku",
                        "description": "some nice description",
                        "price": 5.8
                    }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.name").isEqualTo("Pepsi")
            .jsonPath("$.sku").isEqualTo("some sku")
            .jsonPath("$.description").isEqualTo("some nice description")
            .jsonPath("$.price").isEqualTo(5.8)
    }
}
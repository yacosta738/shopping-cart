package com.tul.shoppingcart

import com.tul.shoppingcart.domain.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.test.web.reactive.server.expectBody
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


    /**
     * Test get products Get /products/{id}
     */
    @Test
    fun getProductById() {
        // insert product first and store the uuid
        val product = Product(
            name = "Coca Cola",
            sku = "some sku",
            description = "some nice description",
            _price = 5.8
        )
        val result = webTestClient.post()
            .uri("api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(product)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Product>()
            .returnResult()

        val productId = result.responseBody!!.id

        // get the product by id
        webTestClient.get()
            .uri("api/products/$productId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Coca Cola")
            .jsonPath("$.sku").isEqualTo("some sku")
            .jsonPath("$.description").isEqualTo("some nice description")
            .jsonPath("$.price").isEqualTo(5.8)
    }

    /**
     * Test get products Get /products
     * this test depends on the previous test
     */

    @Test
    @Order(Int.MAX_VALUE)
    fun getProducts() {
        // list all products
        val result = webTestClient.get()
            .uri("api/products")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<Product>>()
            .returnResult()

        // check if the list is not empty
        assertThat(result.responseBody).isNotEmpty

        // check if the list contains the inserted product
        assertThat(result.responseBody!!.find { it.name == "Coca Cola" }).isNotNull

    }

    /**
     * Update product PUT /products/{id}
     *
     */
    @Test
    fun updateProduct() {
        // insert product first and store the uuid
        val product = Product(
            name = "Mac Pro",
            sku = "some sku",
            description = "M1 Mac Pro",
            _price = 2500.0
        )
        val result = webTestClient.post()
            .uri("api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(product)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Product>()
            .returnResult()

        val productId = result.responseBody!!.id

        // update the product
        webTestClient.put()
            .uri("api/products/$productId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                product.copy(
                    id = productId,
                    name = "MacBook Pro 16 GB",
                    sku = "some sku but this time bigger",
                    description = "The new Mac Pro 16 GB"
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("MacBook Pro 16 GB")
            .jsonPath("$.sku").isEqualTo("some sku but this time bigger")
            .jsonPath("$.description").isEqualTo("The new Mac Pro 16 GB")
            .jsonPath("$.price").isEqualTo(2500.0)
    }

    /**
     * Delete product DELETE /products/{id}
     */
    @Test
    fun deleteProduct() {
        // insert product first and store the uuid
        val product = Product(
            name = "Iphone 13 Pro",
            sku = "some sku",
            description = "Iphone 13 Pro",
            _price = 1000.0
        )
        val result = webTestClient.post()
            .uri("api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(product)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Product>()
            .returnResult()

        val productId = result.responseBody!!.id

        // delete the product
        webTestClient.delete()
            .uri("api/products/$productId")
            .exchange()
            .expectStatus().isNoContent
    }
}
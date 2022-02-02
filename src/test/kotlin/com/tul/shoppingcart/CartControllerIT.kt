package com.tul.shoppingcart

import com.tul.shoppingcart.domain.Cart
import com.tul.shoppingcart.domain.Product
import com.tul.shoppingcart.dto.ProductQuantity
import com.tul.shoppingcart.service.ProductService
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
class CartControllerIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var productService: ProductService

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
     * Test create a cart /carts
     */
    @Test
    fun addProducts() {
        webTestClient.post()
            .uri("api/carts")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.state").isEqualTo("PENDING")
    }

    /**
     * List all carts /carts
     */
    @Test
    fun listCarts() {
        webTestClient.get()
            .uri("api/carts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray
    }

    /**
     * Get a cart by id /carts/{id}
     */
    @Test
    fun getCartById() {
        val cart = webTestClient.post()
            .uri("api/carts")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Cart>()
            .returnResult()
        val cartId = cart.responseBody!!.id
        if (cartId != null) {
            webTestClient.get()
                .uri("api/carts/$cartId")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(cartId.toString())
                .jsonPath("$.state").isEqualTo("PENDING")
        }
    }

    /**
     * Add a product to a cart /carts/{id}/products
     */
    @Test
    fun addProductToCart() {
        val cart = webTestClient.post()
            .uri("api/carts")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Cart>()
            .returnResult()
        val cartId = cart.responseBody!!.id
        var product = Product(
            name = "Coca Cola",
            sku = "some sku",
            description = "some nice description",
            _price = 5.8
        )
        product = productService.saveProduct(product)
        val productQuantity = ProductQuantity (
            productId = product.id!!,
            quantity = 2
        )
        if (cartId != null) {
            webTestClient.post()
                .uri("api/carts/$cartId/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productQuantity)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.state").isEqualTo("PENDING")

            webTestClient.get()
                .uri("api/carts/$cartId")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(cartId.toString())
                .jsonPath("$.state").isEqualTo("PENDING")
                .jsonPath("$.products[0].quantity").isEqualTo(2)
        }
    }

    /**
     * Remove a product from a cart /carts/{id}/products
     */
    @Test
    fun removeProductFromCart() {
        val cart = webTestClient.post()
            .uri("api/carts")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Cart>()
            .returnResult()
        val cartId = cart.responseBody!!.id
        var product = Product(
            name = "Coca Cola",
            sku = "some sku",
            description = "some nice description",
            _price = 5.8
        )
        product = productService.saveProduct(product)
        val productQuantity = ProductQuantity (
            productId = product.id!!,
            quantity = 2
        )
        if (cartId != null) {
            webTestClient.post()
                .uri("api/carts/$cartId/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productQuantity)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.state").isEqualTo("PENDING")

            webTestClient.get()
                .uri("api/carts/$cartId")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(cartId.toString())
                .jsonPath("$.state").isEqualTo("PENDING")
                .jsonPath("$.products[0].quantity").isEqualTo(2)

            webTestClient.delete()
                .uri("api/carts/$cartId/products/${product.id}")
                .exchange()
                .expectStatus().isNoContent

        }
        // check if the product is removed from the cart
        webTestClient.get()
            .uri("api/carts/$cartId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(cartId.toString())
            .jsonPath("$.state").isEqualTo("PENDING")
            .jsonPath("$.products").isEmpty
    }
}
package com.tul.shoppingcart.controller

import com.tul.shoppingcart.domain.Cart
import com.tul.shoppingcart.domain.Product
import com.tul.shoppingcart.dto.ProductQuantity
import com.tul.shoppingcart.service.ProductService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CartControllerTest {

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
        val productQuantity = ProductQuantity(
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
        val productQuantity = ProductQuantity(
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

    /**
     * Update a product from a cart /carts/{id}/products
     */
    @Test
    fun updateProductFromCart() {
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
        val productQuantity = ProductQuantity(
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

            webTestClient.put()
                .uri("api/carts/$cartId/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ProductQuantity(productId = product.id!!, quantity = 3))
                .exchange()
                .expectStatus().isOk

        }
        // check if the product is removed from the cart
        webTestClient.get()
            .uri("api/carts/$cartId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(cartId.toString())
            .jsonPath("$.state").isEqualTo("PENDING")
            .jsonPath("$.products[0].quantity").isEqualTo(3)
    }

    /**
     * Delete a product from a cart /carts/{id}/products
     */
    @Test
    fun deleteProductFromCart() {
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
        val productQuantity = ProductQuantity(
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

    /**
     * Get total price of a cart /carts/{id}/total-price
     */
    @Test
    fun getTotalPriceOfCart() {
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
            _price = 5.85
        )
        product = productService.saveProduct(product)
        val productQuantity = ProductQuantity(
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
                .uri("api/carts/$cartId/total-price")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isEqualTo(product.price * productQuantity.quantity)
        }
    }

    /**
     * Get total price of a cart /carts/{id}/total-price
     * With products with discount
     */
    @Test
    fun getTotalPriceOfCartWithDiscount() {
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
            _price = 5.85
        )
        product = productService.saveProduct(product)
        val productQuantity = ProductQuantity(
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
                .uri("api/carts/$cartId/total-price")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isEqualTo(product.price * productQuantity.quantity)

            val currentPrice = product.price * productQuantity.quantity
            var pepsi = Product(
                name = "Pepsi",
                sku = "some sku",
                description = "some nice description",
                hasDiscount = true, // the half price discount
                _price = 10.0
            )
            pepsi = productService.saveProduct(pepsi)
            val pepsiQuantity = ProductQuantity(
                productId = pepsi.id!!,
                quantity = 12
            )
            webTestClient.post()
                .uri("api/carts/$cartId/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pepsiQuantity)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.state").isEqualTo("PENDING")

            webTestClient.get()
                .uri("api/carts/$cartId/total-price")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isEqualTo((pepsi.price * pepsiQuantity.quantity)  + currentPrice)
        }
    }

    /**
     * Get all products of a cart /carts/{id}/products
     */
    @Test
    fun getAllProductsOfCart() {
        val cart = webTestClient.post()
            .uri("api/carts")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Cart>()
            .returnResult()
        val cartId = cart.responseBody!!.id
        var product = Product(
            name = "Another product",
            sku = "some sku",
            description = "some nice description",
            _price = 565.85
        )
        product = productService.saveProduct(product)
        val productQuantity = ProductQuantity(
            productId = product.id!!,
            quantity = 20
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
                .uri("api/carts/$cartId/products")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$[0].first.name").isEqualTo(product.name!!)
                .jsonPath("$[0].second").isEqualTo(productQuantity.quantity)
        }
    }

    /**
     * checkout a cart /carts/{id}/checkout
     */
    @Test
    fun checkoutCart() {
        val cart = webTestClient.post()
            .uri("api/carts")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Cart>()
            .returnResult()
        val cartId = cart.responseBody!!.id
        var product = Product(
            name = "Another product",
            sku = "some sku",
            description = "some nice description",
            _price = 565.85
        )
        product = productService.saveProduct(product)
        val productQuantity = ProductQuantity(
            productId = product.id!!,
            quantity = 20
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

            val currentPrice = product.price * productQuantity.quantity
            var pepsi = Product(
                name = "Pepsi",
                sku = "some sku",
                description = "some nice description",
                hasDiscount = true, // the half price discount
                _price = 10.0
            )
            pepsi = productService.saveProduct(pepsi)
            val pepsiQuantity = ProductQuantity(
                productId = pepsi.id!!,
                quantity = 12
            )
            webTestClient.post()
                .uri("api/carts/$cartId/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pepsiQuantity)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.state").isEqualTo("PENDING")


            webTestClient.post()
                .uri("api/carts/$cartId/checkout")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isNotEmpty
                .jsonPath("$").isEqualTo((pepsi.price * pepsiQuantity.quantity)  + currentPrice)

            // check if the cart is checked out. State should be COMPLETED
            webTestClient.get()
                .uri("api/carts/$cartId")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.state").isEqualTo("COMPLETED")
        }
    }


}
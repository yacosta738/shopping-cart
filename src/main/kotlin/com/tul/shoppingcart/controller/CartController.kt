package com.tul.shoppingcart.controller

import com.tul.shoppingcart.domain.Cart
import com.tul.shoppingcart.domain.Product
import com.tul.shoppingcart.dto.ProductQuantity
import com.tul.shoppingcart.service.CartService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.*

/**
 * REST controller for managing [com.tul.shoppingcart.domain.Cart].
 * @author Acosta
 */
@RestController
@RequestMapping("/api")
class CartController(private val cartService: CartService) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * GET  /carts : get all the carts.
     * @return the  [ResponseEntity] with status 200 (OK) and the list of carts in body
     *
     */
    @GetMapping("/carts")
    fun getAll(): List<Cart> {
        log.debug("REST request to get all Carts")
        return cartService.getCarts()
    }

    /**
     * GET  /carts/:id : get the "id" cart.
     * @return the  [ResponseEntity] with status 200 (OK) and with body the cart, or with status 404 (Not Found)
     */
    @GetMapping("/carts/{id}")
    fun getCart(@PathVariable id: UUID): ResponseEntity<Cart> {
        log.debug("REST request to get Cart : {}", id)
        val cart = cartService.getCart(id)
        return ResponseEntity.ok()
            .body(cart.map { it }.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) })
    }

    /**
     * POST /carts : Create a new cart.
     * @return the ResponseEntity with status 201 (Created) and with body the new cart, or with status 400 (Bad Request) if the cart has already an ID
     */
    @PostMapping("/carts")
    fun createCart(): ResponseEntity<Cart> {
        log.debug("REST request to add product to cart")
        val cart = cartService.createCart()
        return ResponseEntity.created(URI("/api/carts/${cart.id}"))
            .body(cart)
    }
    /**
     * POST  /carts/:id/products : Add product to cart.
     * @param id the id of the cart to add product.
     * @param product the product to add.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */

    @PostMapping("/carts/{id}/products")
    fun addProductToCart(@PathVariable id: UUID, @RequestBody product: ProductQuantity): ResponseEntity<Cart> {
        log.debug("REST request to add product to cart : $id")
        val cart = cartService.addProductToCart(id, product.productId, product.quantity)
        return ResponseEntity.created(URI("/api/carts/${cart.id}/products"))
            .body(cart)
    }

    /**
     * DELETE  /carts/:id/products : Remove product from cart.
     * @param id the id of the cart to remove product.
     * @param productId the id of the product to remove.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @DeleteMapping("/carts/{id}/products/{productId}")
    fun removeProductFromCart(@PathVariable id: UUID, @PathVariable productId: UUID): ResponseEntity<Cart> {
        log.debug("REST request to remove product from cart : $id")
        cartService.removeProductFromCart(id, productId)
        return ResponseEntity.noContent()
            .build()
    }
    /**
     * PUT /carts/:id/products : Update product quantity in cart.
     * @param id the id of the cart to update product.
     * @param product the product to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @PutMapping("/carts/{id}/products")
    fun updateProductQuantityInCart(@PathVariable id: UUID, @RequestBody product: ProductQuantity): ResponseEntity<Cart> {
        log.debug("REST request to update product quantity in cart : $id")
        val cart = cartService.updateProductQuantityInCart(id, product.productId, product.quantity)
        return ResponseEntity.ok()
            .body(cart)
    }

    /**
     * GET /carts/:id/total-price : Get total price of cart.
     * @param id the id of the cart to get total price.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @GetMapping("/carts/{id}/total-price")
    fun getTotalPrice(@PathVariable id: UUID): ResponseEntity<Double> {
        log.debug("REST request to get total price of cart : $id")
        val totalPrice = cartService.getTotalPrice(id)
        return ResponseEntity.ok().body(totalPrice)
    }

    /**
     * GET /carts/{id}/products : Get all products in cart.
     * @param id the id of the cart to get all products.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @GetMapping("/carts/{id}/products")
    fun getAllProductsInCart(@PathVariable id: UUID): ResponseEntity<List<Pair<Product?, Int>>> {
        log.debug("REST request to get all products in cart : $id")
        val products = cartService.getAllProductsInCart(id)
        return ResponseEntity.ok().body(products)
    }

    /**
     * POST /carts/:id/checkout : Checkout cart.
     * @param id the id of the cart to check out.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the total price, or with status `404 (Not Found)`.
     * */
    @PostMapping("/carts/{id}/checkout")
    fun checkoutCart(@PathVariable id: UUID): ResponseEntity<Double> {
        log.debug("REST request to checkout cart : $id")
        val totalPrices = cartService.checkout(id)
        return ResponseEntity.ok().body(totalPrices)
    }
}
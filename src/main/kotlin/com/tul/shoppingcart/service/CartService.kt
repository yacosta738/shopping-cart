package com.tul.shoppingcart.service

import com.tul.shoppingcart.domain.Cart
import com.tul.shoppingcart.domain.Product
import com.tul.shoppingcart.domain.enumeration.CartState
import com.tul.shoppingcart.repository.CartRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val CART_NOT_OPEN = "Cart is not open"

private const val CART_NOT_FOUND = "Cart not found"

/**
 * Service class for cart operations
 * @author acosta
 */
@Service
@Transactional
class CartService(
    private val cartRepository: CartRepository, private val cartItemService: CartItemService,
    private val productService: ProductService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Get a cart by id
     */
    @Transactional(readOnly = true)
    fun getCart(cartId: UUID): Optional<Cart> {
        log.debug("Request to get cart : {}", cartId)
        return cartRepository.findById(cartId)
    }

    /**
     * Get all carts
     */
    @Transactional(readOnly = true)
    fun getCarts(): List<Cart> {
        log.debug("Request to get all carts")
        return cartRepository.findAll()
    }

    /**
     * Create a cart
     */
    fun createCart(): Cart {
        log.debug("Request to create cart")
        return cartRepository.save(Cart())
    }

    /**
     * Add product to cart.
     * @param cartId cart id
     * @param productId product to add
     * @param quantity quantity
     * @return cart with added product
     */
    fun addProductToCart(cartId: UUID, productId: UUID, quantity: Int): Cart {
        log.debug("Request to add product to cart : $cartId, $productId, $quantity")
        val cart = getCart(cartId).orElseThrow { IllegalArgumentException(CART_NOT_FOUND) }
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException(CART_NOT_OPEN)
        }
        val product = productService.getProduct(productId).get()
        cartItemService.addProduct(cart, product, quantity)

        return cartRepository.save(cart)
    }

    /**
     * Remove product from cart.
     * @param cartId cart id
     * @param productId product to remove
     * @param quantity quantity
     * @return cart with removed product
     */
    fun removeProductFromCart(cartId: UUID, productId: UUID, quantity: Int = 1): Cart {
        log.debug("Request to remove product from cart : $cartId, $productId, $quantity")
        val cart = getCart(cartId).orElseThrow { IllegalArgumentException(CART_NOT_FOUND) }
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException(CART_NOT_OPEN)
        }
        val product = productService.getProduct(productId).get()
        cartItemService.removeProduct(cart, product, quantity)

        return cartRepository.save(cart)
    }

    /**
     * Update product quantity in cart.
     * @param cartId cart id
     * @param productId product to update
     * @param quantity quantity
     * @return cart with updated product
     */
    fun updateProductQuantityInCart(cartId: UUID, productId: UUID, quantity: Int = 1): Cart {
        log.debug("Request to update product quantity in cart : $cartId, $productId, $quantity")
        val cart = getCart(cartId).orElseThrow { IllegalArgumentException(CART_NOT_FOUND) }
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException(CART_NOT_OPEN)
        }
        val product = productService.getProduct(productId).get()
        cartItemService.updateProductQuantity(cart, product, quantity)

        return cartRepository.save(cart)
    }

    /**
     * Get cart products.
     * @param cartId cart id
     * @return cart products
     */
    fun getAllProductsInCart(cartId: UUID): List<Pair<Product?, Int>> {
        log.debug("Request to get cart products : $cartId")

        val cartItems = cartItemService.getCartItemsByCartId(cartId)
        // return list of products with quantity
        return cartItems.map { Pair(it.product, it.quantity) }
    }

    /**
     * Get total price.
     * @param cartId cart id
     * @return cart products
     */
    fun getTotalPrice(cartId: UUID): Double {
        log.debug("Request to get cart total price : $cartId")

        val cartItems = cartItemService.getCartItemsByCartId(cartId)
        return cartItems.sumOf { it.product!!.price * it.quantity }
    }

    /**
     * Checkout, return the final cost of the products in the cart and change its status to "COMPLETED".
     * @param cartId cart id
     * @return cart
     */
    fun checkout(cartId: UUID): Double {
        log.debug("Request to checkout cart : $cartId")
        val cart = getCart(cartId).orElseThrow { IllegalArgumentException(CART_NOT_FOUND) }
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException(CART_NOT_OPEN)
        }
        val totalPrice = getTotalPrice(cartId)
        cart.state = CartState.COMPLETED
        cartRepository.save(cart)
        return totalPrice
    }
}
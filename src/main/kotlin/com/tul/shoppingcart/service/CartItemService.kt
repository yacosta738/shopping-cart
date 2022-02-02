package com.tul.shoppingcart.service

import com.tul.shoppingcart.domain.Cart
import com.tul.shoppingcart.domain.CartItem
import com.tul.shoppingcart.domain.Product
import com.tul.shoppingcart.repository.CartItemRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service class for cart item
 * @author acosta
 */
@Service
@Transactional
class CartItemService(private val cartItemRepository: CartItemRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getCartItem(id: Long): CartItem? {
        log.info("Getting cart item with id: $id")
        return cartItemRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun getCartItems(): List<CartItem> {
        log.info("Getting all cart items")
        return cartItemRepository.findAll()
    }

    fun saveCartItem(cartItem: CartItem): CartItem {
        log.info("Saving cart item: $cartItem")
        return cartItemRepository.save(cartItem)
    }

    fun deleteCartItem(id: Long) {
        log.info("Deleting cart item with id: $id")
        cartItemRepository.deleteById(id)
    }

    fun deleteAllCartItems() {
        log.info("Deleting all cart items")
        cartItemRepository.deleteAll()
    }

    /**
     * add product to cart
     * @param cart cart
     * @param product product
     * @param quantity quantity
     * @return cart
     */
    fun addProduct(cart: Cart, product: Product, quantity: Int) {
        log.debug("Request to add product to cart")

        val cartItem = CartItem(cart = cart, product = product, quantity = quantity)
        cartItemRepository.save(cartItem)
    }

    /**
     * remove product from cart
     * @param cart cart
     * @param product product
     * @param quantity quantity
     * @return cart
     */
    fun removeProduct(cart: Cart, product: Product, quantity: Int) {
        log.debug("Request to remove product from cart")

        val cartItem = cart.id?.let { product.id?.let { it1 -> cartItemRepository.findByCartIdAndProductId(it, it1) } }
        cartItem?.let { cartItemRepository.delete(it) }
    }
    /**
     * update product quantity in cart
     * @param cart cart
     * @param product product
     * @param quantity quantity
     * @return cart item
     */
    fun updateProductQuantity(cart: Cart, product: Product, quantity: Int) {
        log.debug("Request to update product quantity in cart")

        val cartItem = cart.id?.let { product.id?.let { it1 -> cartItemRepository.findByCartIdAndProductId(it, it1) } }
        cartItem?.let {
            it.quantity = quantity
            cartItemRepository.save(it)
        }
    }

    /**
     * get cart items by cart id
     * @param cartId cart id
     * @return cart items
     */
    fun getCartItemsByCartId(cartId: UUID): MutableList<CartItem> {
        log.debug("Request to get cart items by cart id")

        return cartItemRepository.findAllByCartId(cartId)
    }
}

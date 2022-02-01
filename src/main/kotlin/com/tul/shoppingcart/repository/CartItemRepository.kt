package com.tul.shoppingcart.repository

import com.tul.shoppingcart.domain.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Spring Data SQL repository for the [CartItem] entity.
 * @author acosta
 */
@Suppress("unused")
@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCartIdAndProductId(cartId: UUID, productId: UUID): CartItem?

    // find all cart items by cart id and return a list of cart items
    @Query("SELECT c FROM CartItem c WHERE c.cart.id = ?1")
    fun findAllByCartId(cartId: UUID): MutableList<CartItem>
}

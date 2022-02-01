package com.tul.shoppingcart.repository

import com.tul.shoppingcart.domain.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Spring Data SQL repository for the [Cart] entity.
 * @author acosta
 */
@Suppress("unused")
@Repository
interface CartRepository : JpaRepository<Cart, UUID> {
}

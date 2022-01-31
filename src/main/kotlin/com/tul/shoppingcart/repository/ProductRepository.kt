package com.tul.shoppingcart.repository

import com.tul.shoppingcart.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Spring Data SQL repository for the [Product] entity.
 */
@Suppress("unused")
@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
}

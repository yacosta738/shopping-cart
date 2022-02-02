package com.tul.shoppingcart.service

import com.tul.shoppingcart.domain.Product
import com.tul.shoppingcart.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import org.springframework.transaction.annotation.Transactional

/**
 * Service class for Product
 * @author acosta
 */
@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository // Injected ProductRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Get all products
     * @return List of products
     */
    @Transactional(readOnly = true)
    fun getAllProducts(): List<Product> {
        log.info("Getting all products")
        return productRepository.findAll()
    }

    /**
     * Get product by id
     * @param id Product id
     * @return Product
     */
    @Transactional(readOnly = true)
    fun getProduct(id: UUID): Optional<Product> {
        log.info("Getting product with id: $id")
        return productRepository.findById(id)
    }

    /**
     * Create a new product
     * @param product Product to create
     * @return Product created
     */
    fun saveProduct(product: Product): Product {
        log.info("Saving product: $product")
        // trim product name
        product.name = product.name?.trim()
        return productRepository.save(product)
    }

    /**
     * Delete a product by id
     * @param id Product id
     *
     */
    fun deleteProduct(id: UUID) {
        log.info("Deleting product with id: $id")
        productRepository.deleteById(id)
    }

}
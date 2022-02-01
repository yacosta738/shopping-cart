package com.tul.shoppingcart.controller

import com.tul.shoppingcart.domain.Product
import com.tul.shoppingcart.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api")
class ProductController(private val productService: ProductService) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * `GET /products` Get all products.
     * @return the list of products.
     * *
     */
    @GetMapping("/products")
    fun getAllProducts(): List<Product> {
        log.debug("REST request to get all Products")
        return productService.getAllProducts()
    }

    /**
     *  `GET /products/{id}` Get a product by id.
     *  @param id the id of the product to retrieve.
     *  @return the ResponseEntity with status 200 (OK) and with body the product, or with status 404 (Not Found)
     */
    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: UUID): ResponseEntity<Product> {
        log.debug("REST request to get Product : {}", id)
        val product = productService.getProduct(id)
        return ResponseEntity.ok()
            .body(product.map { it }.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) })
    }

    /**
     * `POST /products` Create a new product.
     * @param product the product to create.
     * @return the ResponseEntity with status 201 (Created) and with body the new product, or with status 400 (Bad Request) if the product has already an ID
     */
    @PostMapping("/products")
    fun createProduct(@RequestBody product: Product): ResponseEntity<Product> {
        log.debug("REST request to save Product : {}", product)
        if (product.id != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A new product cannot already have an ID")
        }
        val result = productService.saveProduct(product)
        return ResponseEntity.created(URI("/api/products/${result.id}"))
            .body(result)
    }

}
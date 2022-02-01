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
import javax.validation.Valid

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
    fun createProduct(@Valid @RequestBody product: Product): ResponseEntity<Product> {
        log.debug("REST request to save Product : {}", product)
        if (product.id != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A new product cannot already have an ID")
        }
        val result = productService.saveProduct(product)
        return ResponseEntity.created(URI("/api/products/${result.id}"))
            .body(result)
    }

    /**
     * `PUT /products/{id}` Update an existing product.
     * @param id the id of the product to update.
     * @param product the product to update.
     * @return the ResponseEntity with status 200 (OK) and with body the updated product,
     *          or with status 400 (Bad Request) if the product is not valid,
     *          or with status 500 (Internal Server Error) if the product couldn't be updated
     *          or with status 404 (Not Found) if the product with the given id doesn't exist
     *
     */
    @PutMapping("/products/{id}")
    fun updateProduct(@PathVariable id: UUID, @Valid @RequestBody product: Product): ResponseEntity<Product> {
        log.debug("REST request to update Product : {}, {}", id, product)
        if (product.id == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "An existing product must have an ID")
        }
        if (!Objects.equals(id, product.id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path must match ID in request body")
        }
        // find if the product exists
        val existingProduct = productService.getProduct(id)
        if (existingProduct.isEmpty) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")
        }
        val result = productService.saveProduct(product)
        return ResponseEntity.ok()
            .body(result)
    }

    /**
     * `DELETE /products/{id}` Delete a product by id.
     * @param id the id of the product to delete.
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: UUID): ResponseEntity<Void> {
        log.debug("REST request to delete Product : {}", id)
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }
}
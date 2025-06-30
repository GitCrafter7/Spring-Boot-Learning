package com.example.spring_boot_learning.controller

import com.example.spring_boot_learning.controller.utils.GenericResponse
import com.example.spring_boot_learning.database.model.Product
import com.example.spring_boot_learning.database.repository.ProductRepository
import com.example.spring_boot_learning.database.repository.UserRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.bson.types.ObjectId
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    val productRepository: ProductRepository,
    val userRepository: UserRepository
) {

    data class AddProductRequest(
        val name: String,
        val description: String,
        val price: Double,
        val category: String,
        val image: String? = null,
        val stock: Int = 0,
        val brand: String,
        val rating: Double
    )

    data class InsightRequest
        (
        @field:NotNull(message = "Price must not be null")
        @field:Positive(message = "Price must be greater than 0")
        val price: Double
    )

    @PostMapping("/add")
    fun addProduct(
        @Valid @RequestBody request: AddProductRequest,
        @AuthenticationPrincipal principal: Any
    ): ResponseEntity<GenericResponse> {
        return try {
            val userId = principal.toString()

            val user = userRepository.findById(ObjectId(userId)).orElseThrow {
                RuntimeException("User not found")
            }
            val product = Product(
                name = request.name,
                description = request.description,
                price = request.price,
                category = request.category,
                image = request.image,
                stock = request.stock,
                brand = request.brand,
                rating = request.rating
            )
            productRepository.save(product)
            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Product added successfully",
                    data = mapOf(
                        "id" to product.id.toHexString(),
                        "name" to product.name,
                        "description" to product.description,
                        "price" to product.price,
                        "category" to product.category,
                        "image" to product.image,
                        "stock" to product.stock,
                        "brand" to product.brand,
                        "rating" to product.rating
                    )
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                GenericResponse(
                    status = 500,
                    message = "Failed to add product: ${e.message}"
                )
            )
        }


    }

    @GetMapping("/")
    fun getAllProducts(
        @AuthenticationPrincipal principal: Any,
    ): ResponseEntity<GenericResponse> {
        return try {
            val userId = principal.toString()

            val user = userRepository.findById(ObjectId(userId)).orElseThrow {
                RuntimeException("User not found")
            }


            val products = productRepository.findAll()  // Correct repository used here
            if (products.isEmpty()) {
                ResponseEntity.ok(
                    GenericResponse(
                        status = 200,
                        message = "No products found",
                    )
                )
            } else {
                ResponseEntity.ok(
                    GenericResponse(
                        status = 200,
                        message = "Products fetched successfully",
                        data = products
                    )
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                GenericResponse(
                    status = 500,
                    message = "Failed to fetch products: ${e.message}"
                )
            )
        }
    }


    @PostMapping("/insight")
    fun getProductInsight(
        @AuthenticationPrincipal principal: Any,
        @Valid @RequestBody body: InsightRequest
    ): ResponseEntity<GenericResponse> {
        return try {
            val userId = principal.toString()

            val user = userRepository.findById(ObjectId(userId)).orElseThrow {
                RuntimeException("User not found")
            }

            val totalProducts = productRepository.count()
            val maxRating = productRepository.findByRatingGreaterThanEqual(
                rating = 4.0
            ).size
            val productsBelowPrice = productRepository.findByPriceLessThan(
                    body.price
            )

            if (productsBelowPrice.isEmpty()) {
                return ResponseEntity.ok(
                    GenericResponse(
                        status = 200,
                        message = "No products found below the specified price",
                        data = mapOf(
                            "totalProducts" to totalProducts,
                            "maxRatingProducts" to maxRating,
                            "productsBelowPrice" to emptyList<Product>()
                        )
                    )
                )
            }


            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Product insight fetched successfully Max Rating Products(>= 4.0) ",
                    data = mapOf(
                        "totalProducts" to totalProducts,
                        "maxRatingProducts" to maxRating,
                        "productsBelowPrice" to productsBelowPrice
                    )
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                GenericResponse(
                    status = 500,
                    message = "Failed to fetch product insight: ${e.message}"
                )
            )
        }
    }


}
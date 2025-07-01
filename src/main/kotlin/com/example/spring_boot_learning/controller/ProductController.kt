package com.example.spring_boot_learning.controller

import com.example.spring_boot_learning.controller.utils.GenericResponse
import com.example.spring_boot_learning.database.model.Product
import com.example.spring_boot_learning.database.model.toProductResponse
import com.example.spring_boot_learning.database.repository.ProductRepository
import com.example.spring_boot_learning.database.repository.UserRepository
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    val productRepository: ProductRepository,
    val userRepository: UserRepository,
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
                rating = request.rating,
            )
            productRepository.save(product)
            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Product added successfully",
                    data = mapOf(
                        "id" to product.id.toString(),
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


            val products = productRepository.findAll().map{
                it.toProductResponse()
            }  // Correct repository used here
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


    @GetMapping("/insight")
    fun getProductInsight(
        @AuthenticationPrincipal principal: Any,
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
            val maxPrice = productRepository.findFirstByOrderByPriceDesc().firstOrNull()?.price ?: 0.0
            val minPrice = productRepository.findAll().minByOrNull { it.price }?.price ?: 0.0
            val avgPrice = productRepository.findAvgPriceOfProducts()


            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Product insight fetched successfully Max Rating Products(>= 4.0) ",
                    data = mapOf(
                        "totalProducts" to totalProducts,
                        "maxRatingProducts" to maxRating,
                        "MaxPrice" to maxPrice,
                        "MinPrice" to minPrice,
                        "AvgPrice" to avgPrice
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

    @DeleteMapping("/{id}")
    fun deleteProductById(
        @AuthenticationPrincipal principal: Any,
        @PathVariable id:String): ResponseEntity<GenericResponse?> {
        return try {
            val userId = principal.toString()
            val user = userRepository.findById(ObjectId(userId)).orElseThrow {
                RuntimeException("User not found")
            }

            val product =  productRepository.findById(ObjectId(id)).orElseThrow{
                RuntimeException("Product Doesn't Exist")
            }
            productRepository.deleteById(ObjectId(id))
            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Deleted Successfully",
                )
            )
        }catch (
            e: Exception
        ){
            ResponseEntity.status(500).body(
                GenericResponse(
                    status = 500,
                    message = "Failed to delete product: ${e.message}"
                )
            )
        }


    }


    @GetMapping("/search")
    fun getProductById(
        @AuthenticationPrincipal principal: Any,
        @RequestParam id: String): ResponseEntity<GenericResponse> {
        return try {
            val product = productRepository.findById(ObjectId(id)).orElseThrow {
                RuntimeException("Product not found")
            }
            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Product fetched successfully",
                    data = product
                )
            )
        }catch (
            e: Exception
        ){
            ResponseEntity.status(404).body(
                GenericResponse(
                    status = 404,
                    message = "Product not found: ${e.message}",
                    data = null
                )
            )
        }
    }
}
package com.example.spring_boot_learning.database.repository

import com.example.spring_boot_learning.database.model.Product
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ProductRepository: MongoRepository<Product, ObjectId> , ProductRepositoryCustom {
    fun findByRatingGreaterThanEqual(rating: Double): List<Product>
    fun findByPriceLessThan(price : Double): List<Product>
    fun findFirstByOrderByPriceDesc() : List<Product>

}

interface ProductRepositoryCustom {
    fun findAvgPriceOfProducts(): Double?
}
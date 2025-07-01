package com.example.spring_boot_learning

import com.example.spring_boot_learning.database.repository.ProductRepository
import com.example.spring_boot_learning.database.repository.ProductRepositoryCustom
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl (
    private val mongoTemplate: MongoTemplate
): ProductRepositoryCustom{
    override fun findAvgPriceOfProducts(): Double? {
        val aggregation = Aggregation.newAggregation(
            Aggregation.group().avg("price"). `as` ("AvgPrice")
        )
        val result = mongoTemplate.aggregate(aggregation,"products", Document::class.java)
        return result.uniqueMappedResult?.getDouble("AvgPrice")
    }

}
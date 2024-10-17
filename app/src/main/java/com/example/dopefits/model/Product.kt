package com.example.dopefits.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int = 0,
    val title: String = "",
    val categoryId: Int = 0,
    val condition: String = "",
    val description: String = "",
    val brand: String = "",
    val picUrl: List<String> = emptyList(),
    val price: Double = 0.0,
    val issue: String = "",
    val size: String = "",
    val dimensions: String = "",
    val stability: Int = 0,
    val quantity: Int = 1 // Default quantity is 1
) : Parcelable
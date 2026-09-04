package com.example.data.model

data class AsmaAllah(
    val id: Int,
    val nameArabic: String,
    val meaning: String = "",
    val number: Int = id,
    val meaningArabic: String = meaning,
    val benefitOrEvidence: String = ""
)

data class HadithWisdom(
    val id: Int,
    val text: String,
    val narrator: String,
    val source: String,
    val explanation: String = ""
)

package com.example.data.model

import androidx.annotation.DrawableRes

enum class GalleryCategory(val title: String) {
    HOLY_MOSQUES("المساجد الثلاثة"),
    HISTORIC_ARCHITECTURE("العمارة الإسلامية"),
    CALLIGRAPHY("الخط والزخرفة"),
    WALLPAPERS("خلفيات فاخرة")
}

data class IslamicArtwork(
    val id: Int,
    val title: String,
    val subtitle: String,
    val location: String,
    val description: String,
    val category: GalleryCategory,
    @DrawableRes val localDrawableRes: Int,
    val imageUrl: String,
    val tags: List<String> = emptyList()
)

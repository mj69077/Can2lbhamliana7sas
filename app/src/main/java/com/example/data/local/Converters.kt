package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AthkarCategory
import com.example.data.model.DuaCategory
import com.example.data.model.FatwaCategory
import com.example.data.model.RulingType
import com.example.data.model.TaskCategory

class Converters {
    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = try {
        TaskCategory.valueOf(value)
    } catch (e: Exception) {
        TaskCategory.QURAN
    }

    @TypeConverter
    fun fromDuaCategory(category: DuaCategory): String = category.name

    @TypeConverter
    fun toDuaCategory(value: String): DuaCategory = try {
        DuaCategory.valueOf(value)
    } catch (e: Exception) {
        DuaCategory.DAILY
    }

    @TypeConverter
    fun fromAthkarCategory(category: AthkarCategory): String = category.name

    @TypeConverter
    fun toAthkarCategory(value: String): AthkarCategory = try {
        AthkarCategory.valueOf(value)
    } catch (e: Exception) {
        AthkarCategory.GENERAL
    }

    @TypeConverter
    fun fromFatwaCategory(category: FatwaCategory): String = category.name

    @TypeConverter
    fun toFatwaCategory(value: String): FatwaCategory = try {
        FatwaCategory.valueOf(value)
    } catch (e: Exception) {
        FatwaCategory.ALL
    }

    @TypeConverter
    fun fromRulingType(ruling: RulingType): String = ruling.name

    @TypeConverter
    fun toRulingType(value: String): RulingType = try {
        RulingType.valueOf(value)
    } catch (e: Exception) {
        RulingType.PERMISSIBLE
    }
}

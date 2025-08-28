package com.enlightenment.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val duration: Int,
    val questions: String, // JSON
    val createdAt: Long,
    val childAge: Int?,
    val lastPlayedAt: Long? = null,
    val playCount: Int = 0
)

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromQuestionList(questions: List<QuestionEntity>): String {
        return gson.toJson(questions)
    }
    
    @TypeConverter
    fun toQuestionList(json: String): List<QuestionEntity> {
        val type = object : TypeToken<List<QuestionEntity>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}

data class QuestionEntity(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String?
)
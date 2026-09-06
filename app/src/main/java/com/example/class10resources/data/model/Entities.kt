package com.example.class10resources.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val link: String?,
    val filename: String?,
    val createdAt: String
)

@Entity(tableName = "notes_2026")
data class Note2026Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val link: String?,
    val filename: String?,
    val createdAt: String
)

@Entity(tableName = "dpps")
data class DppItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val driveLink: String,
    val filename: String? = null,
    val createdAt: String
)

@Entity(tableName = "mcq_quizzes")
data class McqQuizItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val details: String?,
    val filename: String,
    val fileType: String,
    val createdAt: String
)

@Entity(tableName = "owner_info")
data class OwnerInfo(
    @PrimaryKey
    val id: Long = 1,
    val name: String,
    val description: String,
    val contact: String,
    val photoFilename: String,
    val telegramLink: String,
    val instagramLink: String,
    val mcqLink: String
)

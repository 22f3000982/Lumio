package com.example.class10resources.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconType: String = "physics", // "physics", "chemistry", "biology", "math", "general"
    val colorHex: String = "#2563EB",
    val chapters: String = "", // Comma-separated list of chapters
    val displayOrder: Int = 0
)

@Entity(tableName = "resources")
data class ResourceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val link: String?,
    val filename: String?,
    val subject: String = "Physics",
    val createdAt: String
)

@Entity(tableName = "notes_2026")
data class Note2026Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val link: String?,
    val filename: String?,
    val subject: String = "Physics",
    val createdAt: String
)

@Entity(tableName = "dpps")
data class DppItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val driveLink: String,
    val filename: String? = null,
    val subject: String = "Physics",
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
    val subject: String = "Physics",
    val createdAt: String
)

@Entity(tableName = "pyqs")
data class PyqItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val year: String = "2024",
    val link: String? = null,
    val filename: String? = null,
    val subject: String = "Physics",
    val chapter: String? = null,
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
    val instagramLink: String = "https://www.instagram.com/ashraj7777/",
    val mcqLink: String = ""
)


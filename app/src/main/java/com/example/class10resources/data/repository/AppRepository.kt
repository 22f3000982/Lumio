package com.example.class10resources.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.class10resources.data.db.AppDatabase
import com.example.class10resources.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("class10_prefs", Context.MODE_PRIVATE)

    private val _isAdmin = MutableStateFlow(prefs.getBoolean("is_admin", false))
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    companion object {
        const val ADMIN_PASSWORD = "4129"
    }

    fun loginAdmin(password: String): Boolean {
        if (password == ADMIN_PASSWORD) {
            prefs.edit().putBoolean("is_admin", true).apply()
            _isAdmin.value = true
            return true
        }
        return false
    }

    fun logoutAdmin() {
        prefs.edit().putBoolean("is_admin", false).apply()
        _isAdmin.value = false
    }

    // Resources
    val allResources: Flow<List<ResourceItem>> = database.resourceDao().getAllResources()

    suspend fun addResource(name: String, link: String?, filename: String?): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return database.resourceDao().insertResource(
            ResourceItem(
                name = name,
                link = link?.ifBlank { null },
                filename = filename?.ifBlank { null },
                createdAt = dateStr
            )
        )
    }

    suspend fun updateResource(resource: ResourceItem) {
        database.resourceDao().updateResource(resource)
    }

    suspend fun deleteResource(id: Long) {
        database.resourceDao().deleteById(id)
    }

    // Notes 2026
    val allNotes: Flow<List<Note2026Item>> = database.note2026Dao().getAllNotes()

    suspend fun addNote2026(name: String, link: String?, filename: String?): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return database.note2026Dao().insertNote(
            Note2026Item(
                name = name,
                link = link?.ifBlank { null },
                filename = filename?.ifBlank { null },
                createdAt = dateStr
            )
        )
    }

    suspend fun updateNote2026(note: Note2026Item) {
        database.note2026Dao().updateNote(note)
    }

    suspend fun deleteNote2026(id: Long) {
        database.note2026Dao().deleteById(id)
    }

    val appDatabase: AppDatabase = database

    // DPPs
    val allDpps: Flow<List<DppItem>> = database.dppDao().getAllDpps()

    suspend fun addDpp(title: String, driveLink: String, filename: String? = null): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return database.dppDao().insertDpp(
            DppItem(
                title = title,
                driveLink = driveLink,
                filename = filename?.ifBlank { null },
                createdAt = dateStr
            )
        )
    }

    suspend fun updateDpp(dpp: DppItem) {
        database.dppDao().updateDpp(dpp)
    }

    suspend fun deleteDpp(id: Long) {
        database.dppDao().deleteById(id)
    }

    // MCQ Quizzes
    val allQuizzes: Flow<List<McqQuizItem>> = database.mcqQuizDao().getAllQuizzes()

    suspend fun addQuiz(title: String, details: String?, filename: String, fileType: String): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return database.mcqQuizDao().insertQuiz(
            McqQuizItem(
                title = title,
                details = details?.ifBlank { null },
                filename = filename,
                fileType = fileType,
                createdAt = dateStr
            )
        )
    }

    suspend fun updateQuiz(quiz: McqQuizItem) {
        database.mcqQuizDao().updateQuiz(quiz)
    }

    suspend fun deleteQuiz(id: Long) {
        database.mcqQuizDao().deleteById(id)
    }

    // Owner Info
    val ownerInfo: Flow<OwnerInfo?> = database.ownerInfoDao().getOwnerInfo()

    suspend fun updateOwnerInfo(ownerInfo: OwnerInfo) {
        database.ownerInfoDao().insertOrUpdate(ownerInfo)
    }

    // Reset default seed data
    suspend fun resetToDefault() {
        AppDatabase.populateInitialData(database)
    }
}

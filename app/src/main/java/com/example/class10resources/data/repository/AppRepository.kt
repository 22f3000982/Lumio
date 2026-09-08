package com.example.class10resources.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.class10resources.data.db.AppDatabase
import com.example.class10resources.data.firebase.FirebaseSyncManager
import com.example.class10resources.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(
    private val database: AppDatabase,
    private val context: Context,
    val firebaseSyncManager: FirebaseSyncManager
) {
    private val repoScope = CoroutineScope(Dispatchers.IO)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("class10_prefs", Context.MODE_PRIVATE)

    private val _isAdmin = MutableStateFlow(prefs.getBoolean("is_admin", false))
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // Default download URL for sharing with direct APK download/install capability
    val defaultAppDownloadUrl = "https://ais-pre-5hf3vdks2xhklgkxmsk5y6-892925382596.asia-east1.run.app"
    private val _appDownloadUrl = MutableStateFlow(prefs.getString("app_download_url", defaultAppDownloadUrl) ?: defaultAppDownloadUrl)
    val appDownloadUrl: StateFlow<String> = _appDownloadUrl.asStateFlow()

    fun updateAppDownloadUrl(url: String) {
        val cleanUrl = url.trim()
        prefs.edit().putString("app_download_url", cleanUrl).apply()
        _appDownloadUrl.value = cleanUrl
    }

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

    // Subjects
    val allSubjects: Flow<List<SubjectItem>> = database.subjectDao().getAllSubjects()

    suspend fun addSubject(
        name: String,
        description: String,
        chapters: String = "",
        colorHex: String = "#2563EB",
        iconType: String = "physics"
    ): Long {
        val item = SubjectItem(
            name = name.trim(),
            description = description.trim(),
            chapters = chapters.trim(),
            colorHex = colorHex,
            iconType = iconType
        )
        val id = database.subjectDao().insertSubject(item)
        repoScope.launch { firebaseSyncManager.syncAllToCloud(database) }
        return id
    }

    suspend fun updateSubject(subject: SubjectItem) {
        database.subjectDao().updateSubject(subject)
        repoScope.launch { firebaseSyncManager.syncAllToCloud(database) }
    }

    suspend fun deleteSubject(id: Long) {
        database.subjectDao().deleteById(id)
        repoScope.launch { firebaseSyncManager.deleteCloudItem("subjects", id) }
    }

    // Resources
    val allResources: Flow<List<ResourceItem>> = database.resourceDao().getAllResources()

    suspend fun addResource(name: String, link: String?, filename: String?, subject: String = "Physics"): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return database.resourceDao().insertResource(
            ResourceItem(
                name = name,
                link = link?.ifBlank { null },
                filename = filename?.ifBlank { null },
                subject = subject,
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

    suspend fun addNote2026(name: String, link: String?, filename: String?, subject: String = "Physics"): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val item = Note2026Item(
            name = name,
            link = link?.ifBlank { null },
            filename = filename?.ifBlank { null },
            subject = subject,
            createdAt = dateStr
        )
        val id = database.note2026Dao().insertNote(item)
        repoScope.launch { firebaseSyncManager.pushNote(item.copy(id = id)) }
        return id
    }

    suspend fun updateNote2026(note: Note2026Item) {
        database.note2026Dao().updateNote(note)
        repoScope.launch { firebaseSyncManager.pushNote(note) }
    }

    suspend fun deleteNote2026(id: Long) {
        database.note2026Dao().deleteById(id)
        repoScope.launch { firebaseSyncManager.deleteCloudItem("notes", id) }
    }

    val appDatabase: AppDatabase = database

    // DPPs
    val allDpps: Flow<List<DppItem>> = database.dppDao().getAllDpps()

    suspend fun addDpp(title: String, driveLink: String, filename: String? = null, subject: String = "Physics"): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val item = DppItem(
            title = title,
            driveLink = driveLink,
            filename = filename?.ifBlank { null },
            subject = subject,
            createdAt = dateStr
        )
        val id = database.dppDao().insertDpp(item)
        repoScope.launch { firebaseSyncManager.pushDpp(item.copy(id = id)) }
        return id
    }

    suspend fun updateDpp(dpp: DppItem) {
        database.dppDao().updateDpp(dpp)
        repoScope.launch { firebaseSyncManager.pushDpp(dpp) }
    }

    suspend fun deleteDpp(id: Long) {
        database.dppDao().deleteById(id)
        repoScope.launch { firebaseSyncManager.deleteCloudItem("dpps", id) }
    }

    // MCQ Quizzes
    val allQuizzes: Flow<List<McqQuizItem>> = database.mcqQuizDao().getAllQuizzes()

    suspend fun addQuiz(title: String, details: String?, filename: String, fileType: String, subject: String = "Physics"): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val item = McqQuizItem(
            title = title,
            details = details?.ifBlank { null },
            filename = filename,
            fileType = fileType,
            subject = subject,
            createdAt = dateStr
        )
        val id = database.mcqQuizDao().insertQuiz(item)
        repoScope.launch { firebaseSyncManager.pushQuiz(item.copy(id = id)) }
        return id
    }

    suspend fun updateQuiz(quiz: McqQuizItem) {
        database.mcqQuizDao().updateQuiz(quiz)
        repoScope.launch { firebaseSyncManager.pushQuiz(quiz) }
    }

    suspend fun deleteQuiz(id: Long) {
        database.mcqQuizDao().deleteById(id)
        repoScope.launch { firebaseSyncManager.deleteCloudItem("quizzes", id) }
    }

    // Previous Year Questions (PYQ)
    val allPyqs: Flow<List<PyqItem>> = database.pyqDao().getAllPyqs()

    suspend fun addPyq(
        title: String,
        year: String = "2024",
        link: String? = null,
        filename: String? = null,
        subject: String = "Physics",
        chapter: String? = null
    ): Long {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val item = PyqItem(
            title = title.trim(),
            year = year.trim(),
            link = link?.ifBlank { null },
            filename = filename?.ifBlank { null },
            subject = subject.trim(),
            chapter = chapter?.ifBlank { null },
            createdAt = dateStr
        )
        val id = database.pyqDao().insertPyq(item)
        repoScope.launch { firebaseSyncManager.pushPyq(item.copy(id = id)) }
        return id
    }

    suspend fun updatePyq(pyq: PyqItem) {
        database.pyqDao().updatePyq(pyq)
        repoScope.launch { firebaseSyncManager.pushPyq(pyq) }
    }

    suspend fun deletePyq(id: Long) {
        database.pyqDao().deleteById(id)
        repoScope.launch { firebaseSyncManager.deleteCloudItem("pyqs", id) }
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

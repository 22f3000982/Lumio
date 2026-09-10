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

    // Default download URL for sharing with direct APK download/install capability (GitHub Releases)
    val defaultAppDownloadUrl = "https://github.com/22f3000982/Lumio_/releases/download/v2.1/Lumio_Class10.apk"
    private val _appDownloadUrl = MutableStateFlow(
        prefs.getString("app_download_url", null)?.ifBlank { null }?.let { saved ->
            if (saved == "https://www.mediafire.com" || saved.contains("gofile.io") || saved.contains("temp.sh")) {
                defaultAppDownloadUrl
            } else {
                saved
            }
        } ?: defaultAppDownloadUrl
    )
    val appDownloadUrl: StateFlow<String> = _appDownloadUrl.asStateFlow()

    // App Update State
    private val _appUpdateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val appUpdateInfo: StateFlow<AppUpdateInfo?> = _appUpdateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _versionStats = MutableStateFlow(VersionStats())
    val versionStats: StateFlow<VersionStats> = _versionStats.asStateFlow()

    init {
        // Ensure app download URL defaults to latest GitHub Release APK
        val existingUrl = prefs.getString("app_download_url", null)
        if (existingUrl.isNullOrBlank() || existingUrl == "https://www.mediafire.com" || existingUrl.contains("gofile.io") || existingUrl.contains("temp.sh")) {
            prefs.edit().putString("app_download_url", defaultAppDownloadUrl).apply()
            _appDownloadUrl.value = defaultAppDownloadUrl
        }

        // Ensure default subjects (Physics, Chemistry, Biology) and learning resources exist
        repoScope.launch {
            try {
                if (database.subjectDao().getCount() == 0) {
                    AppDatabase.populateInitialData(database)
                }
                val currentOwner = database.ownerInfoDao().getOwnerInfoDirect()
                if (currentOwner == null || currentOwner.description.contains("Class 10 Resource Manager")) {
                    database.ownerInfoDao().insertOrUpdate(
                        OwnerInfo(
                            id = 1,
                            name = "Ashish Maurya",
                            description = "Physics Teacher & Educator | Pursuing BS in Data Science at IIT Madras | Full Stack & Web Developer | Dedicated to making Class 10 concepts intuitive, rigorous, and accessible.",
                            contact = "ashraj77777@gmail.com",
                            photoFilename = "mee.jpeg",
                            instagramLink = "https://www.instagram.com/ashraj7777/",
                            mcqLink = "https://www.perplexity.ai/apps/1d5d3a09-a3b4-4c9d-ae02-b5951bb98a80"
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Register this device telemetry in Firebase so Admin knows how many students have updated
        repoScope.launch {
            try {
                val vCode = getCurrentVersionCode()
                val vName = getCurrentVersionName()
                firebaseSyncManager.registerDeviceTelemetry(vCode, vName)
                // Ensure update trigger is deactivated in cloud so no student is forced to update
                firebaseSyncManager.setUpdateTriggerActive(false)
                refreshVersionStats()
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Start real-time Firestore listener for remote update broadcasts
        firebaseSyncManager.startUpdateListener { remoteInfo ->
            if (remoteInfo != null && remoteInfo.isTriggerActive) {
                val currentVersionCode = getCurrentVersionCode()
                // ONLY prompt if current version is older than latest remote version
                val isStudentOnOlderVersion = remoteInfo.latestVersionCode > currentVersionCode
                val dismissedVersion = prefs.getInt("dismissed_version_code", -1)
                val isDismissed = prefs.getBoolean("update_prompt_dismissed", false)

                // Never force anyone, and respect dismissal
                if (isStudentOnOlderVersion && !isDismissed && dismissedVersion != remoteInfo.latestVersionCode) {
                    _appUpdateInfo.value = remoteInfo.copy(isForceUpdate = false)
                } else {
                    _appUpdateInfo.value = null
                }
            } else {
                _appUpdateInfo.value = null
            }
            repoScope.launch {
                refreshVersionStats()
            }
        }
    }

    suspend fun refreshVersionStats() {
        val targetCode = _appUpdateInfo.value?.latestVersionCode ?: (getCurrentVersionCode() + 1)
        val stats = firebaseSyncManager.fetchVersionStats(targetCode)
        _versionStats.value = stats
    }

    suspend fun checkForAppUpdates(): AppUpdateInfo? {
        _isCheckingUpdate.value = true
        return try {
            val remoteInfo = firebaseSyncManager.fetchAppUpdateInfo()
            if (remoteInfo != null) {
                // If remote version code is strictly greater than current app version code (2)
                val currentVersionCode = getCurrentVersionCode()
                if (remoteInfo.isTriggerActive && remoteInfo.latestVersionCode > currentVersionCode) {
                    _appUpdateInfo.value = remoteInfo
                    remoteInfo
                } else {
                    _appUpdateInfo.value = null
                    null
                }
            } else {
                _appUpdateInfo.value = null
                null
            }
        } catch (e: Exception) {
            _appUpdateInfo.value = null
            null
        } finally {
            _isCheckingUpdate.value = false
        }
    }

    suspend fun checkForAppUpdatesStatus(): String {
        _isCheckingUpdate.value = true
        return try {
            val remoteInfo = firebaseSyncManager.fetchAppUpdateInfo()
            if (remoteInfo != null) {
                val currentVersionCode = getCurrentVersionCode()
                val currentVersionName = getCurrentVersionName()
                if (remoteInfo.isTriggerActive && remoteInfo.latestVersionCode > currentVersionCode) {
                    _appUpdateInfo.value = remoteInfo
                    "New update v${remoteInfo.latestVersionName} found! Displaying update prompt."
                } else if (!remoteInfo.isTriggerActive) {
                    _appUpdateInfo.value = null
                    "Update broadcast is currently set to INACTIVE in Cloud."
                } else {
                    _appUpdateInfo.value = null
                    "App is already on latest version (v$currentVersionName). Update popup will trigger for students on older versions."
                }
            } else {
                _appUpdateInfo.value = null
                "No update published in Cloud yet. Tap '⚡ Trigger Popup' to publish."
            }
        } catch (e: Exception) {
            _appUpdateInfo.value = null
            "Could not check updates: ${e.localizedMessage}"
        } finally {
            _isCheckingUpdate.value = false
        }
    }

    fun previewUpdatePrompt() {
        val currentCode = getCurrentVersionCode()
        val currentName = getCurrentVersionName()
        val preview = AppUpdateInfo(
            latestVersionCode = currentCode + 1,
            latestVersionName = "2.2",
            updateTitle = "New Lumio Update Available! 🚀",
            releaseNotes = "• New CBSE Class 10 Physics notes\n• Fresh Daily Practice Problems (DPP)\n• Faster PDF loading and interactive MCQs",
            apkDownloadUrl = _appDownloadUrl.value,
            isForceUpdate = false,
            releasedAt = "Preview Test",
            isTriggerActive = true
        )
        _appUpdateInfo.value = preview
    }

    fun dismissUpdatePrompt() {
        val current = _appUpdateInfo.value
        val editor = prefs.edit()
        editor.putLong("last_dismissed_update_time", System.currentTimeMillis())
        editor.putBoolean("update_prompt_dismissed", true)
        if (current != null) {
            editor.putInt("dismissed_version_code", current.latestVersionCode)
        }
        editor.apply()
        _appUpdateInfo.value = null
    }

    suspend fun triggerInstantUpdate(
        versionCode: Int,
        versionName: String,
        title: String,
        notes: String,
        downloadUrl: String,
        forceUpdate: Boolean = false
    ): Result<Unit> {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val update = AppUpdateInfo(
            latestVersionCode = versionCode,
            latestVersionName = versionName,
            updateTitle = title,
            releaseNotes = notes,
            apkDownloadUrl = downloadUrl.ifBlank { _appDownloadUrl.value },
            isForceUpdate = false, // Never force update, students can always dismiss
            releasedAt = dateStr,
            isTriggerActive = true,
            triggerTimestamp = System.currentTimeMillis()
        )
        if (downloadUrl.isNotBlank()) {
            updateAppDownloadUrl(downloadUrl)
        }
        return firebaseSyncManager.publishAppUpdate(update)
    }

    suspend fun setUpdateTriggerActive(active: Boolean): Result<Unit> {
        return firebaseSyncManager.setUpdateTriggerActive(active)
    }

    suspend fun publishNewVersion(
        versionCode: Int,
        versionName: String,
        title: String,
        notes: String,
        downloadUrl: String,
        forceUpdate: Boolean = false
    ): Result<Unit> {
        return triggerInstantUpdate(versionCode, versionName, title, notes, downloadUrl, forceUpdate)
    }

    fun getCurrentVersionCode(): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            3 // Fallback to current build versionCode
        }
    }

    fun getCurrentVersionName(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "2.1"
        } catch (e: Exception) {
            "2.1"
        }
    }

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

package com.example.class10resources.data.firebase

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.class10resources.data.db.AppDatabase
import com.example.class10resources.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class FirebaseConfig(
    val projectId: String,
    val apiKey: String,
    val storageBucket: String,
    val appId: String
)

class FirebaseSyncManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("firebase_sync_prefs", Context.MODE_PRIVATE)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _statusMessage = MutableStateFlow("Local storage mode (Firebase not connected)")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _config = MutableStateFlow<FirebaseConfig?>(null)
    val config: StateFlow<FirebaseConfig?> = _config.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null

    init {
        initIfConfigured()
    }

    private fun initIfConfigured() {
        val defaultProjectId = "lumio-421c0"
        val defaultApiKey = "AIzaSyBVMxP0EoyRPU_CCpPbIXIiu8HB8VCTQw8"
        val defaultBucket = "lumio-421c0.firebasestorage.app"
        val defaultAppId = "1:467765840358:android:lumio"

        val projectId = prefs.getString("project_id", defaultProjectId) ?: defaultProjectId
        val apiKey = prefs.getString("api_key", defaultApiKey) ?: defaultApiKey
        val storageBucket = prefs.getString("storage_bucket", defaultBucket) ?: defaultBucket
        val appId = prefs.getString("app_id", defaultAppId) ?: defaultAppId

        if (projectId.isNotBlank() && apiKey.isNotBlank()) {
            connect(projectId, apiKey, storageBucket, appId)
        }
    }

    fun connect(
        projectId: String,
        apiKey: String,
        storageBucket: String,
        appId: String = "com.aistudio.class10resources.kznpqr"
    ): Result<Unit> {
        return try {
            val appName = "Class10FirebaseApp"
            val existingApp = FirebaseApp.getApps(context).find { it.name == appName }

            val firebaseApp = if (existingApp != null) {
                existingApp
            } else {
                val cleanBucket = storageBucket.trim().removePrefix("gs://")
                val cleanAppId = if (appId.isNotBlank()) appId.trim() else "1:100000000000:android:class10"
                val builder = FirebaseOptions.Builder()
                    .setProjectId(projectId.trim())
                    .setApiKey(apiKey.trim())
                    .setApplicationId(cleanAppId)

                if (cleanBucket.isNotBlank()) {
                    builder.setStorageBucket(cleanBucket)
                }

                FirebaseApp.initializeApp(context, builder.build(), appName)
            }

            firestore = FirebaseFirestore.getInstance(firebaseApp)
            if (storageBucket.isNotBlank()) {
                val cleanBucket = storageBucket.trim().removePrefix("gs://")
                storage = try {
                    FirebaseStorage.getInstance(firebaseApp, "gs://$cleanBucket")
                } catch (e: Exception) {
                    FirebaseStorage.getInstance(firebaseApp)
                }
            }

            prefs.edit()
                .putString("project_id", projectId.trim())
                .putString("api_key", apiKey.trim())
                .putString("storage_bucket", storageBucket.trim())
                .putString("app_id", appId.trim())
                .apply()

            val currentConfig = FirebaseConfig(
                projectId = projectId.trim(),
                apiKey = apiKey.trim(),
                storageBucket = storageBucket.trim(),
                appId = appId.trim()
            )
            _config.value = currentConfig
            _isConnected.value = true
            _statusMessage.value = "Connected to Firebase ($projectId)"

            // Ensure update listener is immediately started upon connection
            lastUpdateCallback?.let { callback ->
                startUpdateListener(callback)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            _isConnected.value = false
            _statusMessage.value = "Firebase init failed: ${e.localizedMessage}"
            Result.failure(e)
        }
    }

    fun disconnect() {
        prefs.edit().clear().apply()
        _config.value = null
        _isConnected.value = false
        updateListener?.remove()
        updateListener = null
        firestore = null
        storage = null
        _statusMessage.value = "Disconnected. Running in local database mode."
    }

    suspend fun uploadPdf(uri: Uri, folder: String, filename: String): Result<String> = withContext(Dispatchers.IO) {
        val st = storage ?: return@withContext Result.failure(IllegalStateException("Firebase Storage is not configured. Please set Storage Bucket."))
        try {
            val storageRef = st.reference.child("$folder/$filename")
            val stream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open file stream"))

            suspendCancellableCoroutine { continuation ->
                val uploadTask = storageRef.putStream(stream)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        continuation.resume(downloadUri.toString())
                    }.addOnFailureListener { error ->
                        continuation.resumeWithException(error)
                    }
                }.addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
            }.let { Result.success(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncFromCloud(database: AppDatabase): Result<String> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(IllegalStateException("Firebase is not connected"))
        _isSyncing.value = true
        _statusMessage.value = "Syncing data from cloud..."

        try {
            var syncedCount = 0

            // 1. Subjects
            val subjectsSnap = suspendCancellableCoroutine { cont ->
                fs.collection("subjects").get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            val cloudSubjects = subjectsSnap.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val desc = doc.getString("description") ?: ""
                val chapters = doc.getString("chapters") ?: ""
                val colorHex = doc.getString("colorHex") ?: "#2563EB"
                val iconType = doc.getString("iconType") ?: "physics"
                val displayOrder = doc.getLong("displayOrder")?.toInt() ?: 0
                val id = doc.getLong("id") ?: (doc.id.hashCode().toLong())
                SubjectItem(id = id, name = name, description = desc, chapters = chapters, colorHex = colorHex, iconType = iconType, displayOrder = displayOrder)
            }
            if (cloudSubjects.isNotEmpty()) {
                database.subjectDao().insertAll(cloudSubjects)
                syncedCount += cloudSubjects.size
            }

            // 2. Notes
            val notesSnap = suspendCancellableCoroutine { cont ->
                fs.collection("notes").get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            val cloudNotes = notesSnap.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val link = doc.getString("link")
                val filename = doc.getString("filename")
                val subject = doc.getString("subject") ?: "Physics"
                val createdAt = doc.getString("createdAt") ?: ""
                val id = doc.getLong("id") ?: (doc.id.hashCode().toLong())
                Note2026Item(id = id, name = name, link = link, filename = filename, subject = subject, createdAt = createdAt)
            }
            if (cloudNotes.isNotEmpty()) {
                database.note2026Dao().insertAll(cloudNotes)
                syncedCount += cloudNotes.size
            }

            // 3. DPPs
            val dppSnap = suspendCancellableCoroutine { cont ->
                fs.collection("dpps").get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            val cloudDpps = dppSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val driveLink = doc.getString("driveLink") ?: ""
                val filename = doc.getString("filename")
                val subject = doc.getString("subject") ?: "Physics"
                val createdAt = doc.getString("createdAt") ?: ""
                val id = doc.getLong("id") ?: (doc.id.hashCode().toLong())
                DppItem(id = id, title = title, driveLink = driveLink, filename = filename, subject = subject, createdAt = createdAt)
            }
            if (cloudDpps.isNotEmpty()) {
                database.dppDao().insertAll(cloudDpps)
                syncedCount += cloudDpps.size
            }

            // 4. Quizzes
            val quizSnap = suspendCancellableCoroutine { cont ->
                fs.collection("quizzes").get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            val cloudQuizzes = quizSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val details = doc.getString("details")
                val filename = doc.getString("filename") ?: ""
                val fileType = doc.getString("fileType") ?: "mcq"
                val subject = doc.getString("subject") ?: "Physics"
                val createdAt = doc.getString("createdAt") ?: ""
                val id = doc.getLong("id") ?: (doc.id.hashCode().toLong())
                McqQuizItem(id = id, title = title, details = details, filename = filename, fileType = fileType, subject = subject, createdAt = createdAt)
            }
            if (cloudQuizzes.isNotEmpty()) {
                database.mcqQuizDao().insertAll(cloudQuizzes)
                syncedCount += cloudQuizzes.size
            }

            // 5. PYQs
            val pyqSnap = suspendCancellableCoroutine { cont ->
                fs.collection("pyqs").get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            val cloudPyqs = pyqSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val year = doc.getString("year") ?: "2024"
                val link = doc.getString("link")
                val filename = doc.getString("filename")
                val subject = doc.getString("subject") ?: "Physics"
                val chapter = doc.getString("chapter")
                val createdAt = doc.getString("createdAt") ?: ""
                val id = doc.getLong("id") ?: (doc.id.hashCode().toLong())
                PyqItem(id = id, title = title, year = year, link = link, filename = filename, subject = subject, chapter = chapter, createdAt = createdAt)
            }
            if (cloudPyqs.isNotEmpty()) {
                database.pyqDao().insertAll(cloudPyqs)
                syncedCount += cloudPyqs.size
            }

            _isSyncing.value = false
            _statusMessage.value = "Synced $syncedCount items from cloud successfully."
            Result.success("Downloaded $syncedCount items from Firebase Cloud.")
        } catch (e: Exception) {
            _isSyncing.value = false
            _statusMessage.value = "Cloud sync error: ${e.localizedMessage}"
            Result.failure(e)
        }
    }

    suspend fun syncAllToCloud(database: AppDatabase): Result<String> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(IllegalStateException("Firebase is not connected"))
        _isSyncing.value = true
        _statusMessage.value = "Pushing local materials to cloud..."

        try {
            var uploadedCount = 0

            // Upload Subjects
            val subjects = database.subjectDao().getAllSubjectsList()
            for (subj in subjects) {
                val data = mapOf(
                    "id" to subj.id,
                    "name" to subj.name,
                    "description" to subj.description,
                    "chapters" to subj.chapters,
                    "colorHex" to subj.colorHex,
                    "iconType" to subj.iconType,
                    "displayOrder" to subj.displayOrder
                )
                suspendCancellableCoroutine<Unit> { cont ->
                    fs.collection("subjects").document(subj.id.toString())
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener { cont.resume(Unit) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                uploadedCount++
            }

            // Upload Notes
            val notes = database.note2026Dao().getAllNotesList()
            for (note in notes) {
                val data = mapOf(
                    "id" to note.id,
                    "name" to note.name,
                    "link" to (note.link ?: ""),
                    "filename" to (note.filename ?: ""),
                    "subject" to note.subject,
                    "createdAt" to note.createdAt
                )
                suspendCancellableCoroutine<Unit> { cont ->
                    fs.collection("notes").document(note.id.toString())
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener { cont.resume(Unit) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                uploadedCount++
            }

            // Upload DPPs
            val dpps = database.dppDao().getAllDppsList()
            for (dpp in dpps) {
                val data = mapOf(
                    "id" to dpp.id,
                    "title" to dpp.title,
                    "driveLink" to dpp.driveLink,
                    "filename" to (dpp.filename ?: ""),
                    "subject" to dpp.subject,
                    "createdAt" to dpp.createdAt
                )
                suspendCancellableCoroutine<Unit> { cont ->
                    fs.collection("dpps").document(dpp.id.toString())
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener { cont.resume(Unit) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                uploadedCount++
            }

            // Upload Quizzes
            val quizzes = database.mcqQuizDao().getAllQuizzesList()
            for (quiz in quizzes) {
                val data = mapOf(
                    "id" to quiz.id,
                    "title" to quiz.title,
                    "details" to (quiz.details ?: ""),
                    "filename" to quiz.filename,
                    "fileType" to quiz.fileType,
                    "subject" to quiz.subject,
                    "createdAt" to quiz.createdAt
                )
                suspendCancellableCoroutine<Unit> { cont ->
                    fs.collection("quizzes").document(quiz.id.toString())
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener { cont.resume(Unit) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                uploadedCount++
            }

            // Upload PYQs
            val pyqs = database.pyqDao().getAllPyqsList()
            for (pyq in pyqs) {
                val data = mapOf(
                    "id" to pyq.id,
                    "title" to pyq.title,
                    "year" to pyq.year,
                    "link" to (pyq.link ?: ""),
                    "filename" to (pyq.filename ?: ""),
                    "subject" to pyq.subject,
                    "chapter" to (pyq.chapter ?: ""),
                    "createdAt" to pyq.createdAt
                )
                suspendCancellableCoroutine<Unit> { cont ->
                    fs.collection("pyqs").document(pyq.id.toString())
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener { cont.resume(Unit) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                uploadedCount++
            }

            _isSyncing.value = false
            _statusMessage.value = "Successfully pushed $uploadedCount items to Firebase Cloud."
            Result.success("Pushed $uploadedCount items to Firebase Cloud.")
        } catch (e: Exception) {
            _isSyncing.value = false
            _statusMessage.value = "Cloud push error: ${e.localizedMessage}"
            Result.failure(e)
        }
    }

    suspend fun pushNote(note: Note2026Item) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val data = mapOf(
                "id" to note.id,
                "name" to note.name,
                "link" to (note.link ?: ""),
                "filename" to (note.filename ?: ""),
                "subject" to note.subject,
                "createdAt" to note.createdAt
            )
            fs.collection("notes").document(note.id.toString()).set(data, SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteCloudItem(collection: String, id: Long) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            fs.collection(collection).document(id.toString()).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun pushDpp(dpp: DppItem) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val data = mapOf(
                "id" to dpp.id,
                "title" to dpp.title,
                "driveLink" to dpp.driveLink,
                "filename" to (dpp.filename ?: ""),
                "subject" to dpp.subject,
                "createdAt" to dpp.createdAt
            )
            fs.collection("dpps").document(dpp.id.toString()).set(data, SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun pushQuiz(quiz: McqQuizItem) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val data = mapOf(
                "id" to quiz.id,
                "title" to quiz.title,
                "details" to (quiz.details ?: ""),
                "filename" to quiz.filename,
                "fileType" to quiz.fileType,
                "subject" to quiz.subject,
                "createdAt" to quiz.createdAt
            )
            fs.collection("quizzes").document(quiz.id.toString()).set(data, SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun pushPyq(pyq: PyqItem) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val data = mapOf(
                "id" to pyq.id,
                "title" to pyq.title,
                "year" to pyq.year,
                "link" to (pyq.link ?: ""),
                "filename" to (pyq.filename ?: ""),
                "subject" to pyq.subject,
                "chapter" to (pyq.chapter ?: ""),
                "createdAt" to pyq.createdAt
            )
            fs.collection("pyqs").document(pyq.id.toString()).set(data, SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchAppUpdateInfo(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext null
        try {
            val doc = suspendCancellableCoroutine { cont ->
                fs.collection("app_config").document("version_info").get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            if (doc.exists()) {
                val latestVersionCode = doc.getLong("latestVersionCode")?.toInt() ?: 2
                val latestVersionName = doc.getString("latestVersionName") ?: "2.0"
                val updateTitle = doc.getString("updateTitle") ?: "New Lumio Update Available"
                val releaseNotes = doc.getString("releaseNotes") ?: "New study materials, notes and improved experience."
                val apkDownloadUrl = doc.getString("apkDownloadUrl") ?: ""
                val isForceUpdate = doc.getBoolean("isForceUpdate") ?: false
                val releasedAt = doc.getString("releasedAt") ?: ""
                val isTriggerActive = doc.getBoolean("isTriggerActive") ?: true
                val triggerTimestamp = doc.getLong("triggerTimestamp") ?: 0L
                AppUpdateInfo(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    updateTitle = updateTitle,
                    releaseNotes = releaseNotes,
                    apkDownloadUrl = apkDownloadUrl,
                    isForceUpdate = isForceUpdate,
                    releasedAt = releasedAt,
                    isTriggerActive = isTriggerActive,
                    triggerTimestamp = triggerTimestamp
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private var updateListener: ListenerRegistration? = null
    private var lastUpdateCallback: ((AppUpdateInfo?) -> Unit)? = null

    fun startUpdateListener(onUpdate: (AppUpdateInfo?) -> Unit) {
        lastUpdateCallback = onUpdate
        val fs = firestore ?: return
        try {
            updateListener?.remove()
            updateListener = fs.collection("app_config").document("version_info")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val latestVersionCode = snapshot.getLong("latestVersionCode")?.toInt() ?: 2
                        val latestVersionName = snapshot.getString("latestVersionName") ?: "2.0"
                        val updateTitle = snapshot.getString("updateTitle") ?: "New Lumio Update Available"
                        val releaseNotes = snapshot.getString("releaseNotes") ?: "New study materials, notes and improved experience."
                        val apkDownloadUrl = snapshot.getString("apkDownloadUrl") ?: ""
                        val isForceUpdate = snapshot.getBoolean("isForceUpdate") ?: false
                        val releasedAt = snapshot.getString("releasedAt") ?: ""
                        val isTriggerActive = snapshot.getBoolean("isTriggerActive") ?: true
                        val triggerTimestamp = snapshot.getLong("triggerTimestamp") ?: 0L

                        val info = AppUpdateInfo(
                            latestVersionCode = latestVersionCode,
                            latestVersionName = latestVersionName,
                            updateTitle = updateTitle,
                            releaseNotes = releaseNotes,
                            apkDownloadUrl = apkDownloadUrl,
                            isForceUpdate = isForceUpdate,
                            releasedAt = releasedAt,
                            isTriggerActive = isTriggerActive,
                            triggerTimestamp = triggerTimestamp
                        )
                        onUpdate(info)
                    } else {
                        onUpdate(null)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setUpdateTriggerActive(active: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(IllegalStateException("Firebase is not connected"))
        try {
            val data = mapOf(
                "isTriggerActive" to active,
                "triggerTimestamp" to System.currentTimeMillis()
            )
            suspendCancellableCoroutine<Unit> { cont ->
                fs.collection("app_config").document("version_info")
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishAppUpdate(info: AppUpdateInfo): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext Result.failure(IllegalStateException("Firebase is not connected"))
        try {
            val data = mapOf(
                "latestVersionCode" to info.latestVersionCode,
                "latestVersionName" to info.latestVersionName,
                "updateTitle" to info.updateTitle,
                "releaseNotes" to info.releaseNotes,
                "apkDownloadUrl" to info.apkDownloadUrl,
                "isForceUpdate" to info.isForceUpdate,
                "releasedAt" to info.releasedAt,
                "isTriggerActive" to info.isTriggerActive,
                "triggerTimestamp" to if (info.triggerTimestamp > 0L) info.triggerTimestamp else System.currentTimeMillis()
            )
            suspendCancellableCoroutine<Unit> { cont ->
                fs.collection("app_config").document("version_info")
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Records device heartbeat with its installed version so admin knows
     * exactly how many students are on latest vs older versions.
     */
    suspend fun registerDeviceTelemetry(versionCode: Int, versionName: String) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            var deviceId = prefs.getString("device_telemetry_id", null)
            if (deviceId.isNullOrBlank()) {
                deviceId = java.util.UUID.randomUUID().toString()
                prefs.edit().putString("device_telemetry_id", deviceId).apply()
            }
            val payload = mapOf(
                "deviceId" to deviceId,
                "versionCode" to versionCode,
                "versionName" to versionName,
                "lastActive" to System.currentTimeMillis()
            )
            fs.collection("active_devices").document(deviceId)
                .set(payload, SetOptions.merge())
        } catch (e: Exception) {
            // Non-blocking telemetry
        }
    }

    /**
     * Fetches real-time count of updated vs pending devices for admin dashboard
     */
    suspend fun fetchVersionStats(latestVersionCode: Int): VersionStats = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext VersionStats()
        try {
            val snapshot = suspendCancellableCoroutine<com.google.firebase.firestore.QuerySnapshot> { cont ->
                fs.collection("active_devices").get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            var updated = 0
            var pending = 0
            val total = snapshot.size()
            for (doc in snapshot.documents) {
                val vCode = doc.getLong("versionCode")?.toInt() ?: 1
                if (vCode >= latestVersionCode) {
                    updated++
                } else {
                    pending++
                }
            }
            val percent = if (total > 0) ((updated.toDouble() / total) * 100).toInt() else 100
            VersionStats(
                totalActiveDevices = total,
                updatedDevicesCount = updated,
                pendingDevicesCount = pending,
                updatePercentage = percent
            )
        } catch (e: Exception) {
            VersionStats()
        }
    }
}


package com.example.class10resources.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.class10resources.Class10Application
import com.example.class10resources.data.firebase.FirebaseConfig
import com.example.class10resources.data.model.*
import com.example.class10resources.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository =
        (application as Class10Application).repository

    val isFirebaseConnected: StateFlow<Boolean> = repository.firebaseSyncManager.isConnected
    val isFirebaseSyncing: StateFlow<Boolean> = repository.firebaseSyncManager.isSyncing
    val firebaseStatusMessage: StateFlow<String> = repository.firebaseSyncManager.statusMessage
    val firebaseConfig: StateFlow<FirebaseConfig?> = repository.firebaseSyncManager.config

    init {
        // Auto-fetch latest cloud materials on launch so all students get the latest updates
        viewModelScope.launch {
            if (repository.firebaseSyncManager.isConnected.value) {
                repository.firebaseSyncManager.syncFromCloud(repository.appDatabase)
            }
        }
    }

    val isAdmin: StateFlow<Boolean> = repository.isAdmin
    val appDownloadUrl: StateFlow<String> = repository.appDownloadUrl

    fun updateAppDownloadUrl(url: String) {
        repository.updateAppDownloadUrl(url)
    }

    val subjects: StateFlow<List<SubjectItem>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val resources: StateFlow<List<ResourceItem>> = repository.allResources
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notes: StateFlow<List<Note2026Item>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dpps: StateFlow<List<DppItem>> = repository.allDpps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quizzes: StateFlow<List<McqQuizItem>> = repository.allQuizzes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pyqs: StateFlow<List<PyqItem>> = repository.allPyqs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val ownerInfo: StateFlow<OwnerInfo?> = repository.ownerInfo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loginAdmin(password: String): Boolean {
        return repository.loginAdmin(password)
    }

    fun logoutAdmin() {
        repository.logoutAdmin()
    }

    fun addSubject(name: String, description: String, chapters: String = "", colorHex: String = "#2563EB", iconType: String = "physics") {
        viewModelScope.launch {
            repository.addSubject(name, description, chapters, colorHex, iconType)
        }
    }

    fun updateSubject(subject: SubjectItem) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    fun addResource(name: String, link: String?, filename: String? = null, subject: String = "Physics") {
        viewModelScope.launch {
            repository.addResource(name, link, filename, subject)
        }
    }

    fun updateResource(resource: ResourceItem) {
        viewModelScope.launch {
            repository.updateResource(resource)
        }
    }

    fun deleteResource(id: Long) {
        viewModelScope.launch {
            repository.deleteResource(id)
        }
    }

    fun addNote(name: String, link: String?, filename: String? = null, subject: String = "Physics") {
        viewModelScope.launch {
            repository.addNote2026(name, link, filename, subject)
        }
    }

    fun updateNote(note: Note2026Item) {
        viewModelScope.launch {
            repository.updateNote2026(note)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote2026(id)
        }
    }

    fun addDpp(title: String, link: String, filename: String? = null, subject: String = "Physics") {
        viewModelScope.launch {
            repository.addDpp(title, link, filename, subject)
        }
    }

    fun updateDpp(dpp: DppItem) {
        viewModelScope.launch {
            repository.updateDpp(dpp)
        }
    }

    fun deleteDpp(id: Long) {
        viewModelScope.launch {
            repository.deleteDpp(id)
        }
    }

    fun addQuiz(title: String, details: String?, filename: String, fileType: String = "html", subject: String = "Physics") {
        viewModelScope.launch {
            repository.addQuiz(title, details, filename, fileType, subject)
        }
    }

    fun updateQuiz(quiz: McqQuizItem) {
        viewModelScope.launch {
            repository.updateQuiz(quiz)
        }
    }

    fun deleteQuiz(id: Long) {
        viewModelScope.launch {
            repository.deleteQuiz(id)
        }
    }

    fun addPyq(
        title: String,
        year: String = "2024",
        link: String? = null,
        filename: String? = null,
        subject: String = "Physics",
        chapter: String? = null
    ) {
        viewModelScope.launch {
            repository.addPyq(title, year, link, filename, subject, chapter)
        }
    }

    fun updatePyq(pyq: PyqItem) {
        viewModelScope.launch {
            repository.updatePyq(pyq)
        }
    }

    fun deletePyq(id: Long) {
        viewModelScope.launch {
            repository.deletePyq(id)
        }
    }

    fun updateOwner(ownerInfo: OwnerInfo) {
        viewModelScope.launch {
            repository.updateOwnerInfo(ownerInfo)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetToDefault()
        }
    }

    fun exportBackup(uri: android.net.Uri, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = com.example.class10resources.util.BackupManager.exportBackup(
                getApplication(),
                uri,
                repository.appDatabase
            )
            onResult(result)
        }
    }

    fun restoreBackup(uri: android.net.Uri, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = com.example.class10resources.util.BackupManager.restoreBackup(
                getApplication(),
                uri,
                repository.appDatabase
            )
            onResult(result)
        }
    }

    fun connectFirebase(projectId: String, apiKey: String, storageBucket: String, appId: String = ""): Result<Unit> {
        return repository.firebaseSyncManager.connect(projectId, apiKey, storageBucket, appId)
    }

    fun disconnectFirebase() {
        repository.firebaseSyncManager.disconnect()
    }

    fun syncFromCloud(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.firebaseSyncManager.syncFromCloud(repository.appDatabase)
            onResult(result)
        }
    }

    fun syncToCloud(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.firebaseSyncManager.syncAllToCloud(repository.appDatabase)
            onResult(result)
        }
    }
}

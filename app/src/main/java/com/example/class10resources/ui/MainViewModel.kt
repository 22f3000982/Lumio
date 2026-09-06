package com.example.class10resources.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.class10resources.Class10Application
import com.example.class10resources.data.model.*
import com.example.class10resources.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository =
        (application as Class10Application).repository

    val isAdmin: StateFlow<Boolean> = repository.isAdmin

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

    fun addResource(name: String, link: String?, filename: String? = null) {
        viewModelScope.launch {
            repository.addResource(name, link, filename)
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

    fun addNote(name: String, link: String?, filename: String? = null) {
        viewModelScope.launch {
            repository.addNote2026(name, link, filename)
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

    fun addDpp(title: String, link: String, filename: String? = null) {
        viewModelScope.launch {
            repository.addDpp(title, link, filename)
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

    fun deleteQuiz(id: Long) {
        viewModelScope.launch {
            repository.deleteQuiz(id)
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
}

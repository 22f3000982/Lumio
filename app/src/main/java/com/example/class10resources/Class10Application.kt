package com.example.class10resources

import android.app.Application
import com.example.class10resources.data.db.AppDatabase
import com.example.class10resources.data.firebase.FirebaseSyncManager
import com.example.class10resources.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class Class10Application : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val firebaseSyncManager by lazy { FirebaseSyncManager(this) }
    val repository by lazy { AppRepository(database, this, firebaseSyncManager) }
}

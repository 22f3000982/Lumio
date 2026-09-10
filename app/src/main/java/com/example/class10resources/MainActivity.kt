package com.example.class10resources

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.class10resources.data.model.McqQuizItem
import com.example.class10resources.data.model.SubjectItem
import com.example.class10resources.ui.MainViewModel
import com.example.class10resources.ui.components.AdminLoginDialog
import com.example.class10resources.ui.components.AppUpdateDialog
import com.example.class10resources.ui.components.LumioIcon
import com.example.class10resources.ui.components.ShareAppDialog
import com.example.class10resources.ui.screens.*
import com.example.class10resources.ui.theme.AccentSuccess
import com.example.class10resources.ui.theme.Class10ResourcesTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDark) }

            val subjects by viewModel.subjects.collectAsStateWithLifecycle()
            val notes by viewModel.notes.collectAsStateWithLifecycle()
            val dpps by viewModel.dpps.collectAsStateWithLifecycle()
            val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
            val pyqs by viewModel.pyqs.collectAsStateWithLifecycle()
            val ownerInfo by viewModel.ownerInfo.collectAsStateWithLifecycle()
            val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
            val isFirebaseConnected by viewModel.isFirebaseConnected.collectAsStateWithLifecycle()
            val isFirebaseSyncing by viewModel.isFirebaseSyncing.collectAsStateWithLifecycle()
            val firebaseStatusMessage by viewModel.firebaseStatusMessage.collectAsStateWithLifecycle()
            val firebaseConfig by viewModel.firebaseConfig.collectAsStateWithLifecycle()
            val appDownloadUrl by viewModel.appDownloadUrl.collectAsStateWithLifecycle()
            val appUpdateInfo by viewModel.appUpdateInfo.collectAsStateWithLifecycle()
            val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
            val versionStats by viewModel.versionStats.collectAsStateWithLifecycle()

            var selectedTab by remember { mutableIntStateOf(0) }
            var activeSubject by remember { mutableStateOf<Pair<SubjectItem, String?>?>(null) }
            var activeQuiz by remember { mutableStateOf<McqQuizItem?>(null) }
            var showOwnerScreen by remember { mutableStateOf(false) }
            var showLoginDialog by remember { mutableStateOf(false) }
            var showShareDialog by remember { mutableStateOf(false) }
            var loginErrorMessage by remember { mutableStateOf<String?>(null) }

            val context = LocalContext.current

            // Prevent direct app closure on system back press; navigate back within app hierarchy
            BackHandler(enabled = activeQuiz != null || activeSubject != null || showOwnerScreen || selectedTab != 0) {
                when {
                    activeQuiz != null -> activeQuiz = null
                    activeSubject != null -> activeSubject = null
                    showOwnerScreen -> showOwnerScreen = false
                    selectedTab != 0 -> selectedTab = 0
                }
            }

            Class10ResourcesTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (activeQuiz != null) {
                        QuizPlayerScreen(
                            quiz = activeQuiz!!,
                            onBack = { activeQuiz = null }
                        )
                    } else if (activeSubject != null) {
                        SubjectDetailScreen(
                            subject = activeSubject!!.first,
                            allSubjects = subjects,
                            notes = notes,
                            dpps = dpps,
                            quizzes = quizzes,
                            pyqs = pyqs,
                            initialChapterFilter = activeSubject!!.second,
                            isAdmin = isAdmin,
                            onBack = { activeSubject = null },
                            onStartQuiz = { quiz -> activeQuiz = quiz },
                            onAddNote = { name, link, filename, subj ->
                                viewModel.addNote(name, link, filename, subj)
                            },
                            onUpdateNote = { note -> viewModel.updateNote(note) },
                            onDeleteNote = { id -> viewModel.deleteNote(id) },
                            onAddDpp = { title, link, filename, subj ->
                                viewModel.addDpp(title, link, filename, subj)
                            },
                            onUpdateDpp = { dpp -> viewModel.updateDpp(dpp) },
                            onDeleteDpp = { id -> viewModel.deleteDpp(id) },
                            onAddQuiz = { title, details, filename, fileType, subj ->
                                viewModel.addQuiz(title, details, filename, fileType, subj)
                            },
                            onUpdateQuiz = { quiz -> viewModel.updateQuiz(quiz) },
                            onDeleteQuiz = { id -> viewModel.deleteQuiz(id) },
                            onAddPyq = { title, year, link, filename, subj, chapter ->
                                viewModel.addPyq(title, year, link, filename, subj, chapter)
                            },
                            onUpdatePyq = { pyq -> viewModel.updatePyq(pyq) },
                            onDeletePyq = { id -> viewModel.deletePyq(id) }
                        )
                    } else if (showOwnerScreen) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Column {
                                            Text("Educator Profile", fontWeight = FontWeight.Bold)
                                            Text(
                                                "Ashish Maurya • System & Backup",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { showOwnerScreen = false }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                AboutOwnerScreen(
                                    ownerInfo = ownerInfo,
                                    isAdmin = isAdmin,
                                    onSaveOwner = { updated ->
                                        viewModel.updateOwner(updated)
                                    },
                                    onLoginClick = {
                                        loginErrorMessage = null
                                        showLoginDialog = true
                                    },
                                    onLogoutClick = {
                                        viewModel.logoutAdmin()
                                        Toast.makeText(context, "Logged out of Admin mode", Toast.LENGTH_SHORT).show()
                                    },
                                    onExportBackup = { uri ->
                                        viewModel.exportBackup(uri) { res ->
                                            val msg = res.getOrElse { it.message ?: "Export failed" }
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onRestoreBackup = { uri ->
                                        viewModel.restoreBackup(uri) { res ->
                                            val msg = res.getOrElse { it.message ?: "Restore failed" }
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onResetData = {
                                        viewModel.resetData()
                                        Toast.makeText(context, "Database restored to default content.", Toast.LENGTH_SHORT).show()
                                    },
                                    isFirebaseConnected = isFirebaseConnected,
                                    isFirebaseSyncing = isFirebaseSyncing,
                                    firebaseStatusMessage = firebaseStatusMessage,
                                    firebaseConfig = firebaseConfig,
                                    onConnectFirebase = { projId, key, bucket, app ->
                                        val res = viewModel.connectFirebase(projId, key, bucket, app)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Connected to Firebase project: $projId", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Connection failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onDisconnectFirebase = {
                                        viewModel.disconnectFirebase()
                                        Toast.makeText(context, "Disconnected from Firebase", Toast.LENGTH_SHORT).show()
                                    },
                                    onSyncFromCloud = {
                                        Toast.makeText(context, "Syncing from cloud...", Toast.LENGTH_SHORT).show()
                                        viewModel.syncFromCloud { res ->
                                            val msg = res.getOrElse { it.message ?: "Sync completed" }
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onSyncToCloud = {
                                        Toast.makeText(context, "Pushing all materials to cloud...", Toast.LENGTH_SHORT).show()
                                        viewModel.syncToCloud { res ->
                                            val msg = res.getOrElse { it.message ?: "Sync completed" }
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onShareApp = {
                                        showShareDialog = true
                                    },
                                    currentVersionCode = viewModel.currentVersionCode,
                                    currentVersionName = viewModel.currentVersionName,
                                    currentDownloadUrl = appDownloadUrl,
                                    appUpdateInfo = appUpdateInfo,
                                    versionStats = versionStats,
                                    onRefreshStats = { viewModel.refreshVersionStats() },
                                    onSetTriggerActive = { active ->
                                        viewModel.setUpdateTriggerActive(active) { res ->
                                            if (res.isSuccess) {
                                                val msg = if (active) "Update popup active for students!" else "Update popup turned off."
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onPublishUpdate = { vCode, vName, title, notes, dlUrl, force ->
                                        viewModel.triggerInstantUpdate(vCode, vName, title, notes, dlUrl, force) { res ->
                                            if (res.isSuccess) {
                                                Toast.makeText(context, "⚡ Update popup triggered instantly to all students!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Failed to broadcast update: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onCheckUpdateNow = {
                                        Toast.makeText(context, "Checking cloud for latest app version...", Toast.LENGTH_SHORT).show()
                                        viewModel.checkForAppUpdates { status ->
                                            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onPreviewPopup = {
                                        viewModel.previewUpdatePrompt()
                                    }
                                )
                            }
                        }
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            LumioIcon(
                                                size = 32.dp,
                                                modifier = Modifier.testTag("app_logo_lumio")
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Lumio",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            letterSpacing = 0.5.sp
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "Class 10",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = when (selectedTab) {
                                                        0 -> "Subjects Hub"
                                                        1 -> "2026 Batch Notes"
                                                        2 -> "Daily Practice (DPP)"
                                                        3 -> "Practice MCQs"
                                                        4 -> "Previous Year Questions"
                                                        else -> "Lumio Learning"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    actions = {
                                        // Cloud Sync status - only visible to admin
                                        if (isAdmin) {
                                            IconButton(
                                                onClick = { showOwnerScreen = true },
                                                modifier = Modifier.testTag("cloud_status_button")
                                            ) {
                                                Icon(
                                                    imageVector = if (isFirebaseConnected) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                                    contentDescription = if (isFirebaseConnected) "Cloud Synced" else "Cloud Setup",
                                                    tint = if (isFirebaseConnected) AccentSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Educator Profile action
                                        IconButton(
                                            onClick = { showOwnerScreen = true },
                                            modifier = Modifier.testTag("educator_profile_action")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Person,
                                                contentDescription = "Educator Profile"
                                            )
                                        }

                                        // Direct Share App Action
                                        IconButton(
                                            onClick = { showShareDialog = true },
                                            modifier = Modifier.testTag("topbar_share_app_action")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share App with Students",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Theme Toggle Button
                                        IconButton(
                                            onClick = { isDarkMode = !isDarkMode },
                                            modifier = Modifier.testTag("theme_toggle_button")
                                        ) {
                                            Icon(
                                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                contentDescription = "Toggle Theme"
                                            )
                                        }

                                        // Admin Status / Login Action
                                        if (isAdmin) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.logoutAdmin()
                                                    Toast.makeText(context, "Logged out of Admin mode", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.testTag("admin_status_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.LockOpen,
                                                    contentDescription = "Admin Unlocked (Tap to lock)",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    loginErrorMessage = null
                                                    showLoginDialog = true
                                                },
                                                modifier = Modifier.testTag("admin_login_action")
                                            ) {
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = "Admin Login"
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        titleContentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = {
                                            selectedTab = 0
                                            activeSubject = null
                                        },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                                contentDescription = "Subjects"
                                            )
                                        },
                                        label = { Text("Subjects") },
                                        modifier = Modifier.testTag("nav_subjects")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = {
                                            selectedTab = 1
                                            activeSubject = null
                                        },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 1) Icons.Filled.Description else Icons.Outlined.Description,
                                                contentDescription = "Notes"
                                            )
                                        },
                                        label = { Text("Notes") },
                                        modifier = Modifier.testTag("nav_notes")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = {
                                            selectedTab = 2
                                            activeSubject = null
                                        },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 2) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                                                contentDescription = "DPP"
                                            )
                                        },
                                        label = { Text("DPP") },
                                        modifier = Modifier.testTag("nav_dpp")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 3,
                                        onClick = {
                                            selectedTab = 3
                                            activeSubject = null
                                        },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 3) Icons.Filled.Quiz else Icons.Outlined.Quiz,
                                                contentDescription = "Quiz"
                                            )
                                        },
                                        label = { Text("Quiz") },
                                        modifier = Modifier.testTag("nav_quiz")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 4,
                                        onClick = {
                                            selectedTab = 4
                                            activeSubject = null
                                        },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 4) Icons.Filled.School else Icons.Outlined.School,
                                                contentDescription = "PYQ"
                                            )
                                        },
                                        label = { Text("PYQ") },
                                        modifier = Modifier.testTag("nav_pyq")
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AnimatedContent(
                                    targetState = selectedTab,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "tab_content"
                                ) { tabIndex ->
                                    when (tabIndex) {
                                        0 -> SubjectsDashboardScreen(
                                            subjects = subjects,
                                            notes = notes,
                                            dpps = dpps,
                                            quizzes = quizzes,
                                            pyqs = pyqs,
                                            ownerInfo = ownerInfo,
                                            isAdmin = isAdmin,
                                            onSelectSubject = { subj, chapterFilter ->
                                                activeSubject = Pair(subj, chapterFilter)
                                            },
                                            onNavigateToDpp = { selectedTab = 2 },
                                            onNavigateToMcq = { selectedTab = 3 },
                                            onNavigateToNotes = { selectedTab = 1 },
                                            onNavigateToPyq = { selectedTab = 4 },
                                            onAddSubject = { name, description, chapters, colorHex, iconType ->
                                                viewModel.addSubject(name, description, chapters, colorHex, iconType)
                                            },
                                            onDeleteSubject = { id ->
                                                viewModel.deleteSubject(id)
                                            },
                                            onShareApp = {
                                                showShareDialog = true
                                            },
                                            appUpdateInfo = appUpdateInfo,
                                            onOpenUpdateDialog = {
                                                viewModel.checkForAppUpdates()
                                            }
                                        )
                                        1 -> Notes2026Screen(
                                            notes = notes,
                                            isAdmin = isAdmin,
                                            onAddNote = { name, link, filename ->
                                                viewModel.addNote(name, link, filename)
                                            },
                                            onEditNote = { note ->
                                                viewModel.updateNote(note)
                                            },
                                            onDeleteNote = { id ->
                                                viewModel.deleteNote(id)
                                            }
                                        )
                                        2 -> DppScreen(
                                            dpps = dpps,
                                            isAdmin = isAdmin,
                                            onAddDpp = { title, link, filename ->
                                                viewModel.addDpp(title, link, filename)
                                            },
                                            onEditDpp = { dpp ->
                                                viewModel.updateDpp(dpp)
                                            },
                                            onDeleteDpp = { id ->
                                                viewModel.deleteDpp(id)
                                            }
                                        )
                                        3 -> PracticeMcqScreen(
                                            quizzes = quizzes,
                                            isAdmin = isAdmin,
                                            onStartQuiz = { quiz ->
                                                activeQuiz = quiz
                                            },
                                            onAddQuiz = { title, details, filename, fileType, subj ->
                                                viewModel.addQuiz(title, details, filename, fileType, subj)
                                            },
                                            onEditQuiz = { quiz ->
                                                viewModel.updateQuiz(quiz)
                                            },
                                            onDeleteQuiz = { id ->
                                                viewModel.deleteQuiz(id)
                                            }
                                        )
                                        4 -> PyqScreen(
                                            pyqs = pyqs,
                                            subjects = subjects,
                                            isAdmin = isAdmin,
                                            onAddPyq = { title, year, link, filename, subj, chapter ->
                                                viewModel.addPyq(title, year, link, filename, subj, chapter)
                                            },
                                            onUpdatePyq = { pyq ->
                                                viewModel.updatePyq(pyq)
                                            },
                                            onDeletePyq = { id ->
                                                viewModel.deletePyq(id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showLoginDialog) {
                        AdminLoginDialog(
                            onDismiss = {
                                showLoginDialog = false
                                loginErrorMessage = null
                            },
                            onLogin = { pass ->
                                val success = viewModel.loginAdmin(pass)
                                if (success) {
                                    showLoginDialog = false
                                    loginErrorMessage = null
                                    Toast.makeText(context, "Welcome Admin! Edit privileges unlocked.", Toast.LENGTH_SHORT).show()
                                } else {
                                    loginErrorMessage = "Incorrect admin password! Please try again."
                                }
                            },
                            errorMessage = loginErrorMessage
                        )
                    }

                    if (showShareDialog) {
                        ShareAppDialog(
                            currentDownloadUrl = appDownloadUrl,
                            isAdmin = isAdmin,
                            onDismiss = { showShareDialog = false },
                            onUpdateDownloadUrl = { newUrl ->
                                viewModel.updateAppDownloadUrl(newUrl)
                            }
                        )
                    }

                    // Automatic In-App Update Prompt for existing users
                    appUpdateInfo?.let { updateInfo ->
                        AppUpdateDialog(
                            updateInfo = updateInfo,
                            currentVersionName = viewModel.currentVersionName,
                            onDismiss = {
                                viewModel.dismissUpdatePrompt()
                            }
                        )
                    }
                }
            }
        }
    }
}

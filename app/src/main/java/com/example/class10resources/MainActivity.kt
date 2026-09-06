package com.example.class10resources

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import com.example.class10resources.ui.MainViewModel
import com.example.class10resources.ui.components.AdminLoginDialog
import com.example.class10resources.ui.screens.*
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

            val resources by viewModel.resources.collectAsStateWithLifecycle()
            val notes by viewModel.notes.collectAsStateWithLifecycle()
            val dpps by viewModel.dpps.collectAsStateWithLifecycle()
            val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
            val ownerInfo by viewModel.ownerInfo.collectAsStateWithLifecycle()
            val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()

            var selectedTab by remember { mutableIntStateOf(0) }
            var activeQuiz by remember { mutableStateOf<McqQuizItem?>(null) }
            var showLoginDialog by remember { mutableStateOf(false) }
            var loginErrorMessage by remember { mutableStateOf<String?>(null) }

            val context = LocalContext.current

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
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Icon(
                                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_lumio_logo),
                                                contentDescription = "Lumio Logo",
                                                modifier = Modifier.size(32.dp),
                                                tint = androidx.compose.ui.graphics.Color.Unspecified
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
                                                        0 -> "Study Resources"
                                                        1 -> "2026 Batch Notes"
                                                        2 -> "Daily Practice (DPP)"
                                                        3 -> "Practice MCQs"
                                                        else -> "Educator & System"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    actions = {
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
                                        onClick = { selectedTab = 0 },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 0) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                                contentDescription = "Resources"
                                            )
                                        },
                                        label = { Text("Resources") },
                                        modifier = Modifier.testTag("nav_resources")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 1) Icons.Filled.Description else Icons.Outlined.Description,
                                                contentDescription = "2026 Notes"
                                            )
                                        },
                                        label = { Text("2026 Notes") },
                                        modifier = Modifier.testTag("nav_notes")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
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
                                        onClick = { selectedTab = 3 },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 3) Icons.Filled.Quiz else Icons.Outlined.Quiz,
                                                contentDescription = "MCQs"
                                            )
                                        },
                                        label = { Text("MCQs") },
                                        modifier = Modifier.testTag("nav_mcqs")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 4,
                                        onClick = { selectedTab = 4 },
                                        icon = {
                                            Icon(
                                                if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
                                                contentDescription = "Owner"
                                            )
                                        },
                                        label = { Text("Owner") },
                                        modifier = Modifier.testTag("nav_owner")
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
                                        0 -> ResourcesScreen(
                                            resources = resources,
                                            ownerInfo = ownerInfo,
                                            isAdmin = isAdmin,
                                            onNavigateToDpp = { selectedTab = 2 },
                                            onNavigateToMcq = { selectedTab = 3 },
                                            onNavigateToNotes = { selectedTab = 1 },
                                            onAddResource = { name, link, filename ->
                                                viewModel.addResource(name, link, filename)
                                            },
                                            onEditResource = { resource ->
                                                viewModel.updateResource(resource)
                                            },
                                            onDeleteResource = { id ->
                                                viewModel.deleteResource(id)
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
                                            onDeleteQuiz = { id ->
                                                viewModel.deleteQuiz(id)
                                            }
                                        )
                                        4 -> AboutOwnerScreen(
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
                }
            }
        }
    }
}

package com.example.class10resources.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.class10resources.data.model.*
import com.example.class10resources.ui.components.AddEditSubjectDialog
import com.example.class10resources.ui.components.LumioBrandHeader
import com.example.class10resources.ui.components.LumioIcon
import com.example.class10resources.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsDashboardScreen(
    subjects: List<SubjectItem>,
    notes: List<Note2026Item>,
    dpps: List<DppItem>,
    quizzes: List<McqQuizItem>,
    pyqs: List<PyqItem>,
    ownerInfo: OwnerInfo?,
    isAdmin: Boolean,
    onSelectSubject: (SubjectItem, String?) -> Unit,
    onNavigateToDpp: () -> Unit,
    onNavigateToMcq: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToPyq: () -> Unit,
    onAddSubject: (name: String, description: String, chapters: String, colorHex: String, iconType: String) -> Unit,
    onDeleteSubject: (Long) -> Unit,
    onShareApp: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    // Filtered subjects based on search
    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) {
            subjects
        } else {
            subjects.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.chapters.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddSubjectDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_subject_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Subject")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lumio Branding Hero Banner
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Lumio Brand Logo & Wordmark
                            LumioBrandHeader(
                                iconSize = 46.dp,
                                showSubtitle = false,
                                modifier = Modifier.testTag("brand_header_lumio")
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Concepts First • Class 10 Board Excellence",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Shortcuts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToNotes,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentNotes),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Notes", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = onNavigateToDpp,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentDpp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DPP", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = onNavigateToMcq,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentMcq),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quiz", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = onNavigateToPyq,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PYQ", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Share App with Students Direct Link Banner
                            FilledTonalButton(
                                onClick = onShareApp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("dashboard_share_app_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Share App with Students (Direct APK Download)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Global Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search subjects, chapters, notes...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Physics Chapters Direct Jump Section
            item {
                val physicsSubject = subjects.find { it.name.equals("Physics", ignoreCase = true) }
                if (physicsSubject != null) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Physics Chapters Spotlight",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Chips for user-specified chapters: Light, Human Eye, Electricity, Magnetism
                        val physicsChapters = listOf(
                            "⚡ Electricity" to "Electricity",
                            "💡 Light" to "Light",
                            "👁️ Human Eye" to "Human Eye",
                            "🧲 Magnetism" to "Magnetism"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            physicsChapters.take(2).forEach { (label, filterVal) ->
                                OutlinedButton(
                                    onClick = { onSelectSubject(physicsSubject, filterVal) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            physicsChapters.drop(2).forEach { (label, filterVal) ->
                                OutlinedButton(
                                    onClick = { onSelectSubject(physicsSubject, filterVal) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Core Subjects Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Class 10 Core Subjects (${filteredSubjects.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isAdmin) {
                        TextButton(onClick = { showAddSubjectDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Subject")
                        }
                    }
                }
            }

            // Subject Cards List
            items(filteredSubjects) { subj ->
                val subjColor = remember(subj.colorHex) {
                    try {
                        Color(android.graphics.Color.parseColor(subj.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF2563EB)
                    }
                }

                // Calculate item counts for this subject
                val notesCount = notes.count { it.subject.equals(subj.name, ignoreCase = true) }
                val dppCount = dpps.count { it.subject.equals(subj.name, ignoreCase = true) }
                val quizCount = quizzes.count { it.subject.equals(subj.name, ignoreCase = true) }
                val pyqCount = pyqs.count { it.subject.equals(subj.name, ignoreCase = true) }
                val totalCount = notesCount + dppCount + quizCount + pyqCount

                SubjectCard(
                    subject = subj,
                    subjectColor = subjColor,
                    totalCount = totalCount,
                    notesCount = notesCount,
                    dppCount = dppCount,
                    quizCount = quizCount,
                    pyqCount = pyqCount,
                    isAdmin = isAdmin,
                    onClick = { onSelectSubject(subj, null) },
                    onDelete = { onDeleteSubject(subj.id) }
                )
            }

            // Share & Community Footer
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AccentInstagram.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AccentInstagram)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connect with Ashish Maurya",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Instagram: @ashraj7777",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                val link = ownerInfo?.instagramLink?.ifBlank { "https://www.instagram.com/ashraj7777/" }
                                    ?: "https://www.instagram.com/ashraj7777/"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                context.startActivity(intent)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Visit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        AddEditSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { name, description, chapters, colorHex, iconType ->
                onAddSubject(name, description, chapters, colorHex, iconType)
                showAddSubjectDialog = false
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: SubjectItem,
    subjectColor: Color,
    totalCount: Int,
    notesCount: Int,
    dppCount: Int,
    quizCount: Int,
    pyqCount: Int,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("subject_card_${subject.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(subjectColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when (subject.iconType.lowercase()) {
                        "chemistry" -> Icons.Default.Science
                        "biology" -> Icons.Default.Eco
                        "math" -> Icons.Default.Calculate
                        else -> Icons.Default.ElectricBolt
                    }
                    Icon(iconVector, contentDescription = null, tint = subjectColor, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (totalCount > 0) "$totalCount learning materials" else "New Subject Hub",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isAdmin && !isDefaultSubject(subject.name)) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Subject", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(subjectColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open ${subject.name}",
                            tint = subjectColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (subject.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = subject.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Chapter pills preview
            if (subject.chapters.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                val chapterList = subject.chapters.split(",").map { it.trim() }.filter { it.isNotBlank() }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(chapterList) { chap ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = chap,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer statistics & direct button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listOfNotNull(
                        if (notesCount > 0) "$notesCount Notes" else null,
                        if (dppCount > 0) "$dppCount DPPs" else null,
                        if (quizCount > 0) "$quizCount Quizzes" else null,
                        if (pyqCount > 0) "$pyqCount PYQs" else null
                    ).joinToString(" • ").ifEmpty { "Tap to explore" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = subjectColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Open Hub", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

private fun isDefaultSubject(name: String): Boolean {
    return name.equals("Physics", ignoreCase = true) ||
            name.equals("Chemistry", ignoreCase = true) ||
            name.equals("Biology", ignoreCase = true)
}

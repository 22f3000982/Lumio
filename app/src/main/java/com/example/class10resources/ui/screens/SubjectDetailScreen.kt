package com.example.class10resources.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.class10resources.data.model.*
import com.example.class10resources.ui.components.*
import com.example.class10resources.ui.theme.*
import com.example.class10resources.util.FileUtils

sealed class SubjectContentItem {
    data class Note(val item: Note2026Item) : SubjectContentItem()
    data class Dpp(val item: DppItem) : SubjectContentItem()
    data class Quiz(val item: McqQuizItem) : SubjectContentItem()
    data class Pyq(val item: PyqItem) : SubjectContentItem()

    val title: String
        get() = when (this) {
            is Note -> item.name
            is Dpp -> item.title
            is Quiz -> item.title
            is Pyq -> item.title
        }

    val typeLabel: String
        get() = when (this) {
            is Note -> "Note"
            is Dpp -> "DPP"
            is Quiz -> "Quiz"
            is Pyq -> "PYQ"
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subject: SubjectItem,
    allSubjects: List<SubjectItem>,
    notes: List<Note2026Item>,
    dpps: List<DppItem>,
    quizzes: List<McqQuizItem>,
    pyqs: List<PyqItem>,
    initialChapterFilter: String? = null,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onStartQuiz: (McqQuizItem) -> Unit,
    onAddNote: (name: String, link: String?, filename: String?, subject: String) -> Unit,
    onUpdateNote: (Note2026Item) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddDpp: (title: String, driveLink: String, filename: String?, subject: String) -> Unit,
    onUpdateDpp: (DppItem) -> Unit,
    onDeleteDpp: (Long) -> Unit,
    onAddQuiz: (title: String, details: String?, filename: String, fileType: String, subject: String) -> Unit,
    onUpdateQuiz: (McqQuizItem) -> Unit,
    onDeleteQuiz: (Long) -> Unit,
    onAddPyq: (title: String, year: String, link: String?, filename: String?, subject: String, chapter: String?) -> Unit,
    onUpdatePyq: (PyqItem) -> Unit,
    onDeletePyq: (Long) -> Unit
) {
    // Intercept back button so it navigates back inside the app instead of closing/cutting the app
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedChapter by remember { mutableStateOf(initialChapterFilter ?: "All") }
    var selectedCategory by remember { mutableStateOf("All") } // All, Notes, DPP, Quiz, PYQ

    var showAdminAddMenu by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddDppDialog by remember { mutableStateOf(false) }
    var showAddQuizDialog by remember { mutableStateOf(false) }
    var showAddPyqDialog by remember { mutableStateOf(false) }

    var editingNote by remember { mutableStateOf<Note2026Item?>(null) }
    var editingDpp by remember { mutableStateOf<DppItem?>(null) }
    var editingQuiz by remember { mutableStateOf<McqQuizItem?>(null) }
    var editingPyq by remember { mutableStateOf<PyqItem?>(null) }

    val subjectColor = remember(subject.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(subject.colorHex))
        } catch (e: Exception) {
            Color(0xFF2563EB)
        }
    }

    // Parse chapters from subject
    val chaptersList = remember(subject.chapters) {
        if (subject.chapters.isNotBlank()) {
            listOf("All") + subject.chapters.split(",").map { it.trim() }.filter { it.isNotBlank() }
        } else {
            listOf("All")
        }
    }

    // Aggregate all items for this subject (Notes, DPPs, Quizzes, PYQs)
    val subjectItems = remember(subject.name, notes, dpps, quizzes, pyqs) {
        val list = mutableListOf<SubjectContentItem>()

        // 2026 notes
        notes.filter { it.subject.equals(subject.name, ignoreCase = true) }
            .forEach { list.add(SubjectContentItem.Note(it)) }

        // DPPs
        dpps.filter { it.subject.equals(subject.name, ignoreCase = true) }
            .forEach { list.add(SubjectContentItem.Dpp(it)) }

        // Quizzes
        quizzes.filter { it.subject.equals(subject.name, ignoreCase = true) }
            .forEach { list.add(SubjectContentItem.Quiz(it)) }

        // PYQs
        pyqs.filter { it.subject.equals(subject.name, ignoreCase = true) }
            .forEach { list.add(SubjectContentItem.Pyq(it)) }

        list
    }

    // Filter by search, chapter, and category
    val filteredItems = remember(subjectItems, searchQuery, selectedChapter, selectedCategory) {
        subjectItems.filter { item ->
            // Category filter
            val matchesCategory = when (selectedCategory) {
                "Notes" -> item is SubjectContentItem.Note
                "DPP" -> item is SubjectContentItem.Dpp
                "Quiz" -> item is SubjectContentItem.Quiz
                "PYQ" -> item is SubjectContentItem.Pyq
                else -> true
            }

            // Chapter filter
            val matchesChapter = if (selectedChapter == "All") {
                true
            } else {
                when (item) {
                    is SubjectContentItem.Pyq -> {
                        item.item.chapter?.contains(selectedChapter, ignoreCase = true) == true ||
                                item.title.contains(selectedChapter, ignoreCase = true)
                    }
                    else -> item.title.contains(selectedChapter, ignoreCase = true)
                }
            }

            // Search query
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                item.title.contains(searchQuery, ignoreCase = true)
            }

            matchesCategory && matchesChapter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${subjectItems.size} materials • Notes, DPP, Quiz, PYQ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("subject_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Dashboard")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                Column(horizontalAlignment = Alignment.End) {
                    if (showAdminAddMenu) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                TextButton(
                                    onClick = {
                                        showAdminAddMenu = false
                                        showAddNoteDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_note_btn_menu")
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentNotes)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Note (PDF / Link)")
                                }
                                TextButton(
                                    onClick = {
                                        showAdminAddMenu = false
                                        showAddDppDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_dpp_btn_menu")
                                ) {
                                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentDpp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add DPP Problem Sheet")
                                }
                                TextButton(
                                    onClick = {
                                        showAdminAddMenu = false
                                        showAddQuizDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_quiz_btn_menu")
                                ) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentMcq)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Practice MCQ Quiz")
                                }
                                TextButton(
                                    onClick = {
                                        showAdminAddMenu = false
                                        showAddPyqDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_pyq_btn_menu")
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Previous Year Question (PYQ)")
                                }
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { showAdminAddMenu = !showAdminAddMenu },
                        containerColor = subjectColor,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("subject_add_fab")
                    ) {
                        Icon(
                            if (showAdminAddMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add material to subject"
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Subject Banner Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = subjectColor.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(subjectColor),
                                contentAlignment = Alignment.Center
                            ) {
                                val iconVector = when (subject.iconType.lowercase()) {
                                    "chemistry" -> Icons.Default.Science
                                    "biology" -> Icons.Default.Eco
                                    "math" -> Icons.Default.Calculate
                                    else -> Icons.Default.ElectricBolt
                                }
                                Icon(iconVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Class 10 • ${subject.name}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (subject.description.isNotBlank()) {
                                    Text(
                                        text = subject.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Mini metrics: Notes, DPP, Quiz, PYQ
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubjectMiniStat(
                                label = "Notes",
                                count = subjectItems.count { it is SubjectContentItem.Note },
                                tint = AccentNotes,
                                modifier = Modifier.weight(1f)
                            )
                            SubjectMiniStat(
                                label = "DPP",
                                count = subjectItems.count { it is SubjectContentItem.Dpp },
                                tint = AccentDpp,
                                modifier = Modifier.weight(1f)
                            )
                            SubjectMiniStat(
                                label = "Quiz",
                                count = subjectItems.count { it is SubjectContentItem.Quiz },
                                tint = AccentMcq,
                                modifier = Modifier.weight(1f)
                            )
                            SubjectMiniStat(
                                label = "PYQ",
                                count = subjectItems.count { it is SubjectContentItem.Pyq },
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search in ${subject.name} (e.g. formula, theory, question)") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Chapter Filter Chips
            if (chaptersList.size > 1) {
                item {
                    Column {
                        Text(
                            text = "Chapters in ${subject.name}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(chaptersList) { chap ->
                                val isSelected = selectedChapter.equals(chap, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedChapter = chap },
                                    label = { Text(chap, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // Category Tabs: All, Notes, DPP, Quiz, PYQ
            item {
                val categories = listOf("All", "Notes", "DPP", "Quiz", "PYQ")
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            text = { Text(cat, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Items Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Materials (${filteredItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedChapter != "All" || selectedCategory != "All" || searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                selectedChapter = "All"
                                selectedCategory = "All"
                                searchQuery = ""
                            }
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            }

            // Empty state
            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No $selectedCategory materials for ${subject.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Try searching with different keywords"
                                else "You can upload notes, DPPs, quizzes, and PYQs for this subject using the '+' button.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredItems) { item ->
                    SubjectMaterialCard(
                        item = item,
                        isAdmin = isAdmin,
                        onOpen = { openItem(context, item, onStartQuiz) },
                        onEdit = {
                            when (item) {
                                is SubjectContentItem.Note -> editingNote = item.item
                                is SubjectContentItem.Dpp -> editingDpp = item.item
                                is SubjectContentItem.Quiz -> editingQuiz = item.item
                                is SubjectContentItem.Pyq -> editingPyq = item.item
                            }
                        },
                        onDelete = {
                            when (item) {
                                is SubjectContentItem.Note -> onDeleteNote(item.item.id)
                                is SubjectContentItem.Dpp -> onDeleteDpp(item.item.id)
                                is SubjectContentItem.Quiz -> onDeleteQuiz(item.item.id)
                                is SubjectContentItem.Pyq -> onDeletePyq(item.item.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs for Admin - Add
    if (showAddNoteDialog) {
        AddEditNoteDialog(
            subjects = allSubjects,
            defaultSubject = subject.name,
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { name, link, filename, subj ->
                onAddNote(name, link, filename, subj)
                showAddNoteDialog = false
            }
        )
    }

    if (showAddDppDialog) {
        AddEditDppDialog(
            subjects = allSubjects,
            defaultSubject = subject.name,
            onDismiss = { showAddDppDialog = false },
            onConfirm = { title, driveLink, filename, subj ->
                onAddDpp(title, driveLink, filename, subj)
                showAddDppDialog = false
            }
        )
    }

    if (showAddQuizDialog) {
        AddEditQuizDialog(
            subjects = allSubjects,
            defaultSubject = subject.name,
            onDismiss = { showAddQuizDialog = false },
            onConfirm = { title, details, filename, fileType, subj ->
                onAddQuiz(title, details, filename, fileType, subj)
                showAddQuizDialog = false
            }
        )
    }

    if (showAddPyqDialog) {
        AddEditPyqDialog(
            subjects = allSubjects,
            defaultSubject = subject.name,
            defaultChapter = if (selectedChapter != "All") selectedChapter else null,
            onDismiss = { showAddPyqDialog = false },
            onConfirm = { title, year, link, filename, subj, chap ->
                onAddPyq(title, year, link, filename, subj, chap)
                showAddPyqDialog = false
            }
        )
    }

    // Dialogs for Admin - Edit
    if (editingNote != null) {
        AddEditNoteDialog(
            initialNote = editingNote,
            subjects = allSubjects,
            defaultSubject = subject.name,
            onDismiss = { editingNote = null },
            onConfirm = { name, link, filename, subj ->
                onUpdateNote(
                    editingNote!!.copy(
                        name = name,
                        link = link,
                        filename = filename,
                        subject = subj
                    )
                )
                editingNote = null
            }
        )
    }

    if (editingDpp != null) {
        AddEditDppDialog(
            initialDpp = editingDpp,
            subjects = allSubjects,
            defaultSubject = subject.name,
            onDismiss = { editingDpp = null },
            onConfirm = { title, driveLink, filename, subj ->
                onUpdateDpp(
                    editingDpp!!.copy(
                        title = title,
                        driveLink = driveLink,
                        filename = filename,
                        subject = subj
                    )
                )
                editingDpp = null
            }
        )
    }

    if (editingQuiz != null) {
        AddEditQuizDialog(
            initialQuiz = editingQuiz,
            subjects = allSubjects,
            defaultSubject = subject.name,
            onDismiss = { editingQuiz = null },
            onConfirm = { title, details, filename, fileType, subj ->
                onUpdateQuiz(
                    editingQuiz!!.copy(
                        title = title,
                        details = details,
                        filename = filename,
                        fileType = fileType,
                        subject = subj
                    )
                )
                editingQuiz = null
            }
        )
    }

    if (editingPyq != null) {
        AddEditPyqDialog(
            initialPyq = editingPyq,
            subjects = allSubjects,
            defaultSubject = subject.name,
            defaultChapter = editingPyq?.chapter,
            onDismiss = { editingPyq = null },
            onConfirm = { title, year, link, filename, subj, chap ->
                onUpdatePyq(
                    editingPyq!!.copy(
                        title = title,
                        year = year,
                        link = link,
                        filename = filename,
                        subject = subj,
                        chapter = chap
                    )
                )
                editingPyq = null
            }
        )
    }
}

@Composable
private fun SubjectMiniStat(
    label: String,
    count: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SubjectMaterialCard(
    item: SubjectContentItem,
    isAdmin: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeColor, badgeText, iconVector) = when (item) {
        is SubjectContentItem.Note -> Triple(AccentNotes, "NOTE", Icons.Default.Description)
        is SubjectContentItem.Dpp -> Triple(AccentDpp, "DPP", Icons.Default.Assignment)
        is SubjectContentItem.Quiz -> Triple(AccentMcq, "QUIZ", Icons.Default.Quiz)
        is SubjectContentItem.Pyq -> Triple(MaterialTheme.colorScheme.primary, "PYQ ${item.item.year}", Icons.Default.School)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(iconVector, contentDescription = null, tint = typeColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Admin Action Buttons: Edit and Delete
                if (isAdmin) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Material", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Material", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Subtitle or details
            val subtitle = when (item) {
                is SubjectContentItem.Note -> if (!item.item.filename.isNullOrBlank()) "Annotated PDF Note" else "Class 10 Study Note"
                is SubjectContentItem.Dpp -> if (!item.item.filename.isNullOrBlank()) "Practice Sheet PDF" else "Google Drive DPP"
                is SubjectContentItem.Quiz -> item.item.details ?: "Interactive Chapter Assessment"
                is SubjectContentItem.Pyq -> {
                    val chapStr = if (!item.item.chapter.isNullOrBlank()) " • ${item.item.chapter}" else ""
                    "CBSE Board ${item.item.year}$chapStr"
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val actionLabel = when (item) {
                        is SubjectContentItem.Quiz -> "Start Quiz"
                        is SubjectContentItem.Pyq -> if (!item.item.filename.isNullOrBlank()) "View PDF" else "Open PYQ"
                        is SubjectContentItem.Note -> if (!item.item.filename.isNullOrBlank()) "View PDF" else "Open Note"
                        is SubjectContentItem.Dpp -> "Open DPP"
                    }
                    val actionIcon = when (item) {
                        is SubjectContentItem.Quiz -> Icons.Default.PlayArrow
                        else -> Icons.AutoMirrored.Filled.OpenInNew
                    }
                    Icon(actionIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(actionLabel, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun openItem(context: Context, item: SubjectContentItem, onStartQuiz: (McqQuizItem) -> Unit) {
    when (item) {
        is SubjectContentItem.Note -> {
            FileUtils.openItem(context, item.item.link, item.item.filename, item.item.name)
        }
        is SubjectContentItem.Dpp -> {
            FileUtils.openItem(context, item.item.driveLink, item.item.filename, item.item.title)
        }
        is SubjectContentItem.Quiz -> {
            onStartQuiz(item.item)
        }
        is SubjectContentItem.Pyq -> {
            FileUtils.openItem(context, item.item.link, item.item.filename, item.item.title)
        }
    }
}

package com.example.class10resources.ui.screens

import android.content.Context
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.class10resources.data.model.PyqItem
import com.example.class10resources.data.model.SubjectItem
import com.example.class10resources.ui.components.AddEditPyqDialog
import com.example.class10resources.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqScreen(
    pyqs: List<PyqItem>,
    subjects: List<SubjectItem>,
    isAdmin: Boolean,
    onAddPyq: (title: String, year: String, link: String?, filename: String?, subject: String, chapter: String?) -> Unit,
    onUpdatePyq: (PyqItem) -> Unit,
    onDeletePyq: (Long) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var selectedYearFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPyq by remember { mutableStateOf<PyqItem?>(null) }
    var pyqToDelete by remember { mutableStateOf<PyqItem?>(null) }

    // Dynamic subjects and years
    val subjectFilters = remember(subjects) {
        listOf("All") + (subjects.map { it.name } + listOf("Physics", "Chemistry", "Biology")).distinct()
    }

    val availableYears = remember(pyqs) {
        listOf("All") + pyqs.map { it.year }.filter { it.isNotBlank() }.distinct().sortedDescending()
    }

    // Filtered PYQs
    val filteredPyqs = remember(pyqs, searchQuery, selectedSubjectFilter, selectedYearFilter) {
        pyqs.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.year.contains(searchQuery, ignoreCase = true) ||
                    (item.chapter?.contains(searchQuery, ignoreCase = true) == true) ||
                    item.subject.contains(searchQuery, ignoreCase = true)

            val matchesSubject = selectedSubjectFilter == "All" ||
                    item.subject.equals(selectedSubjectFilter, ignoreCase = true)

            val matchesYear = selectedYearFilter == "All" ||
                    item.year.equals(selectedYearFilter, ignoreCase = true)

            matchesSearch && matchesSubject && matchesYear
        }
    }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_pyq_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Upload PYQ")
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
            // Hero Banner
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Previous Year Questions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "CBSE Class 10 Board Papers & Chapter-wise PYQs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${pyqs.size} Papers",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                    placeholder = { Text("Search PYQ by chapter, year, question...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pyq_search_input")
                )
            }

            // Subject Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Filter by Subject",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjectFilters) { sFilter ->
                            FilterChip(
                                selected = selectedSubjectFilter == sFilter,
                                onClick = { selectedSubjectFilter = sFilter },
                                label = { Text(sFilter) }
                            )
                        }
                    }
                }
            }

            // Year Filter Chips
            if (availableYears.size > 2) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Filter by Examination Year",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableYears) { yFilter ->
                                FilterChip(
                                    selected = selectedYearFilter == yFilter,
                                    onClick = { selectedYearFilter = yFilter },
                                    label = { Text(yFilter) }
                                )
                            }
                        }
                    }
                }
            }

            // Empty state
            if (filteredPyqs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No PYQ papers found",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isAdmin) "Tap the '+' button to upload Previous Year Questions for your students."
                                else "No questions uploaded for this category yet. Check back soon!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // PYQ List
            items(filteredPyqs, key = { it.id }) { pyq ->
                PyqItemCard(
                    pyq = pyq,
                    isAdmin = isAdmin,
                    onOpen = { FileUtils.openItem(context, pyq.link, pyq.filename, pyq.title) },
                    onEdit = { editingPyq = pyq },
                    onDelete = { pyqToDelete = pyq }
                )
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditPyqDialog(
            subjects = subjects,
            defaultSubject = if (selectedSubjectFilter != "All") selectedSubjectFilter else "Physics",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, year, link, filename, subject, chapter ->
                onAddPyq(title, year, link, filename, subject, chapter)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    if (editingPyq != null) {
        AddEditPyqDialog(
            initialPyq = editingPyq,
            subjects = subjects,
            onDismiss = { editingPyq = null },
            onConfirm = { title, year, link, filename, subject, chapter ->
                onUpdatePyq(
                    editingPyq!!.copy(
                        title = title,
                        year = year,
                        link = link,
                        filename = filename,
                        subject = subject,
                        chapter = chapter
                    )
                )
                editingPyq = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (pyqToDelete != null) {
        AlertDialog(
            onDismissRequest = { pyqToDelete = null },
            title = { Text("Delete PYQ Paper") },
            text = { Text("Are you sure you want to delete \"${pyqToDelete?.title}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        pyqToDelete?.let { onDeletePyq(it.id) }
                        pyqToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pyqToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PyqItemCard(
    pyq: PyqItem,
    isAdmin: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = when (pyq.subject.lowercase()) {
        "chemistry" -> Color(0xFFD97706)
        "biology" -> Color(0xFF059669)
        else -> Color(0xFF2563EB)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("pyq_item_${pyq.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Year Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "CBSE ${pyq.year}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Subject Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = subjectColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = pyq.subject,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = subjectColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Chapter Tag (if any)
                if (!pyq.chapter.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = pyq.chapter,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Admin Action Buttons
                if (isAdmin) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp).testTag("edit_pyq_${pyq.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit PYQ",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_pyq_${pyq.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete PYQ",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = pyq.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // File / Link details & Open button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!pyq.filename.isNullOrBlank()) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "PDF Document",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (!pyq.link.isNullOrBlank()) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Cloud Drive Link",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (!pyq.filename.isNullOrBlank()) "View PDF" else "Open Paper",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

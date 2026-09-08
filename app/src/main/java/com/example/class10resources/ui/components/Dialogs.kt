package com.example.class10resources.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.class10resources.data.model.*
import com.example.class10resources.util.FileUtils

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String) -> Unit,
    errorMessage: String?
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Admin Authentication", fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column {
                Text(
                    "Enter admin password to manage class resources, notes, DPPs, and configuration.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Admin Security PIN") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = errorMessage != null,
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.testTag("toggle_password_visibility")
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Secure 4-digit PIN verification")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLogin(password) },
                modifier = Modifier.testTag("admin_login_submit")
            ) {
                Text("Verify & Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditResourceDialog(
    initialResource: ResourceItem? = null,
    subjects: List<SubjectItem> = emptyList(),
    defaultSubject: String = "Physics",
    onDismiss: () -> Unit,
    onConfirm: (name: String, link: String?, filename: String?, subject: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialResource?.name ?: "") }
    var link by remember { mutableStateOf(initialResource?.link ?: "") }
    var selectedSubject by remember { mutableStateOf(initialResource?.subject ?: defaultSubject) }
    var localFilename by remember { mutableStateOf(initialResource?.filename) }
    var uploadedFileSizeText by remember { mutableStateOf<String?>(null) }
    var hasError by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = FileUtils.saveUploadedFile(context, uri)
            if (saved != null) {
                localFilename = saved.first
                uploadedFileSizeText = FileUtils.formatFileSize(saved.second)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialResource == null) "Add New Resource" else "Edit Resource", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        hasError = it.isBlank()
                    },
                    label = { Text("Resource Title *") },
                    placeholder = { Text("e.g. Chapter 10 Light Complete Notes") },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resource_name_input")
                )

                // PDF Upload Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Direct PDF Upload",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!localFilename.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        localFilename ?: "Document.pdf",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    if (uploadedFileSizeText != null) {
                                        Text(
                                            uploadedFileSizeText!!,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        localFilename = null
                                        uploadedFileSizeText = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove PDF",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_pdf_btn")
                        ) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (localFilename == null) "Select PDF from Device" else "Change PDF File")
                        }
                    }
                }

                // Subject Selector
                Text(
                    text = "Subject Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val subjectOptions = if (subjects.isNotEmpty()) {
                    subjects.map { it.name }.distinct()
                } else {
                    listOf("Physics", "Chemistry", "Biology")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectOptions.forEach { opt ->
                        val isSelected = selectedSubject.equals(opt, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                // Web or Google Drive Link
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Google Drive / Web Link (Optional)") },
                    placeholder = { Text("https://drive.google.com/...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resource_link_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            link.trim().ifBlank { null },
                            localFilename?.trim()?.ifBlank { null },
                            selectedSubject.trim()
                        )
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("save_resource_button")
            ) {
                Text(if (initialResource == null) "Add Resource" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditDppDialog(
    initialDpp: DppItem? = null,
    subjects: List<SubjectItem> = emptyList(),
    defaultSubject: String = "Physics",
    onDismiss: () -> Unit,
    onConfirm: (title: String, driveLink: String, filename: String?, subject: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialDpp?.title ?: "") }
    var driveLink by remember { mutableStateOf(initialDpp?.driveLink ?: "") }
    var selectedSubject by remember { mutableStateOf(initialDpp?.subject ?: defaultSubject) }
    var localFilename by remember { mutableStateOf(initialDpp?.filename) }
    var uploadedFileSizeText by remember { mutableStateOf<String?>(null) }
    var hasError by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = FileUtils.saveUploadedFile(context, uri)
            if (saved != null) {
                localFilename = saved.first
                uploadedFileSizeText = FileUtils.formatFileSize(saved.second)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialDpp == null) "Add Daily Practice Problem (DPP)" else "Edit DPP", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        hasError = it.isBlank()
                    },
                    label = { Text("DPP Title *") },
                    placeholder = { Text("e.g. DPP-01 Electricity Level-1") },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dpp_title_input")
                )

                // PDF Upload Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Direct PDF Upload",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!localFilename.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        localFilename ?: "Document.pdf",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    if (uploadedFileSizeText != null) {
                                        Text(
                                            uploadedFileSizeText!!,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        localFilename = null
                                        uploadedFileSizeText = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove PDF",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_dpp_pdf_btn")
                        ) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (localFilename == null) "Select PDF from Device" else "Change PDF File")
                        }
                    }
                }

                // Subject Selector
                Text(
                    text = "Subject Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val dppSubjectOptions = if (subjects.isNotEmpty()) {
                    subjects.map { it.name }.distinct()
                } else {
                    listOf("Physics", "Chemistry", "Biology")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dppSubjectOptions.forEach { opt ->
                        val isSelected = selectedSubject.equals(opt, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                // Google Drive Link
                OutlinedTextField(
                    value = driveLink,
                    onValueChange = { driveLink = it },
                    label = { Text("Google Drive / Cloud Link (Optional if PDF attached)") },
                    placeholder = { Text("https://drive.google.com/...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dpp_link_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && (driveLink.isNotBlank() || !localFilename.isNullOrBlank())) {
                        onConfirm(
                            title.trim(),
                            driveLink.trim().ifBlank { "" },
                            localFilename?.trim()?.ifBlank { null },
                            selectedSubject.trim()
                        )
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("save_dpp_button")
            ) {
                Text(if (initialDpp == null) "Add DPP" else "Save DPP")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditNoteDialog(
    initialNote: Note2026Item? = null,
    subjects: List<SubjectItem> = emptyList(),
    defaultSubject: String = "Physics",
    onDismiss: () -> Unit,
    onConfirm: (name: String, link: String?, filename: String?, subject: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialNote?.name ?: "") }
    var link by remember { mutableStateOf(initialNote?.link ?: "") }
    var selectedSubject by remember { mutableStateOf(initialNote?.subject ?: defaultSubject) }
    var localFilename by remember { mutableStateOf(initialNote?.filename) }
    var uploadedFileSizeText by remember { mutableStateOf<String?>(null) }
    var hasError by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = FileUtils.saveUploadedFile(context, uri)
            if (saved != null) {
                localFilename = saved.first
                uploadedFileSizeText = FileUtils.formatFileSize(saved.second)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialNote == null) "Add Class 10 Note (2026)" else "Edit Note", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        hasError = it.isBlank()
                    },
                    label = { Text("Note Title *") },
                    placeholder = { Text("e.g. Human Eye & Colourful World - Annotated") },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // PDF Upload Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Direct PDF Upload",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!localFilename.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        localFilename ?: "Document.pdf",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    if (uploadedFileSizeText != null) {
                                        Text(
                                            uploadedFileSizeText!!,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        localFilename = null
                                        uploadedFileSizeText = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove PDF",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (localFilename == null) "Select PDF from Device" else "Change PDF File")
                        }
                    }
                }

                // Subject Selector
                Text(
                    text = "Subject Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val noteSubjectOptions = if (subjects.isNotEmpty()) {
                    subjects.map { it.name }.distinct()
                } else {
                    listOf("Physics", "Chemistry", "Biology")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    noteSubjectOptions.forEach { opt ->
                        val isSelected = selectedSubject.equals(opt, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Drive Link / Online Resource (Optional)") },
                    placeholder = { Text("https://drive.google.com/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            link.trim().ifBlank { null },
                            localFilename?.trim()?.ifBlank { null },
                            selectedSubject.trim()
                        )
                    } else {
                        hasError = true
                    }
                }
            ) {
                Text(if (initialNote == null) "Upload Note" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BackupRestoreDialog(
    onDismiss: () -> Unit,
    onExportBackup: (Uri) -> Unit,
    onRestoreBackup: (Uri) -> Unit,
    onResetData: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            onExportBackup(uri)
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onRestoreBackup(uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Backup & Restore System", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Manage local database state. Export your full resources, notes, DPPs, and quiz datasets as a portable JSON file, or restore from a previously exported backup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Export Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Backup (JSON)", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Saves all current items and settings to a JSON file in your storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val timestamp = System.currentTimeMillis()
                                createBackupLauncher.launch("Lumio_Backup_$timestamp.json")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Backup File")
                        }
                    }
                }

                // Import Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore from Backup", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Restores content from a previously generated Lumio backup JSON.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                openBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Backup File to Restore")
                        }
                    }
                }

                // Factory Reset
                if (!showResetConfirm) {
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Default Class 10 Syllabus")
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Are you sure? This will reload the default CBSE Class 10 science resources.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onResetData()
                                        showResetConfirm = false
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Yes, Reset")
                                }
                                OutlinedButton(
                                    onClick = { showResetConfirm = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EditOwnerDialog(
    ownerInfo: OwnerInfo,
    onDismiss: () -> Unit,
    onSave: (OwnerInfo) -> Unit
) {
    var name by remember { mutableStateOf(ownerInfo.name) }
    var description by remember { mutableStateOf(ownerInfo.description) }
    var contact by remember { mutableStateOf(ownerInfo.contact) }
    var instagramLink by remember { mutableStateOf(ownerInfo.instagramLink) }
    var mcqLink by remember { mutableStateOf(ownerInfo.mcqLink) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Teacher / Owner Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Teacher Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Bio / Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instagramLink,
                    onValueChange = { instagramLink = it },
                    label = { Text("Instagram Profile Link (@ashraj7777)") },
                    placeholder = { Text("https://www.instagram.com/ashraj7777/") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mcqLink,
                    onValueChange = { mcqLink = it },
                    label = { Text("AI / Perplexity MCQ Portal Link") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ownerInfo.copy(
                            name = name.trim(),
                            description = description.trim(),
                            contact = contact.trim(),
                            instagramLink = instagramLink.trim(),
                            mcqLink = mcqLink.trim()
                        )
                    )
                }
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditSubjectDialog(
    initialSubject: SubjectItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, chapters: String, colorHex: String, iconType: String) -> Unit
) {
    var name by remember { mutableStateOf(initialSubject?.name ?: "") }
    var description by remember { mutableStateOf(initialSubject?.description ?: "") }
    var chapters by remember { mutableStateOf(initialSubject?.chapters ?: "") }
    var selectedColor by remember { mutableStateOf(initialSubject?.colorHex ?: "#2563EB") }
    var iconType by remember { mutableStateOf(initialSubject?.iconType ?: "physics") }
    var hasError by remember { mutableStateOf(false) }

    val colorPresets = listOf(
        "#2563EB", // Blue
        "#DC2626", // Red
        "#059669", // Emerald
        "#7C3AED", // Purple
        "#D97706", // Amber
        "#0891B2"  // Cyan
    )

    val iconPresets = listOf(
        "physics" to "⚡ Physics",
        "chemistry" to "🧪 Chemistry",
        "biology" to "🌱 Biology",
        "math" to "📐 Math",
        "book" to "📚 General"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSubject == null) "Add New Subject" else "Edit Subject",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        hasError = it.isBlank()
                    },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Mathematics, Social Science") },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Brief Description") },
                    placeholder = { Text("e.g. NCERT Class 10 full curriculum") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = chapters,
                    onValueChange = { chapters = it },
                    label = { Text("Chapters (comma separated)") },
                    placeholder = { Text("Light, Human Eye, Electricity, Magnetism") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Theme Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorPresets.forEach { hex ->
                        val color = try {
                            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (selectedColor.equals(hex, ignoreCase = true)) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape)
                                    } else Modifier
                                )
                        )
                    }
                }

                Text("Icon / Category Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    iconPresets.forEach { (type, label) ->
                        FilterChip(
                            selected = iconType == type,
                            onClick = { iconType = type },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            description.trim(),
                            chapters.trim(),
                            selectedColor,
                            iconType
                        )
                    } else {
                        hasError = true
                    }
                }
            ) {
                Text(if (initialSubject == null) "Create Subject" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditPyqDialog(
    initialPyq: PyqItem? = null,
    subjects: List<SubjectItem> = emptyList(),
    defaultSubject: String = "Physics",
    defaultChapter: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, year: String, link: String?, filename: String?, subject: String, chapter: String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialPyq?.title ?: "") }
    var year by remember { mutableStateOf(initialPyq?.year ?: "2024") }
    var link by remember { mutableStateOf(initialPyq?.link ?: "") }
    var selectedSubject by remember { mutableStateOf(initialPyq?.subject ?: defaultSubject) }
    var selectedChapter by remember { mutableStateOf(initialPyq?.chapter ?: defaultChapter ?: "") }
    var localFilename by remember { mutableStateOf(initialPyq?.filename) }
    var uploadedFileSizeText by remember { mutableStateOf<String?>(null) }
    var hasError by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = FileUtils.saveUploadedFile(context, uri)
            if (saved != null) {
                localFilename = saved.first
                uploadedFileSizeText = FileUtils.formatFileSize(saved.second)
            }
        }
    }

    val subjectObj = subjects.find { it.name.equals(selectedSubject, ignoreCase = true) }
    val availableChapters = remember(subjectObj) {
        subjectObj?.chapters?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }

    val yearPresets = listOf("2024", "2023", "2022", "2020", "All Years")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialPyq == null) "Add Previous Year Question (PYQ)" else "Edit PYQ",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        hasError = it.isBlank()
                    },
                    label = { Text("PYQ Title / Board Paper *") },
                    placeholder = { Text("e.g. CBSE 2024 Science Set-1 or Human Eye PYQ") },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pyq_title_input")
                )

                // Year selector
                Text(
                    text = "Examination Year",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    yearPresets.forEach { y ->
                        FilterChip(
                            selected = year == y,
                            onClick = { year = y },
                            label = { Text(y, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Custom Year / Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Subject Selector
                Text(
                    text = "Subject Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val subjectNames = if (subjects.isNotEmpty()) {
                    subjects.map { it.name }.distinct()
                } else {
                    listOf("Physics", "Chemistry", "Biology")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectNames.forEach { sName ->
                        FilterChip(
                            selected = selectedSubject.equals(sName, ignoreCase = true),
                            onClick = {
                                selectedSubject = sName
                                selectedChapter = ""
                            },
                            label = { Text(sName) }
                        )
                    }
                }

                // Chapter Selector (if available)
                if (availableChapters.isNotEmpty()) {
                    Text(
                        text = "Related Chapter (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        availableChapters.chunked(2).forEach { rowChapters ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowChapters.forEach { chap ->
                                    FilterChip(
                                        selected = selectedChapter == chap,
                                        onClick = {
                                            selectedChapter = if (selectedChapter == chap) "" else chap
                                        },
                                        label = { Text(chap, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // PDF Upload Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Direct PDF Upload",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!localFilename.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        localFilename ?: "Document.pdf",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    if (uploadedFileSizeText != null) {
                                        Text(
                                            uploadedFileSizeText!!,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        localFilename = null
                                        uploadedFileSizeText = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove PDF",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth().testTag("upload_pyq_pdf_btn")
                        ) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (localFilename == null) "Select PDF from Device" else "Change PDF File")
                        }
                    }
                }

                // Drive or external link
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Google Drive / Cloud Link (Optional if PDF selected)") },
                    placeholder = { Text("https://drive.google.com/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pyq_link_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            year.trim(),
                            link.trim().ifBlank { null },
                            localFilename?.trim()?.ifBlank { null },
                            selectedSubject.trim(),
                            selectedChapter.trim().ifBlank { null }
                        )
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("save_pyq_button")
            ) {
                Text(if (initialPyq == null) "Add PYQ" else "Save PYQ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditQuizDialog(
    initialQuiz: McqQuizItem? = null,
    subjects: List<SubjectItem> = emptyList(),
    defaultSubject: String = "Physics",
    onDismiss: () -> Unit,
    onConfirm: (title: String, details: String?, filename: String, fileType: String, subject: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialQuiz?.title ?: "") }
    var details by remember { mutableStateOf(initialQuiz?.details ?: "") }
    var filename by remember { mutableStateOf(initialQuiz?.filename ?: "") }
    var selectedSubject by remember { mutableStateOf(initialQuiz?.subject ?: defaultSubject) }
    var fileType by remember { mutableStateOf(initialQuiz?.fileType ?: "html") }
    var hasError by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = FileUtils.saveUploadedFile(context, uri)
            if (saved != null) {
                filename = saved.first
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialQuiz == null) "Add MCQ Quiz" else "Edit MCQ Quiz", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        hasError = it.isBlank()
                    },
                    label = { Text("Quiz Title *") },
                    placeholder = { Text("e.g. Life Processes Level-1 MCQ") },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("quiz_title_input")
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Description / Topics Covered") },
                    placeholder = { Text("e.g. 20 Questions on Photosynthesis, Respiration") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Subject
                Text("Subject Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                val subjectNames = if (subjects.isNotEmpty()) {
                    subjects.map { it.name }.distinct()
                } else {
                    listOf("Physics", "Chemistry", "Biology")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectNames.forEach { sName ->
                        FilterChip(
                            selected = selectedSubject.equals(sName, ignoreCase = true),
                            onClick = { selectedSubject = sName },
                            label = { Text(sName) }
                        )
                    }
                }

                // File Attachment / Name
                OutlinedTextField(
                    value = filename,
                    onValueChange = { filename = it },
                    label = { Text("Quiz HTML File / Asset Name *") },
                    placeholder = { Text("e.g. mcq_quiz.html or pick file below") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("text/html", "application/json", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Quiz File from Device")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && filename.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            details.trim().ifBlank { null },
                            filename.trim(),
                            fileType,
                            selectedSubject.trim()
                        )
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("save_quiz_button")
            ) {
                Text(if (initialQuiz == null) "Add Quiz" else "Save Quiz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


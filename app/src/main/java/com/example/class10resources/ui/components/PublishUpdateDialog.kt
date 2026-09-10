package com.example.class10resources.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PublishUpdateDialog(
    currentVersionCode: Int,
    currentVersionName: String,
    currentDownloadUrl: String,
    onDismiss: () -> Unit,
    onPublish: (versionCode: Int, versionName: String, title: String, notes: String, downloadUrl: String, forceUpdate: Boolean) -> Unit
) {
    val context = LocalContext.current
    var newVersionCode by remember { mutableIntStateOf(currentVersionCode + 1) }
    var newVersionName by remember { mutableStateOf("v2.2") }
    var updateTitle by remember { mutableStateOf("New Lumio Update Available!") }
    var releaseNotes by remember { mutableStateOf("Added new board notes, Daily Practice Problems (DPPs), practice MCQs, and important PYQs.") }
    var downloadUrl by remember { mutableStateOf(currentDownloadUrl) }
    var isForceUpdate by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("publish_update_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Trigger Update Popup to Students",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• जैसे Play Store/बाकी ऐप्स में होता है: यह पॉपअप केवल उन्हीं छात्रों के फ़ोन पर आएगा जिनके पास पुराना वर्ज़न है!\n• जिन छात्रों ने पहले ही नया ऐप इंस्टॉल कर रखा है, उन्हें यह पॉपअप कभी नहीं दिखेगा।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newVersionName,
                        onValueChange = { newVersionName = it },
                        label = { Text("Version Name") },
                        placeholder = { Text("e.g. 2.1") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = newVersionCode.toString(),
                        onValueChange = { newVersionCode = it.toIntOrNull() ?: (currentVersionCode + 1) },
                        label = { Text("Version Code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = updateTitle,
                    onValueChange = { updateTitle = it },
                    label = { Text("Notification Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = { releaseNotes = it },
                    label = { Text("What's New / Release Notes") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = downloadUrl,
                    onValueChange = { downloadUrl = it },
                    label = { Text("New APK Download Link") },
                    placeholder = { Text("https://... or Google Drive direct link") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Non-intrusive guarantee - never force students
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ Non-intrusive update: Students can update whenever ready or dismiss anytime to continue using the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            var cleanUrl = downloadUrl.trim()
                            if (cleanUrl.contains("drive.google.com/file/d/")) {
                                val fileId = cleanUrl.substringAfter("drive.google.com/file/d/").substringBefore("/")
                                cleanUrl = "https://drive.google.com/uc?export=download&id=$fileId"
                            }
                            onPublish(
                                newVersionCode,
                                newVersionName.trim().removePrefix("v"),
                                updateTitle.trim(),
                                releaseNotes.trim(),
                                cleanUrl,
                                false // Never force update
                            )
                        }
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⚡ Trigger & Send Popup")
                    }
                }
            }
        }
    }
}

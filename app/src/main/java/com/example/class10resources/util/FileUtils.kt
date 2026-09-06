package com.example.class10resources.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object FileUtils {

    /**
     * Copies a PDF or document from a user-selected Uri into the app's internal uploads folder.
     * Returns the relative filename inside uploads/ and file size in bytes.
     */
    fun saveUploadedFile(context: Context, uri: Uri): Pair<String, Long>? {
        return try {
            val uploadsDir = File(context.filesDir, "uploads")
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs()
            }

            var originalName: String? = null
            var fileSize: Long = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        originalName = cursor.getString(nameIndex)
                    }
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val sanitizedBase = (originalName ?: "document.pdf")
                .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val finalFilename = "${System.currentTimeMillis()}_$sanitizedBase"
            val targetFile = File(uploadsDir, finalFilename)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (fileSize <= 0L && targetFile.exists()) {
                fileSize = targetFile.length()
            }

            Pair(finalFilename, fileSize)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Formats bytes to readable string (e.g. 1.2 MB)
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val formatted = DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble()))
        return "$formatted ${units[digitGroups]}"
    }

    /**
     * Resolves the local file if it exists (checks uploads/, cache/, or extracts from assets)
     */
    fun getLocalFile(context: Context, filename: String): File? {
        // 1. Check in uploads
        val uploadedFile = File(context.filesDir, "uploads/$filename")
        if (uploadedFile.exists()) return uploadedFile

        // 2. Direct in files dir
        val directFile = File(context.filesDir, filename)
        if (directFile.exists()) return directFile

        // 3. In cache dir
        val cacheFile = File(context.cacheDir, filename)
        if (cacheFile.exists()) return cacheFile

        // 4. In assets (e.g. notes_2026 or root)
        try {
            val assetPath = if (context.assets.list("notes_2026")?.contains(filename) == true) {
                "notes_2026/$filename"
            } else {
                filename
            }
            context.assets.open(assetPath).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (cacheFile.exists()) return cacheFile
        } catch (ignored: Exception) {
        }

        return null
    }

    /**
     * Opens a resource either as a local PDF or external URL.
     */
    fun openItem(context: Context, link: String?, filename: String?, title: String = "Resource") {
        // First priority: Local PDF if filename provided
        if (!filename.isNullOrBlank()) {
            val localFile = getLocalFile(context, filename)
            if (localFile != null && localFile.exists()) {
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        localFile
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open with PDF Viewer"))
                    return
                } catch (e: Exception) {
                    Toast.makeText(context, "No PDF viewer found. Opening link...", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Second priority: Web or Google Drive Link
        if (!link.isNullOrBlank()) {
            try {
                val webUri = if (!link.startsWith("http://") && !link.startsWith("https://")) {
                    Uri.parse("https://$link")
                } else {
                    Uri.parse(link)
                }
                val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        Toast.makeText(context, "No link or local file available for $title", Toast.LENGTH_SHORT).show()
    }
}

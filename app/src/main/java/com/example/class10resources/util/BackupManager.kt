package com.example.class10resources.util

import android.content.Context
import android.net.Uri
import com.example.class10resources.data.db.AppDatabase
import com.example.class10resources.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    /**
     * Exports all database items to JSON and writes to the selected URI.
     */
    suspend fun exportBackup(
        context: Context,
        destinationUri: Uri,
        database: AppDatabase
    ): Result<String> {
        return try {
            val resources = database.resourceDao().getAllResourcesList()
            val notes = database.note2026Dao().getAllNotesList()
            val dpps = database.dppDao().getAllDppsList()
            val quizzes = database.mcqQuizDao().getAllQuizzesList()
            val subjects = database.subjectDao().getAllSubjectsList()
            val owner = database.ownerInfoDao().getOwnerInfoDirect()

            val rootJson = JSONObject()
            rootJson.put("appName", "Lumio")
            rootJson.put("version", 3)
            rootJson.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            // Subjects
            val subjArray = JSONArray()
            subjects.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("description", item.description)
                obj.put("iconType", item.iconType)
                obj.put("colorHex", item.colorHex)
                obj.put("chapters", item.chapters)
                obj.put("displayOrder", item.displayOrder)
                subjArray.put(obj)
            }
            rootJson.put("subjects", subjArray)

            // Resources
            val resArray = JSONArray()
            resources.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("link", item.link ?: "")
                obj.put("filename", item.filename ?: "")
                obj.put("subject", item.subject)
                obj.put("createdAt", item.createdAt)
                resArray.put(obj)
            }
            rootJson.put("resources", resArray)

            // Notes
            val notesArray = JSONArray()
            notes.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("link", item.link ?: "")
                obj.put("filename", item.filename ?: "")
                obj.put("subject", item.subject)
                obj.put("createdAt", item.createdAt)
                notesArray.put(obj)
            }
            rootJson.put("notes", notesArray)

            // DPPs
            val dppArray = JSONArray()
            dpps.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("title", item.title)
                obj.put("driveLink", item.driveLink)
                obj.put("filename", item.filename ?: "")
                obj.put("subject", item.subject)
                obj.put("createdAt", item.createdAt)
                dppArray.put(obj)
            }
            rootJson.put("dpps", dppArray)

            // Quizzes
            val quizArray = JSONArray()
            quizzes.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("title", item.title)
                obj.put("details", item.details ?: "")
                obj.put("filename", item.filename)
                obj.put("fileType", item.fileType)
                obj.put("subject", item.subject)
                obj.put("createdAt", item.createdAt)
                quizArray.put(obj)
            }
            rootJson.put("quizzes", quizArray)

            // Owner
            if (owner != null) {
                val ownerObj = JSONObject()
                ownerObj.put("name", owner.name)
                ownerObj.put("description", owner.description)
                ownerObj.put("contact", owner.contact)
                ownerObj.put("instagramLink", owner.instagramLink)
                ownerObj.put("mcqLink", owner.mcqLink)
                rootJson.put("owner", ownerObj)
            }

            context.contentResolver.openOutputStream(destinationUri)?.use { outStream ->
                OutputStreamWriter(outStream, Charsets.UTF_8).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            }

            Result.success("Exported ${subjects.size} subjects, ${resources.size} resources, ${notes.size} notes, ${dpps.size} DPPs, and ${quizzes.size} quizzes successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Imports data from a JSON file and updates the Room database.
     */
    suspend fun restoreBackup(
        context: Context,
        sourceUri: Uri,
        database: AppDatabase
    ): Result<String> {
        return try {
            val jsonString = StringBuilder()
            context.contentResolver.openInputStream(sourceUri)?.use { inStream ->
                BufferedReader(InputStreamReader(inStream, Charsets.UTF_8)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        jsonString.append(line)
                        line = reader.readLine()
                    }
                }
            }

            val rootJson = JSONObject(jsonString.toString())

            // Parse Subjects
            val subjList = mutableListOf<SubjectItem>()
            if (rootJson.has("subjects")) {
                val array = rootJson.getJSONArray("subjects")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    subjList.add(
                        SubjectItem(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            name = obj.optString("name", "Subject"),
                            description = obj.optString("description", ""),
                            iconType = obj.optString("iconType", "physics"),
                            colorHex = obj.optString("colorHex", "#2563EB"),
                            chapters = obj.optString("chapters", ""),
                            displayOrder = obj.optInt("displayOrder", 0)
                        )
                    )
                }
            }

            // Parse Resources
            val resList = mutableListOf<ResourceItem>()
            if (rootJson.has("resources")) {
                val array = rootJson.getJSONArray("resources")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    resList.add(
                        ResourceItem(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            name = obj.optString("name", "Resource"),
                            link = obj.optString("link").ifBlank { null },
                            filename = obj.optString("filename").ifBlank { null },
                            subject = obj.optString("subject", "Physics"),
                            createdAt = obj.optString("createdAt", "2026-09-06")
                        )
                    )
                }
            }

            // Parse Notes
            val notesList = mutableListOf<Note2026Item>()
            if (rootJson.has("notes")) {
                val array = rootJson.getJSONArray("notes")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    notesList.add(
                        Note2026Item(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            name = obj.optString("name", "Note"),
                            link = obj.optString("link").ifBlank { null },
                            filename = obj.optString("filename").ifBlank { null },
                            subject = obj.optString("subject", "Physics"),
                            createdAt = obj.optString("createdAt", "2026-09-06")
                        )
                    )
                }
            }

            // Parse DPPs
            val dppList = mutableListOf<DppItem>()
            if (rootJson.has("dpps")) {
                val array = rootJson.getJSONArray("dpps")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    dppList.add(
                        DppItem(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            title = obj.optString("title", "DPP"),
                            driveLink = obj.optString("driveLink", ""),
                            filename = obj.optString("filename").ifBlank { null },
                            subject = obj.optString("subject", "Physics"),
                            createdAt = obj.optString("createdAt", "2026-09-06")
                        )
                    )
                }
            }

            // Parse Quizzes
            val quizList = mutableListOf<McqQuizItem>()
            if (rootJson.has("quizzes")) {
                val array = rootJson.getJSONArray("quizzes")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    quizList.add(
                        McqQuizItem(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            title = obj.optString("title", "Quiz"),
                            details = obj.optString("details").ifBlank { null },
                            filename = obj.optString("filename", ""),
                            fileType = obj.optString("fileType", "json"),
                            subject = obj.optString("subject", "Physics"),
                            createdAt = obj.optString("createdAt", "2026-09-06")
                        )
                    )
                }
            }

            // Replace data in Room database
            if (subjList.isNotEmpty()) {
                database.subjectDao().deleteAll()
                database.subjectDao().insertAll(subjList)
            }

            if (resList.isNotEmpty()) {
                database.resourceDao().deleteAll()
                database.resourceDao().insertAll(resList)
            }

            if (notesList.isNotEmpty()) {
                database.note2026Dao().deleteAll()
                database.note2026Dao().insertAll(notesList)
            }

            if (dppList.isNotEmpty()) {
                database.dppDao().deleteAll()
                database.dppDao().insertAll(dppList)
            }

            if (quizList.isNotEmpty()) {
                database.mcqQuizDao().deleteAll()
                database.mcqQuizDao().insertAll(quizList)
            }

            // Owner
            if (rootJson.has("owner")) {
                val ownerObj = rootJson.getJSONObject("owner")
                val current = database.ownerInfoDao().getOwnerInfoDirect()
                val updated = (current ?: OwnerInfo(
                    name = "Ashish Maurya",
                    description = "",
                    contact = "",
                    photoFilename = "mee.jpeg",
                    instagramLink = "https://www.instagram.com/ashraj7777/",
                    mcqLink = ""
                )).copy(
                    name = ownerObj.optString("name", current?.name ?: "Ashish Maurya"),
                    description = ownerObj.optString("description", current?.description ?: ""),
                    contact = ownerObj.optString("contact", current?.contact ?: ""),
                    instagramLink = ownerObj.optString("instagramLink", current?.instagramLink ?: "https://www.instagram.com/ashraj7777/"),
                    mcqLink = ownerObj.optString("mcqLink", current?.mcqLink ?: "")
                )
                database.ownerInfoDao().insertOrUpdate(updated)
            }

            Result.success("Backup Restored: ${subjList.size} subjects, ${resList.size} resources, ${notesList.size} notes, ${dppList.size} DPPs, ${quizList.size} quizzes!")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

package com.example.class10resources.data.db

import androidx.room.*
import com.example.class10resources.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY id ASC")
    fun getAllResources(): Flow<List<ResourceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<ResourceItem>)

    @Update
    suspend fun updateResource(resource: ResourceItem)

    @Delete
    suspend fun deleteResource(resource: ResourceItem)

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM resources")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM resources ORDER BY id ASC")
    suspend fun getAllResourcesList(): List<ResourceItem>

    @Query("SELECT COUNT(*) FROM resources")
    suspend fun getCount(): Int
}

@Dao
interface Note2026Dao {
    @Query("SELECT * FROM notes_2026 ORDER BY id ASC")
    fun getAllNotes(): Flow<List<Note2026Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note2026Item): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note2026Item>)

    @Update
    suspend fun updateNote(note: Note2026Item)

    @Delete
    suspend fun deleteNote(note: Note2026Item)

    @Query("DELETE FROM notes_2026 WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM notes_2026")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM notes_2026 ORDER BY id ASC")
    suspend fun getAllNotesList(): List<Note2026Item>

    @Query("SELECT COUNT(*) FROM notes_2026")
    suspend fun getCount(): Int
}

@Dao
interface DppDao {
    @Query("SELECT * FROM dpps ORDER BY id DESC")
    fun getAllDpps(): Flow<List<DppItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDpp(dpp: DppItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dpps: List<DppItem>)

    @Update
    suspend fun updateDpp(dpp: DppItem)

    @Delete
    suspend fun deleteDpp(dpp: DppItem)

    @Query("DELETE FROM dpps WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM dpps")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM dpps ORDER BY id DESC")
    suspend fun getAllDppsList(): List<DppItem>

    @Query("SELECT COUNT(*) FROM dpps")
    suspend fun getCount(): Int
}

@Dao
interface McqQuizDao {
    @Query("SELECT * FROM mcq_quizzes ORDER BY id DESC")
    fun getAllQuizzes(): Flow<List<McqQuizItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: McqQuizItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quizzes: List<McqQuizItem>)

    @Update
    suspend fun updateQuiz(quiz: McqQuizItem)

    @Delete
    suspend fun deleteQuiz(quiz: McqQuizItem)

    @Query("DELETE FROM mcq_quizzes WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM mcq_quizzes")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM mcq_quizzes ORDER BY id DESC")
    suspend fun getAllQuizzesList(): List<McqQuizItem>

    @Query("SELECT COUNT(*) FROM mcq_quizzes")
    suspend fun getCount(): Int
}

@Dao
interface OwnerInfoDao {
    @Query("SELECT * FROM owner_info WHERE id = 1")
    fun getOwnerInfo(): Flow<OwnerInfo?>

    @Query("SELECT * FROM owner_info WHERE id = 1")
    suspend fun getOwnerInfoDirect(): OwnerInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(ownerInfo: OwnerInfo)

    @Query("SELECT COUNT(*) FROM owner_info")
    suspend fun getCount(): Int
}

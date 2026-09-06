package com.example.class10resources.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.class10resources.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ResourceItem::class,
        Note2026Item::class,
        DppItem::class,
        McqQuizItem::class,
        OwnerInfo::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resourceDao(): ResourceDao
    abstract fun note2026Dao(): Note2026Dao
    abstract fun dppDao(): DppDao
    abstract fun mcqQuizDao(): McqQuizDao
    abstract fun ownerInfoDao(): OwnerInfoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "class10_resources.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            // Seed Resources
            val initialResources = listOf(
                ResourceItem(1, "Light Notes", "https://drive.google.com/file/d/1OJRbj75KMQ1cmgSxveR6vvjUP-EgUw8L/view?usp=sharing", null, "2025-10-12"),
                ResourceItem(2, "Human Eye Note", "https://drive.google.com/file/d/1LtQ7gv9m_nF_oS6_n_246pACNCMoJJPN/view?usp=sharing", null, "2025-10-12"),
                ResourceItem(3, "Magnetism Notes", "https://drive.google.com/file/d/1USandWSEzuZy1EDyuBB_VTGSr9U0EGqO/view?usp=sharing", null, "2025-10-12"),
                ResourceItem(4, "Electricity Notes", "https://drive.google.com/file/d/1z9m4fUAcNVZz_zbGZxxV6BdJwdHyFFWW/view?usp=drive_link", null, "2025-10-12"),
                ResourceItem(5, "Human EYE PYQ*", "https://drive.google.com/file/d/1rAMLSW9w_joPIPOz4r59LPoCeQGdIAtz/view?usp=sharing", null, "2025-10-12"),
                ResourceItem(7, "Electricity PYQ*", "https://drive.google.com/file/d/1fqxKdj1rpM_I5onU7KG4uVLucXkv0QGA/view?usp=sharing", null, "2025-10-12"),
                ResourceItem(8, "Magnetism PYQ*", null, null, "2025-10-12"),
                ResourceItem(9, "Light PYQ*", null, null, "2025-10-12"),
                ResourceItem(10, "CHAPTER WISE PYQ - SCIENCE", "https://drive.google.com/drive/u/0/folders/1Aej_oKU5_8pAZb2X7x64RvQ7iIQnTzri", null, "2025-10-14")
            )
            database.resourceDao().insertAll(initialResources)

            // Seed Notes 2026
            val initialNotes = listOf(
                Note2026Item(1, "Human Eye Note 2026", null, "note_20260728135408_huma_eye_annotated.pdf", "2026-07-28")
            )
            database.note2026Dao().insertAll(initialNotes)

            // Seed DPPs
            val initialDpps = listOf(
                DppItem(1, "Electricity DPP", "https://drive.google.com/file/d/1IBG9COSOy6eTwmAMVL_vMTh9J8zdgkkw/view?usp=drive_link", null, "2026-04-13")
            )
            database.dppDao().insertAll(initialDpps)

            // Seed MCQ Quizzes
            val initialQuizzes = listOf(
                McqQuizItem(2, "Class 10th electricity level-1", "Basic circuits, Ohm's law, and resistors", "mcq_20260603064028_Class-10th_electricity_level-1.html", "html", "2026-06-01"),
                McqQuizItem(3, "Class 10th electricity level-2", "Equivalent resistance, heating effect, and power", "mcq_20260603064113_electricity_level-2.html", "html", "2026-06-01"),
                McqQuizItem(4, "Human EYE level-1 MCQ", "Structure of human eye, cornea, retina, pupil", "mcq_20260728135056_human_eye_practice_level_-1.html", "html", "2026-07-28"),
                McqQuizItem(5, "Human Eye Practice MCQ Level-2", "Defects of vision, lens corrections, and numericals", "mcq_20260730190527_index.html", "html", "2026-07-28"),
                McqQuizItem(6, "Human Eye Practice MCQ Level-3", "Olympiad / IJSO optics challenge & assertion reasons", "mcq_20260730193855_index.html", "html", "2026-07-30"),
                McqQuizItem(7, "Light- Reflection MCQ Level-1", "Spherical mirrors, focal length, ray diagrams", "mcq_20260801113303_index.html", "html", "2026-08-01"),
                McqQuizItem(8, "Light- Reflection MCQ Level-2", "Mirror formula, magnification & challenging cases", "mcq_20260801114609_index.html", "html", "2026-08-01")
            )
            database.mcqQuizDao().insertAll(initialQuizzes)

            // Seed Owner Info
            val initialOwner = OwnerInfo(
                id = 1,
                name = "Ashish Maurya",
                description = "Class 10 Resource Manager | Pursuing BS in Data Science at IIT Madras | Web Developer & Physics Teacher | Passionate about technology and education",
                contact = "ashraj77777@gmail.com",
                photoFilename = "mee.jpeg",
                telegramLink = "https://t.me/chaipe_charcha",
                instagramLink = "https://www.instagram.com/ashraj77777/",
                mcqLink = "https://www.perplexity.ai/apps/1d5d3a09-a3b4-4c9d-ae02-b5951bb98a80"
            )
            database.ownerInfoDao().insertOrUpdate(initialOwner)
        }
    }
}

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
        SubjectItem::class,
        ResourceItem::class,
        Note2026Item::class,
        DppItem::class,
        McqQuizItem::class,
        PyqItem::class,
        OwnerInfo::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun resourceDao(): ResourceDao
    abstract fun note2026Dao(): Note2026Dao
    abstract fun dppDao(): DppDao
    abstract fun mcqQuizDao(): McqQuizDao
    abstract fun pyqDao(): PyqDao
    abstract fun ownerInfoDao(): OwnerInfoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: AppDatabase? = null
                val created = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "class10_resources.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(scope) { instance ?: INSTANCE })
                    .build()
                instance = created
                INSTANCE = created
                created
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope,
            private val databaseProvider: () -> AppDatabase?
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    try {
                        databaseProvider()?.let { populateInitialData(it) }
                    } catch (e: Exception) {
                        // Handled in repository init as well
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                scope.launch(Dispatchers.IO) {
                    try {
                        databaseProvider()?.let { populateInitialData(it) }
                    } catch (e: Exception) {
                        // Handled in repository init as well
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            // Seed Subjects with full NCERT chapter names
            val initialSubjects = listOf(
                SubjectItem(
                    id = 1,
                    name = "Physics",
                    description = "Light, Human Eye, Electricity, Magnetism & Natural Phenomena",
                    iconType = "physics",
                    colorHex = "#2563EB",
                    chapters = "Light - Reflection & Refraction, The Human Eye & Colourful World, Electricity, Magnetic Effects of Electric Current",
                    displayOrder = 1
                ),
                SubjectItem(
                    id = 2,
                    name = "Chemistry",
                    description = "Chemical Reactions, Acids & Bases, Metals & Carbon Compounds",
                    iconType = "chemistry",
                    colorHex = "#D97706",
                    chapters = "Chemical Reactions & Equations, Acids, Bases & Salts, Metals & Non-Metals, Carbon & Its Compounds",
                    displayOrder = 2
                ),
                SubjectItem(
                    id = 3,
                    name = "Biology",
                    description = "Life Processes, Control & Coordination, Reproduction & Heredity",
                    iconType = "biology",
                    colorHex = "#059669",
                    chapters = "Life Processes, Control & Coordination, How do Organisms Reproduce?, Heredity & Evolution, Our Environment",
                    displayOrder = 3
                )
            )
            database.subjectDao().insertAll(initialSubjects)

            // Seed Notes (Only Physics - Chemistry and Biology are left for user self-upload)
            val initialNotes = listOf(
                Note2026Item(1, "Human Eye Note 2026 (Annotated)", null, "note_20260728135408_huma_eye_annotated.pdf", "Physics", "2026-07-28"),
                Note2026Item(2, "Electricity Complete Notes", "https://drive.google.com/file/d/1z9m4fUAcNVZz_zbGZxxV6BdJwdHyFFWW/view?usp=drive_link", null, "Physics", "2026-07-29"),
                Note2026Item(3, "Light - Reflection & Refraction Notes", "https://drive.google.com/file/d/1OJRbj75KMQ1cmgSxveR6vvjUP-EgUw8L/view?usp=sharing", null, "Physics", "2026-07-29"),
                Note2026Item(4, "Human Eye & Colourful World Notes", "https://drive.google.com/file/d/1LtQ7gv9m_nF_oS6_n_246pACNCMoJJPN/view?usp=sharing", null, "Physics", "2026-07-29"),
                Note2026Item(5, "Magnetic Effects of Electric Current Notes", "https://drive.google.com/file/d/1USandWSEzuZy1EDyuBB_VTGSr9U0EGqO/view?usp=sharing", null, "Physics", "2026-07-29")
            )
            database.note2026Dao().insertAll(initialNotes)

            // Seed DPPs (Only Physics)
            val initialDpps = listOf(
                DppItem(1, "Electricity DPP 01", "https://drive.google.com/file/d/1IBG9COSOy6eTwmAMVL_vMTh9J8zdgkkw/view?usp=drive_link", null, "Physics", "2026-04-13"),
                DppItem(2, "Light Reflection & Refraction DPP", "https://drive.google.com/drive/u/0/folders/1Aej_oKU5_8pAZb2X7x64RvQ7iIQnTzri", null, "Physics", "2026-04-15")
            )
            database.dppDao().insertAll(initialDpps)

            // Seed Previous Year Questions (PYQs)
            val initialPyqs = listOf(
                PyqItem(
                    id = 1,
                    title = "The Human Eye & Colourful World - CBSE 2024 PYQ",
                    year = "2024",
                    link = "https://drive.google.com/file/d/1rAMLSW9w_joPIPOz4r59LPoCeQGdIAtz/view?usp=sharing",
                    filename = null,
                    subject = "Physics",
                    chapter = "The Human Eye & Colourful World",
                    createdAt = "2026-07-28"
                ),
                PyqItem(
                    id = 2,
                    title = "Electricity - CBSE 2024 Board Questions & Answers",
                    year = "2024",
                    link = "https://drive.google.com/file/d/1fqxKdj1rpM_I5onU7KG4uVLucXkv0QGA/view?usp=sharing",
                    filename = null,
                    subject = "Physics",
                    chapter = "Electricity",
                    createdAt = "2026-07-28"
                ),
                PyqItem(
                    id = 3,
                    title = "Magnetic Effects of Electric Current - CBSE 2023 PYQ",
                    year = "2023",
                    link = "https://drive.google.com/drive/u/0/folders/1Aej_oKU5_8pAZb2X7x64RvQ7iIQnTzri",
                    filename = null,
                    subject = "Physics",
                    chapter = "Magnetic Effects of Electric Current",
                    createdAt = "2026-07-28"
                ),
                PyqItem(
                    id = 4,
                    title = "Light Reflection & Refraction - CBSE 2024 PYQ",
                    year = "2024",
                    link = "https://drive.google.com/drive/u/0/folders/1Aej_oKU5_8pAZb2X7x64RvQ7iIQnTzri",
                    filename = null,
                    subject = "Physics",
                    chapter = "Light - Reflection & Refraction",
                    createdAt = "2026-07-28"
                ),
                PyqItem(
                    id = 5,
                    title = "Science Chapter-Wise Board PYQ Master Bank",
                    year = "2024",
                    link = "https://drive.google.com/drive/u/0/folders/1Aej_oKU5_8pAZb2X7x64RvQ7iIQnTzri",
                    filename = null,
                    subject = "Physics",
                    chapter = null,
                    createdAt = "2026-07-28"
                )
            )
            database.pyqDao().insertAll(initialPyqs)

            // Seed MCQ Quizzes
            val initialQuizzes = listOf(
                McqQuizItem(2, "Class 10th electricity level-1", "Basic circuits, Ohm's law, and resistors", "mcq_20260603064028_Class-10th_electricity_level-1.html", "html", "Physics", "2026-06-01"),
                McqQuizItem(3, "Class 10th electricity level-2", "Equivalent resistance, heating effect, and power", "mcq_20260603064113_electricity_level-2.html", "html", "Physics", "2026-06-01"),
                McqQuizItem(4, "Human EYE level-1 MCQ", "Structure of human eye, cornea, retina, pupil", "mcq_20260728135056_human_eye_practice_level_-1.html", "html", "Physics", "2026-07-28"),
                McqQuizItem(5, "Human Eye Practice MCQ Level-2", "Defects of vision, lens corrections, and numericals", "mcq_20260730190527_index.html", "html", "Physics", "2026-07-28"),
                McqQuizItem(6, "Human Eye Practice MCQ Level-3", "Olympiad / IJSO optics challenge & assertion reasons", "mcq_20260730193855_index.html", "html", "Physics", "2026-07-30"),
                McqQuizItem(7, "Light- Reflection MCQ Level-1", "Spherical mirrors, focal length, ray diagrams", "mcq_20260801113303_index.html", "html", "Physics", "2026-08-01"),
                McqQuizItem(8, "Light- Reflection MCQ Level-2", "Mirror formula, magnification & challenging cases", "mcq_20260801114609_index.html", "html", "Physics", "2026-08-01")
            )
            database.mcqQuizDao().insertAll(initialQuizzes)

            // Seed Owner Info
            val initialOwner = OwnerInfo(
                id = 1,
                name = "Ashish Maurya",
                description = "Physics Teacher & Educator | Pursuing BS in Data Science at IIT Madras | Full Stack & Web Developer | Dedicated to making Class 10 concepts intuitive, rigorous, and accessible.",
                contact = "ashraj77777@gmail.com",
                photoFilename = "mee.jpeg",
                instagramLink = "https://www.instagram.com/ashraj7777/",
                mcqLink = "https://www.perplexity.ai/apps/1d5d3a09-a3b4-4c9d-ae02-b5951bb98a80"
            )
            database.ownerInfoDao().insertOrUpdate(initialOwner)
        }
    }
}

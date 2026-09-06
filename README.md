# VidyaSetu 10 (विद्यासेतु 10) - Version 2.0

A modern, offline-first native Android educational platform built for Class 10 students and educators. Developed with Kotlin and Jetpack Compose, **VidyaSetu 10** provides organized chapter resources, revision notes, daily practice problems (DPP), interactive offline MCQ tests, and full data management.

---

## 📱 What's New in Version 2.0 (v2.0)

1. **Unique Branding & Identity**:
   - Rebranded as **VidyaSetu 10** with custom vector logo and adaptive launcher icon.
   - Elegant dark/light Material Design 3 theme with smooth animated transitions.
2. **Direct PDF Upload & Drive Link Support**:
   - Teachers can either provide a Google Drive link **OR** upload PDF files directly from phone storage.
   - Uploaded PDFs are stored in the app's internal sandboxed directory and opened seamlessly via Android's native `FileProvider`.
3. **Full Backup & Restore System**:
   - Export all database tables (Resources, Notes, DPPs, MCQs, Teacher profile) to a portable JSON file.
   - Restore database anytime from any saved JSON backup file.
   - One-tap reset to original default curriculum.
4. **Secure Admin Login**:
   - Fixed password visibility during admin login; added a toggleable show/hide eye icon. Default admin password is `4129`.
5. **Database Upgrade to Schema v2**:
   - Enhanced Room database with multi-format attachments and complete DAO controls.

---

## 🗄️ Database Architecture & Usage (डेटाबेस का पूरा विवरण)

VidyaSetu 10 uses **Android Jetpack Room** over an embedded **SQLite** engine. Below is a detailed breakdown of how and why the database is used:

### 1. Which Database Engine is Used?
- **Core Engine:** **SQLite** (the industry-standard ACID-compliant relational SQL engine built into Android OS).
- **Abstraction Layer:** **Android Jetpack Room (`androidx.room` v2.7.0)** with **Kotlin Symbol Processing (KSP)**.

### 2. Why Room / SQLite was Chosen?
- **100% Offline Availability (बिना इंटरनेट काम करता है):** Students can access all saved notes, DPPs, and chapter materials without requiring active internet access or cloud server dependency.
- **Reactive Flow Data Streams:** Room DAOs return Kotlin Coroutine `Flow<List<T>>`. Whenever an admin adds, edits, or deletes an item, the Jetpack Compose UI automatically updates in real time without refreshing.
- **Data Integrity & Type Safety:** SQL queries and entity structures are checked and validated at compile time, eliminating runtime SQL injection and syntax crashes.
- **Privacy & Security:** All data resides exclusively on the user's device in protected internal storage (`/data/data/com.aistudio.vidyasetu10.app/databases/`).

### 3. Database Schema & Tables (टेबल्स की संरचना)

The database (`AppDatabase`, Version 2) contains 5 relational tables:

| Table Name | Entity Class | Purpose | Schema / Fields |
|---|---|---|---|
| `resources` | `ResourceItem` | Study notes, formula sheets, chapter materials | `id` (PK, Auto), `name` (TEXT), `link` (TEXT?), `filename` (TEXT?), `createdAt` (TEXT) |
| `notes_2026` | `Note2026Item` | Dedicated 2026 batch annotated revision notes | `id` (PK, Auto), `name` (TEXT), `link` (TEXT?), `filename` (TEXT?), `createdAt` (TEXT) |
| `dpps` | `DppItem` | Daily Practice Problem sets & assignment sheets | `id` (PK, Auto), `title` (TEXT), `driveLink` (TEXT), `filename` (TEXT?), `createdAt` (TEXT) |
| `mcq_quizzes` | `McqQuizItem` | Interactive practice quizzes with answers & explanations | `id` (PK, Auto), `title` (TEXT), `details` (TEXT?), `filename` (TEXT), `fileType` (TEXT), `createdAt` (TEXT) |
| `owner_info` | `OwnerInfo` | Teacher profile, bio, and social contact handles | `id` (PK), `name` (TEXT), `title` (TEXT), `tagline` (TEXT), `bio` (TEXT), `contactEmail` (TEXT), `telegramHandle` (TEXT), `instagramHandle` (TEXT), `updatedAt` (TEXT) |

### 4. Direct PDF & File Storage System
- When an admin selects a PDF file from the device via the system file picker (`ActivityResultContracts.OpenDocument`), the file is copied safely to the app's internal sandbox:
  `context.filesDir/uploads/<timestamp>_<filename>.pdf`
- The file path/name is recorded in the Room database (`filename` column).
- When opened, the app uses an Android `FileProvider` (`com.example.class10resources.fileprovider`) to grant secure, temporary read permission to the device's PDF viewer.

### 5. Backup & Restore System (JSON Serialization)
- **Export Backup:** Serializes all 5 Room tables into a structured JSON file via Android's Storage Access Framework (`ActivityResultContracts.CreateDocument`).
- **Restore Backup:** Reads any exported VidyaSetu JSON file, validates schema integrity, clears existing tables, and atomically inserts the restored entities.
- **Factory Reset:** Re-seeds the database back to the original curriculum and teacher profile.

---

## 📚 Core Modules

1. **Resources Tab**: Search, filter by category (Notes, PYQ, Chapters), and access materials via Drive or local PDF.
2. **2026 Notes Tab**: Syllabus notes with direct PDF viewer launcher.
3. **Daily Practice (DPP) Tab**: Practice question sets with sequence badges and links.
4. **Practice MCQs Tab**: 7 offline interactive quizzes covering Electricity, Human Eye, and Light Reflection with instant scoring, timers, and step-by-step solutions.
5. **Teacher Profile & Data Tab**: Ashish Maurya's profile, direct email/Telegram/Instagram links, admin login/logout, and the **Backup & Restore System**.

---

## 🔐 Admin Access
- **Admin Password**: `4129`
- Allows adding, editing, and deleting resources, DPPs, and notes.
- Password input includes an eye toggle to reveal/hide characters safely.

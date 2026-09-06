# Lumio (ल्यूमियो) - Class 10 Learning Suite

A modern, minimal, offline-first native Android educational platform built for Class 10 students and educators. Developed with Kotlin and Jetpack Compose, **Lumio** provides organized chapter resources, 2026 revision notes, daily practice problems (DPP), interactive offline MCQ tests with timer and instant feedback, direct PDF uploads, and a comprehensive JSON backup/restore engine.

---

## ✦ Brand Identity & Logo Design

- **App Name**: **Lumio**
- **Icon Symbol**: Stylized, modern rounded flat **"L"** combined with a radiant, soft-glowing **light beam & 4-point spark (`✦`)**.
- **Visual Style**: Minimal, flat geometry with smooth rounded terminals and subtle, translucent luminescence.
- **Color Palette**:
  - Background: Deep midnight navy / indigo canvas (`#090D1E`, `#121833`, `#1B234B`).
  - Letterform: Crisp, modern rounded pure white (`#FFFFFF`).
  - Glow & Spark: Warm golden-yellow flare (`#FACC15`, `#FDE047`) with a radiant warm-white core (`#FFFFFF`).
- **Wordmark**: Clean, modern rounded sans-serif wordmark with balanced letter spacing.
- **Design Persona**: Intelligent, youthful, and premium — avoiding childish cartoonish clip-art in favor of high-end, clean architectural minimalism.

---

## 📱 Core Features

1. **Intelligent Curriculum Organization**:
   - Filter materials by All, Notes, PYQ, and Chapters with search indexing.
2. **Direct PDF Upload & Drive Link Support**:
   - Teachers can either provide a Google Drive link **OR** upload PDF files directly from phone storage.
   - Uploaded PDFs are stored in the app's internal sandboxed directory and opened seamlessly via Android's native `FileProvider`.
3. **Full Backup & Restore System**:
   - Export all database tables (Resources, Notes, DPPs, MCQs, Teacher profile) to a portable JSON file.
   - Restore database anytime from any saved JSON backup file.
   - One-tap reset to original default curriculum.
4. **Secure Admin Login**:
   - Masked password input with toggleable show/hide eye icon. Default admin password is `4129`.
5. **Interactive Offline MCQ Quizzes**:
   - 7 full chapter tests covering Electricity, Light Reflection/Refraction, and Human Eye with immediate scoring and step-by-step explanations.

---

## 🗄️ Database Architecture & Usage (डेटाबेस का पूरा विवरण)

Lumio uses **Android Jetpack Room** over an embedded **SQLite** engine:


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

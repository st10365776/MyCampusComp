# MyCampusComp 🎓

**MyCampusComp** is a modern, feature-rich native Android application designed to simplify student life. From tracking daily lectures and assignment deadlines to consulting an AI study assistant, calculating APS scores, trading on a student marketplace, and earning gamification badges — MyCampusComp serves as an all-in-one digital companion on campus.

Built with **Kotlin**, **Jetpack Architecture Components (MVVM)**, **Firebase**, and **Room Database**, the app is optimized for both online real-time sync and seamless offline usage.

---

## 🌟 Key Features

* **Smart Dashboard**: Instant snapshot of your daily classes, next upcoming lecture across the week, pending assignment count, current study streak, and APS score.
* **Timetable & Class Schedule**: Filterable weekly timetable with offline caching and Firestore sync.
* **Assignments Tracker**: Manage pending and completed assignments with due date urgency indicators and gamification point rewards.
* **AI Study Assistant**: Interactive AI tutor powered by Retrofit to answer academic questions on the fly.
* **Gamification & Badges System**:
  * **Levels & Points**: Earn points by using the AI tutor, completing assignments, and saving APS calculations.
  * **Badges**: Unlock achievements (*Point Collector*, *Task Master*, *AI Scholar*, *APS Planner*, *Rising Star*, *Master Scholar*).
  * **Daily Study Streak**: Automatic streak tracking that keeps you motivated every day.
  * **Offline Persistence**: Gamification data is saved locally and synced to the cloud seamlessly.
* **APS Calculator**: Calculate your Admission Point Score / GPA across modules and save it directly to your profile.
* **Student Marketplace**: Buy, sell, and manage textbook or gear listings with fellow campus students.
* **Attendance & QR Scanner**: On-device barcode scanner powered by CameraX and ML Kit for class check-ins.
* **Customization & Dark Theme**: Full profile editor (name and custom profile image URL) plus System/Light/Dark theme support.

---

## 🚀 How to Register & Log In

### 1. Account Registration
1. Open the app. On the initial login screen, tap **Register** at the bottom (or select *Neutral / Register* from the prompt).
2. Enter your **Full Name**, **Email Address**, and a secure **Password**.
3. Tap **Register**.
4. *Alternative*: You can also tap **Continue with Google** on the registration page to quickly sign up with your Google account.

### 2. Logging In
1. Enter your registered **Email Address** and **Password** on the main login screen.
2. Tap **Login** to access your dashboard.
3. *Alternative*: Tap **Sign in with Google** for one-tap passwordless authentication.

### 3. Forgot Password?
* Tap **Forgot Password?** on the login screen, enter your email address, and tap **Send Link** to receive a password reset link in your inbox.

---

## 🗺️ Navigating the App

### Bottom Navigation Bar
The bottom bar allows instant access to the core daily features:
* **Home (Dashboard)**: Overview of your daily schedule, upcoming assignments, study streak, and academic stats.
* **Timetable**: View and manage your weekly class schedule (Monday through Friday).
* **AI Tutor**: Ask academic questions and receive instant answers from the AI assistant.
* **Assignments**: View due assignments, add new tasks, or mark assignments as complete.
* **More**: Opens the side navigation drawer for additional campus tools.

### Side Navigation Drawer
Access the drawer by tapping the top-left menu icon or selecting **More** from the bottom bar:
* **Marketplace**: Browse student items for sale, view product details, contact sellers, or create your own listing under **My Listings**.
* **Campus Map**: Interactive map navigation to locate lecture halls and campus buildings.
* **Attendance**: View attendance records and check-in history.
* **APS Calculator**: Calculate your module scores/GPA and save results to earn gamification points.
* **Gamification & Badges**: View your current level, point progress bar, unlocked achievements, and daily streak.
* **QR Scanner**: Open the camera to scan attendance or event QR codes.
* **Notifications**: View campus updates and assignment reminders.
* **Settings**: Change app themes (Light, Dark, or System Default), edit your profile, or log out.

### Profile Customization
1. Open the drawer, tap **Settings**, then select **Edit Profile**.
2. Enter your **Full Name** and paste a **Profile Image URL**.
3. Tap **Save Changes**. Your profile photo and name update across the dashboard, drawer header, and database instantly.

---

## 🛠️ Tech Stack & Architecture

* **Language**: Kotlin
* **Architecture Pattern**: MVVM (Model-View-ViewModel) + Repository Pattern
* **Local Storage**: 
  * **Room Database**: Local caching for timetable classes and assignments.
  * **SharedPreferences**: Persistent offline cache for user profile, study streak, points, level, and badges.
* **Cloud & Backend**:
  * **Firebase Authentication**: Email/Password and Google Sign-In.
  * **Cloud Firestore**: Real-time cloud database for user profiles, marketplace items, timetable, and assignments.
* **Networking**: Retrofit 2 + Gson Converter for AI assistant API integration.
* **Image Loading**: Coil (Coroutines Image Loader).
* **UI Components**: Material Design 3, ConstraintLayout, ViewPager2, DrawerLayout, CameraX, and ML Kit Barcode Scanning.

---

## 💻 Setup & Installation

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer
* **JDK**: Version 11 or 17
* **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 37

### Building the Project
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/MyCampusComp.git
   cd MyCampusComp
   ```
2. **Configure Firebase**:
   * Add your `google-services.json` file inside the `app/` directory.
   * Enable **Email/Password** and **Google Sign-In** in the Firebase Authentication console.
   * Provision a Cloud Firestore database.
3. **Build & Run**:
   * Open the project in Android Studio.
   * Perform a Gradle Sync (`File > Sync Project with Gradle Files`).
   * Run the app on an Android Emulator or connected physical device.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

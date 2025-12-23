# Sewage Management App

## Overview
The **Sewage Management App** is an Android application designed to streamline the process of reporting and tracking sewage-related issues. Users can register, submit complaints with details (and potentially location/images), view the status of their past complaints, and manage their profile.

## Features
*   **User Authentication**: Secure Login and Registration using Firebase Authentication.
*   **Submit Complaint**: Users can report issues directly through the app.
*   **Complaint History**: View a list of previously submitted complaints and their current status.
*   **Dashboard**: A central hub for accessing app features.
*   **Profile Management**: View and update user details.
*   **Location Services**: (Planned/Implemented) Google Maps integration for pinpointing issue locations.

## Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Android XML Views (with ViewBinding)
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Backend / Database**:
    *   Firebase Authentication
    *   Firebase Firestore (NoSQL Database)
    *   Firebase Storage (for media)
*   **Asynchronous Processing**: Kotlin Coroutines & Flow
*   **Dependency Injection**: Manual (Constructor Injection / Factory)
*   **Maps**: Google Maps SDK for Android

## Project Structure
The project follows a Clean Architecture / MVVM structure:
```
com.example.sewagemanagement
├── data
│   ├── model       # Data classes (User, Complaint)
│   └── repository  # Data access logic (ComplaintRepository)
├── ui
│   ├── auth        # Login & Register activities/viewmodels
│   ├── complaint   # Complaint submission & history
│   ├── dashboard   # Main dashboard screen
│   └── profile     # User profile screen
└── utils           # Helper classes (Resource, Constants)
```

## Setup Instructions

### Prerequisites
*   Android Studio (latest version recommended)
*   JDK 11 or higher
*   A Firebase Project

### Installation
1.  **Clone the repository**:
    ```bash
    git clone <repository_url>
    ```
2.  **Firebase Setup**:
    *   Create a project in the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android App to the project with package name: `com.example.sewagemanagement`.
    *   Download the `google-services.json` file.
    *   Place `google-services.json` in the `app/` directory.
    *   Enable **Authentication** (Email/Password).
    *   Enable **Firestore Database**.
3.  **Google Maps Setup**:
    *   Get an API Key from the Google Cloud Console with "Maps SDK for Android" enabled.
    *   Open `app/src/main/AndroidManifest.xml`.
    *   Replace `YOUR_API_KEY_HERE` with your actual API key.
4.  **Build and Run**:
    *   Sync Gradle in Android Studio.
    *   Run the app on an Emulator or Physical Device.

## Permissions
The app requires the following permissions:
*   `INTERNET`: To communicate with Firebase.
*   `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: To fetch the location for complaints.

## Notes
*   Ensure your emulator has Google Play Services installed if you are testing Maps or Firebase features.

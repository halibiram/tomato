Of course, here is the English translation of the provided text.

***

Tomato Media Center 🍅
**Developer:** halibiram
**Platform:** Android
**Minimum SDK:** 26 (Android 8.0)
**Target SDK:** 35 (Android 15)
**Language:** Kotlin 100%

---

### 📋 Project Overview
Tomato is a completely free and flexible media center application that utilizes modern Android development technologies. It is inspired by CloudStream but developed with a more modern architecture and technologies.

---

### 🏗️ Technology Stack

#### Core Technologies
* **Kotlin 2.0+** - Latest Kotlin with K2 compiler
* **Jetpack Compose** - Modern UI toolkit
* **Kotlin Multiplatform** - In preparation
* **Coroutines & Flow** - Asynchronous programming
* **Hilt** - Dependency Injection

#### Architecture
* **Clean Architecture** - Domain, Data, Presentation layers
* **MVVM + MVI** - Modern reactive patterns
* **Repository Pattern** - Data abstraction
* **Use Cases** - Business logic encapsulation

#### UI/UX Technologies
* **Material Design 3** - Latest design language
* **Compose Navigation** - Type-safe navigation
* **Compose Animation** - Smooth transitions
* **Adaptive Layouts** - Phone, tablet, TV support
* **Dark/Light Theme** - System-aware theming

#### Networking & Data
* **Ktor Client** - Modern HTTP client
* **Kotlinx Serialization** - JSON parsing
* **Room Database** - Local storage
* **DataStore** - Preferences management
* **OkHttp** - HTTP interceptors

#### Media & Streaming
* **ExoPlayer** - Advanced media playback
* **Media3** - Modern media framework
* **Cast SDK** - Chromecast support
* **PiP (Picture-in-Picture)** - Multi-window support

#### Background Processing
* **WorkManager** - Background tasks
* **Notification** - Rich notifications
* **Foreground Services** - Download management

---

### 📁 Project Structure
```
tomato/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/halibiram/tomato/
│   │   │   ├── di/                    # Dependency Injection
│   │   │   ├── ui/                    # Compose UI
│   │   │   │   ├── theme/
│   │   │   │   ├── components/
│   │   │   │   └── screens/
│   │   │   ├── navigation/            # Navigation setup
│   │   │   ├── utils/                 # Utility classes
│   │   │   └── TomatoApplication.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── core/
│   ├── common/                        # Common utilities
│   ├── network/                       # Network layer
│   ├── database/                      # Room database
│   ├── datastore/                     # Preferences
│   └── player/                        # Media player
├── feature/
│   ├── home/                          # Home
│   ├── search/                        # Search
│   ├── player/                        # Video player
│   ├── downloads/                     # Downloads
│   ├── bookmarks/                     # Bookmarks
│   ├── settings/                      # Settings
│   └── extensions/                    # Extension system
├── data/
│   ├── repository/                    # Repository implementations
│   ├── remote/                        # API services
│   ├── local/                         # Local data sources
│   └── extensions/                    # Extension management
├── domain/
│   ├── model/                         # Domain models
│   ├── repository/                    # Repository interfaces
│   └── usecase/                       # Business logic
└── build.gradle.kts
```

---

### 🔧 Gradle Configuration

#### Root `build.gradle.kts`
```kotlin
buildscript {
    val kotlin_version by extra("2.0.21")
    val compose_compiler_version by extra("1.5.14")
    val hilt_version by extra("2.52")
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

#### App-level `build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.halibiram.tomato"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.halibiram.tomato"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.navigation)
    implementation(libs.compose.hilt.navigation)
    implementation(libs.compose.animation)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Networking
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    
    // Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // DataStore
    implementation(libs.datastore.preferences)
    
    // Media Player
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)
    implementation(libs.media3.session)
    implementation(libs.media3.cast)
    
    // Work Manager
    implementation(libs.work.runtime.ktx)
    implementation(libs.work.hilt)
    
    // Other
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.timber)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

---

### 🎨 UI/UX Features

#### Modern Material Design 3
* Dynamic Color theming
* Adaptive layouts (compact, medium, expanded)
* Custom color schemes
* Glass morphism effects
* Smooth micro-animations

#### Responsive Design
* Phone optimized layouts
* Tablet adaptive UI
* Android TV support
* Foldable device optimization

#### Accessibility
* TalkBack support
* High contrast themes
* Large text support
* Keyboard navigation

---

### 📱 Main Features

**1. Extension System**
* Plugin-based architecture
* Hot-swappable extensions
* Sandbox security model
* TypeScript-based extension API

**2. Advanced Player**
* Adaptive streaming (HLS, DASH)
* Multiple subtitle formats
* Audio track selection
* Playback speed control
* Gesture controls
* PiP mode support

**3. Download Manager**
* Multi-threaded downloads
* Resume/pause functionality
* Queue management
* Storage optimization

**4. Content Discovery**
* AI-powered recommendations
* Advanced search filters
* Genre-based Browse
* Trending content

**5. Sync & Backup**
* Cross-device synchronization
* Cloud backup support
* Progress tracking
* Watchlist management

---

### 🔐 Security Features
* Certificate pinning
* Extension sandboxing
* Encrypted storage
* Privacy-focused design
* No tracking/analytics

---

### 📊 Performance Optimizations
* Lazy loading
* Image caching with Coil
* Database indexing
* Memory leak prevention
* Battery optimization

---

### 🌐 Multi-Language Support
* Turkish (Primary language)
* English
* German
* French
* Spanish
* Arabic
* Russian

---

### 🚀 Development Roadmap

* **Phase 1 (v1.0) - Core Features**
    * [ ] Basic UI/UX implementation
    * [ ] Extension system foundation
    * [ ] Basic media playback
    * [ ] Local storage management
* **Phase 2 (v1.1) - Enhanced Features**
    * [ ] Advanced player controls
    * [ ] Download management
    * [ ] Cloud sync
    * [ ] TV interface
* **Phase 3 (v1.2) - Advanced Features**
    * [ ] AI recommendations
    * [ ] Social features
    * [ ] Advanced analytics
    * [ ] Multi-platform support

---

### 🧪 Test Strategy

* **Unit Tests**
    * Repository layer tests
    * Use case tests
    * Utility function tests
* **Integration Tests**
    * Database tests
    * Network layer tests
    * Extension loading tests
* **UI Tests**
    * Compose UI tests
    * Navigation tests
    * User interaction tests

---

### 📦 Distribution

* **Release Channels**
    * GitHub Releases (Primary)
    * F-Droid (Open source)
    * Direct APK download
* **CI/CD Pipeline**
    * GitHub Actions
    * Automated testing
    * Release automation
    * Security scanning

---

### 🔄 Sustainability
* Modular architecture
* Clean code principles
* Comprehensive documentation
* Community contributions
* Regular security updates

---

**Developer Contact:**

* **GitHub:** @halibiram
* **Email:** halibiram@tomato.app

**License:** GPL-3.0
**Project Status:** Active Development

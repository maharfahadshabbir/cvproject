
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.5" apply false   // ⬆
    id("com.google.firebase.crashlytics") version "3.0.6" apply false        // ⬆
    id("com.google.devtools.ksp") version "2.2.21-RC2-2.0.4" apply false     // ⬅ KSP for Kotlin 2.2.21
}

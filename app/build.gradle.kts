plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")                  // ✅ correct order (before Hilt)
    id("com.google.dagger.hilt.android")           // ✅ use official plugin ID
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
}


android {
    namespace = "com.example.cvmaker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cvmaker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }


    // To avoid splitting languages (by default it will split language in bundle and user will only be able to download app with language of his/her region.)
    bundle {
        language {
            enableSplit = false
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation (latest)
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")          // ⬆ :contentReference[oaicite:4]{index=4}
    // --------------------------------------------------------
    // 🧩 Hilt (Dagger) + KSP
    // --------------------------------------------------------
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")

    // Optional — Hilt extensions for WorkManager / Navigation
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Lifecycle (latest stable 2.9.3)
    implementation("androidx.lifecycle:lifecycle-process:2.9.3")            // ⬆
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.3")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.9.3")       // :contentReference[oaicite:6]{index=6}

    // Room — KSP, not kapt
    implementation("androidx.room:room-runtime:2.7.2")                      // ⬆
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")                                // :contentReference[oaicite:7]{index=7}

    // Retrofit / OkHttp (current stable)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")                 // ⬆ :contentReference[oaicite:8]{index=8}
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.2.1")        // ⬆ :contentReference[oaicite:9]{index=9}

    // Glide — switch to KSP (see note below)
    implementation("com.github.bumptech.glide:glide:4.16.0")                // or 5.x if you want previews
    ksp("com.github.bumptech.glide:ksp:5.0.5")                               // ⬆ KSP artifact :contentReference[oaicite:10]{index=10}
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // UI utils
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.tbuonomo:dotsindicator:5.1.0")

    // SDP
    implementation("com.intuit.sdp:sdp-android:1.1.1")                      // ⬆ latest published :contentReference[oaicite:11]{index=11}

    // In-app updates
    implementation("com.google.android.play:app-update-ktx:2.1.0")          // still current listing :contentReference[oaicite:12]{index=12}

    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    // Rich editor (no change)
    implementation("jp.wasabeef:richeditor-android:2.0.0")
}

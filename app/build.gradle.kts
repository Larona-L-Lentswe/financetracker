plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.financetracker"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.financetracker"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // AndroidX Core Libraries
    implementation(libs.appcompat.v151)
    implementation(libs.core.ktx)
    implementation(libs.constraintlayout.v214)
    implementation(libs.legacy.support.v4)

    // Material Design
    implementation(libs.material.v170)

    // RecyclerView and CardView
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Lifecycle components
    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Firebase Core dependencies
    implementation(platform(libs.firebase.bom.v3223))
    implementation(libs.google.firebase.analytics)
    implementation(libs.google.firebase.database)
    implementation(libs.google.firebase.auth)

    // Firebase Authentication methods
    implementation(libs.com.google.firebase.firebase.auth)
    implementation(libs.play.services.auth) // Google Sign-In

    // Firebase UI Auth (simplifies authentication flows)
    implementation(libs.firebase.ui.auth.v802)

    // Facebook Login (required by FirebaseUI)
    implementation(libs.facebook.android.sdk)

    // Phone number authentication
    implementation(libs.com.google.firebase.firebase.auth2)
    implementation(libs.play.services.safetynet) // For reCAPTCHA verification

    // Apple Sign-In (for Android)
    implementation(libs.play.services.auth.v2060)
    implementation(libs.play.services.auth.api.phone)

    // JSON parsing
    implementation(libs.json)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v114)
    androidTestImplementation(libs.espresso.core.v350)
}
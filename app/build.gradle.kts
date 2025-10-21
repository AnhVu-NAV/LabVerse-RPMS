import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.prm392.g5.labverse"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prm392.g5.labverse"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Đọc local.properties **ngay trong defaultConfig**
        val props = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            props.load(localFile.inputStream())
        }
        val webClientId = props.getProperty("WEB_CLIENT_ID", "")
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true   // <--- bật tính năng BuildConfig
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

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")

    implementation("androidx.credentials:credentials:1.3.0") // Credential Manager
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0") // GIS mới
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // ViewPager2 and TabLayout
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
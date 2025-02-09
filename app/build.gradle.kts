plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.fahadmalik5509.playbox"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fahadmalik5509.playbox"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    implementation(libs.gifDrawable)
    implementation(libs.lottie)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

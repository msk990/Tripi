plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.tripi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tripi"
        minSdk = 27
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
    androidResources {
        noCompress.add(".tflite") // For all .tflite files
        // If your model is in assets/models/1.tflite, the above line is sufficient.
        // You could also be more specific if needed, but not usually necessary for just .tflite:
        // noCompress.add("models/1.tflite") // If you only wanted to target this specific file path in assets
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation (libs.androidx.camera.extensions)
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("com.google.mlkit:common:18.11.0")
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.robolectric)
    testImplementation (libs.mockito.core)
    testImplementation (libs.mockito.kotlin)
    implementation ("nl.dionsegijn:konfetti-xml:2.0.4")
    implementation ("nl.dionsegijn:konfetti-core:2.0.4")
    implementation ("com.google.code.gson:gson:2.10.1")



    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

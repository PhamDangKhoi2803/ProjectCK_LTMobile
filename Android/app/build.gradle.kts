plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ute.nhom27.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "ute.nhom27.android"
        minSdk = 26
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
    buildToolsVersion = "35.0.0"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation ("com.google.code.gson:gson:2.10.1")

    implementation (libs.stompprotocolandroid)
    implementation (libs.rxjava)
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide (Thêm vào đây)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.picasso:picasso:2.4.0")

    implementation ("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
}
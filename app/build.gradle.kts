plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.terfess.miradioyopal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.terfess.miradioyopal"
        minSdk = 21
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {//data binding
        enable = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //dependencias ExoPlayer Radio
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    //ads
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation("com.google.firebase:firebase-analytics")
    // firebase cloud messagin
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    //coroutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    //splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    //glide fotos
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

}
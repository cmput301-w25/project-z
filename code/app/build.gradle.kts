plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.z"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.z"
        minSdk = 26
        targetSdk = 34
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




    // JUnit 4 (Ensure JUnit 5 is not used because PowerMock does not support JUnit 5)
    testImplementation("junit:junit:4.13.2")

    // Mockito for unit testing
    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("org.mockito:mockito-inline:3.12.4")
    androidTestImplementation("org.mockito:mockito-android:3.12.4")

    // PowerMock dependencies for Mockito
    testImplementation("org.powermock:powermock-core:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")

    testImplementation("org.powermock:powermock-classloading-xstream:2.0.9")

    androidTestImplementation("org.powermock:powermock-module-junit4:2.0.9")
    androidTestImplementation("org.powermock:powermock-api-mockito2:2.0.9")


    // JUnit for testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")




    configurations.all {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
        exclude(group = "org.mockito", module = "mockito-android")
        exclude(group = "org.mockito", module = "mockito-inline")
    }

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    //implementation(files("C:/Users/deols/AppData/Local/Android/Sdk/platforms/android-34/android.jar"))

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    //implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.espresso.contrib)

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    // https://firebase.google.com/docs/android/setup#available-libraries

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}


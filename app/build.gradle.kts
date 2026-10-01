plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.dexcount)

}

android {
    namespace = "com.example.notesappusingfirebase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.notesappusingfirebase"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true  //allow multi dex file (65,356)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    //enabling ViewBinding
    buildFeatures { viewBinding = true }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // GIF dependency
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")

    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("com.github.yukuku:ambilwarna:2.0.1")

    implementation("com.squareup.picasso:picasso:2.8")

    // FirebaseUI for Firebase Realtime Database
    implementation("com.firebaseui:firebase-ui-database:7.1.1")

    //Expandable TextView Dependency
    implementation("io.github.glailton.expandabletextview:expandabletextview:1.0.4")

    implementation("io.github.ParkSangGwon:tedpermission-normal:3.4.2")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("com.github.barteksc:AndroidPdfViewerV1:1.6.0")


    implementation("org.apache.poi:poi-ooxml:4.0.1")
    implementation("org.apache.poi:poi-scratchpad:4.0.1")

    dependencies {
        implementation("androidx.core:core-splashscreen:1.0.1")
    }


}
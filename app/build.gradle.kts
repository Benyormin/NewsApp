
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
    id ("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.newsapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.newsapp"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
/*
    ext{
            val kotlinVersion = "2.1.0"
            val roomVersion = "2.6.1"
        }
        */

    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.androidx.room.common)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)
    //implementation(libs.play.services.ads.api)
    //implementation(libs.ads.mobile.sdk)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //new room dependencies
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    //
    //kapt(libs.android.compiler)

    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")



    // Retrofit for network requests
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("androidx.exifinterface:exifinterface:1.2.0")

    //interceptor
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    //swipe refresh
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit with Coroutines support
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // LifecycleScope
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")



/*
   val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

*/

    // annotationProcessor("androidx.room:room-compiler:$room_version")
   // ksp("android.arch.persistence.room:compiler:2.6.0")
    ksp(libs.androidx.room.compiler)


    val nav_version = "2.8.8"
    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    implementation ("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation ("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    //for getting time

    implementation ("commons-net:commons-net:3.8.0")

    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // HTML Parsing
    implementation("org.jsoup:jsoup:1.17.2")



    //imported from another project
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))


    implementation("com.google.firebase:firebase-analytics")

    // for the database usage
    implementation("com.google.firebase:firebase-firestore:25.1.4")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.1.1")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")

    implementation ("com.google.android.gms:play-services-auth:21.3.0")

    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.4")

    //notifications
    implementation ("androidx.work:work-runtime-ktx:2.10.2")


    //google ads
    implementation("com.google.android.gms:play-services-ads:24.4.0")

    //glass theme
    implementation ("com.eightbitlab:blurview:1.6.6")


}
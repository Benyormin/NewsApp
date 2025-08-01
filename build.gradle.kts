// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) version "2.1.0" apply false
    //new
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.0" apply false

    id("com.google.devtools.ksp") version "2.1.0-1.0.28" apply false

}
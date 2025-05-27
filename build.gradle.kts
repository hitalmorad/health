// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("com.chaquo.python") version "15.0.1" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false



}
allprojects {
    configurations.all {
        resolutionStrategy {
            force ("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
            force( "org.jetbrains.kotlin:kotlin-reflect:1.9.0")
        }
    }
}
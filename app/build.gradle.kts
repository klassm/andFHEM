/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */


buildscript {
    val kotlinVersion: String by project
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

    dependencies {
        classpath(group = "com.github.bjoernq", name = "unmockplugin", version = "0.7.9")
        classpath(
            group = "org.jetbrains.kotlin",
            name = "kotlin-gradle-plugin",
            version = kotlinVersion
        )
        classpath(group = "com.google.gms", name = "google-services", version = "4.3.15")
        classpath(group = "com.github.triplet.gradle", name = "play-publisher", version = "3.8.1")
        classpath(
            group = "org.jetbrains.kotlin",
            name = "kotlin-serialization",
            version = kotlinVersion
        )
        classpath(
            group = "androidx.navigation",
            name = "navigation-safe-args-gradle-plugin",
            version = "2.5.3"
        )
        classpath(group = "com.google.firebase", name = "perf-plugin", version = "1.4.2")
        classpath(group = "com.google.firebase", name = "firebase-crashlytics-gradle", version = "2.9.4")
    }
}

val kotlinVersion: String by project
val kotlinSerializationVersion = "1.0.1"
val ankoVersion = "0.10.8"
val architectureComponentsVersion = "1.1.1"
val glideVersion = "4.15.1"
val daggerVersion = "2.45"
val coroutinesVersion = "1.6.4"
val androidXNavigationVersion = "2.5.3"
val roomVersion = "2.5.1"

plugins {
    id("net.researchgate.release") version "3.0.2"
    id("com.android.application") version "7.4.2"
    kotlin("android").version("1.8.20")
    kotlin("kapt").version("1.8.20")
    kotlin("plugin.serialization") version "1.8.20"
}

repositories {
    mavenCentral()
    maven { setUrl("https://plugins.gradle.org/m2/") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://maven.google.com") }
}


apply(plugin = "de.mobilej.unmock")
apply(plugin = "com.github.triplet.play")
apply(plugin = "androidx.navigation.safeargs.kotlin")

val unmock = configurations.findByName("unmock")!!
dependencies {
    implementation(project(":external-dep"))

    implementation("com.google.firebase", name = "firebase-appindexing", version = "19.1.0")
    implementation(group = "com.google.firebase", name = "firebase-messaging", version = "23.1.2")
    implementation(group = "com.google.firebase", name = "firebase-ads", version = "20.6.0")
    implementation(group = "com.google.firebase", name = "firebase-perf", version = "20.3.1")
    implementation(group = "com.google.android.material", name = "material", version = "1.8.0")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.10.1")
    implementation(group = "com.google.http-client", name = "google-http-client-android", version = "1.43.1") {
        exclude(group = "com.google.code.findbugs")
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(group = "com.google.dagger", name = "dagger", version = daggerVersion)
    implementation(
        group = "com.google.dagger",
        name = "dagger-android-support",
        version = daggerVersion
    )


    kapt(group = "com.google.dagger", name = "dagger-compiler", version = daggerVersion)
    kapt(group = "com.google.dagger", name = "dagger-android-processor", version = daggerVersion)
    implementation(
        group = "com.google.errorprone",
        name = "error_prone_annotations",
        version = "2.18.0"
    )
    implementation(group = "com.google.j2objc", name = "j2objc-annotations", version = "2.8")

    implementation(group = "com.android.billingclient", name = "billing", version = "5.2.0")
    implementation(group = "com.android.billingclient", name = "billing-ktx", version = "5.2.0")
    implementation(group = "androidx.multidex", name = "multidex", version = "2.0.1")
    implementation(group = "androidx.cardview", name = "cardview", version = "1.0.0")
    implementation(group = "androidx.recyclerview", name = "recyclerview", version = "1.3.0")
    implementation(group = "androidx.percentlayout", name = "percentlayout", version = "1.0.0")
    implementation(group = "androidx.annotation", name = "annotation", version = "1.6.0")
    implementation(group = "androidx.preference", name = "preference", version = "1.2.0")
    implementation(group = "androidx.preference", name = "preference-ktx", version = "1.2.0")
    implementation(group = "androidx.room", name = "room-runtime", version = roomVersion)
    kapt(group = "androidx.room", name = "room-compiler", version = roomVersion)
    implementation(
        group = "androidx.navigation",
        name = "navigation-fragment-ktx",
        version = androidXNavigationVersion
    )
    implementation(
        group = "androidx.navigation",
        name = "navigation-ui-ktx",
        version = androidXNavigationVersion
    )
    implementation(
        group = "androidx.navigation",
        name = "navigation-dynamic-features-fragment",
        version = androidXNavigationVersion
    )
    implementation(group = "androidx.fragment", name = "fragment-ktx", version = "1.5.6")

    implementation(group = "commons-net", name = "commons-net", version = "3.9.0")
    implementation(group = "commons-codec", name = "commons-codec", version = "20041127.091804")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.12.0")


    compileOnly(group = "javax.annotation", name = "jsr250-api", version = "1.0")
    implementation(group = "joda-time", name = "joda-time", version = "2.12.5")
    implementation(group = "org.slf4j", name = "slf4j-android", version = "1.7.36")
    implementation(group = "org.apmem.tools", name = "layouts", version = "1.10")
    implementation(group = "net.lingala.zip4j", name = "zip4j", version = "2.11.5")
    implementation(group = "com.github.PhilJay", name = "MPAndroidChart", version = "3.1.0")
    implementation(group = "com.squareup.picasso", name = "picasso", version = "2.71828")
    implementation(group = "com.github.alexfu", name = "Phoenix", version = "1.0.0")
    implementation(group = "com.github.bumptech.glide", name = "glide", version = glideVersion)
    kapt(group = "com.github.bumptech.glide", name = "compiler", version = glideVersion)
    implementation(group = "com.github.bumptech.glide", name = "okhttp3-integration", version = glideVersion) {
        exclude(group = "glide-parent")
    }
    implementation(group = "com.google.firebase", name = "firebase-crashlytics", version = "18.3.6")

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk7", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.5.0")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version = coroutinesVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)

    // Anko Layouts
    implementation(group = "org.jetbrains.anko", name = "anko-sdk25", version = ankoVersion)
    // sdk15, sdk19, sdk21, sdk23 are also available
    implementation(group = "org.jetbrains.anko", name = "anko-appcompat-v7", version = ankoVersion)
    // Coroutine listeners for Anko Layouts
    implementation(group = "org.jetbrains.anko", name = "anko-sdk25-coroutines", version = ankoVersion)
    implementation(group = "org.jetbrains.anko", name = "anko-appcompat-v7-coroutines", version = ankoVersion)
    implementation(group = "org.jetbrains.anko", name = "anko-coroutines", version = ankoVersion)

    implementation(group = "org.reactivestreams", name = "reactive-streams", version = "1.0.4")
    implementation(group = "io.reactivex.rxjava2", name = "rxjava", version = "2.2.21")

    testImplementation(group = "junit", name = "junit", version = "4.13.2")
    testImplementation(group = "com.tngtech.java", name = "junit-dataprovider", version = "1.13.1")
    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.24.2")
    testImplementation(group = "io.mockk", name = "mockk", version = "1.13.5")

    androidTestImplementation(group = "androidx.test", name = "runner", version = "1.5.2")
    androidTestImplementation(group = "androidx.test", name = "rules", version = "1.5.0")
    androidTestImplementation(group = "androidx.test.espresso", name = "espresso-core", version = "3.5.1")
    androidTestImplementation(group = "androidx.test.espresso", name = "espresso-contrib", version = "3.5.1")
}

apply(from = "build-includes/whatsnew.gradle.kts")
apply(from = "build-includes/github.gradle")
apply(from = "build-includes/resourceIdMapper.gradle.kts")
apply(from = "build-includes/deviceConfiguration.gradle.kts")
apply(from = "build-includes/release.gradle")
apply(from = "build-includes/android.gradle")
apply(from = "build-includes/unmock.gradle")
apply(from = "build-includes/postTasks.gradle")
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.firebase-perf")
apply(plugin = "com.google.firebase.crashlytics")

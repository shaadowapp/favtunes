plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.firebase.perf)
}

android {
    namespace = "com.shaadow.tunes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shaadow.tunes"
        minSdk = 24
        targetSdk = 35
        versionCode = 11
        versionName = "2.1.5"
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "17"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.material.motion.compose)
    implementation(libs.compose.navigation)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.core.splashscreen)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.media)
    implementation(libs.media3.exoplayer)
    implementation(libs.reorderable)
    implementation(libs.room)
    implementation(libs.appcompat)
    implementation(libs.ui.tooling.preview.android)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.measurement.api)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.runtime.livedata)
    implementation(libs.core)
    implementation(libs.material)
    ksp(libs.room.compiler)
    implementation(projects.innertube)
    implementation(projects.kugou)
    coreLibraryDesugaring(libs.desugaring)
    implementation(platform(libs.firebase.bom))
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.material3)
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)
}
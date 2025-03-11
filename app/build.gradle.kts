
plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "its.madruga.warevamp"
    compileSdk = 35

    defaultConfig {
        applicationId = "its.madruga.warevamp"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.1"
    }

//    signingConfigs {
//        create("release") {
//            storeFile = file(project.property("RELEASE_FILE") as String)
//            storePassword = project.property("RELEASE_PASSWORD") as String
//            keyAlias = project.property("RELEASE_ALIAS") as String
//            keyPassword = project.property("RELEASE_PASSWORD") as String
//        }
//    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "META-INF/**"
            excludes += "okhttp3/**"
            excludes += "kotlin/**"
            excludes += "org/**"
            excludes += "**.properties"
            excludes += "**.bin"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.preference)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.material)

    implementation(libs.dexkit)
    implementation(libs.appcompat)
    compileOnly(libs.libxposed)

    implementation(libs.rikkax.appcompat)
    implementation(libs.rikkax.material)
    implementation(libs.rikkax.material.preference)
}


configurations.all {
    exclude("androidx.appcompat", "appcompat")
}
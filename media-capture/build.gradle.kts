import java.net.URI

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("maven-publish")
    id("kotlin-kapt")
    jacoco
}

android {
    namespace = "mediacapture.io"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        aarMetadata {
            minCompileSdk = 24
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")

    // Not managed by compose-bom
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.compose.material3:material3")

    // camera X
    implementation("androidx.camera:camera-core:1.3.0-alpha07")
    implementation("androidx.camera:camera-camera2:1.3.0-alpha07")
    implementation("androidx.camera:camera-lifecycle:1.3.0-alpha07")
    implementation("androidx.camera:camera-video:1.3.0-alpha07")
    implementation("androidx.camera:camera-view:1.3.0-alpha07")
    implementation("androidx.camera:camera-extensions:1.3.0-alpha07")

    // RX
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.6.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-inline:3.11.2")
    testImplementation("junit:junit:4.13")

    val daggerVersion = "2.51"
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
}

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
        // ...
    }
}

tasks.create("unitTestCoverageReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    reports {
        html.required = true
        csv.required = true
    }

    executionData("${project.buildDir}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")

    val fileFilter = arrayOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "model/"
    )

    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug/mediacapture/io") {
        exclude(*fileFilter)
    }
    this.classDirectories.from(debugTree)

    val mainSrc = "${project.projectDir}/src/main/java"
    this.sourceDirectories.from(mainSrc)

}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("release"))
                groupId = "com.github.jcunningham"
                artifactId = "mediacapture.io"
                version = "v0.1.1-alpha"
            }
        }

        repositories {
            maven {
                url = URI("https://jitpack.io")
            }
        }
    }
}

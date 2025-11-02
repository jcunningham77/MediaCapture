import java.net.URI

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    id("maven-publish")
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose.compiler)
    jacoco
}

android {
    namespace = "mediacapture.io"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
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
            enableAndroidTestCoverage = true
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
        targetSdk = 33
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
}

jacoco {
    toolVersion = "0.8.12"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.datetime)

    // Not managed by compose-bom
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.material3)

    // camera X
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // RX
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.reactivestreams.ktx)
    implementation(libs.rxjava)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.test.rules)

    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
}

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
        // ...
    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

tasks.register<JacocoReport>("mergeDebugCoverageReports") {
    group = "Reporting"
    description = "Merge JaCoCo coverage reports for debug."
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")

    val coverageFiles = fileTree("$buildDir") {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/**/*.ec")
    }

    val srcDirs = files("src/main/java", "src/main/kotlin")

    sourceDirectories.setFrom(srcDirs)

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

    executionData.setFrom(coverageFiles)

    reports {
        csv.required = true
        html.required = true
    }
}

tasks.register("runDebugCoverageReport") {
    group = "Verification"
    description =
        "Run both unit and instrumented tests and generate a merged coverage report for debug."

    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest", "mergeDebugCoverageReports")
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

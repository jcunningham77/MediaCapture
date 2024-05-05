import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = URI("https://jitpack.io") }
    }
}

rootProject.name = "MediaCapture"
include(":app")
include(":media-capture")

plugins {
    id("com.gradle.enterprise") version("3.16.2")
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        println("This build is running on Github")
//        buildScan {
//            publishAlways()
//            termsOfServiceUrl = "https://gradle.com/terms-of-service"
//            termsOfServiceAgree = "yes"
//        }
    } else {
        println("This build is running locally")
    }
}

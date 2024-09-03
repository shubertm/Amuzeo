import groovy.util.logging.Log
import java.io.FileInputStream
import java.net.URI
import java.util.Properties
import java.util.logging.Logger

var user: String = ""
var pass: String = ""

Properties().apply {
    try {
        load(
            FileInputStream(rootDir.resolve("github.properties"))
        )
        user = this["gpr.usr"].toString()
        pass = this["gpr.key"].toString()

    } catch (e: NoSuchFileException) {
        e.printStackTrace()
    }
}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            name = "GithubPackages"
            url = URI.create("https://maven.pkg.github.com/shubertm/amuze")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: user
                password = System.getenv("GITHUB_TOKEN") ?: pass
            }
        }

        maven {
            name = "GitHubPackages"
            url = URI.create("https://maven.pkg.github.com/ARK-Builders/arklib-android")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: user
                password = System.getenv("GITHUB_TOKEN") ?: pass
            }
        }
    }
}

rootProject.name = "Amuzeo"
include(":app")

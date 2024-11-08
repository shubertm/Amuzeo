import java.io.FileInputStream
import java.net.URI
import java.util.Properties

var user: String = ""
var pass: String = ""

Properties().apply {
    val file = rootDir.resolve("github.properties")
    if (file.exists()) {
        load(
            FileInputStream(file)
        )
        user = this["gpr.usr"].toString()
        pass = this["gpr.key"].toString()
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

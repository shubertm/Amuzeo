import java.io.FileInputStream
import java.net.URI
import java.util.Properties

val githubProperties = Properties().apply {
    load(
        FileInputStream(rootDir.resolve("github.properties"))
    )
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
                username = githubProperties["gpr.usr"].toString()
                password = githubProperties["gpr.key"].toString()
            }
        }
    }
}

rootProject.name = "Amuzeo"
include(":app")

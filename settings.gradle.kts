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
                username = "token"
                password = "\u0037\u0066\u0066\u0036\u0030\u0039\u0033\u0066\u0032\u0037\u0033\u0036\u0033\u0037\u0064\u0036\u0037\u0066\u0038\u0030\u0034\u0039\u0062\u0030\u0039\u0038\u0039\u0038\u0066\u0034\u0066\u0034\u0031\u0064\u0062\u0033\u0064\u0033\u0038\u0065"
            }
        }
    }
}

rootProject.name = "Amuzeo"
include(":app")

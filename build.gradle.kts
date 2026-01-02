import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    // Android
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Kotlin
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    // Tools
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
        android.set(true)
        debug.set(true)
        coloredOutput.set(true)
        verbose.set(true)
        outputToConsole.set(true)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    if (name != "app") {
        apply(plugin = "com.vanniktech.maven.publish")

        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()

            coordinates("io.github.angrypodo", name, "0.1.0")

            pom {
                name.set(project.name)
                description.set("Wisp library: ${project.name}")
                inceptionYear.set("2025")
                url.set("https://github.com/angrypodo/wisp")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("angrypodo")
                        name.set("MinJae Han")
                        url.set("https://github.com/angrypodo")
                    }
                }
                scm {
                    url.set("https://github.com/angrypodo/wisp")
                    connection.set("scm:git:git://github.com/angrypodo/wisp.git")
                    developerConnection.set("scm:git:ssh://git@github.com/angrypodo/wisp.git")
                }
            }
        }
    }
}

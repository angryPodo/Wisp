plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":wisp-annotations"))
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
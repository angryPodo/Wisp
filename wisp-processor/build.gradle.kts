plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":wisp-annotations"))
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

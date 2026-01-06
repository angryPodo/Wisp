plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())
}

dependencies {
    implementation(project(":wisp-annotations"))
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Unit Test
    testImplementation(libs.mockk)

    // Integration Test (KCT)
    testImplementation(libs.kct.fork.core)
    testImplementation(libs.kct.fork.ksp)
}
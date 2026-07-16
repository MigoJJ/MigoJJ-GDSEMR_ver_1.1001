plugins {
    application
    alias(libs.plugins.javafx)
    // jacoco (disabled for Java 25 compatibility)
}

val javafxVersion: String by project
val sqliteVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation(project(":core"))
    implementation(project(":ai"))

    implementation(libs.sqlite.jdbc)
    runtimeOnly(libs.logback.classic)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    // Test Dependencies
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.assertj.core)
    // testImplementation(libs.testfx.core)
    // testImplementation(libs.testfx.junit5)
    testImplementation(libs.sqlite.jdbc)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",
        "-Dnet.bytebuddy.experimental=true",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED"
    )
}

// JaCoCo Configuration (disabled for Java 25 compatibility)
// jacoco {
//     toolVersion = "0.8.12"
// }
//
// tasks.jacocoTestReport {
//     dependsOn(tasks.test)
//     reports {
//         xml.required = true
//         html.required = true
//         csv.required = false
//     }
// }

javafx {
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}

application {
    mainClass.set("com.emr.gds.IttiaApp")
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics", "--enable-native-access=ALL-UNNAMED")
}
plugins {
    application
    alias(libs.plugins.javafx)
}

val javafxVersion: String by project
val sqliteVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation(project(":core"))
    implementation(project(":ai"))

    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.16")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1") // Added for JSON parsing

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",
        "-Dnet.bytebuddy.experimental=true",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
    )
}

javafx {
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}

application {
    mainClass.set("com.emr.gds.IttiaApp")
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics", "--enable-native-access=ALL-UNNAMED")
}
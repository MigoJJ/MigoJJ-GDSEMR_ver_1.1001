plugins {
    `java-library`
}

dependencies {
    // core 모듈에서 외부 라이브러리 쓰면 여기에 추가
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

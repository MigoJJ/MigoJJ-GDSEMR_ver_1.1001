# GDSEMR_ver_1.1001

JavaFX EMR prototype targeting Java 25 and JavaFX 25.

## Architecture

```mermaid
graph TD;
    app[app (JavaFX UI)] -->|Network/REST| server[server (Spring Boot API)];
    app --> utilities;
    app --> list;
    utilities --> list;
```

- **app**: Main JavaFX application.
- **utilities**: Shared logic and helper classes.
- **list**: Core data structures and lists.
- **server**: Spring Boot REST API backend.

## Requirements
- JDK 25 (Gradle toolchains will download/use it automatically if available)
- JavaFX 25 SDK artifacts (fetched from Maven Central by the OpenJFX Gradle plugin)
- SQLite JDBC (declared as a dependency; no manual install needed)

## Build & Run
- Root task: `./gradlew run` (delegates to `:app:run`)
- Module tasks: `./gradlew :app:run`, `./gradlew :list:test`, etc.
- API stub: `./gradlew runServer` (delegates to `:server:bootRun`, serves REST skeleton on port 8080)
- If multiple JDKs are installed, point Gradle at Java 25 with `export ORG_GRADLE_JAVA_HOME=/path/to/jdk-25`.
- `./run-gradle.sh` is available as a convenience wrapper; update its paths if you move the project.

## Notes
- Java toolchain and version properties are centralized in `gradle.properties`.
- JavaFX version is configurable via `gradle.properties` (`javafxVersion`).
- Kotlin DSL templates for Gradle 9.2 live in `templates/` (`build.gradle.kts.template`, `app.build.gradle.kts.template`, `build-logic.build.gradle.kts.template`) to help migrate without version drift between app and build-logic.
- Spring Boot web skeleton lives in `server/` with health and template endpoints to extend.
# GDSEMR_ver_1.1001

JavaFX EMR prototype targeting Java 25 and JavaFX 25.

## Architecture

```mermaid
graph TD;
    app[app (JavaFX UI)] -->|Network/REST| server[server (Spring Boot API)];
    app --> ai[ai (LLM Gateway)];
    app --> core[core (Shared Config/Utilities)];
```

- **app**: Main JavaFX application with specialized medical modules (EKG, CXR, Thyroid). Now features **AI Documentation Assist**.
- **ai**: (Linked from APIGDSEMR) Gateway for LLM services (Gemini 1.5 Pro/Flash, OpenAI).
- **server**: Spring Boot REST API prototype with persistent JPA storage (H2).
- **core**: Shared utilities, runtime environment configuration, and common data structures.

## Requirements
- JDK 25 (Gradle toolchains will download/use it automatically if available)
- JavaFX 25 SDK artifacts (fetched from Maven Central by the OpenJFX Gradle plugin)
- SQLite JDBC (declared as a dependency; no manual install needed)
- `GEMINI_API_KEY` (Environment variable required for AI features)

## Build & Run
- Root task: `./gradlew run` (delegates to `:app:run`)
- Module tasks: `./gradlew :app:run`, `./gradlew :server:bootRun`, etc.
- API server: `./gradlew runServer` (delegates to `:server:bootRun`, serves on port 8080)

## Modernization & Refactoring (Phase 1-4 Completed)
1. **Standardized Runtime**: Data stored in `~/.gdsemr/db`, removing local repo dependency.
2. **Server Persistence**: Replaced in-memory repo with JPA + H2 + Flyway migrations.
3. **Modular UI**: Refactored monolithic panes (e.g., ThyroidPane) into clean, testable sub-panes and controllers.
4. **AI Bridge**: Integrated `APIGDSEMR` as a module for automated clinical documentation.

## Notes
- Java toolchain and version properties are centralized in `gradle/libs.versions.toml`.
- SLF4J/Logback integrated for professional logging (logs stored in `~/.gdsemr/logs`).

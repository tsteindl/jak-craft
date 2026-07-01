# Repository Guidelines

## Project Structure & Module Organization

This repository is a Minecraft Forge mod for Minecraft 1.20.1 using Java 17. Main Java sources live in `src/main/java/com/tsteindl/jakcraft`, with client-only setup under `src/main/java/com/tsteindl/jakcraft/client`. Forge metadata and assets live in `src/main/resources`, especially `META-INF/mods.toml` and `assets/jakcraft`. Item models are in `assets/jakcraft/models/item`, translations in `assets/jakcraft/lang/en_us.json`, and textures in `assets/jakcraft/textures/item`. Generated data, when used, should go under `src/generated/resources`. Test directories exist under `src/test`, but there are currently no test files.

## Build, Test, and Development Commands

- `./gradlew genIntellijRuns`: generates IntelliJ run configurations for Forge development.
- `./gradlew build`: compiles the mod, processes resources, reobfuscates the jar, and writes artifacts to `build/libs/`.
- `./gradlew runData`: runs Forge data generation into `src/generated/resources`.
- `./gradlew test`: runs Java tests if test classes are added.

For local gameplay testing, use the IDE `runClient` or `runServer` configurations after generating runs. The existing README notes that these should be launched from the IDE run configurations, not directly from the Gradle task list.

## Coding Style & Naming Conventions

Use Java 17 and UTF-8. Keep code in the `com.tsteindl.jakcraft` package unless adding a clear subpackage such as `client`. Existing Java files use two-space indentation and Forge `DeferredRegister` patterns. Use PascalCase for classes, `UPPER_SNAKE_CASE` for static registry constants, and lowercase snake_case for registry IDs and asset names, for example `carlos_die_klinge` or `jak_velociraptor_spawn_egg`. Keep the mod id as `jakcraft` and ensure resource paths match registry names.

## Testing Guidelines

Add unit tests under `src/test/java` when logic can be tested outside Minecraft. For mod behavior, verify in the IDE `runClient` configuration and, where relevant, `runServer` for dedicated-server compatibility. If GameTests are introduced, keep their namespace aligned with `jakcraft` because the Gradle run configs enable GameTests for that mod id.

## Commit & Pull Request Guidelines

Recent commits use short, informal, imperative-style summaries such as `added carlos texture` and `muellager now has custom drops`. Keep commits focused and describe the gameplay or asset change directly. Pull requests should include a concise summary, testing performed (`./gradlew build`, `runClient`, screenshots for visual assets), and any required dependency or asset notes. Link issues when applicable and mention new registry IDs, textures, or generated resources.

## Agent-Specific Instructions

Do not overwrite generated assets or user-created textures without checking intent. When adding entities or items, update registration classes, language entries, models, textures, and client renderer setup together so the mod loads cleanly.

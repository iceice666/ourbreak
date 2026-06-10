# Verification

## Release Audit

Verified on June 10, 2026:

- JUnit 6.1.0 is the latest stable release:
  <https://github.com/junit-team/junit-framework/releases/tag/r6.1.0>
- Guava 33.6.0-jre is the latest stable JRE release:
  <https://github.com/google/guava/releases/tag/v33.6.0>
- jMonkeyEngine 3.9.0-stable remains the latest stable release. Version 3.10.0-beta1 is newer but is explicitly marked
  as a prerelease, so it is excluded:
  <https://github.com/jMonkeyEngine/jmonkeyengine/releases>
- Zay-ES 1.6.0 remains the latest Maven Central release:
  <https://repo1.maven.org/maven2/com/simsilica/zay-es/maven-metadata.xml>
- Gradle 9.5.1 remains the latest stable Gradle release:
  <https://gradle.org/releases/>
- Foojay resolver convention 1.0.0 remains the latest stable plugin release:
  <https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention>

Applying this approved OpenSpec change confirms repository-policy approval for the direct Guava override. Gson 2.9.1 and
SLF4J API 1.7.32 remain transitively managed because the audit found no applicable OSV advisory or compatibility need.

## Dependency Resolution

- `:app:runtimeClasspath` resolves Guava 19.0 to Guava 33.6.0-jre by conflict resolution.
- `:app:testRuntimeClasspath` resolves JUnit Jupiter API, engine, parameters, Platform commons, engine, and launcher to
  version 6.1.0.
- `:app:dependencyInsight` confirms Guava 33.6.0-jre is selected over Zay-ES's requested Guava 19.0.

## Compatibility

- `EntityDataTest` exercises Zay-ES entity creation, component access, entity-set additions, filtered membership changes,
  and the Guava-backed `FieldFilter` equality path.
- `nix develop --command ./gradlew test --rerun-tasks` completed successfully after the override.

## Vulnerability Check

An OSV batch query covered all 32 unique Maven artifacts resolved on the application and test runtime classpaths. Every
result was empty, including Guava 33.6.0-jre, Gson 2.9.1, SLF4J API 1.7.32, jMonkeyEngine 3.9.0-stable, Zay-ES 1.6.0,
JUnit 6.1.0, and their resolved transitives.

## ADDED Requirements

### Requirement: Stable direct dependency selection
The build SHALL pin direct dependencies and build plugins to explicit stable releases unless a prerelease is deliberately
approved for a documented reason.

#### Scenario: Audited direct versions
- **WHEN** the implementation dependency audit is completed
- **THEN** jMonkeyEngine, Zay-ES, Gradle, and the Foojay resolver remain on the latest verified stable releases

#### Scenario: Prerelease excluded
- **WHEN** a newer jMonkeyEngine beta exists but the current stable release remains supported
- **THEN** the build continues to select the stable release

### Requirement: Current test platform
The build SHALL use JUnit Jupiter 6.1.0 or a newer compatible stable patch release verified during implementation.

#### Scenario: Test runtime resolution
- **WHEN** the test runtime classpath is resolved
- **THEN** JUnit API, engine, parameters, platform, and launcher resolve to one compatible version family

### Requirement: Secure Guava resolution
The runtime classpath SHALL override Zay-ES's transitive Guava 19.0 with Guava 33.6.0-jre or a newer compatible stable
release that is not affected by the audited Guava OSV advisories.

#### Scenario: Runtime dependency resolution
- **WHEN** the application runtime classpath is resolved
- **THEN** Guava 19.0 is absent and the selected Guava version is the approved secure version

#### Scenario: Zay-ES compatibility
- **WHEN** the secure Guava override is active
- **THEN** entity creation, component access, entity-set change tracking, filtering used by the project, and all gameplay
  tests pass

### Requirement: Evidence-based transitive overrides
The build SHALL NOT override unrelated transitive dependencies solely because a newer version exists.

#### Scenario: Old transitive version without demonstrated issue
- **WHEN** a transitive dependency is old but has no applicable known vulnerability or required compatibility fix
- **THEN** it remains managed by its owning direct dependency

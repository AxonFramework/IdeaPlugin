# Axon Framework IntelliJ Plugin - Technical Overview

## Quick Summary

IntelliJ plugin that provides IDE support for Axon Framework 4.x and 5.x. Automatically detects project version and adapts behavior. Provides handler detection, navigation, and code validation for both Java and Kotlin.

**Version:** 0.9.5
**Test Suite:** 159 tests (140 v4, 19 v5) - all passing
**Min IntelliJ:** 2024.2

---

## Key Architecture Concepts

### Version Detection & Adaptation

**AxonVersionService** (`/usage/AxonVersionService.kt`):
- Scans JAR dependencies on project open
- Reads `META-INF/maven/*/pom.properties` to extract version
- Creates appropriate `VersionedComponentFactory` (v4 or v5)
- Re-checks on dependency changes only

**Factory Pattern:**
```
VersionedComponentFactory
├── Axon4ComponentFactory → Returns v4 searchers (Saga, Deadline, AggregateConstructor)
└── Axon5ComponentFactory → Returns v5 searchers (EntityCreator only)
```

This eliminates need for version checks scattered throughout code.

### Handler Discovery

**Flow:**
1. `AnnotationResolver` scans for Axon annotations (both v4 & v5 packages, meta-annotations up to 5 levels)
2. `MessageHandlerResolver` gets version-appropriate searchers from factory
3. Searchers (CommandHandler, EventHandler, etc.) scan PSI for annotated methods
4. Results cached using PSI modification tracking

**Handler Types:**
- **v4 & v5:** CommandHandler, EventHandler, EventSourcingHandler, QueryHandler
- **v4 only:** SagaEventHandler, DeadlineHandler, AggregateConstructor (implicit)
- **v5 only:** EntityCreator (explicit)

### Key Differences: v4 vs v5

**Package Changes (v5):**
- Handlers moved to `org.axonframework.messaging.*.annotation` packages
- EventSourcingHandler: `org.axonframework.eventsourcing.annotation`

**Annotation Changes (v5):**
```java
// Aggregates
@AggregateRoot → @EventSourcedEntity (or @EventSourced for Spring)
@AggregateMember → @EntityMember

// Handlers
Constructor → @EntityCreator (must be explicit)

// Removed
@Saga, @SagaEventHandler, @DeadlineHandler (features removed in v5)
```

**Critical:** Spring users use `@EventSourced` stereotype which is meta-annotated with `@EventSourcedEntity`.

---

## Project Structure

```
/src/main/kotlin/.../plugin/
├── api/                    # AxonAnnotation, MessageHandlerType, Handler types
├── usage/                  # AxonVersionService + component factories
├── resolving/
│   ├── AnnotationResolver.kt       # Scans annotations (v4 & v5)
│   ├── MessageHandlerResolver.kt   # Gets handlers from factory
│   ├── AggregateStructureResolver.kt  # Entity hierarchy
│   └── handlers/searchers/         # 8 searcher implementations
├── markers/                # Line marker providers (gutter icons)
└── inspections/            # Code checks (v4 only - disabled for v5)
```

---

## Important Implementation Details

### AxonAnnotation Enum (`/api/AxonAnnotation.kt`)

Defines all annotations with both v4 and v5 fully qualified names:
```kotlin
COMMAND_HANDLER(
    "org.axonframework.commandhandling.CommandHandler",  // v4
    "org.axonframework.messaging.commandhandling.annotation.CommandHandler"  // v5
),
EVENT_SOURCED(
    null,  // No v4 equivalent
    "org.axonframework.extension.spring.stereotype.EventSourced"  // v5 Spring
),
AGGREGATE_MEMBER(
    "org.axonframework.modelling.command.AggregateMember",  // v4
    "org.axonframework.modelling.entity.annotation.EntityMember"  // v5
),
```

**Note:** `EVENT_SOURCED` points to Spring stereotype, `EVENT_SOURCED_ENTITY` points to core annotation. Both resolve to same behavior via meta-annotation scanning.

### Aggregate Recognition (`/util/PSiProcessingUtils.kt`)

```kotlin
fun PsiClass?.isAggregate(): Boolean {
    if (this == null) return false
    return isAnnotated(AGGREGATE_ROOT) ||      // v4
           isAnnotated(EVENT_SOURCED) ||        // v5 Spring
           isAnnotated(EVENT_SOURCED_ENTITY)    // v5 Core
}
```

### Line Marker Caret Position

Line markers detect annotations by looking for `UAnnotation` parent. Caret must be **inside** annotation:
```kotlin
@CommandHandler<caret>  // ✅ Works
fun handle() {}

@CommandHandler
fun <caret>handle() {}  // ❌ Doesn't work
```

This is why line marker tests place caret inside annotations.

---

## Test Infrastructure

### Dual Test Base Classes

**Why needed:** Gradle resolves to single version, but we need both v4 and v5 for tests.

**Solution:** Download JARs from Maven Central, cache locally, add via `PsiTestUtil`.

**Version Configuration:** Versions configured in `gradle.properties`:
```properties
axonVersion=4.10.1    # v4 test version
axon5Version=5.0.0    # v5 test version
```

Passed to tests via system properties (see `build.gradle.kts`):
```kotlin
test {
    systemProperty("axonVersion", properties("axonVersion"))
    systemProperty("axon5Version", properties("axon5Version"))
}
```

**AbstractAxonFixtureTestCase** (v4 tests):
- Reads `System.getProperty("axonVersion")` (defaults to "4.10.1")
- Downloads JARs to `src/test/resources/libraries/`
- Adds via `PsiTestUtil.addLibrary()` (test scope)
- 140 tests

**AbstractAxon5FixtureTestCase** (v5 tests):
- Reads `System.getProperty("axon5Version")` (defaults to "5.0.0")
- Downloads JARs to same directory
- Adds via `PsiTestUtil.addProjectLibrary()` (production scope - required for version detection)
- Calls `AxonVersionService.runCheck()` after adding JARs
- Auto-imports v5 packages (different from v4)
- 19 tests

**Critical:** v5 tests use `addProjectLibrary()` not `addLibrary()` because `AxonVersionService.getAxonVersions()` uses `.productionOnly()` scope.

**Updating versions:** Just change `gradle.properties` - no code changes needed.

---

## Common Pitfalls & Fixes

### Issue: Version Detection Not Working in Tests
**Cause:** JARs added via `PsiTestUtil.addLibrary()` go to test scope, but `AxonVersionService` checks production scope.
**Fix:** Use `PsiTestUtil.addProjectLibrary()` and call `runCheck()` after adding.

### Issue: Entity Hierarchy Not Showing
**Cause:** Annotation name wrong (v5 uses `@EntityMember` not `@AggregateMember`).
**Fix:** Updated `AGGREGATE_MEMBER` enum to map v5 to `org.axonframework.modelling.entity.annotation.EntityMember`.

### Issue: Line Marker Tests Failing
**Cause:** Caret on method name instead of annotation.
**Fix:** Place caret inside annotation: `@EntityCreator<caret>`.

### Issue: Spring @EventSourced Not Recognized
**Cause:** Wrong package in enum.
**Fix:** Point to Spring stereotype: `org.axonframework.extension.spring.stereotype.EventSourced` (meta-annotated with @EventSourcedEntity).

---

## Inspections (Code Warnings)

**v4 only:**
- AggregateConstructorInspection - Checks for no-arg constructor
- AggregateIdInspection - Checks for @EntityId
- AggregateMemberRoutingKeyInspection - Validates routing keys
- SagaAssociationPropertyInspection - Validates saga associations

**All disabled for v5 projects** via `shouldCheck()` method checking version service.

---

## Key Files to Know

| File | Purpose |
|------|---------|
| `AxonVersionService.kt` | Detects v4/v5, creates factory |
| `AxonAnnotation.kt` | All annotation FQNs (v4 & v5) |
| `Axon4ComponentFactory.kt` | Creates v4 searchers |
| `Axon5ComponentFactory.kt` | Creates v5 searchers |
| `MessageHandlerResolver.kt` | Central handler lookup |
| `AnnotationResolver.kt` | Annotation scanning (meta-annotations) |
| `PSiProcessingUtils.kt` | `isAggregate()`, payload resolution |
| `AbstractAxonFixtureTestCase.kt` | v4 test base |
| `AbstractAxon5FixtureTestCase.kt` | v5 test base |

---

## Development Commands

```bash
./gradlew test                    # All 159 tests
./gradlew test --tests "*Axon5*"  # Just v5 tests
./gradlew buildPlugin             # Build plugin
./gradlew runIde                  # Test in IDE
rm -rf src/test/resources/libraries/  # Clear JAR cache
```

---

## Maven Central JAR Download Pattern

Used by both test base classes:

```kotlin
private fun addLibrary(artifactName: String, version: String) {
    val jarFile = File(librariesDir, "$artifactName-$version.jar")

    if (!jarFile.exists()) {
        val url = "https://repo1.maven.org/maven2/org/axonframework/$artifactName/$version/$artifactName-$version.jar"
        URL(url).openStream().use { input ->
            FileOutputStream(jarFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    PsiTestUtil.addProjectLibrary(module, "$artifactName-$version", listOf(jarFile.absolutePath))
}
```

**Benefits:** Works in CI, reproducible, cached locally in project.

---

## What to Remember

1. **Version detection is automatic** - code doesn't need version checks everywhere
2. **Factory pattern provides version-specific components** - searchers, annotations, terminology
3. **Both v4 and v5 annotations defined in AxonAnnotation enum** - resolver checks both
4. **Inspections check version before running** - v4 checks disabled for v5
5. **Tests download real JARs from Maven Central** - no mocking, tests real behavior
6. **Line marker caret position matters** - must be inside annotation
7. **Spring @EventSourced is meta-annotated** - works via descendant scanning
8. **v5 uses @EntityMember not @AggregateMember** - different package too

---

## Resources

- [Axon 4.x Docs](https://docs.axoniq.io/reference-guide/v/4.10/)
- [Axon 5.x Docs](https://docs.axoniq.io/reference-guide/)
- [Migration Guide](https://docs.axoniq.io/reference-guide/axon-framework/migration-guide)
- [Plugin Repo](https://github.com/AxonFramework/IdeaPlugin)

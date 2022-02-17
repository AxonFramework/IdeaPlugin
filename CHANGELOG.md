<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Axon Framework plugin Changelog

## [Unreleased]

### Added

## [1.0.0]

### Added

- Rewritten from scratch for Axon Framework 4.0
- Added support for Kotlin in addition to Java
- Add line marker on handler methods, such as Command-, Event- and DeadlineHandlers
- Add line marker on constructors of classes that are used as payload, for navigation to handlers
- Add line marker on scheduling of deadlines, for navigation to deadline handlers
- Add line marker on class declarations of classes that are used as payloads, for navigation to both constructors and handlers
- Add line marker on command handlers that are intercepted by a `@CommandHandlerInterceptor` annotation
- Add usage provider which marks methods (and payloads) used by Axon Framework as used
- Add inspection for missing empty constructor in aggregates
- Add inspection for missing identifier in aggregates
- Add inspection for missing @EntityId annotation when required

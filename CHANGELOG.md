<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Axon Framework plugin Changelog

## [0.8.8]
- Fix class hierarchy not being taken into account for handler and creator resolving.
- Remove wrong implementation of interceptor support, to possibly be re-implemented in a future release in a better form.

## [0.8.7]
- Plugin is now compatible with IDEA 2024.2 (IDEA 242.*)

## [0.8.6]
- Plugin is now compatible with IDEA 2024.1 (IDEA 241.*)

## [0.8.5]
- Fix StackOverflow issue when aggregate model is recursive (https://github.com/AxonFramework/IdeaPlugin/issues/261)

## [0.8.4]
- Plugin is now compatible with IDEA 2023.3 (IDEA 233.*) (Thanks to @maartenn)

## [0.8.3]
- Move to built-in gutter renderer to prevent exception (#235)
- Fix exception on deadline manager methods when arguments are not filled
- Move base to IDEA 2023.2 due to incompatibility of certain APIs

## [0.8.2]
- Plugin is now compatible with IDEA 2023.2 (IDEA 232.*)


## [0.8.1]
- Plugin is now compatible with IDEA 2023.1 (IDEA 231.*)
- Fixes deprecation warnings

## [0.8.0]
- Plugin is now compatible with IDEA 2022.3 (IDEA 223.*)
- Upgraded various dependencies to go with the upgrade
- Baselined plugin to JDK 17 and IDEA 2022.2. Plugin is now incompatible with versions 2022.1 and older.

## [0.7.3]

### Fixed
- Issue where Axon version detection did not work properly, disabling the plugin while it should not.
- NPE when analyzing Kotlin files for AggregateIdentifier inspection

## [0.7.2]

### Fixed
- Issue where Axon version detection did not work properly
- Prevent infinite recursion in annotation scanning
- Fix AggregateIdentifier inspection to include methods in addition to fields
- Fix issue where element became invalid upon opening line marker popup

### Added
- AggregateIdentifier now warns if the method that was annotated returns void

### Changed
- Upgrade Sentry version

## [0.7.1]
The plugin is now marked as compatible with IntelliJ 2022.1.

## [0.7.0]

### Fixed
- [#66] Fix possible race condition between primary and secondary cache computations in annotation resolver
- [#68] Remove unnecessary catching of errors during resolving a qualifiedName in PsiProcessingUtil
- [#69] Disable plugin functionality when old versions are used
- Fix OutOfBoundException in AxonImplicitUsageProvider when using Groovy

### Added

- [#43] Support builder method references, both ways
- [#38] Aggregate structure hierarchy is now shown in model popup
- [#41] Mark methods annotated with @ResetHandler as used
- [#31] Aggregate structure hierarchy is now shown in model popup
- [#27] Inspection when associationProperty on @SagaEventHandler is missing in the message
- [#23] Inspection when routingKey is missing in messages

## [0.6.2]

### Fixed
- [#59] Fixed ClassCastException during querying provider ClassLineMarkerProvider (thanks @kaleev for reporting the error)
- Fix various occasions where invalid PsiElements could be shown in line marker popups by filtering on validity

## [0.6.1]

### Fixed
- Uncaught exceptions were reported to Sentry, even ones not caused by the plugin. Disabled uncaught exception handling. 
- Require restart on plugin installation. Otherwise, ClassCastExceptions or Duplicate PluginExceptions can occur. 

## [0.6.0]

### Fixed
- Solved memory leak by excessive caching of handler calculation result

### Added

- Users can now report their exceptions and will be stored in our Sentry installation
- Add option for people to report feedback through the Tools menu

## [0.5.2]

### Added

- Recommend plugin when Axon Framework is found in project

## [0.5.1]

### Fixed

- Fix popup on deadline manager methods when there are qualified references as arguments. Fixes #16
- Fix empty inspection description in inspection window. Fixes #21
- The correct icon is now shown for publishers in line marker popup. Fixes #18

### Changed

- Query handlers are more easily identifiable in line marker popup

## [0.5.0]

### Changed

- Improve list rendering for better performance
- Restructured packages
- Improve caching on line markers to improve performance

## [0.4.2]

### Changed

- Always show popup for references, even if there is only one.

## [0.4.1]

### Changed

- Split deadline resolver from normal message creation resolver for performance improvements

### Added

- Utility to cache information on PsiElement level to improve performance

## [0.4.0]

### Fixed

- Fixed issue with deadline cancel methods not always being detected correctly.

## [0.3.0]

### Fixed

- Ghost line marker on aggregate constructors
- Fixed event handlers showing as "Event Processor". They will now show the name of the containing class

### Added

- Line marker on aggregate members to easily related models with the aggregate model hierarchy.

## [0.2.0]

### Changed

- New icons added based on the new logo
- Intercepted command handlers now have their own icon

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

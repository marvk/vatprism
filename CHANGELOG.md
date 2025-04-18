# Changelog

## [0.3.6](https://github.com/marvk/vatprism/compare/v0.3.4...v0.3.5) - 2025-04-18

### Fixed

- Fixed an issue with broken vatsim api airport data causing the application to crash at launch ([#148](https://github.com/marvk/vatprism/issues/91))

## [0.3.5](https://github.com/marvk/vatprism/compare/v0.3.4...v0.3.5) - 2022-06-04

### Fixed

- Fixed an issue with the callsign matcher leading to matches that weren't as specific as possible ([#91](https://github.com/marvk/vatprism/issues/91))

### Security

- Update to Log4j (2.17.1 -> 2.17.2)

## [0.3.4](https://github.com/marvk/vatprism/compare/v0.3.3...v0.3.4) - 2022-02-17

### Fixed

- Fixed an issue where filters wouldn't show planes with specific statuses ([#92](https://github.com/marvk/vatprism/issues/92))

## [0.3.3](https://github.com/marvk/vatprism/compare/v0.3.2...v0.3.3) - 2022-02-13

### Fixed

- ~~Fixed an issue where filters wouldn't show planes with specific statuses ([#92](https://github.com/marvk/vatprism/issues/92))~~

### Security

- Update to Log4j (2.17.0 -> 2.17.1)

## [0.3.2](https://github.com/marvk/vatprism/compare/v0.3.1...v0.3.2) - 2022-01-30

### Fixed

- VATprism no longer crashes on startup when the API serves bad `logon_time` client data

## [0.3.1](https://github.com/marvk/vatprism/compare/v0.3.0...v0.3.1) - 2022-01-02

### Added

- Changelog button in the toolbar

### Fixed

- Fixed parsing issue with invalid fuel remaining and enroute times preventing application startup

## [0.3.0](https://github.com/marvk/vatprism/compare/v0.2.0...v0.3.0) - 2021-12-28

### Added

- Ability to turn off labels on uncontrolled airports and uncontrolled airports with arrivals and/or departures
- Added button to close the preloader after an issue occurred ([#66](https://github.com/marvk/vatprism/issues/66))
- Added tooltip for airline information like name, ICAO, callsign and country when hovering over the callsign in the flight detail view
- Context menus now correctly reflect what is painted on the map ([#53](https://github.com/marvk/vatprism/issues/53))
- Context menus now communicate how many items of a certain type aren't displayed if there are too many items inside the
  selection circle ([#53](https://github.com/marvk/vatprism/issues/53))
- Added context menu preferences that allow a user to enable certain categories of items to be always shown in context
  menu regardless of whether they are painted on the map
- Added preference for the application to remember its last position on
  startup ([#53](https://github.com/marvk/vatprism/issues/55))
- Added countries to FIR table in upper UIR detail view
- Added indicator for FIRs with active controller to FIR table in upper UIR detail view
- Accelerated requests to the VATSIM API
- Accelerated startup times by caching unchanged static map data
- Added toggle to enable and disable static map data cache

### Changed

- Reduced automatic refresh rate to 15 seconds from 30 seconds to match VATSIM api refresh
  rate ([#68](https://github.com/marvk/vatprism/issues/68))
- Debounced preferences file writing: Previously, the file would be written on every preference change, now, when
  multiple changes are made in quick succession, writing will take place only if no changes are detected for one second
  or when the application shuts down gracefully
- VATprism is now sending a non-default user agent to the VATSIM API
- VATprism will now cache static map data by default while still updating map data when required
- Slightly darkened info hint font in preferences to increase contrast

### Fixed

- VAT-Spy color scheme delivery and ground colors swapped ([#57](https://github.com/marvk/vatprism/issues/57))
- Fixed various issues with context menus not being displayed correctly
- Fixed incorrect label in airport painter preference dialog

### Security

- Updated various dependencies

## [0.2.1](https://github.com/marvk/vatprism/compare/v0.2.0...v0.2.1) - 2021-12-18

### Security

- Critical update to Log4j (2.14.1 -> 2.17.0) preventing the [Log4Shell](https://en.wikipedia.org/wiki/Log4Shell) exploit 

## [0.2.0](https://github.com/marvk/vatprism/compare/v0.1.0...v0.2.0) - 2021-09-17

### Added

- macOS releases
- Keybindings for panning and zooming the map
- Ability to load local data files by placing `VATSpy.dat` and/or `FIRBoundaries.dat` inside the config directory

### Removed

- Outlines (strokes) on the world and lakes have been disabled due to performance issues and artifacts on the map

### Fixed

- Map now updates automatically when updating filter settings
- Background can't be disabled anymore and is now always painted at maximum
  opacity ([#48](https://github.com/marvk/vatprism/issues/48))
- "No Controllers" no longer being squished on high traffic volume
  airports ([#42](https://github.com/marvk/vatprism/issues/42))

## [0.1.0](https://github.com/marvk/vatprism/releases/tag/v0.1.0) - 2021-09-13

Initial release

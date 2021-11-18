# Changelog

## [0.3.0](https://github.com/marvk/vatprism/compare/v0.2.0...v0.3.0) - Unreleased

### Added

- Ability to turn off Labels on uncontrolled Airports and uncontrolled Airports with arrivals and/or departures
- Added button to close the preloader after an issue occurred ([#66](https://github.com/marvk/vatprism/issues/66))

### Changed

- Reduced automatic refresh rate to 15 seconds from 30 seconds to match VATSIM api refresh
  rate ([#68](https://github.com/marvk/vatprism/issues/68))

### Fixed

- VAT-Spy color scheme delivery and ground colors swapped ([#57](https://github.com/marvk/vatprism/issues/57))

## [0.2.1](https://github.com/marvk/vatprism/compare/v0.2.0...v0.2.1) - 2021-12-18

### Security

- Critical: Updated Log4j to 2.17.0 to prevent the [Log4Shell](https://en.wikipedia.org/wiki/Log4Shell) exploit 

## [0.2.0](https://github.com/marvk/vatprism/compare/v0.1.0...v0.2.0) - 2021-09-17

### Added

- macOS releases
- Keybindings for panning and zooming the map
- Ability to load local data files by placing `VATSpy.dat` and/or `FIRBoundaries.dat` inside the config directory

### Removed

- Outlines (Strokes) on the World and Lakes have been disabled due to performance issues and artifacts on the map

### Fixed

- Map now updates automatically when updating filter settings
- Background can't be disabled anymore and is now always painted at maximum
  opacity ([#48](https://github.com/marvk/vatprism/issues/48))
- "No Controllers" no longer being squished on high traffic volume
  airports ([#42](https://github.com/marvk/vatprism/issues/42))

## [0.1.0](https://github.com/marvk/vatprism/releases/tag/v0.1.0) - 2021-09-13

Initial release
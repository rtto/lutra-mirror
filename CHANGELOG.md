# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [0.5.0] - ???

This version is a complete source rewrite of previous versions of Lutra.

### Added
- Support most of the new [mOTTR spec](https://dev.spec.ottr.xyz/mOTTR/develop/)
  - Support default values
  - Support zipMin and zipMax expansion modes
  - Support non-blank flag with corresponding checks
  - Support signatures and base templates
  - An internal representation of templates, instances, terms, etc. corresponding to their definitions in the spec
- Support the new type system according to [rOTTR spec](https://dev.spec.ottr.xyz/rOTTR/develop/)
- Support the new [wOTTR spec](https://dev.spec.ottr.xyz/wOTTR/develop/)
- Support many sanity checks before expansion (cycles, unedefined templates, etc.)
- An internal dependency graph, giving a more scalable expansion mechanism
- Parsing and writing based on Java Streams, allowing easy parallelisation

### Changed
- Complete rewrite of the source code
- A new CLI with similar, yet different flags

### Removed
- Support for writing the old (legacy) version of wOTTR (still support reading)

### Unsupported (from specs)
- mOTTR (version 0.?.?)
  - Annotation instances
  - Type-checking instances
  - Type-checking default values
- stOTTR (version 0.?.?)
  - Cannot parse stOTTR syntax
  - Only partial support for writing stOTTR syntax
- tabOTTR (version 0.?.?)
  - Cannot write (only read) tabOTTR syntax

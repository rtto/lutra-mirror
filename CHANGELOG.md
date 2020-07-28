# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.6.8-SNAPSHOT

### Added

- Improved docttr.

### Bug fixes

- Include hostname and URI fragment in output folder hierarchy, #206 

### Dependencies

- Bump all github reported dependencies, except jena 3.15.
- Replaced RDFVizler dependency with guru.nidi.graphviz-java 

## 0.6.7

### Added

- Introduced docttr documentation format for templates. In prototypical state.

### Dependencies

- Bump all github reported dependencies

### Bug fixes

- Expansion of ListTerms, #235
- Correct location of local copy of tpl.ottr.xyz standard library

## 0.6.6

### Added

- Introduced api module 
- Improved parsing for wOTTR and stOTTR using Builerpattern

### Removed

- Removed support for legacy format


## 0.6.5

### Bug fixes

 - Fix possible infinite loop when fetching online templates
 - Fix slow/frozen expansion by efficient handling of traces

## 0.6.4

### Added

 - Reintroduce type checking instances from 0.6.2

## 0.6.3

### Added

- Minor improvements to weblutra
- Adding unit tests for pOTTR's stOTTR files

### Removed

- Revert Core Type checking instances from 0.6.2, bugs found

## 0.6.2

### Added

- Core Type checking instances
- CLI: Support loading multiple libraries
- WebLutra: preload tpl.ottr.xyz library

## 0.6.1

### Added

- WebLutra improvements
  - support file upload
  - support tabOTTR and bOTTR

### Bugs fixes

- bOTTR
  - fix handling of language tagged literals
  - fix getting parent directory of bOTTR map for resolving path to
    relative sources

- tabOTTR
  - add default prefixes in the case of no prefix instruction
  - handling unsupported spreadsheet functions better by
    printing list of supported functions.

## 0.6.0

### Added
- Support for stOTTR for both reading and writing, and both instances and templates
- CLI-flag to set format for template library fetched online
- Initial support for bOTTR
- WebLutra, java servlet and HTML + JS frontend for testing and learning
- Bug fixes

### Changed
- Switched the flags `-l` and `-L` for consistency
- Removed default `.ttl`-filter on library files
- Now only writes the used prefixes for template libraries
- No longer adds the definition of Triple-template into written libraries
- Improved formatting of RDF output

### Unsupported (from specs)
- mOTTR (version 0.?.?)
  - Annotation instances
  - Type-checking instances
  - Type-checking default values
- Cannot write (only read) tabOTTR syntax

## 0.5.0

This version is a complete source rewrite of previous versions of Lutra.

### Added
- Support most of the new [mOTTR spec](https://dev.spec.ottr.xyz/mOTTR/develop/):
  - default values
  - zipMin and zipMax expansion modes
  - non-blank flag with corresponding checks
  - signatures and base templates
  - internal representation of templates, instances, terms, etc. corresponding to their definitions in the spec
- New type system according to [rOTTR spec](https://dev.spec.ottr.xyz/rOTTR/develop/)
- Support the new [wOTTR spec](https://dev.spec.ottr.xyz/wOTTR/develop/)
- Many sanity checks before expansion (cycles, undefined templates, etc.)
- Internal dependency graph, giving a more scalable expansion mechanism
- Parsing and writing based on Java Streams, allowing easy parallelisation

### Changed
- Complete rewrite of the source code
- A new CLI with similar, yet different flags

### Deprecated
- Reading the old (legacy) version of wOTTR

### Removed
- Writing/Outputting the old (legacy) version of wOTTR
- Output and sanity checks using OWLAPI
- CLI options removed:
  - `-noCheck` and `-noOWLOutput`, due to removal of OWLAPI
  - `-noCache`, replaced by new internal dependency graph

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

## 0.2--0.4

No versions between 0.1 and 0.5 have been publicly released.

## 0.1

Proof of concept implementation.

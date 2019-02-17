# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.5.0 -- WIP 

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
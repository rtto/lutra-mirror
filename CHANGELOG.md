# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.6.17 - 2022.04.10

Milestone: https://gitlab.com/groups/ottr/lutra/-/milestones/2

Added several checks for prefixes, datatypes and literals. Enhancement and bug fix of list types.

Closed issues:

  - stOTTR prefix parsing #415
  - Check for validity of typed values #414
  - Unknown datatypes #412
  - [ERROR] List expander applied to non-list argument: ++ottr:none #345
  - Irrelevant prefixes in docttr output #274
  - Give warning if using different or old `ottr:` prefix than the current #202
  - check prefixes #51

## 0.6.16 - 2022.02.06

Milestone: https://gitlab.com/ottr/lutra/lutra/-/milestones/21

Closed issues:

 - Multiple test sets: unit tests and integration tests #400
 - Ensure stable use of external non-code dependencies #388
 - Streamed processing of bOTTR-maps #351
 - Parameterise deployment of lutra.jar to ottr.xyz with project name #407
 - # [INFO] Fetched template: -- really?! #399
 - Build should not fail because remote resources are not available #391
 - Docker image in container registry is replaced for new versions #387
 - Write javadoc package descriptions for modules #385
 - Check if commons-codec from poi still causes problems with jena #361

## 0.6.15 - 2022.12.05

Milestone: https://gitlab.com/ottr/lutra/lutra/-/milestones/20

Closed issues:

 - NullPointerException on extra commas in instance arguments #393
 - SLF4J error #338
 - Formatting wottr to illegal stottr #242
 - Error in stOTTR spec: no-value list not allowed #371
 - Automate release procedure #369
 - Check if antlr dependency in stottr module can be removed from artifacts #358
 - Avoid fetching ottr:Triple #333
 - Generate javadoc #84
 - Switch project to Junit 5 #360
 - Represent parameter variables as IRIs, not blank nodes #282
 - Unexpected error handling (drop dot in stOTTR statements?) #231


## 0.6.14 - 2022.10.03

Milestone: https://gitlab.com/ottr/lutra/lutra/-/milestones/19

Closed issues:

 - CLI help text outdated? #372
 - Create release plan and procedure #370
 - Missing colon after prefix gives NullPointerException    #367
 - Error expanding wOTTR instance    #334
 - Accepting whitespace in URI #323
 - Default list values in stOTTR    #297
 - Dependency graph does not return all Result.errors?  #203
 - Give error for multiple definitions of template    #251
 - duplicate class: xyz.ottr.lutra.stottr.antlr.*    #375
 - Error deploying maven artifacts to maven central    #368
 - Check if unused dependencies could be removed    #359
 - bottr error with empty sql select #354
 - Extend testing in DataValidator class #343
 - lutra-restapi: hard coded MAX_FILE_SIZE and MAX_REQUEST_SIZE

## 0.6.13 - 2022.03.14

### Features

 - Improved error checking of IRIs, #323
 - Error messages written to console as #-prefixed comments
 - Improving error message phrasing
 - Removing jQuery from javadoc due to reported vulnerability issues
 - Removing some INFO messages that cluttered output

### Bug fixes

 - Correct character encoding WebLutra, #342

## 0.6.12 - 2022.01.14

### Added

 - Redesign of internal template store: #308, #319

### Bug fixes

 - #328: log4j security updates
 - #327: incomplete expansion when output to console
 - #302: Bump to Jena v4
 - Logging: #216, #152, #314
 - #298: Docttr sometimes fails on blank nodes

## 0.6.11

### Added

 - #20 initial support for "direct writing" of expanded instances.

### Bug fixes

 - !140 Fix bug where multiple ottr:none values in template body would lead to type error.
 - #228 Add check to format manager for empty formats
 - #298 Simplistic fix for "docttr fails on blank node types"
 - #252 No error message when wrong input file
 - !142 Fix bug where messages were not printed if using wottr format.
 - #300 build bug "spotbugs-sast fails"

## 0.6.10

Do not fail build due to errors reported by javadoc.


## 0.6.9

### Added

- Support for reading and writing annotations.
- Improved docttr, support annotations.

### Bug fixes

- Remove WARNING reported when ottr: vocabulary elements are used as arguments, #268

### Dependencies

- Bump all github reported dependencies

## 0.6.8

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

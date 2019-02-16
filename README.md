# Lutra – Reasonable Ontology Template (OTTR) reference implementation

Detailed documentation on the OTTR framework can be found on our web site: https://ottr.xyz/.

Lutra is our open source reference implementation of the OTTR language available under an LGPL licence. Lutra can read and write OTTR templates and instances on different serialisation formats, and expand instances into regular RDF graphs or RDF/OWL ontologies. Lutra is written in Java 8 and is currently available for use as a command line interface.


## Running and installation

### Prerequisites

To run Lutra you will need to install Java 8, for instance [OpenJDK][1].

### Downloads

* [Releases][2]
* [Latest master snapshot][3]
* [Latest develop snapshot][4]

### Installing

Simply download the executable jar file. No installation required.

Run the jar file with for example: `java -jar lutra.jar --help`.

## Versioning

We use [Semantic Versioning][8].

See the [tags on this repository][6] for release versions and other important milestones. 

See [CHANGELOG](CHANGELOG.md) for description of major changes between versions. 

## Building

To build, clone the project and use Maven to build: `mvn package`. This will produce the executable jar at `lutra-cli/target/lutra.jar`.

Requirements:

* Java 8, for instance [OpenJDK][1]
* [Maven](https://maven.apache.org/) - Dependency Management

The project depends on the following external libraries:

* [Apache Jena](https://jena.apache.org/) for RDF processing
* Various Apache Commons libraries for utility functionality:
  * [Collections](https://commons.apache.org/proper/commons-collections/)
  * [Math](http://commons.apache.org/proper/commons-math/)
  * [IO](https://commons.apache.org/proper/commons-io/)
  * [Validator](https://commons.apache.org/proper/commons-validator/)
* [Apache POI](https://poi.apache.org/) for handling spreadsheet files

See [pom.xml](pom.xml) for details.

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for information on how to best contribute to the project.

## Maintainers

* [Martin G. Skjæveland](http://folk.uio.no/martige/)
* [Leif Harald Karlsen](http://folk.uio.no/leifhka/)

See also the list of [CONTRIBUTORS](CONTRIBUTORS.md) who participated in this project.

## License

This project is licensed under the GNU Lesser General Public License v2.1 - see the [LICENSE.txt](LICENSE.txt) file for details.

## Acknowledgments

The project is supported by the [Department of Informatics][9] at University of Oslo and the [SIRIUS Centre][10] for Research-driven Innovation.


[1]:https://openjdk.java.net/install/index.html
[2]:https://gitlab.com/ottr/lutra/lutra/releases
[3]:https://gitlab.com/ottr/lutra/lutra/builds/artifacts/master/raw/lutra.jar?job=snapshot
[4]:https://gitlab.com/ottr/lutra/lutra/builds/artifacts/develop/raw/lutra.jar?job=snapshot
[5]:https://docs.gitlab.com/ee/gitlab-basics/add-merge-request.html
[6]:https://gitlab.com/ottr/lutra/lutra/tags
[7]:https://gitlab.com/ottr/lutra/lutra/graphs/master
[8]:https://semver.org
[9]:https://www.ifi.uio.no
[10]:https://sirius-labs.no

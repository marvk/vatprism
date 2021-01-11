<p align="center"><img src="https://i.imgur.com/orfmevM.png"  alt="logo"/></p>

# VATprism

Welcome to the VATprism repository! VATprism is a data explorer for [VATSIM](https://www.vatsim.net/), the
**V**irtual **A**ir **T**raffic **Sim**ulation Network. VATprism allows users to explore available ATC services,
connected pilots, Airports, Flight and Upper Information Regions and more!

VATprism was born out of a desire to simplify, and make more customizable, access to VATSIM data and is inspired by the
excellent [VAT-Spy](http://www1.metacraft.com/VATSpy/), which, as of this time, unfortunately remains closed source.

VATprism uses the [VAT-Spy Client Data Update Project](https://github.com/vatsimnetwork/vatspy-data-project) as a source
of Data.

#### Airport Detail

![Screenshot1](https://i.imgur.com/oSE1y7s.png)

#### Airport Table

![Screenshot2](https://i.imgur.com/Py7kWgq.png)

#### Pilot Detail

![Screenshot3](https://i.imgur.com/4GYHvDe.png)

#### Full text search!

![Screenshot4](https://i.imgur.com/RKEhpk4.png)

#### Fully customizable!

![Screenshot5](https://i.imgur.com/GkxMnGy.png)

# Issues

Issue tracking takes place on this GitHub issue repository. You are most invited to contribute bugs, issues, feature or
any other constructive feedback [as an issue.](https://github.com/marvk/vatsim-map/issues) Please check existing issues
before creating a new one.

# Build status ![Build Status](https://github.com/marvk/vatsim-map/workflows/Build/badge.svg)

If you want to build the project yourself, you require

* [Apache Maven](https://maven.apache.org/)

* [JDK 15+](https://adoptopenjdk.net/)

* [WiX Toolset](https://wixtoolset.org/) (For building a Windows Installer via `jpackage`)

To build the project, run clone the repository and run `mvn package`. Currently, this will fail if WiX Toolkit is not
installed. To disable the Windows Installer build, removed the Exec Maven Plugin from the `pom.xml`.
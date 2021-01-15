<p align="center"><img src="https://i.imgur.com/orfmevM.png"  alt="logo"/></p>

# VATprism

Welcome to the VATprism repository! VATprism is a data explorer for [VATSIM](https://www.vatsim.net/), the
**V**irtual **A**ir **T**raffic **Sim**ulation Network. VATprism allows users to explore available ATC services,
connected pilots, Airports, Flight and Upper Information Regions and more!

## Motivation

VATprism was born out of a desire to simplify, and make more customizable, access to VATSIM data and is inspired by the
excellent [VAT-Spy](http://www1.metacraft.com/VATSpy/), which, as of this time, unfortunately remains closed source.

## Screenshots

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

## Issues

Issue tracking takes place on this GitHub issue repository. You are most invited to contribute bugs, issues, feature or
any other constructive feedback [as an issue.](https://github.com/marvk/vatprism/issues) Please check existing issues
before creating a new one.

## Installation

#### Windows

Simply download the latest installer (`.msi`) from the [releases page](https://github.com/marvk/vatprism/releases)
and run it. The installer will guide you through the installation.

If you have another version of VATprism installed, the installer may prompt you to remove an existing version. In this
case, head to Windows' *add or remove programs* dialog, remove your existing version, and rerun the installer. Don't
worry, your settings will not be removed.

#### Linux and macOS

Currently, there is no support for native linux or macOS binaries. It is still possible to run VATprism with `java` by
downloading the `.jar` from the [releases page](https://github.com/marvk/vatprism/releases) and
running `java -jar vatprism-VERSION.jar`. This requires an installation of JDK 15+ or JRE 15+, which are available on
the [AdoptOpenJDK website.](https://adoptopenjdk.net/index.html)

If there is demand for macOS or Linux native binaries in the future, I will think about adding support. Feel free to
request [Linux](https://github.com/marvk/vatprism/issues/31) or [macOS](https://github.com/marvk/vatprism/issues/30)
builds via the linked issues.

## Build ![Build Status](https://github.com/marvk/vatprism/workflows/Build/badge.svg)

If you want to build the project yourself, you require

* [Apache Maven](https://maven.apache.org/)

* [JDK 15+](https://adoptopenjdk.net/)

* [WiX Toolset](https://wixtoolset.org/) (For building a Windows Installer via `jpackage`)

To build the project, run clone the repository and run `mvn package`. Currently, this will fail if WiX Toolkit is not
installed. To disable the Windows installer build, removed the Exec Maven Plugin from the `pom.xml`.

## Acknowledgements

VATprism uses the [VAT-Spy Client Data Update Project](https://github.com/vatsimnetwork/vatspy-data-project) as a source
of Data.
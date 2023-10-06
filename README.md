<div align="center">

# CEREI
<!-- ![version](https://img.shields.io/badge/version-1.0-blue) ![License](https://img.shields.io/cran/l/NCC) -->
 
</div>

**CEREI** is an open-source Java desktop application designed to model the energy market using spot market charges, network and retailer tariffs, including associated losses for cost-effective renewable energy investments.

CEREI presents a number of novelties and advantages:
- To help industrial and commercial customers to reduce their electrical energy bills and the distribution of the on-site generation behind-the-meter.
- To help decision-makers within the industrial and commercial organisations to identify cost-effective renewable energy solutions.
- To support the integration of renewable energy sources into existing power systems, and to facilitate the transition to a more sustainable energy system.

## Table of Contents
<!-- TOC generated with https://freelance-tech-writer.github.io/table-of-contents-generator/index.html -->
   * [Contents of the repo](#contents-of-the-repo)
   * [Building CEREI from source code](#building-cerei-from-source-code)
   * [Tool Installation](#tool-installation)
       * [Installation on systems with jdk 18, openjdk 18 or later installed](#installation-on-systems-with-jdk-18-openjdk-18-or-later-installed)
       * [Installation of jdk or openjdk](#installation-of-jdk-or-openjdk)
       * [Installation of standalone CEREI (Windows only)](#installation-of-standalone-cerei-windows-only)
   * [System architecture](#system-architecture)
   * [User interface](#user-interface)
   * [CERI operation](#ceri-operation)
   * [Documentation](#documentation)
   * [Developers](#developers)
   * [Get involved with the community](#get-involved-with-the-community)
       * [Bug reporting and bug fixing](#bug-reporting-and-bug-fixing)
       * [New features and enhancements](#new-features-and-enhancements)
   * [License](#license)
   * [Acknowledgements](#acknowledgements)
   * [Citation](#citation)

## Contents of the repo
- The [src](https://github.com/uts-isf/CEREI/tree/main/src/main/java) folder: It contains the followings:
    - [./main/java/](https://github.com/uts-isf/CEREI/tree/main/src/main/java): It contains `module-info.java` file which is used to create the minimal java runtime environment so that the program can run without the need to install java.
    - [./main/java/au/org/nifpi/cerei/](https://github.com/uts-isf/CEREI/tree/main/src/main/java/au/org/nifpi/cerei): It contains the source code.
- The [build](https://github.com/uts-isf/CEREI/tree/main/build) folder: It contains `cerei.jar` file used to create the `cerei.exe` file and it contains the [classes](https://github.com/uts-isf/CEREI/tree/main/build/classes/au/org/nifpi/cerei) folder which has the templates used to create objects and to define object data types and methods in the tool.
- The [tools](https://github.com/uts-isf/CEREI/tree/main/tools) folder: It contains `CEREI.xml` file which is used by Launch4j to create the `cerei.exe` file from the `cerei.jar` file (`cerei.jar` is created from the source code and it is stored in ).
- The [docs](https://github.com/uts-isf/CEREI/tree/main/docs) folder: It contains the source code documentation produced by Javadoc (APIs that provide documentation for subsequent developers).
- The [app](https://github.com/uts-isf/CEREI/tree/main/app) folder: It contains the executable file and the associated folders for the current version.
- The [blank_templates](https://github.com/uts-isf/CEREI/tree/main/blank_templates) folder: It contains blank templates of the seven input files, allowing users to customize the simulation according to their specific needs by populating with their own data. For more information how to fill them up, see [Documentation](#documentation).
- The [sample_data](https://github.com/uts-isf/CEREI/tree/main/sample_data) folder: It contains a set of sub-folders containing input files specifically tailored for 15 different case study scenarios. These input files are carefully crafted from real-world data and represent various scenarios, each representing a unique use case.
- The [supporting_docs](https://github.com/uts-isf/CEREI/tree/main/supporting_docs) folder: It contains a set of sub-folders containing the images of the tool, help file and general contribution guidelines.

## Building CEREI from source code
Building CEREI from source code requires technical knowledge of developing java applications.  The following describes the method used to build the version on the tool on the repository, although other methods can be used.
- Download the source code from the repository, which can be found in the [src](https://github.com/uts-isf/CEREI/tree/main/src/main/java) folder.
- Compile the code.
- Create a .jar file.
- Optional - Create a java run-time environment that contains the java base and desktop modules as a minimum. The repository has a functional jre in the sub-directory `smalljre`.
- Optional - Use Launch4j (or similar) to create a Windows executable that references the small jre.  The repository has a launch4j configuration file (at [tools](https://github.com/uts-isf/CEREI/tree/main/tools)/`CEREI.xml`) that can be edited and used with launch4j.
-  Optional - Create a .zip archive that contains the Windows executable and two sub-directories:
    - **smalljre** (or as configured in the launch4j configuration file) - that contains the java run-time environment.
    - **help** - that contains html help instructions.  This directory must contain `CEREI.htm` as the initial entry point into the help instructions. 

## Tool Installation

### Installation on systems with jdk 18, openjdk 18 or later installed
Download CEREI.jar from the repository. CEREI.jar has been tested with openjdk 18, 19 and 20.  The jar file can be run from a terminal/command window using the command:

`java -jar CEREI.jar`

It is also possible to run CEREI by clicking on CEREI.jar by changing the configuration of your device. Follow the relevant instructions provided by the manufacturer of your device and/or operating system.

Note that CEREI was developed using jdk 18 and has been tested with openjdk19 and openjdk 20. 

### Installation of jdk or openjdk
Install jdk 20 (or later) from this [link](https://www.oracle.com/au/java/technologies/downloads) or openjdk 20 (or later) from [link](https://openjdk.org/projects/jdk/), taking note and complying with the licence conditions and instructions associated with jdk or openjdk as appropriate.

You may need administrative or superuser (root) privileges to install jdk or openjdk.
Run CEREI as described in Subsection **Installation on systems with jdk 18, openjdk 18 or later installed** above.

### Installation of standalone CEREI (Windows only)
Download the version folder from [app](https://github.com/uts-isf/CEREI/tree/main/app) folder, which is currently [v1.0](https://github.com/uts-isf/CEREI/tree/main/app/v1.0). Double-click on CEREI.exe to run the tool.
You do not need administrative privileges to install and run CEREI using this method.

## System architecture

![Conceptual framework](https://github.com/uts-isf/CEREI/blob/main/supporting_docs/images/main_images/Conceptual_framework.png)

The tool is composed of several internal modules, receives a number of files as inputs, and allows the user to monthly, quarterly and yearly energy bills, price efficiency index mapping, potential saving as well as the life-cycle cost assessment with and without DER, as outputs.

## User interface

![CERI_Interface](https://github.com/uts-isf/CEREI/blob/main/supporting_docs/images/main_images/CERI_Interface.png)

## CERI operation

CEREI can be operated in two simple steps:
- **Step 1:** Click on each ***button***, within the ***red*** highlighted box in the Figure below, and select the relevant input file.
- **Step 2:** Click ***Calculate***. This is highlighted by the ***blue*** font button in the Figure below. This will generate the output(s) in the relevant tab(s) and produce a pop-up, stating “*Calculation Complete. Results in relevant tabs*”.

The user needs to click on the “***Reset Input Files***” button to reset all input files.

If any individual file(s) needs to be replaced, then only the relevant button(s) (within the ***red*** highlighted box in the Figure below) needs to be clicked to select the new file.
    
CEREI generates four outputs, highlighted by the ***green*** box. Therefore, to generate the output(s), the tool allows for seven user inputs, which are highlighted by the ***red*** box Each of these inputs and outputs is discussed in detail in [Documentation](#documentation).

![CERI_Interface_Marked](https://github.com/uts-isf/CEREI/blob/main/supporting_docs/images/main_images/CERI_Interface_Marked.png)

## Documentation
More documentation on CEREI can be found at the following <a href="https://github.com/uts-isf/CEREI/wiki/CEREI:-Help-File">link</a> and in [help_file](https://github.com/uts-isf/CEREI/tree/main/supporting_docs/help_file) folder.

## Developers
Created on March 29, 2023
Created by:
- <a href="https://github.com/Ibrahim-a-Ibrahim" target="_blank">Ibrahim Anwar Ibrahim</a>
- <a href="https://github.com/etanvah" target="_blank">Tanveer Choudhury</a>
- <a href="https://github.com/james007au" target="_blank">James Sargeant</a>

## Get involved with the community

### Bug reporting and bug fixing
You can help us by submitting bug reports or fixing bugs in the [CEREI issue tracker](https://github.com/uts-isf/CEREI/issues).

### New features and enhancements
If you wish to contribute patches you can:

- [fork the repo](https://docs.github.com/en/get-started/quickstart/fork-a-repo)
- make your changes
- commit to your repository
- and then [create a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request-from-a-fork).
The development team can then review your contribution and commit it upstream as appropriate.

If you commit a new feature, add [FEATURE] to your commit message AND give a clear description of the new feature. The label Needs documentation will be added by maintainers and will automatically create an issue on the CEREI-Documentation, where you or others should write documentation about it.

CEREI tool requires future updates to extend its functionality as follows:
- The tool was developed using 30-minute time interval and then converting the data to be hourly. Therefore, the energy usage, spot market price, and FIT should be imported with 30-minute time interval. The tool can be extended to standardise the time interval based on a certain reference.
- The tool does not support the modeling of the energy source. The user should use other modeling tool to generate the time-series energy output with a timeframe matches the demand.
- The tool has been developed and tested using industrial tariff structures including TOU tariff structure (e.g., peak, shoulder, off-peak tariffs). Accordingly, it is not including flat tariffs at this stage.
- The tool is not supporting the network reliability, stability, and constraints analysis. Therefore, it considers that all of the excess energy can be exported to the grid without any curtailment, which needs to be updated in the later version.

## License
This project has been developed in collaboration between [UTS](https://www.uts.edu.au/) and [Federation University](https://federation.edu.au/) and it is licensed under the terms of the <a href="https://github.com/uts-isf/CEREI/blob/main/LICENSE">MIT License</a>.

## Acknowledgements
This work was funded by the [National Institute for Forestry Products Innovation (NIFPI)](https://nifpi.org.au/) within the project number NV063 [grant number NIF146-2122]; and partially supported by [Centre for New Energy Transition Research](https://federation.edu.au/research/research-centres/cfnetr).

## Citation
TBC.

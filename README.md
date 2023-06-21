<div align="center">

  # RECRNTAzero
  ![version](https://img.shields.io/badge/version-1.0-blue) ![License](https://img.shields.io/cran/l/NCC)
  
</div>

**RECRNTAzero** is an open-source Java desktop application designed to model the energy market using network and retailer tariffs, including associated losses.

RECRNTAzero presents a number of novelties and advantages:
- To help industrial and commercial customers to reduce their energy costs and improve their environmental performance.
- To help decision-makers within the industrial and commercial organisations to identify cost-effective renewable energy solutions.
- To support the integration of renewable energy sources into existing power systems, and to facilitate the transition to a more sustainable energy system.

## Building RECRNTAzero from source code
Building RECRNTAzero from source code requires technical knowledge of developing java applications.  The following describes the method used to build the version on the tool on the repository, although other methods can be used.
- Download the source code from the repository.
- Compile the code.
- Create a .jar file.
- Optional - Create a java run-time environment that contains the java base and desktop modules as a minimum.  The repository has a functional jre in the sub-directory \texttt{smalljre}.
- Optional - Use Launch4j (or similar) to create a Windows executable that references the small jre.  The repository has a launch4j configuration file (at dist/energyCalculator3.xml) that can be edited and used with launch4j.
-  Optional - Create a .zip archive that contains the Windows executable and two sub-directories:
    - **smalljre** (or as configured in the launch4j configuration file) - that contains the java run-time environment.
    - **help** - that contains html help instructions.  This directory must contain \texttt{EnergyCalculator.htm} as the initial entry point into the help instructions. 


## Tool Installation
### Installation on systems with jdk 18, openjdk 18 or later installed
Download RECRNTAzero.jar from the repository. RECRNTAzero.jar has been tested with openjdk 18, 19 and 20.  The jar file can be run from a terminal/command window using the command:

`java -jar RECRNTAzero.jar`

It is also possible to run RECRNTAzero by clicking on RECRNTAzero.jar by changing the configuration of your device.  Follow the relevant instructions provided by the manufacturer of your device and/or operating system.

Note that RECRNTAzero was developed using jdk 18 and has been tested with openjdk19 and openjdk 20. 

### Installation of jdk or openjdk
Install jdk 20 (or later) from this [link](https://www.oracle.com/au/java/technologies/downloads) or openjdk 20 (or later) from [link](https://openjdk.org/projects/jdk/), taking note and complying with the licence conditions and instructions associated with jdk or openjdk as appropriate.

You may need administrative or superuser (root) privileges to install jdk or openjdk.
Run RECRNTAzero as described in Subsection **Installation on systems with jdk 18, openjdk 18 or later installed** above.

### Installation of standalone RECRNTAzero (Windows only)
Download the RECRNTAzero.zip file from the repository.  Extract all the files and subdirectories to a known location.  Double-click on RECRNTAzero.exe to run the tool.
You do not need administrative privileges to install and run RECRNTAzero using this method.

## User interface

![Tool_Interface](https://github.com/uts-isf/RECRNTAzero/assets/63223580/c460fddf-3dd5-455c-a976-aba4e432a345)

## System architecture

![Conceptual framework of RECRNTAzero](https://github.com/uts-isf/RECRNTAzero/assets/63223580/7ea38d52-04f9-4ee4-8d32-f337a7add722)

The tool is composed of several internal modules, receives a number of files as inputs, and allows the user to monthly, quarterly and yearly energy bills, price efficiency index mapping, potential saving as well as the life-cycle cost assessment with and without DER, as outputs.

## Documentation
More documentation on ARGAEL can be found at the following <a href="https://github.com/uts-isf/RECRNTAzero/wiki">link</a>.

## Authors
Created on March 29, 2022  
Created by:
- <a href="https://github.com/Ibrahim-a-Ibrahim" target="_blank">Ibrahim Anwar Ibrahim</a>
- <a href="https://github.com/etanvah" target="_blank">Tanveer Choudhury</a>
- <a href="https://github.com/james007au" target="_blank">James Sargeant</a>

## License
This project is licensed under the terms of the <a href="https://github.com/uts-isf/RECRNTAzero/blob/main/LICENSE">MIT License</a>.

## Acknowledgements
This work is partially supported by the National Institute for Forestry Products Innovation (NIFPI) and Centre for New Energy Transition Research.

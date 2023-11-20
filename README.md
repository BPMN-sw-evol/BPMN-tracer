# BPMN-tracer
Software to parse a XML file describing a BPMN model for knowing information that helps developers to evolve the sw that supports the execution of the model. Mainly the execution links of the activities in the model. This code is used for models executed on the Camunda Engine. 

## Index

1. [Description](#description)
2. [Prerequisites](#prerequisites)
3. [Usage](#usage)


## Description

This software is designed to parse XML files describing BPMN (Business Process Model and Notation) models, providing developers with essential information to enhance the software supporting the execution of these models. The primary focus is on extracting details about the execution links of activities within the BPMN model. Specifically tailored for models executed on the Camunda Engine, this tool assists developers in understanding and working with the dynamic aspects of business processes encoded in BPMN, facilitating effective software development and maintenance.


## Prerequisites

To use this program you need the following:

1. **Version control system**: Install GIT from the [GIT official website](https://git-scm.com/downloads).


2. **IntelliJ IDEA or VSCODE**: To run and/or modify the project.

3. **Java 17 or higher**: You can get help to download and install the java version by following [this link](https://www.youtube.com/watch?v=oAin-q1oTDw&pp=ygUXY29tbyBjb25maWd1cmFyIGphdmEgMTc%3D)

4. **Maven 3.9**: You can get help to download and install the maven version by following [this link](https://www.youtube.com/watch?v=1QfiyR_PWxU&pp=ygUSaW5zdGFsYXIgbWF2ZW4gMy45)


## Usage

To use the program you must do:

1. Open a terminal in the folder where you want to download the program and clone it with:

   ```
   git clone https://github.com/BPMN-sw-evol/BPMN-tracer.git
   ```

2. Open the **BPMN-trace** program with IntelliJ or VSCODE and run it

3. The program will run an explorer window to select the .bpmn file to be analyzed.

4. If the .bpmn file does not have any problem in its structure, you can see its respective analysis
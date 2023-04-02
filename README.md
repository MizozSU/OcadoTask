# Ocado Task
Author: Michal Zwatrzko

## Project Overview
This project aims to solve the ISF Picking Scheduler problem using two unique methods:
- Google OR-Tools CP-SAT Solver
- Genetic Algorithm (implemented using Jenetics library)

### Method 1: Google OR-Tools CP-SAT Solver
The ISF Picking Scheduler problem can be modeled as an integer programming problem and solved using the state-of-the-art CP-SAT Solver provided by Google OR-Tools. This method results in a solution that is close to or optimal. However, the OR-Tools solver has some limitations:
- It cannot be used for very large problem sizes with a time constraint of 20 seconds
- The libraries are platform-dependent (aarch64, x64)

### Method 2: Genetic Algorithm (Jenetics library)
The genetic algorithm approach does not guarantee optimal solutions but can be used for either:
- Very large problem sizes
- Situations where the platform does not match one of the supported ones by Google's library

## Build Instructions
To build the project, execute the following Maven command:
```mvn package```
After building, a JAR file with dependencies will be generated in the `target` directory.

## Running the Application
To run the application, use the following command:
```java -jar <jar_file_path> <store_file_path> <orders_file_path>```
The schedule will be printed to the standard output.

# Acknowledgements
This project uses the following open-source libraries:
1. **Google OR-Tools**
   - Website: https://developers.google.com/optimization
   - License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)

2. **Jenetics**
   - Website: http://jenetics.io
   - License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)

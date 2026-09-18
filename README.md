# VITyarthi Project - Programming in Java

## Project Overview
This repository contains a comprehensive Java-based project featuring **SmartOrganizer**, a command-line tool to automatically sort and organize files in a directory into categorized folders (`Documents`, `Images`, `Videos`, `Audio`, `Archives`, `Code`, `Executables`) based on their extensions.

## Prerequisites
- **Java Development Kit (JDK)**: Version 8 or higher is required. Ensure `javac` and `java` are available in your system's PATH.

## Project Executability (Command Line Instructions)

As per the evaluation guidelines, the core functionality of this project is fully executable via the command line.

### Running the Smart Organizer
The `SmartOrganizer` reads files from a directory and categorizes them automatically.

**Compilation:**
```bash
javac SmartOrganizer.java
```

**Execution:**
By default, the organizer targets a directory named `./MessyFolder` in the current working directory. You can also specify a custom directory as an argument.

*Run with default directory:*
```bash
java SmartOrganizer
```

*Run with a specific target directory:*
```bash
java SmartOrganizer "C:\path\to\your\folder"
```


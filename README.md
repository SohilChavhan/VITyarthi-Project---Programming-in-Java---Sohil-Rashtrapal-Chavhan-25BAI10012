# VITyarthi Project - Programming in Java

## Project Overview
This repository contains a comprehensive Java-based project featuring a File Organizer system and a Java string manipulation challenge. It includes:
1. **SmartOrganizer**: A command-line tool to automatically sort and organize files in a directory into categorized folders (Documents, Images, Videos, Audio, Code, etc.) based on their extensions.
2. **ProductOnboarding**: A string manipulation challenge solution located in `Java Challenging Task.java`.

## Prerequisites
- **Java Development Kit (JDK)**: Version 8 or higher is required. Ensure `javac` and `java` are available in your system's PATH.

## Project Executability (Command Line Instructions)

As per the evaluation guidelines, the core functionality of this project is fully executable via the command line.

### 1. Running the Smart Organizer
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

### 2. Running the Java Challenging Task (Product Onboarding)
This is an interactive command-line script for metadata sanitation.

**Compilation:**
```bash
javac "Java Challenging Task.java"
```

**Execution:**
```bash
java ProductOnboarding
```
*Note: The class name inside the file is `ProductOnboarding`.*

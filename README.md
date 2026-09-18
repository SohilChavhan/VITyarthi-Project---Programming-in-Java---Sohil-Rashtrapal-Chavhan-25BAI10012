# VITyarthi Project - Programming in Java

## 📌 Project Overview
**SmartOrganizer** is a robust command-line Java application designed to clean up clutter by automatically scanning a target directory and categorizing unorganized files into structured folders (`Documents`, `Images`, `Videos`, `Audio`, `Archives`, `Code`, and `Executables`) based on their file extensions.

---

## 🛠️ Environment Setup & Prerequisites

### 1. Java Development Kit (JDK)
- **Required**: Java SE Development Kit (JDK) version **8 or higher**.
- Verify your Java environment by running the following commands in your terminal/command prompt:
  ```bash
  java -version
  javac -version
  ```
- If Java is not installed or not recognized, download and install JDK from [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [Eclipse Adoptium OpenJDK](https://adoptium.net/). Ensure `java` and `javac` are added to your system's `PATH`.

### 2. Repository Setup
Clone the repository to your local machine:
```bash
git clone https://github.com/SohilChavhan/VITyarthi-Project---Programming-in-Java---Sohil-Rashtrapal-Chavhan-25BAI10012.git
cd VITyarthi-Project---Programming-in-Java---Sohil-Rashtrapal-Chavhan-25BAI10012
```

---

## 📦 Dependencies & Installation
- **External Dependencies**: **None (0 dependencies)**.
- **Built-in Libraries**: Standard Java SE Libraries (`java.nio.file.*`, `java.util.*`, `java.io.*`).
- No build tools like Maven or Gradle are required. Standard `javac` compilation is all that is needed.

---

## ⚙️ Configuration & Categorization Rules

`SmartOrganizer` organizes files using an internal extension map:

| Category | Supported Extensions |
|---|---|
| **Documents** | `.pdf`, `.doc`, `.docx`, `.txt`, `.odt`, `.rtf`, `.xls`, `.xlsx`, `.ppt`, `.pptx`, `.csv` |
| **Images** | `.jpg`, `.jpeg`, `.png`, `.gif`, `.bmp`, `.svg`, `.webp`, `.ico`, `.tiff` |
| **Videos** | `.mp4`, `.mkv`, `.avi`, `.mov`, `.wmv`, `.flv`, `.webm` |
| **Audio** | `.mp3`, `.wav`, `.flac`, `.aac`, `.ogg`, `.wma` |
| **Archives** | `.zip`, `.rar`, `.7z`, `.tar`, `.gz`, `.bz2` |
| **Code** | `.java`, `.py`, `.js`, `.ts`, `.c`, `.cpp`, `.h`, `.cs`, `.html`, `.css`, `.xml`, `.json`, `.sql`, `.sh` |
| **Executables** | `.exe`, `.msi`, `.dmg`, `.deb`, `.rpm`, `.apk`, `.jar` |

*Note: Files without extensions or with unrecognized extensions will be safely skipped and left untouched.*

---

## 🚀 Step-by-Step Execution Guide

### Step 1: Compile the Project
Open a terminal in the root directory of the project and run:
```bash
javac SmartOrganizer.java
```
This produces `SmartOrganizer.class`.

---

### Step 2: Prepare a Target Folder for Testing
By default, `SmartOrganizer` looks for a folder named `./MessyFolder` in the current working directory.

#### On Windows (PowerShell):
```powershell
mkdir MessyFolder
New-Item MessyFolder/doc1.pdf, MessyFolder/pic1.png, MessyFolder/script.py, MessyFolder/song.mp3, MessyFolder/notes.txt, MessyFolder/unknown.xyz -ItemType File
```

#### On Linux / macOS (Bash):
```bash
mkdir -p MessyFolder
touch MessyFolder/doc1.pdf MessyFolder/pic1.png MessyFolder/script.py MessyFolder/song.mp3 MessyFolder/notes.txt MessyFolder/unknown.xyz
```

---

### Step 3: Run the Program

#### Option A: Running with Default Directory (`./MessyFolder`)
```bash
java SmartOrganizer
```

#### Option B: Running with a Custom Directory Path
You can pass any target folder path as a command-line argument:
```bash
# Windows
java SmartOrganizer "C:\Users\YourName\Downloads"

# Linux / macOS
java SmartOrganizer "/home/user/Downloads"
```

---

## 🖥️ Expected Output

### Console Logs:
```text
╔══════════════════════════════════════════════╗
║         📂  SmartOrganizer v1.0  📂         ║
╚══════════════════════════════════════════════╝

  Target directory : C:\path\to\project\MessyFolder

  [NEW]   Created folder: Audio/
  [MOVED] song.mp3  →  Audio/
  [NEW]   Created folder: Code/
  [MOVED] script.py  →  Code/
  [NEW]   Created folder: Documents/
  [MOVED] doc1.pdf  →  Documents/
  [MOVED] notes.txt  →  Documents/
  [NEW]   Created folder: Images/
  [MOVED] pic1.png  →  Images/
  [SKIP]  unknown.xyz  (unknown extension: .xyz)

──────────────────────────────────────────────
  Done!  Files moved: 5  |  Skipped: 1
──────────────────────────────────────────────
```

### Resulting Directory Structure:
```text
MessyFolder/
├── Audio/
│   └── song.mp3
├── Code/
│   └── script.py
├── Documents/
│   ├── doc1.pdf
│   └── notes.txt
├── Images/
│   └── pic1.png
└── unknown.xyz
```

---

## 📁 Repository Structure

```text
.
├── SmartOrganizer.java   # Main Java source file
├── README.md             # Project documentation and guide
└── .gitignore            # Git ignore configuration
```

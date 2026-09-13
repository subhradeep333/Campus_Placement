# 🚀 Campus Recruitment & Placement Portal (Java + SQLite + HTML/CSS/JS)

A full-stack Campus Placement and Job Recruitment Portal built in Java. Features role-based management for **Students** and **Companies**, student CV dossier submissions, job application tracking, candidate selection tools, an embedded **SQLite relational SQL database**, and a modern **Dark Glassmorphism Web Frontend**.

![Java](https://img.shields.io/badge/Java-26-orange.svg)
![Database](https://img.shields.io/badge/Database-SQLite3-blue.svg)
![Frontend](https://img.shields.io/badge/Frontend-HTML5%20%7C%20CSS3%20%7C%20JS-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

---

## ✨ Features

- **🎓 Student Portal**:
  - Account Registration & Login.
  - Interactive **CV Dossier Builder** (Skills, experience summary, attached resume file path).
  - Search & filter available job openings by keyword, technical skills, or company name.
  - One-click job applications with automated eligibility validation (Min CGPA cutoff & CV requirement).
  - Real-time **Application Tracker** (`PENDING ⏳`, `SHORTLISTED ⭐`, `ACCEPTED 🎉`, `REJECTED ✖`).

- **🏢 Company Portal**:
  - Account Registration & Login.
  - Job Listing Management (Title, description, salary package LPA, min CGPA criteria).
  - Received Application Management with clickable **Candidate CV Dossier Inspection Modal**.
  - Candidate status updater (`SHORTLIST`, `ACCEPT`, `REJECT`).

- **🌐 Dual Interface Support**:
  - **Web Frontend (`http://localhost:8080`)**: Built with standard Java `HttpServer` (`com.sun.net.httpserver.HttpServer`), custom JSON REST API endpoints, and a glassmorphism SPA UI.
  - **Terminal Console (CLI)**: Full menu-driven interactive terminal interface (`ConsoleUI`).

- **🗄️ Relational Database & Persistence**:
  - **SQLite SQL Database** (`data/placement_portal.db`) via JDBC (`java.sql.PreparedStatement`).
  - Auto-exported human-readable JSON summary files (`students.json`, `jobs.json`, `companies.json`, `applications.json`).

---

## 📁 Project Structure

```
.
├── build.sh                 # Compilation script
├── run.sh                   # Application launcher (Web Server + CLI)
├── lib/
│   ├── sqlite-jdbc.jar      # SQLite JDBC Driver
│   ├── slf4j-api.jar        # SLF4J Logging Facade
│   └── slf4j-simple.jar     # SLF4J Logger
├── src/
│   ├── Main.java            # Main entry point
│   ├── model/               # User, Student, Company, JobPosting, Application
│   ├── service/             # DatabaseService (SQLite JDBC), AuthService, RecruitmentService
│   ├── ui/                  # ConsoleUI & TerminalUtils
│   └── web/                 # WebServer REST API & Static File Router
└── web/
    ├── index.html           # Single Page Application
    ├── style.css            # Dark glassmorphism stylesheet
    └── app.js               # REST API Client & Dashboard logic
```

---

## 🛠️ Quick Start

### 1. Compile the Project
```bash
./build.sh
```

### 2. Launch the Portal
```bash
./run.sh
```
Open **[http://localhost:8080](http://localhost:8080)** in your browser or use the terminal CLI prompt!

---

## 🔑 Quick Demo Login Credentials

| Role | Email | Password |
|---|---|---|
| **Student** | `alex@university.edu` | `pass123` |
| **Company** | `recruitment@google.com` | `google123` |

---

## 📜 License
This project is open-source under the MIT License.

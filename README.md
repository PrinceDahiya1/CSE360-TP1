# 🎓 Student Discussion Board Platform 

**An interactive, role-based educational discussion system (inspired by Ed Discussion).** Developed as part of the Computer Science Software Engineering coursework (CSE 360) at Arizona State University by Prince Dahiya and team. 

This desktop application leverages **Java**, **JavaFX**, and an embedded **H2 Relational Database** to provide a secure, multi-role platform for students and instructors to collaborate. The codebase strictly adheres to the **Model-View-Controller (MVC)** architectural pattern and emphasizes Agile development practices, performance-optimized database queries, and rigorous automated testing.

---

## 🚀 Core Features

### 👨‍🏫 Instructor Analytics & Automated Grading (Phase 3)
* **Automated Participation Tracking ("Rule of 3"):** Engineered a high-performance SQL-driven verification engine (`RuleOfThreeVerifier`) that instantly calculates if a student has met participation thresholds by querying distinct peer interactions, filtering out self-replies.
* **Discussion Analytics Dashboard:** Implemented a real-time analytics suite to track unresolved questions, question-to-statement ratios, and calculate Peak Activity Times using SQL aggregations.
* **Student Performance Export:** Developed an OS-level file generation tool allowing instructors to instantly export aggregated grading metrics to CSV format for gradebook integration.
* **Contextual Grading & Endorsements:** Built specialized UI views for graders to evaluate student replies alongside their parent threads, complete with a one-click "Instructor Endorsed" badge system to highlight accurate answers.

### 💬 Interactive Q&A Discussion Board (Phase 2)
* **Full CRUD Functionality:** Designed a robust frontend using the MVC architecture allowing students to seamlessly publish, edit, search, and delete their questions and statements.
* **Targeted Search & Filtering:** Implemented method-overloading within the JavaFX Controllers to allow real-time string filtering and targeted post-history isolation without redundant database calls.
* **Advanced Moderation Overrides:** Engineered a role-checking mechanism granting Instructors and Admins the ability to bypass standard ownership rules to forcefully hide/delete inappropriate content and append internal "Staff-Only" grading comments.

### 🔐 Secure Authentication & RBAC (Phase 1)
* **Multi-Role Access Control:** Established a strict Role-Based Access Control (RBAC) system that dynamically routes users to contextual Admin, Student, or Instructor interfaces.
* **Invitation & Recovery System:** Developed an admin-controlled invitation system using secure, single-use generation codes and time-sensitive One-Time Passwords (OTPs) for account recovery.
* **Security & Credential Management:** Built a robust password evaluation engine enforcing strict cryptographic constraints and patched critical bypass vulnerabilities to ensure forced credential updates.

---

## 🧠 Technical Highlights & Architecture

* **Performance Optimization:** Deliberately offloaded heavy data computations (such as peak activity calculations and interaction verification) to the H2 database layer using optimized SQL queries (`COUNT DISTINCT`, `GROUP BY`), preventing JVM memory bottlenecks and ensuring scalable UI performance.
* **Component Reuse & Low Technical Debt:** Strategically expanded existing entity classes (`Post.java`) via `ALTER TABLE` operations to support new grading metadata, rather than building redundant tables, maintaining a lightweight memory footprint.
* **Rigorous Automated Testing:** Engineered a comprehensive JUnit test suite utilizing isolated, in-memory H2 databases. This allowed the team to inject extreme, edge-case test data to validate automated grading logic without corrupting the live production database.
* **Security-First Database Operations:** Prevented SQL Injection (CWE-89) and Broken Access Control (CWE-862) vulnerabilities by exclusively utilizing parameterized JDBC `PreparedStatement` objects across all CRUD operations.

---

## 🛠️ Tech Stack & Tools
* **Language:** Java (JDK 25)
* **GUI Framework:** JavaFX
* **Database:** H2 Database Engine (Embedded SQL)
* **Architecture:** Model-View-Controller (MVC), Client-Server Model
* **Testing:** JUnit 5 (with in-memory database isolation)
* **Version Control:** Git & GitHub (Feature-branch workflows, PR Code Reviews)
* **Project Management:** Jira (Agile/Scrum methodologies, 2-week sprints)

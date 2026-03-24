# 🎓 Student Discussion Board Platform 

**An interactive, role-based educational discussion system (inspired by Ed Discussion).** Developed as part of the Computer Science Software Engineering coursework (CSE 360) at Arizona State University by Prince Dahiya and team. 

This desktop application leverages **Java**, **JavaFX**, and an **H2 Relational Database** to provide a secure, multi-role platform for students and instructors to collaborate, ask questions, and manage coursework discussions. The codebase strictly adheres to the **Model-View-Controller (MVC)** architectural pattern and emphasizes Agile development practices, rigorous testing, and extensive Javadoc documentation.

---

## 🚀 Currently Developing (Phase 2)
*The team is actively building the core discussion functionalities and expanding the application's interactive features.*

* **Implementing** a robust Q&A discussion board frontend using the Model-View-Controller (MVC) architecture (`ModelStudentPosts`, `ViewStudentPosts`, `ControllerStudentPosts`).
* **Building** backend entity classes (`Post.java`) to support full CRUD (Create, Read, Update, Delete) database operations for student discussion threads.
* **Designing** a dynamic JavaFX user interface that allows students to seamlessly view, search, publish, edit, and delete their questions and replies.
* **Engineering** a comprehensive automated testing suite to validate all database interactions, UI state changes, and system reliability.
* **Writing** extensive Javadoc documentation mapped directly to Agile User Stories to maintain high code readability and maintainability standards.
* **Managing** iterative Agile sprints using Jira, conducting regular standups, and utilizing feature-branch Git workflows with strict pull request reviews.

---

## ✅ Completed Features (Phase 1 & Foundation)
*The foundational architecture, security, and administrative tools have been successfully deployed and tested.*

* **Engineered** a secure user authentication system that supported initial account creation exclusively via admin-generated, single-use invitation codes.
* **Established** a multi-role access control (RBAC) system that allowed seamless routing and contextual switching between Admin, Student, and Instructor interfaces.
* **Developed** a comprehensive Admin Dashboard enabling authorized users to invite new members, list active accounts, delete users, and trigger secure One-Time Passwords (OTPs) for account recovery.
* **Integrated** a local H2 relational database to securely persist user credentials, role assignments, and profile metadata.
* **Built** a robust password evaluation engine that enforced strict security criteria (minimum length, casing, numeric, and special character requirements).
* **Created** a user profile management module allowing members to update their credentials, email addresses, and preferred names securely.
* **Patched** critical security vulnerabilities, including an OTP bypass exploit, ensuring users were forced to update temporary passwords before accessing platform features.

---

## 🛠️ Tech Stack & Tools
* **Language:** Java (JDK 25)
* **GUI Framework:** JavaFX
* **Database:** H2 Database Engine (SQL)
* **Architecture:** Model-View-Controller (MVC), Singleton Pattern
* **Version Control:** Git & GitHub (Branch Protection, Code Reviews)
* **Project Management:** Jira (Agile/Scrum methodologies)

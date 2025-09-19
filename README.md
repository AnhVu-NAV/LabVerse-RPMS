# LabVerse – Research Paper Management System

## 📖 Introduction

In today’s research environment, scientists, students, and academic teams face an overwhelming flood of scientific publications. Managing these resources through scattered folders, inconsistent reference tools, or manual notes often leads to inefficiency, duplication of work, and difficulty in sharing knowledge across collaborators.

**LabVerse** is an Android-based Research Paper Management System built to address this challenge. It provides a **centralized, secure, and collaborative platform** for managing academic papers, tailored to the unique workflows of laboratories and research groups.

The application empowers users to:

- **Discover and Import** research papers from multiple sources (PDF, BibTeX).
- **Organize and Annotate** papers in personal and shared libraries.
- **Collaborate in Teams** through collections, reading lists, and status tracking.
- **Manage References** with automatic metadata extraction and export to APA, MLA, and BibTeX.
- **Track Reading Progress** at both individual and team levels, giving Principal Investigators (PIs) oversight of their group’s engagement.

Designed with scalability in mind, LabVerse can support small research labs in Hanoi or Ho Chi Minh City as well as larger international networks. Beyond being a digital repository, LabVerse aspires to become a **research ecosystem** that fosters critical thinking, teamwork, and innovation in scientific discovery.

---

## 🎯 Project Objectives

- Provide **role-based access** for Principal Investigators, Researchers, and Students.
- Ensure **secure authentication** and data synchronization between devices.
- Deliver an **integrated reading experience** with PDF annotations and offline access.
- Enhance productivity with **advanced search, filtering, and citation management**.
- Encourage collaboration through **shared collections, reading lists, and discussion tools**.

---

## 🏗️ Tech Stack

- **Frontend:** Android (Java, XML, Material Design)
- **Local Storage:** Room Database (offline-first architecture)
- **Backend (planned):** REST API (Firebase/Node.js or Spring Boot)
- **Authentication:** JWT, Encrypted SharedPreferences
- **Storage:** Firebase Storage or AWS S3 (PDF files)
- **Notifications:** Firebase Cloud Messaging (FCM)

---

## 👥 User Roles

- **Principal Investigator (PI) / Lab Head:** Full administrative control, create/manage collections, invite members, monitor progress.
- **Researcher (Postdoc/PhD):** Core contributor, manage library, annotate, and participate in team discussions.
- **Student/Intern:** Limited contributor or read-only access, focus on reading and note-taking.

---

## 🚀 Key Features

- Secure User Authentication (Email/Password, ORCID planned)
- Personal Library Dashboard (Recently Added, Favorites, Recently Read)
- Advanced Search & Filter (author, journal, keywords, year)
- Import Papers (PDF, BibTeX)
- Integrated PDF Reader with Annotations
- Shared Collections & Reading Lists for teams
- Paper Status & Priority (To Read, Reading, Finished; High/Medium/Low)
- Reference & Citation Management (APA, MLA, BibTeX)
- Reading Progress Tracking (individual + team overview)
- Offline Access & Sync with Room + WorkManager

---

## 🎯 Key Features

1. **Secure Login & Registration** (Email/Password, JWT, ORCID planned)
2. **Personal Library Dashboard** (Room DB, RecyclerView, tabs)
3. **Advanced Search & Filter** (author, journal, keyword, year)
4. **Import New Papers** (PDF, BibTeX)
5. **Integrated PDF Reader + Annotation** (highlights, notes, sync)
6. **Shared Collections (Projects)** with team collaboration
7. **Paper Status & Priority** (To Read, Reading, Finished; High/Med/Low)
8. **Reference & Citation Management** (APA, MLA, BibTeX formats)
9. **Reading Progress Tracking** (per-user, team overview for PI)
10. **Personalized Feed & Alerts** (Firebase Cloud Messaging)
11. **Offline Access & Sync** (Room DB + WorkManager)
12. **User Profile Management** (profile, password, preferences)
13. **Export/Import Annotations** (JSON/XML)
14. **BibTeX Import** (bulk add from Zotero/Mendeley)
15. **Reading Lists & Journal Clubs** (curated lists, collaboration)

---

## 📅 Roadmap

- [x] Project Initialization & Team Setup
- [ ] Secure Authentication & Profile Management
- [ ] Personal Library + Import PDF
- [ ] PDF Reader + Annotations
- [ ] Shared Collections & Status Tracking
- [ ] Citation Management (basic BibTeX)
- [ ] Offline Sync & Notifications

---

## 👨‍💻 Team Members

- Student 1 – Authentication & Profile
- Student 2 – Library & Search
- Student 3 – PDF Reader & Annotations
- Student 4 – Collections & Status Tracking
- Student 5 – UI/UX Mockups & Documentation

---

## 📄 License

This project is developed for **academic purposes** as part of the Mobile Programming course.  
It is not intended for production use.

---

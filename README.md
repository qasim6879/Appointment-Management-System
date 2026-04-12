# AppointEase | Appointment Scheduling Engine

[![]](https://img.shields.io/badge/build-passing-brightgreen)
[![]](https://img.shields.io/badge/Java-8%2B-orange)
[![]](https://img.shields.io/badge/JUnit-5-red)
[![]](https://img.shields.io/badge/Core_Logic_Coverage-%3E90%25-blue)

**AppointEase** is a high-reliability appointment management system engineered in Java. Built with a focus on data integrity and user experience, the system provides a seamless bridge between administrative control and user-side scheduling.

---

### 📊 Reliability & Testing Metrics
Following industry-standard Quality Assurance (QA) practices, the system has been rigorously tested to ensure the stability of its internal engine.

*   **Core Engine & Business Logic:** `> 90% Coverage`
    *The "brain" of the application—including booking algorithms, data serialization, and authentication logic—is fully verified.*
*   **Total System Coverage:** `62%`
    *This comprehensive metric includes the entire codebase, encompassing all Graphical User Interface (GUI) components and visual rendering logic.*

---

## 👥 Project Contributors
*   **Qasem Alawneh** - *json files & Testing*
*   **Momen Saleh** - *UI/UX Design & Architecture*
*   **Ali Diab** - *Systems Analysis & Documentation*

## ✨ Core Capabilities
*   **Dual-Dashboard Architecture**: Dedicated environments for Administrators and end-users.
*   **Intelligent Scheduling**: Real-time slot availability checking with conflict prevention.
*   **Automated Notification Engine**: Observer-based alerts for all appointment status changes.
*   **Data Persistence Layer**: Reliable local storage utilizing JSON serialization via Google Gson.

## 🛠️ Technical Specifications
*   **Architecture**: Object-Oriented Design (OOD) with focus on the Observer and Factory patterns.
*   **Testing Suite**: JUnit 5 (Jupiter) for unit and integration testing.
*   **Dependencies**: Google Gson (JSON handling).

## 🚀 Deployment & Execution
1.  **Environment**: Ensure JDK 8 or higher is configured.
2.  **Launch**: Execute `org.example.Main` to initialize the application.
3.  **Verification**: Execute JUnit tests in the `tests` package to verify reliability.

---
*Developed as a final requirement for the Course at University.*
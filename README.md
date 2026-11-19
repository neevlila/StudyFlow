# StudyFlow - Personal Student Study Planner & Assignment Manager

StudyFlow is a modern, responsive, dark-first Java web application designed to help college students organize academic assignments, track upcoming deadlines, manage course priorities, and monitor overall study progress.

Built specifically for academic demonstration, college viva defense, and practical learning, the project showcases clean Object-Oriented Programming (OOP) in Java 21, Spring Boot, Vaadin Flow, and persistent H2 SQL storage.

---

## 1. Project Introduction

College coursework across multiple subjects (such as Algorithms, Java, DBMS, Computer Networks, and Web Technology) often leads to missed deadlines and disorganized preparation. **StudyFlow** offers an intuitive, single-dashboard interface where students can:
- Instantly see total, pending, completed, and overdue tasks.
- Monitor their completion progress with a real-time progress bar.
- Create, modify, filter, search, and delete assignments with full data safety.
- Keep all records persistent locally without needing third-party cloud services or internet access.

---

## 2. Problem Statement

Students frequently juggle diverse deadlines written across disparate notebooks or chat apps. Existing tools are either bloated with unnecessary enterprise workflows or too rudimentary to track deadline urgency and completion statistics accurately. StudyFlow provides a focused, elegant, student-centric productivity dashboard tailored specifically to college workloads.

---

## 3. Project Objectives

1. **Practical OOP Demonstration**: Apply Java classes, inheritance, encapsulation, enums, interfaces, and methods.
2. **Robust Data Persistence**: Store records in a local persistent H2 SQL database that survives application restarts.
3. **Safe Database Operations**: Use Spring's `JdbcTemplate` with parameterized queries to prevent SQL injection.
4. **Pure Java UI**: Construct 100% of the UI (buttons, grids, modals, layouts, and event listeners) in Java using Vaadin Flow without writing custom JavaScript or complex HTML.
5. **Zero Division-by-Zero Errors**: Gracefully handle edge cases such as empty datasets during mathematical calculations.
6. **Viva Readiness**: Provide clear, straightforward code with intuitive loops and readable methods that a student can confidently defend in front of an examiner.

---

## 4. Technologies Used

| Technology | Role |
| :--- | :--- |
| **Java 21** | Primary Object-Oriented programming language |
| **Spring Boot 3.3.5** | Application foundation, dependency injection, and embedded Tomcat |
| **Vaadin Flow 24.4** | Pure Java modern web UI framework |
| **H2 Database (v2.2)** | Local file-based persistent relational SQL database |
| **Spring JDBC (`JdbcTemplate`)** | Safe, parameterized SQL data access |
| **Maven** | Dependency management and build tool |
| **JUnit 5** | Automated unit and integration testing |

---

## 5. Key Features

- **Dashboard Overview**:
  - Four dynamic stat cards: **Total Tasks**, **Pending**, **Completed**, and **Overdue**.
  - **Progress Bar**: Calculated via `(Completed / Total) * 100` with zero-task safety.
  - **Upcoming Deadlines**: Highlighting tasks due *Today*, *Soon*, or *Overdue*.
- **Comprehensive Task Management**:
  - **Create Task**: Modal dialog with title, subject, due date, priority, and optional description.
  - **Live Search**: Case-insensitive filtering across both assignment titles and subject names.
  - **Multi-Filter System**: Filter simultaneously by status (*All / Pending / Completed*) and priority (*All / High / Medium / Low*).
  - **Quick Status Toggle**: One-click checkmark to toggle between Pending and Completed.
  - **Edit Task**: Pre-populated modal form for updating existing assignments.
  - **Safe Deletion**: Confirmation dialog preventing accidental data removal.
- **Completed Archive**:
  - Dedicated view showing completed tasks with strike-through styling, restore capability, and permanent deletion.
- **Sample Data Seeding**:
  - Automatically loads realistic college assignments (DAA, Java, DBMS, Networks) on first launch without creating duplicates on subsequent restarts.
  - "Reset Sample Data" button on the dashboard for quick live examiner demonstrations.

---

## 6. Installation & Prerequisites

1. **Java Development Kit (JDK 21)** installed.
2. **Apache Maven 3.8+** installed.
3. Git (optional).

Verify your environment:
```bash
java -version
mvn -version
```

---

## 7. How to Run the Application

### Option A: One-Click Runner (Easiest)
Double-click `run.bat` or run in PowerShell:
```powershell
.\run.ps1
```
or in Command Prompt:
```cmd
run.bat
```
This automatically finds your Java 21 runtime, starts the production bundle, and serves the application.

### Option B: Running the Prebuilt JAR Directly
```powershell
java -jar target\studyflow.jar
```
*(If `java` is not recognized on your global PATH, run with full path: `& "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -jar target\studyflow.jar`)*

### Option C: Using Maven Wrapper
If `mvn` is not installed on your system PATH, use the included wrapper:
```powershell
.\mvnw.ps1 spring-boot:run
```
Or if you have Maven globally installed:
```bash
mvn spring-boot:run
```

### Accessing the Web Application
Once started, open your web browser and visit:
```text
http://localhost:8080
```

### Accessing the H2 Database Console
To inspect the underlying SQL database directly during your viva demonstration:
1. Open `http://localhost:8080/h2-console`
2. Enter the JDBC URL: `jdbc:h2:mem:studyflowdb` (or `jdbc:h2:file:./data/studyflowdb` if using file DB)
3. Username: `sa`
4. Password: *(leave blank)*
5. Click **Connect** to query the `TASKS` table directly using SQL.

---

## 8. Deploying to Render (Cloud Hosting Guide)

StudyFlow is pre-configured and 100% compatible with [Render](https://render.com) for free cloud hosting.

### Why Browser LocalStorage is Ideal for Render:
1. **Zero Data Loss on Sleep**: Render's free tier spins down after 15 minutes of inactivity. When it wakes up, your tasks are preserved permanently inside your browser's `localStorage`!
2. **Multi-User Privacy**: Multiple students or professors visiting your live Render link each get their own private study planner without colliding with or overwriting each other's tasks.
3. **No External Database Costs**: No need to configure expensive managed PostgreSQL instances or cloud credentials.

### Step-by-Step Render Deployment:
1. **Push to GitHub**:
   Initialize git and push your StudyFlow repository to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Deploy StudyFlow with LocalStorage database"
   git remote add origin https://github.com/<your-username>/studyflow.git
   git push -u origin main
   ```
2. **Create New Web Service on Render**:
   - Log in to your [Render Dashboard](https://dashboard.render.com).
   - Click **New +** and select **Web Service**.
   - Connect your GitHub repository.
3. **Configure the Service**:
   - **Name**: `studyflow`
   - **Language**: `Docker` *(Render will automatically use the optimized multi-stage `Dockerfile`)*
   - **Branch**: `main`
   - **Plan**: `Free`
   *(Render will automatically bind to its dynamic `$PORT` via `server.port=${PORT:8080}`).*
4. **Deploy**:
   - Click **Create Web Service**.
   - Render will build the container and deploy your application.
   - Once deployed, open your live Render URL (e.g. `https://studyflow.onrender.com`). All tasks you create will be safely stored in your browser's local storage!

---

## 9. Project Folder Structure

```text
StudyFlow/
├── pom.xml                                    # Maven configuration & dependencies
├── README.md                                  # Project overview and setup guide
├── PROJECT_EXPLANATION.md                     # Viva questions & technical guide
├── data/                                      # Persistent H2 database storage
│   └── studyflowdb.mv.db                      # Local database file
├── frontend/
│   └── themes/studyflow/                      # Dark-first design theme & CSS
│       └── styles.css
└── src/
    ├── main/
    │   ├── java/com/studyflow/
    │   │   ├── StudyFlowApplication.java      # Spring Boot entry point
    │   │   ├── model/
    │   │   │   ├── Priority.java              # LOW, MEDIUM, HIGH enum
    │   │   │   ├── TaskStatus.java            # PENDING, COMPLETED enum
    │   │   │   └── Task.java                  # Task domain entity POJO
    │   │   ├── repository/
    │   │   │   └── TaskRepository.java        # Parameterized SQL & JdbcTemplate
    │   │   ├── service/
    │   │   │   └── TaskService.java           # Calculations, filters, & logic
    │   │   ├── components/
    │   │   │   ├── TaskFormDialog.java        # Modal form for create/edit
    │   │   │   └── ConfirmDeleteDialog.java   # Confirmation modal
    │   │   └── view/
    │   │       ├── MainLayout.java            # Navigation drawer & header
    │   │       ├── DashboardView.java         # Stat cards, progress, upcoming
    │   │       ├── TasksView.java             # Task table, search, & filters
    │   │       ├── CompletedTasksView.java    # Completed archive view
    │   │       └── Refreshable.java           # View refresh contract interface
    │   └── resources/
    │       ├── application.properties         # H2 and Vaadin configuration
    │       └── schema.sql                     # SQL table DDL definition
    └── test/
        └── java/com/studyflow/
            ├── StudyFlowApplicationTests.java # Context startup verification
            └── service/
                └── TaskServiceTest.java       # Complete CRUD & calculation tests
```

---

## 9. Database Explanation & Schema

StudyFlow uses an embedded H2 file database (`./data/studyflowdb.mv.db`). It creates the table automatically on first boot using the following DDL:

```sql
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    due_date DATE NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- **id**: Primary Key with auto-increment.
- **title** and **subject**: Required string attributes.
- **due_date**: Standard ANSI SQL `DATE` mapped to Java's `java.time.LocalDate`.
- **priority** & **status**: Stored as strings representing Enum values (`HIGH`, `PENDING`, etc.).
- **created_at**: Timestamp defaulting to the current system time.

---

## 10. How CRUD Operations Work

1. **CREATE**:
   - The user opens `TaskFormDialog` and fills in Title, Subject, Due Date, and Priority.
   - Upon clicking "Save", input validation ensures required fields are non-empty.
   - `TaskService.saveTask(task)` calls `TaskRepository.save(task)`.
   - `TaskRepository` runs an `INSERT INTO tasks ...` using Spring's `KeyHolder` to retrieve the auto-generated ID.
   - The UI displays a success notification and refreshes all metrics.
2. **READ**:
   - `TaskRepository.findAll()` executes `SELECT * FROM tasks ORDER BY due_date ASC, id DESC`.
   - A Spring `RowMapper<Task>` maps SQL columns to a Java `Task` object.
   - `TaskService.searchAndFilter(...)` filters tasks in-memory by keyword, status, and priority.
3. **UPDATE**:
   - The user clicks the Edit button or the completion toggle.
   - `TaskRepository.save(task)` detects an existing `id` and executes a parameterized `UPDATE tasks SET ... WHERE id = ?`.
4. **DELETE**:
   - The user clicks the Trash button.
   - `ConfirmDeleteDialog` prompts the user for confirmation.
   - Once confirmed, `TaskRepository.deleteById(id)` runs `DELETE FROM tasks WHERE id = ?`.

---

## 11. Future Improvements

- User authentication (e.g. login for multiple students).
- Email or push notifications for tasks due within 24 hours.
- Study session timer / Pomodoro clock integrated into the dashboard.
- Export task schedule to PDF or iCalendar (.ics) format.

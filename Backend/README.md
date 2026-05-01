# 🚀 Team Task Manager — Backend API

Built with **Spring Boot 3**, **JWT**, **PostgreSQL**, deployed on **Railway**.

---

## 🏗️ Tech Stack

| Layer        | Technology              |
|--------------|-------------------------|
| Framework    | Spring Boot 3.2         |
| Language     | Java 17                 |
| Auth         | JWT (HS256)             |
| Database     | PostgreSQL              |
| ORM          | JPA / Hibernate         |
| Security     | Spring Security 6       |
| Build        | Maven                   |
| Deploy       | Railway (Docker)        |

---

## 📁 Project Structure

```
src/main/java/com/taskmanager/
├── config/          # Security & CORS config
├── controller/      # REST endpoints
├── dto/
│   ├── request/     # Input DTOs
│   └── response/    # Output DTOs
├── entity/          # JPA entities
├── enums/           # Role, Status, Priority enums
├── exception/       # Custom exceptions & global handler
├── repository/      # Spring Data JPA repos
├── security/        # JWT filter, UserDetails
└── service/         # Business logic
```

---

## ⚡ Local Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Step 1 — Create Database
```sql
CREATE DATABASE taskmanager;
```

### Step 2 — Configure Environment
Copy `.env.example` to `.env` and fill in your values:
```
DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanager
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000
```

Or update `src/main/resources/application.properties` directly.

### Step 3 — Run the App
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

Server starts at: `http://localhost:8080`

---

## 🧪 Postman Testing Guide

### Step 1 — Import Collection
1. Open **Postman**
2. Click **Import** → drag in `TaskManager-API.postman_collection.json`
3. The collection appears in your sidebar

### Step 2 — Set Base URL
1. Click the collection name → **Variables** tab
2. Set `base_url` to `http://localhost:8080` (or your Railway URL)

### Step 3 — Test Flow (in order!)

#### 🔐 Auth First
```
1. POST /api/auth/register  →  Register as ADMIN (alice@example.com)
   ✅ Token auto-saved to {{auth_token}} variable

2. POST /api/auth/register  →  Register second user as MEMBER (bob@example.com)
   (Login as alice again after this)

3. POST /api/auth/login     →  Login with alice's credentials
   ✅ Token refreshed in {{auth_token}}
```

#### 📁 Projects
```
4. POST   /api/projects            → Create a project
   ✅ project_id auto-saved

5. GET    /api/projects            → List all your projects
6. GET    /api/projects/{{project_id}}  → Get project details
7. PUT    /api/projects/{{project_id}}  → Update project
```

#### 👥 Members
```
8. POST   /api/projects/{{project_id}}/members/invite
   Body: { "email": "bob@example.com", "role": "MEMBER" }
   → Invite Bob to the project
```

#### ✅ Tasks
```
9.  POST  /api/projects/{{project_id}}/tasks  → Create task (assigneeId: 1)
    ✅ task_id auto-saved

10. GET   /api/projects/{{project_id}}/tasks  → List all tasks
11. PATCH /api/projects/{{project_id}}/tasks/{{task_id}}/status
    Body: { "status": "IN_PROGRESS" }   → Update status

12. GET   /api/tasks/my       → My assigned tasks
13. GET   /api/tasks/overdue  → My overdue tasks
```

#### 📊 Dashboard
```
14. GET /api/dashboard  → Full dashboard with stats
```

---

## 🔐 API Endpoints Reference

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Login & get token |
| GET | `/api/health` | ❌ | Health check |

### Users
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/me` | ✅ | Get my profile |
| GET | `/api/users` | ✅ | Get all users |
| GET | `/api/users/{id}` | ✅ | Get user by ID |

### Projects
| Method | Endpoint | Auth | RBAC |
|--------|----------|------|------|
| POST | `/api/projects` | ✅ | Any member |
| GET | `/api/projects` | ✅ | Own projects |
| GET | `/api/projects/{id}` | ✅ | Project members |
| PUT | `/api/projects/{id}` | ✅ | Admin only |
| DELETE | `/api/projects/{id}` | ✅ | Admin only |

### Tasks
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/projects/{pid}/tasks` | ✅ | Create task |
| GET | `/api/projects/{pid}/tasks` | ✅ | List project tasks |
| GET | `/api/projects/{pid}/tasks/{tid}` | ✅ | Get task detail |
| PUT | `/api/projects/{pid}/tasks/{tid}` | ✅ | Update task |
| PATCH | `/api/projects/{pid}/tasks/{tid}/status` | ✅ | Update status |
| DELETE | `/api/projects/{pid}/tasks/{tid}` | ✅ | Admin only |
| GET | `/api/tasks/my` | ✅ | My assigned tasks |
| GET | `/api/tasks/overdue` | ✅ | My overdue tasks |

### Members
| Method | Endpoint | Auth | RBAC |
|--------|----------|------|------|
| GET | `/api/projects/{pid}/members` | ✅ | Any member |
| POST | `/api/projects/{pid}/members/invite` | ✅ | Admin only |
| PATCH | `/api/projects/{pid}/members/{uid}/role` | ✅ | Admin only |
| DELETE | `/api/projects/{pid}/members/{uid}` | ✅ | Admin only |
| DELETE | `/api/projects/{pid}/members/leave` | ✅ | Any member |

### Dashboard
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/dashboard` | ✅ | Aggregated stats |

---

## 🌐 Deploy on Railway

### Step 1 — Push to GitHub
```bash
git init
git add .
git commit -m "Initial backend"
git remote add origin https://github.com/yourname/task-manager-api.git
git push -u origin main
```

### Step 2 — Create Railway Project
1. Go to [railway.app](https://railway.app) → New Project
2. **Deploy from GitHub repo** → select your repo
3. Railway auto-detects Dockerfile ✅

### Step 3 — Add PostgreSQL
1. In Railway dashboard → **New Service** → **PostgreSQL**
2. Copy the `DATABASE_URL` from Variables tab

### Step 4 — Set Environment Variables
In Railway → your service → **Variables**, add:
```
DATABASE_URL     = (copy from PostgreSQL service)
DB_USERNAME      = (from PostgreSQL service)
DB_PASSWORD      = (from PostgreSQL service)
JWT_SECRET       = 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION   = 86400000
```

### Step 5 — Deploy 🚀
Railway auto-deploys. Visit your live URL!

---

## 📊 Database Schema

```
users
├── id, name, email, password, role (ADMIN/MEMBER)
├── is_active, created_at

projects
├── id, name, description, status (ACTIVE/ON_HOLD/COMPLETED)
├── color, due_date, created_by (FK→users)
├── created_at, updated_at

project_members
├── id, project_id (FK), user_id (FK), role (ADMIN/MEMBER)
├── joined_at
├── UNIQUE(project_id, user_id)

tasks
├── id, title, description
├── status (TODO/IN_PROGRESS/IN_REVIEW/DONE)
├── priority (LOW/MEDIUM/HIGH)
├── due_date, project_id (FK), assignee_id (FK), created_by (FK)
├── created_at, updated_at
```

---

## 🔑 Role-Based Access Control

| Action | ADMIN | MEMBER |
|--------|-------|--------|
| Create project | ✅ | ✅ |
| View project | ✅ | ✅ |
| Edit / Delete project | ✅ | ❌ |
| Create task | ✅ | ✅ |
| Update task | ✅ | ✅ |
| Delete task | ✅ | ❌ |
| Invite member | ✅ | ❌ |
| Change member role | ✅ | ❌ |
| Remove member | ✅ | ❌ |
| Leave project | ✅ | ✅ |

---

*Built with ❤️ using Spring Boot 3 + PostgreSQL*

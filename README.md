# 🏨 Raffles Hotel Management System

A full-featured desktop hotel management system built with **JavaFX** and an **H2 embedded database**, following a clean **DAO/Repository architecture**. Covers the complete hotel operations lifecycle — rooms, guests, reservations, invoices, housekeeping, maintenance, employees, and billing.

---

## ✨ Features

| Module | Description |
|---|---|
| 🔐 Auth | Login, Sign-up, Role-based access control |
| 🛏️ Room Management | Room types, floors, status tracking (Available / Occupied / Maintenance / Reserved) |
| 👥 Guest Management | Guest profiles, account management, guest portal |
| 📅 Reservations | Full booking lifecycle — create, modify, cancel |
| 🧾 Invoices & Billing | Invoice generation, charges, payments |
| 🧹 Housekeeping | Task assignment and tracking |
| 🔧 Maintenance | Request logging and resolution |
| 👨‍💼 Employees | Staff records and department management |
| 📊 Dashboard | Overview of occupancy, revenue, and operations |

---

## 🏗️ Project Structure

```
src/main/java/com/example/demo10/raffles/hotelmgmt/
├── model/                        # Domain entities
│   ├── Room.java
│   ├── RoomType.java
│   ├── Reservation.java
│   ├── Invoice.java
│   ├── Payment.java
│   ├── Service.java
│   ├── Role.java
│   └── MaintenanceRequest.java
│
├── dao/                          # Data Access Objects (Repository pattern)
│   ├── RoomDAO.java
│   ├── RoomTypeDAO.java
│   ├── ReservationDAO.java
│   ├── InvoiceDAO.java
│   ├── PaymentDAO.java
│   ├── ChargeDAO.java
│   ├── ServiceDAO.java
│   ├── GuestDAO.java
│   ├── GuestAccountDAO.java
│   ├── RoleDAO.java
│   ├── DepartmentDAO.java
│   ├── EmployeeDAO.java
│   ├── HousekeepingTaskDAO.java
│   └── MaintenanceRequestDAO.java
│
├── controller/                   # JavaFX controllers (UI logic)
│   ├── DashboardController.java
│   ├── RoomManagementController.java
│   ├── GuestManagementController.java
│   ├── GuestPortalController.java
│   ├── InvoiceManagementController.java
│   ├── EmployeeManagementController.java
│   ├── LoginController.java
│   └── SignUpController.java
│
├── util/                         # Shared utilities
│   ├── AuthUIFactory.java
│   ├── DialogUtil.java
│   └── UIConstants.java
│
├── DatabaseConnector.java        # H2 connection management
├── DatabaseInitializer.java      # Schema creation & seeding
├── DatabaseResetUtil.java        # DB reset utility
├── MainApp.java                  # JavaFX Application entry point
├── Launcher.java                 # IDE-friendly launcher (auto-resolves JavaFX modules)
├── DatabaseTest.java             # Verifies connectivity and table structure
├── DatabasePersistenceTest.java  # Verifies data persists across connections
└── DbReset.java                  # Quick DB reset runner
```

---

## 🧪 Tests

| Class | What It Verifies |
|---|---|
| `DatabaseTest` | Connection, table existence, room type and room counts, auto-seeds if empty |
| `DatabasePersistenceTest` | Data written in connection 1 is readable in a separate connection 2 |
| `TestConnection` | Minimal connectivity smoke test |

---

## ⚙️ Setup

**Prerequisites:** Java 17+, Maven

```bash
git clone https://github.com/Aml-Asd/raffles-hotel-management-system
cd raffles-hotel-management-system

# Run with Maven (handles JavaFX module path automatically)
mvn javafx:run
```

> **Or run directly from IntelliJ/Eclipse:**
> Run `Launcher.java` — it automatically locates JavaFX JARs from your Maven repo or common install paths. No manual VM args needed.

**Database:** H2 file-based, auto-created at `~/raffles_hotel_db` on first run. Schema is initialised automatically by `DatabaseInitializer`.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | JavaFX (FXML) |
| Database | H2 Embedded Database |
| Data Access | JDBC + DAO Pattern |
| Build | Maven |

---

## 👩‍💻 Author

**Aml Abdelrhman Ahmed Mohamed**  
B.Sc. Computer Science — AASTMT Aswan, Egypt  
GPA: 3.67 / 4.0 (Excellence)

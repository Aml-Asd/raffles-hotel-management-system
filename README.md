# 🏨 Raffles Hotel Management System

A full desktop hotel management system with a JavaFX UI, H2 embedded database, and a clean DAO/Repository architecture. Covers room inventory, booking lifecycle, and billing.

---

## ✨ Features

- **Room Management** — add, edit, and track rooms by type (Standard, Deluxe, Suite), floor, occupancy, and status (Available / Occupied / Maintenance / Reserved)
- **Room Types** — configurable types with base rates and max occupancy
- **Database Persistence** — H2 embedded database with verified cross-connection data integrity
- **DAO Pattern** — clean Repository architecture separating UI from data access
- **Auto-seeding** — creates sample room types and rooms if database is empty
- **JavaFX Module Resolution** — `Launcher.java` automatically locates JavaFX JARs from Maven repo or common install paths, eliminating IDE setup friction

---

## 🏗️ Architecture

```
src/
├── model/
│   ├── Room.java
│   └── RoomType.java
├── dao/
│   ├── RoomDAO.java
│   └── RoomTypeDAO.java
├── DatabaseConnector.java    # H2 connection management
├── MainApp.java              # JavaFX Application entry point
├── Launcher.java             # IDE-friendly launcher (resolves JavaFX modules)
├── DatabaseTest.java         # Verifies connectivity and table structure
└── DatabasePersistenceTest.java  # Verifies data persists across connections
```

---

## 🧪 Tests

| Class | What it verifies |
|---|---|
| `DatabaseTest` | Connection, table existence, room type and room counts |
| `DatabasePersistenceTest` | Data written in connection 1 is readable in connection 2 |

---

## ⚙️ Setup

**Prerequisites:** Java 17+, Maven

```bash
git clone https://github.com/Aml-Asd/raffles-hotel-management-system
cd raffles-hotel-management-system

# Run with Maven (handles JavaFX automatically)
mvn javafx:run

# Or run the Launcher directly from your IDE
# Launcher.java will find JavaFX JARs automatically
```

**Database location:** `~/raffles_hotel_db` (H2 file-based, auto-created on first run)

---

## 🛠️ Tech Stack

Java · JavaFX · H2 Embedded Database · JDBC · DAO Pattern · Maven

---

## 👩‍💻 Author

**Aml Abdelrhman Ahmed Mohamed**  
B.Sc. Computer Science — AASTMT Aswan (GPA: 3.67/4.0 — Excellence)


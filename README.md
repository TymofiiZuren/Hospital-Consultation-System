# Hospital Consultation System

A Java Swing hospital workflow system for patient registration, authentication, appointments, consultations, medical records, labs, insurance, billing, and admin management.

## What is included

- Patient, doctor, admin, and lab technician account flows
- Appointment booking and management with medical need support
- Consultation, medical record, invoice, and lab-result workflows
- Admin management screens for accounts, patients, doctors, visits, billing, and insurance
- Shared UI helpers, custom exception hierarchy, and validation utilities
- Unit tests and database-backed integration tests

## Tech stack

- Java 25
- Maven
- Swing
- MySQL
- JUnit 5

## Project structure

```text
src/main/java/ie/setu/hcs
  config/       database configuration
  controller/   form and auth controllers
  dao/          DAO interfaces and implementations
  exception/    custom exception hierarchy
  model/        domain models
  service/      business logic
  ui/           Swing frames and dashboards
  util/         shared helpers, validation, table utilities, transaction runner

src/main/resources
  application.properties
  hospital_consultation_db.sql

src/test/java
  service/      unit tests
  integration/  DB-backed integration tests
```

## Prerequisites

- Java 25
- Maven 3.9+
- MySQL running locally

The current default local configuration expects:

```properties
db.url=jdbc:mysql://localhost:3307/hospital_consultation_db
db.user=root
db.password=
```

These values live in:
[application.properties](/Users/thomas/Documents/ProgrammeSETU/O_O_S_D_2/java/Hospital-Consultation-System/src/main/resources/application.properties)

## Database setup

1. Create the database in MySQL if it does not exist.
2. Import the schema and seed data from:
   [hospital_consultation_db.sql](/Users/thomas/Documents/ProgrammeSETU/O_O_S_D_2/java/Hospital-Consultation-System/src/main/resources/hospital_consultation_db.sql)
3. Confirm MySQL is reachable on `localhost:3307` or update `application.properties`.

## Running the application

Compile:

```bash
mvn clean compile
```

Run the main class directly from your IDE:

`ie.setu.hcs.ui.MainForm`

Or build the submission fat jar and run it with:

```bash
java -jar target/Hospital-Consultation-System-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Testing

Run unit tests:

```bash
mvn test
```

Run unit tests plus integration tests:

```bash
mvn verify
```

Integration tests use the configured MySQL database. If the local database is unavailable, they are skipped with a JUnit assumption instead of failing the build.

Current integration coverage includes:

- transaction rollback verification
- patient registration persistence
- consultation save/delete synchronization
- doctor management service flow

## Submission build

To generate the submission bundle:

```bash
mvn -Psubmission clean package
```

This produces:

- a runnable fat jar in `target/`
- a submission zip in `target/`

The submission zip includes:

- `README.md`
- `application.properties`
- `hospital_consultation_db.sql`
- the packaged fat jar

## Notes on recent project work

### Transaction handling

Multi-step write flows now use:
[TransactionRunner.java](/Users/thomas/Documents/ProgrammeSETU/O_O_S_D_2/java/Hospital-Consultation-System/src/main/java/ie/setu/hcs/util/TransactionRunner.java)

This is used to keep registration and consultation writes atomic.

### Validation utilities

Shared validation now lives in:
[InputValidationUtil.java](/Users/thomas/Documents/ProgrammeSETU/O_O_S_D_2/java/Hospital-Consultation-System/src/main/java/ie/setu/hcs/util/InputValidationUtil.java)

### Table model utility

Reusable table-model helpers live in:
[TableModelUtil.java](/Users/thomas/Documents/ProgrammeSETU/O_O_S_D_2/java/Hospital-Consultation-System/src/main/java/ie/setu/hcs/util/TableModelUtil.java)

### Doctor management service

Admin doctor management now has a dedicated service:
[DoctorManagementService.java](/Users/thomas/Documents/ProgrammeSETU/O_O_S_D_2/java/Hospital-Consultation-System/src/main/java/ie/setu/hcs/service/DoctorManagementService.java)

# Doctor Appointment System

Web application for managing doctors, appointment availability, and patient bookings, developed with Java and Spring Boot.

## Features

### Patient

* Browse and filter available appointments by doctor or specialization
* Book an appointment
* View appointments using a phone number
* Cancel existing appointments

### Administrator

* Session-based access to the administration panel
* Add and remove doctors
* Create and manage appointment slots
* Filter appointments by booking status
* Prevent deletion of doctors with active appointments

### Additional

* Persistent H2 database
* Duplicate appointment prevention
* Validation of appointment operations
* Automatic sample data initialization

## Tech Stack

* Java 17
* Spring Boot
* Spring MVC
* Spring Data JPA / Hibernate
* Thymeleaf
* H2
* Maven

## Running the Application

### 1. Clone the repository

```bash
git clone https://github.com/m-sadlowski/doctor-appointment.git
cd doctor-appointment
```

### 2. Set the administrator password

Windows PowerShell:

```powershell
$env:APP_ADMIN_PASSWORD="your-password"
```

Linux / macOS:

```bash
export APP_ADMIN_PASSWORD="your-password"
```

### 3. Start the application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
./mvnw spring-boot:run
```

The application will be available at:

```text
http://localhost:8081
```

## Database

The application uses a persistent H2 database stored locally in the `data/` directory. Database files are excluded from version control.

The administrator panel uses simple session-based access control intended for educational purposes.

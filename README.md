# Clinic App – system rejestracji wizyt lekarskich

Aplikacja uruchamia się lokalnie pod adresem:

> http://localhost:8081

---

## 1. Wymagania

- **Java 17** 
- system Windows / Linux / macOS
- projekt zawiera **Maven Wrapper** (`mvnw`, `mvnw.cmd`)

---

### 2. Uruchomienie z linii komend (bez IDE)

W katalogu projektu (`clinic-app`):

#### Windows

```bash
mvnw.cmd spring-boot:run
```
### 3. Wejscie do bazy danych localhost:xxxx/h2-console
SHOW TABLES; 
SELECT * FROM DOCTOR;
SELECT * FROM APPOINTMENT;
SELECT * FROM PATIENT;
SHOW COLUMNS FROM APPOINTMENT;
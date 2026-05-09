# 🏥 GUIA DE IMPLEMENTAÇÃO — SISTEMA DE CLÍNICA MÉDICA
## Parte 2: Modelagem de Banco de Dados e Estrutura de Pastas

---

# 3. MODELAGEM DO BANCO DE DADOS

## 3.1 Estratégia: Database per Service

Cada microsserviço possui seu banco de dados **isolado**. Nenhum serviço acessa diretamente o banco de outro.

| Serviço | Banco | Porta |
|---|---|---|
| `auth-service` | `auth_db` | 3306 |
| `admin-service` | `admin_db` | 3307 |
| `patient-service` | `patient_db` | 3308 |
| `scheduling-service` | `scheduling_db` | 3309 |
| `medical-record-service` | `medical_record_db` | 3310 |

> **Por quê?** Se os serviços compartilham banco, uma migration mal feita em um serviço pode derrubar todos os outros. Bancos isolados garantem **independência** e **resiliência**.

## 3.2 Diagrama ER — auth_db

```
┌──────────────────────────┐       ┌──────────────────────────┐
│          users           │       │          roles           │
├──────────────────────────┤       ├──────────────────────────┤
│ id          BIGINT PK    │       │ id          BIGINT PK    │
│ username    VARCHAR(100) │       │ name        VARCHAR(50)  │
│ password    VARCHAR(255) │       │ description VARCHAR(200) │
│ employee_id BIGINT       │       │ created_at  TIMESTAMP    │
│ role_id     BIGINT FK────┼──────►│                          │
│ active      BOOLEAN      │       └──────────────────────────┘
│ login_attempts INT       │
│ locked_until TIMESTAMP   │
│ created_at  TIMESTAMP    │
│ updated_at  TIMESTAMP    │
└──────────────────────────┘
```

**SQL de criação:**
```sql
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    employee_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id),
    INDEX idx_username (username),
    INDEX idx_employee_id (employee_id)
);

-- Dados iniciais
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrador do sistema'),
('DOCTOR', 'Médico'),
('RECEPTIONIST', 'Recepcionista'),
('NURSE', 'Enfermeiro(a)');
```

## 3.3 Diagrama ER — admin_db

```
┌──────────────────────────────┐
│         employees            │
├──────────────────────────────┤
│ id             BIGINT PK     │
│ full_name      VARCHAR(150)  │
│ rg             VARCHAR(20)   │
│ cpf            VARCHAR(14)   │
│ address        VARCHAR(255)  │
│ neighborhood   VARCHAR(100)  │
│ city           VARCHAR(100)  │
│ state          VARCHAR(2)    │
│ zip_code       VARCHAR(10)   │
│ phone          VARCHAR(20)   │
│ cellphone      VARCHAR(20)   │
│ ctps_number    VARCHAR(20)   │
│ pis_number     VARCHAR(20)   │
│ active         BOOLEAN       │
│ created_at     TIMESTAMP     │
│ updated_at     TIMESTAMP     │
└──────────────────────────────┘

┌──────────────────────────────┐
│        specialties           │
├──────────────────────────────┤
│ id             BIGINT PK     │
│ description    VARCHAR(100)  │
│ active         BOOLEAN       │
│ created_at     TIMESTAMP     │
└──────────────────────────────┘

┌──────────────────────────────┐      ┌──────────────────────┐
│          doctors             │      │    specialties       │
├──────────────────────────────┤      │                      │
│ id             BIGINT PK     │      │                      │
│ name           VARCHAR(150)  │      │                      │
│ crm            VARCHAR(20)   │      │                      │
│ specialty_id   BIGINT FK─────┼─────►│                      │
│ active         BOOLEAN       │      └──────────────────────┘
│ created_at     TIMESTAMP     │
│ updated_at     TIMESTAMP     │
└──────────────────────────────┘

┌──────────────────────────────┐
│     health_insurances        │
├──────────────────────────────┤
│ id             BIGINT PK     │
│ company_name   VARCHAR(150)  │
│ cnpj           VARCHAR(18)   │
│ phone          VARCHAR(20)   │
│ active         BOOLEAN       │
│ created_at     TIMESTAMP     │
│ updated_at     TIMESTAMP     │
└──────────────────────────────┘
```

**SQL de criação:**
```sql
CREATE DATABASE IF NOT EXISTS admin_db;
USE admin_db;

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    rg VARCHAR(20) NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    phone VARCHAR(20),
    cellphone VARCHAR(20) NOT NULL,
    ctps_number VARCHAR(20) NOT NULL,
    pis_number VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cpf (cpf),
    INDEX idx_rg (rg),
    INDEX idx_active (active)
);

CREATE TABLE specialties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    crm VARCHAR(20) NOT NULL UNIQUE,
    specialty_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id),
    INDEX idx_crm (crm),
    INDEX idx_specialty (specialty_id)
);

CREATE TABLE health_insurances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    phone VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cnpj (cnpj)
);

-- Dados iniciais
INSERT INTO specialties (description) VALUES
('Cardiologia'), ('Dermatologia'), ('Ortopedia'),
('Pediatria'), ('Ginecologia'), ('Neurologia'),
('Oftalmologia'), ('Urologia'), ('Psiquiatria'),
('Clínico Geral');
```

## 3.4 Diagrama ER — patient_db

```
┌──────────────────────────────┐
│          patients            │
├──────────────────────────────┤
│ id             BIGINT PK     │
│ full_name      VARCHAR(150)  │
│ rg             VARCHAR(20)   │
│ cpf            VARCHAR(14)   │
│ address        VARCHAR(255)  │
│ city           VARCHAR(100)  │
│ state          VARCHAR(2)    │
│ zip_code       VARCHAR(10)   │
│ phone          VARCHAR(20)   │
│ cellphone      VARCHAR(20)   │
│ birth_date     DATE          │
│ gender         ENUM          │
│ has_insurance  BOOLEAN       │
│ insurance_id   BIGINT        │
│ active         BOOLEAN       │
│ created_at     TIMESTAMP     │
│ updated_at     TIMESTAMP     │
└──────────────────────────────┘
```

> **Nota:** `insurance_id` é um ID referencial do `admin-service`. **Não existe FK real** entre bancos diferentes. A integridade é garantida via validação Feign em tempo de execução.

```sql
CREATE DATABASE IF NOT EXISTS patient_db;
USE patient_db;

CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    rg VARCHAR(20) NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    phone VARCHAR(20),
    cellphone VARCHAR(20) NOT NULL,
    birth_date DATE NOT NULL,
    gender ENUM('MASCULINO', 'FEMININO', 'OUTRO') NOT NULL,
    has_insurance BOOLEAN DEFAULT FALSE,
    insurance_id BIGINT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cpf (cpf),
    INDEX idx_rg (rg),
    INDEX idx_name (full_name),
    INDEX idx_insurance (insurance_id)
);
```

## 3.5 Diagrama ER — scheduling_db

```
┌──────────────────────────────┐       ┌──────────────────────────────┐
│        appointments          │       │       cancellations          │
├──────────────────────────────┤       ├──────────────────────────────┤
│ id             BIGINT PK     │◄──────│ appointment_id BIGINT FK     │
│ patient_id     BIGINT        │       │ id             BIGINT PK     │
│ doctor_id      BIGINT        │       │ reason         TEXT          │
│ date_time      DATETIME      │       │ cancelled_by   VARCHAR(100) │
│ status         ENUM          │       │ cancelled_at   TIMESTAMP    │
│ type           ENUM          │       └──────────────────────────────┘
│ return_of_id   BIGINT FK(self)│
│ created_at     TIMESTAMP     │
│ updated_at     TIMESTAMP     │
└──────────────────────────────┘
```

```sql
CREATE DATABASE IF NOT EXISTS scheduling_db;
USE scheduling_db;

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    date_time DATETIME NOT NULL,
    status ENUM('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    type ENUM('FIRST_VISIT', 'FOLLOW_UP', 'RETURN') DEFAULT 'FIRST_VISIT',
    return_of_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_return_of FOREIGN KEY (return_of_id) REFERENCES appointments(id),
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_datetime (date_time),
    INDEX idx_status (status),
    UNIQUE INDEX idx_doctor_datetime (doctor_id, date_time)
);

CREATE TABLE cancellations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    reason TEXT NOT NULL,
    cancelled_by VARCHAR(100) NOT NULL,
    cancelled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cancellation_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
```

> **Índice composto `idx_doctor_datetime`:** Garante que um médico não pode ter duas consultas no mesmo horário a nível de banco de dados. Mesmo que a validação falhe na aplicação, o banco impede a duplicação.

## 3.6 Diagrama ER — medical_record_db

```
┌─────────────────────────┐
│     medical_records     │
├─────────────────────────┤     ┌─────────────────────────┐
│ id         BIGINT PK    │◄────│     medical_visits      │
│ patient_id BIGINT       │     ├─────────────────────────┤
│ created_at TIMESTAMP    │     │ id          BIGINT PK   │
└─────────────────────────┘     │ record_id   BIGINT FK   │
                                │ appointment_id BIGINT   │
                                │ doctor_id   BIGINT      │
         ┌──────────────────────│ visit_date  DATETIME    │
         │                      │ symptoms    TEXT        │
         │                      │ diagnosis   TEXT        │
         │                      │ observations TEXT       │
         │                      │ created_at  TIMESTAMP   │
         │                      └─────────────────────────┘
         │
         ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│     prescriptions       │     │     exam_requests       │
├─────────────────────────┤     ├─────────────────────────┤
│ id          BIGINT PK   │     │ id          BIGINT PK   │
│ visit_id    BIGINT FK   │     │ visit_id    BIGINT FK   │
│ medication  VARCHAR(200)│     │ exam_type   VARCHAR(100)│
│ dosage      VARCHAR(100)│     │ description TEXT        │
│ frequency   VARCHAR(100)│     │ urgency     ENUM       │
│ duration    VARCHAR(100)│     │ status      ENUM       │
│ observations TEXT       │     │ result_date DATETIME    │
│ created_at  TIMESTAMP   │     │ result      TEXT        │
└─────────────────────────┘     │ created_at  TIMESTAMP   │
                                └─────────────────────────┘
```

```sql
CREATE DATABASE IF NOT EXISTS medical_record_db;
USE medical_record_db;

CREATE TABLE medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_patient (patient_id)
);

CREATE TABLE medical_visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    appointment_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    visit_date DATETIME NOT NULL,
    symptoms TEXT,
    diagnosis TEXT,
    observations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visit_record FOREIGN KEY (record_id) REFERENCES medical_records(id),
    INDEX idx_record (record_id),
    INDEX idx_appointment (appointment_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_date (visit_date)
);

CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id BIGINT NOT NULL,
    medication VARCHAR(200) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(100) NOT NULL,
    observations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescription_visit FOREIGN KEY (visit_id) REFERENCES medical_visits(id)
);

CREATE TABLE exam_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id BIGINT NOT NULL,
    exam_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    urgency ENUM('NORMAL', 'URGENTE', 'EMERGENCIAL') DEFAULT 'NORMAL',
    status ENUM('REQUESTED', 'SCHEDULED', 'COMPLETED') DEFAULT 'REQUESTED',
    result_date DATETIME NULL,
    result TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exam_visit FOREIGN KEY (visit_id) REFERENCES medical_visits(id),
    INDEX idx_status (status),
    INDEX idx_urgency (urgency)
);
```

## 3.7 Boas Práticas de Modelagem Aplicadas

| Prática | Aplicação |
|---|---|
| **Normalização (3FN)** | Todos os bancos estão na 3ª forma normal |
| **Soft Delete** | Campo `active` em vez de DELETE físico |
| **Auditoria** | `created_at` e `updated_at` em todas as tabelas |
| **Índices** | Campos de busca frequente possuem INDEX |
| **Constraints** | UNIQUE em CPF, CRM, CNPJ — integridade no banco |
| **ENUM** | Usado para campos com valores fixos (status, gender) |
| **Auto-referência** | `appointments.return_of_id` referencia a própria tabela |

---

# 4. ESTRUTURA DE PASTAS

## 4.1 Estrutura Raiz do Projeto

```
clinica-medica/
├── commons/
├── auth-service/
├── admin-service/
├── patient-service/
├── scheduling-service/
├── medical-record-service/
├── docker-compose.yml
├── build-and-run.sh
├── .gitignore
├── README.md
└── docs/
    ├── architecture.md
    ├── api-contracts.md
    ├── database-model.md
    └── postman/
        ├── auth-service.postman_collection.json
        ├── admin-service.postman_collection.json
        ├── patient-service.postman_collection.json
        ├── scheduling-service.postman_collection.json
        └── medical-record-service.postman_collection.json
```

## 4.2 Estrutura Padrão de um Microsserviço

Todos os microsserviços seguem a **mesma estrutura de pacotes**. Exemplo usando `admin-service`:

```
admin-service/
├── src/
│   ├── main/
│   │   ├── java/br/com/clinica/admin/
│   │   │   ├── AdminServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── EmployeeController.java
│   │   │   │   ├── SpecialtyController.java
│   │   │   │   ├── DoctorController.java
│   │   │   │   └── HealthInsuranceController.java
│   │   │   ├── service/
│   │   │   │   ├── EmployeeService.java
│   │   │   │   ├── SpecialtyService.java
│   │   │   │   ├── DoctorService.java
│   │   │   │   └── HealthInsuranceService.java
│   │   │   ├── repository/
│   │   │   │   ├── EmployeeRepository.java
│   │   │   │   ├── SpecialtyRepository.java
│   │   │   │   ├── DoctorRepository.java
│   │   │   │   └── HealthInsuranceRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Employee.java
│   │   │   │   ├── Specialty.java
│   │   │   │   ├── Doctor.java
│   │   │   │   └── HealthInsurance.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── EmployeeRequest.java
│   │   │   │   │   ├── SpecialtyRequest.java
│   │   │   │   │   ├── DoctorRequest.java
│   │   │   │   │   └── HealthInsuranceRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── EmployeeResponse.java
│   │   │   │       ├── SpecialtyResponse.java
│   │   │   │       ├── DoctorResponse.java
│   │   │   │       └── HealthInsuranceResponse.java
│   │   │   ├── mapper/
│   │   │   │   ├── EmployeeMapper.java
│   │   │   │   ├── SpecialtyMapper.java
│   │   │   │   ├── DoctorMapper.java
│   │   │   │   └── HealthInsuranceMapper.java
│   │   │   ├── exception/
│   │   │   │   └── (usa GlobalExceptionHandler do commons)
│   │   │   ├── config/
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   └── validation/
│   │   │       ├── CpfValid.java (annotation customizada)
│   │   │       └── CpfValidator.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/ (se usar Flyway)
│   └── test/
│       └── java/br/com/clinica/admin/
│           ├── controller/
│           │   └── EmployeeControllerTest.java
│           ├── service/
│           │   └── EmployeeServiceTest.java
│           └── repository/
│               └── EmployeeRepositoryTest.java
├── Dockerfile
└── pom.xml
```

## 4.3 Papel de Cada Camada

```
Request HTTP
     │
     ▼
┌─────────────┐   Recebe request, valida input,
│ Controller  │   delega para Service, retorna Response
└──────┬──────┘
       │
       ▼
┌─────────────┐   Contém regras de negócio,
│  Service    │   orquestra operações, chama Repository e Feign Clients
└──────┬──────┘
       │
       ▼
┌─────────────┐   Acessa banco de dados,
│ Repository  │   executa queries JPA
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Database   │
└─────────────┘
```

| Camada | Responsabilidade | Regras |
|---|---|---|
| **Controller** | Recebe HTTP, valida DTO, retorna response | NUNCA contém lógica de negócio |
| **Service** | Regras de negócio, validações, orquestração | NUNCA acessa `HttpServletRequest` |
| **Repository** | Interface JPA para acesso a dados | NUNCA contém lógica de negócio |
| **Entity** | Mapeamento JPA da tabela do banco | NUNCA exposta na API (usar DTOs) |
| **DTO Request** | Dados recebidos do cliente | Contém validações Bean Validation |
| **DTO Response** | Dados retornados ao cliente | Apenas campos necessários |
| **Mapper** | Converte Entity ↔ DTO | Classe estática ou component |
| **Config** | Configurações Spring (Swagger, CORS, etc.) | — |
| **Exception** | Exceções customizadas do domínio | Padronizadas pelo commons |

## 4.4 Exemplo: application.yml do admin-service

```yaml
server:
  port: 8081

spring:
  application:
    name: admin-service
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3307}/admin_db
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    br.com.clinica: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

*Continua na Parte 3 → Padronização Corporativa, Ordem de Desenvolvimento e Cronograma*

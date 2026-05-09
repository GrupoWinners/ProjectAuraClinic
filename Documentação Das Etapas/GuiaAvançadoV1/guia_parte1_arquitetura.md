# 🏥 GUIA DE IMPLEMENTAÇÃO — SISTEMA DE CLÍNICA MÉDICA
## Parte 1: Arquitetura e Microsserviços

> **Nível:** Intermediário | **Stack:** Java 17 + Spring Boot 3.2 | **Arquitetura:** Microsserviços

---

# 1. VISÃO GERAL DA ARQUITETURA

## 1.1 Arquitetura Escolhida

O sistema adota **Arquitetura de Microsserviços** com comunicação **síncrona via REST/HTTP** usando **OpenFeign**.

Cada microsserviço é um projeto Spring Boot independente, com seu próprio banco de dados MySQL (**Database per Service Pattern**), deploy independente e responsabilidade única sobre um domínio de negócio.

## 1.2 Por Que Microsserviços?

| Aspecto | Monolito | Microsserviços (Escolhido) |
|---|---|---|
| Deploy | Tudo junto — um bug trava tudo | Independente — cada serviço é deployado separado |
| Escalabilidade | Escala tudo ou nada | Escala apenas o serviço sobrecarregado |
| Equipe | Todos mexem no mesmo código | Cada time cuida de um serviço |
| Tecnologia | Uma stack para tudo | Liberdade tecnológica por serviço |
| Complexidade | Simples no início | Maior complexidade de infraestrutura |
| Resiliência | Falha afeta todo o sistema | Falha isolada por serviço |

**Motivação principal para este projeto:** Aprendizado de padrões corporativos reais. Empresas como Nubank, iFood, Mercado Livre utilizam microsserviços. Ao adotar essa arquitetura, os alunos aprendem:
- Como separar domínios de negócio
- Como serviços se comunicam via HTTP
- Como gerenciar múltiplos bancos de dados
- Como dockerizar e orquestrar containers

## 1.3 Vantagens Concretas no Projeto

1. **Independência de deploy** — Se o `scheduling-service` tem bug, o `admin-service` continua operando
2. **Divisão de trabalho** — Cada membro da equipe pode trabalhar em um serviço diferente
3. **Banco isolado** — Mudanças no schema de pacientes não afetam o schema de agendamentos
4. **Testabilidade** — Cada serviço é testado isoladamente

## 1.4 Desvantagens e Desafios

1. **Complexidade de infraestrutura** — Mais containers, mais configuração
2. **Latência de rede** — Chamadas HTTP entre serviços são mais lentas que chamadas em memória
3. **Consistência eventual** — Dados podem ficar temporariamente inconsistentes entre serviços
4. **Debugging distribuído** — Rastrear um erro que cruza serviços exige correlation-id

## 1.5 Diagrama Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CLIENTE (Browser / Postman)                      │
└─────────┬──────────┬──────────────┬──────────────┬─────────────────────┘
          │          │              │              │
          ▼          ▼              ▼              ▼
  ┌──────────┐ ┌──────────┐ ┌────────────┐ ┌──────────────────┐
  │  auth    │ │  admin   │ │  patient   │ │   scheduling     │
  │ service  │ │ service  │ │  service   │ │    service       │
  │  :8080   │ │  :8081   │ │   :8082    │ │     :8083        │
  └────┬─────┘ └────┬─────┘ └─────┬──────┘ └───────┬──────────┘
       │            │             │                 │
       │   Feign    │    Feign    │      Feign      │
       │◄───────────┤◄────────────┤◄────────────────┤
       │            │             │                 │
       ▼            ▼             ▼                 ▼
  ┌──────────┐ ┌──────────┐ ┌──────────┐    ┌──────────────────┐
  │ auth_db  │ │ admin_db │ │patient_db│    │  scheduling_db   │
  │  MySQL   │ │  MySQL   │ │  MySQL   │    │     MySQL        │
  └──────────┘ └──────────┘ └──────────┘    └──────────────────┘

                                                    │  Feign
                                                    ▼
                                            ┌──────────────────┐
                                            │  medical-record  │
                                            │    service       │
                                            │     :8084        │
                                            └───────┬──────────┘
                                                    │
                                                    ▼
                                            ┌──────────────────┐
                                            │medical_record_db │
                                            │     MySQL        │
                                            └──────────────────┘
```

## 1.6 Fluxo Geral do Sistema

```
1. Administrador faz login → auth-service valida credenciais → retorna JWT
2. Admin cadastra funcionários, especialidades, médicos, convênios → admin-service
3. Admin cadastra pacientes → patient-service
4. Recepcionista agenda consulta → scheduling-service consulta patient-service (Feign) para validar paciente
5. Médico atende paciente → medical-record-service consulta patient-service e scheduling-service
6. Médico registra diagnóstico, receita, exames → medical-record-service
```

## 1.7 Princípios Arquiteturais

| Princípio | Aplicação no Projeto |
|---|---|
| **Single Responsibility** | Cada serviço = um domínio |
| **Baixo Acoplamento** | Serviços comunicam via HTTP, não compartilham banco |
| **Alta Coesão** | Toda lógica de paciente fica em patient-service |
| **Database per Service** | Cada serviço tem seu MySQL isolado |
| **API First** | Contratos definidos antes da implementação |
| **Fail Fast** | Validações na entrada, respostas rápidas de erro |

---

# 2. DEFINIÇÃO DOS MICROSSERVIÇOS

## 2.1 Mapa de Serviços

| Serviço | Porta | Banco | Responsabilidade |
|---|---|---|---|
| `auth-service` | 8080 | `auth_db` | Autenticação, autorização, JWT, gestão de usuários |
| `admin-service` | 8081 | `admin_db` | Funcionários, especialidades, médicos, convênios |
| `patient-service` | 8082 | `patient_db` | Cadastro de pacientes |
| `scheduling-service` | 8083 | `scheduling_db` | Agendamentos, cancelamentos, retornos |
| `medical-record-service` | 8084 | `medical_record_db` | Prontuário, atendimentos, receitas, exames |
| `commons` | — | — | Módulo compartilhado: DTOs, exceptions, configs |

---

## 2.2 auth-service (Porta 8080)

### Responsabilidades
- Autenticação de usuários (login/logout)
- Geração e validação de tokens JWT
- Gestão de usuários e perfis de acesso
- Controle de permissões (RBAC — Role Based Access Control)

### Entidades
- `User` (idUser, username, password, role, employeeId, active, createdAt, updatedAt)
- `Role` (id, name, description)

### Endpoints Principais

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autenticar usuário |
| `POST` | `/api/v1/auth/register` | Registrar novo usuário |
| `POST` | `/api/v1/auth/refresh` | Renovar token JWT |
| `GET` | `/api/v1/auth/validate` | Validar token |
| `GET` | `/api/v1/users` | Listar usuários |
| `GET` | `/api/v1/users/{id}` | Buscar usuário por ID |
| `PUT` | `/api/v1/users/{id}` | Atualizar usuário |
| `DELETE` | `/api/v1/users/{id}` | Desativar usuário |
| `PUT` | `/api/v1/users/{id}/roles` | Atualizar permissões |

### Regras de Negócio
1. Senha deve ter mínimo 8 caracteres, 1 maiúscula, 1 número, 1 especial
2. Usuário é vinculado a um funcionário (employeeId validado via Feign no admin-service)
3. Token JWT expira em 24h; refresh token em 7 dias
4. Máximo 5 tentativas de login — bloqueia por 15 minutos
5. Senhas armazenadas com BCrypt (strength 12)

### Dependências (Feign)
- `admin-service` → validar se employeeId existe

### Exemplo de Payload — Login

**Request:**
```json
{
  "username": "admin@clinica.com",
  "password": "Senh@F0rt3!"
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "expiresIn": 86400,
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "admin@clinica.com",
    "role": "ADMIN"
  }
}
```

---

## 2.3 admin-service (Porta 8081)

### Responsabilidades
- CRUD de Funcionários
- CRUD de Especialidades médicas
- CRUD de Médicos
- CRUD de Convênios

### Entidades
- `Employee` (id, fullName, rg, cpf, address, neighborhood, city, state, zipCode, phone, cellphone, ctpsNumber, pisNumber, active, createdAt, updatedAt)
- `Specialty` (id, description, active)
- `Doctor` (id, name, crm, specialtyId, active, createdAt, updatedAt)
- `HealthInsurance` (id, companyName, cnpj, phone, active, createdAt, updatedAt)

### Endpoints Principais

**Funcionários:**

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/employees` | Criar funcionário |
| `GET` | `/api/v1/employees` | Listar funcionários (paginado) |
| `GET` | `/api/v1/employees/{id}` | Buscar por ID |
| `GET` | `/api/v1/employees/cpf/{cpf}` | Buscar por CPF |
| `PUT` | `/api/v1/employees/{id}` | Atualizar |
| `DELETE` | `/api/v1/employees/{id}` | Soft delete |
| `GET` | `/api/v1/employees/{id}/validate-active` | Validar se está ativo |

**Especialidades:**

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/specialties` | Criar especialidade |
| `GET` | `/api/v1/specialties` | Listar todas |
| `GET` | `/api/v1/specialties/{id}` | Buscar por ID |
| `PUT` | `/api/v1/specialties/{id}` | Atualizar |
| `DELETE` | `/api/v1/specialties/{id}` | Soft delete |

**Médicos:**

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/doctors` | Criar médico |
| `GET` | `/api/v1/doctors` | Listar médicos |
| `GET` | `/api/v1/doctors/{id}` | Buscar por ID |
| `GET` | `/api/v1/doctors/crm/{crm}` | Buscar por CRM |
| `GET` | `/api/v1/doctors/specialty/{specialtyId}` | Listar por especialidade |
| `PUT` | `/api/v1/doctors/{id}` | Atualizar |
| `DELETE` | `/api/v1/doctors/{id}` | Soft delete |
| `GET` | `/api/v1/doctors/{id}/validate-active` | Validar se está ativo |

**Convênios:**

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/health-insurances` | Criar convênio |
| `GET` | `/api/v1/health-insurances` | Listar convênios |
| `GET` | `/api/v1/health-insurances/{id}` | Buscar por ID |
| `PUT` | `/api/v1/health-insurances/{id}` | Atualizar |
| `DELETE` | `/api/v1/health-insurances/{id}` | Soft delete |

### Regras de Negócio
1. CPF e RG devem ser únicos para funcionários
2. CRM deve ser único para médicos
3. CNPJ deve ser único para convênios
4. CPF deve ser validado (algoritmo de validação)
5. CNPJ deve ser validado
6. Exclusões são lógicas (soft delete — campo `active = false`)
7. Médico deve ter uma especialidade válida cadastrada

### Dependências (Feign)
- Nenhuma (é consumido por outros serviços)

---

## 2.4 patient-service (Porta 8082)

### Responsabilidades
- CRUD de Pacientes
- Validação de paciente ativo (endpoint de integração)
- Vinculação com convênio

### Entidades
- `Patient` (id, fullName, rg, cpf, address, city, state, zipCode, phone, cellphone, birthDate, gender, hasInsurance, insuranceId, active, createdAt, updatedAt)

### Endpoints Principais

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/patients` | Criar paciente |
| `GET` | `/api/v1/patients` | Listar pacientes (paginado) |
| `GET` | `/api/v1/patients/{id}` | Buscar por ID |
| `GET` | `/api/v1/patients/cpf/{cpf}` | Buscar por CPF |
| `PUT` | `/api/v1/patients/{id}` | Atualizar |
| `DELETE` | `/api/v1/patients/{id}` | Soft delete |
| `GET` | `/api/v1/patients/{id}/validate-active` | Validar se paciente está ativo |
| `GET` | `/api/v1/patients/search?name={name}` | Buscar por nome |

### Regras de Negócio
1. CPF deve ser único e válido
2. RG deve ser único
3. Se `hasInsurance = true`, o `insuranceId` é obrigatório
4. insuranceId é validado via Feign no admin-service
5. Data de nascimento não pode ser futura
6. Gênero aceita: MASCULINO, FEMININO, OUTRO

### Dependências (Feign)
- `admin-service` → validar convênio (health-insurance)

---

## 2.5 scheduling-service (Porta 8083)

### Responsabilidades
- Agendamento de consultas
- Cancelamento de consultas
- Agendamento de retornos
- Controle de horários
- Validação de conflitos

### Entidades
- `Appointment` (id, patientId, doctorId, dateTime, status, type, createdAt, updatedAt)
- `Cancellation` (id, appointmentId, reason, cancelledBy, password, cancelledAt)

### Status possíveis do agendamento
```
SCHEDULED → CONFIRMED → IN_PROGRESS → COMPLETED
                ↓
            CANCELLED
```

### Endpoints Principais

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/appointments` | Agendar consulta |
| `GET` | `/api/v1/appointments` | Listar agendamentos (filtros) |
| `GET` | `/api/v1/appointments/{id}` | Buscar por ID |
| `GET` | `/api/v1/appointments/doctor/{doctorId}` | Listar por médico |
| `GET` | `/api/v1/appointments/patient/{patientId}` | Listar por paciente |
| `GET` | `/api/v1/appointments/available-slots` | Buscar horários disponíveis |
| `PUT` | `/api/v1/appointments/{id}/cancel` | Cancelar consulta |
| `POST` | `/api/v1/appointments/{id}/return` | Agendar retorno |
| `PUT` | `/api/v1/appointments/{id}/status` | Atualizar status |

### Regras de Negócio
1. Paciente deve estar cadastrado e ativo (valida via Feign → patient-service)
2. Médico deve estar cadastrado e ativo (valida via Feign → admin-service)
3. Não pode haver conflito de horário para o mesmo médico
4. Não pode haver conflito de horário para o mesmo paciente
5. Consultas duram 30 minutos por padrão
6. Horário de atendimento: 08:00 às 18:00, segunda a sexta
7. Cancelamento exige senha do usuário (validada via Feign → auth-service)
8. Cancelamento deve conter motivo obrigatório
9. Cancelamento libera o horário automaticamente
10. Retorno é um novo agendamento com type = RETURN, vinculado ao original

### Dependências (Feign)
- `patient-service` → validar paciente ativo
- `admin-service` → validar médico ativo
- `auth-service` → validar senha no cancelamento

### Exemplo de Payload — Agendar Consulta

**Request:**
```json
{
  "patientId": 1,
  "doctorId": 3,
  "dateTime": "2026-06-15T10:00:00",
  "type": "FIRST_VISIT"
}
```

**Response (201):**
```json
{
  "id": 42,
  "patientId": 1,
  "patientName": "João da Silva",
  "doctorId": 3,
  "doctorName": "Dra. Maria Souza",
  "doctorCrm": "CRM/SP 12345",
  "specialty": "Cardiologia",
  "dateTime": "2026-06-15T10:00:00",
  "status": "SCHEDULED",
  "type": "FIRST_VISIT",
  "createdAt": "2026-05-09T09:30:00"
}
```

---

## 2.6 medical-record-service (Porta 8084)

### Responsabilidades
- Prontuário eletrônico do paciente
- Registro de atendimentos
- Registro de diagnósticos e sintomas
- Receituário médico
- Solicitação de exames
- Histórico médico completo

### Entidades
- `MedicalRecord` (id, patientId, createdAt)
- `MedicalVisit` (id, medicalRecordId, appointmentId, doctorId, visitDate, symptoms, diagnosis, observations, createdAt)
- `Prescription` (id, medicalVisitId, medication, dosage, frequency, duration, observations, createdAt)
- `ExamRequest` (id, medicalVisitId, examType, description, urgency, status, resultDate, result, createdAt)

### Endpoints Principais

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/medical-records/patient/{patientId}` | Prontuário completo do paciente |
| `POST` | `/api/v1/medical-visits` | Registrar atendimento |
| `GET` | `/api/v1/medical-visits/{id}` | Buscar atendimento |
| `GET` | `/api/v1/medical-visits/patient/{patientId}` | Histórico de atendimentos |
| `POST` | `/api/v1/prescriptions` | Registrar receita |
| `GET` | `/api/v1/prescriptions/visit/{visitId}` | Receitas de um atendimento |
| `GET` | `/api/v1/prescriptions/patient/{patientId}` | Todas receitas do paciente |
| `POST` | `/api/v1/exam-requests` | Solicitar exame |
| `GET` | `/api/v1/exam-requests/visit/{visitId}` | Exames de um atendimento |
| `GET` | `/api/v1/exam-requests/patient/{patientId}` | Todos exames do paciente |
| `PUT` | `/api/v1/exam-requests/{id}/result` | Registrar resultado de exame |

### Regras de Negócio
1. Prontuário é criado automaticamente no primeiro atendimento do paciente
2. Cada atendimento é vinculado a um agendamento (appointmentId)
3. O agendamento deve estar com status IN_PROGRESS ou COMPLETED
4. Apenas médicos podem registrar atendimentos
5. Receita deve conter: medicamento, dosagem, frequência e duração
6. Exame pode ter urgência: NORMAL, URGENTE, EMERGENCIAL
7. Status do exame: REQUESTED → SCHEDULED → COMPLETED
8. Resultado do exame é texto livre + possibilidade de anexo (futuro)

### Dependências (Feign)
- `patient-service` → buscar dados do paciente
- `scheduling-service` → validar agendamento
- `admin-service` → buscar dados do médico

### Exemplo de Payload — Registrar Atendimento

**Request:**
```json
{
  "appointmentId": 42,
  "doctorId": 3,
  "patientId": 1,
  "symptoms": "Dor no peito ao esforço, falta de ar",
  "diagnosis": "Angina estável — CID I20.8",
  "observations": "Paciente relata início dos sintomas há 2 semanas. Solicitar ECG e enzimas cardíacas.",
  "prescriptions": [
    {
      "medication": "Isordil 5mg",
      "dosage": "1 comprimido",
      "frequency": "Sublingual em caso de dor",
      "duration": "Uso contínuo",
      "observations": "Orientar paciente sobre uso correto"
    }
  ],
  "examRequests": [
    {
      "examType": "ECG",
      "description": "Eletrocardiograma de repouso",
      "urgency": "URGENTE"
    },
    {
      "examType": "LABORATORIAL",
      "description": "Troponina, CK-MB, BNP",
      "urgency": "URGENTE"
    }
  ]
}
```

---

## 2.7 commons (Módulo Compartilhado)

### Responsabilidades
- DTOs compartilhados entre serviços
- Exception handlers globais
- Padrão de resposta de erro
- Configurações comuns
- Utilitários (validadores de CPF, CNPJ, formatadores)

### Estrutura

```
commons/
├── src/main/java/br/com/clinica/commons/
│   ├── dto/
│   │   ├── ErrorResponse.java
│   │   ├── PageResponse.java
│   │   └── ValidationError.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── DuplicateResourceException.java
│   │   ├── IntegrationException.java
│   │   └── GlobalExceptionHandler.java
│   ├── config/
│   │   ├── LogbookConfig.java
│   │   └── JacksonConfig.java
│   ├── util/
│   │   ├── CpfValidator.java
│   │   ├── CnpjValidator.java
│   │   └── DateUtils.java
│   └── constants/
│       └── ApiConstants.java
└── pom.xml
```

> **Como usar:** O `commons` é um módulo Maven. Cada microsserviço adiciona como dependência no `pom.xml`:
> ```xml
> <dependency>
>     <groupId>br.com.clinica</groupId>
>     <artifactId>commons</artifactId>
>     <version>${project.version}</version>
> </dependency>
> ```

---

## 2.8 Matriz de Comunicação entre Serviços

| Serviço Origem | Serviço Destino | Endpoint Consumido | Motivo |
|---|---|---|---|
| `auth-service` | `admin-service` | `GET /api/v1/employees/{id}/validate-active` | Validar funcionário ao criar usuário |
| `patient-service` | `admin-service` | `GET /api/v1/health-insurances/{id}` | Validar convênio do paciente |
| `scheduling-service` | `patient-service` | `GET /api/v1/patients/{id}/validate-active` | Validar paciente ao agendar |
| `scheduling-service` | `admin-service` | `GET /api/v1/doctors/{id}/validate-active` | Validar médico ao agendar |
| `scheduling-service` | `auth-service` | `POST /api/v1/auth/validate-password` | Validar senha no cancelamento |
| `medical-record-service` | `patient-service` | `GET /api/v1/patients/{id}` | Buscar dados do paciente |
| `medical-record-service` | `scheduling-service` | `GET /api/v1/appointments/{id}` | Validar agendamento |
| `medical-record-service` | `admin-service` | `GET /api/v1/doctors/{id}` | Buscar dados do médico |

---

*Continua na Parte 2 → Modelagem de Banco de Dados, Estrutura de Pastas e Padronização*

# 🏥 GUIA DE IMPLEMENTAÇÃO — SISTEMA DE CLÍNICA MÉDICA
## Parte 3: Padronização, Cronograma e Tarefas

---

# 5. PADRONIZAÇÃO CORPORATIVA

## 5.1 Convenção de Nomes

### Classes Java

| Tipo | Convenção | Exemplo |
|---|---|---|
| Entity | `NomeSingular` | `Patient`, `Doctor`, `Appointment` |
| Repository | `NomeRepository` | `PatientRepository` |
| Service | `NomeService` | `PatientService` |
| Controller | `NomeController` | `PatientController` |
| DTO Request | `NomeRequest` | `PatientRequest` |
| DTO Response | `NomeResponse` | `PatientResponse` |
| Mapper | `NomeMapper` | `PatientMapper` |
| Exception | `NomeException` | `ResourceNotFoundException` |
| Feign Client | `NomeServiceClient` | `PatientServiceClient` |
| Config | `NomeConfig` | `SwaggerConfig` |
| Enum | `NomeSingularMaiúsculo` | `AppointmentStatus`, `Gender` |

### Pacotes Java

```
br.com.clinica.<serviço>.<camada>
```

Exemplos:
- `br.com.clinica.admin.controller`
- `br.com.clinica.admin.service`
- `br.com.clinica.admin.entity`
- `br.com.clinica.scheduling.dto.request`

### Variáveis e Métodos

| Tipo | Convenção | Exemplo |
|---|---|---|
| Variável local | camelCase | `patientName`, `appointmentDate` |
| Constante | UPPER_SNAKE_CASE | `MAX_LOGIN_ATTEMPTS`, `JWT_EXPIRATION` |
| Método | camelCase, verbo no início | `findById()`, `createPatient()`, `validateCpf()` |
| Boolean | prefixo `is/has` | `isActive`, `hasInsurance` |

## 5.2 Nomenclatura de Endpoints

Padrão: `/api/v1/<recurso-no-plural>`

| Operação | Método HTTP | Endpoint | Corpo |
|---|---|---|---|
| Criar | `POST` | `/api/v1/patients` | Request body |
| Listar todos | `GET` | `/api/v1/patients` | — |
| Buscar por ID | `GET` | `/api/v1/patients/{id}` | — |
| Atualizar | `PUT` | `/api/v1/patients/{id}` | Request body |
| Deletar (soft) | `DELETE` | `/api/v1/patients/{id}` | — |
| Ação específica | `PUT` | `/api/v1/appointments/{id}/cancel` | Request body |
| Filtro | `GET` | `/api/v1/patients?name=João&page=0&size=10` | — |

**Regras:**
- Sempre plural: `/patients`, não `/patient`
- Sempre lowercase com hífens: `/health-insurances`, não `/healthInsurances`
- Versão na URL: `/api/v1/`
- Nunca verbos na URL: ~~`/api/v1/getPatients`~~

## 5.3 Padronização de Commits (Conventional Commits)

```
<tipo>(<escopo>): <mensagem curta>

[corpo opcional]
[rodapé opcional]
```

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Documentação |
| `style` | Formatação (sem mudança de lógica) |
| `refactor` | Refatoração de código |
| `test` | Adição ou correção de testes |
| `chore` | Tarefas de build, dependências, configs |
| `ci` | Mudanças em CI/CD |

**Exemplos:**
```
feat(patient-service): add patient CRUD endpoints
fix(scheduling-service): fix time conflict validation
docs(admin-service): add swagger annotations to employee endpoints
test(patient-service): add unit tests for PatientService
chore(docker): add docker-compose for all services
refactor(commons): extract CPF validation to utility class
```

## 5.4 Padronização de Branches (GitFlow)

```
main                    ← Produção (código estável, tags de release)
  └── develop           ← Integração (merge de todas as features)
       ├── feature/admin-service-employee-crud
       ├── feature/patient-service-crud
       ├── feature/scheduling-service-appointments
       ├── feature/medical-record-service-visits
       ├── feature/auth-service-jwt
       ├── feature/docker-compose-setup
       ├── bugfix/fix-cpf-validation
       └── hotfix/fix-login-security-breach
```

**Regras:**
1. **Nunca commitar direto em `main` ou `develop`**
2. Criar branch a partir de `develop`
3. Nomear: `feature/<serviço>-<descrição-curta>`
4. Pull Request obrigatório para merge em `develop`
5. Pelo menos 1 revisão de código antes do merge
6. `main` recebe apenas merges de `develop` via release

## 5.5 Versionamento Semântico

Formato: `MAJOR.MINOR.PATCH`

| Número | Quando incrementa | Exemplo |
|---|---|---|
| MAJOR | Breaking changes | 1.0.0 → 2.0.0 |
| MINOR | Nova feature, compatível | 1.0.0 → 1.1.0 |
| PATCH | Correção de bug | 1.0.0 → 1.0.1 |

Para este projeto acadêmico, versão inicial: **1.0.0**

---

# 6. ORDEM IDEAL DE DESENVOLVIMENTO

## 6.1 Sequência com Justificativas

```
FASE 1: Fundação
   └── commons → base compartilhada (exceptions, DTOs, configs)
   └── Docker Compose → infraestrutura de bancos

FASE 2: Domínios Independentes
   └── admin-service → não depende de nenhum serviço
   └── auth-service → depende apenas de admin-service (validar employee)

FASE 3: Domínios Dependentes
   └── patient-service → depende de admin-service (validar convênio)

FASE 4: Orquestração
   └── scheduling-service → depende de patient-service e admin-service

FASE 5: Complexidade
   └── medical-record-service → depende de todos os anteriores
```

## 6.2 Por Que Esta Ordem?

| Fase | Serviço | Justificativa |
|---|---|---|
| 1 | `commons` | Todas as exceptions, DTOs e configs são compartilhados — precisa existir primeiro |
| 1 | Docker Compose | Bancos MySQL precisam estar rodando para qualquer serviço funcionar |
| 2 | `admin-service` | **Não depende de nenhum outro serviço.** Fornece dados base: funcionários, médicos, especialidades, convênios |
| 2 | `auth-service` | Depende apenas de `admin-service` (validar employee). Com auth pronto, todos os outros serviços podem ter segurança |
| 3 | `patient-service` | Depende de `admin-service` para validar convênios. Pacientes são necessários para agendamentos |
| 4 | `scheduling-service` | Depende de `patient-service` (validar paciente) e `admin-service` (validar médico) |
| 5 | `medical-record-service` | Depende de patient, scheduling e admin — é o mais complexo e o último |

## 6.3 Riscos de Alterar a Ordem

| Risco | Consequência |
|---|---|
| Começar pelo scheduling-service | Não terá pacientes nem médicos para validar, vai travar no Feign |
| Pular o commons | Cada serviço terá seu próprio padrão de erros, gerando inconsistência |
| Ignorar Docker Compose | Cada dev terá MySQL em porta diferente, causando problemas de ambiente |
| Deixar auth para o final | Todos os serviços ficarão sem segurança, refatorar depois é custoso |

---

# 7. CRONOGRAMA COMPLETO (8 Semanas)

## 7.1 Visão Geral

| Semana | Sprint | Entregas |
|---|---|---|
| 1 | Sprint 0 — Setup | Infraestrutura, Docker, commons, estrutura dos projetos |
| 2 | Sprint 1 — Admin | admin-service completo (CRUD funcionários, especialidades, médicos, convênios) |
| 3 | Sprint 2 — Auth | auth-service (JWT, login, register, roles) |
| 4 | Sprint 3 — Patient | patient-service (CRUD pacientes, integração Feign com admin) |
| 5 | Sprint 4 — Scheduling | scheduling-service (agendamentos, cancelamentos, retornos) |
| 6 | Sprint 5 — Medical | medical-record-service (prontuário, atendimentos, receitas, exames) |
| 7 | Sprint 6 — Qualidade | Testes, Swagger completo, Postman collections, bug fixes |
| 8 | Sprint 7 — Entrega | Documentação final, apresentação, deploy, revisão |

## 7.2 Detalhamento por Sprint

### Sprint 0 — Setup (Semana 1)
- [ ] Criar repositório no GitHub
- [ ] Configurar `.gitignore`
- [ ] Definir branches (main, develop)
- [ ] Criar `docker-compose.yml` com 5 MySQL + rede
- [ ] Criar módulo `commons` (exceptions, DTOs, configs)
- [ ] Criar estrutura base dos 5 microsserviços (Spring Initializr)
- [ ] Configurar `pom.xml` de cada serviço com dependências
- [ ] Criar `application.yml` de cada serviço
- [ ] Testar conexão de cada serviço com seu banco
- [ ] Criar `build-and-run.sh`
- [ ] Escrever `README.md` inicial

### Sprint 1 — Admin Service (Semana 2)
- [ ] Criar entities: Employee, Specialty, Doctor, HealthInsurance
- [ ] Criar repositories
- [ ] Criar DTOs (request/response)
- [ ] Criar mappers
- [ ] Criar services com regras de negócio
- [ ] Criar controllers REST
- [ ] Implementar validações (CPF, CNPJ, Bean Validation)
- [ ] Implementar soft delete
- [ ] Implementar paginação
- [ ] Adicionar Swagger annotations
- [ ] Testes unitários do Service
- [ ] Testes de integração do Controller
- [ ] Testar no Postman

### Sprint 2 — Auth Service (Semana 3)
- [ ] Criar entities: User, Role
- [ ] Configurar Spring Security
- [ ] Implementar JWT (geração, validação, refresh)
- [ ] Criar endpoints de login/register
- [ ] Implementar Feign Client para admin-service
- [ ] Implementar RBAC (Role Based Access Control)
- [ ] Implementar bloqueio por tentativas
- [ ] BCrypt para senhas
- [ ] Swagger annotations
- [ ] Testes unitários
- [ ] Testar no Postman

### Sprint 3 — Patient Service (Semana 4)
- [ ] Criar entity: Patient
- [ ] Criar repository, DTOs, mapper
- [ ] Criar service com regras de negócio
- [ ] Criar controller REST
- [ ] Implementar Feign Client para admin-service (validar convênio)
- [ ] Implementar validações (CPF, data de nascimento, gênero)
- [ ] Implementar busca por nome (LIKE)
- [ ] Endpoint `/validate-active` para integração
- [ ] Swagger annotations
- [ ] Testes unitários e de integração
- [ ] Testar no Postman

### Sprint 4 — Scheduling Service (Semana 5)
- [ ] Criar entities: Appointment, Cancellation
- [ ] Criar repository, DTOs, mapper
- [ ] Criar service com regras de negócio
- [ ] Criar Feign Clients (patient-service, admin-service, auth-service)
- [ ] Implementar validação de conflito de horário
- [ ] Implementar busca de horários disponíveis
- [ ] Implementar cancelamento com motivo + validação de senha
- [ ] Implementar agendamento de retorno
- [ ] Implementar máquina de estados do agendamento
- [ ] Swagger annotations
- [ ] Testes unitários e de integração
- [ ] Testar no Postman

### Sprint 5 — Medical Record Service (Semana 6)
- [ ] Criar entities: MedicalRecord, MedicalVisit, Prescription, ExamRequest
- [ ] Criar repositories, DTOs, mappers
- [ ] Criar services
- [ ] Criar Feign Clients (patient, scheduling, admin)
- [ ] Implementar criação automática de prontuário
- [ ] Implementar registro de atendimento com prescrições e exames
- [ ] Implementar histórico completo do paciente
- [ ] Swagger annotations
- [ ] Testes
- [ ] Testar no Postman

### Sprint 6 — Qualidade (Semana 7)
- [ ] Completar cobertura de testes (mínimo 70%)
- [ ] Completar documentação Swagger de todos os endpoints
- [ ] Criar Postman collections completas
- [ ] Corrigir bugs encontrados
- [ ] Refatorar código duplicado
- [ ] Revisar tratamento de exceções
- [ ] Revisar logs
- [ ] Testar todos os fluxos ponta a ponta
- [ ] Teste de cenários negativos

### Sprint 7 — Entrega (Semana 8)
- [ ] Documentação final (README, architecture.md)
- [ ] Preparar apresentação
- [ ] Gravar demo do sistema
- [ ] Code review final
- [ ] Tag release v1.0.0
- [ ] Merge em main
- [ ] Entregar

---

# 8. DIVISÃO DE EQUIPE

## 8.1 Papéis (equipe de 5 pessoas)

| Papel | Responsabilidades | Quem |
|---|---|---|
| **Tech Lead** | Arquitetura, revisão de código, decisões técnicas, merge PRs | Membro 1 |
| **Backend Dev 1** | `admin-service` + `commons` | Membro 2 |
| **Backend Dev 2** | `auth-service` + segurança JWT | Membro 3 |
| **Backend Dev 3** | `patient-service` + `scheduling-service` | Membro 4 |
| **Backend Dev 4 / QA** | `medical-record-service` + testes + Postman | Membro 5 |

## 8.2 Responsabilidades Compartilhadas

| Tarefa | Responsável |
|---|---|
| Docker Compose | Tech Lead |
| README e documentação | Todos (cada um documenta seu serviço) |
| Swagger | Cada dev documenta seus endpoints |
| Code Review | Tech Lead + pelo menos 1 dev |
| Postman Collections | QA + cada dev testa seu serviço |
| Apresentação | Todos |

## 8.3 Cerimônias Recomendadas

| Cerimônia | Frequência | Duração | Objetivo |
|---|---|---|---|
| Daily Standup | Diária | 15 min | O que fez, o que vai fazer, impedimentos |
| Sprint Planning | Semanal (início) | 1h | Definir tarefas da sprint |
| Sprint Review | Semanal (final) | 30 min | Demonstrar o que foi feito |
| Code Review | A cada Pull Request | 30 min | Qualidade de código |

---

# 9. LISTA COMPLETA DE TAREFAS TÉCNICAS

## 9.1 Tarefas por Microsserviço

### commons (12 tarefas)
1. Criar módulo Maven `commons`
2. Criar `ErrorResponse.java` (DTO padrão de erro)
3. Criar `PageResponse.java` (DTO padrão de paginação)
4. Criar `ValidationError.java`
5. Criar `BusinessException.java`
6. Criar `ResourceNotFoundException.java`
7. Criar `DuplicateResourceException.java`
8. Criar `IntegrationException.java`
9. Criar `GlobalExceptionHandler.java`
10. Criar `CpfValidator.java` (utilitário)
11. Criar `CnpjValidator.java` (utilitário)
12. Criar `ApiConstants.java`

### admin-service (28 tarefas)
1. Criar projeto Spring Boot via Spring Initializr
2. Configurar `pom.xml` (dependências + commons)
3. Configurar `application.yml`
4. Criar entity `Employee`
5. Criar entity `Specialty`
6. Criar entity `Doctor`
7. Criar entity `HealthInsurance`
8. Criar `EmployeeRepository`
9. Criar `SpecialtyRepository`
10. Criar `DoctorRepository`
11. Criar `HealthInsuranceRepository`
12. Criar `EmployeeRequest` + `EmployeeResponse`
13. Criar `SpecialtyRequest` + `SpecialtyResponse`
14. Criar `DoctorRequest` + `DoctorResponse`
15. Criar `HealthInsuranceRequest` + `HealthInsuranceResponse`
16. Criar `EmployeeMapper`
17. Criar `SpecialtyMapper`
18. Criar `DoctorMapper`
19. Criar `HealthInsuranceMapper`
20. Criar `EmployeeService` (CRUD + validações)
21. Criar `SpecialtyService`
22. Criar `DoctorService`
23. Criar `HealthInsuranceService`
24. Criar `EmployeeController`
25. Criar `SpecialtyController`
26. Criar `DoctorController`
27. Criar `HealthInsuranceController`
28. Adicionar anotações Swagger a todos os endpoints

### auth-service (18 tarefas)
1. Criar projeto Spring Boot
2. Configurar `pom.xml` (Spring Security, JWT, commons)
3. Configurar `application.yml`
4. Criar entity `User`
5. Criar entity `Role`
6. Criar `UserRepository`
7. Criar `RoleRepository`
8. Criar DTOs (LoginRequest, RegisterRequest, AuthResponse)
9. Criar `JwtService` (gerar, validar, refresh token)
10. Criar `AuthService` (login, register, validate)
11. Criar `UserService` (CRUD de usuários)
12. Criar `AuthController`
13. Criar `UserController`
14. Criar Feign Client `AdminServiceClient`
15. Configurar `SecurityFilterChain`
16. Criar `JwtAuthenticationFilter`
17. Implementar bloqueio por tentativas de login
18. Adicionar Swagger annotations

### patient-service (16 tarefas)
1. Criar projeto Spring Boot
2. Configurar `pom.xml` e `application.yml`
3. Criar entity `Patient`
4. Criar `PatientRepository`
5. Criar `PatientRequest` + `PatientResponse`
6. Criar `PatientMapper`
7. Criar `PatientService` (CRUD + validações)
8. Criar `PatientController`
9. Criar Feign Client `AdminServiceClient`
10. Implementar validação de CPF
11. Implementar validação de convênio via Feign
12. Implementar busca por nome (LIKE)
13. Implementar endpoint `/validate-active`
14. Implementar paginação
15. Adicionar Swagger annotations
16. Testes unitários e de integração

### scheduling-service (20 tarefas)
1. Criar projeto Spring Boot
2. Configurar `pom.xml` e `application.yml`
3. Criar entity `Appointment`
4. Criar entity `Cancellation`
5. Criar enums `AppointmentStatus`, `AppointmentType`
6. Criar repositories
7. Criar DTOs (AppointmentRequest, CancelRequest, etc.)
8. Criar mappers
9. Criar Feign Client `PatientServiceClient`
10. Criar Feign Client `AdminServiceClient`
11. Criar Feign Client `AuthServiceClient`
12. Criar `AppointmentService`
13. Implementar validação de conflito de horário
14. Implementar busca de horários disponíveis
15. Implementar cancelamento com motivo
16. Implementar agendamento de retorno
17. Implementar máquina de estados
18. Criar `AppointmentController`
19. Adicionar Swagger annotations
20. Testes unitários e de integração

### medical-record-service (22 tarefas)
1. Criar projeto Spring Boot
2. Configurar `pom.xml` e `application.yml`
3. Criar entity `MedicalRecord`
4. Criar entity `MedicalVisit`
5. Criar entity `Prescription`
6. Criar entity `ExamRequest`
7. Criar enums `ExamUrgency`, `ExamStatus`
8. Criar repositories
9. Criar DTOs (requests e responses)
10. Criar mappers
11. Criar Feign Client `PatientServiceClient`
12. Criar Feign Client `SchedulingServiceClient`
13. Criar Feign Client `AdminServiceClient`
14. Criar `MedicalRecordService`
15. Criar `MedicalVisitService`
16. Criar `PrescriptionService`
17. Criar `ExamRequestService`
18. Implementar criação automática de prontuário
19. Criar controllers
20. Implementar histórico completo do paciente
21. Adicionar Swagger annotations
22. Testes unitários e de integração

### Infraestrutura (10 tarefas)
1. Criar `docker-compose.yml` com 5 MySQL
2. Criar Dockerfile de cada serviço
3. Criar `build-and-run.sh`
4. Criar `.gitignore`
5. Criar `README.md` completo
6. Criar `docs/architecture.md`
7. Criar Postman collections (1 por serviço)
8. Criar dados iniciais (seeds SQL)
9. Configurar Logbook em todos os serviços
10. Tag release v1.0.0

**Total: ~126 tarefas**

---

*Continua na Parte 4 → Comunicação entre Microsserviços, Segurança, Exceções e Logs*

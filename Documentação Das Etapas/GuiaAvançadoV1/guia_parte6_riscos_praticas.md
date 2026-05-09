# 🏥 GUIA DE IMPLEMENTAÇÃO — SISTEMA DE CLÍNICA MÉDICA
## Parte 6: Versionamento, Checklists, Riscos e Boas Práticas

---

# 19. ESTRATÉGIA DE VERSIONAMENTO

## 19.1 GitFlow Detalhado

```
main ──────●───────────────────────────────●──────── (releases)
           │                               ▲
           │                               │ merge
           ▼                               │
develop ──●───●───●───●───●───●───●───●───●──────── (integração)
              │       │       ▲       ▲
              │       │       │       │
              ▼       ▼       │       │
        feature/A  feature/B  │       │
              │       │       │       │
              └───────┼───────┘       │
                      └───────────────┘
```

## 19.2 Workflow Passo a Passo

### Criar Feature Branch
```bash
git checkout develop
git pull origin develop
git checkout -b feature/admin-service-employee-crud
```

### Trabalhar na Feature
```bash
# Fazer commits seguindo Conventional Commits
git add .
git commit -m "feat(admin-service): create Employee entity with JPA annotations"
git commit -m "feat(admin-service): add EmployeeRepository with custom queries"
git commit -m "feat(admin-service): implement EmployeeService with CRUD operations"
git commit -m "test(admin-service): add unit tests for EmployeeService"
```

### Criar Pull Request
```bash
git push origin feature/admin-service-employee-crud
# Abrir PR no GitHub: feature/admin-service-employee-crud → develop
```

### Template de Pull Request
```markdown
## Descrição
Implementação do CRUD de Funcionários no admin-service.

## O que foi feito
- [x] Entity Employee com anotações JPA
- [x] EmployeeRepository
- [x] EmployeeService com validações (CPF, RG)
- [x] EmployeeController (REST endpoints)
- [x] DTOs (Request/Response)
- [x] Testes unitários (85% cobertura)
- [x] Swagger annotations

## Como testar
1. Subir admin-db: `docker-compose up admin-db`
2. Rodar: `mvn spring-boot:run`
3. Acessar: `http://localhost:8081/swagger-ui.html`
4. Testar POST /api/v1/employees

## Checklist
- [x] Código segue padrões do projeto
- [x] Testes passando
- [x] Swagger documentado
- [x] Sem System.out.println
- [x] Tratamento de exceções
```

### Code Review e Merge
```bash
# Após aprovação do PR:
# 1. Squash & Merge no GitHub
# 2. Deletar branch remota
# 3. Localmente:
git checkout develop
git pull origin develop
git branch -d feature/admin-service-employee-crud
```

### Criar Release
```bash
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0
# Ajustar versão no pom.xml, testar, corrigir bugs
git commit -m "chore: prepare release v1.0.0"
git checkout main
git merge release/v1.0.0
git tag -a v1.0.0 -m "Release v1.0.0 - MVP Clínica Médica"
git push origin main --tags
git checkout develop
git merge release/v1.0.0
git push origin develop
git branch -d release/v1.0.0
```

---

# 20. CHECKLISTS DE VALIDAÇÃO

## 20.1 Checklist Backend (por microsserviço)

- [ ] Entidades mapeadas com JPA (@Entity, @Table, @Column)
- [ ] Repositories criados com Spring Data JPA
- [ ] DTOs separados (Request/Response) — NUNCA expor Entity
- [ ] Mappers implementados (Entity ↔ DTO)
- [ ] Services com regras de negócio
- [ ] Controllers REST com endpoints padronizados
- [ ] Bean Validation nas DTOs (@NotBlank, @NotNull, @Valid)
- [ ] Soft delete implementado (campo active)
- [ ] Paginação nos endpoints de listagem
- [ ] Lombok usado (@Data, @Builder, @RequiredArgsConstructor)
- [ ] Sem `System.out.println` — usar `@Slf4j`
- [ ] Sem lógica de negócio no Controller
- [ ] Imports organizados (sem imports não utilizados)

## 20.2 Checklist Banco de Dados

- [ ] DDL de todas as tabelas criado
- [ ] Índices nos campos de busca frequente
- [ ] UNIQUE constraints em CPF, CRM, CNPJ, username
- [ ] Campos de auditoria (created_at, updated_at)
- [ ] Dados iniciais (seeds) criados
- [ ] Tipos de dados corretos (VARCHAR tamanhos adequados)
- [ ] ENUM para campos com valores fixos
- [ ] FK constraints onde aplicável (mesmo banco)
- [ ] Nenhuma FK entre bancos de serviços diferentes

## 20.3 Checklist Endpoints

- [ ] Todos seguem `/api/v1/<recurso-plural>`
- [ ] Métodos HTTP corretos (GET, POST, PUT, DELETE)
- [ ] Status codes corretos (200, 201, 204, 400, 404, 409, 422)
- [ ] Respostas de erro padronizadas (ErrorResponse)
- [ ] Paginação com parâmetros page e size
- [ ] Filtros via query parameters
- [ ] Content-Type: application/json
- [ ] Testados no Postman (positivo + negativo)

## 20.4 Checklist Segurança

- [ ] JWT implementado e funcionando
- [ ] Senhas criptografadas com BCrypt
- [ ] Endpoints públicos definidos (login, register, swagger)
- [ ] Endpoints protegidos exigem Bearer token
- [ ] Roles configuradas (ADMIN, DOCTOR, RECEPTIONIST)
- [ ] Bloqueio por tentativas de login
- [ ] Token com expiração configurada
- [ ] Dados sensíveis não expostos nos logs
- [ ] SQL Injection prevenido (JPA parametrizado)

## 20.5 Checklist Docker

- [ ] Dockerfile funcional para cada serviço
- [ ] Multi-stage build (build + runtime)
- [ ] Docker Compose com todos os serviços e bancos
- [ ] Volumes para persistência de dados MySQL
- [ ] Rede bridge para comunicação entre containers
- [ ] Healthcheck nos bancos de dados
- [ ] Variáveis de ambiente configuradas
- [ ] `depends_on` com conditions corretas
- [ ] `docker-compose up -d --build` funciona sem erros

## 20.6 Checklist Testes

- [ ] Testes unitários dos Services (Mockito)
- [ ] Testes de integração dos Controllers (MockMvc)
- [ ] Cenários positivos testados
- [ ] Cenários negativos testados (exceções)
- [ ] Perfil de teste (application-test.yml) com H2
- [ ] Mocks de Feign Clients nos testes de integração
- [ ] Cobertura mínima 70%
- [ ] `mvn test` passa sem erros

## 20.7 Checklist Documentação

- [ ] README.md completo
- [ ] Diagrama de arquitetura
- [ ] Swagger configurado em todos os serviços
- [ ] Endpoints documentados com @Operation
- [ ] Exemplos de request/response no Swagger
- [ ] Postman collections exportadas
- [ ] Instruções de setup no README
- [ ] Variáveis de ambiente documentadas

---

# 21. RISCOS TÉCNICOS

## 21.1 Mapa de Riscos

| # | Risco | Probabilidade | Impacto | Mitigação |
|---|---|---|---|---|
| 1 | **Serviço indisponível** — Feign falha ao chamar outro serviço | Alta | Alto | Implementar fallback, retry, timeout; testar com serviço down |
| 2 | **Inconsistência de dados** — Paciente deletado mas agendamento existe | Média | Alto | Soft delete (nunca deletar fisicamente); validar status via Feign |
| 3 | **Conflito de horário** — Duas requisições simultâneas agendam o mesmo horário | Média | Alto | Índice UNIQUE no banco (doctor_id + date_time); tratamento de constraint violation |
| 4 | **Deadlock no banco** — Transações concorrentes bloqueiam uma à outra | Baixa | Alto | Transações curtas; order de locks consistente; retry on deadlock |
| 5 | **Memory leak** — Conexões não fechadas com o banco | Baixa | Alto | Usar HikariCP (padrão Spring Boot); configurar pool limits |
| 6 | **Acoplamento via Feign** — Mudança na API de um serviço quebra outro | Alta | Médio | Versionamento de API (/v1/); contratos claros; testes de contrato |
| 7 | **Docker: portas em conflito** — Outro processo usando a porta | Média | Baixo | Documentar portas; usar variáveis de ambiente; `docker-compose ps` |
| 8 | **Dados de teste em produção** — Seeds rodam em produção | Baixa | Alto | Profiles Spring (dev/prod); seeds apenas em profile dev |
| 9 | **JWT secret fraco** — Token pode ser forjado | Média | Crítico | Usar secret longo (256 bits); armazenar em variável de ambiente |
| 10 | **N+1 queries** — JPA carrega relações em loop | Alta | Médio | Usar `@EntityGraph` ou `JOIN FETCH`; monitorar SQL gerado |

## 21.2 Estratégias de Mitigação Detalhadas

### Conflito de Horário (Race Condition)
```java
// No Service, tratar a exceção de constraint violation
@Transactional
public AppointmentResponse createAppointment(AppointmentRequest request) {
    try {
        // ... validações ...
        return appointmentRepository.save(appointment);
    } catch (DataIntegrityViolationException e) {
        throw new BusinessException("Horário já ocupado para este médico");
    }
}
```

### Inconsistência entre Serviços
```
Cenário: Admin desativa médico → mas scheduling ainda tem consultas futuras

Solução: 
1. scheduling-service SEMPRE valida médico ativo antes de confirmar consulta
2. Job periódico que cancela consultas de médicos inativos (melhoria futura)
3. Eventos assíncronos via mensageria (melhoria futura com RabbitMQ)
```

---

# 22. ESCALABILIDADE E MELHORIAS FUTURAS

## 22.1 Roadmap de Evolução

```
v1.0 (Atual)              v2.0                    v3.0
─────────────────  →  ─────────────────  →  ─────────────────
✅ REST Síncrono       📬 Mensageria          ☸️ Kubernetes
✅ Feign Client        📬 RabbitMQ/Kafka       🔍 Service Discovery
✅ Docker Compose      🗄️ Cache (Redis)        🚪 API Gateway
✅ JWT Manual          📊 Monitoring           📊 ELK Stack
✅ MySQL               🔔 Notificações         🔄 CI/CD Pipeline
                       📧 Email Service        📱 App Mobile
```

## 22.2 Melhorias Detalhadas

### Cache com Redis
```
Cenário: Listagem de especialidades é consultada em toda agendamento.
Problema: Query no banco a cada requisição.
Solução: Cache Redis com TTL de 1 hora.

@Cacheable(value = "specialties", key = "#id")
public SpecialtyResponse findById(Long id) { ... }
```

### Mensageria com RabbitMQ
```
Cenário: Ao cancelar consulta, notificar paciente por email.
Problema: Enviar email síncrono trava a requisição.
Solução: scheduling-service publica evento → notification-service consome e envia email.

scheduling-service → [RabbitMQ] → notification-service → Email
```

### API Gateway (Spring Cloud Gateway)
```
Cenário: Cliente precisa saber URL de cada serviço.
Problema: Múltiplos endpoints para gerenciar.
Solução: API Gateway centraliza tudo em uma única URL.

Cliente → API Gateway (:8080) → auth-service
                               → admin-service
                               → patient-service
                               → scheduling-service
```

### Service Discovery (Eureka)
```
Cenário: Serviços precisam saber endereço uns dos outros.
Problema: URLs hardcodadas no application.yml.
Solução: Eureka Server para registro e descoberta automática.
```

### CI/CD com GitHub Actions
```yaml
# .github/workflows/ci.yml
name: CI Pipeline
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: cd commons && mvn clean install -DskipTests
      - run: cd admin-service && mvn clean verify
      - run: cd patient-service && mvn clean verify
```

### Monitoramento (Prometheus + Grafana)
```
Spring Boot Actuator → Prometheus (coleta métricas) → Grafana (dashboards)

Métricas: CPU, memória, requests/s, latência, erros, conexões DB
```

---

# 23. BOAS PRÁTICAS OBRIGATÓRIAS — RESUMO

## 23.1 Código

| Regra | Por Quê |
|---|---|
| **NUNCA** expor Entity na API | Entity tem relações JPA que causam loop infinito e exposição de dados |
| **SEMPRE** usar DTOs | Request e Response separados dão flexibilidade e segurança |
| **NUNCA** colocar lógica no Controller | Controller é porta de entrada — delega para Service |
| **SEMPRE** tratar exceções | Erros sem tratamento retornam stack trace em produção |
| **NUNCA** usar `System.out.println` | Use `@Slf4j` do Lombok para logs profissionais |
| **SEMPRE** validar input | `@Valid` + Bean Validation previnem dados corrompidos |
| **NUNCA** hardcodar configurações | Use `application.yml` + variáveis de ambiente |
| **SEMPRE** usar soft delete | DELETE físico perde dados e quebra integridade |
| **NUNCA** retornar senha em responses | Mesmo criptografada, é um risco de segurança |
| **SEMPRE** paginar listagens | Listar 100 mil pacientes sem paginação trava a API |

## 23.2 Arquitetura

| Regra | Por Quê |
|---|---|
| **NUNCA** compartilhar banco entre serviços | Acoplamento direto viola independência dos microsserviços |
| **SEMPRE** versionar APIs (`/api/v1/`) | Permite evolução sem quebrar consumidores |
| **NUNCA** fazer chamadas Feign em loop | N chamadas HTTP = N * latência — use batch endpoints |
| **SEMPRE** ter fallback para Feign | Serviço indisponível não deve derrubar quem consome |
| **SEMPRE** usar correlation-id | Sem ele, debugar fluxo entre 5 serviços é impossível |

## 23.3 Ambientes

| Ambiente | Banco | Logs | Swagger | Debug |
|---|---|---|---|---|
| **Desenvolvimento** | H2 ou MySQL local | DEBUG | Habilitado | Habilitado |
| **Homologação** | MySQL dedicado | INFO | Habilitado | Desabilitado |
| **Produção** | MySQL em nuvem | WARN/ERROR | Desabilitado | Desabilitado |

Configurar via Spring Profiles:
```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
logging:
  level:
    br.com.clinica: DEBUG

# application-prod.yml
spring:
  jpa:
    show-sql: false
logging:
  level:
    br.com.clinica: WARN
springdoc:
  api-docs:
    enabled: false
```

---

# REFERÊNCIA RÁPIDA DE COMANDOS

```bash
# Git
git checkout -b feature/nome-da-feature       # Criar branch
git add . && git commit -m "feat: mensagem"    # Commitar
git push origin feature/nome-da-feature        # Enviar

# Maven
mvn clean install                              # Build completo
mvn spring-boot:run                            # Rodar localmente
mvn test                                       # Rodar testes

# Docker
docker-compose up -d                           # Subir containers
docker-compose down                            # Parar containers
docker-compose logs -f <service>               # Ver logs

# MySQL
docker exec -it admin-db mysql -u root -p      # Acessar MySQL do container
```

---

> **🎓 NOTA FINAL:** Este guia foi projetado para simular um ambiente profissional real de engenharia de software. Ao seguir todas as seções, os alunos terão experiência prática com as mesmas ferramentas, padrões e processos utilizados em empresas de tecnologia. O diferencial não está apenas em "fazer funcionar", mas em fazer **como profissionais fazem** — com organização, padronização, testes, documentação e rastreabilidade.

---

*Fim do Guia — 6 Partes completas*

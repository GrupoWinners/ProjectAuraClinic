# 🏥 PARTE 3 — PADRONIZAÇÃO, CRONOGRAMA, EQUIPE E TAREFAS

---

# 5. PADRONIZAÇÃO CORPORATIVA

## 5.1 Convenção de Nomes — Classes Java

| Tipo | Convenção | Exemplo |
|---|---|---|
| Entity | `NomeSingularPT` | `Medico`, `Paciente`, `Consulta` |
| Repository | `NomeRepository` | `MedicoRepository` |
| Service | `NomeService` | `MedicoService` |
| Controller | `NomeController` | `MedicoController` |
| DTO Request | `NomeRequisicao` | `MedicoRequisicao` |
| DTO Response | `NomeResposta` | `MedicoResposta` |
| Mapper | `NomeMapper` | `MedicoMapper` |
| Feign Client | `NomeServiceClient` | `AdminServiceClient` |
| Enum | `NomeMaiusculo` | `StatusConsulta`, `NivelUrgencia` |

## 5.2 Pacotes Java

```
br.com.clinica.<servico>.<camada>
```
Exemplos: `br.com.clinica.admin.controller`, `br.com.clinica.agendamento.service`

## 5.3 Variáveis e Métodos

| Tipo | Convenção | Exemplo |
|---|---|---|
| Variável local | camelCase | `nomePaciente`, `dataConsulta` |
| Constante | UPPER_SNAKE_CASE | `MAXIMO_TENTATIVAS_LOGIN` |
| Método | camelCase, verbo | `buscarPorId()`, `criarMedico()` |
| Boolean | prefixo is/possui | `isAtivo`, `possuiConvenio` |

## 5.4 Endpoints REST

Padrão: `/api/v1/<recurso-no-plural-pt>`

**Regras:**
- Sempre plural: `/pacientes`, não `/paciente`
- Sempre lowercase com hífens: `/relatorios-clinicos`
- Versão na URL: `/api/v1/`
- Nunca verbos: ~~`/api/v1/buscarPacientes`~~

## 5.5 Padrão de Commits (Conventional Commits)

```
<tipo>(<escopo>): <mensagem curta>
```

| Tipo | Uso | Exemplo |
|---|---|---|
| `feat` | Nova funcionalidade | `feat(admin): criar CRUD de médicos` |
| `fix` | Correção de bug | `fix(agendamento): corrigir validação de conflito` |
| `docs` | Documentação | `docs(admin): adicionar anotações Swagger` |
| `test` | Testes | `test(admin): adicionar testes unitários MedicoService` |
| `chore` | Build, configs | `chore(docker): adicionar docker-compose` |
| `refactor` | Refatoração | `refactor(commons): extrair validador de CPF` |

## 5.6 Branches (GitFlow)

```
main              ← Produção (tags de release)
  └── develop     ← Integração
       ├── feature/admin-crud-medicos
       ├── feature/agendamento-consultas
       ├── feature/atendimento-prontuario
       ├── bugfix/corrigir-validacao-cpf
       └── hotfix/corrigir-falha-login
```

**Regras:** Nunca commitar direto em `main` ou `develop`. PR obrigatório. Mínimo 1 revisão.

## 5.7 Versionamento Semântico

`MAJOR.MINOR.PATCH` → Versão inicial do projeto: **1.0.0**

---

# 6. ORDEM IDEAL DE DESENVOLVIMENTO

```
FASE 1: Fundação
   └── commons → base compartilhada
   └── Docker Compose → bancos MySQL

FASE 2: Domínio Base (independente)
   └── admin-service → não depende de ninguém

FASE 3: Domínio Dependente
   └── agendamento-service → depende de admin-service

FASE 4: Domínio Complexo
   └── atendimento-service → depende de admin + agendamento
```

| Fase | Serviço | Justificativa |
|---|---|---|
| 1 | `commons` | Exceptions e DTOs compartilhados — precisa existir primeiro |
| 1 | Docker Compose | Bancos precisam estar rodando |
| 2 | `admin-service` | Não depende de ninguém. Fornece médicos, pacientes, convênios |
| 3 | `agendamento-service` | Depende de admin para validar paciente/médico/convênio |
| 4 | `atendimento-service` | Depende de admin e agendamento — é o mais complexo |

---

# 7. CRONOGRAMA (8 Semanas)

| Semana | Sprint | Entregas |
|---|---|---|
| 1 | Sprint 0 — Setup | Infra, Docker, commons, estrutura dos projetos |
| 2 | Sprint 1 — Admin Base | CRUD médicos, pacientes, especialidades, convênios |
| 3 | Sprint 2 — Admin Perfis | Usuários, perfis, JWT, segurança, relatórios |
| 4 | Sprint 3 — Agendamento Base | Agendar, remarcar, cancelar consulta |
| 5 | Sprint 4 — Agendamento Avançado | Disponibilidade, bloqueio, consultar agenda |
| 6 | Sprint 5 — Atendimento | Prontuário, relatório clínico, encaminhamento, urgência |
| 7 | Sprint 6 — Qualidade | Testes, Swagger, Postman, bug fixes |
| 8 | Sprint 7 — Entrega | Documentação, apresentação, release v1.0.0 |

### Sprint 0 — Setup (Semana 1)
- [ ] Criar repositório GitHub
- [ ] Configurar `.gitignore` e branches (main, develop)
- [ ] Criar `docker-compose.yml` com 3 MySQL + rede
- [ ] Criar módulo `commons` (exceptions, DTOs, configs)
- [ ] Criar estrutura base dos 3 microsserviços (Spring Initializr)
- [ ] Configurar `pom.xml` e `application.yml` de cada serviço
- [ ] Testar conexão de cada serviço com seu banco
- [ ] Criar `build-and-run.sh`
- [ ] Escrever `README.md` inicial

### Sprint 1 — Admin Base (Semana 2)
- [ ] Criar entities: Medico, Paciente, Especialidade, Convenio
- [ ] Criar repositories, DTOs, mappers
- [ ] Criar services com regras de negócio
- [ ] Criar controllers REST
- [ ] Validações (CPF, CNPJ, Bean Validation)
- [ ] Soft delete, paginação
- [ ] Swagger annotations
- [ ] Testes unitários

### Sprint 2 — Admin Perfis + Segurança (Semana 3)
- [ ] Criar entities: Usuario, Perfil
- [ ] Implementar JWT (geração, validação)
- [ ] Spring Security + RBAC (ADM, MEDICO, SECRETARIA)
- [ ] Endpoints de login/registro
- [ ] BCrypt para senhas
- [ ] Relatórios gerenciais
- [ ] Testes

### Sprint 3 — Agendamento Base (Semana 4)
- [ ] Criar entities: Consulta, Cancelamento
- [ ] Criar Feign Client para admin-service
- [ ] Agendar consulta (com validação de convênio via Feign)
- [ ] Remarcar consulta
- [ ] Cancelar consulta com motivo obrigatório
- [ ] Validação de conflito de horário
- [ ] Máquina de estados
- [ ] Testes

### Sprint 4 — Agendamento Avançado (Semana 5)
- [ ] Criar entities: DisponibilidadeMedico, BloqueioAgenda
- [ ] Definir Disponibilidade (Médico)
- [ ] Bloquear Agenda (Secretária e Médico)
- [ ] Consultar Agenda com filtros
- [ ] Buscar horários disponíveis
- [ ] Validar disponibilidade e bloqueios ao agendar
- [ ] Testes

### Sprint 5 — Atendimento (Semana 6)
- [ ] Criar entities: Prontuario, Atendimento, RelatorioClinico, Receita, SolicitacaoExame, Encaminhamento
- [ ] Feign Clients (admin + agendamento)
- [ ] Criar prontuário automático
- [ ] Registrar atendimento com escopo médico
- [ ] Criar/editar relatório clínico
- [ ] Definir urgência
- [ ] Atribuir a outro médico (encaminhamento)
- [ ] Agendar reconsulta via Feign
- [ ] Testes

### Sprint 6 — Qualidade (Semana 7)
- [ ] Completar cobertura de testes (mínimo 70%)
- [ ] Swagger completo em todos os endpoints
- [ ] Postman collections completas
- [ ] Bug fixes, refatoração
- [ ] Testes ponta a ponta
- [ ] Revisão de logs e exceções

### Sprint 7 — Entrega (Semana 8)
- [ ] README.md final, docs de arquitetura
- [ ] Apresentação
- [ ] Code review final
- [ ] Tag release v1.0.0
- [ ] Merge em main

---

# 8. DIVISÃO DE EQUIPE (5 pessoas)

| Papel | Responsabilidades | Quem |
|---|---|---|
| **Tech Lead** | Arquitetura, code review, merge PRs, Docker | Membro 1 |
| **Backend Dev 1** | `admin-service` + `commons` | Membro 2 |
| **Backend Dev 2** | `admin-service` (perfis, JWT, relatórios) | Membro 3 |
| **Backend Dev 3** | `agendamento-service` completo | Membro 4 |
| **Backend Dev 4 / QA** | `atendimento-service` + testes + Postman | Membro 5 |

---

# 9. LISTA COMPLETA DE TAREFAS TÉCNICAS

### commons (12 tarefas)
1. Criar módulo Maven
2. `RespostaErro.java`
3. `RespostaPaginada.java`
4. `ErroValidacao.java`
5. `RegraDeNegocioException.java`
6. `RecursoNaoEncontradoException.java`
7. `RecursoDuplicadoException.java`
8. `IntegracaoException.java`
9. `TratadorGlobalExcecoes.java`
10. `ValidadorCpf.java`
11. `ValidadorCnpj.java`
12. `ConstantesApi.java`

### admin-service (32 tarefas)
1–4. Entities: Medico, Paciente, Especialidade, Convenio
5–8. Repositories
9–12. DTOs Requisicao
13–16. DTOs Resposta
17–20. Mappers
21–24. Services com regras
25–28. Controllers REST
29. Entity Usuario + Perfil
30. JwtService + SecurityConfig
31. RelatorioService + RelatorioController
32. Swagger annotations

### agendamento-service (20 tarefas)
1–2. Entities: Consulta, Cancelamento
3–4. Entities: DisponibilidadeMedico, BloqueioAgenda
5–6. Enums: StatusConsulta, TipoConsulta
7–8. Repositories
9–12. DTOs
13–14. Mappers
15. Feign Client AdminServiceClient
16. ConsultaService (agendar, remarcar, cancelar)
17. DisponibilidadeService
18. BloqueioAgendaService
19. Controllers
20. Swagger annotations

### atendimento-service (24 tarefas)
1–6. Entities: Prontuario, Atendimento, RelatorioClinico, Receita, SolicitacaoExame, Encaminhamento
7–8. Enums: NivelUrgencia, StatusEncaminhamento
9–10. Repositories
11–14. DTOs
15–16. Mappers
17–18. Feign Clients (Admin + Agendamento)
19. ProntuarioService
20. AtendimentoService
21. RelatorioClinicoService
22. EncaminhamentoService
23. Controllers
24. Swagger annotations

### Infraestrutura (10 tarefas)
1. docker-compose.yml com 3 MySQL
2. Dockerfile de cada serviço
3. build-and-run.sh
4. .gitignore
5. README.md
6. docs/arquitetura.md
7–9. Postman collections (1 por serviço)
10. Tag release v1.0.0

**Total: ~98 tarefas**

---

*Continua na Parte 4 → Comunicação Feign, Segurança JWT, Exceções e Logs*

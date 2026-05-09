# 🏥 PARTE 1 — VISÃO GERAL DA ARQUITETURA E DEFINIÇÃO DOS MICROSSERVIÇOS

> **Nível:** Intermediário | **Stack:** Java 17 + Spring Boot 3.2 | **Arquitetura:** Microsserviços

---

# 1. VISÃO GERAL DA ARQUITETURA

## 1.1 Arquitetura Escolhida

O sistema adota **Arquitetura de Microsserviços** com comunicação **síncrona via REST/HTTP** usando **OpenFeign**. Cada microsserviço é um projeto Spring Boot independente, com banco MySQL isolado (**Database per Service Pattern**).

Os 3 módulos UML mapeiam diretamente para 3 microsserviços:

| Módulo UML | Microsserviço | Porta | Banco | Ator(es) |
|---|---|---|---|---|
| Administrativo | `admin-service` | 8081 | `admin_db` | Administrador (ADM) |
| Agendamento | `agendamento-service` | 8082 | `agendamento_db` | Secretária, Médico |
| Atendimento | `atendimento-service` | 8083 | `atendimento_db` | Médico |
| *(suporte)* | `commons` | — | — | — |

## 1.2 Por Que Microsserviços?

| Aspecto | Monolito | Microsserviços (Escolhido) |
|---|---|---|
| Deploy | Tudo junto — um bug trava tudo | Independente por serviço |
| Escalabilidade | Escala tudo ou nada | Escala apenas o sobrecarregado |
| Equipe | Todos no mesmo código | Cada time cuida de um serviço |
| Resiliência | Falha afeta tudo | Falha isolada por serviço |
| Complexidade | Simples no início | Maior complexidade infra |

**Motivação:** Empresas como Nubank, iFood e Mercado Livre usam microsserviços. Ao adotar esta arquitetura, os alunos aprendem padrões corporativos reais.

## 1.3 Diagrama Geral

```
┌─────────────────────────────────────────────────────┐
│              CLIENTE (Browser / Postman)             │
└─────────┬──────────────┬──────────────┬─────────────┘
          │              │              │
          ▼              ▼              ▼
   ┌────────────┐ ┌────────────────┐ ┌────────────────┐
   │   admin    │ │  agendamento   │ │  atendimento   │
   │  service   │ │    service     │ │    service     │
   │   :8081    │ │     :8082      │ │     :8083      │
   └─────┬──────┘ └──────┬─────────┘ └──────┬─────────┘
         │     Feign      │      Feign       │
         │◄───────────────┤◄─────────────────┤
         ▼               ▼                  ▼
   ┌──────────┐  ┌──────────────┐   ┌──────────────┐
   │ admin_db │  │agendamento_db│   │atendimento_db│
   │  MySQL   │  │    MySQL     │   │    MySQL     │
   └──────────┘  └──────────────┘   └──────────────┘
```

## 1.4 Fluxo Geral do Sistema

```
1. ADM cadastra médicos, pacientes, especialidades, convênios → admin-service
2. ADM gerencia perfis de acesso (usuários e permissões) → admin-service
3. Secretária agenda consulta (valida convênio via Feign) → agendamento-service
4. Médico define disponibilidade e consulta agenda → agendamento-service
5. Médico atende paciente, registra relatório clínico → atendimento-service
6. Médico encaminha paciente a outro médico → atendimento-service
```

## 1.5 Princípios Arquiteturais

| Princípio | Aplicação |
|---|---|
| **Single Responsibility** | Cada serviço = um domínio UML |
| **Baixo Acoplamento** | Comunicação via HTTP, sem banco compartilhado |
| **Alta Coesão** | Toda lógica de agendamento fica em agendamento-service |
| **Database per Service** | Cada serviço tem MySQL isolado |
| **API First** | Contratos definidos antes da implementação |

---

# 2. DEFINIÇÃO DOS MICROSSERVIÇOS

## 2.1 admin-service (Porta 8081) — Módulo Administrativo

### Responsabilidades
- CRUD de Médicos
- CRUD de Pacientes
- CRUD de Especialidades
- CRUD de Convênios
- Gerenciamento de Perfis (usuários + permissões)
- Geração de Relatórios Gerenciais

### Entidades Principais
- `Medico` (id, nomeCompleto, crm, especialidadeId, ativo, criadoEm, atualizadoEm)
- `Paciente` (id, nomeCompleto, rg, cpf, endereco, cidade, estado, cep, telefone, celular, dataNascimento, genero, possuiConvenio, convenioId, ativo, criadoEm, atualizadoEm)
- `Especialidade` (id, descricao, ativo, criadoEm)
- `Convenio` (id, nomeEmpresa, cnpj, telefone, ativo, criadoEm, atualizadoEm)
- `Usuario` (id, nomeUsuario, senha, perfil, ativo, criadoEm, atualizadoEm)
- `Perfil` (id, nome, descricao) → valores: ADM, MEDICO, SECRETARIA

### Endpoints — Médicos

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/medicos` | Cadastrar médico |
| `GET` | `/api/v1/medicos` | Listar médicos (paginado) |
| `GET` | `/api/v1/medicos/{id}` | Buscar por ID |
| `GET` | `/api/v1/medicos/crm/{crm}` | Buscar por CRM |
| `GET` | `/api/v1/medicos/especialidade/{especialidadeId}` | Listar por especialidade |
| `PUT` | `/api/v1/medicos/{id}` | Atualizar médico |
| `DELETE` | `/api/v1/medicos/{id}` | Desativar (soft delete) |
| `GET` | `/api/v1/medicos/{id}/validar-ativo` | Validar se está ativo (integração) |

### Endpoints — Pacientes

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/pacientes` | Cadastrar paciente |
| `GET` | `/api/v1/pacientes` | Listar pacientes (paginado) |
| `GET` | `/api/v1/pacientes/{id}` | Buscar por ID |
| `GET` | `/api/v1/pacientes/cpf/{cpf}` | Buscar por CPF |
| `GET` | `/api/v1/pacientes/buscar?nome={nome}` | Buscar por nome |
| `PUT` | `/api/v1/pacientes/{id}` | Atualizar |
| `DELETE` | `/api/v1/pacientes/{id}` | Soft delete |
| `GET` | `/api/v1/pacientes/{id}/validar-ativo` | Validar se ativo (integração) |

### Endpoints — Especialidades

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/especialidades` | Criar |
| `GET` | `/api/v1/especialidades` | Listar todas |
| `GET` | `/api/v1/especialidades/{id}` | Buscar por ID |
| `PUT` | `/api/v1/especialidades/{id}` | Atualizar |
| `DELETE` | `/api/v1/especialidades/{id}` | Soft delete |

### Endpoints — Convênios

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/convenios` | Criar |
| `GET` | `/api/v1/convenios` | Listar |
| `GET` | `/api/v1/convenios/{id}` | Buscar por ID |
| `PUT` | `/api/v1/convenios/{id}` | Atualizar |
| `DELETE` | `/api/v1/convenios/{id}` | Soft delete |
| `GET` | `/api/v1/convenios/{id}/validar-ativo` | Validar (integração) |

### Endpoints — Perfis e Usuários

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autenticar usuário |
| `POST` | `/api/v1/usuarios` | Criar usuário |
| `GET` | `/api/v1/usuarios` | Listar usuários |
| `PUT` | `/api/v1/usuarios/{id}` | Atualizar |
| `PUT` | `/api/v1/usuarios/{id}/perfil` | Atualizar permissões |
| `DELETE` | `/api/v1/usuarios/{id}` | Desativar |

### Endpoints — Relatórios Gerenciais

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/relatorios/consultas-por-periodo?inicio={}&fim={}` | Consultas por período |
| `GET` | `/api/v1/relatorios/consultas-por-medico?medicoId={}&inicio={}&fim={}` | Consultas por médico |
| `GET` | `/api/v1/relatorios/consultas-por-especialidade` | Por especialidade |
| `GET` | `/api/v1/relatorios/convenios-utilizados` | Convênios mais utilizados |
| `GET` | `/api/v1/relatorios/pacientes-cadastrados?inicio={}&fim={}` | Pacientes novos |

### Regras de Negócio
1. CPF e RG devem ser únicos para pacientes
2. CRM deve ser único para médicos
3. CNPJ deve ser único para convênios
4. CPF e CNPJ devem ser validados (algoritmo)
5. Exclusões são lógicas (soft delete — campo `ativo = false`)
6. Médico deve ter especialidade válida cadastrada
7. Senha armazenada com BCrypt (strength 12)
8. Relatórios consultam dados via Feign nos demais serviços

### Exemplo de Payload — Cadastrar Médico

**Request:**
```json
{
  "nomeCompleto": "Dra. Maria Souza",
  "crm": "CRM/SP 12345",
  "especialidadeId": 1
}
```

**Response (201):**
```json
{
  "id": 3,
  "nomeCompleto": "Dra. Maria Souza",
  "crm": "CRM/SP 12345",
  "especialidade": "Cardiologia",
  "ativo": true,
  "criadoEm": "2026-05-09T10:30:00"
}
```

### Dependências (Feign)
- `agendamento-service` → para relatórios de consultas por período
- `atendimento-service` → para relatórios clínicos

---

## 2.2 agendamento-service (Porta 8082) — Módulo de Agendamento

### Responsabilidades
- Agendar Consulta (Secretária) com validação de convênio
- Remarcar Consulta (Secretária)
- Cancelar Consulta (Secretária)
- Consultar Agenda (Secretária e Médico)
- Definir Disponibilidade (Médico)
- Bloquear Agenda (Secretária e Médico)

### Entidades
- `Consulta` (id, pacienteId, medicoId, dataHora, status, tipo, consultaOriginalId, criadoEm, atualizadoEm)
- `Cancelamento` (id, consultaId, motivo, canceladoPor, criadoEm)
- `DisponibilidadeMedico` (id, medicoId, diaSemana, horaInicio, horaFim, ativo)
- `BloqueioAgenda` (id, medicoId, dataInicio, dataFim, motivo, bloqueadoPor, criadoEm)

### Status da Consulta
```
AGENDADA → CONFIRMADA → EM_ATENDIMENTO → CONCLUIDA
               ↓
          CANCELADA
               ↓
          REMARCADA
```

### Endpoints — Consultas

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/consultas` | Agendar consulta |
| `GET` | `/api/v1/consultas` | Listar consultas (filtros) |
| `GET` | `/api/v1/consultas/{id}` | Buscar por ID |
| `GET` | `/api/v1/consultas/medico/{medicoId}` | Listar por médico |
| `GET` | `/api/v1/consultas/paciente/{pacienteId}` | Listar por paciente |
| `PUT` | `/api/v1/consultas/{id}/remarcar` | Remarcar consulta |
| `PUT` | `/api/v1/consultas/{id}/cancelar` | Cancelar consulta |
| `PUT` | `/api/v1/consultas/{id}/status` | Atualizar status |
| `GET` | `/api/v1/consultas/horarios-disponiveis` | Horários livres |

### Endpoints — Disponibilidade

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/disponibilidades` | Definir horário de trabalho |
| `GET` | `/api/v1/disponibilidades/medico/{medicoId}` | Consultar disponibilidade |
| `PUT` | `/api/v1/disponibilidades/{id}` | Atualizar |
| `DELETE` | `/api/v1/disponibilidades/{id}` | Remover |

### Endpoints — Bloqueio de Agenda

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/bloqueios` | Bloquear período |
| `GET` | `/api/v1/bloqueios/medico/{medicoId}` | Listar bloqueios |
| `DELETE` | `/api/v1/bloqueios/{id}` | Remover bloqueio |

### Regras de Negócio
1. Paciente deve estar ativo (valida via Feign → admin-service)
2. Médico deve estar ativo (valida via Feign → admin-service)
3. Se paciente possui convênio, **obrigatório validar convênio** via Feign
4. Não pode haver conflito de horário para o mesmo médico
5. Não pode agendar em horário bloqueado
6. Não pode agendar fora da disponibilidade do médico
7. Consultas duram 30 minutos por padrão
8. Cancelamento exige motivo obrigatório
9. Cancelamento libera o horário automaticamente
10. Remarcar = cancelar atual + criar nova consulta vinculada
11. Bloqueio impede novos agendamentos no período

### Exemplo de Payload — Agendar Consulta

**Request:**
```json
{
  "pacienteId": 1,
  "medicoId": 3,
  "dataHora": "2026-06-15T10:00:00",
  "tipo": "PRIMEIRA_CONSULTA"
}
```

**Response (201):**
```json
{
  "id": 42,
  "pacienteId": 1,
  "nomePaciente": "João da Silva",
  "medicoId": 3,
  "nomeMedico": "Dra. Maria Souza",
  "crmMedico": "CRM/SP 12345",
  "especialidade": "Cardiologia",
  "dataHora": "2026-06-15T10:00:00",
  "status": "AGENDADA",
  "tipo": "PRIMEIRA_CONSULTA",
  "criadoEm": "2026-05-09T09:30:00"
}
```

### Dependências (Feign)
- `admin-service` → validar paciente ativo, médico ativo, convênio ativo

---

## 2.3 atendimento-service (Porta 8083) — Módulo de Atendimento

### Responsabilidades
- Agendar Reconsulta (retorno)
- Criar Relatório Clínico (com escopo médico + consultar/editar anteriores)
- Salvar Dados do Paciente (atualizar prontuário)
- Atribuir a Outro Médico (encaminhamento)
- Definir Urgência (classificação de risco)

### Entidades
- `Prontuario` (id, pacienteId, criadoEm)
- `Atendimento` (id, prontuarioId, consultaId, medicoId, dataAtendimento, sintomas, diagnostico, escopoMedico, observacoes, nivelUrgencia, criadoEm, atualizadoEm)
- `RelatorioClinico` (id, atendimentoId, conteudo, criadoEm, atualizadoEm)
- `Encaminhamento` (id, atendimentoId, medicoOrigemId, medicoDestinoId, motivo, especialidadeDestino, prioridade, status, criadoEm)
- `Receita` (id, atendimentoId, medicamento, dosagem, frequencia, duracao, observacoes, criadoEm)
- `SolicitacaoExame` (id, atendimentoId, tipoExame, descricao, urgencia, status, criadoEm)

### Níveis de Urgência (Enum)
```
VERDE (Não urgente) → AMARELO (Pouco urgente) → LARANJA (Urgente) → VERMELHO (Emergência)
```

### Status do Encaminhamento
```
PENDENTE → ACEITO → EM_ATENDIMENTO → CONCLUIDO
```

### Endpoints — Atendimento

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/atendimentos` | Registrar atendimento |
| `GET` | `/api/v1/atendimentos/{id}` | Buscar atendimento |
| `GET` | `/api/v1/atendimentos/paciente/{pacienteId}` | Histórico do paciente |
| `PUT` | `/api/v1/atendimentos/{id}` | Atualizar dados (salvar prontuário) |
| `PUT` | `/api/v1/atendimentos/{id}/urgencia` | Definir urgência |

### Endpoints — Relatório Clínico

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/relatorios-clinicos` | Criar relatório |
| `GET` | `/api/v1/relatorios-clinicos/{id}` | Consultar relatório |
| `GET` | `/api/v1/relatorios-clinicos/paciente/{pacienteId}` | Relatórios do paciente |
| `PUT` | `/api/v1/relatorios-clinicos/{id}` | Editar relatório |

### Endpoints — Encaminhamento

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/encaminhamentos` | Atribuir a outro médico |
| `GET` | `/api/v1/encaminhamentos/{id}` | Buscar encaminhamento |
| `GET` | `/api/v1/encaminhamentos/medico-destino/{medicoId}` | Encaminhamentos recebidos |
| `PUT` | `/api/v1/encaminhamentos/{id}/status` | Atualizar status |

### Endpoints — Reconsulta

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/reconsultas` | Agendar reconsulta/retorno |

### Regras de Negócio
1. Prontuário é criado automaticamente no primeiro atendimento
2. Atendimento é vinculado a uma consulta com status `EM_ATENDIMENTO`
3. Apenas médicos podem registrar atendimentos
4. Relatório clínico inclui definição de escopo médico obrigatória
5. Relatórios anteriores podem ser consultados e editados
6. Encaminhamento exige: médico destino, motivo, especialidade, prioridade
7. Médico destino é validado via Feign → admin-service
8. Reconsulta cria novo agendamento via Feign → agendamento-service com tipo RETORNO
9. Urgência usa escala de cores: VERDE, AMARELO, LARANJA, VERMELHO
10. Salvar dados do paciente atualiza o prontuário base

### Exemplo de Payload — Registrar Atendimento

**Request:**
```json
{
  "consultaId": 42,
  "medicoId": 3,
  "pacienteId": 1,
  "sintomas": "Dor no peito ao esforço, falta de ar",
  "diagnostico": "Angina estável — CID I20.8",
  "escopoMedico": "Avaliação cardiológica completa",
  "observacoes": "Paciente relata início dos sintomas há 2 semanas",
  "nivelUrgencia": "LARANJA",
  "receitas": [
    {
      "medicamento": "Isordil 5mg",
      "dosagem": "1 comprimido",
      "frequencia": "Sublingual em caso de dor",
      "duracao": "Uso contínuo"
    }
  ],
  "solicitacoesExame": [
    {
      "tipoExame": "ECG",
      "descricao": "Eletrocardiograma de repouso",
      "urgencia": "URGENTE"
    }
  ]
}
```

### Exemplo — Atribuir a Outro Médico

**Request:**
```json
{
  "atendimentoId": 10,
  "medicoOrigemId": 3,
  "medicoDestinoId": 7,
  "motivo": "Paciente necessita avaliação neurológica especializada",
  "especialidadeDestino": "Neurologia",
  "prioridade": "ALTA"
}
```

### Dependências (Feign)
- `admin-service` → validar médico destino, buscar dados paciente/médico
- `agendamento-service` → validar consulta, criar reconsulta

---

## 2.4 commons (Módulo Maven Compartilhado)

### Responsabilidades
- DTOs compartilhados (RespostaErro, RespostaPaginada)
- Exception handlers globais
- Utilitários (ValidadorCpf, ValidadorCnpj)
- Configurações comuns (Logbook, Jackson)
- Constantes da API

### Estrutura
```
commons/
├── src/main/java/br/com/clinica/commons/
│   ├── dto/
│   │   ├── RespostaErro.java
│   │   ├── RespostaPaginada.java
│   │   └── ErroValidacao.java
│   ├── exception/
│   │   ├── RegraDeNegocioException.java
│   │   ├── RecursoNaoEncontradoException.java
│   │   ├── RecursoDuplicadoException.java
│   │   ├── IntegracaoException.java
│   │   └── TratadorGlobalExcecoes.java
│   ├── config/
│   │   ├── LogbookConfig.java
│   │   └── JacksonConfig.java
│   ├── util/
│   │   ├── ValidadorCpf.java
│   │   ├── ValidadorCnpj.java
│   │   └── DataUtils.java
│   └── constantes/
│       └── ConstantesApi.java
└── pom.xml
```

---

## 2.5 Matriz de Comunicação entre Serviços

| Origem | Destino | Endpoint Consumido | Motivo |
|---|---|---|---|
| `agendamento-service` | `admin-service` | `GET /api/v1/pacientes/{id}/validar-ativo` | Validar paciente ao agendar |
| `agendamento-service` | `admin-service` | `GET /api/v1/medicos/{id}/validar-ativo` | Validar médico ao agendar |
| `agendamento-service` | `admin-service` | `GET /api/v1/convenios/{id}/validar-ativo` | Validar convênio ao agendar |
| `atendimento-service` | `admin-service` | `GET /api/v1/pacientes/{id}` | Buscar dados do paciente |
| `atendimento-service` | `admin-service` | `GET /api/v1/medicos/{id}` | Validar médico destino |
| `atendimento-service` | `agendamento-service` | `GET /api/v1/consultas/{id}` | Validar consulta |
| `atendimento-service` | `agendamento-service` | `POST /api/v1/consultas` | Criar reconsulta |
| `admin-service` | `agendamento-service` | `GET /api/v1/consultas?periodo=...` | Relatórios gerenciais |

---

*Continua na Parte 2 → Modelagem de Banco de Dados e Estrutura de Pastas*

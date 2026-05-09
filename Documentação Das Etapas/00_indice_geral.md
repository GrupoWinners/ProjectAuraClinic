# 🏥 PROJETO AURA CLINIC — MANUAL DE IMPLEMENTAÇÃO PROFISSIONAL

## Sistema de Gerenciamento de Clínica Médica
> **Stack:** Java 17 + Spring Boot 3.2 + MySQL | **Arquitetura:** Microsserviços | **Nível:** Intermediário

---

## 📚 ÍNDICE GERAL DO MANUAL

O manual está dividido em **7 partes** para facilitar a navegação:

| Parte | Arquivo | Conteúdo | Seções |
|---|---|---|---|
| **1** | `01_arquitetura_microsservicos.md` | Visão geral, definição dos 3 microsserviços, endpoints, payloads | 1–2 |
| **2** | `02_banco_de_dados_estrutura.md` | Modelagem ER, SQL completo, estrutura de pastas | 3–4 |
| **3** | `03_padronizacao_cronograma_tarefas.md` | Convenções, GitFlow, cronograma, equipe, lista de tarefas | 5–9 |
| **4** | `04_comunicacao_seguranca_excecoes.md` | OpenFeign, JWT, RBAC, exceções globais, logs | 10–13 |
| **5** | `05_testes_swagger_docker.md` | Testes unitários/integração, Postman, Swagger, Docker | 14–18 |
| **6** | `06_versionamento_checklists_riscos.md` | GitFlow, PRs, checklists, riscos técnicos | 19–21 |
| **7** | `07_escalabilidade_boas_praticas.md` | Melhorias futuras, requisitos opcionais, boas práticas | 22–23 |

---

## 🎯 ESCOPO DO PROJETO (BASEADO NOS DIAGRAMAS UML)

### Módulos Obrigatórios

| Módulo UML | Microsserviço | Porta | Banco | Ator(es) |
|---|---|---|---|---|
| **Administrativo** | `admin-service` | 8081 | `admin_db` | Administrador (ADM) |
| **Agendamento** | `agendamento-service` | 8082 | `agendamento_db` | Secretária, Médico |
| **Atendimento** | `atendimento-service` | 8083 | `atendimento_db` | Médico |
| *(Suporte)* | `commons` | — | — | — |

### Funcionalidades por Módulo

#### 1. MÓDULO ADMINISTRATIVO (admin-service)
- Gerenciar Médicos (CRUD)
- Gerenciar Pacientes (CRUD)
- Gerenciar Especialidades (CRUD)
- Gerenciar Perfis (Usuários e permissões)
- Gerenciar Convênios (CRUD)
- Gerenciar Relatórios (relatórios gerenciais)

#### 2. MÓDULO DE AGENDAMENTO (agendamento-service)
- Agendar Consulta (Secretária) — inclui Validar Convênio
- Remarcar Consulta (Secretária)
- Cancelar Consulta (Secretária)
- Consultar Agenda (Secretária e Médico)
- Definir Disponibilidade (Médico)
- Bloquear Agenda (Secretária e Médico)

#### 3. MÓDULO DE ATENDIMENTO (atendimento-service)
- Agendar Reconsulta (Médico)
- Criar Relatório Clínico (inclui Definir Escopo Médico + Consultar/Editar Relatórios)
- Salvar Dados do Paciente (atualização de prontuário)
- Atribuir a Outro Médico (encaminhamento)
- Definir Urgência (classificação de risco)

### Stack Tecnológica

| Categoria | Tecnologias |
|---|---|
| **Linguagem** | Java 17, Maven |
| **Framework** | Spring Boot 3.2, Spring MVC, Spring Data JPA, Spring Security |
| **Banco** | MySQL 8.0 (um por microsserviço) |
| **Libs** | Lombok, Bean Validation, Jackson, Logbook |
| **Comunicação** | OpenFeign (HTTP entre microsserviços) |
| **Documentação** | OpenAPI 3.0 / Swagger UI (SpringDoc) |
| **Segurança** | JWT (JSON Web Token) |
| **Containers** | Docker, Docker Compose |
| **Testes** | JUnit 5, Mockito, MockMvc, H2 |
| **Versionamento** | Git, GitHub, GitFlow |
| **Testes Manuais** | Postman |

---

> **📖 Comece pela Parte 1** → `01_arquitetura_microsservicos.md`

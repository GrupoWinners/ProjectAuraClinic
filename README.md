# Aura Clinic

Sistema de gerenciamento para clínicas médicas baseado em arquitetura de microsserviços utilizando Spring Boot, Docker e MySQL.

## Arquitetura do Projeto

O projeto é dividido em múltiplos microsserviços independentes:

- `admin-service`
  - Responsável pelo gerenciamento administrativo.

- `agendamento-service`
  - Responsável pelo gerenciamento de agendamentos.

- `atendimento-service`
  - Responsável pelo gerenciamento de atendimentos.

- `commons`
  - Biblioteca compartilhada contendo DTOs, exceptions, utilitários e configurações comuns.

---

# Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- Spring Cloud OpenFeign
- MySQL 8
- Docker
- Docker Compose
- Lombok
- Swagger/OpenAPI
- Logbook

---

# Estrutura do Projeto

```text
aura-clinic/
│
├── admin-service/
├── agendamento-service/
├── atendimento-service/
├── commons/
│
├── docker-compose.yml
├── build-and-run.sh
├── build-and-run.bat
└── README.md
```

# Portas dos Serviços

|Serviço|Porta|
|---|---|
|admin-service|8081|
|agendamento-service|8082|
|atendimento-service|8083|

---

# Bancos de Dados

|Banco|Porta|
|---|---|
|admin-db|3306|
|agendamento-db|3307|
|atendimento-db|3308|

---

# Como Executar o Projeto

## Pré-requisitos

- Docker Desktop
- Java 17
- Maven 3.9+

---

## Executar automaticamente

### Linux / Git Bash

```bash
./build-and-run.sh
```

### Windows

```bash
build-and-run.bat
```

---

## Executar manualmente

### Instalar módulo commons

```bash
cd commonsmvn clean install
```

### Voltar para raiz

```bash
cd ..
```

### Subir containers

```bash
docker compose up -d --build
```

---

# Swagger

## Admin Service

```bash
http://localhost:8081/swagger-ui.html
```

## Agendamento Service

```bash
http://localhost:8082/swagger-ui.html
```

## Atendimento Service

```bash
http://localhost:8083/swagger-ui.html
```

---

# Logs

Visualizar logs dos microsserviços:

```bash
docker compose logs -f
```

Logs específicos:

```bash
docker compose logs -f admin-service 
docker compose logs -f agendamento-service
docker compose logs -f atendimento-service
```

---

# Time de Desenvolvimento

|Nome|Função|
|---|---|
|João Vitor|Tech Lead + Desenvolvedor|
|Samela|DeveOps + Desenvolvedora|
|Nadiny|QA + Desenvolvedora|

# 🏥 PARTE 2 — MODELAGEM DO BANCO DE DADOS E ESTRUTURA DE PASTAS

---

# 3. MODELAGEM DO BANCO DE DADOS

## 3.1 Estratégia: Database per Service

Cada microsserviço possui banco **isolado**. Nenhum serviço acessa diretamente o banco de outro.

| Serviço | Banco | Porta Host |
|---|---|---|
| `admin-service` | `admin_db` | 3306 |
| `agendamento-service` | `agendamento_db` | 3307 |
| `atendimento-service` | `atendimento_db` | 3308 |

> **Por quê?** Bancos isolados garantem que uma migration mal feita em um serviço não derrube os outros.

## 3.2 Diagrama ER — admin_db

```
┌───────────────────────────┐
│         usuarios          │
├───────────────────────────┤
│ id            BIGINT PK   │    ┌──────────────────────┐
│ nome_usuario  VARCHAR(100)│    │       perfis         │
│ senha         VARCHAR(255)│    ├──────────────────────┤
│ perfil_id     BIGINT FK───┼───►│ id        BIGINT PK  │
│ ativo         BOOLEAN     │    │ nome      VARCHAR(50)│
│ criado_em     TIMESTAMP   │    │ descricao VARCHAR(200│
│ atualizado_em TIMESTAMP   │    │ criado_em TIMESTAMP  │
└───────────────────────────┘    └──────────────────────┘

┌───────────────────────────┐
│       especialidades      │
├───────────────────────────┤
│ id          BIGINT PK     │
│ descricao   VARCHAR(100)  │
│ ativo       BOOLEAN       │
│ criado_em   TIMESTAMP     │
└───────────────────────────┘

┌───────────────────────────┐    ┌──────────────────────┐
│         medicos           │    │   especialidades     │
├───────────────────────────┤    │                      │
│ id             BIGINT PK  │    │                      │
│ nome_completo  VARCHAR(150│    │                      │
│ crm            VARCHAR(20)│    │                      │
│ especialidade_id BIGINT FK┼───►│                      │
│ ativo          BOOLEAN    │    └──────────────────────┘
│ criado_em      TIMESTAMP  │
│ atualizado_em  TIMESTAMP  │
└───────────────────────────┘

┌───────────────────────────┐
│        pacientes          │
├───────────────────────────┤
│ id             BIGINT PK  │
│ nome_completo  VARCHAR(150│
│ rg             VARCHAR(20)│
│ cpf            VARCHAR(14)│
│ endereco       VARCHAR(255│
│ bairro         VARCHAR(100│
│ cidade         VARCHAR(100│
│ estado         VARCHAR(2) │
│ cep            VARCHAR(10)│
│ telefone       VARCHAR(20)│
│ celular        VARCHAR(20)│
│ data_nascimento DATE      │
│ genero         ENUM       │
│ possui_convenio BOOLEAN   │
│ convenio_id    BIGINT     │
│ ativo          BOOLEAN    │
│ criado_em      TIMESTAMP  │
│ atualizado_em  TIMESTAMP  │
└───────────────────────────┘

┌───────────────────────────┐
│        convenios          │
├───────────────────────────┤
│ id            BIGINT PK   │
│ nome_empresa  VARCHAR(150)│
│ cnpj          VARCHAR(18) │
│ telefone      VARCHAR(20) │
│ ativo         BOOLEAN     │
│ criado_em     TIMESTAMP   │
│ atualizado_em TIMESTAMP   │
└───────────────────────────┘
```

### SQL — admin_db

```sql
CREATE DATABASE IF NOT EXISTS admin_db;
USE admin_db;

CREATE TABLE perfis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(200),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_usuario VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil_id BIGINT NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    tentativas_login INT DEFAULT 0,
    bloqueado_ate TIMESTAMP NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (perfil_id) REFERENCES perfis(id),
    INDEX idx_nome_usuario (nome_usuario)
);

CREATE TABLE especialidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL UNIQUE,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    crm VARCHAR(20) NOT NULL UNIQUE,
    especialidade_id BIGINT NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_medico_especialidade FOREIGN KEY (especialidade_id) REFERENCES especialidades(id),
    INDEX idx_crm (crm),
    INDEX idx_especialidade (especialidade_id)
);

CREATE TABLE convenios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_empresa VARCHAR(150) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    telefone VARCHAR(20),
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cnpj (cnpj)
);

CREATE TABLE pacientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    rg VARCHAR(20) NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    endereco VARCHAR(255) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    cep VARCHAR(10) NOT NULL,
    telefone VARCHAR(20),
    celular VARCHAR(20) NOT NULL,
    data_nascimento DATE NOT NULL,
    genero ENUM('MASCULINO', 'FEMININO', 'OUTRO') NOT NULL,
    possui_convenio BOOLEAN DEFAULT FALSE,
    convenio_id BIGINT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_paciente_convenio FOREIGN KEY (convenio_id) REFERENCES convenios(id),
    INDEX idx_cpf (cpf),
    INDEX idx_rg (rg),
    INDEX idx_nome (nome_completo)
);

-- Dados iniciais
INSERT INTO perfis (nome, descricao) VALUES
('ADM', 'Administrador do sistema'),
('MEDICO', 'Médico'),
('SECRETARIA', 'Secretária/Recepcionista');

INSERT INTO especialidades (descricao) VALUES
('Cardiologia'), ('Dermatologia'), ('Ortopedia'),
('Pediatria'), ('Ginecologia'), ('Neurologia'),
('Oftalmologia'), ('Urologia'), ('Psiquiatria'),
('Clínico Geral');
```

## 3.3 Diagrama ER — agendamento_db

```
┌───────────────────────────┐     ┌──────────────────────────┐
│         consultas         │     │     cancelamentos        │
├───────────────────────────┤     ├──────────────────────────┤
│ id             BIGINT PK  │◄────│ consulta_id  BIGINT FK   │
│ paciente_id    BIGINT     │     │ id           BIGINT PK   │
│ medico_id      BIGINT     │     │ motivo       TEXT         │
│ data_hora      DATETIME   │     │ cancelado_por VARCHAR(100│
│ status         ENUM       │     │ criado_em    TIMESTAMP   │
│ tipo           ENUM       │     └──────────────────────────┘
│ consulta_original_id BIGINT FK(self)│
│ criado_em      TIMESTAMP  │
│ atualizado_em  TIMESTAMP  │
└───────────────────────────┘

┌───────────────────────────┐     ┌──────────────────────────┐
│ disponibilidades_medico   │     │   bloqueios_agenda       │
├───────────────────────────┤     ├──────────────────────────┤
│ id          BIGINT PK     │     │ id           BIGINT PK   │
│ medico_id   BIGINT        │     │ medico_id    BIGINT      │
│ dia_semana  ENUM          │     │ data_inicio  DATETIME    │
│ hora_inicio TIME          │     │ data_fim     DATETIME    │
│ hora_fim    TIME          │     │ motivo       VARCHAR(255)│
│ ativo       BOOLEAN       │     │ bloqueado_por VARCHAR(100│
│ criado_em   TIMESTAMP     │     │ criado_em    TIMESTAMP   │
└───────────────────────────┘     └──────────────────────────┘
```

### SQL — agendamento_db

```sql
CREATE DATABASE IF NOT EXISTS agendamento_db;
USE agendamento_db;

CREATE TABLE consultas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    data_hora DATETIME NOT NULL,
    status ENUM('AGENDADA','CONFIRMADA','EM_ATENDIMENTO','CONCLUIDA','CANCELADA','REMARCADA') DEFAULT 'AGENDADA',
    tipo ENUM('PRIMEIRA_CONSULTA','ACOMPANHAMENTO','RETORNO') DEFAULT 'PRIMEIRA_CONSULTA',
    consulta_original_id BIGINT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_consulta_original FOREIGN KEY (consulta_original_id) REFERENCES consultas(id),
    INDEX idx_paciente (paciente_id),
    INDEX idx_medico (medico_id),
    INDEX idx_data_hora (data_hora),
    INDEX idx_status (status),
    UNIQUE INDEX idx_medico_horario (medico_id, data_hora)
);

CREATE TABLE cancelamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consulta_id BIGINT NOT NULL UNIQUE,
    motivo TEXT NOT NULL,
    cancelado_por VARCHAR(100) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cancelamento_consulta FOREIGN KEY (consulta_id) REFERENCES consultas(id)
);

CREATE TABLE disponibilidades_medico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id BIGINT NOT NULL,
    dia_semana ENUM('SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO') NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_medico_dia (medico_id, dia_semana)
);

CREATE TABLE bloqueios_agenda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id BIGINT NOT NULL,
    data_inicio DATETIME NOT NULL,
    data_fim DATETIME NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    bloqueado_por VARCHAR(100) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_medico_periodo (medico_id, data_inicio, data_fim)
);
```

## 3.4 Diagrama ER — atendimento_db

```
┌─────────────────────────┐
│      prontuarios        │
├─────────────────────────┤     ┌──────────────────────────┐
│ id         BIGINT PK    │◄────│      atendimentos        │
│ paciente_id BIGINT UQ   │     ├──────────────────────────┤
│ criado_em  TIMESTAMP    │     │ id           BIGINT PK   │
└─────────────────────────┘     │ prontuario_id BIGINT FK  │
                                │ consulta_id  BIGINT      │
       ┌────────────────────────│ medico_id    BIGINT      │
       │                        │ data         DATETIME    │
       │                        │ sintomas     TEXT        │
       │                        │ diagnostico  TEXT        │
       │                        │ escopo_medico TEXT       │
       │                        │ observacoes  TEXT        │
       │                        │ nivel_urgencia ENUM     │
       │                        │ criado_em    TIMESTAMP   │
       │                        │ atualizado_em TIMESTAMP  │
       │                        └──────────────────────────┘
       ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│ relatorios_clinicos  │  │      receitas        │  │ solicitacoes_exame   │
├──────────────────────┤  ├──────────────────────┤  ├──────────────────────┤
│ id       BIGINT PK   │  │ id       BIGINT PK   │  │ id       BIGINT PK   │
│ atend_id BIGINT FK   │  │ atend_id BIGINT FK   │  │ atend_id BIGINT FK   │
│ conteudo TEXT        │  │ medicamento VARCHAR  │  │ tipo_exame VARCHAR   │
│ criado_em TIMESTAMP  │  │ dosagem  VARCHAR     │  │ descricao TEXT       │
│ atualizado_em TS     │  │ frequencia VARCHAR   │  │ urgencia ENUM       │
└──────────────────────┘  │ duracao  VARCHAR     │  │ status   ENUM       │
                          │ observacoes TEXT     │  │ criado_em TIMESTAMP  │
┌──────────────────────┐  │ criado_em TIMESTAMP  │  └──────────────────────┘
│   encaminhamentos    │  └──────────────────────┘
├──────────────────────┤
│ id          BIGINT PK│
│ atend_id    BIGINT FK│
│ medico_origem BIGINT │
│ medico_destino BIGINT│
│ motivo      TEXT     │
│ especialidade VARCHAR│
│ prioridade  ENUM    │
│ status      ENUM    │
│ criado_em   TS      │
└──────────────────────┘
```

### SQL — atendimento_db

```sql
CREATE DATABASE IF NOT EXISTS atendimento_db;
USE atendimento_db;

CREATE TABLE prontuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL UNIQUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_paciente (paciente_id)
);

CREATE TABLE atendimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prontuario_id BIGINT NOT NULL,
    consulta_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    data_atendimento DATETIME NOT NULL,
    sintomas TEXT,
    diagnostico TEXT,
    escopo_medico TEXT NOT NULL,
    observacoes TEXT,
    nivel_urgencia ENUM('VERDE','AMARELO','LARANJA','VERMELHO') DEFAULT 'VERDE',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_atendimento_prontuario FOREIGN KEY (prontuario_id) REFERENCES prontuarios(id),
    INDEX idx_prontuario (prontuario_id),
    INDEX idx_consulta (consulta_id),
    INDEX idx_medico (medico_id)
);

CREATE TABLE relatorios_clinicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    conteudo TEXT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_relatorio_atendimento FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id)
);

CREATE TABLE receitas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    medicamento VARCHAR(200) NOT NULL,
    dosagem VARCHAR(100) NOT NULL,
    frequencia VARCHAR(100) NOT NULL,
    duracao VARCHAR(100) NOT NULL,
    observacoes TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receita_atendimento FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id)
);

CREATE TABLE solicitacoes_exame (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    tipo_exame VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    urgencia ENUM('NORMAL','URGENTE','EMERGENCIAL') DEFAULT 'NORMAL',
    status ENUM('SOLICITADO','AGENDADO','CONCLUIDO') DEFAULT 'SOLICITADO',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exame_atendimento FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id)
);

CREATE TABLE encaminhamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    medico_origem_id BIGINT NOT NULL,
    medico_destino_id BIGINT NOT NULL,
    motivo TEXT NOT NULL,
    especialidade_destino VARCHAR(100) NOT NULL,
    prioridade ENUM('BAIXA','MEDIA','ALTA','URGENTE') DEFAULT 'MEDIA',
    status ENUM('PENDENTE','ACEITO','EM_ATENDIMENTO','CONCLUIDO') DEFAULT 'PENDENTE',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_encaminhamento_atendimento FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id),
    INDEX idx_medico_destino (medico_destino_id),
    INDEX idx_status (status)
);
```

---

# 4. ESTRUTURA DE PASTAS

## 4.1 Estrutura Raiz

```
clinica-medica/
├── commons/
├── admin-service/
├── agendamento-service/
├── atendimento-service/
├── docker-compose.yml
├── build-and-run.sh
├── .gitignore
├── README.md
└── docs/
    ├── arquitetura.md
    ├── contratos-api.md
    ├── modelo-banco.md
    └── postman/
        ├── admin-service.postman_collection.json
        ├── agendamento-service.postman_collection.json
        └── atendimento-service.postman_collection.json
```

## 4.2 Estrutura Padrão de Microsserviço (exemplo: admin-service)

```
admin-service/
├── src/
│   ├── main/
│   │   ├── java/br/com/clinica/admin/
│   │   │   ├── AdminServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── MedicoController.java
│   │   │   │   ├── PacienteController.java
│   │   │   │   ├── EspecialidadeController.java
│   │   │   │   ├── ConvenioController.java
│   │   │   │   ├── UsuarioController.java
│   │   │   │   └── RelatorioController.java
│   │   │   ├── service/
│   │   │   │   ├── MedicoService.java
│   │   │   │   ├── PacienteService.java
│   │   │   │   ├── EspecialidadeService.java
│   │   │   │   ├── ConvenioService.java
│   │   │   │   ├── UsuarioService.java
│   │   │   │   └── RelatorioService.java
│   │   │   ├── repository/
│   │   │   │   ├── MedicoRepository.java
│   │   │   │   ├── PacienteRepository.java
│   │   │   │   ├── EspecialidadeRepository.java
│   │   │   │   ├── ConvenioRepository.java
│   │   │   │   └── UsuarioRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Medico.java
│   │   │   │   ├── Paciente.java
│   │   │   │   ├── Especialidade.java
│   │   │   │   ├── Convenio.java
│   │   │   │   ├── Usuario.java
│   │   │   │   └── Perfil.java
│   │   │   ├── dto/
│   │   │   │   ├── requisicao/
│   │   │   │   │   ├── MedicoRequisicao.java
│   │   │   │   │   ├── PacienteRequisicao.java
│   │   │   │   │   ├── EspecialidadeRequisicao.java
│   │   │   │   │   └── ConvenioRequisicao.java
│   │   │   │   └── resposta/
│   │   │   │       ├── MedicoResposta.java
│   │   │   │       ├── PacienteResposta.java
│   │   │   │       ├── EspecialidadeResposta.java
│   │   │   │       └── ConvenioResposta.java
│   │   │   ├── mapper/
│   │   │   │   ├── MedicoMapper.java
│   │   │   │   ├── PacienteMapper.java
│   │   │   │   ├── EspecialidadeMapper.java
│   │   │   │   └── ConvenioMapper.java
│   │   │   ├── config/
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── security/
│   │   │   │   ├── JwtService.java
│   │   │   │   └── JwtFiltroAutenticacao.java
│   │   │   └── validacao/
│   │   │       ├── CpfValido.java
│   │   │       └── ValidadorCpf.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/br/com/clinica/admin/
│           ├── controller/
│           │   └── MedicoControllerTest.java
│           └── service/
│               └── MedicoServiceTest.java
├── Dockerfile
└── pom.xml
```

## 4.3 Papel de Cada Camada

```
Request HTTP → Controller → Service → Repository → Database
```

| Camada | Responsabilidade | Regra |
|---|---|---|
| **Controller** | Recebe HTTP, valida DTO, retorna response | NUNCA contém lógica de negócio |
| **Service** | Regras de negócio, validações, orquestração | NUNCA acessa HttpServletRequest |
| **Repository** | Interface JPA para acesso a dados | NUNCA contém lógica de negócio |
| **Entity** | Mapeamento JPA da tabela | NUNCA exposta na API |
| **DTO Requisicao** | Dados recebidos do cliente | Contém validações Bean Validation |
| **DTO Resposta** | Dados retornados ao cliente | Apenas campos necessários |
| **Mapper** | Converte Entity ↔ DTO | Classe estática ou @Component |

## 4.4 application.yml do admin-service

```yaml
server:
  port: 8081

spring:
  application:
    name: admin-service
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/admin_db
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
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

*Continua na Parte 3 → Padronização Corporativa, Cronograma e Tarefas*

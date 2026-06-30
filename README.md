# Project Aura Clinic

Sistema moderno de gerenciamento para clínicas médicas baseado em uma arquitetura de microsserviços robusta, escalável e segura. O projeto utiliza as melhores práticas do ecossistema Spring Boot, Docker e MySQL, garantindo alta disponibilidade e separação clara de domínios.

---

## Arquitetura do Sistema

O ecossistema do Aura Clinic é dividido em microsserviços independentes, cada um responsável por um domínio de negócio específico e possuindo seu próprio banco de dados isolado:

```text
aura-clinic/
│
├── admin-service/          # Domínio administrativo, usuários, médicos, pacientes e relatórios
├── agendamento-service/    # Controle de agenda de consultas, remarcações e cancelamentos
├── atendimento-service/    # Domínio clínico (prontuários, receitas, exames, encaminhamentos)
├── commons/                # Biblioteca compartilhada (DTOs, Exceptions e Utilitários comuns)
│
├── docker-compose.yml      # Orquestração local dos microsserviços e bancos de dados
├── build-and-run.bat       # Script automatizado de build e execução para Windows
├── build-and-run.sh        # Script automatizado de build e execução para Linux/macOS
└── README.md               # Documentação principal do projeto
```

---

## Microsserviços e Funcionalidades

### 1. admin-service (Porta 8081)
O núcleo administrativo do sistema. Suas principais responsabilidades são:
* **Autenticação & Segurança**: Registro, login e controle de acesso com **JWT (JSON Web Token)** e perfis de permissão específicos (ex: Administrador).
* **Cadastros Base**: Gerenciamento de Médicos, Pacientes, Especialidades Médicas e Convênios.
* **Integração**: Fornece endpoints de validação consumidos via **OpenFeign** pelos demais microsserviços.
* **Relatórios Administrativos**: Geração de relatórios gerenciais estruturados.

### 2. agendamento-service (Porta 8082)
Gerencia o fluxo de consultas médicas:
* **Agendamento de Consultas**: Criação de novas consultas validando a disponibilidade do médico e cadastro do paciente via integração com o `admin-service`.
* **Remarcação**: Ajustes de datas e horários de consultas.
* **Cancelamento**: Registro de cancelamentos contendo o motivo detalhado.

### 3. atendimento-service (Porta 8083)
Focado na parte clínica e no prontuário do paciente:
* **Registros de Atendimento**: Criação e atualização de atendimentos clínicos.
* **Prontuário Eletrônico**: Atualização automática do prontuário do paciente após cada consulta.
* **Documentos Clínicos**: Emissão de Receitas Médicas, Solicitações de Exame e Relatórios Clínicos.
* **Encaminhamentos**: Encaminhamento de pacientes para outros especialistas dentro da clínica.

### 4. commons
Biblioteca Java compartilhada para otimizar o reaproveitamento de código:
* Contém DTOs comuns utilizados na comunicação inter-serviços.
* Tratamento global de exceções e validações customizadas (ex: Validadores de CPF/CNPJ).

---

## Tecnologias & Ferramentas (Tech Stack)

* **Java 17** & **Spring Boot 3.2.5**
* **Spring Security & JJWT (JSON Web Token)**
* **Spring Data JPA & Hibernate**
* **Spring Cloud OpenFeign** (comunicação síncrona entre microsserviços)
* **MySQL 8** (instâncias dedicadas para cada microsserviço)
* **Docker & Docker Compose** (conteinerização completa)
* **Lombok** & **MapStruct**
* **Springdoc OpenAPI (Swagger UI)**
* **Zalando Logbook** (auditoria estruturada de logs HTTP)

---

## Mapeamento de Portas e Bancos

Para garantir o isolamento completo exigido pelo padrão de microsserviços, cada aplicação possui sua própria porta e seu banco de dados MySQL dedicado:

| Serviço | Porta Aplicação | Banco de Dados | Porta Interna/Exposta |
| :--- | :---: | :--- | :---: |
| **admin-service** | `8081` | `admin-db` (MySQL) | `3306:3306` |
| **agendamento-service** | `8082` | `agendamento-db` (MySQL) | `3307:3306` |
| **atendimento-service** | `8083` | `atendimento-db` (MySQL) | `3308:3306` |

---

## Como Executar o Projeto

### Pré-requisitos
* **Docker Desktop** instalado e em execução.
* **Git** para clonar/gerenciar o repositório.

### Inicialização Rápida

1. **Clonar o Repositório**:
   ```bash
   git clone https://github.com/GrupoWinners/ProjectAuraClinic.git
   cd ProjectAuraClinic
   ```

2. **Iniciar Automatizado**:
   * No **Windows**: Dê duplo clique no arquivo `build-and-run.bat` ou rode no PowerShell:
     ```powershell
     .\build-and-run.bat
     ```
   * No **Linux/macOS**: Execute o script shell:
     ```bash
     chmod +x build-and-run.sh
     ./build-and-run.sh
     ```

*Esse script irá compilar e instalar a biblioteca `commons`, construir as imagens docker de cada microsserviço e subir os containers (aplicações e bancos de dados) em segundo plano automaticamente.*

---

## Documentação da API (Swagger UI)

Cada microsserviço conta com sua própria documentação OpenAPI interativa. Certifique-se de que os containers estejam rodando e acesse:

* **Swagger Admin-Service**: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
* **Swagger Agendamento-Service**: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
* **Swagger Atendimento-Service**: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

---

## Logs do Sistema

Você pode monitorar os logs dos containers em tempo real para fins de depuração:

* Ver todos os logs juntos:
  ```bash
  docker compose logs -f
  ```
* Ver logs de um microsserviço específico:
  ```bash
  docker compose logs -f admin-service
  docker compose logs -f agendamento-service
  docker compose logs -f atendimento-service
  ```

---

## Equipe de Desenvolvimento

| Nome | Função Principal |
| :--- | :--- |
| **João Vitor** | Tech Lead & Desenvolvedor Backend |
| **Samela** | DevOps & Desenvolvedora Backend |
| **Nadiny** | QA & Desenvolvedora Backend |

### Contribuidores Especiais
* **Maycon Fidelis**

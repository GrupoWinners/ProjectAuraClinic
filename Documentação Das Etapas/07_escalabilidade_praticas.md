# 🏥 PARTE 7 — ESCALABILIDADE, MELHORIAS FUTURAS E BOAS PRÁTICAS

---

# 22. ESCALABILIDADE E MELHORIAS FUTURAS

## 22.1 Roadmap de Evolução

```
v1.0 (Atual - MVP)         v2.0                      v3.0
────────────────────  →  ────────────────────  →  ────────────────────
✅ REST Síncrono          📬 Mensageria (RabbitMQ)  ☸️ Kubernetes
✅ OpenFeign              🗄️ Cache (Redis)           🔍 Service Discovery
✅ Docker Compose         📊 Monitoring              🚪 API Gateway
✅ JWT Manual             🔔 Notificações Email      📊 ELK Stack
✅ MySQL                  📧 Notification Service    🔄 CI/CD Pipeline
✅ 3 Microsserviços       📱 Frontend React/Angular  📱 App Mobile
```

## 22.2 Requisitos Opcionais — Como Encaixar no Futuro

### Requisito Opcional 1: Cadastro de Funcionários Gerais

**Impacto:** Baixo — adicionar entity `Funcionario` no `admin-service`.

```java
// Nova entity no admin-service
@Entity @Table(name = "funcionarios")
public class Funcionario {
    private Long id;
    private String nomeCompleto;
    private String rg;
    private String cpf;
    private String cargo; // RECEPCIONISTA, ENFERMEIRO, AUXILIAR, etc.
    private String ctps;
    private String pis;
    // ... endereço, contato, etc.
}
```

### Requisito Opcional 2: Receituário e Exames Detalhados

**Impacto:** Médio — expandir entidades no `atendimento-service`.

```java
// Expandir entity Receita
@Entity @Table(name = "receitas")
public class Receita {
    // ... campos existentes ...
    private String viaAdministracao; // ORAL, INTRAVENOSO, TOPICO
    private Boolean usoControlado;
    private String codigoMedicamento; // código ANVISA
    private LocalDate validadeReceita;
}
```

### Requisito Opcional 3: Cancelamento com Auditoria

**Impacto:** Baixo — adicionar campos à entity `Cancelamento` no `agendamento-service`.

```java
@Entity @Table(name = "cancelamentos")
public class Cancelamento {
    // ... campos existentes ...
    private String senhaSecretaria; // validada via BCrypt
    private String motivoDetalhado;
    private String ipRequisicao;
    private LocalDateTime dataHoraRegistro;
}
```

**Regra adicional:** Exigir senha da secretária no endpoint de cancelar e registrar em log de auditoria.

## 22.3 Melhorias Técnicas Detalhadas

### Cache com Redis
```
Cenário: Listagem de especialidades consultada em todo agendamento.
Problema: Query no banco a cada requisição.
Solução: Cache Redis com TTL de 1 hora.

@Cacheable(value = "especialidades", key = "#id")
public EspecialidadeResposta buscarPorId(Long id) { ... }
```

### Mensageria com RabbitMQ
```
Cenário: Ao cancelar consulta, notificar paciente.
Problema: Enviar email síncrono trava a requisição.
Solução: agendamento-service publica evento → notification-service consome.

agendamento-service → [RabbitMQ] → notification-service → Email/SMS
```

### API Gateway (Spring Cloud Gateway)
```
Cenário: Cliente precisa saber URL de cada serviço.
Solução: Gateway centraliza em uma única URL.

Cliente → API Gateway (:8080) → admin-service
                                → agendamento-service
                                → atendimento-service
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
      - run: cd agendamento-service && mvn clean verify
      - run: cd atendimento-service && mvn clean verify
```

### Monitoramento (Prometheus + Grafana)
```
Spring Boot Actuator → Prometheus → Grafana
Métricas: CPU, memória, requests/s, latência, erros, conexões DB
```

---

# 23. BOAS PRÁTICAS OBRIGATÓRIAS

## 23.1 Código

| Regra | Por Quê |
|---|---|
| **NUNCA** expor Entity na API | Entity tem relações JPA → loop infinito e exposição de dados |
| **SEMPRE** usar DTOs | Requisição e Resposta separados dão flexibilidade |
| **NUNCA** lógica no Controller | Controller é porta de entrada — delega para Service |
| **SEMPRE** tratar exceções | Erros sem tratamento expõem stack trace |
| **NUNCA** `System.out.println` | Usar `@Slf4j` do Lombok |
| **SEMPRE** validar input | `@Valid` + Bean Validation previnem dados corrompidos |
| **NUNCA** hardcodar configs | Usar `application.yml` + variáveis de ambiente |
| **SEMPRE** soft delete | DELETE físico perde dados e quebra integridade |
| **NUNCA** retornar senha | Mesmo criptografada, é risco de segurança |
| **SEMPRE** paginar listagens | 100 mil registros sem paginação trava a API |

## 23.2 Arquitetura

| Regra | Por Quê |
|---|---|
| **NUNCA** compartilhar banco | Acoplamento viola independência dos microsserviços |
| **SEMPRE** versionar APIs (`/v1/`) | Evolução sem quebrar consumidores |
| **NUNCA** Feign em loop | N chamadas HTTP = N * latência — usar batch endpoints |
| **SEMPRE** fallback para Feign | Serviço indisponível não deve derrubar quem consome |
| **SEMPRE** correlation-id | Sem ele, debugar fluxo entre 3 serviços é impossível |

## 23.3 Ambientes (Spring Profiles)

| Ambiente | Banco | Logs | Swagger | Debug |
|---|---|---|---|---|
| **dev** | MySQL local | DEBUG | Habilitado | Habilitado |
| **test** | H2 em memória | WARN | — | — |
| **prod** | MySQL em nuvem | WARN/ERROR | Desabilitado | Desabilitado |

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
git checkout -b feature/nome-da-feature
git add . && git commit -m "feat(admin): mensagem"
git push origin feature/nome-da-feature

# Maven
mvn clean install          # Build completo
mvn spring-boot:run        # Rodar localmente
mvn test                   # Rodar testes

# Docker
docker-compose up -d       # Subir containers
docker-compose down        # Parar
docker-compose logs -f <servico>  # Ver logs

# MySQL (acessar banco no container)
docker exec -it admin-db mysql -u root -p
```

---

> **🎓 NOTA FINAL:** Este guia simula um ambiente profissional de engenharia de software. Ao seguir todas as seções, os alunos terão experiência com as mesmas ferramentas, padrões e processos de empresas reais. O diferencial não está em "fazer funcionar", mas em fazer **como profissionais fazem** — com organização, padronização, testes, documentação e rastreabilidade.

---

*Fim do Manual — 7 Partes Completas | Projeto Aura Clinic v1.0.0*

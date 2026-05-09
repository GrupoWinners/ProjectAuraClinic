# 🏥 PARTE 5 — TESTES, SWAGGER, DOCKER E PIPELINE

---

# 14. TESTES AUTOMATIZADOS

## 14.1 Pirâmide de Testes

```
         /\
        /  \        Testes E2E (Postman)
       /    \       — Poucos, caros
      /──────\
     /        \     Testes de Integração
    /          \    — Controller + banco H2
   /────────────\
  /              \  Testes Unitários
 /                \ — Rápidos, muitos
/──────────────────\
```

## 14.2 Dependências

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## 14.3 Teste Unitário — Service

```java
@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @InjectMocks
    private MedicoService medicoService;

    @Test
    @DisplayName("Deve criar médico com sucesso")
    void deveCriarMedicoComSucesso() {
        // Arrange
        MedicoRequisicao requisicao = MedicoRequisicao.builder()
                .nomeCompleto("Dra. Maria Souza")
                .crm("CRM/SP 12345")
                .especialidadeId(1L)
                .build();

        Especialidade especialidade = Especialidade.builder().id(1L).descricao("Cardiologia").build();
        Medico medicoSalvo = Medico.builder().id(3L).nomeCompleto("Dra. Maria Souza").crm("CRM/SP 12345").build();

        when(medicoRepository.existsByCrm(requisicao.getCrm())).thenReturn(false);
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(medicoRepository.save(any(Medico.class))).thenReturn(medicoSalvo);

        // Act
        MedicoResposta resposta = medicoService.criar(requisicao);

        // Assert
        assertNotNull(resposta);
        assertEquals(3L, resposta.getId());
        assertEquals("Dra. Maria Souza", resposta.getNomeCompleto());
        verify(medicoRepository, times(1)).save(any(Medico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar médico com CRM duplicado")
    void deveLancarExcecaoQuandoCrmDuplicado() {
        MedicoRequisicao requisicao = MedicoRequisicao.builder().crm("CRM/SP 12345").build();
        when(medicoRepository.existsByCrm(requisicao.getCrm())).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> medicoService.criar(requisicao));
        verify(medicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar médico inexistente")
    void deveLancarExcecaoQuandoMedicoNaoEncontrado() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> medicoService.buscarPorId(99L));
    }
}
```

## 14.4 Teste de Integração — Controller

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MedicoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MedicoRepository medicoRepository;

    @BeforeEach
    void setUp() { medicoRepository.deleteAll(); }

    @Test
    @DisplayName("POST /api/v1/medicos - Deve criar e retornar 201")
    void deveCriarMedicoERetornar201() throws Exception {
        MedicoRequisicao req = MedicoRequisicao.builder()
                .nomeCompleto("Dr. João").crm("CRM/SP 99999").especialidadeId(1L).build();

        mockMvc.perform(post("/api/v1/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nomeCompleto").value("Dr. João"));
    }

    @Test
    @DisplayName("GET /api/v1/medicos/{id} - Deve retornar 404")
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/medicos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }
}
```

### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
```

## 14.5 Cobertura Recomendada

| Camada | Mínimo | O Que Testar |
|---|---|---|
| Service | 80% | Regras de negócio, validações, exceções |
| Controller | 70% | Status codes, payloads |
| Repository | 60% | Queries customizadas |

---

# 15. TESTES MANUAIS (Postman)

## 15.1 Organização

```
📁 Clínica Médica
├── 📁 admin-service
│   ├── 📁 Médicos (POST, GET, PUT, DELETE)
│   ├── 📁 Pacientes
│   ├── 📁 Especialidades
│   ├── 📁 Convênios
│   ├── 📁 Usuários
│   └── 📁 Relatórios
├── 📁 agendamento-service
│   ├── 📁 Consultas (Agendar, Remarcar, Cancelar)
│   ├── 📁 Disponibilidade
│   └── 📁 Bloqueios
└── 📁 atendimento-service
    ├── 📁 Atendimentos
    ├── 📁 Relatórios Clínicos
    ├── 📁 Encaminhamentos
    └── 📁 Reconsultas
```

## 15.2 Cenários de Teste

### Positivos (Happy Path)
- [ ] Criar recurso com campos válidos → 201
- [ ] Listar recursos → 200
- [ ] Buscar por ID existente → 200
- [ ] Atualizar existente → 200
- [ ] Deletar existente → 204
- [ ] Agendar consulta válida → 201
- [ ] Cancelar com motivo → 200

### Negativos (Error Path)
- [ ] Criar sem campo obrigatório → 400
- [ ] CPF inválido → 400
- [ ] CPF duplicado → 409
- [ ] ID inexistente → 404
- [ ] Agendar em horário conflitante → 422
- [ ] Agendar em horário bloqueado → 422
- [ ] Cancelar sem motivo → 400
- [ ] Acessar sem token → 401
- [ ] Secretária tentar deletar → 403

---

# 16. DOCUMENTAÇÃO SWAGGER / OPENAPI

## 16.1 Dependência

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## 16.2 Configuração

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Admin Service API")
                .description("Microsserviço administrativo da clínica médica")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes("Bearer Auth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
```

## 16.3 Anotações no Controller

```java
@RestController
@RequestMapping("/api/v1/medicos")
@RequiredArgsConstructor
@Tag(name = "Médicos", description = "Gerenciamento de médicos da clínica")
public class MedicoController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar médico", description = "Cadastra um novo médico no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Médico criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "CRM já cadastrado")
    })
    public MedicoResposta criar(@RequestBody @Valid MedicoRequisicao requisicao) {
        return medicoService.criar(requisicao);
    }
}
```

### Acesso: `http://localhost:8081/swagger-ui.html`

---

# 17. DOCKERIZAÇÃO

## 17.1 Dockerfile Padrão (multi-stage)

```dockerfile
# Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 17.2 Docker Compose

```yaml
version: '3.8'
services:
  # ===== BANCOS =====
  admin-db:
    image: mysql:8.0
    container_name: admin-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: admin_db
    ports: ["3306:3306"]
    volumes: [admin_data:/var/lib/mysql]
    networks: [clinica-network]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  agendamento-db:
    image: mysql:8.0
    container_name: agendamento-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: agendamento_db
    ports: ["3307:3306"]
    volumes: [agendamento_data:/var/lib/mysql]
    networks: [clinica-network]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  atendimento-db:
    image: mysql:8.0
    container_name: atendimento-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: atendimento_db
    ports: ["3308:3306"]
    volumes: [atendimento_data:/var/lib/mysql]
    networks: [clinica-network]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ===== MICROSSERVIÇOS =====
  admin-service:
    build: ./admin-service
    container_name: admin-service
    ports: ["8081:8081"]
    environment:
      DB_HOST: admin-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
    depends_on:
      admin-db: { condition: service_healthy }
    networks: [clinica-network]

  agendamento-service:
    build: ./agendamento-service
    container_name: agendamento-service
    ports: ["8082:8082"]
    environment:
      DB_HOST: agendamento-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
      ADMIN_SERVICE_URL: http://admin-service:8081
    depends_on:
      agendamento-db: { condition: service_healthy }
      admin-service: { condition: service_started }
    networks: [clinica-network]

  atendimento-service:
    build: ./atendimento-service
    container_name: atendimento-service
    ports: ["8083:8083"]
    environment:
      DB_HOST: atendimento-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
      ADMIN_SERVICE_URL: http://admin-service:8081
      AGENDAMENTO_SERVICE_URL: http://agendamento-service:8082
    depends_on:
      atendimento-db: { condition: service_healthy }
      admin-service: { condition: service_started }
      agendamento-service: { condition: service_started }
    networks: [clinica-network]

volumes:
  admin_data:
  agendamento_data:
  atendimento_data:

networks:
  clinica-network:
    driver: bridge
```

## 17.3 Comandos Docker

```bash
docker-compose up -d --build          # Subir tudo
docker-compose up -d admin-db agendamento-db atendimento-db  # Só bancos
docker-compose logs -f admin-service  # Logs
docker-compose down                   # Parar
docker-compose down -v                # Parar + remover dados
```

---

# 18. PIPELINE DE BUILD E EXECUÇÃO

## 18.1 Maven Lifecycle

| Comando | O que faz |
|---|---|
| `mvn clean` | Limpa target |
| `mvn compile` | Compila |
| `mvn test` | Roda testes |
| `mvn package` | Gera JAR |
| `mvn spring-boot:run` | Roda em dev |

## 18.2 Script build-and-run.sh

```bash
#!/bin/bash
set -e
echo "========================================"
echo "   AURA CLINIC — Build & Run Pipeline"
echo "========================================"

echo "[1/4] Building commons..."
cd commons && mvn clean install -DskipTests && cd ..

echo "[2/4] Building admin-service..."
cd admin-service && mvn clean package -DskipTests && cd ..

echo "[3/4] Building agendamento-service..."
cd agendamento-service && mvn clean package -DskipTests && cd ..

echo "[4/4] Building atendimento-service..."
cd atendimento-service && mvn clean package -DskipTests && cd ..

echo "Starting Docker..."
docker-compose up -d --build

echo ""
echo "Serviços disponíveis:"
echo "  admin-service:       http://localhost:8081/swagger-ui.html"
echo "  agendamento-service: http://localhost:8082/swagger-ui.html"
echo "  atendimento-service: http://localhost:8083/swagger-ui.html"
```

---

*Continua na Parte 6 → Versionamento, Checklists e Riscos*

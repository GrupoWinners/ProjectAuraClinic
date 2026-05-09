# 🏥 GUIA DE IMPLEMENTAÇÃO — SISTEMA DE CLÍNICA MÉDICA
## Parte 5: Testes, Swagger, Docker e Pipeline

---

# 14. TESTES AUTOMATIZADOS

## 14.1 Pirâmide de Testes

```
         /\
        /  \        Testes E2E (manuais/Postman)
       /    \       — Poucos, caros, lentos
      /──────\
     /        \     Testes de Integração
    /          \    — Controller + banco real
   /────────────\
  /              \  Testes Unitários
 /                \ — Rápidos, isolados, muitos
/──────────────────\
```

## 14.2 Dependências de Teste

```xml
<!-- Já vem com Spring Boot Starter Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 para testes de integração (banco em memória) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## 14.3 Teste Unitário — Service

Testa a lógica de negócio **isolada** (sem banco, sem HTTP):

```java
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AdminServiceClient adminServiceClient;

    @InjectMocks
    private PatientService patientService;

    @Test
    @DisplayName("Deve criar paciente com sucesso")
    void shouldCreatePatientSuccessfully() {
        // Arrange (Preparar)
        PatientRequest request = PatientRequest.builder()
                .fullName("João da Silva")
                .cpf("123.456.789-09")
                .rg("12.345.678-9")
                .address("Rua das Flores, 100")
                .city("São Paulo")
                .state("SP")
                .zipCode("01234-567")
                .cellphone("(11) 99999-0000")
                .birthDate(LocalDate.of(1990, 5, 15))
                .gender(Gender.MASCULINO)
                .hasInsurance(false)
                .build();

        Patient savedPatient = Patient.builder()
                .id(1L)
                .fullName("João da Silva")
                .cpf("123.456.789-09")
                .active(true)
                .build();

        when(patientRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act (Agir)
        PatientResponse response = patientService.create(request);

        // Assert (Verificar)
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("João da Silva", response.getFullName());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar paciente com CPF duplicado")
    void shouldThrowExceptionWhenCpfDuplicate() {
        PatientRequest request = PatientRequest.builder()
                .cpf("123.456.789-09")
                .build();

        when(patientRepository.existsByCpf(request.getCpf())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> patientService.create(request));

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar paciente inexistente")
    void shouldThrowExceptionWhenPatientNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.findById(99L));
    }

    @Test
    @DisplayName("Deve validar convênio via Feign ao criar paciente com seguro")
    void shouldValidateInsuranceViaFeign() {
        PatientRequest request = PatientRequest.builder()
                .fullName("Maria Souza")
                .cpf("987.654.321-00")
                .hasInsurance(true)
                .insuranceId(5L)
                .build();

        when(patientRepository.existsByCpf(any())).thenReturn(false);
        when(adminServiceClient.getHealthInsurance(5L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> patientService.create(request));
    }
}
```

## 14.4 Teste de Integração — Controller

Testa o endpoint completo (HTTP → Controller → Service → Repository → H2):

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @MockBean
    private AdminServiceClient adminServiceClient; // Mock do Feign

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/patients - Deve criar paciente e retornar 201")
    void shouldCreatePatientAndReturn201() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .fullName("João da Silva")
                .cpf("529.982.247-25")
                .rg("12.345.678-9")
                .address("Rua das Flores, 100")
                .city("São Paulo")
                .state("SP")
                .zipCode("01234-567")
                .cellphone("(11) 99999-0000")
                .birthDate(LocalDate.of(1990, 5, 15))
                .gender(Gender.MASCULINO)
                .hasInsurance(false)
                .build();

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").value("529.982.247-25"));
    }

    @Test
    @DisplayName("POST /api/v1/patients - Deve retornar 400 com CPF inválido")
    void shouldReturn400WithInvalidCpf() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .fullName("João")
                .cpf("000.000.000-00")
                .build();

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/patients/{id} - Deve retornar 404 para ID inexistente")
    void shouldReturn404ForNonExistentPatient() throws Exception {
        mockMvc.perform(get("/api/v1/patients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Paciente com ID 999 não encontrado"));
    }
}
```

### application-test.yml (perfil de teste)
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
  h2:
    console:
      enabled: false
```

## 14.5 Cobertura de Testes Recomendada

| Camada | Cobertura Mínima | O Que Testar |
|---|---|---|
| Service | 80% | Regras de negócio, validações, exceções |
| Controller | 70% | Status codes, payloads, validações |
| Repository | 60% | Queries customizadas |

---

# 15. TESTES MANUAIS (Postman)

## 15.1 Organização das Collections

```
📁 Clinica Médica
├── 📁 auth-service
│   ├── POST Login
│   ├── POST Register
│   └── POST Refresh Token
├── 📁 admin-service
│   ├── 📁 Employees
│   │   ├── POST Criar Funcionário
│   │   ├── GET Listar Funcionários
│   │   ├── GET Buscar por ID
│   │   ├── PUT Atualizar Funcionário
│   │   └── DELETE Excluir Funcionário
│   ├── 📁 Specialties
│   ├── 📁 Doctors
│   └── 📁 Health Insurances
├── 📁 patient-service
├── 📁 scheduling-service
└── 📁 medical-record-service
```

## 15.2 Checklist de Cenários de Teste

### Cenários Positivos (Happy Path)
- [ ] Criar recurso com todos os campos válidos → 201
- [ ] Listar recursos (vazio) → 200 + array vazio
- [ ] Listar recursos (com dados) → 200 + array com dados
- [ ] Buscar por ID existente → 200
- [ ] Atualizar recurso existente → 200
- [ ] Deletar recurso existente → 204

### Cenários Negativos (Error Path)
- [ ] Criar com campo obrigatório faltando → 400
- [ ] Criar com CPF inválido → 400
- [ ] Criar com CPF duplicado → 409
- [ ] Buscar por ID inexistente → 404
- [ ] Atualizar recurso inexistente → 404
- [ ] Enviar JSON malformado → 400
- [ ] Acessar sem token JWT → 401
- [ ] Acessar com token expirado → 401
- [ ] Acessar endpoint sem permissão → 403
- [ ] Agendar em horário conflitante → 422
- [ ] Cancelar sem motivo → 400

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
                .description("Microsserviço de gestão administrativa da clínica médica")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipe Clínica Médica")
                    .email("equipe@clinica.com"))
            )
            .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes("Bearer Auth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );
    }
}
```

## 16.3 Anotações no Controller

```java
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Gerenciamento de pacientes da clínica")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Criar paciente",
        description = "Cadastra um novo paciente no sistema"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Paciente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    public PatientResponse create(
            @RequestBody @Valid PatientRequest request) {
        return patientService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public PatientResponse findById(
            @PathVariable @Parameter(description = "ID do paciente") Long id) {
        return patientService.findById(id);
    }
}
```

### Acesso ao Swagger UI
- `http://localhost:8081/swagger-ui.html` (admin-service)
- `http://localhost:8082/swagger-ui.html` (patient-service)

---

# 17. ESTRATÉGIA DE DOCKERIZAÇÃO

## 17.1 Dockerfile Padrão (igual para todos os serviços)

```dockerfile
# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Se usa commons como módulo local, copie também:
# COPY commons ../commons
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

> **Multi-stage build:** A imagem final contém apenas o JRE e o JAR, sem Maven nem código fonte. Resultado: imagem ~200MB em vez de ~800MB.

## 17.2 Docker Compose Completo

```yaml
version: '3.8'

services:
  # ==================== BANCOS DE DADOS ====================

  auth-db:
    image: mysql:8.0
    container_name: auth-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: auth_db
    ports:
      - "3306:3306"
    volumes:
      - auth_data:/var/lib/mysql
    networks:
      - clinica-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  admin-db:
    image: mysql:8.0
    container_name: admin-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: admin_db
    ports:
      - "3307:3306"
    volumes:
      - admin_data:/var/lib/mysql
    networks:
      - clinica-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  patient-db:
    image: mysql:8.0
    container_name: patient-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: patient_db
    ports:
      - "3308:3306"
    volumes:
      - patient_data:/var/lib/mysql
    networks:
      - clinica-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  scheduling-db:
    image: mysql:8.0
    container_name: scheduling-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: scheduling_db
    ports:
      - "3309:3306"
    volumes:
      - scheduling_data:/var/lib/mysql
    networks:
      - clinica-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  medical-record-db:
    image: mysql:8.0
    container_name: medical-record-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: medical_record_db
    ports:
      - "3310:3306"
    volumes:
      - medical_data:/var/lib/mysql
    networks:
      - clinica-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ==================== MICROSSERVIÇOS ====================

  auth-service:
    build: ./auth-service
    container_name: auth-service
    ports:
      - "8080:8080"
    environment:
      DB_HOST: auth-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
      ADMIN_SERVICE_URL: http://admin-service:8081
    depends_on:
      auth-db:
        condition: service_healthy
    networks:
      - clinica-network

  admin-service:
    build: ./admin-service
    container_name: admin-service
    ports:
      - "8081:8081"
    environment:
      DB_HOST: admin-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
    depends_on:
      admin-db:
        condition: service_healthy
    networks:
      - clinica-network

  patient-service:
    build: ./patient-service
    container_name: patient-service
    ports:
      - "8082:8082"
    environment:
      DB_HOST: patient-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
      ADMIN_SERVICE_URL: http://admin-service:8081
    depends_on:
      patient-db:
        condition: service_healthy
      admin-service:
        condition: service_started
    networks:
      - clinica-network

  scheduling-service:
    build: ./scheduling-service
    container_name: scheduling-service
    ports:
      - "8083:8083"
    environment:
      DB_HOST: scheduling-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
      PATIENT_SERVICE_URL: http://patient-service:8082
      ADMIN_SERVICE_URL: http://admin-service:8081
      AUTH_SERVICE_URL: http://auth-service:8080
    depends_on:
      scheduling-db:
        condition: service_healthy
      patient-service:
        condition: service_started
      admin-service:
        condition: service_started
    networks:
      - clinica-network

  medical-record-service:
    build: ./medical-record-service
    container_name: medical-record-service
    ports:
      - "8084:8084"
    environment:
      DB_HOST: medical-record-db
      DB_PORT: 3306
      DB_USERNAME: root
      DB_PASSWORD: root
      PATIENT_SERVICE_URL: http://patient-service:8082
      SCHEDULING_SERVICE_URL: http://scheduling-service:8083
      ADMIN_SERVICE_URL: http://admin-service:8081
    depends_on:
      medical-record-db:
        condition: service_healthy
      patient-service:
        condition: service_started
      scheduling-service:
        condition: service_started
    networks:
      - clinica-network

volumes:
  auth_data:
  admin_data:
  patient_data:
  scheduling_data:
  medical_data:

networks:
  clinica-network:
    driver: bridge
```

## 17.3 Comandos Docker

```bash
# Subir tudo (bancos + serviços)
docker-compose up -d --build

# Subir apenas os bancos (desenvolvimento)
docker-compose up -d auth-db admin-db patient-db scheduling-db medical-record-db

# Ver logs de um serviço
docker-compose logs -f admin-service

# Parar tudo
docker-compose down

# Parar tudo e remover volumes (CUIDADO: apaga dados)
docker-compose down -v

# Ver status
docker-compose ps
```

---

# 18. PIPELINE DE BUILD E EXECUÇÃO

## 18.1 Maven Lifecycle

```
validate → compile → test → package → verify → install → deploy
```

| Comando | O que faz |
|---|---|
| `mvn clean` | Limpa diretório target |
| `mvn compile` | Compila o código |
| `mvn test` | Roda testes unitários |
| `mvn package` | Gera o JAR |
| `mvn package -DskipTests` | Gera JAR sem rodar testes |
| `mvn spring-boot:run` | Roda a aplicação em dev |

## 18.2 Script build-and-run.sh

```bash
#!/bin/bash
set -e

echo "================================================"
echo "   CLÍNICA MÉDICA — Build & Run Pipeline"
echo "================================================"

echo ""
echo "[1/6] Building commons module..."
cd commons
mvn clean install -DskipTests
cd ..

echo ""
echo "[2/6] Building auth-service..."
cd auth-service
mvn clean package -DskipTests
cd ..

echo ""
echo "[3/6] Building admin-service..."
cd admin-service
mvn clean package -DskipTests
cd ..

echo ""
echo "[4/6] Building patient-service..."
cd patient-service
mvn clean package -DskipTests
cd ..

echo ""
echo "[5/6] Building scheduling-service..."
cd scheduling-service
mvn clean package -DskipTests
cd ..

echo ""
echo "[6/6] Building medical-record-service..."
cd medical-record-service
mvn clean package -DskipTests
cd ..

echo ""
echo "================================================"
echo "   All services built! Starting Docker..."
echo "================================================"
docker-compose up -d --build

echo ""
echo "Done! Services available at:"
echo "  auth-service:           http://localhost:8080"
echo "  admin-service:          http://localhost:8081"
echo "  patient-service:        http://localhost:8082"
echo "  scheduling-service:     http://localhost:8083"
echo "  medical-record-service: http://localhost:8084"
echo ""
echo "Swagger UIs:"
echo "  http://localhost:8081/swagger-ui.html"
echo "  http://localhost:8082/swagger-ui.html"
echo "  http://localhost:8083/swagger-ui.html"
echo "  http://localhost:8084/swagger-ui.html"
```

---

*Continua na Parte 6 → Versionamento, Checklists, Riscos, Escalabilidade e Boas Práticas*

# 🏥 GUIA DE IMPLEMENTAÇÃO — SISTEMA DE CLÍNICA MÉDICA
## Parte 4: Comunicação, Segurança, Exceções e Logs

---

# 10. ESTRATÉGIA DE COMUNICAÇÃO ENTRE MICROSSERVIÇOS

## 10.1 OpenFeign — O Que É

OpenFeign é um **cliente HTTP declarativo** do Spring Cloud. Em vez de escrever código com `RestTemplate` ou `WebClient`, você define uma **interface Java** e o Feign gera a implementação automaticamente.

### Dependência Maven
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### Habilitar no Application
```java
@SpringBootApplication
@EnableFeignClients
public class SchedulingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulingServiceApplication.class, args);
    }
}
```

## 10.2 Exemplo Prático — Feign Client

O `scheduling-service` precisa validar se o paciente está ativo antes de agendar:

```java
package br.com.clinica.scheduling.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "patient-service",
    url = "${services.patient-service.url}",
    fallback = PatientServiceClientFallback.class
)
public interface PatientServiceClient {

    @GetMapping("/api/v1/patients/{id}/validate-active")
    Boolean validatePatientActive(@PathVariable("id") Long id);

    @GetMapping("/api/v1/patients/{id}")
    PatientResponse getPatientById(@PathVariable("id") Long id);
}
```

### Configuração da URL no application.yml
```yaml
services:
  patient-service:
    url: ${PATIENT_SERVICE_URL:http://localhost:8082}
  admin-service:
    url: ${ADMIN_SERVICE_URL:http://localhost:8081}
  auth-service:
    url: ${AUTH_SERVICE_URL:http://localhost:8080}
```

## 10.3 Uso no Service

```java
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientServiceClient patientServiceClient;
    private final AdminServiceClient adminServiceClient;

    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // 1. Validar paciente via Feign
        Boolean patientActive = patientServiceClient.validatePatientActive(request.getPatientId());
        if (patientActive == null || !patientActive) {
            throw new BusinessException("Paciente não encontrado ou inativo");
        }

        // 2. Validar médico via Feign
        Boolean doctorActive = adminServiceClient.validateDoctorActive(request.getDoctorId());
        if (doctorActive == null || !doctorActive) {
            throw new BusinessException("Médico não encontrado ou inativo");
        }

        // 3. Validar conflito de horário
        validateTimeConflict(request.getDoctorId(), request.getDateTime());

        // 4. Criar agendamento
        Appointment appointment = AppointmentMapper.toEntity(request);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment = appointmentRepository.save(appointment);

        return AppointmentMapper.toResponse(appointment);
    }
}
```

## 10.4 Timeouts e Retries

```yaml
# application.yml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connect-timeout: 5000    # 5 segundos para conectar
            read-timeout: 10000      # 10 segundos para ler resposta
            logger-level: FULL       # Log completo das chamadas
```

### Retry com Retryer

```java
@Configuration
public class FeignConfig {

    @Bean
    public Retryer retryer() {
        // Retry até 3 vezes, com intervalo de 1 segundo, máximo 3 segundos
        return new Retryer.Default(1000, 3000, 3);
    }
}
```

## 10.5 Fallback (Circuit Breaker Pattern)

Quando o serviço destino está fora, o fallback retorna uma resposta padrão em vez de lançar exceção:

```java
@Component
public class PatientServiceClientFallback implements PatientServiceClient {

    @Override
    public Boolean validatePatientActive(Long id) {
        // Em caso de falha, retorna false (segurança)
        return false;
    }

    @Override
    public PatientResponse getPatientById(Long id) {
        // Retorna null — o service deve tratar
        return null;
    }
}
```

## 10.6 Tratamento de Erro Feign (ErrorDecoder)

```java
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new ResourceNotFoundException(
                "Recurso não encontrado no serviço remoto");
            case 400 -> new BusinessException(
                "Requisição inválida para o serviço remoto");
            case 503 -> new IntegrationException(
                "Serviço temporariamente indisponível");
            default -> new IntegrationException(
                "Erro na comunicação com serviço: " + response.status());
        };
    }
}
```

## 10.7 Boas Práticas de Comunicação

| Prática | Descrição |
|---|---|
| **Timeouts** | Sempre configurar connect-timeout e read-timeout |
| **Retry** | Máximo 3 tentativas com backoff |
| **Fallback** | Sempre ter resposta padrão para falhas |
| **Idempotência** | GETs são seguros para retry; POSTs precisam de cuidado |
| **Circuit Breaker** | Evita cascata de falhas entre serviços |
| **Logs** | Logar todas as chamadas Feign (request + response) |
| **URLs configuráveis** | Nunca hardcodar URLs — usar variáveis de ambiente |

---

# 11. ESTRATÉGIA DE AUTENTICAÇÃO E SEGURANÇA

## 11.1 Fluxo de Autenticação JWT

```
1. Cliente envia POST /api/v1/auth/login com username + password
2. auth-service valida credenciais no banco
3. Se válido, gera JWT (access token + refresh token)
4. Cliente armazena o token
5. Nas próximas requisições, envia: Authorization: Bearer <token>
6. Cada microsserviço valida o token antes de processar
```

```
┌──────────┐                    ┌──────────────┐
│ Cliente  │───POST /login─────►│ auth-service │
│          │◄──JWT Token────────│              │
│          │                    └──────────────┘
│          │
│          │──GET /patients─────►┌────────────────┐
│          │  Authorization:     │patient-service │
│          │  Bearer eyJhbG...   │ valida JWT     │
│          │◄──200 OK + data─────│                │
│          │                     └────────────────┘
```

## 11.2 Estrutura do JWT

```json
// Header
{
  "alg": "HS256",
  "typ": "JWT"
}

// Payload
{
  "sub": "admin@clinica.com",
  "userId": 1,
  "role": "ADMIN",
  "iat": 1715255400,
  "exp": 1715341800
}

// Signature
HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

## 11.3 Implementação do JwtService

```java
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration; // 86400000 (24h em ms)

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration; // 604800000 (7 dias em ms)

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().getName());
        return buildToken(claims, user.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user.getUsername(), refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

## 11.4 Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

## 11.5 JWT Authentication Filter

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (jwtService.isTokenValid(token)) {
            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    username, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
```

## 11.6 Roles e Permissões

| Role | Permissões |
|---|---|
| `ADMIN` | Tudo — CRUD de funcionários, usuários, convênios, visualização total |
| `DOCTOR` | Acessar agenda própria, registrar atendimento, criar receita, solicitar exames |
| `RECEPTIONIST` | Cadastrar pacientes, agendar/cancelar consultas, visualizar agenda |
| `NURSE` | Visualizar prontuário, registrar triagem |

## 11.7 Proteção Contra Ataques

| Ataque | Mitigação |
|---|---|
| Brute Force | Bloqueio após 5 tentativas + cooldown 15min |
| SQL Injection | Spring Data JPA parametriza queries automaticamente |
| XSS | Spring Security headers + sanitização |
| CSRF | Desabilitado (API stateless com JWT) |
| Token Roubo | Expiração curta + HTTPS obrigatório em produção |

---

# 12. TRATAMENTO GLOBAL DE EXCEÇÕES

## 12.1 Padrão de Resposta de Erro

Toda resposta de erro segue o **mesmo formato JSON** em todos os microsserviços:

```java
// commons/dto/ErrorResponse.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;
}

@Data
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
}
```

Exemplo de resposta de erro:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Paciente com ID 99 não encontrado",
  "path": "/api/v1/patients/99",
  "timestamp": "2026-05-09T10:30:00",
  "validationErrors": null
}
```

Exemplo de erro de validação:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "path": "/api/v1/patients",
  "timestamp": "2026-05-09T10:30:00",
  "validationErrors": [
    { "field": "cpf", "message": "CPF inválido" },
    { "field": "fullName", "message": "Nome é obrigatório" }
  ]
}
```

## 12.2 Exceções Customizadas (commons)

```java
// Recurso não encontrado → 404
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}

// Recurso duplicado → 409
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
}

// Regra de negócio violada → 422
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}

// Falha de integração com outro serviço → 503
public class IntegrationException extends RuntimeException {
    public IntegrationException(String message) { super(message); }
}
```

## 12.3 Global Exception Handler (commons)

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Recurso duplicado: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {
        log.warn("Erro de negócio: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegration(
            IntegrationException ex, HttpServletRequest request) {
        log.error("Falha de integração: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("Erro de validação: {} campos inválidos", errors.size());
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Erro de validação")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .validationErrors(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
```

## 12.4 Mapa de Códigos HTTP

| Código | Quando Usar | Exceção |
|---|---|---|
| `200` | Sucesso (GET, PUT) | — |
| `201` | Recurso criado (POST) | — |
| `204` | Sem conteúdo (DELETE) | — |
| `400` | Dados inválidos | `MethodArgumentNotValidException` |
| `401` | Não autenticado | `AuthenticationException` |
| `403` | Sem permissão | `AccessDeniedException` |
| `404` | Recurso não encontrado | `ResourceNotFoundException` |
| `409` | Conflito (CPF duplicado) | `DuplicateResourceException` |
| `422` | Regra de negócio violada | `BusinessException` |
| `503` | Serviço indisponível | `IntegrationException` |
| `500` | Erro inesperado | `Exception` |

---

# 13. ESTRATÉGIA DE LOGS E OBSERVABILIDADE

## 13.1 Logbook — Logging HTTP Automático

Logbook intercepta **todas as requisições e respostas HTTP** automaticamente.

### Dependência Maven
```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
    <version>3.7.0</version>
</dependency>
```

### Configuração (application.yml)
```yaml
logbook:
  filter:
    enabled: true
  format:
    style: json
  obfuscate:
    headers:
      - Authorization
    parameters:
      - password
  minimum-status: 400  # Logar apenas respostas com erro
```

### Exemplo de Log Gerado
```json
{
  "origin": "remote",
  "type": "request",
  "correlation": "abc-123-def",
  "method": "POST",
  "uri": "/api/v1/patients",
  "headers": {
    "Content-Type": ["application/json"],
    "Authorization": ["XXX"]
  },
  "body": {
    "fullName": "João da Silva",
    "cpf": "123.456.789-00"
  }
}
```

## 13.2 Correlation ID (Rastreamento)

Para rastrear uma requisição que passa por múltiplos serviços:

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);
        response.setHeader(CORRELATION_HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
```

### Propagar Correlation ID nas chamadas Feign

```java
@Component
public class FeignCorrelationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            template.header("X-Correlation-ID", correlationId);
        }
    }
}
```

### Configurar formato do log com correlation ID

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{correlationId}] %-5level %logger{36} - %msg%n"
```

Resultado:
```
2026-05-09 10:30:00 [http-nio-8083-exec-1] [abc-123-def] INFO  AppointmentService - Agendamento criado: ID=42
```

## 13.3 Boas Práticas de Logging

| Nível | Quando Usar | Exemplo |
|---|---|---|
| `ERROR` | Falhas que impedem a operação | `log.error("Falha ao salvar paciente", exception)` |
| `WARN` | Situações inesperadas mas tratáveis | `log.warn("Paciente ID={} não encontrado", id)` |
| `INFO` | Operações de negócio importantes | `log.info("Consulta agendada: ID={}", appointmentId)` |
| `DEBUG` | Detalhes técnicos para debugging | `log.debug("Validando CPF: {}", cpf)` |

**Regras:**
1. **NUNCA logar dados sensíveis** (senha, token completo, dados de cartão)
2. **SEMPRE incluir IDs** para rastreabilidade
3. **SEMPRE incluir correlation-id** em chamadas entre serviços
4. **Usar SLF4J** (`@Slf4j` do Lombok) — nunca `System.out.println`
5. **Logar exceções com stack trace** no nível ERROR: `log.error("Mensagem", ex)`

---

*Continua na Parte 5 → Testes, Swagger, Docker, Pipeline, Versionamento e Checklists*

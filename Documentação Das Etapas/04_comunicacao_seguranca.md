# 🏥 PARTE 4 — COMUNICAÇÃO, SEGURANÇA, EXCEÇÕES E LOGS

---

# 10. COMUNICAÇÃO ENTRE MICROSSERVIÇOS (OpenFeign)

## 10.1 O Que É OpenFeign

OpenFeign é um **cliente HTTP declarativo**. Em vez de escrever `RestTemplate`, você define uma **interface Java** e o Feign gera a implementação.

### Dependência Maven
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### Habilitar na Application
```java
@SpringBootApplication
@EnableFeignClients
public class AgendamentoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendamentoServiceApplication.class, args);
    }
}
```

## 10.2 Exemplo — Feign Client no agendamento-service

```java
package br.com.clinica.agendamento.client;

@FeignClient(
    name = "admin-service",
    url = "${servicos.admin-service.url}",
    fallback = AdminServiceClientFallback.class
)
public interface AdminServiceClient {

    @GetMapping("/api/v1/pacientes/{id}/validar-ativo")
    Boolean validarPacienteAtivo(@PathVariable("id") Long id);

    @GetMapping("/api/v1/medicos/{id}/validar-ativo")
    Boolean validarMedicoAtivo(@PathVariable("id") Long id);

    @GetMapping("/api/v1/convenios/{id}/validar-ativo")
    Boolean validarConvenioAtivo(@PathVariable("id") Long id);

    @GetMapping("/api/v1/pacientes/{id}")
    PacienteResposta buscarPacientePorId(@PathVariable("id") Long id);

    @GetMapping("/api/v1/medicos/{id}")
    MedicoResposta buscarMedicoPorId(@PathVariable("id") Long id);
}
```

### Configuração no application.yml
```yaml
servicos:
  admin-service:
    url: ${ADMIN_SERVICE_URL:http://localhost:8081}
  agendamento-service:
    url: ${AGENDAMENTO_SERVICE_URL:http://localhost:8082}
```

## 10.3 Uso no Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final AdminServiceClient adminServiceClient;

    public ConsultaResposta agendarConsulta(ConsultaRequisicao requisicao) {
        // 1. Validar paciente via Feign
        Boolean pacienteAtivo = adminServiceClient.validarPacienteAtivo(requisicao.getPacienteId());
        if (pacienteAtivo == null || !pacienteAtivo) {
            throw new RegraDeNegocioException("Paciente não encontrado ou inativo");
        }

        // 2. Validar médico via Feign
        Boolean medicoAtivo = adminServiceClient.validarMedicoAtivo(requisicao.getMedicoId());
        if (medicoAtivo == null || !medicoAtivo) {
            throw new RegraDeNegocioException("Médico não encontrado ou inativo");
        }

        // 3. Validar convênio (se paciente possui)
        PacienteResposta paciente = adminServiceClient.buscarPacientePorId(requisicao.getPacienteId());
        if (paciente.isPossuiConvenio()) {
            Boolean convenioAtivo = adminServiceClient.validarConvenioAtivo(paciente.getConvenioId());
            if (convenioAtivo == null || !convenioAtivo) {
                throw new RegraDeNegocioException("Convênio do paciente está inativo ou inválido");
            }
        }

        // 4. Validar conflito de horário
        validarConflitoHorario(requisicao.getMedicoId(), requisicao.getDataHora());

        // 5. Criar consulta
        Consulta consulta = ConsultaMapper.paraEntidade(requisicao);
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta = consultaRepository.save(consulta);

        log.info("Consulta agendada com sucesso: ID={}", consulta.getId());
        return ConsultaMapper.paraResposta(consulta);
    }
}
```

## 10.4 Timeouts e Retry

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connect-timeout: 5000
            read-timeout: 10000
            logger-level: FULL
```

## 10.5 Fallback

```java
@Component
public class AdminServiceClientFallback implements AdminServiceClient {
    @Override
    public Boolean validarPacienteAtivo(Long id) { return false; }
    @Override
    public Boolean validarMedicoAtivo(Long id) { return false; }
    @Override
    public Boolean validarConvenioAtivo(Long id) { return false; }
    @Override
    public PacienteResposta buscarPacientePorId(Long id) { return null; }
    @Override
    public MedicoResposta buscarMedicoPorId(Long id) { return null; }
}
```

## 10.6 ErrorDecoder

```java
@Component
public class DecodificadorErroFeign implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new RecursoNaoEncontradoException("Recurso não encontrado no serviço remoto");
            case 400 -> new RegraDeNegocioException("Requisição inválida para o serviço remoto");
            case 503 -> new IntegracaoException("Serviço temporariamente indisponível");
            default -> new IntegracaoException("Erro na comunicação: " + response.status());
        };
    }
}
```

---

# 11. AUTENTICAÇÃO E SEGURANÇA (JWT)

## 11.1 Fluxo JWT

```
1. Cliente → POST /api/v1/auth/login (usuario + senha)
2. admin-service valida credenciais → gera JWT
3. Cliente armazena token
4. Próximas requisições: Authorization: Bearer <token>
5. Cada serviço valida o token antes de processar
```

## 11.2 Roles Baseadas nos Atores UML

| Role (Perfil) | Permissões |
|---|---|
| `ADM` | Tudo — CRUD completo, relatórios, gerenciar perfis |
| `MEDICO` | Agenda própria, atendimentos, relatórios clínicos, encaminhamentos |
| `SECRETARIA` | Agendar/remarcar/cancelar consultas, cadastrar pacientes, consultar agenda |

## 11.3 JwtService

```java
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.chave-secreta}")
    private String chaveSecreta;

    @Value("${jwt.expiracao}")
    private long expiracao; // 86400000 (24h)

    public String gerarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("usuarioId", usuario.getId());
        claims.put("perfil", usuario.getPerfil().getNome());
        return construirToken(claims, usuario.getNomeUsuario(), expiracao);
    }

    private String construirToken(Map<String, Object> claims, String subject, long expiracao) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiracao))
                .signWith(obterChaveAssinatura(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValido(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(obterChaveAssinatura()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) { return false; }
    }

    public String extrairNomeUsuario(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public String extrairPerfil(String token) {
        return extrairClaim(token, claims -> claims.get("perfil", String.class));
    }

    private <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder().setSigningKey(obterChaveAssinatura())
                .build().parseClaimsJws(token).getBody();
        return resolver.apply(claims);
    }

    private Key obterChaveAssinatura() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(chaveSecreta));
    }
}
```

## 11.4 Security Config

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFiltroAutenticacao jwtFiltro;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADM")
                .requestMatchers("/api/v1/usuarios/**").hasRole("ADM")
                .requestMatchers("/api/v1/relatorios/**").hasRole("ADM")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

## 11.5 Filtro JWT

```java
@Component
@RequiredArgsConstructor
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        if (jwtService.isTokenValido(token)) {
            String usuario = jwtService.extrairNomeUsuario(token);
            String perfil = jwtService.extrairPerfil(token);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
```

---

# 12. TRATAMENTO GLOBAL DE EXCEÇÕES

## 12.1 Padrão de Resposta de Erro

```java
// commons/dto/RespostaErro.java
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class RespostaErro {
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private LocalDateTime timestamp;
    private List<ErroValidacao> errosValidacao;
}

@Data @AllArgsConstructor
public class ErroValidacao {
    private String campo;
    private String mensagem;
}
```

### Exemplo de resposta:
```json
{
  "status": 404,
  "erro": "Não Encontrado",
  "mensagem": "Paciente com ID 99 não encontrado",
  "caminho": "/api/v1/pacientes/99",
  "timestamp": "2026-05-09T10:30:00",
  "errosValidacao": null
}
```

## 12.2 Exceções Customizadas

```java
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String msg) { super(msg); }
}
public class RecursoDuplicadoException extends RuntimeException {
    public RecursoDuplicadoException(String msg) { super(msg); }
}
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String msg) { super(msg); }
}
public class IntegracaoException extends RuntimeException {
    public IntegracaoException(String msg) { super(msg); }
}
```

## 12.3 Handler Global

```java
@RestControllerAdvice
@Slf4j
public class TratadorGlobalExcecoes {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<RespostaErro> tratarDuplicado(RecursoDuplicadoException ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<RespostaErro> tratarNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(IntegracaoException.class)
    public ResponseEntity<RespostaErro> tratarIntegracao(IntegracaoException ex, HttpServletRequest req) {
        log.error("Falha de integração: {}", ex.getMessage());
        return construirResposta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErro> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroValidacao> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErroValidacao(fe.getField(), fe.getDefaultMessage())).toList();
        RespostaErro resposta = RespostaErro.builder()
                .status(400).erro("Requisição Inválida").mensagem("Erro de validação")
                .caminho(req.getRequestURI()).timestamp(LocalDateTime.now()).errosValidacao(erros).build();
        return ResponseEntity.badRequest().body(resposta);
    }

    private ResponseEntity<RespostaErro> construirResposta(HttpStatus status, String msg, HttpServletRequest req) {
        RespostaErro resp = RespostaErro.builder()
                .status(status.value()).erro(status.getReasonPhrase()).mensagem(msg)
                .caminho(req.getRequestURI()).timestamp(LocalDateTime.now()).build();
        return new ResponseEntity<>(resp, status);
    }
}
```

## 12.4 Mapa de Códigos HTTP

| Código | Quando | Exceção |
|---|---|---|
| `200` | Sucesso (GET, PUT) | — |
| `201` | Criado (POST) | — |
| `204` | Sem conteúdo (DELETE) | — |
| `400` | Dados inválidos | `MethodArgumentNotValidException` |
| `404` | Não encontrado | `RecursoNaoEncontradoException` |
| `409` | Duplicado (CPF) | `RecursoDuplicadoException` |
| `422` | Regra violada | `RegraDeNegocioException` |
| `503` | Serviço indisponível | `IntegracaoException` |

---

# 13. LOGS E OBSERVABILIDADE

## 13.1 Logbook — Logging HTTP Automático

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
    <version>3.7.0</version>
</dependency>
```

```yaml
logbook:
  filter.enabled: true
  format.style: json
  obfuscate:
    headers: [Authorization]
    parameters: [senha]
```

## 13.2 Correlation ID (Rastreamento entre serviços)

```java
@Component
public class FiltroCorrelationId extends OncePerRequestFilter {
    private static final String HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String correlationId = req.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);
        res.setHeader(HEADER, correlationId);
        try { chain.doFilter(req, res); } finally { MDC.remove("correlationId"); }
    }
}
```

### Propagar nas chamadas Feign:
```java
@Component
public class InterceptadorFeignCorrelation implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String id = MDC.get("correlationId");
        if (id != null) template.header("X-Correlation-ID", id);
    }
}
```

### Formato do log:
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{correlationId}] %-5level %logger{36} - %msg%n"
```

## 13.3 Boas Práticas de Log

| Nível | Quando | Exemplo |
|---|---|---|
| `ERROR` | Falha que impede operação | `log.error("Falha ao salvar", ex)` |
| `WARN` | Inesperado mas tratável | `log.warn("Paciente ID={} não encontrado", id)` |
| `INFO` | Operação de negócio | `log.info("Consulta agendada: ID={}", id)` |
| `DEBUG` | Detalhes técnicos | `log.debug("Validando CPF: {}", cpf)` |

**Regras:** NUNCA logar senhas/tokens. SEMPRE usar `@Slf4j` (Lombok). NUNCA `System.out.println`.

---

*Continua na Parte 5 → Testes, Swagger, Docker e Pipeline*

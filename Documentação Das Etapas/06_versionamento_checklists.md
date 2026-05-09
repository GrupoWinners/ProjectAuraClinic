# 🏥 PARTE 6 — VERSIONAMENTO, CHECKLISTS E RISCOS

---

# 19. ESTRATÉGIA DE VERSIONAMENTO

## 19.1 GitFlow

```
main ──────●───────────────────●──── (releases)
           │                   ▲
           ▼                   │ merge
develop ──●───●───●───●───●───●──── (integração)
              │       │   ▲
              ▼       ▼   │
        feature/A  feature/B
```

## 19.2 Workflow Passo a Passo

### Criar Feature Branch
```bash
git checkout develop
git pull origin develop
git checkout -b feature/admin-crud-medicos
```

### Trabalhar na Feature
```bash
git add .
git commit -m "feat(admin): criar entity Medico com anotações JPA"
git commit -m "feat(admin): implementar MedicoService com CRUD"
git commit -m "test(admin): adicionar testes unitários para MedicoService"
```

### Criar Pull Request
```bash
git push origin feature/admin-crud-medicos
# Abrir PR no GitHub: feature/admin-crud-medicos → develop
```

### Template de Pull Request
```markdown
## Descrição
Implementação do CRUD de Médicos no admin-service.

## O que foi feito
- [x] Entity Medico com anotações JPA
- [x] MedicoRepository
- [x] MedicoService com validações (CRM único)
- [x] MedicoController (REST endpoints)
- [x] DTOs (Requisição/Resposta) e Mapper
- [x] Testes unitários (85% cobertura)
- [x] Swagger annotations

## Como testar
1. Subir admin-db: `docker-compose up admin-db`
2. Rodar: `mvn spring-boot:run`
3. Acessar: `http://localhost:8081/swagger-ui.html`

## Checklist
- [x] Código segue padrões do projeto
- [x] Testes passando
- [x] Sem System.out.println
- [x] Tratamento de exceções
```

### Criar Release
```bash
git checkout develop && git pull origin develop
git checkout -b release/v1.0.0
# Ajustar versão nos pom.xml, testar
git commit -m "chore: preparar release v1.0.0"
git checkout main && git merge release/v1.0.0
git tag -a v1.0.0 -m "Release v1.0.0 - MVP Aura Clinic"
git push origin main --tags
git checkout develop && git merge release/v1.0.0
```

---

# 20. CHECKLISTS DE VALIDAÇÃO

## 20.1 Checklist Backend (por microsserviço)

- [ ] Entidades mapeadas com JPA (@Entity, @Table, @Column)
- [ ] Repositories criados com Spring Data JPA
- [ ] DTOs separados (Requisição/Resposta) — NUNCA expor Entity
- [ ] Mappers implementados (Entity ↔ DTO)
- [ ] Services com regras de negócio
- [ ] Controllers REST com endpoints padronizados
- [ ] Bean Validation (@NotBlank, @NotNull, @Valid)
- [ ] Soft delete (campo `ativo`)
- [ ] Paginação nos endpoints de listagem
- [ ] Lombok (@Data, @Builder, @RequiredArgsConstructor)
- [ ] Sem `System.out.println` — usar `@Slf4j`
- [ ] Sem lógica de negócio no Controller

## 20.2 Checklist Banco de Dados

- [ ] DDL de todas as tabelas
- [ ] Índices nos campos de busca frequente
- [ ] UNIQUE em CPF, CRM, CNPJ, nome_usuario
- [ ] Campos de auditoria (criado_em, atualizado_em)
- [ ] Dados iniciais (seeds)
- [ ] ENUM para campos com valores fixos
- [ ] FK constraints onde aplicável (mesmo banco)
- [ ] Nenhuma FK entre bancos de serviços diferentes

## 20.3 Checklist Endpoints

- [ ] Seguem `/api/v1/<recurso-plural>`
- [ ] Métodos HTTP corretos
- [ ] Status codes corretos (200, 201, 204, 400, 404, 409, 422)
- [ ] Respostas de erro padronizadas (RespostaErro)
- [ ] Paginação com page e size
- [ ] Testados no Postman (positivo + negativo)

## 20.4 Checklist Segurança

- [ ] JWT implementado
- [ ] Senhas com BCrypt
- [ ] Endpoints públicos definidos (login, swagger)
- [ ] Endpoints protegidos exigem Bearer token
- [ ] Roles configuradas (ADM, MEDICO, SECRETARIA)
- [ ] Dados sensíveis não expostos em logs

## 20.5 Checklist Docker

- [ ] Dockerfile funcional (multi-stage)
- [ ] Docker Compose com 3 bancos + 3 serviços
- [ ] Volumes para persistência MySQL
- [ ] Rede bridge entre containers
- [ ] Healthcheck nos bancos
- [ ] Variáveis de ambiente configuradas
- [ ] `docker-compose up -d --build` funciona

## 20.6 Checklist Testes

- [ ] Testes unitários dos Services (Mockito)
- [ ] Testes de integração dos Controllers (MockMvc)
- [ ] Cenários positivos e negativos
- [ ] application-test.yml com H2
- [ ] Mocks de Feign Clients nos testes
- [ ] Cobertura mínima 70%
- [ ] `mvn test` passa sem erros

## 20.7 Checklist Documentação

- [ ] README.md completo
- [ ] Swagger configurado em todos os serviços
- [ ] Endpoints documentados com @Operation
- [ ] Postman collections exportadas
- [ ] Instruções de setup no README

## 20.8 Checklist UML — Validação do Escopo

### Módulo Administrativo
- [ ] ✅ Gerenciar Médicos
- [ ] ✅ Gerenciar Pacientes
- [ ] ✅ Gerenciar Especialidades
- [ ] ✅ Gerenciar Perfis (Usuários/permissões)
- [ ] ✅ Gerenciar Convênios
- [ ] ✅ Gerenciar Relatórios

### Módulo de Agendamento
- [ ] ✅ Agendar Consulta (com Validar Convênio)
- [ ] ✅ Remarcar Consulta
- [ ] ✅ Cancelar Consulta
- [ ] ✅ Consultar Agenda
- [ ] ✅ Definir Disponibilidade
- [ ] ✅ Bloquear Agenda

### Módulo de Atendimento
- [ ] ✅ Agendar Reconsulta
- [ ] ✅ Criar Relatório Clínico (com Escopo Médico + Consultar/Editar)
- [ ] ✅ Salvar Dados do Paciente
- [ ] ✅ Atribuir a Outro Médico
- [ ] ✅ Definir Urgência

---

# 21. RISCOS TÉCNICOS

## 21.1 Mapa de Riscos

| # | Risco | Prob. | Impacto | Mitigação |
|---|---|---|---|---|
| 1 | **Serviço indisponível** — Feign falha | Alta | Alto | Fallback, retry, timeout |
| 2 | **Inconsistência de dados** — Paciente deletado mas consulta existe | Média | Alto | Soft delete; validar status via Feign |
| 3 | **Conflito de horário** — Duas requisições simultâneas | Média | Alto | UNIQUE INDEX (medico_id, data_hora); tratar constraint violation |
| 4 | **Bloqueio não verificado** — Agendamento em período bloqueado | Média | Alto | Validar bloqueios antes de agendar |
| 5 | **Acoplamento Feign** — Mudança na API quebra outro serviço | Alta | Médio | Versionamento (/v1/); contratos claros |
| 6 | **JWT secret fraco** — Token forjado | Média | Crítico | Secret 256 bits; variável de ambiente |
| 7 | **N+1 queries** — JPA carrega relações em loop | Alta | Médio | @EntityGraph ou JOIN FETCH |
| 8 | **Docker: portas em conflito** | Média | Baixo | Documentar portas; variáveis de ambiente |
| 9 | **Dados de teste em produção** | Baixa | Alto | Spring Profiles (dev/prod) |
| 10 | **Deadlock no banco** | Baixa | Alto | Transações curtas; HikariCP |

## 21.2 Mitigações Detalhadas

### Conflito de Horário (Race Condition)
```java
@Transactional
public ConsultaResposta agendarConsulta(ConsultaRequisicao requisicao) {
    try {
        // ... validações ...
        return consultaRepository.save(consulta);
    } catch (DataIntegrityViolationException e) {
        throw new RegraDeNegocioException("Horário já ocupado para este médico");
    }
}
```

### Bloqueio de Agenda
```java
private void validarBloqueioAgenda(Long medicoId, LocalDateTime dataHora) {
    List<BloqueioAgenda> bloqueios = bloqueioRepository
        .findByMedicoIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
            medicoId, dataHora, dataHora);
    if (!bloqueios.isEmpty()) {
        throw new RegraDeNegocioException(
            "Médico possui bloqueio de agenda neste período: " + bloqueios.get(0).getMotivo());
    }
}
```

---

*Continua na Parte 7 → Escalabilidade, Melhorias Futuras e Boas Práticas*

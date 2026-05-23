package br.com.clinica.admin.controller;

import br.com.clinica.admin.dto.requisicao.MedicoRequisicao;
import br.com.clinica.admin.dto.resposta.MedicoResposta;
import br.com.clinica.admin.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller REST responsável pelos endpoints de CRUD de médicos no admin-service.
// Delega toda a lógica de negócio para o MedicoService — nunca contém regras aqui.
@RestController
@RequestMapping("/api/v1/medicos")
@RequiredArgsConstructor
@Tag(name = "Médicos", description = "Endpoints para gerenciamento de médicos da clínica")
public class MedicoController {

    private final MedicoService medicoService;

    @PostMapping
    @Operation(summary = "Cadastrar médico", description = "Cria um novo médico vinculado a uma especialidade existente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Médico criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
        @ApiResponse(responseCode = "409", description = "CRM já cadastrado")
    })
    public ResponseEntity<MedicoResposta> criarMedico(@Valid @RequestBody MedicoRequisicao requisicao) {
        MedicoResposta resposta = medicoService.criarMedico(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar médicos", description = "Retorna lista paginada de médicos ativos")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<MedicoResposta>> listarMedicos(
            @PageableDefault(size = 20, sort = "nomeCompleto") Pageable pageable) {
        return ResponseEntity.ok(medicoService.listarMedicos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar médico por ID", description = "Retorna os dados de um médico específico pelo seu ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Médico encontrado"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    })
    public ResponseEntity<MedicoResposta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.buscarPorId(id));
    }

    @GetMapping("/crm/{crm}")
    @Operation(summary = "Buscar médico por CRM", description = "Retorna os dados de um médico pelo número do CRM")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Médico encontrado"),
        @ApiResponse(responseCode = "404", description = "Médico com o CRM informado não encontrado")
    })
    public ResponseEntity<MedicoResposta> buscarPorCrm(@PathVariable String crm) {
        return ResponseEntity.ok(medicoService.buscarPorCrm(crm));
    }

    @GetMapping("/especialidade/{especialidadeId}")
    @Operation(summary = "Listar médicos por especialidade", description = "Retorna lista paginada de médicos de uma especialidade específica")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<MedicoResposta>> listarPorEspecialidade(
            @PathVariable Long especialidadeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(medicoService.listarPorEspecialidade(especialidadeId, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar médico", description = "Atualiza os dados de um médico existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Médico atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado"),
        @ApiResponse(responseCode = "409", description = "CRM já cadastrado para outro médico")
    })
    public ResponseEntity<MedicoResposta> atualizarMedico(
            @PathVariable Long id,
            @Valid @RequestBody MedicoRequisicao requisicao) {
        return ResponseEntity.ok(medicoService.atualizarMedico(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar médico", description = "Realiza soft delete — marca o médico como inativo sem remover do banco")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Médico desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    })
    public ResponseEntity<Void> desativarMedico(@PathVariable Long id) {
        medicoService.desativarMedico(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/validar-ativo")
    @Operation(summary = "Validar se médico está ativo", description = "Endpoint de integração usado pelo agendamento-service via Feign")
    @ApiResponse(responseCode = "200", description = "Resultado da validação (true/false)")
    public ResponseEntity<Boolean> validarMedicoAtivo(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.validarMedicoAtivo(id));
    }
}

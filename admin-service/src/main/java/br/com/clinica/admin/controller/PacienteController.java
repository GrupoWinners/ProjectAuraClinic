package br.com.clinica.admin.controller;

import br.com.clinica.admin.dto.requisicao.PacienteRequisicao;
import br.com.clinica.admin.dto.resposta.PacienteResposta;
import br.com.clinica.admin.service.PacienteService;
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

// Controller REST responsável pelos endpoints de CRUD de pacientes no admin-service.
// Delega toda a lógica de negócio para o PacienteService — nunca contém regras aqui.
@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Endpoints para gerenciamento de pacientes da clínica")
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    @Operation(summary = "Cadastrar paciente", description = "Cria um novo paciente com dados pessoais, endereço e vínculo opcional com convênio")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Paciente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
        @ApiResponse(responseCode = "409", description = "CPF ou RG já cadastrado")
    })
    public ResponseEntity<PacienteResposta> criarPaciente(@Valid @RequestBody PacienteRequisicao requisicao) {
        PacienteResposta resposta = pacienteService.criarPaciente(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar pacientes", description = "Retorna lista paginada de pacientes ativos")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<PacienteResposta>> listarPacientes(
            @PageableDefault(size = 20, sort = "nomeCompleto") Pageable pageable) {
        return ResponseEntity.ok(pacienteService.listarPacientes(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID", description = "Retorna os dados completos de um paciente específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<PacienteResposta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar paciente por CPF", description = "Retorna os dados de um paciente pelo número do CPF")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
        @ApiResponse(responseCode = "404", description = "Paciente com o CPF informado não encontrado")
    })
    public ResponseEntity<PacienteResposta> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(pacienteService.buscarPorCpf(cpf));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar pacientes por nome", description = "Retorna lista paginada de pacientes filtrados por nome (busca parcial)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<PacienteResposta>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pacienteService.buscarPorNome(nome, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar paciente", description = "Atualiza os dados de um paciente existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paciente atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado"),
        @ApiResponse(responseCode = "409", description = "CPF ou RG já cadastrado para outro paciente")
    })
    public ResponseEntity<PacienteResposta> atualizarPaciente(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequisicao requisicao) {
        return ResponseEntity.ok(pacienteService.atualizarPaciente(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar paciente", description = "Realiza soft delete — marca o paciente como inativo sem remover do banco")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Paciente desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<Void> desativarPaciente(@PathVariable Long id) {
        pacienteService.desativarPaciente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/validar-ativo")
    @Operation(summary = "Validar se paciente está ativo", description = "Endpoint de integração usado pelo agendamento-service via Feign")
    @ApiResponse(responseCode = "200", description = "Resultado da validação (true/false)")
    public ResponseEntity<Boolean> validarPacienteAtivo(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.validarPacienteAtivo(id));
    }
}

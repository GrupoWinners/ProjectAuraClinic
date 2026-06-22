package br.com.clinica.admin.controller;

import br.com.clinica.admin.dto.requisicao.EspecialidadeRequisicao;
import br.com.clinica.admin.dto.resposta.EspecialidadeResposta;
import br.com.clinica.admin.service.EspecialidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller REST responsável pelos endpoints de CRUD de especialidades médicas.
// Delega toda a lógica de negócio para o EspecialidadeService.
@RestController
@RequestMapping("/api/v1/especialidades")
@RequiredArgsConstructor
@Tag(name = "Especialidades", description = "Endpoints para gerenciamento de especialidades médicas")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    @PostMapping
    @Operation(summary = "Criar especialidade", description = "Cadastra uma nova especialidade médica (ex: Cardiologia, Neurologia)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Especialidade criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
        @ApiResponse(responseCode = "409", description = "Especialidade com mesma descrição já existe")
    })
    public ResponseEntity<EspecialidadeResposta> criarEspecialidade(@Valid @RequestBody EspecialidadeRequisicao requisicao) {
        EspecialidadeResposta resposta = especialidadeService.criarEspecialidade(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar especialidades", description = "Retorna todas as especialidades ativas disponíveis")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<EspecialidadeResposta>> listarEspecialidades() {
        return ResponseEntity.ok(especialidadeService.listarEspecialidades());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar especialidade por ID", description = "Retorna os dados de uma especialidade específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Especialidade encontrada"),
        @ApiResponse(responseCode = "404", description = "Especialidade não encontrada")
    })
    public ResponseEntity<EspecialidadeResposta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadeService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar especialidade", description = "Atualiza a descrição de uma especialidade existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Especialidade atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Especialidade não encontrada"),
        @ApiResponse(responseCode = "409", description = "Descrição já cadastrada em outra especialidade")
    })
    public ResponseEntity<EspecialidadeResposta> atualizarEspecialidade(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadeRequisicao requisicao) {
        return ResponseEntity.ok(especialidadeService.atualizarEspecialidade(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar especialidade", description = "Realiza soft delete — bloqueia se houver médicos ativos vinculados")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Especialidade desativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Especialidade não encontrada"),
        @ApiResponse(responseCode = "422", description = "Não é possível desativar — possui médicos ativos vinculados")
    })
    public ResponseEntity<Void> desativarEspecialidade(@PathVariable Long id) {
        especialidadeService.desativarEspecialidade(id);
        return ResponseEntity.noContent().build();
    }
}

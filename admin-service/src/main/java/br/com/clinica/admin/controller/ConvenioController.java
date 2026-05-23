package br.com.clinica.admin.controller;

import br.com.clinica.admin.dto.requisicao.ConvenioRequisicao;
import br.com.clinica.admin.dto.resposta.ConvenioResposta;
import br.com.clinica.admin.service.ConvenioService;
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

// Controller REST responsável pelos endpoints de CRUD de convênios médicos.
// Delega toda a lógica de negócio para o ConvenioService.
@RestController
@RequestMapping("/api/v1/convenios")
@RequiredArgsConstructor
@Tag(name = "Convênios", description = "Endpoints para gerenciamento de convênios médicos")
public class ConvenioController {

    private final ConvenioService convenioService;

    @PostMapping
    @Operation(summary = "Criar convênio", description = "Cadastra um novo convênio médico com CNPJ único")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Convênio criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
        @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<ConvenioResposta> criarConvenio(@Valid @RequestBody ConvenioRequisicao requisicao) {
        ConvenioResposta resposta = convenioService.criarConvenio(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar convênios", description = "Retorna lista paginada de convênios ativos")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<ConvenioResposta>> listarConvenios(
            @PageableDefault(size = 20, sort = "nomeEmpresa") Pageable pageable) {
        return ResponseEntity.ok(convenioService.listarConvenios(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar convênio por ID", description = "Retorna os dados de um convênio específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Convênio encontrado"),
        @ApiResponse(responseCode = "404", description = "Convênio não encontrado")
    })
    public ResponseEntity<ConvenioResposta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(convenioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar convênio", description = "Atualiza os dados de um convênio existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Convênio atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Convênio não encontrado"),
        @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado para outro convênio")
    })
    public ResponseEntity<ConvenioResposta> atualizarConvenio(
            @PathVariable Long id,
            @Valid @RequestBody ConvenioRequisicao requisicao) {
        return ResponseEntity.ok(convenioService.atualizarConvenio(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar convênio", description = "Realiza soft delete — marca o convênio como inativo sem remover do banco")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Convênio desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Convênio não encontrado")
    })
    public ResponseEntity<Void> desativarConvenio(@PathVariable Long id) {
        convenioService.desativarConvenio(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/validar-ativo")
    @Operation(summary = "Validar se convênio está ativo", description = "Endpoint de integração usado pelo agendamento-service via Feign")
    @ApiResponse(responseCode = "200", description = "Resultado da validação (true/false)")
    public ResponseEntity<Boolean> validarConvenioAtivo(@PathVariable Long id) {
        return ResponseEntity.ok(convenioService.validarConvenioAtivo(id));
    }
}

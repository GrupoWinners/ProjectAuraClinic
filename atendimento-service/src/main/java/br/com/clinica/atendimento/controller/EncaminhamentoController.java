package br.com.clinica.atendimento.controller;

import br.com.clinica.atendimento.dto.requisicao.AtualizarStatusEncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.EncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.resposta.EncaminhamentoResposta;
import br.com.clinica.atendimento.service.EncaminhamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/encaminhamentos")
@RequiredArgsConstructor
@Tag(name = "Encaminhamentos", description = "Operações relacionadas ao encaminhamento de pacientes para outros médicos")
public class EncaminhamentoController {

    private final EncaminhamentoService encaminhamentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar encaminhamento", description = "Cria um encaminhamento de atendimento para outro médico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Encaminhamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Atendimento ou médico destino não encontrado"),
            @ApiResponse(responseCode = "422", description = "Médico destino inativo ou inválido"),
            @ApiResponse(responseCode = "503", description = "Falha de integração com admin-service")
    })
    public EncaminhamentoResposta criar(@RequestBody @Valid EncaminhamentoRequisicao requisicao) {
        return encaminhamentoService.criar(requisicao);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar encaminhamento por ID", description = "Retorna um encaminhamento específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encaminhamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Encaminhamento não encontrado")
    })
    public EncaminhamentoResposta buscarPorId(@PathVariable Long id) {
        return encaminhamentoService.buscarPorId(id);
    }

    @GetMapping("/medico-destino/{medicoId}")
    @Operation(summary = "Listar encaminhamentos por médico destino", description = "Retorna os encaminhamentos recebidos por um médico.")
    @ApiResponse(responseCode = "200", description = "Lista de encaminhamentos retornada com sucesso")
    public List<EncaminhamentoResposta> listarPorMedicoDestino(@PathVariable Long medicoId) {
        return encaminhamentoService.listarPorMedicoDestino(medicoId);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status do encaminhamento", description = "Atualiza o status de um encaminhamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Encaminhamento não encontrado")
    })
    public EncaminhamentoResposta atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusEncaminhamentoRequisicao requisicao
    ) {
        return encaminhamentoService.atualizarStatus(id, requisicao);
    }
}
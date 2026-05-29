package br.com.clinica.atendimento.controller;

import br.com.clinica.atendimento.dto.requisicao.AtendimentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.AtualizarUrgenciaRequisicao;
import br.com.clinica.atendimento.dto.resposta.AtendimentoResposta;
import br.com.clinica.atendimento.service.AtendimentoService;
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
@RequestMapping("/api/v1/atendimentos")
@RequiredArgsConstructor
@Tag(name = "Atendimentos", description = "Operações relacionadas ao registro e manutenção de atendimentos clínicos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar atendimento", description = "Registra um atendimento clínico vinculado a uma consulta e cria o prontuário automaticamente quando necessário.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Atendimento registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Paciente, médico ou consulta não encontrado"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada"),
            @ApiResponse(responseCode = "503", description = "Falha de integração com outro microsserviço")
    })
    public AtendimentoResposta registrar(@RequestBody @Valid AtendimentoRequisicao requisicao) {
        return atendimentoService.registrar(requisicao);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar atendimento por ID", description = "Retorna os dados de um atendimento específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atendimento encontrado"),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    })
    public AtendimentoResposta buscarPorId(@PathVariable Long id) {
        return atendimentoService.buscarPorId(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar atendimentos por paciente", description = "Retorna o histórico de atendimentos de um paciente.")
    @ApiResponse(responseCode = "200", description = "Lista de atendimentos retornada com sucesso")
    public List<AtendimentoResposta> listarPorPaciente(@PathVariable Long pacienteId) {
        return atendimentoService.listarPorPaciente(pacienteId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar atendimento", description = "Atualiza dados clínicos de um atendimento existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atendimento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    })
    public AtendimentoResposta atualizar(
            @PathVariable Long id,
            @RequestBody @Valid AtendimentoRequisicao requisicao
    ) {
        return atendimentoService.atualizar(id, requisicao);
    }

    @PutMapping("/{id}/urgencia")
    @Operation(summary = "Atualizar urgência", description = "Atualiza o nível de urgência/classificação de risco do atendimento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Urgência atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    })
    public AtendimentoResposta atualizarUrgencia(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarUrgenciaRequisicao requisicao
    ) {
        return atendimentoService.atualizarUrgencia(id, requisicao);
    }
}
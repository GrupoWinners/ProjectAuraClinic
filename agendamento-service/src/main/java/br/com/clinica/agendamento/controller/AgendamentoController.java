package br.com.clinica.agendamento.controller;

import br.com.clinica.agendamento.dto.ConsultaRequisicao;
import br.com.clinica.agendamento.dto.CancelamentoRequisicao;
import br.com.clinica.agendamento.dto.ConsultaResposta;
import br.com.clinica.agendamento.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Endpoints para agendamento, remarcação e cancelamento de consultas médicas")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    @Operation(summary = "Agendar consulta", description = "Registra um novo agendamento de consulta médica após validar médico e paciente.")
    public ResponseEntity<ConsultaResposta> agendar(@RequestBody @Valid ConsultaRequisicao requisicao) {
        ConsultaResposta resposta = agendamentoService.agendarConsulta(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/{id}/remarcar")
    @Operation(summary = "Remarcar consulta", description = "Altera o horário e/ou a data de uma consulta agendada anteriormente.")
    public ResponseEntity<ConsultaResposta> remarcar(
            @PathVariable("id") Long id,
            @RequestBody @Valid ConsultaRequisicao requisicao) {
        ConsultaResposta resposta = agendamentoService.remarcarConsulta(id, requisicao);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar consulta", description = "Cancela uma consulta agendada registrando o motivo do cancelamento.")
    public ResponseEntity<Void> cancelar(
            @PathVariable("id") Long id,
            @RequestBody @Valid CancelamentoRequisicao requisicao) {
        agendamentoService.cancelarConsulta(id, requisicao);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar todas as consultas", description = "Retorna o histórico/lista de todas as consultas cadastradas.")
    public ResponseEntity<List<ConsultaResposta>> listarTodas() {
        List<ConsultaResposta> consultas = agendamentoService.listarConsultas();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar consulta por ID", description = "Retorna os detalhes de uma consulta médica específica.")
    public ResponseEntity<ConsultaResposta> buscarPorId(@PathVariable("id") Long id) {
        ConsultaResposta resposta = agendamentoService.buscarPorId(id);
        return ResponseEntity.ok(resposta);
    }
}
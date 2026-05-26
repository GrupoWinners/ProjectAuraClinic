package br.com.clinica.agendamento.controller;

import br.com.clinica.agendamento.dto.ConsultaRequisicao;
import br.com.clinica.agendamento.dto.CancelamentoRequisicao;
import br.com.clinica.agendamento.dto.ConsultaResposta;
import br.com.clinica.agendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultas")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<ConsultaResposta> agendar(@RequestBody @Valid ConsultaRequisicao requisicao) {
        ConsultaResposta resposta = agendamentoService.agendarConsulta(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/{id}/remarcar")
    public ResponseEntity<ConsultaResposta> remarcar(
            @PathVariable Long id,
            @RequestBody @Valid ConsultaRequisicao requisicao) {
        ConsultaResposta resposta = agendamentoService.remarcarConsulta(id, requisicao);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @RequestBody @Valid CancelamentoRequisicao requisicao) {
        agendamentoService.cancelarConsulta(id, requisicao);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResposta>> listarTodas() {
        List<ConsultaResposta> consultas = agendamentoService.listarConsultas();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResposta> buscarPorId(@PathVariable Long id) {
        ConsultaResposta resposta = agendamentoService.buscarPorId(id);
        return ResponseEntity.ok(resposta);
    }
}
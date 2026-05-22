package br.com.clinica.agendamento.controller;

import br.com.clinica.agendamento.dto.CancelamentoRequest;
import br.com.clinica.agendamento.dto.ConsultaRequest;
import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.service.AgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    // Rota para Agendar Consulta
    @PostMapping
    public ResponseEntity<Consulta> agendar(@RequestBody ConsultaRequest request) {
        Consulta consulta = agendamentoService.agendarConsulta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(consulta);
    }

    // Rota para Remarcar Consulta
    @PutMapping("/{id}/remarcar")
    public ResponseEntity<Consulta> remarcar(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime novaDataHora) {
        Consulta consultaUpdated = agendamentoService.remarcarConsulta(id, novaDataHora);
        return ResponseEntity.ok(consultaUpdated);
    }

    // Rota para Cancelar Consulta
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @RequestBody CancelamentoRequest request) {
        agendamentoService.cancelarConsulta(id, request);
        return ResponseEntity.noContent().build();
    }
}
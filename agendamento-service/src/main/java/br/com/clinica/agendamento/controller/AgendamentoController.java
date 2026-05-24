package br.com.clinica.agendamento.controller;

import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.service.AgendamentoService;
import br.com.clinica.agendamento.dto.ConsultaRequest;
import br.com.clinica.agendamento.dto.CancelamentoRequest;
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
    public ResponseEntity<Consulta> agendar(@RequestBody @Valid ConsultaRequest request) {
        Consulta consulta = agendamentoService.agendarConsulta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(consulta);
    }

    @PutMapping("/{id}/remarcar")
    public ResponseEntity<Consulta> remarcar(
            @PathVariable Long id,
            @RequestBody @Valid ConsultaRequest request) {
        Consulta novaConsulta = agendamentoService.remarcarConsulta(id, request);
        return ResponseEntity.ok(novaConsulta);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @RequestBody @Valid CancelamentoRequest request) {
        agendamentoService.cancelarConsulta(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Consulta>> listarTodas() {
        List<Consulta> consultas = agendamentoService.listarConsultas();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consulta> buscarPorId(@PathVariable Long id) {
        Consulta consulta = agendamentoService.buscarPorId(id);
        return ResponseEntity.ok(consulta);
    }
}
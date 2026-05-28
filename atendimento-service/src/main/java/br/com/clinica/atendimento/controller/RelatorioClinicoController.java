package br.com.clinica.atendimento.controller;

import br.com.clinica.atendimento.dto.requisicao.RelatorioClinicoRequisicao;
import br.com.clinica.atendimento.dto.resposta.RelatorioClinicoResposta;
import br.com.clinica.atendimento.service.RelatorioClinicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relatorios-clinicos")
@RequiredArgsConstructor
public class RelatorioClinicoController {

    private final RelatorioClinicoService relatorioClinicoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RelatorioClinicoResposta criar(@RequestBody @Valid RelatorioClinicoRequisicao requisicao) {
        return relatorioClinicoService.criar(requisicao);
    }

    @GetMapping("/{id}")
    public RelatorioClinicoResposta buscarPorId(@PathVariable Long id) {
        return relatorioClinicoService.buscarPorId(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<RelatorioClinicoResposta> listarPorPaciente(@PathVariable Long pacienteId) {
        return relatorioClinicoService.listarPorPaciente(pacienteId);
    }

    @PutMapping("/{id}")
    public RelatorioClinicoResposta atualizar(
            @PathVariable Long id,
            @RequestBody @Valid RelatorioClinicoRequisicao requisicao
    ) {
        return relatorioClinicoService.atualizar(id, requisicao);
    }
}